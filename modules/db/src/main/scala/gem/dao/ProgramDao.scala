// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package dao

import gem.dao.meta._

import cats.implicits._
import doobie._, doobie.implicits._

import scala.collection.immutable.TreeMap

object ProgramDao {
  import EnumeratedMeta._
  import ProgramIdMeta._

  /** Insert a program, disregarding its observations, if any. */
  def insertFlat(p: Program[_]): ConnectionIO[Program.Id] =
    insertProgramIdSlice(p.id) *>
    Statements.insert(p).run.as(p.id)

  /** Insert a complete program. */
  def insert(p: Program[Observation.Full]): ConnectionIO[Program.Id] =
    insertFlat(p) <* p.observations.toList.traverse { case (i,o) =>
      ObservationDao.insert(Observation.Id(p.id, i), o)
    }

  private def insertProgramIdSlice(pid: Program.Id): ConnectionIO[Int] =
    pid match {
      case id: Program.Id.Science     => insertScienceProgramIdSlice(id)
      case id: Program.Id.Daily       => insertDailyProgramIdSlice(id)
      case id: Program.Id.Nonstandard => insertNonstandardProgramIdSlice(id)
    }

  private def insertScienceProgramIdSlice(pid: Program.Id.Science): ConnectionIO[Int] =
    SemesterDao.canonicalize(pid.semester) *>
    Statements.insertScienceProgramIdSlice(pid).run

  private def insertDailyProgramIdSlice(pid: Program.Id.Daily): ConnectionIO[Int] =
    Statements.insertDailyProgramIdSlice(pid).run

  private def insertNonstandardProgramIdSlice(pid: Program.Id.Nonstandard): ConnectionIO[Int] =
    pid.semesterOption.traverse(SemesterDao.canonicalize) *>
    Statements.insertNonstandardProgramIdSlice(pid).run

  def selectBySubstring(pat: String, max: Int): ConnectionIO[List[Program[Nothing]]] =
    Statements.selectBySubstring(pat, max).list

  /** Select a program by Id, without any Observation information. */
  def selectFlat(pid: Program.Id): ConnectionIO[Option[Program[Nothing]]] =
    Statements.selectFlat(pid).option

  /** Select a program by id, with full Observation information. */
  def selectFull(pid: Program.Id): ConnectionIO[Option[Program[Observation.Full]]] =
    for {
      opn <- selectFlat(pid)
      os  <- ObservationDao.selectAll(pid)
    } yield opn.map(_.copy(observations = os))

  object Statements {

    // StepId has a DISTINCT type due to its check constraint so we need a fine-grained mapping
    // here to satisfy the query checker.
    private final case class Index(toInt: Int)
    private object Index {
      implicit val IndexMeta: Meta[Index] =
        Distinct.integer("id_index").xmap(Index(_), _.toInt)
    }

    def selectFlat(pid: Program.Id): Query0[Program[Nothing]] =
      sql"""
        SELECT title
          FROM program
         WHERE program_id = $pid
      """.query[String]
         .map(Program(pid, _, TreeMap.empty))

    def selectBySubstring(pat: String, max: Int): Query0[Program[Nothing]] =
      sql"""
       SELECT program_id, title
         FROM program
        WHERE lower(program_id) like lower($pat) OR lower(title) like lower($pat)
      ORDER BY program_id, title
        LIMIT ${max.toLong}
      """.query[(Program.Id, String)]
        .map { case (pid, title) => Program(pid, title, TreeMap.empty) }

    // N.B. assumes semester has been canonicalized
    def insertNonstandardProgramIdSlice(pid: Program.Id.Nonstandard): Update0 =
      sql"""
        INSERT INTO program (program_id,
                             site,
                             semester_id,
                             program_type)
              VALUES (${pid: Program.Id},
                      ${pid.siteOption},
                      ${pid.semesterOption.map(_.format)},
                      ${pid.programTypeOption})
      """.update

    def insertDailyProgramIdSlice(pid: Program.Id.Daily): Update0 =
      sql"""
        INSERT INTO program (program_id,
                            site,
                            program_type,
                            day)
              VALUES (${pid: Program.Id},
                      ${pid.site},
                      ${pid.programTypeOption},
                      ${pid.localDate})
      """.update

    // N.B. assumes semester has been canonicalized
    def insertScienceProgramIdSlice(pid: Program.Id.Science): Update0 =
      sql"""
         INSERT INTO program (program_id,
                             site,
                             semester_id,
                             program_type,
                             index)
             VALUES (${pid: Program.Id},
                     ${pid.site},
                     ${pid.semester.format},
                     ${pid.programType},
                     ${Index(pid.index)})
      """.update

    // N.B. assumes program id slice has been inserted
    def insert(p: Program[_]): Update0 =
      sql"""
        UPDATE program
           SET title = ${p.title}
         WHERE program_id = ${p.id}
      """.update

  }

}
