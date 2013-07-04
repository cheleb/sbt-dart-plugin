version := "0.2.2-SNAPSHOT"

sbtVersion in Global := "0.13.0-RC1"

scalaVersion := "2.10.2"
//scalaVersion := "2.9.2"

sbtPlugin := true

name := "sbt-dart-plugin"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("play" % "sbt-plugin" % "2.1.2-RC2")

libraryDependencies += "com.typesafe.sbteclipse" %% "sbteclipse" % "2.0.0"

//libraryDependencies += "play" %% "play" % "2.1-0627-sbt12"

//libraryDependencies += "com.google.javascript" % "closure-compiler" % "rr2079.1"

//unmanagedJars in Compile <++= baseDirectory map { base =>
//    val libs = base / "libs"
//    (libs ** "*.jar").classpath
//}



publishTo := Some(Resolver.file("file",  new File( "/tmp/sbt-dart" )) )

organization := "net.orcades"

//javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

//scalacOptions ++= Seq("-target:jvm-1.7")

