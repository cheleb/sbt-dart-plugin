package sbt

import Keys._
import sbt._
import sbt.Keys._
import play.Project._
import DartKeys._
import sbt.ConfigKey.configurationToKey

import sbt.Scoped.richFileSetting

import scalaz.Validation._
import scala.xml._

object DartPlugin extends Plugin
  with DartPlayAssetDeployer
  with Dart2jsCompiler
  with DartTask
  with DartEclipse {

  override lazy val settings = Seq(
    //    webuic <<= webuicTask.runBefore(PlayProject.playCopyAssets),
    //    dart2js <<= dart2jsTask.runBefore(PlayProject.playCopyAssets),

    dartProjectTransform,

    pubInstallTask <<= dartPubInstall.runBefore(play.Project.playCommonClassloader),

    unmanagedBase <<= baseDirectory { base => base / "playlibs" },

    dartDev := false,
    dartVerbose := false,
    dartNoJs := false,

    dartPublicManagedResources <<= (resourceManaged in Compile) / "public",

    dartDirectory <<= baseDirectory,
    dartPackagesDirectory <<= (dartDirectory) / "packages",
    dartWebDirectory <<= (dartDirectory) / "web",
    dartLibDirectory <<= (dartDirectory) / "lib",

    dartPublicDirectory <<= baseDirectory / "public",

    resourceDirectories in Compile <+= dartWebDirectory,
    
    resourceDirectories in Compile <+= dartLibDirectory,

    resourceGenerators in Compile <+= dartWebUICompiler,
    resourceGenerators in Compile <+= dartAssetsDeployer,
    resourceGenerators in Compile <+= dart2jsCompiler,

    dartEntryPoints := Seq.empty[String],
    dartWebUIEntryPoints := Seq.empty[String],

    dartOptions := Seq.empty[String])

}

