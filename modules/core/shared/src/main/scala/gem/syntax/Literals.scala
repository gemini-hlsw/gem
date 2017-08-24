// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package syntax

import contextual._
import gem.math._

/**
 * Module of [[http://co.ntextu.al/ Contextual]] interpolators, which let us define compile-time
 * checked literals for program ids, angles, etc. Note that these don't work in core because they
 * depend on core already being compiled. But you can use them anywhere else, including in core
 * tests.
 */
object Lits {

  // Contextual is much more general than simple literals, so we can absract out the bits we need.
  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
  class Lit[A](parse: String => Either[String, A]) extends Interpolator {

    type Out = A

    // At compile time we just check to see that it will work
    def contextualize(si: StaticInterpolation) = {
      val lit@Literal(_, s) = si.parts.head
      parse(s).fold(si.abort(lit, 0, _), _ => Nil)
    }

    // At runtime we do it again and construct the value
    def evaluate(ri: RuntimeInterpolation): A =
      parse(ri.literals.head).fold(s => sys.error(s"Unpossible: $s"), identity)

  }

  // And now our interpolators are pretty easy to define. Note that they need to be objects because
  // of some assumptions Contextual makes. If you change them to vals it will compile but fail at
  // runtime. Note that each interpolator here needs to be mentioned in the syntax class below
  object pid    extends Lit[ProgramId](ProgramId.fromString(_).toRight("Not a valid ProgramId"))
  object hms    extends Lit[HourAngle](HourAngle.parseHMS(_).toRight("Not a valid HourAngle"))
  object dms    extends Lit[Angle](Angle.parseDMS(_).toRight("Not a valid Angle"))
  object ra     extends Lit[RA](RA.parse(_).toRight("Not a valid RightAscension"))
  object dec    extends Lit[Dec](Dec.parse(_).toRight("Not a valid Declination"))
  object coords extends Lit[Coordinates](Coordinates.parse(_).toRight("Not valid Coordinates"))

}

trait ToLiteralOps {

  /** This adds the syntax that lets you say foo"..." */
  @SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
  implicit class LiteralOps(sc: StringContext) {
    private def lit[A <: Interpolator](a: A) = Prefix(a, sc)

    // The identifier here is the syntactic prefix, so val foo = ... gives us foo"..."
    val pid    = lit(Lits.pid)
    val hms    = lit(Lits.hms)
    val dms    = lit(Lits.dms)
    val ra     = lit(Lits.ra)
    val dec    = lit(Lits.dec)
    val coords = lit(Lits.coords)

  }
}

object literals extends ToLiteralOps
