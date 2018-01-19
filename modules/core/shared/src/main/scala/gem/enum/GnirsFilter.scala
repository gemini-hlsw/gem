// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package enum

import cats.instances.string._
import cats.syntax.eq._
import gem.math.Wavelength
import gem.util.Enumerated

/**
 * Enumerated type for GNRIS Filter.
 * @group Enumerations (Generated)
 */
sealed abstract class GnirsFilter(
  val tag: String,
  val shortName: String,
  val longName: String,
  val waveLength: Option[Wavelength]
) extends Product with Serializable

object GnirsFilter {

  /** @group Constructors */ case object CrossDispersed extends GnirsFilter("CrossDispersed", "XD", "Cross dispersed", Option.empty[Wavelength])
  /** @group Constructors */ case object Order6 extends GnirsFilter("Order6", "X", "Order 6 (X)", Some(Wavelength.unsafeFromAngstroms(11000)))
  /** @group Constructors */ case object Order5 extends GnirsFilter("Order5", "J", "Order 5 (J)", Some(Wavelength.unsafeFromAngstroms(12500)))
  /** @group Constructors */ case object Order4 extends GnirsFilter("Order4", "H", "Order 4 (H: 1.65µm)", Some(Wavelength.unsafeFromAngstroms(16500)))
  /** @group Constructors */ case object Order3 extends GnirsFilter("Order3", "K", "Order 3 (K)", Some(Wavelength.unsafeFromAngstroms(22000)))
  /** @group Constructors */ case object Order2 extends GnirsFilter("Order2", "L", "Order 2 (L)", Some(Wavelength.unsafeFromAngstroms(35000)))
  /** @group Constructors */ case object Order1 extends GnirsFilter("Order1", "M", "Order 1 (M)", Some(Wavelength.unsafeFromAngstroms(48000)))
  /** @group Constructors */ case object H2 extends GnirsFilter("H2", "H2", "H2: 2.12µm", Some(Wavelength.unsafeFromAngstroms(21200)))
  /** @group Constructors */ case object HNd100x extends GnirsFilter("HNd100x", "H+ND100X", "H + ND100X", Some(Wavelength.unsafeFromAngstroms(16500)))
  /** @group Constructors */ case object H2Nd100x extends GnirsFilter("H2Nd100x", "H2+ND100X", "H2 + ND100X", Some(Wavelength.unsafeFromAngstroms(21200)))
  /** @group Constructors */ case object PAH extends GnirsFilter("PAH", "PAH", "PAH: 3.3µm", Some(Wavelength.unsafeFromAngstroms(33000)))
  /** @group Constructors */ case object Y extends GnirsFilter("Y", "Y", "Y: 1.03µm", Some(Wavelength.unsafeFromAngstroms(10300)))
  /** @group Constructors */ case object J extends GnirsFilter("J", "J", "J: 1.25µm", Some(Wavelength.unsafeFromAngstroms(12500)))
  /** @group Constructors */ case object K extends GnirsFilter("K", "K", "K: 2.20µm", Some(Wavelength.unsafeFromAngstroms(22000)))

  /** All members of GnirsFilter, in canonical order. */
  val all: List[GnirsFilter] =
    List(CrossDispersed, Order6, Order5, Order4, Order3, Order2, Order1, H2, HNd100x, H2Nd100x, PAH, Y, J, K)

  /** Select the member of GnirsFilter with the given tag, if any. */
  def fromTag(s: String): Option[GnirsFilter] =
    all.find(_.tag === s)

  /** Select the member of GnirsFilter with the given tag, throwing if absent. */
  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def unsafeFromTag(s: String): GnirsFilter =
    fromTag(s).getOrElse(throw new NoSuchElementException(s))

  /** @group Typeclass Instances */
  implicit val GnirsFilterEnumerated: Enumerated[GnirsFilter] =
    new Enumerated[GnirsFilter] {
      def all = GnirsFilter.all
      def tag(a: GnirsFilter) = a.tag
    }

}