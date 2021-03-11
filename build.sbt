name := "MockCrud"

version := "0.1"

scalaVersion := "2.13.5"
libraryDependencies ++= {
  val akkaV       = "2.5.23"
  val scalaTestV  = "3.2.6"
  lazy val akkaHttpVersion = "10.2.4"

  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    //"com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    //"com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,
   // "com.typesafe.akka" %% "akka-http-testkit" % akkaV,
    "com.github.etaty" %% "rediscala" % "1.9.0",
    //"org.scalatest"     %% "scalatest" % scalaTestV % "test",
    //"org.scalamock" %% "scalamock-scalatest-support" % "3.4.2" % "test"
  )
}