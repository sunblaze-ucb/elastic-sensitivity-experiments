name := "elastic-sensitivity-experiments"

version := "1.0"

scalaVersion := "2.12.2"

mainClass := Some("Experiment")

libraryDependencies += "com.uber.engsec" % "sql-differential-privacy" % "0.1.1"
