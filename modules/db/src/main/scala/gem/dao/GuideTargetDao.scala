// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package dao

import gem.dao.meta._
import gem.enum.{Guider, Instrument}

import cats.implicits._
import doobie._
import doobie.implicits._


object GuideTargetDao {

  final case class ProtoGuideTarget(
    id: GuideTarget.Id,
    groupId: Int,
    targetId: Target.Id,
    guider: Guider,
    obsIndex: Observation.Index
  ) {

    def toGuideTarget: ConnectionIO[Option[GuideTarget]] =
      TargetDao.select(targetId).map { _.map(GuideTarget(_, guider)) }
  }

  def insert(gid: Int, oid: Observation.Id, guideTarget: GuideTarget, instrument: Instrument): ConnectionIO[GuideTarget.Id] =
    for {
      t <- TargetDao.insert(guideTarget.target)
      i <- Statements.insert(gid, t, guideTarget.guider, oid, instrument)
                     .withUniqueGeneratedKeys[Int]("id")
                     .map(GuideTarget.Id(_))
    } yield i

  /** Selects the single `GuideTarget` associated with the given id, if any. */
  def select(id: GuideTarget.Id): ConnectionIO[Option[GuideTarget]] =
    for {
      g <- Statements.select(id).option
      t <- g.fold(Option.empty[GuideTarget].pure[ConnectionIO]) { _.toGuideTarget }
    } yield t

  private def selectAll(
    query: Query0[ProtoGuideTarget]
  ): ConnectionIO[List[(ProtoGuideTarget, Target)]] =
    for {
      gs <- query.to[List]
      ts <- gs.map(_.targetId).traverse(TargetDao.select)
    } yield gs.zip(ts).flatMap { case (g, ot) =>
      ot.map(t => (g, t)).toList
    }

  // This is the straw that broke the camel's back.  TreeMap[Id.TargetGroup, List[(Id.GuideStar, GuideTarget)]]

  def selectObsWithId(oid: Observation.Id): ConnectionIO[Map[Int, List[(GuideTarget.Id, GuideTarget)]]] =
    selectAll(Statements.selectObs(oid)).map {
      _.groupBy { case (g, _) => g.groupId }
       .mapValues { _.map { case (g, t) => (g.id, GuideTarget(t, g.guider)) } }
    }

  object Statements {

    import EnumeratedMeta._

    import gem.dao.meta.ProgramIdMeta._
    import gem.dao.meta.ObservationIndexMeta._

    def insert(gid: Int, tid: Target.Id, guider: Guider, oid: Observation.Id, i: Instrument): Update0 =
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

    def selectGroup(gid: Int): Query0[ProtoGuideTarget] =
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
