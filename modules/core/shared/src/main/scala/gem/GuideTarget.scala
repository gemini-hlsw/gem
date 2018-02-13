// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem

import cats._
import cats.implicits._

import gem.enum.Guider

import monocle.macros.Lenses

/** Pairs a `Target` and a `Guider`. */
@Lenses final case class GuideTarget(target: Target, guider: Guider)

@SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
object GuideTarget {

  /** GuideTarget identifier. */
  final case class Id(toInt: Int) extends AnyVal

  object Id {
    /** Ids ordered by wrapped integer value. */
    implicit val IdOrder: Order[Id] =
      Order.by(_.toInt)
  }

  implicit val OrderGuideTarget: Order[GuideTarget] =
    Order.by(u => (u.target, u.guider))

}

