library SoSimple;

import 'dart:async';
import 'dart:io';


import "package:web_ui/dwc.dart" as dwc;
import "package:web_ui/src/options.dart";
import "package:web_ui/src/compiler.dart";

void main() {
  
  var o = new Options();
  
  var co = CompilerOptions.parse(o.arguments);
  
  dwc.run(o.arguments).then((result) {
    if(result.success){
      dumpDependencies(co.outputDir, co.inputFile, result.inputs);
    }else{
      exit(1);
    }
  });
}


Future dumpDependencies(String outputDir, String entryPoint, List deps){
  var stream = new File(entryPoint.replaceFirst("web/", "web/out/") + ".deps").openWrite();
  deps.forEach((d){
    File dep = new File(d);
    stream.write("file://" + dep.fullPathSync() + "\n");
  });
  
  return stream.close();  
  
}
