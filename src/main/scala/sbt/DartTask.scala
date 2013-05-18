package sbt

import sbt.Keys._
import play.Project._

import sbt.ConfigKey.configurationToKey

import sbt.Scoped.t2ToTable2
import sbt.State.stateOps


trait DartTask extends DartKeys with Pub {

  
  lazy val dartPubInstall = (state, dartDirectory in Compile) map { (state, dartDir) =>
    
    
    val packages = dartDir / "packages"
    
    
    if(!packages.exists()) {
    	state.log.info("pub install")
        pub(dartDir, "install", state.log)      
    }
  }
  
}