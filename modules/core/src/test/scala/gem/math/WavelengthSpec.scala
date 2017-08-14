// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem.math

import gem.arb._
import gem.math.WavelengthInÅngström._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ FlatSpec, Matchers }

import scalaz.{ Equal, Order }
import scalaz.std.anyVal._

@SuppressWarnings(Array("org.wartremover.warts.ToString", "org.wartremover.warts.Equals"))
class WavelengthSpec extends FlatSpec with Matchers with PropertyChecks {
  import ArbWavelength._

  "Equality" must "be natural" in {
    forAll { (a: WavelengthInÅngström, b: WavelengthInÅngström) =>
      a.equals(b) shouldEqual Equal[WavelengthInÅngström].equal(a, b)
    }
  }

  "Order" must "be consistent with .toAngstroms" in {
    forAll { (a: WavelengthInÅngström, b: WavelengthInÅngström) =>
      Order[Int].order(a.value, b.value) shouldEqual
      Order[WavelengthInÅngström].order(a, b)
    }
  }

  /*"Show" must "be natural" in {
    forAll { (a: WavelengthInÅngström) =>
      a.toString shouldEqual Show[WavelengthInÅngström].shows(a)
    }
  }*/

  "Conversion to angstroms" must "be invertable" in {
    forAll { (a: WavelengthInÅngström) =>
      Wavelength.fromAngstroms(a.value) shouldEqual Some(a)
    }
  }

  "Construction from an arbitrary Int" must "not allow negative values" in {
    forAll { (n: Int) =>
      Wavelength.fromAngstroms(n).isDefined shouldEqual n >= 0
    }
  }

}
