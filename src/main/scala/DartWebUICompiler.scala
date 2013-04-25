package sbt

import sbt._
import sbt.Keys._
import play.Project._
import DartKeys._
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import org.apache.ivy.util.FileUtil

trait DartWebUICompiler {
  def DartWebUICompiler(name: String,
    watch: File => PathFinder) = {

    (dartPluginDisabled, state, dartWebDirectory, dartWebUIDirectory, dartPackagesDirectory, dartWebUIEntryPoints, dartWebUIPublicDirectory, dartPublicPackagesLink, dartWebUIPublicPackagesLink, dartDirectory in Compile, resourceManaged in Compile, cacheDirectory, dartOptions) map { (disabled, state, web, webui, dartPackages, entryPoints, webUIPublic, packagesLink, webUIPackagesLink, dartDir, resources, cache, options) =>

      if (disabled || entryPoints.isEmpty) {
        //No webui declared.
        Nil
      } else {

        if (webUIPublic.mkdirs()) {
          Files.createSymbolicLink(webUIPackagesLink.toPath(), webUIPublic.toPath().relativize(packagesLink.toPath()))
          state.log.info("Add package symlink")
        } else {

        }

        import java.io._

        val cacheFile = cache / name
        val currentInfos = watch(web).get.map(f => f -> FileInfo.lastModified(f)).toMap

        val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)

        if (previousInfo != currentInfos) {

          val pub = resources / "public"
          state.log.info("\t++++   " + name + "   ++++")

          val out = resources / "public" / "out"
          if (out.mkdirs()) {
            state.log.info("Create dir: " + out)
          }

          val t = System.currentTimeMillis()

          DartCompiler.compileWebUI(dartDir, Seq.empty[String])

          entryPoints.foreach {
            entry =>
              DartCompiler.dart2jsWithTmp(dartDir, webui / (entry + "_bootstrap.dart"), webui / (entry + "_bootstrap.dart.js"), options :+ "--package-root=packages/")
          }

          val generated = DartCompiler.allFilesIn(new File(webui.absolutePath), f => !f.relativeTo(webui).getOrElse(new File("packages/")).toPath().startsWith("packages/")).map {
            e =>
              val rel = e.relativeTo(web).get.toString()

              val dst = new File(pub, rel)

              (dst, e.relativeTo(web).get)

          }

          //write object graph to cache file 
          Sync.writeInfo(cacheFile,
            Relation.empty[File, File] ++ generated,
            currentInfos)(FileInfo.lastModified.format)

          state.log.debug("Write cache in " + (System.currentTimeMillis() - t) + " s")

          // Return new files
          generated.map(_._1).distinct.toList

        } else {
          // Return previously generated files
          //state.log.debug("    ----   " + name + "   ----")
          val ret = previousRelation._1s.toSeq
          ret
        }
      }

    }
  }
  val dartWebUICompiler = DartWebUICompiler(dartId + "-web_ui-compiler",
    src => (src ** "*") --- (src / "out" ** "*"))
}