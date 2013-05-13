package sbt

import sbt._

trait DartKeys {

  val dartId = "play-dart"
  val dart2js = SettingKey[Boolean](dartId + "-dart2js-disabled")
  val dartDirectory = SettingKey[java.io.File](dartId + "-directory")
  val dartPublicManagedResources = SettingKey[java.io.File](dartId + "-public-managed_resources-directory")
  val dartPublicWebUIManagedResources = SettingKey[java.io.File](dartId + "-public-web-ui-managed_resources-directory")
  val dartPackagesDirectory = SettingKey[java.io.File](dartId + "-packages-directory")
  val dartWebDirectory = SettingKey[java.io.File](dartId + "-web-directory")

  
  val dartPublicDirectory = SettingKey[java.io.File](dartId + "-public-directory")


  val dartWebUIDirectory = SettingKey[java.io.File](dartId + "-web_ui-directory")


  val dartEntryPoints = SettingKey[Seq[String]](dartId + "-entries-point")
  //  val dartEntryPoints = SettingKey[PathFinder](dartId + "-entry-points")
  val dartWebUIEntryPoints = SettingKey[Seq[String]](dartId + "-web_ui-entries-point")

  val dartFiles = SettingKey[PathFinder](dartId + "-files")

  val dartOptions = SettingKey[Seq[String]](dartId + "-options")

  val webuic = TaskKey[Unit]("web_ui", "compile web_ui")
  val dart2jsTask = TaskKey[Unit]("dart2js", "dart2js")
  val pubInstallTask = TaskKey[Unit]("dart-pub-install", "pub install task")

}

object DartKeys extends DartKeys