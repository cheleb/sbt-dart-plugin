scalaVersion := "2.10.3"

// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += Resolver.file("my-test-repo", file("/Users/olivier/projects/scala/Play20/repository/local/"))(Resolver.ivyStylePatterns)

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

