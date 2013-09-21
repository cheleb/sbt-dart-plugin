package sbt

import java.nio.file.Files

object LinkerHelper {

  def insurePackagesLink(siblin: File, dartDir: File) {
    val packages = siblin.toPath().getParent().resolve("packages")
    if (!Files.isSymbolicLink(packages))
      synchronized {
        if (Files.isSymbolicLink(packages) || Files.exists(packages)) {
          if (!Files.isSymbolicLink(packages))
            sys.error(packages + " is a reserved folder name by dart and should not exist")
        } else {
          Files.createSymbolicLink(packages, (dartDir / "packages").toPath)
        }
      }
  }
}