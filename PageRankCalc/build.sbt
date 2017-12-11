name := "PageRankCalc"

version := "0.1"

scalaVersion := "2.11.12"

// https://mvnrepository.com/artifact/com.datastax.spark/spark-cassandra-connector_2.11
libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.5"

// https://mvnrepository.com/artifact/org.apache.spark/spark-core_2.11
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0"

// https://mvnrepository.com/artifact/org.apache.spark/spark-sql_2.11
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.2.0"

// https://mvnrepository.com/artifact/org.apache.spark/spark-graphx_2.11
libraryDependencies += "org.apache.spark" %% "spark-graphx" % "2.2.0"

// https://mvnrepository.com/artifact/org.graphstream/gs-algo
libraryDependencies += "org.graphstream" % "gs-algo" % "1.3"

// https://mvnrepository.com/artifact/org.graphstream/gs-core
libraryDependencies += "org.graphstream" % "gs-core" % "1.3"

libraryDependencies += "org.graphstream" % "gs-ui" % "1.3"