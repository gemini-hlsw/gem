// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem

import cats.Eq
import cats.data.NonEmptyList

import gem.enum.{ AsterismType, Instrument }

import monocle.Prism
import monocle.macros.{ Lenses, GenPrism }


sealed trait Asterism extends Product with Serializable {

  def asterismType: AsterismType

  def instrument: Instrument

  def targets: NonEmptyList[Target]

}

object Asterism extends AsterismOptics {

  @Lenses final case class SingleTarget(target: Target, instrument: Instrument) extends Asterism {

    override def asterismType: AsterismType =
      AsterismType.SingleTarget

    override def targets: NonEmptyList[Target] =
      NonEmptyList.one(target)

  }

  @SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
  object SingleTarget {
    implicit val EqSingleTarget: Eq[SingleTarget] =
      Eq.fromUniversalEquals
  }

  @Lenses final case class GhostDualTarget(ifu1: Target, ifu2: Target) extends Asterism {

    override def asterismType: AsterismType =
      AsterismType.GhostDualTarget

    override def instrument: Instrument =
      Instrument.Ghost

    override def targets: NonEmptyList[Target] =
      NonEmptyList.of(ifu1, ifu2)
  }

  @SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
  object GhostDualTarget {
    implicit val EqGhostDualTarget: Eq[GhostDualTarget] =
      Eq.fromUniversalEquals
  }

  implicit val EqAsterism: Eq[Asterism] =
    Eq.fromUniversalEquals
}

trait AsterismOptics { self: Asterism.type =>

  val singleTarget: Prism[Asterism, Asterism.SingleTarget] =
    GenPrism[Asterism, Asterism.SingleTarget]

  val ghostDualTarget: Prism[Asterism, Asterism.GhostDualTarget] =
    GenPrism[Asterism, Asterism.GhostDualTarget]

}