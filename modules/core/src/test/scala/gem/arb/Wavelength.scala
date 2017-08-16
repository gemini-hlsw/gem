// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package arb

import gem.math.{ Wavelength, WavelengthInÅngström }
import org.scalacheck._
import org.scalacheck.Gen._

import spire.math.UInt

trait ArbWavelength {

  implicit def arbWavelengthInÅngström: Arbitrary[WavelengthInÅngström] =
    Arbitrary(choose(0, Int.MaxValue).map(i => Wavelength.fromAngstroms(UInt(i))))

}

object ArbWavelength extends ArbWavelength
