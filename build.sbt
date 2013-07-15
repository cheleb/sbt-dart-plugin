version := "0.2.2-SNAPSHOT"

offline := true

scalaVersion := "2.10.2"
//scalaVersion := "2.9.2"

sbtPlugin := true

name := "sbt-dart-plugin"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += Resolver.file("my-test-repo", file("/Users/olivier/projects/scala/Play20/repository/local/"))(Resolver.ivyStylePatterns)

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

//addSbtPlugin("play" % "sbt-plugin" % "2.1.2-RC2")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2-SNAPSHOT")

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

