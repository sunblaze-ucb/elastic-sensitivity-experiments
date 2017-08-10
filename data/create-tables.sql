--------------------------------------------------------------------------------
-- Script to load data from TPC-H benchmark
-- Generate data using ./dbgen (this results in .tbl files)
-- Copy the .tbl files to /tpch-experiments/data/
-- Then run this script to load the data into vertica
--------------------------------------------------------------------------------

DROP TABLE IF EXISTS part;
DROP TABLE IF EXISTS supplier;
DROP TABLE IF EXISTS partsupp;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS lineitem;
DROP TABLE IF EXISTS nation;
DROP TABLE IF EXISTS region;

CREATE TABLE part (
  P_PARTKEY integer PRIMARY KEY,
  P_NAME varchar(55),
  P_MFGR varchar(25),
  P_BRAND varchar(10),
  P_TYPE varchar(25),
  P_SIZE integer,
  P_CONTAINER varchar(10),
  P_RETAILPRICE decimal,
  P_COMMENT varchar(23)
);
COPY part FROM '/tpch-experiments/data/part.tbl' DELIMITER '|';

CREATE TABLE supplier (
  S_SUPPKEY integer PRIMARY KEY,
  S_NAME varchar(25),
  S_ADDRESS varchar(40),
  S_NATIONKEY integer,
  S_PHONE varchar(15),
  S_ACCTBAL decimal,
  S_COMMENT varchar(101)
);
COPY supplier FROM '/tpch-experiments/data/supplier.tbl' DELIMITER '|';

CREATE TABLE partsupp (
  PS_PARTKEY integer,
  PS_SUPPKEY integer,
  PS_AVAILQTY integer,
  PS_SUPPLYCOST Decimal,
  PS_COMMENT varchar(199)
);
COPY partsupp FROM '/tpch-experiments/data/partsupp.tbl' DELIMITER '|';

CREATE TABLE customer (
  C_CUSTKEY integer PRIMARY KEY,
  C_NAME varchar(25),
  C_ADDRESS varchar(40),
  C_NATIONKEY integer,
  C_PHONE varchar(15),
  C_ACCTBAL Decimal,
  C_MKTSEGMENT varchar(10),
  C_COMMENT varchar(117)
);
COPY customer FROM '/tpch-experiments/data/customer.tbl' DELIMITER '|';

CREATE TABLE orders (
  O_ORDERKEY integer PRIMARY KEY,
  O_CUSTKEY integer,
  O_ORDERSTATUS varchar(1),
  O_TOTALPRICE Decimal,
  O_ORDERDATE Date,
  O_ORDERPRIORITY varchar(15),
  O_CLERK varchar(15),
  O_SHIPPRIORITY Integer,
  O_COMMENT varchar(79)
);
COPY orders FROM '/tpch-experiments/data/orders.tbl' DELIMITER '|';

CREATE TABLE lineitem (
  L_ORDERKEY integer,
  L_PARTKEY integer,
  L_SUPPKEY integer,
  L_LINENUMBER integer,
  L_QUANTITY decimal,
  L_EXTENDEDPRICE decimal,
  L_DISCOUNT decimal,
  L_TAX decimal,
  L_RETURNFLAG varchar(1),
  L_LINESTATUS varchar(1),
  L_SHIPDATE date,
  L_COMMITDATE date,
  L_RECEIPTDATE date,
  L_SHIPINSTRUCT varchar(25),
  L_SHIPMODE varchar(10),
  L_COMMENT varchar(44)
);
COPY lineitem FROM '/tpch-experiments/data/lineitem.tbl' DELIMITER '|';

CREATE TABLE nation (
  N_NATIONKEY integer PRIMARY KEY,
  N_NAME varchar(25),
  N_REGIONKEY integer,
  N_COMMENT varchar(152)
);
COPY nation FROM '/tpch-experiments/data/nation.tbl' DELIMITER '|';

CREATE TABLE region (
  R_REGIONKEY integer PRIMARY KEY,
  R_NAME varchar(25),
  R_COMMENT varchar(152)
);
COPY region FROM '/tpch-experiments/data/region.tbl' DELIMITER '|';
