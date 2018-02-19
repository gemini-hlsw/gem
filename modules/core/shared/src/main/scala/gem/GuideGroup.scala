// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem

import gem.enum.{ GuideGroupType, Instrument }

import cats._
import cats.implicits._

import scala.collection.immutable.TreeMap

/** GuideGroup collects one or more guide stars intended to be used to observe a
  * science target.  Each guide star is assigned to a distinct guider.
  */
sealed trait GuideGroup {

  type I <: Instrument with Singleton

  def groupType: GuideGroupType
  def targets: TreeMap[GuiderRef.Aux[I], Target]
}

object GuideGroup {

  type Aux[I0 <: Instrument with Singleton] =
    GuideGroup { type I = I0 }

  object Aux {

    private final case class Impl[I0 <: Instrument with Singleton](
      groupType: GuideGroupType,
      targets: TreeMap[GuiderRef.Aux[I0], Target]
    ) extends GuideGroup {
      type I = I0
    }

    def apply[I0 <: Instrument with Singleton](
      groupType: GuideGroupType,
      targets: TreeMap[GuiderRef.Aux[I0], Target]
    ): GuideGroup.Aux[I0] =
      Impl(groupType, targets)

    def empty[I <: Instrument with Singleton](groupType: GuideGroupType): GuideGroup.Aux[I] =
      apply(groupType, TreeMap.empty[GuiderRef.Aux[I], Target])

  }

  /** GuideGroup identifier. */
  final case class Id(toInt: Int) extends AnyVal

  object Id {
    /** Ids ordered by wrapped integer value. */
    implicit val IdOrder: Order[Id] =
      Order.by(_.toInt)
  }
}
