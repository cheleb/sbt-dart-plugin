package sbt

import sbt._
import sbt.Keys._
import play.Project._
import java.nio.file.Files

trait WebUITask extends DartKeys {

//  lazy val dart2jsTask = (dartDirectory in Compile, dartEntryPoints, resourceManaged in Compile, dartOptions) map { (dartDir, inputs, resources, options) =>
//    println("dart2js task")
//    inputs.foreach {
//      filename =>
//        val dartFile = dartDir / "web" / filename
//        val out = resources / "public" / (filename + ".js")
//        println(dartFile + " --> " + out)
//        if (Files.notExists(out.toPath()) || Files.getLastModifiedTime(dartFile.toPath()).toMillis() > Files.getLastModifiedTime(out.toPath()).toMillis())
//          DartCompiler.dart2jsWithTmp(dartDir, dartFile, out, options)
//    }
//  }
//
//  lazy val webuicTask = (dartDirectory in Compile, dartWebUIEntryPoints, dartWebUIDirectory, resourceManaged in Compile, dartOptions) map { (dartDir, entryPoints, webOut, resources, options) =>
//    println("WebUI Task")
//
//    entryPoints.foreach(entryPoint =>
//      DartCompiler.compileWebUI(dartDir, entryPoint, webOut, options))
//
//    val outputDir = resources / "public" / "out"
//    outputDir.mkdirs()
//
//    entryPoints.foreach {
//      filename =>
//        val dartFile = dartDir / "web" / "out" / (filename + "_bootstrap.dart")
//        val out = outputDir / (filename + "_bootstrap.dart.js")
//        println(dartFile + " --> " + out)
//        if (Files.notExists(out.toPath()) || Files.getLastModifiedTime(dartFile.toPath()).toMillis() > Files.getLastModifiedTime(out.toPath()).toMillis())
//          DartCompiler.dart2jsWithTmp(dartDir, dartFile, out, options)
//    }
//
//  }
}