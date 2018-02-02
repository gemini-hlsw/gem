// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package arb

import cats.implicits._

import gem.enum.Instrument

import org.scalacheck._
import org.scalacheck.Arbitrary._
import org.scalacheck.Cogen._

trait ArbAsterism {

  import ArbEnumerated._
  import ArbTarget._

  implicit val arbSingleTarget: Arbitrary[Asterism.SingleTarget] =
    Arbitrary {
      for {
        t <- arbitrary[Target]
        i <- Gen.oneOf(Instrument.all.filterNot(_ === Instrument.Ghost))
      } yield Asterism.SingleTarget(t, i)
    }

  private def genSingleTarget(i: Instrument): Gen[Asterism.SingleTarget] =
    arbitrary[Target].map(Asterism.SingleTarget(_, i))

  implicit val arbGhostDualTarget: Arbitrary[Asterism.GhostDualTarget] =
    Arbitrary {
      for {
        t1 <- arbitrary[Target]
        t2 <- arbitrary[Target]
      } yield Asterism.GhostDualTarget(t1, t2)
    }

  def genAsterism(i: Instrument): Gen[Asterism] =
    i match {
      case Instrument.Ghost => arbitrary[Asterism.GhostDualTarget]
      case _                => genSingleTarget(i)
    }

  implicit val arbAsterism: Arbitrary[Asterism] =
    Arbitrary {
      for {
        i <- arbitrary[Instrument]
        a <- genAsterism(i)
      } yield a
    }

  implicit val cogSingleTarget: Cogen[Asterism.SingleTarget] =
    Cogen[(Target, Instrument)].contramap(a => (a.target, a.instrument))

  implicit val cogGhostDualTarget: Cogen[Asterism.GhostDualTarget] =
    Cogen[(Target, Target)].contramap(a => (a.ifu1, a.ifu2))

  implicit val cogAsterism: Cogen[Asterism] =
    Cogen[(Option[Asterism.SingleTarget], Option[Asterism.GhostDualTarget])].contramap {
      case a0: Asterism.SingleTarget    => (Some(a0), None)
      case a0: Asterism.GhostDualTarget => (None, Some(a0))
    }
}

object ArbAsterism extends ArbAsterism
