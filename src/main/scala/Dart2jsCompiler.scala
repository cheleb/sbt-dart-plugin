package sbt

import sbt._
import sbt.Keys._
import play.Project._
import DartKeys._
import java.nio.file.Files

trait Dart2jsCompiler {
  def Dart2jsCompiler(name: String,
    watch: File => PathFinder,
    filesSetting: sbt.SettingKey[PathFinder],
    naming: String => String) = {

    (dartPluginDisabled, state, dartDirectory, dartEntryPoints, dartWebDirectory, filesSetting in Compile, resourceManaged in Compile, cacheDirectory, dartOptions) map { (disabled, state, dartDir, entryPoints, web, files, resources, cache, options) =>

      val r = if (disabled || entryPoints.isEmpty) {
        Nil
      } else {

        import java.io._

        val cacheFile = cache / name
        val currentInfos = watch(web).get.map(f => f -> FileInfo.lastModified(f)).toMap

        val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)

        if (previousInfo != currentInfos) {
          state.log.info("\t++++   " + name + "   ++++")

          //a changed file can be either a new file, a deleted file or a modified one
          lazy val changedFiles: Seq[File] = currentInfos.filter(e => !previousInfo.get(e._1).isDefined || previousInfo(e._1).lastModified < e._2.lastModified).map(_._1).toSeq ++ previousInfo.filter(e => !currentInfos.get(e._1).isDefined).map(_._1).toSeq
          val dependencies = previousRelation.filter((original, compiled) => changedFiles.contains(original))._2s
          dependencies.foreach(IO.delete)
          val t = System.currentTimeMillis()
          val generated: Seq[(File, java.io.File)] = (files x relativeTo(Seq(web))).flatMap {
            case (sourceFile, name) => {
              //   println(sourceFile)
              if (entryPoints.contains(name)) {

                if (changedFiles.contains(sourceFile) || dependencies.contains(new File(resources, "public/" + naming(name)))) {
                  val (debug, min, dependencies) = try {
                    DartCompiler.dart2js(dartDir, sourceFile, options)
                  } catch {
                    case e: AssetCompilationException => throw reportCompilationError(state, e)
                  }
                  val out = new File(resources, "public/" + naming(name))
                  IO.write(out, debug)
                  (dependencies ++ Seq(sourceFile)).toSet[File].map(_ -> out)
                } else {
                  previousRelation.filter((original, compiled) => original == sourceFile)._2s.map(sourceFile -> _)
                }
              } else {
                Nil
              }
            }
          }

          //write object graph to cache file 
          Sync.writeInfo(cacheFile,
            Relation.empty[File, File] ++ generated,
            currentInfos)(FileInfo.lastModified.format)
          println("Write cache in " + (System.currentTimeMillis() - t) + " s")

          // Return new files
          generated.map(_._2).distinct.toList

        } else {
          // Return previously generated files
          //state.log.info("    ----   " + name + "   ----")
          val ret = previousRelation._2s.toSeq
          ret
        }

      }
      println(r)
      r
    }
  }
  val dart2jsCompiler = Dart2jsCompiler(dartId + "-js-compiler",
    src => (src ** "*") --- (src / "packages" ** "*") --- (src / "out" ** "*"),
    dartFiles in Compile,
    _ + ".js")
}