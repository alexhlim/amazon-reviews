name := "amazon-reviews-streaming"

version := "0.1"

// Spark only compatible with 2.12.x
scalaVersion := "2.12.12"

libraryDependencies += "com.audienceproject" %% "spark-dynamodb" % "1.1.0"
libraryDependencies += "software.amazon.awssdk" % "auth" % "2.15.9"
libraryDependencies += "software.amazon.awssdk" % "kinesis" % "2.15.9"
libraryDependencies += "software.amazon.awssdk" % "regions" % "2.15.9"
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.0.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.0"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "3.0.0"
libraryDependencies += "org.apache.spark" % "spark-streaming-kinesis-asl_2.12" % "3.0.0"
libraryDependencies += "org.json4s" %% "json4s-native" % "3.7.0-M7"
// Contains ScalaTest
libraryDependencies += "com.holdenkarau" %% "spark-testing-base" % "3.0.0_1.0.0" % Test

wartremoverErrors ++= Warts.unsafe

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}