// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem.math

import scalaz.{ Order, Show }
import scalaz.std.anyVal.intInstance
// import scalaz.syntax.equal._
//
/**
 * Exact WavelengthInÅngströms represented as unsigned integral angstroms in the range [0 .. Int.MaxValue]
 * which means the largest representable WavelengthInÅngström is 214.7483647 mm.
 * @param toAngstroms This WavelengthInÅngström in integral angstroms (10^-10 of a meter).
 */
/*final class WavelengthInÅngström private (val toAngstroms: Int) {

  // Sanity checks … should be correct via the companion constructor.
  assert(toAngstroms >= 0, s"Invariant violated. $toAngstroms is negative.")
  assert(toAngstroms <= Int.MaxValue, s"Invariant violated. $toAngstroms is larger than Int.MaxValue.")*/

  /** String representation of this WavelengthInÅngström, for debugging purposes only. */
  // override def toString =
  //   f"WavelengthInÅngström($toAngstroms Å)"

  /** WavelengthInÅngströms are equal if their magnitudes are equal. */
//   override def equals(a: Any) =
//     a match {
//       case a: WavelengthInÅngström => a.toAngstroms === toAngstroms
//       case _             => false
//     }
//
//   override def hashCode =
//     toAngstroms
//
// }
//
object Wavelength {
  import spire.implicits._
  import libra._

  implicit final class BaseQuantityOps[A](val a: A) extends AnyVal {

    def Å: QuantityOf[A, Wavelength, Ångström] = Quantity(a)
  }

  final lazy val ZeroAngstroms: WavelengthInÅngström = 0.Å

  /** Construct a WavelengthInÅngström from integral angstroms, if non-negative. */
  def fromAngstroms(angstroms: Int): Option[WavelengthInÅngström] =
    Some(angstroms).filter(_ >= 0).map(_.Å)

  /** Construct a WavelengthInÅngström from integral angstroms, raising an exception if negative. */
  def unsafeFromAngstroms(angstroms: Int): WavelengthInÅngström =
    fromAngstroms(angstroms).getOrElse(sys.error(s"Negative Wavelength: $angstroms"))
}

object WavelengthInÅngström {

  /** @group Typeclass Instances */
  implicit val WavelengthShow: Show[WavelengthInÅngström] =
    Show.shows(x => f"WavelengthInÅngström(${x.value} Å)")

  /** @group Typeclass Instances */
  implicit val WavelengthOrd: Order[WavelengthInÅngström] =
    Order.orderBy(_.value)

}
