// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem.math

import gem.enum.Site
import gem.math.JulianDate.SecondsPerDay


object SiderealTime {

  /** Days per Julian century. Exact. */
  private val DaysPerJulianCentury: Int = // 100 * 365.25
    36525

  private val SiderealSecondsPerSecond: Double =
    1.002737909350795

  // Constants used in GMST calculation
  private val A: Double =   24110.548410
  private val B: Double = 8640184.812866
  private val C: Double =       0.093104
  private val D: Double = 6.2e-6

  /** Greenwich mean sidereal time, using the ancient 1982 IAU precession model.
    *
    * @see http://www.iausofa.org/2001_0331/sofa/gmst82.for
    */
  def greenwichMean(jd: JulianDate): HourAngle = {

    val j  = jd.toModifiedDouble
    val e  = JulianDate.J2000.toModifiedDouble
    val t  = (j       - e) / DaysPerJulianCentury
    val t0 = (j.floor - e) / DaysPerJulianCentury

    // Greenwich sidereal time at midnight, in sec
    val mid = A + B*t0 + (C - D * t) * t

    // Compute sidereal seconds since midnight
    val secs = SecondsPerDay * (j - j.floor)
    val sids = SiderealSecondsPerSecond * secs

    // GMT in sidereal seconds
    val gmt = mid + sids

    HourAngle.fromMicroseconds((gmt * 1000L * 1000L).round)
  }

  /** Local mean sidereal time at the given site.
    */
  def localMean(jd: JulianDate, s: Site): HourAngle =
    greenwichMean(jd) + Angle.hourAngle.get(s.longitude)
}
