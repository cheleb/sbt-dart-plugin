//scalaVersion := "2.10.1"
scalaVersion := "2.9.2"

// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.sbteclipse" %% "sbteclipse-plugin" % "2.1.2")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.4.0")

//addSbtPlugin("play" % "sbt-plugin" % "2.1.0")

//javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

//scalacOptions ++= Seq("-target:jvm-1.7")

