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
    dartResources in Compile <<= (dartDirectory in Compile).apply(base => ((base / "web" ** "*.*") --- (base / "web" / "out" ** "*") --- (base / "web" / "images" ** "*"))),
    dartFiles in Compile <<= (dartDirectory in Compile).apply(base => ((base / "web" ** "*.dart") --- (base / "web" / "images" ** "*") --- (base / "web" / "out" ** "*"))),
    dartOptions := Seq.empty[String])

}

