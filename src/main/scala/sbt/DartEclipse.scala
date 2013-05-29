package sbt

trait DartEclipse {

  import com.typesafe.sbteclipse.core.EclipsePlugin._
  import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys._
  import com.typesafe.sbteclipse.core.Validation
  import scala.xml._
  import scala.xml.transform.RewriteRule
  import scalaz.Scalaz._

  def dartProjectTransform =
    EclipseKeys.projectTransformerFactories := Seq(new EclipseTransformerFactory[RewriteRule] {

      override def createTransformer(ref: ProjectRef, state: State): Validation[RewriteRule] = {
        val rule = new RewriteRule {
          override def transform(node: scala.xml.Node): Seq[scala.xml.Node] = node match {
            case elem if (elem.label === "projectDescription") =>
              val newChild = elem.child ++ <filteredResources>
                                             <filter>
                                               <id>{ System.currentTimeMillis }</id>
                                               <name></name>
                                               <type>30</type>
                                               <matcher>
                                                 <id>com.google.dart.tools.core.packagesFolderMatcher</id>
                                               </matcher>
                                             </filter>
                                           </filteredResources>
              Elem(elem.prefix, "projectDescription", elem.attributes, elem.scope, newChild: _*)
            case elem if (elem.label === "natures") =>
              val newChild = elem.child ++ <nature>com.google.dart.tools.core.dartNature</nature>
              Elem(elem.prefix, "natures", elem.attributes, elem.scope, newChild: _*)
            case elem if (elem.label === "buildSpec") =>
              val newChild = elem.child ++ <buildCommand>
                                             <name>com.google.dart.tools.core.dartBuilder</name>
                                             <arguments>
                                             </arguments>
                                           </buildCommand>
              Elem(elem.prefix, "buildSpec", elem.attributes, elem.scope, newChild: _*)
            case other =>
              other
          }
        }
        rule.success
      }
    })

}