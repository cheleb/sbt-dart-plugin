version := "0.2.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.9.2"

sbtPlugin := true

name := "sbt-dart-plugin"

//libraryDependencies += "org.scala-tools.sbinary" %% "sbinary" % "0.4.1"

publishTo := Some(Resolver.file("file",  new File( "/tmp/sbt-dart" )) )

organization := "net.orcades"

addSbtPlugin("play" %% "sbt-plugin" % "2.1.0")

//javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

//scalacOptions ++= Seq("-target:jvm-1.7")

