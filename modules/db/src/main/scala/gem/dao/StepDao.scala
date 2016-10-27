package gem
package dao

import edu.gemini.spModel.core._
import gem.config._
import gem.enum._

import doobie.imports._
import scalaz._, Scalaz._

object StepDao {

  def insert[I <: InstrumentConfig](sid: Sequence.Id, index: Int, s: Step[I]): ConnectionIO[Int] =
    insertBaseSlice(sid, index, s.instrument, StepType.forStep(s)) *> {
      s match {
        case BiasStep(_)       => insertBiasSlice(sid, index)
        case DarkStep(_)       => insertDarkSlice(sid, index)
        case ScienceStep(_, t) => insertScienceSlice(sid, index, t)
        case GcalStep(_, g)    => insertGCalSlice(sid, index, g)
      }
    } *> insertConfigSlice(sid, index, s.instrument)

  private def insertBaseSlice(sid: Sequence.Id, index: Int, i: InstrumentConfig, t: StepType): ConnectionIO[Int] =
    sql"""
      INSERT INTO step (observation_id, sequence_name, index, instrument, sequence_id, step_type)
      VALUES (${sid.oid}, ${sid.name}, $index, ${Instrument.forConfig(i).tag}, $sid, ${t.tag} :: step_type)
    """.update.run

  private def insertBiasSlice(sid: Sequence.Id, index: Int): ConnectionIO[Int] =
    sql"""
      INSERT INTO step_bias (sequence_id, index)
      VALUES ($sid, $index)
    """.update.run

  private def insertDarkSlice(sid: Sequence.Id, index: Int): ConnectionIO[Int] =
    sql"""
      INSERT INTO step_dark (sequence_id, index)
      VALUES ($sid, $index)
    """.update.run

  private def insertGCalSlice(sid: Sequence.Id, index: Int, gcal: GcalConfig): ConnectionIO[Int] =
    sql"""
      INSERT INTO step_gcal (sequence_id, index, gcal_lamp, shutter)
      VALUES ($sid, $index, ${gcal.lamp}, ${gcal.shutter} :: gcal_shutter)
    """.update.run

  private def insertScienceSlice(sid: Sequence.Id, index: Int, t: TelescopeConfig): ConnectionIO[Int] =
    sql"""
      INSERT INTO step_science (sequence_id, index, offset_p, offset_q)
      VALUES ($sid, $index, ${t.p}, ${t.q})
    """.update.run

    private def insertConfigSlice(sid: Sequence.Id, index: Int, i: InstrumentConfig): ConnectionIO[Int] =
      i match {

        case F2Config(fpu, mosPreimaging, exposureTime, filter, lyotWheel, disperser, windowCover) =>
          sql"""
            INSERT INTO step_f2 (sequence_id, index, fpu, mos_preimaging, exposure_time, filter, lyot_wheel, disperser, window_cover)
            VALUES ($sid, $index, $fpu, $mosPreimaging, ${exposureTime.getSeconds}, $filter, $lyotWheel, $disperser, $windowCover)
          """.update.run

        case GenericConfig(i) => 0.point[ConnectionIO]

      }

  // The type we get when we select the fully joined step
  private case class StepKernel(
    sid: Sequence.Id,
    i: Instrument,
    stepType: StepType, // todo: make an enum
    gcal: (Option[GCalLamp], Option[GCalShutter]),
    telescope: (Option[OffsetP],  Option[OffsetQ])
  ) {
    def toStep: Step[Instrument] =
      stepType match {

        case StepType.Bias => BiasStep(i)
        case StepType.Dark => DarkStep(i)

        case StepType.Gcal =>
          gcal.apply2(GcalConfig(_, _))
            .map(GcalStep(i, _))
            .getOrElse(sys.error("missing gcal information: " + gcal))

        case StepType.Science =>
          telescope.apply2(TelescopeConfig(_, _))
            .map(ScienceStep(i, _))
            .getOrElse(sys.error("missing telescope information: " + telescope))

      }
  }

  def selectIds(oid: Observation.Id): ConnectionIO[List[Sequence.Id]] =
    sql"""
          SELECT DISTINCT sequence_id
            FROM step
           WHERE observation_id = $oid
        ORDER BY sequence_id
       """.query[Sequence.Id].list

  def selectAll(oid: Observation.Id): ConnectionIO[List[Sequence[Step[Instrument]]]] =
    sql"""
       SELECT s.sequence_id,
              s.instrument,
              s.step_type,
              sg.gcal_lamp,
              sg.shutter,
              sc.offset_p,
              sc.offset_q
       FROM step s
              LEFT OUTER JOIN step_gcal sg
                 ON sg.sequence_id = s.sequence_id AND sg.index = s.index
              LEFT OUTER JOIN step_science sc
                 ON sc.sequence_id = s.sequence_id AND sc.index = s.index
       WHERE s.observation_id = $oid
    ORDER BY s.sequence_id, s.index
    """.query[StepKernel].list.map { (lst: List[StepKernel]) =>
      lst.groupBy(_.sid).map { case (sid, sks) => Sequence(sid, sks.map(_.toStep)) }.toList
    }

  def selectAll(sid: Sequence.Id): ConnectionIO[Option[Sequence[Step[Instrument]]]] =
    sql"""
       SELECT s.sequence_id,
              s.instrument,
              s.step_type,
              sg.gcal_lamp,
              sg.shutter,
              sc.offset_p,
              sc.offset_q
       FROM step s
              LEFT OUTER JOIN step_gcal sg
                 ON sg.sequence_id = s.sequence_id AND sg.index = s.index
              LEFT OUTER JOIN step_science sc
                 ON sc.sequence_id = s.sequence_id AND sc.index = s.index
       WHERE s.sequence_id = $sid
    ORDER BY s.index
    """.query[StepKernel].list.map { (lst: List[StepKernel]) =>
      lst.headOption.map { sk => Sequence(sk.sid, lst.map(_.toStep)) }
    }
}
