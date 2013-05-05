package sbt

import sbt.PlayExceptions.AssetCompilationException
import sbt._
import play.api._
import Keys._
import PlayKeys._
import java.nio.file.Files

object DartCompiler {

  lazy val dartSdk: File = {
    val DART_SDK = System.getenv("DART_SDK")
    if (DART_SDK == null) {
      sys.error("DART_HOME env variable must be defined!")
    } else {
      val dartHome = new File(DART_SDK)
      if (dartHome.exists())
        dartHome
      else
        sys.error(dartHome + " does not exist!")
    }
  }

  lazy val dart2jsExe: File = {
    val path = dartSdk + "/bin/dart2js"
    val exe = new File(path)
    if (exe.exists())
      exe
    else
      sys.error(exe + " does not exist!")

  }

  lazy val dartExe: File = {
    val path = dartSdk + "/bin/dart"
    val exe = new File(path)
    if (exe.exists())
      exe
    else
      sys.error(exe + " does not exist!")

  }

  case class CompilationException(message: String, jsFile: File, atLine: Option[Int]) extends PlayException.ExceptionSource(
    "Dart Compilation error", message) {
    def line = atLine.map(_.asInstanceOf[java.lang.Integer]).orNull
    def position = null
    def input = scalax.file.Path(jsFile).string
    def sourceName = jsFile.getAbsolutePath
  }

  /**
   * Compile dart file into javascript.
   * @param dartFile
   * @param options dart compiler options
   * @return (source, None, Seq(deps))
   */
  private def dart2js(dartFile: File, jsFile: File, options: Seq[String]) {

    val cmd = dart2jsExe.absolutePath + " " + options.mkString(" ") + " -o" + jsFile.absolutePath + " " + dartFile.absolutePath

    import scala.sys.process._
    val d2js = Process(cmd)

    var out = List[String]()
    var err = List[String]()
    val exit = d2js ! ProcessLogger((s) => out ::= s, (s) => err ::= s)

    if (exit != 0) {
      throw CompilationException(out.mkString("\n") + err.mkString("\n"), dartFile, None)
    }

  }

  def js(dartDir: File, entryPoint: String, entryPointFile: File, shakedTree: File, jsFile: File, deps: File, public: File, options: Seq[String]): Seq[File] = {
    val dependencies = treeShake(entryPointFile, shakedTree, deps, public, options)
    dart2js(shakedTree, jsFile, options)
    dependencies
  }

  def wuic(dartDir: File, entryPoint: String, entryPointFile: File, shakedTree: File, jsFile: File, deps: File, public: File, options: Seq[String]): Seq[File] = {

    val bootstrap = compileWebUI(dartDir, entryPoint, entryPointFile, options)

    val dependencies = treeShake(bootstrap, shakedTree, deps, public, options)
    dart2js(shakedTree, jsFile, options)
    dependencies
  }

  def compileWebUI(dartBase: File, entryPoint: String, entryPointFile: File, options: Seq[String]): File = {

    val bootstrap = dartBase / "web" / "out" / (entryPoint + "_bootstrap.dart")

    val cmd = dartExe.absolutePath + " --package-root=packages/ " + options.mkString(" ") + " packages/play_webuic/play_webuic.dart --out web/out/ web/" + entryPoint

    //println("In " + dartBase + "\n" + cmd)

    import scala.sys.process._
    val d2js = Process(cmd, dartBase)

    var out = List[String]()
    var err = List[String]()
    val exit = d2js ! ProcessLogger((s) => out ::= s, (s) => err ::= s)

    println(out.mkString("\n"))

    if (exit != 0) {
      throw CompilationException(out.mkString("\n") + err.mkString("\n"), entryPointFile, None)
    }

    bootstrap

  }

  private def treeShake(sourceFile: File, shakedTree: File, deps: File, public: File, options: Seq[String]): Seq[File] = {

    dart2js(sourceFile, shakedTree, options :+ "--output-type=dart")

    IO.readLines(deps).map(filename => IO.asFile(new java.net.URL(filename)))
  }

}
