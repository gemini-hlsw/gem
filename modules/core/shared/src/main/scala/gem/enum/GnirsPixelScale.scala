// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package enum

import cats.instances.string._
import cats.syntax.eq._
import gem.util.Enumerated

/**
 * Enumerated type for GNIRS Pixel Scale.
 * @group Enumerations (Generated)
 */
sealed abstract class GnirsPixelScale(
  val tag: String,
  val shortName: String,
  val longName: String,
  val value: BigDecimal
) extends Product with Serializable {
  type Self = this.type
}

object GnirsPixelScale {

  type Aux[A] = GnirsPixelScale { type Self = A }

  /** @group Constructors */ case object PixelScale_0_05 extends GnirsPixelScale("PixelScale_0_05", "0.05 arcsec/pix", "Pixel scale for short cameras", 0.05)
  /** @group Constructors */ case object PixelScale_0_15 extends GnirsPixelScale("PixelScale_0_15", "0.15 arcsec/pix", "Pixel scale for long cameras", 0.15)

  /** All members of GnirsPixelScale, in canonical order. */
  val all: List[GnirsPixelScale] =
    List(PixelScale_0_05, PixelScale_0_15)

  /** Select the member of GnirsPixelScale with the given tag, if any. */
  def fromTag(s: String): Option[GnirsPixelScale] =
    all.find(_.tag === s)

  /** Select the member of GnirsPixelScale with the given tag, throwing if absent. */
  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def unsafeFromTag(s: String): GnirsPixelScale =
    fromTag(s).getOrElse(throw new NoSuchElementException(s))

  /** @group Typeclass Instances */
  implicit val GnirsPixelScaleEnumerated: Enumerated[GnirsPixelScale] =
    new Enumerated[GnirsPixelScale] {
      def all = GnirsPixelScale.all
      def tag(a: GnirsPixelScale) = a.tag
    }

}