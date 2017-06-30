// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem.dao

import gem._
import gem.SmartGcal._
import gem.config._
import gem.enum._

import doobie.imports._

import scalaz._, Scalaz._

object SmartGcalDao {

  def selectF2(k: F2SmartGcalKey, t: SmartGcalType): ConnectionIO[List[Int]] =
    t.fold(Statements.selectF2byLamp(k), Statements.selectF2byBaseline(k)).list

  def select(k: SmartGcalKey, t: SmartGcalType): ConnectionIO[List[GcalConfig]] =
    for {
      ids <- k match {
               case f2: F2SmartGcalKey => selectF2(f2, t)
             }
      gcs <- ids.traverseU { GcalDao.select }.map(_.collect { case Some(a) => a })
    } yield gcs

  def insert(l: GcalLampType, b: GcalBaselineType, k: SmartGcalKey, g: GcalConfig): ConnectionIO[Int] =
    for {
      id <- GcalDao.insert(g)
      r  <-
        k match {
          case f2: F2SmartGcalKey => Statements.insertSmartF2(l, b, id, f2).run
        }
    } yield r

  type ExpansionResult[A] = EitherConnectionIO[ExpansionError, A]

  private def lookupʹ(step: MaybeConnectionIO[Step[DynamicConfig]], loc: Location.Middle): ExpansionResult[ExpandedSteps] = {
    // Information we need to extract from a smart gcal step in order to expand
    // it into manual gcal steps.  The key is used to look up the gcal config
    // from the instrument's smart table (e.g., smart_f2).  The type is used to
    // extract the matching steps (arc vs. flat, or night vs. day baseline).
    // The instrument config is needed to create the corresponding gcal steps.
    type SmartContext = (SmartGcalKey, SmartGcalType, DynamicConfig)

    // Get the key, type, and instrument config from the step.  We'll need this
    // information to lookup the corresponding GcalConfig.
    val context: ExpansionResult[SmartContext] =
      for {
        s <- step.toRight(stepNotFound(loc))
        c <- s match {
               case SmartGcalStep(i, t) =>
                 EitherConnectionIO.fromDisjunction {
                   (i.smartGcalKey \/> noMappingDefined).map { k => (k, t, i) }
                 }
               case _                   =>
                 EitherConnectionIO.pointLeft(notSmartGcal)
             }
      } yield c


    // Find the corresponding smart gcal mapping, if any.
    for {
      kti  <- context
      (k, t, i) = kti
      gcal <- EitherConnectionIO(select(k, t).map {
                case Nil => noMappingDefined.left
                case cs  => cs.map(GcalStep(i, _)).right
              })
    } yield gcal
  }

  /** Lookup the corresponding `GcalStep`s for the smart step at `loc`, leaving
    * the sequence unchanged.
    *
    * @param oid observation whose smart gcal expansion is desired for the step
    *            at `loc`
    * @param loc position of the smart gcal step to expand
    *
    * @return expansion of the smart gcal step into `GcalStep`s if `loc` is
    *         found, refers to a smart gcal step, and a mapping is defined for
    *         its instrument configuration
    */
  def lookup(oid: Observation.Id, loc: Location.Middle): ExpansionResult[ExpandedSteps] =
    lookupʹ(StepDao.selectOne(oid, loc), loc)

  /** Expands a smart gcal step into the corresponding gcal steps so that they
    * may be executed. Updates the sequence to replace a smart gcal step with
    * one or more manual gcal steps.
    *
    * @param oid observation whose smart gcal expansion is desired for the step
    * @param loc position of the smart gcal step to expand
    *
    * @return expansion of the smart gcal step into `GcalConfig` if `loc` is
    *         found, refers to a smart gcal step, and a mapping is defined for
    *         its instrument configuration
    */
  def expand(oid: Observation.Id, loc: Location.Middle): ExpansionResult[ExpandedSteps] = {
    // Find the previous and next location for the smart gcal step that we are
    // replacing.  This is needed to generate locations for the steps that will
    // be inserted.
    def bounds(steps: Location.Middle ==>> Step[DynamicConfig]): (Location, Location) =
      steps.split(loc) match {
        case (prev, next) => (prev.findMax.map(_._1).widen[Location] | Location.beginning,
                              next.findMin.map(_._1).widen[Location] | Location.end)
      }

    // Inserts manual gcal steps between `before` and `after` locations.
    def insert(before: Location, gcal: ExpandedSteps, after: Location): ConnectionIO[Unit] =
      Location.find(gcal.size, before, after).toList.zip(gcal).traverseU { case (l, s) =>
        StepDao.insert(oid, l, s)
      }.void

    for {
      steps <- StepDao.selectAll(oid).injectRight
      (locBefore, locAfter) = bounds(steps)
      gcal  <- lookupʹ(MaybeConnectionIO.fromOption(steps.lookup(loc)), loc)
      // replaces the smart gcal step with the expanded manual gcal steps
      _     <- StepDao.deleteAtLocation(oid, loc).injectRight
      _     <- insert(locBefore, gcal, locAfter).injectRight
    } yield gcal
  }

  object Statements {

    def selectF2byLamp(k: F2SmartGcalKey)(l: GcalLampType): Query0[Int] =
        sql"""
          SELECT gcal_id
            FROM smart_f2
           WHERE lamp      = $l :: gcal_lamp_type
             AND disperser = ${k.disperser}
             AND filter    = ${k.filter}
             AND fpu       = ${k.fpu}
        """.query[Int]

    def selectF2byBaseline(k: F2SmartGcalKey)(b: GcalBaselineType): Query0[Int] =
        sql"""
          SELECT gcal_id
            FROM smart_f2
           WHERE baseline  = $b :: gcal_baseline_type
             AND disperser = ${k.disperser}
             AND filter    = ${k.filter}
             AND fpu       = ${k.fpu}
        """.query[Int]

    def insertSmartF2(l: GcalLampType, b: GcalBaselineType, gcalId: Int, k: F2SmartGcalKey): Update0 =
      sql"""
        INSERT INTO smart_f2 (lamp, baseline, disperser, filter, fpu, gcal_id)
             VALUES ($l :: gcal_lamp_type, $b :: gcal_baseline_type, ${k.disperser}, ${k.filter}, ${k.fpu}, $gcalId)
      """.update

  }

}
