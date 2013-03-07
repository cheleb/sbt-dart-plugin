package net.orcades.scala.play.dart

import sbt._
import sbt.Keys._
import play.Project._

object DartPlugin extends Plugin {
  val id = "play-dart"
  val dartFiles = SettingKey[PathFinder](id + "-files")
  val dartEntryPoints = SettingKey[PathFinder](id + "-entry-points")
  val dartOptions = SettingKey[Seq[String]](id + "-options")

  val dartJSCompiler = AssetsCompiler(id + "-dart2js",
    (_ ** "*.dart"),
    dartEntryPoints in Compile,
    { (name, min) => name + ".js" },
    { DartCompiler.compile _ },
    dartOptions in Compile)

  val dart2dartCompiler = AssetsCompiler(id + "-dart2dart",
    (_ ** "*.dart"),
    dartFiles in Compile,
    { (name, min) => name },
    { DartCompiler.dart2dart _ },
    dartOptions in Compile)

  override val settings = Seq(
    dartFiles <<= (sourceDirectory in Compile).apply(base => ((base / "assets" ** "*.dart"))),
    dartEntryPoints <<= (sourceDirectory in Compile).apply(base => ((base / "assets" ** "*.dart") --- (base / "assets" ** "_*")) --- (base / "assets" / "packages" ** "*")),
    dartOptions := Seq.empty[String],
    resourceGenerators in Compile <+= dartJSCompiler,
    resourceGenerators in Compile <+= dart2dartCompiler)

}

