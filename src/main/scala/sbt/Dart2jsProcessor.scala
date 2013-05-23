package sbt

import sbt._
import play.api._

case class CompilationException(message: String, jsFile: File, atLine: Option[Int]) extends PlayException.ExceptionSource(
  "Dart Compilation error", message) {
  def line = atLine.map(_.asInstanceOf[java.lang.Integer]).orNull
  def position = null
  def input = scalax.file.Path(jsFile).string
  def sourceName = jsFile.getAbsolutePath
}

object dart2jsProcessor extends DartProcessor {

  def resolves(public: File, module: Option[String], entryPoint: String): (File, File) = {
    val out = module.map(m => public / m).getOrElse(public)
    (out / entryPoint, (out / (entryPoint + ".js")))
  }

  def compile(dev: Boolean, noJs: Boolean, webDir: File, module: Option[String], entryPoint: String, public: File, options: Seq[String]): Option[File] = {

    if (noJs)
      None
    else {
      val (jsFile, deps) = dart2js(false, webDir, module, entryPoint, options)
      Some(deps)
    }

  }

  def deployables(dev: Boolean, noJs: Boolean, web: File, module: Option[String], entryPoint: String): Seq[String] = {
    if (noJs) {
      Nil
    } else {
      val js = module.map(m => m + "/" + entryPoint).getOrElse(entryPoint) + ".js"
      if (dev)
        List(js, js + ".map")
      else
        List(js)
    }

  }

}

object dartWebUIProcessor extends DartProcessor {

  def compileWebUI(web: File, module: Option[String], entryPoint: String, options: Seq[String]): (String, File, File) = {

    val entryPointPath = module.map(m => m + "/" + entryPoint).getOrElse(entryPoint)

    val entryPointFile = web / entryPointPath

    val out = module map (m => m + "/out") getOrElse ("out")

    val depsFile = web / out / (entryPoint + ".deps")

    val outsFile = web / out / (entryPoint + ".outs")

    val parentFolder = outsFile.getParentFile()

    parentFolder.mkdirs()

    val cmd = dartExePath + " --package-root=packages/ " + options.mkString(" ") + " packages/play_webuic/play_webuic.dart --out " + out + " " + entryPointPath

    println("In " + web + "\n" + cmd)

    import scala.sys.process._
    val d2js = Process(cmd, web)

    var stdout = List[String]()
    var stderr = List[String]()
    val exit = d2js ! ProcessLogger((s) => stdout ::= s, (s) => stderr ::= s)

    println(stdout.mkString("\n"))

    if (exit != 0) {
      throw CompilationException(stdout.mkString("\n") + stderr.mkString("\n"), entryPointFile, None)
    }

    (entryPoint + "_bootstrap.dart", depsFile, outsFile)

  }

  def resolves(public: File, module: Option[String], entryPoint: String): (File, File) = {
    val webuiEntryPoint = entryPoint + "_bootstrap.dart"
    val out = module.map(m => public / m).getOrElse(public) / "out"
    (out / webuiEntryPoint, (out / (webuiEntryPoint + ".js")))
  }

  def compile(dev: Boolean, noJs: Boolean, web: File, module: Option[String], entryPoint: String, public: File, options: Seq[String]): Option[File] = {

    val (bootstrap, deps, outs) = compileWebUI(web, module, entryPoint, options)

    if (!noJs) {
      val (jsFile, jsDeps) = dart2js(true, web, module, bootstrap, options)
      IO.append(outs, "file://" + jsFile + "\n")
      IO.append(outs, "file://" + jsFile + ".map" + "\n")
    }

    Some(deps)
  }

  def deployables(dev: Boolean, noJs: Boolean, web: File, module: Option[String], entryPoint: String): Seq[String] = {
    if (dev) {
//      val outs = module.map(m => web / m).getOrElse(web) / "out" / (entryPoint + ".outs")
//      IO.readLines(outs).map(filename => IO.asFile(new java.net.URL(filename)).relativeTo(web).get.toString())
//    
      
      val out = module.map(m => web / m).getOrElse(web) / "out"
      def gr(m: File => PathFinder)(f: File) = m(f) 
      val pf = gr(_ ** "*")(out)
      
      pf.getFiles.filterNot(_.isDirectory()).map(_.relativeTo(web).get.toString());
      
      
    } else {
      val boot = module.map(m => m + "/out").getOrElse("out") + "/" + entryPoint + "_bootstrap.dart"
      if (noJs)
        List(boot, boot + ".js")
      else
        List(boot)
    }
  }
}