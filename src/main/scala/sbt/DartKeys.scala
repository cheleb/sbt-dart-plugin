package sbt

import sbt._
import Keys._

import com.typesafe.sbteclipse.core._
import com.typesafe.sbteclipse.core.EclipsePlugin._
import com.typesafe.sbteclipse.core.Validation
import scala.xml._
import scala.xml.transform.RewriteRule

trait DartKeys {

  val dartId = "play-dart"

  val classpathT = SettingKey[Seq[EclipseTransformerFactory[RewriteRule]]](dartId + "-eclipse .project-transformer")

  val dartDev = SettingKey[Boolean](dartId + "-dev")
  val dartVerbose = SettingKey[Boolean](dartId + "-verbose")
  val dartNoJs = SettingKey[Boolean](dartId + "-no-js")

  val dartDirectory = SettingKey[java.io.File](dartId + "-directory")
  val dartPublicManagedResources = SettingKey[java.io.File](dartId + "-public-managed_resources-directory")

  val dartPackagesDirectory = SettingKey[java.io.File](dartId + "-packages-directory")
  val dartWebDirectory = SettingKey[java.io.File](dartId + "-web-directory")
  val dartLibDirectory = SettingKey[java.io.File](dartId + "-lib-directory")

  val dartPublicDirectory = SettingKey[java.io.File](dartId + "-public-directory")

  val dartEntryPoints = SettingKey[Seq[String]](dartId + "-entries-point")
  //  val dartEntryPoints = SettingKey[PathFinder](dartId + "-entry-points")
  val dartWebUIEntryPoints = SettingKey[Seq[String]](dartId + "-web_ui-entries-point")

  val dartOptions = SettingKey[Seq[String]](dartId + "-options")

  val webuic = TaskKey[Unit]("web_ui", "compile web_ui")
  val dart2jsTask = TaskKey[Unit]("dart2js", "dart2js")
  val pubInstallTask = TaskKey[Unit]("dart-pub-install", "pub install task")

}

object DartKeys extends DartKeys