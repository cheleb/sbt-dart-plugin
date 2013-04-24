package sbt

import Keys._
import PlayKeys._
import DartKeys._
import PlayExceptions._
import play.api.PlayException
import java.nio.file.Files

trait DartPlayAssetDeployer {

  // ----- Assets

  // Name: name of the compiler
  // files: the function to find files to compile from the assets directory
  // naming: how to name the generated file from the original file and whether it should be minified or not
  // compile: compile the file and return the compiled sources, the minified source (if relevant) and the list of dependencies
  def DartPlayAssetDeployer(name: String,
    watch: File => PathFinder,
    filesSetting: sbt.SettingKey[PathFinder],
    naming: (String, Boolean) => String,
    compile: (File, Seq[String]) => (String, Option[String], Seq[File]),
    optionsSettings: sbt.SettingKey[Seq[String]]) =
    (dartPluginDisabled, state, dartPublicDirectory, dartPackagesDirectory, dartWebDirectory, dartWebPackageLink, dartPublicPackagesLink, resourceManaged in Compile, cacheDirectory, optionsSettings, filesSetting, requireJs) map { (disabled, state, public, dartPackages, web, webPackages, packagesLink, resources, cache, options, files, requireJs) =>

      if (disabled) {
        Nil
      } else {

        if (Files.isSymbolicLink(packagesLink.toPath())) {
          state.log.debug(packagesLink + " OK");
        } else {
          Files.createSymbolicLink(packagesLink.toPath(), public.toPath().relativize(dartPackages.toPath()))
          state.log.info("Add package symlink: " + packagesLink)
        }

        if (Files.isSymbolicLink(webPackages.toPath())) {
          state.log.debug(packagesLink + " OK");
        } else {
          Files.createSymbolicLink(webPackages.toPath(), web.toPath().relativize(packagesLink.toPath()))
          state.log.info("Add package symlink: " + packagesLink)
        }

        import java.io._

        val cacheFile = cache / name
        val currentInfos = watch(web).get.map(f => f -> FileInfo.lastModified(f)).toMap

        val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)

        if (previousInfo != currentInfos) {

          state.log.info("++++ " + name + " ++++ ")

          //a changed file can be either a new file, a deleted file or a modified one
          lazy val changedFiles: Seq[File] = currentInfos.filter(e => !previousInfo.get(e._1).isDefined || previousInfo(e._1).lastModified < e._2.lastModified).map(_._1).toSeq ++ previousInfo.filter(e => !currentInfos.get(e._1).isDefined).map(_._1).toSeq

          //erease dependencies that belong to changed files
          val dependencies = previousRelation.filter((original, compiled) => changedFiles.contains(original))._2s
          dependencies.foreach(IO.delete)

          /**
           * If the given file was changed or
           * if the given file was a dependency,
           * otherwise calculate dependencies based on previous relation graph
           */
          val generated: Seq[(File, java.io.File)] = (files x relativeTo(Seq(web))).flatMap {
            case (sourceFile, name) => {
              if (name.startsWith("packages/") || name.startsWith("out/"))
                Nil
              else if (changedFiles.contains(sourceFile) || dependencies.contains(new File(resources, "public/" + naming(name, false)))) {
                val (debug, min, dependencies) = try {
                  compile(sourceFile, options)
                } catch {
                  case e: AssetCompilationException => throw reportCompilationError(state, e)
                }
                val out = new File(resources, "public/" + naming(name, false))
                IO.write(out, debug)
                (dependencies ++ Seq(sourceFile)).toSet[File].map(_ -> out) ++ min.map { minified =>
                  val outMin = new File(resources, "public/" + naming(name, true))
                  IO.write(outMin, minified)
                  (dependencies ++ Seq(sourceFile)).map(_ -> outMin)
                }.getOrElse(Nil)
              } else {
                previousRelation.filter((original, compiled) => original == sourceFile)._2s.map(sourceFile -> _)
              }
            }
          }

          //write object graph to cache file 
          Sync.writeInfo(cacheFile,
            Relation.empty[File, File] ++ generated,
            currentInfos)(FileInfo.lastModified.format)

          // Return new files
          generated.map(_._2).distinct.toList

        } else {
          // Return previously generated files
          previousRelation._2s.toSeq
        }
      }
    }

  val dart2dartCompiler = DartPlayAssetDeployer(dartId + "-dart2dart",
    (_ ** "*.*"),
    dartResources in Compile,
    { (name, min) => name },
    { DartCompiler.dart2dart _ },
    dartOptions in Compile)

  def reportCompilationError(state: State, error: PlayException.ExceptionSource) = {
    val log = state.log
    // log the source file and line number with the error message
    log.error(Option(error.sourceName).getOrElse("") + Option(error.line).map(":" + _).getOrElse("") + ": " + error.getMessage)
    Option(error.interestingLines(0)).map(_.focus).flatMap(_.headOption) map { line =>
      // log the line
      log.error(line)
      Option(error.position).map { pos =>
        // print a carat under the offending character
        val spaces = (line: Seq[Char]).take(pos).map {
          case '\t' => '\t'
          case x => ' '
        }
        log.error(spaces.mkString + "^")
      }
    }
    error
  }

}
  
