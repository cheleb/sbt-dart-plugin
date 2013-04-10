package sbt

import sbt.PlayExceptions.AssetCompilationException
import sbt._
import play.api._
import Keys._
import PlayKeys._
import java.nio.file.Files

object DartCompiler {

  lazy val dartHome: File = {
    val DART_HOME = System.getenv("DART_HOME")
    if (DART_HOME == null) {
      sys.error("DART_HOME env variable must be defined!")
    } else {
      val dartHome = new File(DART_HOME)
      if (dartHome.exists())
        dartHome
      else
        sys.error(dartHome + " does not exist!")
    }
  }

  lazy val dart2jsExe: File = {
    val path = dartHome + "/bin/dart2js"
    val exe = new File(path)
    if (exe.exists())
      exe
    else
      sys.error(exe + " does not exist!")

  }

  lazy val dartExe: File = {
    val path = dartHome + "/bin/dart"
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
  def dart2jsWithTmp(dartDirectory: File, dartFile: File, tmpFile: File, options: Seq[String]) = {

    val cmd = dart2jsExe.absolutePath + " " + options.mkString(" ") + " -o" + tmpFile.absolutePath + " " + dartFile.absolutePath

    import scala.sys.process._
    val d2js = Process(cmd, dartDirectory)

    var out = List[String]()
    var err = List[String]()
    val exit = d2js ! ProcessLogger((s) => out ::= s, (s) => err ::= s)

    if (exit != 0) {
      throw CompilationException(out.mkString("\n") + err.mkString("\n"), dartFile, None)
    }

    tmpFile

  }

  def compileWebUI(dartBase: File, options: Seq[String]) = {

    val tmpDir = IO.createTemporaryDirectory

    val output = dartBase / "web" / "out"

    val cmd = dartExe.absolutePath + " build.dart"

    println("In " + dartBase + "  " + cmd)

    import scala.sys.process._
    val d2js = Process(cmd, dartBase)

    var out = List[String]()
    var err = List[String]()
    val exit = d2js ! ProcessLogger((s) => out ::= s, (s) => err ::= s)

    println(out.mkString("\n"))

    if (exit != 0) {
      throw CompilationException(out.mkString("\n") + err.mkString("\n"), dartBase / "build.dart", None)
    }

    

    allFilesIn(new File(output.absolutePath), f => true) //output / webui.getName())

  }

  def dart2js(dartDirectory: File, dartFile: File, options: Seq[String]): (String, Option[String], Seq[File]) = {
    try {
      val tmpDir = IO.createTemporaryDirectory

      val tmpFilename = dartFile.name + ".js"

      val tmpFile = tmpDir / tmpFilename
      dart2jsWithTmp(dartDirectory, dartFile, tmpFile, options)
      //(IO.read(tmpFile), None, allSiblings(dartFile))
      (IO.read(tmpFile), None, Nil)
    } catch {
      case e: Exception =>
        e.printStackTrace
        throw new AssetCompilationException(Some(dartFile), "Internal dart2js Compiler error (see logs)", None, None)
    }
  }
  
  /**
   * Publish the dart files.
   * @param dartFile
   * @param options dart compiler options
   * @return (source, None, Self)
   */
  def dart2dart(dartFile: File, options: Seq[String]) = {
    (IO.read(dartFile), None, Seq(dartFile))
  }

  /**
   * Return all Dart files in the same directory than the input file, or subdirectories
   */
  def allSiblings(source: File): Seq[File] = allFilesIn(source.getParentFile(), f => true)

  def allFilesIn(dir: File, filter: File => Boolean): Seq[File] = {
    import scala.collection.JavaConversions._
    val jsFiles = dir.listFiles(new FileFilter {
      override def accept(f: File) = filter(f)
    })
    val directories = dir.listFiles(new FileFilter {
      override def accept(f: File) = f.isDirectory()
    })
    val jsFilesChildren = directories.map(d => allFilesIn(d, filter)).flatten
    jsFiles ++ jsFilesChildren
  }

  

}
