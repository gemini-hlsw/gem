// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package dao

import gem.dao.meta._
import gem.enum.UserTargetType

import doobie._, doobie.implicits._


object UserTargetDao {

  import EnumeratedMeta._
  import ObservationIdMeta._

  def insert(userTarget: UserTarget, oid: Observation.Id): ConnectionIO[Int] =
    for {
      tid <- TargetDao.insert(userTarget.target)
      uid <- Statements.insert(tid, userTarget.targetType, oid).withUniqueGeneratedKeys[Int]("user_target_id")
    } yield uid

  object Statements {
    def insert(targetId: Int, targetType: UserTargetType, oid: Observation.Id): Update0 =
      sql"""
        INSERT INTO user_target (
          target_id,
          user_target_type,
          observation_id
        ) VALUES (
          $targetId,
          $targetType,
          $oid
        )
      """.update
  }
}
