// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package enum

import cats.instances.string._
import cats.syntax.eq._
import gem.util.Enumerated

/**
 * Enumerated type for guider.
 * @group Enumerations (Generated)
 */
sealed abstract class Guider(
  val tag: String,
  val instrument: Option[Instrument],
  val shortName: String,
  val longName: String
) extends Product with Serializable {
  type Self = this.type
}

object Guider {

  type Aux[A] = Guider { type Self = A }

  /** @group Constructors */ case object F2Oi extends Guider("F2Oi", Some(Instrument.Flamingos2), "F2 OI", "Flamingos2 OIWFS")
  /** @group Constructors */ case object GmosSOi extends Guider("GmosSOi", Some(Instrument.GmosS), "GMOS-S OI", "GMOS South OIWFS")
  /** @group Constructors */ case object GmosNOi extends Guider("GmosNOi", Some(Instrument.GmosN), "GMOS-N OI", "GMOS North OIWFS")
  /** @group Constructors */ case object P1Gn extends Guider("P1Gn", None, "P1 GN", "PWFS1 North")
  /** @group Constructors */ case object P1Gs extends Guider("P1Gs", None, "P2 GS", "PWFS2 North")
  /** @group Constructors */ case object P2Gn extends Guider("P2Gn", None, "P2 GN", "PWFS2 South")
  /** @group Constructors */ case object P2Gs extends Guider("P2Gs", None, "P2 GS", "PWFS2 South")

  /** All members of Guider, in canonical order. */
  val all: List[Guider] =
    List(F2Oi, GmosSOi, GmosNOi, P1Gn, P1Gs, P2Gn, P2Gs)

  /** Select the member of Guider with the given tag, if any. */
  def fromTag(s: String): Option[Guider] =
    all.find(_.tag === s)

  /** Select the member of Guider with the given tag, throwing if absent. */
  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def unsafeFromTag(s: String): Guider =
    fromTag(s).getOrElse(throw new NoSuchElementException(s))

  /** @group Typeclass Instances */
  implicit val GuiderEnumerated: Enumerated[Guider] =
    new Enumerated[Guider] {
      def all = Guider.all
      def tag(a: Guider) = a.tag
    }

}