// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem

import libra.si.MetricUnit
import libra.QuantityOf
import spire.math.UInt

import scalaz.Order

/** Mathematical data types for general use, not specific to the Gem model. */
package object math {

  type RA = RightAscension
  val  RA: RightAscension.type = RightAscension

  type Dec = Declination
  val  Dec:  Declination.type = Declination

  /**
   * Type of Wavelength quantities
   */
  type Wavelength

  /**
   * Ångström is a unit for Wavelenegth
   */
  type Ångström = MetricUnit[1, Wavelength]

  /**
   * Exact wavelengths represented as unsigned integral angstroms in the range [0 .. UInt.MaxValue]
   * which means the largest representable wavelength is 214.7483647 mm.
   */
  type WavelengthInÅngström = QuantityOf[UInt, Wavelength, Ångström]

  // Generic scalaz order out of spire order
  implicit def scalazOrder[A](implicit ordering: spire.algebra.Order[A]): Order[A] =
    Order.fromScalaOrdering(spire.compat.ordering)

}
