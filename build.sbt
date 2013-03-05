scalaVersion in ThisBuild := "2.9.2"

sbtPlugin := true

name := "sbt-dart-plugin"

resolvers += Resolver.file("Local Repository", file("/Users/olivier/projects/scala/Play20-myfork/repository/local"))(Resolver.ivyStylePatterns)

//libraryDependencies += "org.scala-tools.sbinary" %% "sbinary" % "0.4.1"

organization := "net.orcades"

addSbtPlugin("play" %% "sbt-plugin" % "2.1.0")

//javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

//scalacOptions ++= Seq("-target:jvm-1.7")

