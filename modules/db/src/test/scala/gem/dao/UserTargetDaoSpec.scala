// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package dao

import gem.config.{ DynamicConfig, StaticConfig }

import org.scalatest._
import org.scalatest.prop._
import org.scalatest.Matchers._

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary._


class UserTargetDaoSpec extends PropSpec with PropertyChecks with DaoTest {
  import gem.arb.ArbUserTarget._

  implicit val arbObservation: Arbitrary[Observation[StaticConfig, Step[DynamicConfig]]] =
    Arbitrary {
      genObservation
    }

  property("UserTargetDao should roundtrip") {
    forAll { (obs: Observation[StaticConfig, Step[DynamicConfig]], ut: UserTarget) =>
      val oid = Observation.Id(pid, Observation.Index.unsafeFromInt(1))

      val utʹ = withProgram {
        for {
          _  <- ObservationDao.insert(oid, obs)
          id <- UserTargetDao.insert(ut, oid)
          uʹ <- UserTargetDao.select(id)
        } yield uʹ
      }

      Some(ut) shouldEqual utʹ
    }
  }
}
