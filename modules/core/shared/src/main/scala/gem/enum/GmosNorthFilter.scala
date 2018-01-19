// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package enum

import cats.instances.string._
import cats.syntax.eq._
import gem.math.Wavelength
import gem.util.Enumerated

/**
 * Enumerated type for GMOS North filters.
 * @group Enumerations (Generated)
 */
sealed abstract class GmosNorthFilter(
  val tag: String,
  val shortName: String,
  val longName: String,
  val wavelength: Wavelength,
  val obsolete: Boolean
) extends Product with Serializable

object GmosNorthFilter {

  /** @group Constructors */ case object GPrime extends GmosNorthFilter("GPrime", "g", "g_G0301", Wavelength.fromAngstroms.unsafeGet(4750), false)
  /** @group Constructors */ case object RPrime extends GmosNorthFilter("RPrime", "r", "r_G0303", Wavelength.fromAngstroms.unsafeGet(6300), false)
  /** @group Constructors */ case object IPrime extends GmosNorthFilter("IPrime", "i", "i_G0302", Wavelength.fromAngstroms.unsafeGet(7800), false)
  /** @group Constructors */ case object ZPrime extends GmosNorthFilter("ZPrime", "z", "z_G0304", Wavelength.fromAngstroms.unsafeGet(9250), false)
  /** @group Constructors */ case object Z extends GmosNorthFilter("Z", "Z", "Z_G0322", Wavelength.fromAngstroms.unsafeGet(8760), false)
  /** @group Constructors */ case object Y extends GmosNorthFilter("Y", "Y", "Y_G0323", Wavelength.fromAngstroms.unsafeGet(10100), false)
  /** @group Constructors */ case object GG455 extends GmosNorthFilter("GG455", "GG455", "GG455_G0305", Wavelength.fromAngstroms.unsafeGet(6800), false)
  /** @group Constructors */ case object OG515 extends GmosNorthFilter("OG515", "OG515", "OG515_G0306", Wavelength.fromAngstroms.unsafeGet(7100), false)
  /** @group Constructors */ case object RG610 extends GmosNorthFilter("RG610", "RG610", "RG610_G0307", Wavelength.fromAngstroms.unsafeGet(7500), false)
  /** @group Constructors */ case object CaT extends GmosNorthFilter("CaT", "CaT", "CaT_G0309", Wavelength.fromAngstroms.unsafeGet(8600), false)
  /** @group Constructors */ case object Ha extends GmosNorthFilter("Ha", "Ha", "Ha_G0310", Wavelength.fromAngstroms.unsafeGet(6550), false)
  /** @group Constructors */ case object HaC extends GmosNorthFilter("HaC", "HaC", "HaC_G0311", Wavelength.fromAngstroms.unsafeGet(6620), false)
  /** @group Constructors */ case object DS920 extends GmosNorthFilter("DS920", "DS920", "DS920_G0312", Wavelength.fromAngstroms.unsafeGet(9200), false)
  /** @group Constructors */ case object SII extends GmosNorthFilter("SII", "SII", "SII_G0317", Wavelength.fromAngstroms.unsafeGet(6720), false)
  /** @group Constructors */ case object OIII extends GmosNorthFilter("OIII", "OIII", "OIII_G0318", Wavelength.fromAngstroms.unsafeGet(4990), false)
  /** @group Constructors */ case object OIIIC extends GmosNorthFilter("OIIIC", "OIIIC", "OIIIC_G0319", Wavelength.fromAngstroms.unsafeGet(5140), false)
  /** @group Constructors */ case object HeII extends GmosNorthFilter("HeII", "HeII", "HeII_G0320", Wavelength.fromAngstroms.unsafeGet(4680), false)
  /** @group Constructors */ case object HeIIC extends GmosNorthFilter("HeIIC", "HeIIC", "HeIIC_G0321", Wavelength.fromAngstroms.unsafeGet(4780), false)
  /** @group Constructors */ case object HartmannA_RPrime extends GmosNorthFilter("HartmannA_RPrime", "r+HartA", "HartmannA_G0313 + r_G0303", Wavelength.fromAngstroms.unsafeGet(6300), false)
  /** @group Constructors */ case object HartmannB_RPrime extends GmosNorthFilter("HartmannB_RPrime", "r+HartB", "HartmannB_G0314 + r_G0303", Wavelength.fromAngstroms.unsafeGet(6300), false)
  /** @group Constructors */ case object GPrime_GG455 extends GmosNorthFilter("GPrime_GG455", "g+GG455", "g_G0301 + GG455_G0305", Wavelength.fromAngstroms.unsafeGet(5060), false)
  /** @group Constructors */ case object GPrime_OG515 extends GmosNorthFilter("GPrime_OG515", "g+OG515", "g_G0301 + OG515_G0306", Wavelength.fromAngstroms.unsafeGet(5360), false)
  /** @group Constructors */ case object RPrime_RG610 extends GmosNorthFilter("RPrime_RG610", "r+RG610", "r_G0303 + RG610_G0307", Wavelength.fromAngstroms.unsafeGet(6570), false)
  /** @group Constructors */ case object IPrime_CaT extends GmosNorthFilter("IPrime_CaT", "i+CaT", "i_G0302 + CaT_G0309", Wavelength.fromAngstroms.unsafeGet(8150), false)
  /** @group Constructors */ case object ZPrime_CaT extends GmosNorthFilter("ZPrime_CaT", "z+CaT", "z_G0304 + CaT_G0309", Wavelength.fromAngstroms.unsafeGet(8900), false)
  /** @group Constructors */ case object UPrime extends GmosNorthFilter("UPrime", "u", "u_G0308", Wavelength.fromAngstroms.unsafeGet(3500), true)

  /** All members of GmosNorthFilter, in canonical order. */
  val all: List[GmosNorthFilter] =
    List(GPrime, RPrime, IPrime, ZPrime, Z, Y, GG455, OG515, RG610, CaT, Ha, HaC, DS920, SII, OIII, OIIIC, HeII, HeIIC, HartmannA_RPrime, HartmannB_RPrime, GPrime_GG455, GPrime_OG515, RPrime_RG610, IPrime_CaT, ZPrime_CaT, UPrime)

  /** Select the member of GmosNorthFilter with the given tag, if any. */
  def fromTag(s: String): Option[GmosNorthFilter] =
    all.find(_.tag === s)

  /** Select the member of GmosNorthFilter with the given tag, throwing if absent. */
  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def unsafeFromTag(s: String): GmosNorthFilter =
    fromTag(s).getOrElse(throw new NoSuchElementException(s))

  /** @group Typeclass Instances */
  implicit val GmosNorthFilterEnumerated: Enumerated[GmosNorthFilter] =
    new Enumerated[GmosNorthFilter] {
      def all = GmosNorthFilter.all
      def tag(a: GmosNorthFilter) = a.tag
    }

}