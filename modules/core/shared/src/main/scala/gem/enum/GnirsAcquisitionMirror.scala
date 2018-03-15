// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package enum

import cats.instances.string._
import cats.syntax.eq._
import gem.util.Enumerated

/**
 * Enumerated type for GNIRS Acquisition Mirror.
 * @group Enumerations (Generated)
 */
sealed abstract class GnirsAcquisitionMirror(
  val tag: String,
  val shortName: String,
  val longName: String
) extends Product with Serializable {
  type Self = this.type
}

object GnirsAcquisitionMirror {

  type Aux[A] = GnirsAcquisitionMirror { type Self = A }

  /** @group Constructors */ case object In extends GnirsAcquisitionMirror("In", "In", "In")
  /** @group Constructors */ case object Out extends GnirsAcquisitionMirror("Out", "Out", "Out")

  /** All members of GnirsAcquisitionMirror, in canonical order. */
  val all: List[GnirsAcquisitionMirror] =
    List(In, Out)

  /** Select the member of GnirsAcquisitionMirror with the given tag, if any. */
  def fromTag(s: String): Option[GnirsAcquisitionMirror] =
    all.find(_.tag === s)

  /** Select the member of GnirsAcquisitionMirror with the given tag, throwing if absent. */
  @SuppressWarnings(Array("org.wartremover.warts.Throw"))
  def unsafeFromTag(s: String): GnirsAcquisitionMirror =
    fromTag(s).getOrElse(throw new NoSuchElementException(s))

  /** @group Typeclass Instances */
  implicit val GnirsAcquisitionMirrorEnumerated: Enumerated[GnirsAcquisitionMirror] =
    new Enumerated[GnirsAcquisitionMirror] {
      def all = GnirsAcquisitionMirror.all
      def tag(a: GnirsAcquisitionMirror) = a.tag
    }

}