package net.orcades.scala.play.dart

import sbt._
import sbt.Keys._
import play.Project._

import net.orcades.scala.play.dart.DartKeys._

object DartPlugin extends Plugin with PlayPublicCompiler {
  
  val dartJSCompiler = PublicCompiler(id + "-dart2js",
    (_ ** "*.dart"),
    dartEntryPoints in Compile,
    { (name, min) => name + ".js" },
    { DartCompiler.compile _ },
    dartOptions in Compile)

//  val dart2dartCompiler = AssetsCompiler(id + "-dart2dart",
//    (_ ** "*.dart"),
//    dartFiles in Compile,
//    { (name, min) => name },
//    { DartCompiler.dart2dart _ },
//    dartOptions in Compile)

  override val settings = Seq(
    dartDirectory in Compile <<= baseDirectory / "public", 
    dartFiles <<= (sourceDirectory in Compile).apply(base => ((base / "assets" ** "*.dart"))),
    dartEntryPoints <<= (dartDirectory in Compile).apply(base => ((base ** "*.dart") --- (base ** "_*")) --- (base / "packages" ** "*") --- (base / "lib" ** "*" )),
    dartOptions := Seq.empty[String],
    resourceGenerators in Compile <+= dartJSCompiler)
//    resourceGenerators in Compile <+= dart2dartCompiler)

}

