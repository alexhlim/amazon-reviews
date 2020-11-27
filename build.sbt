// For root project
lazy val root = (project in file(".")).disablePlugins(AssemblyPlugin)

lazy val commonSettings = Seq(
  version := "1.0.0",
  scalaVersion := "2.12.12",
  organization := "com.amazonreviews",
  libraryDependencies ++= Seq(
    "software.amazon.awssdk" % "auth" % "2.15.9",
    "software.amazon.awssdk" % "kinesis" % "2.15.9",
    "org.apache.spark" %% "spark-sql" % "3.0.0",
    "org.json4s" %% "json4s-jackson" % "3.6.6",
    "com.holdenkarau" %% "spark-testing-base" % "3.0.0_1.0.0" % Test
  ),  
  test in assembly := {},
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
  },
  wartremoverErrors ++= Warts.unsafe
)

lazy val utilProject = "util"
lazy val util = (project in file (utilProject))
  .settings(
    commonSettings,
    name := utilProject
  ).disablePlugins(AssemblyPlugin)

lazy val streamProject = "stream"
lazy val stream = (project in file(streamProject))
  .settings(
    commonSettings,
    name := streamProject,
    mainClass in assembly := Some("com.amazonreviews.stream.KinesisSparkStream"),
    libraryDependencies ++= Seq(
      "com.audienceproject" %% "spark-dynamodb" % "1.1.0",
      "org.apache.spark" % "spark-streaming-kinesis-asl_2.12" % "3.0.0"
    )
  ).dependsOn(util % "test->test;compile->compile")

lazy val batchProject = "batch"
lazy val batch = (project in file(batchProject))
  .settings(
    commonSettings,
    name := batchProject,
    mainClass in assembly := Some("com.amazonreviews.batch.S3SparkBatch"),
    libraryDependencies ++= Seq("com.amazonaws" % "aws-java-sdk" % "1.7.4"),
  ).dependsOn(util % "test->test;compile->compile")
