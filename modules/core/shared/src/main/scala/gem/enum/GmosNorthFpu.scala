// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package enum

import cats.syntax.eq._
import cats.instances.string._
import gem.util.Enumerated

/**
 * Enumerated type for GMOS North focal plane units.
 * @group Enumerations (Generated)
 */
sealed abstract class GmosNorthFpu(
  val tag: String,
  val shortName: String,
  val longName: String,
  val slitWidth: Option[gem.math.Angle]
) extends Product with Serializable

object GmosNorthFpu {

  /** @group Constructors */ case object Longslit1 extends GmosNorthFpu("Longslit1", "0.25arcsec", "Longslit 0.25 arcsec", Some(gem.math.Angle.fromDoubleArcseconds(0.25)))
  /** @group Constructors */ case object Longslit2 extends GmosNorthFpu("Longslit2", "0.50arcsec", "Longslit 0.50 arcsec", Some(gem.math.Angle.fromDoubleArcseconds(0.50)))
  /** @group Constructors */ case object Longslit3 extends GmosNorthFpu("Longslit3", "0.75arcsec", "Longslit 0.75 arcsec", Some(gem.math.Angle.fromDoubleArcseconds(0.75)))
  /** @group Constructors */ case object Longslit4 extends GmosNorthFpu("Longslit4", "1.0arcsec", "Longslit 1.00 arcsec", Some(gem.math.Angle.fromDoubleArcseconds(1.00)))
  /** @group Constructors */ case object Longslit5 extends GmosNorthFpu("Longslit5", "1.5arcsec", "Longslit 1.50 arcsec", Some(gem.math.Angle.fromDoubleArcseconds(1.50)))
  /** @group Constructors */ case object Longslit6 extends GmosNorthFpu("Longslit6", "2.0arcsec", "Longslit 2.00 arcsec", Some(gem.math.Angle.fromDoubleArcseconds(2.00)))
  /** @group Constructors */ case object Longslit7 extends GmosNorthFpu("Longslit7", "5.0arcsec", "Longslit 5.00 arcsec", Some(gem.math.Angle.fromDoubleArcseconds(5.00)))
  /** @group Constructors */ case object Ifu1 extends GmosNorthFpu("Ifu1", "IFU-2", "IFU 2 Slits", Option.empty[gem.math.Angle])
  /** @group Constructors */ case object Ifu2 extends GmosNorthFpu("Ifu2", "IFU-B", "IFU Left Slit (blue)", Option.empty[gem.math.Angle])
  /** @group Constructors */ case object Ifu3 extends GmosNorthFpu("Ifu3", "IFU-R", "IFU Right Slit (red)", Option.empty[gem.math.Angle])
  /** @group Constructors */ case object Ns0 extends GmosNorthFpu("Ns0", "NS0.25arcsec", "N and S 0.25 arcsec", Some(gem.math.Angle.fromDoubleArcseconds(0.25)))
  /** @group Constructors */ case object Ns1 extends GmosNorthFpu("Ns1", "NS0.5arcsec", "N and S 0.50 arcsec", Some(gem.math.Angle.fromDoubleArcseconds(0.50)))
  /** @group Constructors */ case object Ns2 extends GmosNorthFpu("Ns2", "NS0.75arcsec", "N and S 0.75 arcsec", Some(gem.math.Angle.fromDoubleArcseconds(0.75)))
  /** @group Constructors */ case object Ns3 extends GmosNorthFpu("Ns3", "NS1.0arcsec", "N and S 1.00 arcsec", Some(gem.math.Angle.fromDoubleArcseconds(1.00)))
  /** @group Constructors */ case object Ns4 extends GmosNorthFpu("Ns4", "NS1.5arcsec", "N and S 1.50 arcsec", Some(gem.math.Angle.fromDoubleArcseconds(1.50)))
  /** @group Constructors */ case object Ns5 extends GmosNorthFpu("Ns5", "NS2.0arcsec", "N and S 2.00 arcsec", Some(gem.math.Angle.fromDoubleArcseconds(2.00)))

  /** All members of GmosNorthFpu, in canonical order. */
  val all: List[GmosNorthFpu] =
    List(Longslit1, Longslit2, Longslit3, Longslit4, Longslit5, Longslit6, Longslit7, Ifu1, Ifu2, Ifu3, Ns0, Ns1, Ns2, Ns3, Ns4, Ns5)

  /** Select the member of GmosNorthFpu with the given tag, if any. */
  def fromTag(s: String): Option[GmosNorthFpu] =
    all.find(_.tag === s)

  /** Select the member of GmosNorthFpu with the given tag, throwing if absent. */
  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def unsafeFromTag(s: String): GmosNorthFpu =
    fromTag(s).getOrElse(throw new NoSuchElementException(s))

  /** @group Typeclass Instances */
  implicit val GmosNorthFpuEnumerated: Enumerated[GmosNorthFpu] =
    new Enumerated[GmosNorthFpu] {
      def all = GmosNorthFpu.all
      def tag(a: GmosNorthFpu) = a.tag
    }

}