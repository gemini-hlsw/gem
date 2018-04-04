// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem.math

import gem.arb._
import gem.enum.Site

import cats.tests.CatsSuite

import java.time._

@SuppressWarnings(Array("org.wartremover.warts.ToString", "org.wartremover.warts.Equals"))
final class SiderealTimeSpec extends CatsSuite {

  import ArbEnumerated._
  import ArbJulianDate._

  test("specific date") {
    val jd = JulianDate.ofLocalDateTime(
      LocalDateTime.of(2019, 1, 1, 8, 0, 0)
    )

    val g = SiderealTime.greenwichMean(jd)
    g.toDoubleHours shouldEqual HourAngle.fromHMS(14, 42, 45, 400, 0).toDoubleHours +- 0.00001
  }

  test("comparative lst") {
    forAll { (jd: JulianDate, s: Site) =>
      val a   = SiderealTime.localMean(jd, s)

      val hrs = gem.math.old.ImprovedSkyCalcMethods.lst(new gem.math.old.JulianDate(jd.toDouble), -s.longitude.toSignedDoubleDegrees / 15.0)
      val b   = HourAngle.fromDoubleHours(hrs)

      // The new calculation compares to the old down to about a 10th of a
      // second, which seems to be about the level of accuracy of this method
      // of calculating sidereal time anyway.

      a.toDoubleHours shouldEqual b.toDoubleHours +- 0.00001
    }
  }

}
