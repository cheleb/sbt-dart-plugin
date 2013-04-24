package sbt

import Keys._
import sbt._
import sbt.Keys._
import play.Project._

import DartKeys._

object DartPlugin extends Plugin
  with DartPlayAssetDeployer
  with Dart2jsCompiler
  with DartWebUICompiler
  with WebUITask {

  override lazy val settings = Seq(
    //    webuic <<= webuicTask.runBefore(PlayProject.playCopyAssets),
    //    dart2js <<= dart2jsTask.runBefore(PlayProject.playCopyAssets),

    dartPluginDisabled := false,  
      
    dartDirectory <<= (sourceDirectory in Compile) /  "dart",
    dartPackagesDirectory <<= (dartDirectory) / "packages",
    dartWebDirectory <<= (dartDirectory) / "web",
    dartWebPackageLink <<= dartWebDirectory / "packages",
    
    dartPublicDirectory <<= baseDirectory / "public",
    dartPublicPackagesLink <<= dartPublicDirectory / "packages",
    
    dartWebUIDirectory <<= (dartWebDirectory) / "out",
    
    dartWebUIPublicDirectory <<= dartPublicDirectory / "out",
    dartWebUIPublicPackagesLink <<= dartWebUIPublicDirectory / "packages",

    resourceGenerators in Compile <+= dart2dartCompiler,
    resourceGenerators in Compile <+= dart2jsCompiler,
    resourceGenerators in Compile <+= dartWebUICompiler,

    dartEntryPoints := Seq.empty[String],
    dartWebUIEntryPoints := Seq.empty[String],
    dartResources in Compile <<= (dartWebDirectory in Compile).apply(base => ((base  ** "*.*") --- (base / "out" ** "*") )),
    dartFiles in Compile <<= (dartWebDirectory in Compile).apply(base => ((base ** "*.dart")  --- (base  / "out" ** "*"))),
    dartOptions := Seq.empty[String])

}

