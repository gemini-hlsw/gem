// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem

import libra.si.MetricUnit
import libra.QuantityOf
import spire.math.UInt

/** Mathematical data types for general use, not specific to the Gem model. */
package object math {

  type RA = RightAscension
  val  RA: RightAscension.type = RightAscension

  type Dec = Declination
  val  Dec:  Declination.type = Declination

  type Wavelength

  type Ångström = MetricUnit[1, Wavelength]

  type WavelengthInÅngström = QuantityOf[UInt, Wavelength, Ångström]

}
