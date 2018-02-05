// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package arb

import gem.enum.Instrument
import gem.syntax.treesetcompanion._

import org.scalacheck._
import org.scalacheck.Gen._
import org.scalacheck.Arbitrary._

import scala.collection.immutable.TreeSet

trait ArbTargetEnvironment {
  import ArbAsterism._
  import ArbEnumerated._
  import ArbUserTarget._

  implicit def arbTargetEnvironment: Arbitrary[TargetEnvironment] =
    Arbitrary {
      for {
        i <- arbitrary[Instrument]
        e <- genTargetEnvironment[i.type]
      } yield e
    }

  def genTargetEnvironment[I <: Instrument with Singleton: ValueOf]: Gen[TargetEnvironment] =
    for {
      a <- frequency((9, genAsterism[I].map(Option(_))), (1, const(Option.empty[Asterism])))
      n <- choose(0, 10)
      u <- listOfN(n, arbitrary[UserTarget]).map(us => TreeSet.fromList(us))
    } yield TargetEnvironment(a, u)

  implicit val cogTargetEnvironment: Cogen[TargetEnvironment] =
    Cogen[(Option[Asterism], List[UserTarget])].contramap(e => (e.asterism, e.userTargets.toList))

}

object ArbTargetEnvironment extends ArbTargetEnvironment