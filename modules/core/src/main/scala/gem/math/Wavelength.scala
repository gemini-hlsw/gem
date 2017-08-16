// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem.math

import scalaz.{ Order, Show }
import spire.implicits._
import spire.math.UInt
import libra._

object Wavelength {
  implicit final class BaseQuantityOps[A](val a: A) extends AnyVal {

    def Å: QuantityOf[A, Wavelength, Ångström] = Quantity(a)
  }

  implicit val WavelengthInÅngströmOfUint: QuantityOfFromUInt[Wavelength, Ångström] = new QuantityOfFromUInt[Wavelength, Ångström] {
    def fromUInt(v: UInt): QuantityOf[UInt, Wavelength, Ångström] =
      fromAngstroms(v)
  }

  final lazy val ZeroÅngström: WavelengthInÅngström = ui"0".Å
  final lazy val MinWavelengthInÅngström = ZeroÅngström
  final lazy val MaxWavelengthInÅngström = UInt.MaxValue.Å

  /** Construct a WavelengthInÅngström from unsigned integer angstroms. */
  def fromAngstroms(angstroms: UInt): WavelengthInÅngström =
    angstroms.Å

}

object WavelengthInÅngström {
  // Generic scalaz order out of spire order
  implicit def scalazOrder[A](implicit ordering: spire.algebra.Order[A]): Order[A] =
    Order.fromScalaOrdering(spire.compat.ordering)

  /** @group Typeclass Instances */
  implicit val WavelengthShow: Show[WavelengthInÅngström] =
    Show.shows(x => f"WavelengthInÅngström(${x.value} Å)")

  /** @group Typeclass Instances */
  implicit val WavelengthOrd: Order[WavelengthInÅngström] =
    Order.orderBy(_.value)

}
