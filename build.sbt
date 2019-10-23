name := "simple-akka-server"
organization := "works.samazama"
version := "0.1.0"

scalaVersion := "2.13.1"

val akkaVersion = "2.5.25"
val akkaHttpVersion = "10.1.10"
val akkaHttpCirceVersion = "1.29.1"
val circeVersion = "0.12.1"
val scalaTestVersion = "3.0.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,

  "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "ch.qos.logback" % "logback-classic" % "1.2.3",

  // tests
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
)

scalacOptions ++= Seq(
  "-encoding", "utf8",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xlint:unused", // -Ywarn-unused:imports,privates,locals,implicits
  "-Ypresentation-strict",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-language:implicitConversions"
)

Compile / doc / scalacOptions := Seq("-groups", "-implicits")

Test / fork := true
Test / logBuffered := false
// -oD adds test duration
Test / testOptions += Tests.Argument("-oD")
Test / javaOptions ++= Seq("-Dakka.test.timefactor=2")