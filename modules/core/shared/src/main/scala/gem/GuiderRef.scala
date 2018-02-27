// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem

import gem.enum.{ Guider, Instrument }

import cats._
import cats.implicits._


/** GuiderRef wraps a `Guider` enum instance, adding the associated `Instrument`
  * type.
  */
sealed trait GuiderRef {

  type I <: Instrument with Singleton

  def instrument: Instrument.Aux[I]

  def guider: Guider

}

object GuiderRef {

  type Aux[I0 <: Instrument with Singleton] =
    GuiderRef { type I = I0 }

  sealed abstract class Impl[I0 <: Instrument with Singleton](
    val instrument: Instrument.Aux[I0],
    val guider: Guider
  ) extends GuiderRef {
    type I = I0
  }


  case object F2OI    extends Impl(Instrument.Flamingos2, Guider.F2OI)
  case object GmosNOI extends Impl(Instrument.GmosN,      Guider.GmosNOI)
  case object GmosSOI extends Impl(Instrument.GmosS,      Guider.GmosSOI)

  final case class P1GN[I0 <: Instrument with Singleton](
    override val instrument: Instrument.Aux[I0]
  ) extends Impl(instrument, Guider.P1GN)

  final case class P1GS[I0 <: Instrument with Singleton](
    override val instrument: Instrument.Aux[I0]
  ) extends Impl(instrument, Guider.P1GS)

  final case class P2GN[I0 <: Instrument with Singleton](
    override val instrument: Instrument.Aux[I0]
  ) extends Impl(instrument, Guider.P2GN)

  final case class P2GS[I0 <: Instrument with Singleton](
    override val instrument: Instrument.Aux[I0]
  ) extends Impl(instrument, Guider.P2GS)

  final implicit def OrderGuiderRef[I <: Instrument with Singleton]: Order[GuiderRef.Aux[I]] =
    Order.by(gr => (gr.instrument.tag, gr.guider))

  final implicit def OrderGuiderRef2: Order[GuiderRef] =
    Order.by(gr => (gr.instrument.tag, gr.guider))
}
