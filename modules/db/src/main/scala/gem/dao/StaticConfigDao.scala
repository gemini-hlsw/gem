// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package dao

import cats.implicits._
import doobie._, doobie.implicits._
import gem.enum.{ GmosDetector, Instrument, MosPreImaging }
import gem.dao.meta._
import gem.config._

object StaticConfigDao {
  import EnumeratedMeta._
  import ProgramIdMeta._
  import ObservationIndexMeta._
  import OffsetMeta._

  def insert(oid: Observation.Id, s: StaticConfig): ConnectionIO[Unit] =
    s match {
      case _:     StaticConfig.AcqCam    => ().pure[ConnectionIO]
      case _:     StaticConfig.Bhros     => ().pure[ConnectionIO]
      case f2:    StaticConfig.F2        => Statements.F2.insert(oid, f2).run.void
      case g:     StaticConfig.GmosNorth => Gmos.insertNorth(oid, g)
      case g:     StaticConfig.GmosSouth => Gmos.insertSouth(oid, g)
      case gnirs: StaticConfig.Gnirs     => Statements.Gnirs.insert(oid, gnirs).run.void
      case _:     StaticConfig.Gpi       => ().pure[ConnectionIO]
      case _:     StaticConfig.Gsaoi     => ().pure[ConnectionIO]
      case _:     StaticConfig.Michelle  => ().pure[ConnectionIO]
      case _:     StaticConfig.Nici      => ().pure[ConnectionIO]
      case _:     StaticConfig.Nifs      => ().pure[ConnectionIO]
      case _:     StaticConfig.Niri      => ().pure[ConnectionIO]
      case _:     StaticConfig.Phoenix   => ().pure[ConnectionIO]
      case _:     StaticConfig.Trecs     => ().pure[ConnectionIO]
      case _:     StaticConfig.Visitor   => ().pure[ConnectionIO]
    }

  def select(oid: Observation.Id, i: Instrument): ConnectionIO[StaticConfig] = {
    def pure(sc: StaticConfig): ConnectionIO[StaticConfig] =
      sc.pure[ConnectionIO]

    i match {
      case Instrument.AcqCam     => pure(StaticConfig.AcqCam())
      case Instrument.Bhros      => pure(StaticConfig.Bhros())

      case Instrument.Flamingos2 => Statements.F2.select(oid)   .unique.widen[StaticConfig]
      case Instrument.GmosN      => Gmos.selectNorth(oid)              .widen[StaticConfig]
      case Instrument.GmosS      => Gmos.selectSouth(oid)              .widen[StaticConfig]
      case Instrument.Gnirs      => Statements.Gnirs.select(oid).unique.widen[StaticConfig]

      case Instrument.Gpi        => pure(StaticConfig.Gpi())
      case Instrument.Gsaoi      => pure(StaticConfig.Gsaoi())
      case Instrument.Michelle   => pure(StaticConfig.Michelle())
      case Instrument.Nici       => pure(StaticConfig.Nici())
      case Instrument.Nifs       => pure(StaticConfig.Nifs())
      case Instrument.Niri       => pure(StaticConfig.Niri())
      case Instrument.Phoenix    => pure(StaticConfig.Phoenix())
      case Instrument.Trecs      => pure(StaticConfig.Trecs())
      case Instrument.Visitor    => pure(StaticConfig.Visitor())
    }
  }

  /** Combines lower-level GMOS statements into higher-level inserts and
    * selects.
    */
  private object Gmos {

    import gem.config.GmosConfig.{ GmosCustomRoiEntry, GmosNodAndShuffle }
    import gem.enum.Instrument.{ GmosN, GmosS }
    import StaticConfig.{ GmosNorth, GmosSouth }

    def insertNorth(oid: Observation.Id, gn: GmosNorth): ConnectionIO[Unit] =
        insertNodAndShuffle(oid, GmosN, gn.common.nodAndShuffle)   *>
          insertCustomRoiEntries(oid, GmosN, gn.common.customRois) *>
          Statements.Gmos.insertNorth(oid, gn).run.void

    def insertSouth(oid: Observation.Id, gs: GmosSouth): ConnectionIO[Unit] =
        insertNodAndShuffle(oid, GmosS, gs.common.nodAndShuffle)   *>
          insertCustomRoiEntries(oid, GmosS, gs.common.customRois) *>
          Statements.Gmos.insertSouth(oid, gs).run.void

    def insertCustomRoiEntries(oid: Observation.Id, i: Instrument, rois: Set[GmosCustomRoiEntry]): ConnectionIO[Unit] =
      rois.toList.traverse(Statements.Gmos.insertCustomRoiEntry(oid, i, _).run).void

    def insertNodAndShuffle(oid: Observation.Id, i: Instrument, ns: Option[GmosNodAndShuffle]): ConnectionIO[Unit] =
      ns.fold(().pure[ConnectionIO])(ns => Statements.Gmos.insertNodAndShuffle(oid, i, ns).run.void)

    def selectNorth(oid: Observation.Id): ConnectionIO[GmosNorth] =
      for {
        ro <- Statements.Gmos.selectCustomRoiEntry(oid, GmosN).list.map(_.toSet)
        ns <- Statements.Gmos.selectNodAndShuffle(oid, GmosN).option
        gn <- Statements.Gmos.selectNorth(oid).unique
        gnʹ = GmosNorth.customRois.set(ro)(gn)
      } yield GmosNorth.nodAndShuffle.set(ns)(gnʹ)

    def selectSouth(oid: Observation.Id): ConnectionIO[GmosSouth] =
      for {
        ro <- Statements.Gmos.selectCustomRoiEntry(oid, GmosS).list.map(_.toSet)
        ns <- Statements.Gmos.selectNodAndShuffle(oid, GmosS).option
        gs <- Statements.Gmos.selectSouth(oid).unique
        gsʹ = GmosSouth.customRois.set(ro)(gs)
      } yield GmosSouth.nodAndShuffle.set(ns)(gsʹ)
  }

  object Statements {

    /** F2 Statements. */
    object F2 {

      import Instrument.Flamingos2

      def select(oid: Observation.Id): Query0[StaticConfig.F2] =
        sql"""
          SELECT mos_preimaging
            FROM static_f2
           WHERE program_id        = ${oid.pid}
             AND observation_index = ${oid.index}
        """.query[StaticConfig.F2]

      def insert(oid: Observation.Id, f2: StaticConfig.F2): Update0 =
        sql"""
          INSERT INTO static_f2 (program_id, observation_index, instrument, mos_preimaging)
          VALUES (
            ${oid.pid},
            ${oid.index},
            ${Flamingos2: Instrument},
            ${f2.mosPreImaging})
        """.update
    }

    /** GMOS Statements. */
    object Gmos {

      import gem.config.GmosConfig.{ GmosCommonStaticConfig => GmosCommonSC, GmosCustomRoiEntry, GmosNodAndShuffle, GmosShuffleCycles, GmosShuffleOffset }
      import gem.enum.Instrument.{ GmosN, GmosS }
      import StaticConfig.{ GmosNorth, GmosSouth }

      // We need to define this explicitly because we're ignoring the nod and
      // shuffle and custom ROIs.
      implicit val GmosCommonStaticComposite: Composite[GmosCommonSC] =
        Composite[(GmosDetector, MosPreImaging)].imap(
          (t: (GmosDetector, MosPreImaging)) => GmosCommonSC(t._1, t._2, None, Set.empty))(
          (s: GmosCommonSC)                  => (s.detector, s.mosPreImaging)
        )

      implicit val MetaGmosShuffleOffset: Meta[GmosShuffleOffset] =
        Meta[Int].xmap(GmosShuffleOffset.unsafeFromRowCount, _.detectorRows)

      implicit val MetaGmosShuffleCycles: Meta[GmosShuffleCycles] =
        Meta[Int].xmap(GmosShuffleCycles.unsafeFromCycleCount, _.toInt)

      implicit val GmosCustomRoiEntryComposite: Composite[GmosCustomRoiEntry] =
        Composite[(Short, Short, Short, Short)].imap(
          (t: (Short, Short, Short, Short)) => GmosCustomRoiEntry.unsafeFromDescription(t._1, t._2, t._3, t._4))(
          (r: GmosCustomRoiEntry)           => (r.xMin, r.yMin, r.xRange, r.yRange)
        )

      def selectCustomRoiEntry(oid: Observation.Id, i: Instrument): Query0[GmosCustomRoiEntry] =
        sql"""
          SELECT x_min,
                 y_min,
                 x_range,
                 y_range
            FROM gmos_custom_roi
           WHERE program_id        = ${oid.pid}   AND
                 observation_index = ${oid.index} AND
                 instrument        = $i
         """.query[GmosCustomRoiEntry]

      def selectNodAndShuffle(oid: Observation.Id, i: Instrument): Query0[GmosNodAndShuffle] =
        sql"""
          SELECT a_offset_p,
                 a_offset_q,
                 b_offset_p,
                 b_offset_q,
                 e_offset,
                 offset_rows,
                 cycles
            FROM gmos_nod_and_shuffle
           WHERE program_id        = ${oid.pid}
             AND observation_index = ${oid.index}
             AND instrument        = $i
        """.query[GmosNodAndShuffle]

      def selectNorth(oid: Observation.Id): Query0[GmosNorth] =
        sql"""
          SELECT detector,
                 mos_preimaging,
                 stage_mode
            FROM static_gmos_north
           WHERE program_id        = ${oid.pid}
             AND observation_index = ${oid.index}
        """.query[GmosNorth]

      def selectSouth(oid: Observation.Id): Query0[GmosSouth] =
        sql"""
          SELECT detector,
                 mos_preimaging,
                 stage_mode
            FROM static_gmos_south
           WHERE program_id        = ${oid.pid}
             AND observation_index = ${oid.index}
        """.query[GmosSouth]

      def insertCustomRoiEntry(oid: Observation.Id, inst: Instrument, roi: GmosCustomRoiEntry): Update0 =
        sql"""
          INSERT INTO gmos_custom_roi (
                        program_id,
                        observation_index,
                        instrument,
                        x_min,
                        y_min,
                        x_range,
                        y_range)
               VALUES (
                      ${oid.pid},
                      ${oid.index},
                      $inst,
                      ${roi.xMin},
                      ${roi.yMin},
                      ${roi.xRange},
                      ${roi.yRange})
        """.update

      def insertNodAndShuffle(oid: Observation.Id, inst: Instrument, ns: GmosNodAndShuffle): Update0 =
        sql"""
          INSERT INTO gmos_nod_and_shuffle (
                program_id,
                observation_index,
                instrument,
                a_offset_p,
                a_offset_q,
                b_offset_p,
                b_offset_q,
                e_offset,
                offset_rows,
                cycles)
         VALUES (
              ${oid.pid},
              ${oid.index},
              $inst,
              ${ns.posA.p},
              ${ns.posA.q},
              ${ns.posB.p},
              ${ns.posB.q},
              ${ns.eOffset},
              ${ns.shuffle},
              ${ns.cycles})
        """.update

      def insertNorth(oid: Observation.Id, g: GmosNorth): Update0 =
        sql"""
          INSERT INTO static_gmos_north (program_id, observation_index, instrument, detector, mos_preimaging, stage_mode)
          VALUES (
            ${oid.pid},
            ${oid.index},
            ${GmosN: Instrument},
            ${g.common.detector},
            ${g.common.mosPreImaging},
            ${g.stageMode})
        """.update

      def insertSouth(oid: Observation.Id, g: GmosSouth): Update0 =
        sql"""
          INSERT INTO static_gmos_south (program_id, observation_index, instrument, detector, mos_preimaging, stage_mode)
          VALUES (
            ${oid.pid},
            ${oid.index},
            ${GmosS: Instrument},
            ${g.common.detector},
            ${g.common.mosPreImaging},
            ${g.stageMode})
        """.update

    }

    /** GNIRS Statements. */
    object Gnirs {

      def select(oid: Observation.Id): Query0[StaticConfig.Gnirs] =
        sql"""
          SELECT well_depth
            FROM static_gnirs
           WHERE program_id        = ${oid.pid}
             AND observation_index = ${oid.index}
        """.query[StaticConfig.Gnirs]

      def insert(oid: Observation.Id, gnirs: StaticConfig.Gnirs): Update0 =
        sql"""
          INSERT INTO static_gnirs (program_id, observation_index, instrument, well_depth)
          VALUES (
            ${oid.pid},
            ${oid.index},
            ${Instrument.Gnirs: Instrument},
            ${gnirs.wellDepth})
        """.update
    }

  }

}
