// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package dao

import cats.implicits._
import doobie._, doobie.implicits._
import gem.config.{DynamicConfig, StaticConfig}
import gem.dao.meta._
import gem.enum.Instrument
import gem.util.Location

import scala.collection.immutable.TreeMap

object ObservationDao {
  import EnumeratedMeta._
  import ObservationIdMeta._
  import ObservationIndexMeta._
  import ProgramIdMeta._

  /**
   * Construct a program to insert a fully-populated Observation. This program will raise a
   * key violation if an observation with the same id already exists.
   */
  def insert(oid: Observation.Id, o: Observation[StaticConfig, Step[DynamicConfig]]): ConnectionIO[Unit] =
    for {
      id <- StaticConfigDao.insert(o.staticConfig)
      _  <- Statements.insert(oid, o, id).run
      _  <- TargetEnvironmentDao.insert(oid, o.targets)
      _  <- o.steps.zipWithIndex.traverse { case (s, i) =>
              StepDao.insert(oid, Location.unsafeMiddle((i + 1) * 100), s)
            }.void
    } yield ()

  /** Construct a program to select the specified observation, with the instrument and no steps. */
  def selectFlat(id: Observation.Id): ConnectionIO[Observation[Instrument, Nothing]] =
    for {
      o <- Statements.selectFlat(id).unique.map(_._1)
      t <- TargetEnvironmentDao.selectObs(id)
    } yield o.copy(targets = t)

  /** Construct a program to select the specified observation, with static connfig and no steps. */
  def selectStatic(id: Observation.Id): ConnectionIO[Observation[StaticConfig, Nothing]] =
    for {
      obs <- selectFlat(id)
      tup <- Statements.selectStaticId(id).unique
      sc  <- StaticConfigDao.select(tup._1, tup._2)
    } yield obs.leftMap(_ => sc)

  /** Construct a program to select the specified observation, with static connfig and steps. */
  def select(id: Observation.Id): ConnectionIO[Observation[StaticConfig, Step[DynamicConfig]]] =
    for {
      on <- selectStatic(id)
      ss <- StepDao.selectAll(id)
    } yield on.copy(steps = ss.values.toList)

  /** Construct a program to select the all obseravation ids for the specified science program. */
  def selectIds(pid: Program.Id): ConnectionIO[List[Observation.Id]] =
    Statements.selectIds(pid).list

  private def merge[I: Ordering, S, D](
    os: TreeMap[I, Observation[S, D]],
    ts: Map[I, TargetEnvironment]
  ): TreeMap[I, Observation[S, D]] =
    os.foldLeft(TreeMap.empty[I, Observation[S, D]]) { case (m, (i, o)) =>
      ts.get(i).fold(m)(t => m.updated(i, o.copy(targets = t)))
    }

  /**
   * Construct a program to select all observations for the specified science program, with the
   * instrument and no steps.
   */
  def selectAllFlat(pid: Program.Id): ConnectionIO[TreeMap[Observation.Index, Observation[Instrument, Nothing]]] =
    for {
      m  <- Statements.selectAllFlat(pid).list.map(lst => TreeMap(lst.map { case (i,o,_) => (i,o) }: _*))
      ts <- m.keys.toList.traverse(i => TargetEnvironmentDao.selectObs(Observation.Id(pid, i)).tupleLeft(i))
    } yield merge(m, ts.toMap)

  /**
   * Construct a program to select all observations for the specified science program, with the
   * static component and no steps.
   */
  def selectAllStatic(pid: Program.Id): ConnectionIO[TreeMap[Observation.Index, Observation[StaticConfig, Nothing]]] =
    for {
      ids <- selectIds(pid)
      oss <- ids.traverse(selectStatic)
      ts  <- ids.traverse(i => TargetEnvironmentDao.selectObs(i).tupleLeft(i.index))
    } yield merge(TreeMap(ids.map(_.index).zip(oss): _*), ts.toMap)

  /**
   * Construct a program to select all observations for the specified science program, with the
   * static component and steps.
   */
  def selectAll(pid: Program.Id): ConnectionIO[TreeMap[Observation.Index, Observation[StaticConfig, Step[DynamicConfig]]]] =
    for {
      ids <- selectIds(pid)
      oss <- ids.traverse(select)
      ts  <- ids.traverse(i => TargetEnvironmentDao.selectObs(i).tupleLeft(i.index))
    } yield merge(TreeMap(ids.map(_.index).zip(oss): _*), ts.toMap)

  object Statements {


    def insert(oid: Observation.Id, o: Observation[StaticConfig, _], staticId: Int): Update0 =
      sql"""
        INSERT INTO observation (observation_id,
                                program_id,
                                observation_index,
                                title,
                                static_id,
                                instrument)
              VALUES (${oid},
                      ${oid.pid},
                      ${oid.index},
                      ${o.title},
                      $staticId,
                      ${o.staticConfig.instrument: Instrument})
      """.update

    def selectIds(pid: Program.Id): Query0[Observation.Id] =
      sql"""
        SELECT observation_id
          FROM observation
         WHERE program_id = $pid
      """.query[Observation.Id]

    def selectStaticId(id: Observation.Id): Query0[(Instrument, Int)] =
      sql"""
        SELECT instrument, static_id
          FROM observation
         WHERE observation_id = $id
      """.query[(Instrument, Int)]

    def selectFlat(id: Observation.Id): Query0[(Observation[Instrument, Nothing], Int)] =
      sql"""
        SELECT title, instrument, static_id
          FROM observation
         WHERE observation_id = ${id}
      """.query[(String, Instrument, Int)]
        .map { case (t, i, s) =>
          (Observation(t, TargetEnvironment.empty, i, Nil), s)
        }

    def selectAllFlat(pid: Program.Id): Query0[(Observation.Index, Observation[Instrument, Nothing], Int)] =
      sql"""
        SELECT observation_index, title, instrument, static_id
          FROM observation
         WHERE program_id = ${pid}
      ORDER BY observation_index
      """.query[(Short, String, Instrument, Int)]
        .map { case (n, t, i, s) =>
          (Observation.Index.unsafeFromShort(n), Observation(t, TargetEnvironment.empty, i, Nil), s)
        }

  }
}
