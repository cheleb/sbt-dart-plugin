package net.orcades.scala.play.dart


import sbt._
import sbt.Keys._
import play.Project._

object DartPlugin extends Plugin {
val id = "play-dart"
  val dartScriptEntryPoints = SettingKey[PathFinder](id + "-entry-points")
  val dartScriptOptions = SettingKey[Seq[String]](id + "-options")
  
  val dartScriptWatcher = AssetsCompiler(id,
    (_ ** "*.dart"),
    dartScriptEntryPoints in Compile,
    { (name, min) => name + ".js" },
    { DartCompiler.compile _ },
    dartScriptOptions in Compile
  )

  override val settings = Seq(
    dartScriptEntryPoints <<= (sourceDirectory in Compile).apply(base => ((base / "assets" ** "*.dart") --- base / "assets" ** "_*")),
    dartScriptOptions := Seq.empty[String],
    resourceGenerators in Compile <+= dartScriptWatcher
  )

}

