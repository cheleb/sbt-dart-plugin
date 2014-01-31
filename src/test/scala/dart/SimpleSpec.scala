package dart

import org.specs2.mutable.Specification

object SimpleSpec extends Specification {
  
  "First test" should {
    "be easy " in {
      val l = List(1,2,3)
      l must not beNull
    }
  }

}