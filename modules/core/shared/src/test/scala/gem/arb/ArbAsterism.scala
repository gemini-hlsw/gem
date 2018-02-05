// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package arb

import gem.enum.Instrument

import org.scalacheck._
import org.scalacheck.Arbitrary._
import org.scalacheck.Cogen._

trait ArbAsterism {

  import ArbEnumerated._
  import ArbTarget._

  def genSingleTarget[I <: Instrument with Singleton: ValueOf]: Gen[Asterism.SingleTarget[I]] =
    arbitrary[Target].map(Asterism.SingleTarget(_, valueOf[I]))

  val genGhostDualTarget: Gen[Asterism.GhostDualTarget] =
    for {
      t1 <- arbitrary[Target]
      t2 <- arbitrary[Target]
    } yield Asterism.GhostDualTarget(t1, t2)

  def genAsterism[I <: Instrument with Singleton: ValueOf]: Gen[Asterism] =
    valueOf[I] match {
      case Instrument.Ghost => genGhostDualTarget
      case i                => genSingleTarget[I]
    }

  implicit val arbAsterism: Arbitrary[Asterism] =
    Arbitrary {
      for {
        i <- arbitrary[Instrument]
        a <- genAsterism[i.type]
      } yield a
    }

  implicit def cogAsterism: Cogen[Asterism] =
    Cogen[(List[Target], Instrument)].contramap(a => (a.targets.toList, a.instrument))

}

object ArbAsterism extends ArbAsterism
