package sbt

import sbt._
import sbt.Keys._
import play.Project._
import DartKeys._
import java.nio.file.Files

trait Dart2jsCompiler {
  def Dart2jsCompiler(name: String,
    entryPoints: SettingKey[Seq[String]],
    watch: File => PathFinder,
    naming: String => String,
    compiler: (File, String, File, File, File, File, File, Seq[String], Boolean) => Seq[sbt.File],
    pureDart: Boolean,
    output: SettingKey[File]) = {

    (dart2js, state, dartDirectory, entryPoints, dartWebDirectory in Compile,dartLibDirectory in Compile, dartWebUIDirectory, output, cacheDirectory, dartOptions) map { (dart2js, state, dartDir, entryPoints, web, lib, webUIOutput, output, cache, options) =>

      if (!dart2js && pureDart) {
        if(!dart2js)
          state.log.debug("dart2js skipped")
        Nil
      } else {

        import java.io._

        val cacheFile = cache / name
        val currentInfos = (watch(web).get ++ watch(lib).get).map(f => f -> FileInfo.lastModified(f)).toMap

        val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)

        if (previousInfo != currentInfos) {
          state.log.info("\t++++   " + name + "   ++++")

          output.mkdirs();

          //a changed file can be either a new file, a deleted file or a modified one
          lazy val changedFiles: Seq[File] = currentInfos.filter(e => !previousInfo.get(e._1).isDefined || previousInfo(e._1).lastModified < e._2.lastModified).map(_._1).toSeq ++ previousInfo.filter(e => !currentInfos.get(e._1).isDefined).map(_._1).toSeq

          state.log.debug("Changed files: \n" + changedFiles)

          val dependencies = previousRelation.filter((original, compiled) => changedFiles.contains(original))

          state.log.debug("Dependencies: \n" + dependencies)

          dependencies._2s.foreach(IO.delete)

          val t = System.currentTimeMillis()

          def checkIfEntryPointShouldBeCompiled(entryPointFile: File, shakedTreeFile: File) =
            dependencies._2s.contains(shakedTreeFile) || !previousRelation._1s.contains(entryPointFile)

          val generated = entryPoints.map(name => targetFiles(web, if(pureDart)None else Some(webUIOutput), output, name, naming, checkIfEntryPointShouldBeCompiled)).flatMap {
            case (entryPoint, entryPointFile, shakedTreeFile, jsFile, depsFile, compile) =>
              if (compile) {
                state.log.info("Recompile: " + entryPoint)

                val dependencies = try {
                  compiler(dartDir, entryPoint, entryPointFile, shakedTreeFile, jsFile, depsFile, output, options, dart2js)
                } catch {
                  case e: AssetCompilationException => throw reportCompilationError(state, e)
                }

                state.log.info("Updated: \n\t" + shakedTreeFile + "\n\t" + jsFile)

                (dependencies ++ Seq(entryPointFile)).toSet[File].map(_ -> jsFile) ++
                  (dependencies ++ Seq(entryPointFile)).toSet[File].map(_ -> shakedTreeFile)

              } else {
                state.log.info("Cached: " + entryPoint)
                previousRelation.filter((original, compiled) => compiled == jsFile)._1s.map(_ -> jsFile) ++
                  previousRelation.filter((original, compiled) => compiled == shakedTreeFile)._1s.map(_ -> shakedTreeFile)
              }
          }

          //write object graph to cache file

          state.log.debug("Generated: \n" + generated)

          Sync.writeInfo(cacheFile,
            Relation.empty[File, File] ++ generated,
            currentInfos)(FileInfo.lastModified.format)
          state.log.info("Write cache in " + (System.currentTimeMillis() - t) + " s")

          // Return new files
          generated.map(_._2).distinct.toList

        } else {
          // Return previously generated files
          state.log.info("\t----   " + name + "   ----")
          previousRelation._2s.toSeq
        }

      }
    }
  }

  def targetFiles(web: File, tmpOutput: Option[File], output: File, entryPoint: String, naming: String => String, test: (File, File) => Boolean): (String, File, File, File, File, Boolean) = {
    val entryPointFile = web / entryPoint
    val shakedTreeFile = output / naming(entryPoint)
    val jsFile = output / naming(entryPoint).+(".js")
    val depsFile = tmpOutput.getOrElse(output) / entryPoint.+(".deps")
    (entryPoint, entryPointFile, shakedTreeFile, jsFile, depsFile, test(entryPointFile, shakedTreeFile))
  }

  val dart2jsCompiler = Dart2jsCompiler(dartId + "-js-compiler",
    dartEntryPoints,
    src => (src ** "*") --- (src / "out" ** "*"),
    name => name,
    DartCompiler.js,
    true,
    dartPublicManagedResources in Compile)

  val dartWebUICompiler = Dart2jsCompiler(dartId + "-js-web_ui-compiler",
    dartWebUIEntryPoints,
    src => (src ** "*") --- (src / "out" ** "*"),
    _ + "_bootstrap.dart",
    DartCompiler.wuic,
    false,
    (dartPublicWebUIManagedResources in Compile))
}