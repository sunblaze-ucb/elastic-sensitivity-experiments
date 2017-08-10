import java.io.{BufferedInputStream, File, FileInputStream}

import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.{CsvMapper, CsvParser, CsvSchema}
import com.uber.engsec.dp.analysis.differential_privacy.{ElasticSensitivityAnalysis, SensitivityInfo}
import com.uber.engsec.dp.dataflow.column.AbstractColumnAnalysis.ColumnFacts
import com.uber.engsec.dp.sql.QueryParser
import com.uber.engsec.dp.sql.relational_algebra.Relation
import scala.collection.mutable

object Experiment {
  import scala.collection.JavaConverters._

  /** How many simulations to run (for each cell) when calculating mean error. */
  val NUM_SIMULATIONS: Int = 100

  /** Privacy budget */
  val EPSILON: Double = 0.1

  /** Returns pair (median error, median coverage) for each aggregated column in the results. */
  def getColumnErrors(queryResults: Iterator[Array[String]],
                      coverageResults: Iterator[Array[String]],
                      columnSensitivities: Seq[Double]): List[(Double, Double)] = {

    // Ignore histogram bins (sensitivity = 0)
    val aggregatedCols = columnSensitivities.zipWithIndex.filter{ case (sensitivity, _) => sensitivity > 0 }

    // Iterate through results to compute relative error for each cell
    val errors = queryResults.map { row =>
      aggregatedCols.map { case (smoothElasticSensitivity, colIdx) =>

        val sensitiveResult = row(colIdx).toDouble
        val noiseScale = 2 * smoothElasticSensitivity / EPSILON

        // sample the error NUM_SAMPLES times and compute the mean
        val errors = (1 to NUM_SIMULATIONS).map { _ =>
          val noisyResult = sensitiveResult + laplace(noiseScale)
          Math.abs((noisyResult - sensitiveResult) / sensitiveResult)
        }

        errors.sum / NUM_SIMULATIONS
      }
    }.toList.transpose // from row-oriented to column-oriented list of lists

    val coverages = coverageResults.map { row =>
      aggregatedCols.map { case (sensitivity, colIdx) => row(colIdx).toDouble }
    }.toList.transpose

    val medianErrors = errors.map { median }
    val medianCoverages = coverages.map { median }

    medianErrors.zip(medianCoverages)
  }

  /** Generate Laplace noise centered at 0 with the given scale. */
  def laplace(scale: Double): Double = {
    val u = 0.5 - scala.util.Random.nextDouble()
    -math.signum(u) * scale * math.log(1 - 2*math.abs(u))
  }

  /** Compute the median value. */
  def median(s: Seq[Double]): Double = {
    val (lower, upper) = s.sortWith(_ < _).splitAt(s.size / 2)
    if (s.size % 2 == 0) (lower.last + upper.head) / 2.0 else upper.head
  }

  /** Calculates the smooth sensitivity for each output column. */
  def getSmoothSensitivities(tree: Relation): Seq[Double] = {

    val analysis = new ElasticSensitivityAnalysis()
    // cache elastic sensitivity results at k so we don't need to re-run analysis when processing the next column
    val analysisResultsAtK = new mutable.HashMap[Int, ColumnFacts[SensitivityInfo]]

    /** Calculates the smooth elastic sensitivity by recursively computing smooth sensitivity for each value of k
      * until the function decreases at k+1. Since elastic sensitivity increases polynomially (at worst) in k while the
      * smoothing factor decays exponentially in k, this provides the correct (maximum) smooth sensitivity without
      * requiring computation for every k up to the size of the database.
      */
    def smoothSensitivity(colIdx: Int, k: Int, prevSensitivity: Double): Double = {
      val analysisResultAtK = analysisResultsAtK.getOrElseUpdate(k, {
        analysis.setK(k)
        analysis.analyzeQuery(tree)
      })

      val elasticSensitivityAtK = analysisResultAtK(colIdx).sensitivity.get
      val smoothSensitivityAtK = Math.exp(-k * EPSILON) * elasticSensitivityAtK

      if (smoothSensitivityAtK <= prevSensitivity)
        prevSensitivity
      else
        smoothSensitivity(colIdx, k+1, smoothSensitivityAtK)
    }

    val numCols = tree.unwrap.getRowType.getFieldCount
    (0 until numCols).map { smoothSensitivity(_, 0, 0) }
  }

  def main(args: Array[String]): Unit = {
    System.setProperty("schema.config.path", "src/main/resources/schema.yaml")
    System.setProperty("dp.check_bins", "false")

    val resultFiles = getFileList("data/results").filter { _.endsWith(".sql") }

    println("Query\tCoverage\tError")

    resultFiles.foreach { file =>
      val fileName = new File(file).getName

      val query = scala.io.Source.fromFile(file).mkString
      val tree = QueryParser.parseToRelTree(query)

      val sensitivityResults = getSmoothSensitivities(tree)

      val queryResults = parseCSV(file.replace(".sql", ".csv"))
      val coverageResults = parseCSV(file.replace(".sql", "_coverage.csv"))

      // skip header
      queryResults.next
      coverageResults.next

      val results = getColumnErrors(queryResults, coverageResults, sensitivityResults)
      val colNames = tree.unwrap.getRowType.getFieldNames

      // print results to stdout
      results.foreach { case (err, cov) =>
        println( "%s\t%-8.0f\t%f".format(fileName, cov, err) )
      }
    }
  }

  /** Returns an iterator over rows in the given CSV file. */
  def parseCSV(fileName: String): Iterator[Array[String]] = {
    try {
      val bis = new BufferedInputStream(new FileInputStream(fileName))
      val mapper = new CsvMapper
      val schema = CsvSchema.builder.setColumnSeparator(',').build
      mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY)
      val it: MappingIterator[Array[String]] = mapper.readerFor(classOf[Array[String]]).`with`(schema).readValues(bis)
      it.asScala

    } catch {
      case e: Exception => throw new RuntimeException(e)
    }
  }

  def getFileList(dir: String): Seq[String] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory)
      d.listFiles.filter(_.isFile).map{_.getAbsolutePath}
    else
      Nil
  }
}
