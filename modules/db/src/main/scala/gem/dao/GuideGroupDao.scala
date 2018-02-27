// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package dao

import gem.dao.meta._
import gem.enum._
import gem.syntax.treemap._

import cats.implicits._
import doobie._
import doobie.implicits._

import scala.collection.immutable.TreeMap


object GuideGroupDao {
  final case class ProtoGuideGroup(
    id:         GuideGroup.Id,
    instrument: Instrument,
    groupType:  GuideGroupType,
    selected:   Boolean
  )

  def insert(grp: GuideGroup, selected: Boolean, oid: Observation.Id): ConnectionIO[GuideGroup.Id] =
    for {
      i <- Statements.insert(grp.groupType, selected, oid, grp.instrument)
                     .withUniqueGeneratedKeys[Int]("id")
                     .map(GuideGroup.Id(_))
      _ <- grp.targets.toList.traverse { case (guider, target) =>
             GuideTargetDao.insert(i, oid, GuideTarget(target, guider.guider), grp.instrument)
           }.void
    } yield i

  def select(id: GuideGroup.Id, i: Instrument): ConnectionIO[Option[GuideGroup]] =
    for {
      go <- Statements.select(id).option
      ts <- GuideTargetDao.selectGroup(id)
                          .map(lst => TreeMap.fromList(lst.map(gt => (gt.guider.toRef(i), gt.target))))
    } yield go.map(g => GuideGroup.Aux[i.type](i, g.groupType, ts))


  object Statements {

    import EnumeratedMeta._

    import gem.dao.meta.ProgramIdMeta._
    import gem.dao.meta.ObservationIndexMeta._


    def insert(
      groupType:  GuideGroupType,
      selected:   Boolean,
      oid:        Observation.Id,
      instrument: Instrument
    ): Update0 =
      sql"""
        INSERT INTO guide_group (
          type,
          selected,
          program_id,
          observation_index,
          instrument
        ) VALUES (
          $groupType,
          $selected,
          ${oid.pid},
          ${oid.index},
          $instrument
        )
      """.update

    def select(
      id: GuideGroup.Id
    ): Query0[ProtoGuideGroup] =
      sql"""
        SELECT id,
               instrument,
               type,
               selected
          FROM guide_group
         WHERE id = $id
      """.query[ProtoGuideGroup]

  }

}
