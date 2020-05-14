package $package$

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

abstract class BaseTest extends FreeSpec with Matchers with ScalaCheckPropertyChecks with TypeCheckedTripleEquals
