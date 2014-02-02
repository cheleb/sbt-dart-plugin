package sbt

trait DartSdk {

  def dart2jsExePath = DartSdk.dart2jsExe.absolutePath

  def pubExePath = DartSdk.pubExe.absolutePath

  def dartExePath = DartSdk.dartExe.absolutePath
}

object DartSdk {
  import scala.util.Properties
  lazy val dartSdk: File = {

    val DART_SDK = System.getenv("DART_SDK")
    if (DART_SDK == null) {
      sys.error("DART_SDK env variable must be defined!")
    } else {
      val dartHome = new File(DART_SDK)
      if (dartHome.exists())
        dartHome
      else
        sys.error(dartHome + " does not exist!")
    }
  }

  def getSdkFile(fileName: String, windowsExtension: String) = {
    val path = dartSdk + fileName + (if (Properties.isWin) windowsExtension else "")
    val exe = new File(path)
    if (exe.exists())
      exe
    else
      sys.error(exe + " does not exist!")
  }
  
  lazy val pubExe: File = getSdkFile("/bin/pub",".bat")
  lazy val dart2jsExe: File = getSdkFile("/bin/dart2js",".bat")
  lazy val dartExe: File = getSdkFile("/bin/dart",".exe")
   

}

trait DartProcessor extends DartSdk {

  def runCommand(in: File, cmd: String, inputFile: File) {
    import scala.sys.process._
    val d2js = Process(cmd, in)

    var stdout = List[String]()
    var stderr = List[String]()
    val exit = d2js ! ProcessLogger((s) => stdout ::= s, (s) => stderr ::= s)

    if (exit != 0) {
      throw CompilationException(stdout.mkString("\n") + stderr.mkString("\n"), inputFile, None)
    }
  }

  /**
   * Compile dart file into javascript.
   * @param dartFile
   * @param options dart compiler options
   * @return (source, None, Seq(deps))
   */
  def dart2js(webui: Boolean, web: File, module: Option[String], entryPoint: String, options: Seq[String]): (File, File) = {

    val out = if (webui) "out/" else ""

    val entryPointPath = module.map(m => m + "/" + out).getOrElse(out) + entryPoint

    val cmd = dart2jsExePath + " " + options.mkString(" ") + " -o" + entryPointPath + ".js" + " " + entryPointPath

    val dartFile = web / entryPointPath

    runCommand(web, cmd, dartFile)

    (web / (entryPointPath + ".js"), web / (entryPointPath + ".js.deps"))

  }

  def resolves(public: File, module: Option[String], entryPoint: String): (File, File)

  def compile(dev: Boolean, noJs: Boolean, webDir: File, module: Option[String], entryPoint: String, public: File, options: Seq[String]): Option[File]

  def deployables(dev: Boolean, noJs: Boolean, web: File, module: Option[String], entryPoint: String): Seq[String]
}
