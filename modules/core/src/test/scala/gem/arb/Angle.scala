// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package arb

import gem.math.{ Angle, HourAngle }
import org.scalacheck._
import org.scalacheck.Arbitrary._
import org.scalacheck.Cogen._

trait ArbAngle {

  implicit def arbAngle: Arbitrary[Angle] =
    Arbitrary(arbitrary[Double].map(Angle.fromDoubleDegrees))

  implicit def arbHourAngle: Arbitrary[HourAngle] =
    Arbitrary(arbitrary[Double].map(HourAngle.fromDoubleHours))

  implicit def cogAngle: Cogen[Angle] =
    Cogen[Double].contramap(_.toDoubleDegrees)

  implicit def cogHourAngle: Cogen[HourAngle] =
    Cogen[Double].contramap(_.toDoubleDegrees)

}
object ArbAngle extends ArbAngle
