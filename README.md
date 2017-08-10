# Overview

This class calculates error of elastic sensitivity-based differential privacy for queries from the [TCP-H benchmark](http://www.tpc.org/tpc_documents_current_versions/pdf/tpc-h_v2.17.2.pdf).

## Building & Running

```
sbt run
```
## Generating benchmark data
The /data directory contains the generated SQL queries and results of each query executed on a populated database
with scale factor 1.

If you want to regenerate the queries and database from scratch, see the instructions in
data/README.txt. You will need to import the data into a relational database to execute
the benchmark queries.
