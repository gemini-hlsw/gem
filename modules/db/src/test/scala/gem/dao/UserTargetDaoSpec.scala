// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package dao

import cats.implicits._
import doobie.implicits._

import gem.config.{ DynamicConfig, StaticConfig }

import org.scalatest._
import org.scalatest.prop._
import org.scalatest.Matchers._


class UserTargetDaoSpec extends PropSpec with PropertyChecks with DaoTest {
  import gem.arb.ArbUserTarget._

  property("UserTargetDao should roundtrip") {
    forAll { (obs: Observation[StaticConfig, Step[DynamicConfig]], ut: UserTarget) =>
      val oid = Observation.Id(pid, Observation.Index.One)

      val utʹ = withProgram {
        for {
          _  <- ObservationDao.insert(oid, obs)
          id <- UserTargetDao.insert(oid, ut)
          uʹ <- UserTargetDao.select(id)
        } yield uʹ
      }

      Some(ut) shouldEqual utʹ
    }
  }

  property("UserTargetDao should bulk select observation") {
    forAll { (obs: Observation[StaticConfig, Step[DynamicConfig]]) =>
      val oid = Observation.Id(pid, Observation.Index.One)

      val actual = withProgram {
        for {
          _   <- ObservationDao.insert(oid, obs)
          uts <- UserTargetDao.selectObs(oid)
        } yield uts
      }

      obs.targets.userTargets shouldEqual actual
    }
  }

  property("UserTargetDao should bulk select program") {
    forAll(genObservationMap(10)) { m =>

      val obsList = m.toList

      val expected = obsList.map { case (oi, obs) =>
        oi -> obs.targets.userTargets
      }.filter(_._2.nonEmpty).toMap

      val actual = withProgram {
        for {
          _   <- obsList.traverse_ { case (oi, obs) => ObservationDao.insert(Observation.Id(pid, oi), obs) }
          uts <- UserTargetDao.selectProg(pid)
        } yield uts
      }

      expected shouldEqual actual
    }
  }
}
