import play.PlayImport._
import play.PlayScala
import sbtassembly.Plugin.AssemblyKeys._

name := "CloudStore"

version := "0.0.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

net.virtualvoid.sbt.graph.Plugin.graphSettings

assemblySettings

jarName in assembly := "CloudeStore.jar"

mainClass in assembly := Some("play.core.server.NettyServer")

fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)

resolvers ++= Seq(
  "Atlassian Releases" at "https://maven.atlassian.com/public/",
  "JCenter repo" at "https://bintray.com/bintray/jcenter/",
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
)

mergeStrategy in assembly := {
  case PathList("play", "core", "server", "ServerWithStop.class") => MergeStrategy.last
  case x =>
    val oldStrategy = (mergeStrategy in assembly).value
    oldStrategy(x)
}

libraryDependencies ++= Seq(
  ws exclude("commons-logging", "commons-logging"),
  jdbc,
  anorm,
  filters,
  "net.sigusr" %% "scala-mqtt-client" % "0.6.0",
  "com.softwaremill.macwire" %% "macros" % "1.0.1",
  "xalan" % "xalan" % "2.7.1",
  "com.typesafe.akka" %% "akka-actor" % "2.3.12",
  "com.softwaremill.macwire" %% "macros" % "0.7.3",
  "org.scalatestplus" %% "play" % "1.2.0" % "test",
  "net.ceedubs" %% "ficus" % "1.1.2",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test",
  "de.leanovate.play-mockws" %% "play-mockws" % "0.12" % "test"
)

fork in run := true
