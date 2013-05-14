package sbt


import sbt.Keys._
import play.Project._
import DartKeys._


import sbt.ConfigKey.configurationToKey

import sbt.Scoped.t12ToTable12

import sbt.State.stateOps


trait Dart2jsCompiler {

  /**
   * Sync the resources from app/dart/web to cache/web.
   * This step is needed (AFAIU) to avoid relative path issues when generating sourcemap (when output are redirected).
   */
  def syncWorkingFiles(state: State, web: File, lib: File, work: File) {
    val webwork = work / "web"
    webwork.mkdirs()
    val copyCacheFile = work / "timestamps"

    val watch = (base: File) => (base ** "*.*")

    val currentInfos = (watch(web).get ++ watch(lib).get).map(f => f -> FileInfo.lastModified(f)).toMap

    val (previousRelation, previousInfo) = Sync.readInfo(copyCacheFile)(FileInfo.lastModified.format)

    if (previousInfo != currentInfos) {

      //a changed file can be either a new file, a deleted file or a modified one
      lazy val changedFiles: Seq[File] = currentInfos.filter(e => !previousInfo.get(e._1).isDefined || previousInfo(e._1).lastModified < e._2.lastModified).map(_._1).toSeq ++ previousInfo.filter(e => !currentInfos.get(e._1).isDefined).map(_._1).toSeq

      val dependencies = previousRelation.filter((original, compiled) => changedFiles.contains(original))

      dependencies._2s.foreach(IO.delete)

      val dartAssets = watch(web)

      /**
       * If the given file was changed or
       * if the given file was a dependency,
       * otherwise calculate dependencies based on previous relation graph
       */
      val generated: Seq[(File, java.io.File)] = (dartAssets x relativeTo(Seq(web, lib))).flatMap {
        case (sourceFile, name) => {
          if (changedFiles.contains(sourceFile)) {

            state.log.debug("dart work <- " + sourceFile)

            val targetFile = webwork / name

            IO.copyFile(sourceFile, targetFile, true)

            List((sourceFile, targetFile))
          } else {
            previousRelation.filter((original, compiled) => original == sourceFile)._2s.map(sourceFile -> _)
          }
        }
      }
      Sync.writeInfo(copyCacheFile,
        Relation.empty[File, File] ++ generated,
        currentInfos)(FileInfo.lastModified.format)

      // Return new files
      generated.map(_._2).distinct.toList

    } else {
      state.log.debug("\t----   " + name + "   ----")
      // Return previously generated files
      previousRelation._2s.toSeq
    }

  }

  def Dart2jsCompiler(name: String,
    entryPoints: SettingKey[Seq[String]],
    watch: File => PathFinder,
    proc: DartProcessor) = {

    (dartVerbose, dartDev, dartNoJs, state, dartDirectory, entryPoints, dartWebDirectory in Compile, dartLibDirectory in Compile, dartWebUIDirectory, dartPublicManagedResources in Compile, cacheDirectory, dartOptions) map { (verbose, dev, noJs, state, dartDir, entryPoints, webSrc, lib, webUIOutput, public, cache, options) =>

      import java.io._

      val work = cache / "dart"
      val web = work / "web"

      synchronized(syncWorkingFiles(state, webSrc, lib, work))

      val cacheFile = cache / name

      val currentInfos = watch(web).get.map(f => f -> FileInfo.lastModified(f)).toMap

      val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)

      if (previousInfo != currentInfos) {
        if (verbose)
          state.log.info("\t++++   " + name + "   ++++")

        //a changed file can be either a new file, a deleted file or a modified one
        lazy val changedFiles: Seq[File] = currentInfos.filter(e => !previousInfo.get(e._1).isDefined || previousInfo(e._1).lastModified < e._2.lastModified).map(_._1).toSeq ++ previousInfo.filter(e => !currentInfos.get(e._1).isDefined).map(_._1).toSeq

        if (verbose)
          changedFiles.foreach(f => state.log.info("Changed files: " + f))

        val dependencies = previousRelation.filter((original, compiled) => changedFiles.contains(original))

        if (verbose)
          dependencies._2s.foreach(rel => state.log.info("delete: " + rel.getAbsolutePath()))

        dependencies._2s.foreach(IO.delete)

        val t = System.currentTimeMillis()

        def checkIfEntryPointShouldBeCompiled(entryPointFile: File, dartFile: File, jsFile: File) =
          dependencies._2s.contains(jsFile) || dependencies._2s.contains(dartFile) || !previousRelation._1s.contains(entryPointFile)

        val generated = entryPoints.map(name => targetFiles(web, public, name, proc.resolves, checkIfEntryPointShouldBeCompiled)).flatMap {
          case (module, entryPoint, entryPointFile, compile) =>

            if (compile) {
              state.log.info(" dart - processing: " + entryPoint)

              val (dependencies, outs) = try {
                val (deps, outs) = proc.compile(web, module, entryPoint, public, options, dev)
                (IO.readLines(deps).map(filename => IO.asFile(new java.net.URL(filename))), outs)
              } catch {
                case e: AssetCompilationException => throw reportCompilationError(state, e)
              }

              val deployables = proc.deployables(dev, web, module, entryPoint)

              // println(name + " deploy: " + deployables)

              val gen = deployables.map(path => (web / path, public / path)) flatMap {
                case (src, dst) =>
                  IO.copyFile(src, dst, true)
                  if (verbose)
                    state.log.info("deployed: " + dst)
                  (dependencies ++ Seq(entryPointFile)).toSet[File].map(_ -> dst)
              }
              if (verbose)
                gen.map { case (s, d) => d }.distinct.foreach(f => state.log.info("Generated: " + f))
              gen

            } else {
              if (verbose)
                state.log.info("Cached: " + entryPoint)
              val deployables = proc.deployables(dev, web, module, entryPoint)

              deployables.map(path => public / path) flatMap (dst => previousRelation.filter((original, compiled) => compiled == dst)._1s.map(_ -> dst))

            }
        }

        //write object graph to cache file

        Sync.writeInfo(cacheFile,
          Relation.empty[File, File] ++ generated,
          currentInfos)(FileInfo.lastModified.format)
        if (verbose)
          state.log.info("Write cache in " + (System.currentTimeMillis() - t) + " ms")

        // Return new files
        generated.map(_._2).distinct.toList

      } else {
        // Return previously generated files
        state.log.debug("\t----   " + name + "   ----")
        previousRelation._2s.toSeq
      }

    }
  }

  def parseEntryPoint(path: String) = {
    val i = path.lastIndexOf("/")
    if (i == -1)
      (None, path)
    else
      (Some(path.substring(0, i)), path.substring(i + 1))
  }

  def targetFiles(web: File, public: File, entryPointPath: String, resolve: (File, Option[String], String) => (File, File), compile: (File, File, File) => Boolean): (Option[String], String, File, Boolean) = {

    val (module, entryPoint) = parseEntryPoint(entryPointPath)

    val entryPointFile = web / entryPointPath

    val (dartFile, jsFile) = resolve(public, module, entryPoint)

    (module, entryPoint, entryPointFile, compile(entryPointFile, dartFile, jsFile))
  }


  val dart2jsCompiler = Dart2jsCompiler(dartId + "-js-compiler",
    dartEntryPoints,
    src => (src ** "*") --- (src ** "*.dart.*") --- (src ** "out" ** "*"),
    dart2jsProcessor)

  val dartWebUICompiler = Dart2jsCompiler(dartId + "-js-web_ui-compiler",
    dartWebUIEntryPoints,
    src => (src ** "*") --- (src ** "*.dart.*") --- (src ** "out" ** "*"),
    dartWebUIProcessor)
}