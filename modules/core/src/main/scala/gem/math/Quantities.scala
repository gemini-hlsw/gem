// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package math

import libra.{ QuantityOf, Unit }
import spire.math.UInt

/**
 * Typeclass that can create a Quantity from a UInt
 * Useful to create generic encoders
 */
trait QuantityOfFromUInt[A, B <: Unit[A]] {
  def fromUInt(v: UInt): QuantityOf[UInt, A, B]
}
