package sbt

import sbt._
import play.api._
import Keys._
import PlayKeys._


trait Pub extends DartSdk {

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
  def pub(dartDir: File, command: String, log: Logger) {

    val publock = dartDir / "pubspec.lock"

    if (publock.exists())
      publock.delete()

    val cmd = pubExePath + " " + command

    import scala.sys.process._
    val d2js = Process(cmd, dartDir)

    var out = List[String]()
    var err = List[String]()
    val exit = d2js ! ProcessLogger((s) => out ::= s, (s) => err ::= s)

    if (exit != 0)
      throw new PlayException(out.mkString("\n") + err.mkString("\n"), command)
    log.info(out.mkString("\n"))

  }

  

 




  

  //  private def treeShake(sourceFile: File, shakedTree: File, public: File, options: Seq[String]): File = {
  //
  //    dart2js(sourceFile, shakedTree, options :+ "--output-type=dart")
  //
  //    shakedTree.getParentFile() / (shakedTree.getName() + ".deps")
  //
  //  }

}


