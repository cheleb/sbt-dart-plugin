package net.orcades.sbt.dart

import sbt._
import Keys._
import java.lang.ProcessBuilder
import java.lang.Process
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.SimpleFileVisitor
import java.nio.file.FileVisitResult
import java.nio.file.attribute.BasicFileAttributes

object DartPlugin extends Plugin {
  
  
 
  
  
  
  override lazy val settings = Seq(commands += myCommand)

  val outputDir = new File("dart")

  val inputDir = new File("web")

  val mainDarts = SettingKey[Seq[String]]("mainDarts")

  lazy val myCommand =
    Command.command("dart2js") { (state: State) =>
      println("Yo dart! ")

      val extracts = Project.extract(state)

      val dartHome = System.getenv("DART_HOME")
      if (dartHome == null) {
        println("DART_HOME env not defined")
        state.fail
      } else {
        val path = dartHome + "/bin/dart2js"
        val dart2jsExe = new File(path)
        if (dart2jsExe.exists()) {

          Files.walkFileTree(inputDir.toPath(), CopyDirVisitor(inputDir.toPath(), outputDir.toPath()))

          extracts.get(mainDarts) foreach (dart => dart2js(state, dart2jsExe, dart))

        } else {
          print(dart2jsExe.absolutePath)
          println(" does not exist!")
          state.fail
        }
      }
      state

    }

  def dart2js(state: State, dart2jsExe: File, dartFile: String) = {
    println("dart2js: " + dart2jsExe.absolutePath)
    outputDir.ensuring(true).mkdir()

    val processBuilder = new ProcessBuilder(
      dart2jsExe.absolutePath,
      getJSPathParameter(dartFile), inputDir.absolutePath
        + "/" + dartFile);
    println(processBuilder.command());

    val process = processBuilder.start();

    val waitFor = process.waitFor();
    if (waitFor != 0) {
      val bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))

      var line = bufferedReader.readLine()
      while (line != null) {
        println(line);
        line = bufferedReader.readLine
      }

    }

    println("Process ended: " + waitFor);

    state
  }

  private def getJSPathParameter(path: String): String = {
    val file = new File(inputDir, path);
    val filename = file.getName();
    //val i = filename.lastIndexOf(".dart");
    //val jsFilename = filename.substring(0, i) + "dart.js";
    val jsFilename = filename + ".js"
    "-o" + outputDir.absolutePath + "/" + jsFilename;

  }
  
  
   resourceGenerators in Compile <+=
  (resourceManaged in Compile, name, version) map { (dir, n, v) =>
    val file = dir / "demo" / "myapp.properties"
    val contents = "name=%s\nversion=%s".format(n,v)
    IO.write(file, contents)
    Seq(file)
  }

  
}

case class CopyDirVisitor(val fromPath: Path, val toPath: Path) extends SimpleFileVisitor[Path] {

  val copyOption = StandardCopyOption.REPLACE_EXISTING;

  override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {

    val targetPath = toPath.resolve(fromPath.relativize(dir));

    if (!Files.exists(targetPath)) {

      Files.createDirectory(targetPath);

    }

    FileVisitResult.CONTINUE;

  }

  override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {

    Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);

    return FileVisitResult.CONTINUE;

  }

}

