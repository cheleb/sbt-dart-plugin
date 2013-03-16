package net.orcades.scala.play.dart

import sbt._

trait DartKeys {
  val id = "play-dart"
  val dartFiles = SettingKey[PathFinder](id + "-files")
  val dartEntryPoints = SettingKey[PathFinder](id + "-entry-points")
  val dartOptions = SettingKey[Seq[String]](id + "-options")
  val dartDirectory = SettingKey[java.io.File](id + "-directory")
  
}

object DartKeys extends DartKeys