name := "CassandraSparkTest"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-deprecation")

// grading libraries
libraryDependencies += "junit" % "junit" % "4.10" % "test"

// for funsets
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"

// https://mvnrepository.com/artifact/com.datastax.spark/spark-cassandra-connector_2.10
libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.2"

// https://mvnrepository.com/artifact/org.scalatest/scalatest_2.10
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

// https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.25" % "test"

// https://mvnrepository.com/artifact/io.dropwizard.metrics/metrics-core
libraryDependencies += "io.dropwizard.metrics" % "metrics-core" % "3.2.2"

// https://mvnrepository.com/artifact/org.apache.spark/spark-core_2.10
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0"

// https://mvnrepository.com/artifact/org.apache.spark/spark-sql_2.10
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.2.0"


// https://mvnrepository.com/artifact/org.graphstream/gs-algo
libraryDependencies += "org.graphstream" % "gs-algo" % "1.3"

// https://mvnrepository.com/artifact/org.graphstream/gs-core
libraryDependencies += "org.graphstream" % "gs-core" % "1.3"

libraryDependencies += "org.graphstream" % "gs-ui" % "1.3"

// https://mvnrepository.com/artifact/org.apache.poi/poi
libraryDependencies += "org.apache.poi" % "poi" % "3.16"

// https://mvnrepository.com/artifact/org.apache.poi/poi-ooxml
libraryDependencies += "org.apache.poi" % "poi-ooxml" % "3.11"

// https://mvnrepository.com/artifact/com.github.javafaker/javafaker
libraryDependencies += "com.github.javafaker" % "javafaker" % "0.13"

// https://mvnrepository.com/artifact/org.apache.spark/spark-graphx_2.10
libraryDependencies += "org.apache.spark" %% "spark-graphx" % "2.2.0"
