package sbt

import sbt._

trait DartKeys {

  val dartId = "play-dart"
  val dartPluginDisabled = SettingKey[Boolean](dartId + "-disabled")
  val dartDirectory = SettingKey[java.io.File](dartId + "-directory")
  val dartPackagesDirectory = SettingKey[java.io.File](dartId + "-packages-directory")
  val dartWebDirectory = SettingKey[java.io.File](dartId + "-web-directory")
  val dartWebPackageLink = SettingKey[java.io.File](dartId + "-web-package-link")
  
  val dartPublicDirectory = SettingKey[java.io.File](dartId + "-public-directory")
  val dartPublicPackagesLink = SettingKey[java.io.File](dartId + "-public-packages-link")

  val dartWebUIDirectory = SettingKey[java.io.File](dartId + "-web_ui-directory")
  val dartWebUIPublicDirectory = SettingKey[java.io.File](dartId + "-web_ui-public-directory")
  val dartWebUIPublicPackagesLink = SettingKey[java.io.File](dartId + "-web_ui-public-packages-link")


  val dartEntryPoints = SettingKey[Seq[String]](dartId + "-entries-point")
  //  val dartEntryPoints = SettingKey[PathFinder](dartId + "-entry-points")
  val dartWebUIEntryPoints = SettingKey[Seq[String]](dartId + "-web_ui-entries-point")

  val dartFiles = SettingKey[PathFinder](dartId + "-files")
  val dartResources = SettingKey[PathFinder](dartId + "-resources")
  val dartOptions = SettingKey[Seq[String]](dartId + "-options")

  val webuic = TaskKey[Unit]("web_ui", "compile web_ui")
  val dart2js = TaskKey[Unit]("dart2js", "dart2js")

}

object DartKeys extends DartKeys