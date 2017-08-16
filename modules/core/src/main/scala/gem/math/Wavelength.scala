// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem.math

import scalaz.{ Order, Show }
import spire.implicits._
import spire.math.UInt
import libra._

object Wavelength {
  implicit final class BaseQuantityOps[A](val a: A) extends AnyVal {
    /** Syntax to create a quantity using the Å symbol */
    def Å: QuantityOf[A, Wavelength, Ångström] = Quantity(a)
  }

  /** Construct a WavelengthInÅngström from unsigned integer angstroms. */
  def fromAngstroms(angstroms: UInt): WavelengthInÅngström =
    angstroms.Å

}

object WavelengthInÅngström {
  import Wavelength._

  final lazy val ZeroÅngström: WavelengthInÅngström = ui"0".Å
  final lazy val MinWavelengthInÅngström: WavelengthInÅngström = ZeroÅngström
  final lazy val MaxWavelengthInÅngström: WavelengthInÅngström = UInt.MaxValue.Å

  /** @group Typeclass Instances */
  implicit val WavelengthInÅngströmOfUint: QuantityOfFromUInt[Wavelength, Ångström] = new QuantityOfFromUInt[Wavelength, Ångström] {
    def fromUInt(v: UInt): WavelengthInÅngström =
      fromAngstroms(v)
  }

  /** @group Typeclass Instances */
  implicit val WavelengthShow: Show[WavelengthInÅngström] =
    Show.shows(x => f"WavelengthInÅngström(${x.value} Å)")

  /** @group Typeclass Instances */
  implicit val WavelengthOrd: Order[WavelengthInÅngström] =
    Order.orderBy(_.value)

}
