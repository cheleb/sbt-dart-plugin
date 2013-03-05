package net.orcades.scala.play.dart

import sbt.PlayExceptions.AssetCompilationException
import sbt._
import play.api._

import Keys._
import PlayKeys._


object DartCompiler {

  
  
  
 lazy val dartExe: Option[File] = {
    val dartHome = System.getenv("DART_HOME")
    if (dartHome == null) {
      None
    } else {
      val path = dartHome + "/bin/dart2js"
      val exe = new File(path)
      if (exe.exists())
        Some(exe)
      else
        None
    }
  }

 case class CompilationException(message: String, jsFile: File, atLine: Option[Int]) extends PlayException.ExceptionSource(
    "Dart Compilation error", message) {
    def line = atLine.map(_.asInstanceOf[java.lang.Integer]).orNull
    def position = null
    def input = scalax.file.Path(jsFile).string
    def sourceName = jsFile.getAbsolutePath
  }

  def dartProcess(dartFile: File, options: Seq[String] ) = {

   val tmpDir = IO.createTemporaryDirectory
  
   val tmpFilename = dartFile.name + ".js"

   val tmpFile = tmpDir / tmpFilename

   val cmd = dartExe match {
      case None => sys.error("Could not find dart2js!")
      case Some(exe) => exe.absolutePath + " -o" + tmpFile.absolutePath + " " + dartFile.absolutePath
    }
   
   import scala.sys.process._
   val d2js = Process(cmd)
   
    var out = List[String]()
    var err = List[String]()
    val exit = d2js ! ProcessLogger((s) => out ::= s, (s) => err ::= s)

   if(exit != 0){
     throw CompilationException(err.mkString("\n"), dartFile, None)
   }
   
   (IO.read(tmpFile), None, allSiblings(dartFile)) 

  }

  /**
   * Publish the dart files. 
   * @param dartFile 
   * @param options dart compiler options
   * @return (source, None, Self)
   **/ 
  def dart2dart( dartFile: File, options: Seq[String] ) = {
    (IO.read(dartFile), None, Seq(dartFile))    
  }

  /**
   * Compile dart file into javascript.
   * @param dartFile 
   * @param options dart compiler options
   * @return (source, None, Seq(deps))
   **/
  def dart2js(dartFile: File, options: Seq[String] ) = {
    dartExe match {
      case None => sys.error("Could not find dart2js!")
      case Some(exe) => dartProcess(dartFile, options)
    }
  }

 /**
   * Return all Dart files in the same directory than the input file, or subdirectories
   */
  private def allSiblings(source: File): Seq[File] = allJsFilesIn(source.getParentFile())

  private def allJsFilesIn(dir: File): Seq[File] = {
    import scala.collection.JavaConversions._
    val jsFiles = dir.listFiles(new FileFilter {
      override def accept(f: File) = f.getName().endsWith(".dart")
    })
    val directories = dir.listFiles(new FileFilter {
      override def accept(f: File) = f.isDirectory()
    })
    val jsFilesChildren = directories.map(d => allJsFilesIn(d)).flatten
    jsFiles ++ jsFilesChildren
  }
  
  
   def compile(src: File, options: Seq[String]): (String, Option[String], Seq[File]) = {
    try {
      dart2js(src, options)
    }catch {
      case e: Exception =>
        e.printStackTrace
        throw new AssetCompilationException(Some(src), "Internal ClojureScript Compiler error (see logs)", None, None)
    }
   }
    

}
