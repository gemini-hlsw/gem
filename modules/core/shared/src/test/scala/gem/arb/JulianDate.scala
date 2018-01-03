// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package arb

import gem.math.JulianDate

import java.time.LocalDateTime

import org.scalacheck._
import org.scalacheck.Arbitrary._


trait ArbJulianDate {
  import ArbTime._

  implicit val arbJulianDate: Arbitrary[JulianDate] =
    Arbitrary {
      arbitrary[LocalDateTime].map(JulianDate.ofLocalDateTime)
    }
}

object ArbJulianDate extends ArbJulianDate
