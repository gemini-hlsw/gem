// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package dao

import gem.dao.meta._
import gem.enum.{Guider, Instrument}

import cats.implicits._
import doobie._
import doobie.implicits._

import scala.collection.immutable.TreeMap

final case class ProtoGuideTarget(
  id:       GuideTarget.Id,
  groupId:  GuideGroup.Id,
  targetId: Target.Id,
  guider:   Guider,
  obsIndex: Observation.Index
) extends ProtoTargetWrapper[GuideTarget.Id, GuideTarget] {

  override def wrap(t: Target): GuideTarget =
    GuideTarget(t, guider)
}

object GuideTargetDao extends TargetWrapperDao[GuideTarget.Id, GuideTarget, ProtoGuideTarget] {

  def insert(gid: GuideGroup.Id, oid: Observation.Id, guideTarget: GuideTarget, instrument: Instrument): ConnectionIO[GuideTarget.Id] =
    for {
      t <- TargetDao.insert(guideTarget.target)
      i <- Statements.insert(gid, t, guideTarget.guider, oid, instrument)
                     .withUniqueGeneratedKeys[Int]("id")
                     .map(GuideTarget.Id(_))
    } yield i

  /** Selects the single `GuideTarget` associated with the given id, if any. */
  def select(id: GuideTarget.Id): ConnectionIO[Option[GuideTarget]] =
    selectOne(Statements.select(id))

  def selectGroup(gid: GuideGroup.Id): ConnectionIO[List[GuideTarget]] =
    selectAll(Statements.selectGroup(gid)).map(wrapAll)

  def selectGroupWithId(gid: GuideGroup.Id): ConnectionIO[TreeMap[GuideTarget.Id, GuideTarget]] =
    selectAll(Statements.selectGroup(gid)).map(groupById)

  def selectObs(
    oid: Observation.Id
  ): ConnectionIO[TreeMap[GuideGroup.Id, List[GuideTarget]]] =
    selectAll(Statements.selectObs(oid))
      .map(groupAndMap(_.groupId, wrapAll))

  def selectObsWithId(
    oid: Observation.Id
  ): ConnectionIO[TreeMap[GuideGroup.Id, TreeMap[GuideTarget.Id, GuideTarget]]] =
    selectAll(Statements.selectObs(oid))
      .map(groupAndMap(_.groupId, groupById))

  def selectProg(
    pid: Program.Id
  ): ConnectionIO[TreeMap[Observation.Index, TreeMap[GuideGroup.Id, List[GuideTarget]]]] =
    selectAll(Statements.selectProg(pid))
      .map(groupAndMap(_.obsIndex, groupAndMap(_.groupId, wrapAll)))

  def selectProgWithId(
    pid: Program.Id
  ): ConnectionIO[TreeMap[Observation.Index, TreeMap[GuideGroup.Id, TreeMap[GuideTarget.Id, GuideTarget]]]] =
    selectAll(Statements.selectProg(pid))
      .map(groupAndMap(_.obsIndex, groupAndMap(_.groupId, groupById)))

  object Statements {

    import EnumeratedMeta._

    import gem.dao.meta.ProgramIdMeta._
    import gem.dao.meta.ObservationIndexMeta._

    def insert(gid: GuideGroup.Id, tid: Target.Id, guider: Guider, oid: Observation.Id, i: Instrument): Update0 =
      sql"""
        INSERT INTO guide_star (
          group_id,
          target_id,
          guider,
          guider_instrument,
          program_id,
          observation_index,
          instrument
        ) VALUES (
          $gid,
          $tid,
          $guider,
          ${guider.instrument},
          ${oid.pid},
          ${oid.index},
          $i
        )
      """.update

    def select(id: GuideTarget.Id): Query0[ProtoGuideTarget] =
      sql"""
        SELECT id,
               group_id,
               target_id,
               guider,
               observation_index
          FROM guide_star
         WHERE id = $id
      """.query[ProtoGuideTarget]

    def selectGroup(gid: GuideGroup.Id): Query0[ProtoGuideTarget] =
      sql"""
        SELECT id,
               group_id,
               target_id,
               guider,
               observation_index
          FROM guide_star
         WHERE group_id = ${gid}
      """.query[ProtoGuideTarget]

    def selectObs(oid: Observation.Id): Query0[ProtoGuideTarget] =
      sql"""
        SELECT id,
               group_id,
               target_id,
               guider,
               observation_index
          FROM guide_star
         WHERE program_id = ${oid.pid} AND observation_index = ${oid.index}
      """.query[ProtoGuideTarget]

    def selectProg(pid: Program.Id): Query0[ProtoGuideTarget] =
      sql"""
        SELECT id,
               group_id,
               target_id,
               guider,
               observation_index
          FROM guide_star
         WHERE program_id = $pid
      """.query[ProtoGuideTarget]
  }

}
