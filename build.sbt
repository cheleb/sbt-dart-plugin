version := "0.2.2-SNAPSHOT"

offline := false

scalaVersion := "2.10.3"

sbtPlugin := true

name := "sbt-dart-plugin"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.1")


publishTo := Some(Resolver.file("file",  new File( "/tmp/sbt-dart" )) )

organization := "net.orcades"


