================================================================================

To get and build dbgen and qgen:

git clone https://github.com/electrum/tpch-dbgen.git

Copy the makefile.suite to Makefile and make these changes:

CC      = gcc
DATABASE= SQLSERVER
MACHINE = LINUX
WORKLOAD = TPCH

Run 'make'.

================================================================================
To generate the data, run './dbgen'. Pass -s N where N is the "scale
factor" of the desired data. With 1 (default), you get 1GB data.

To load the data, use the 'create-tables.sql' script.

================================================================================
To generate queries, run:

export DSS_QUERY=~/tpch-dbgen/queries
./qgen > queries.sql

================================================================================
Queries to compute metrics for join columns:

13:
SELECT COUNT(c_custkey) FROM customer GROUP BY c_custkey ORDER BY count DESC LIMIT 1;
SELECT COUNT(o_custkey) FROM orders GROUP BY o_custkey ORDER BY count DESC LIMIT 1;

16:
SELECT COUNT(p_partkey) FROM part GROUP BY p_partkey ORDER BY count DESC LIMIT 1;
SELECT COUNT(ps_partkey) FROM partsupp GROUP BY ps_partkey ORDER BY count DESC LIMIT 1;

21:
SELECT COUNT(s_suppkey) FROM supplier GROUP BY s_suppkey ORDER BY count DESC LIMIT 1;
SELECT COUNT(l_suppkey) FROM lineitem GROUP BY l_suppkey ORDER BY count DESC LIMIT 1;
SELECT COUNT(o_orderkey) FROM orders GROUP BY o_orderkey ORDER BY count DESC LIMIT 1;
SELECT COUNT(l_orderkey) FROM lineitem GROUP BY l_orderkey ORDER BY count DESC LIMIT 1;
SELECT COUNT(s_nationkey) FROM supplier GROUP BY s_nationkey ORDER BY count DESC LIMIT 1;
SELECT COUNT(n_nationkey) FROM nation GROUP BY n_nationkey ORDER BY count DESC LIMIT 1;

================================================================================
