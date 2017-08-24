// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package syntax

import cats.tests.CatsSuite
import gem.math._
import gem.syntax.literals._

@SuppressWarnings(Array("org.wartremover.warts.Throw", "org.wartremover.warts.NonUnitStatements"))
final class LiteralsSpec extends CatsSuite {

  test("pid") {
    Some(pid"GS-2012A-Q-10")  shouldEqual ProgramId.Science.fromString("GS-2012A-Q-10")
    Some(pid"GS-CAL20121023") shouldEqual ProgramId.Daily.fromString("GS-CAL20121023")
    Some(pid"GS-Blah")        shouldEqual ProgramId.Nonstandard.fromString("GS-Blah")
    assertTypeError("""pid"this is invalid"""")
  }

  test("hms") {
    Some(hms"17 57 48.49803")    shouldEqual HourAngle.parseHMS("17 57 48.49803")
    Some(hms"17:57:48.49803")    shouldEqual HourAngle.parseHMS("17 57 48.49803")
    Some(hms"17h 57m 48.49803s") shouldEqual HourAngle.parseHMS("17 57 48.49803")
    assertTypeError("""hms"invald syntax"""")
  }

  test("dms") {
    Some(dms"+04 41 36.2072")   shouldEqual Angle.parseDMS("+04 41 36.2072")
    Some(dms"+04:41:36.2072")   shouldEqual Angle.parseDMS("+04 41 36.2072")
    Some(dms"4° 41′ 36.207200″") shouldEqual Angle.parseDMS("+04 41 36.2072")
    assertTypeError("""dms"invald syntax"""")
  }

  test("ra") {
    Some(ra"17 57 48.49803")    shouldEqual RightAscension.parse("17 57 48.49803")
    Some(ra"17:57:48.49803")    shouldEqual RightAscension.parse("17 57 48.49803")
    Some(ra"17h 57m 48.49803s") shouldEqual RightAscension.parse("17 57 48.49803")
    assertTypeError("""ra"invald syntax"""")
  }

  test("dec") {
    Some(dec"+04 41 36.2072")   shouldEqual Declination.parse("+04 41 36.2072")
    Some(dec"+04:41:36.2072")   shouldEqual Declination.parse("+04 41 36.2072")
    Some(dec"4° 41′ 36.207200″") shouldEqual Declination.parse("+04 41 36.2072")
    assertTypeError("""dec"invald syntax"""")
    assertTypeError("""dec"+94 41 36.2072"""") // out of range
  }

  test("coords") {
    Some(coords"17 57 48.49803 +04 41 36.2072") shouldEqual Coordinates.parse("17 57 48.49803 +04 41 36.2072")
    Some(coords"17:57:48.49803 +04:41:36.2072") shouldEqual Coordinates.parse("17 57 48.49803 +04 41 36.2072")
    Some(coords"17h 57m 48.49803s +4° 41′ 36.207200″") shouldEqual Coordinates.parse("17 57 48.49803 +04 41 36.2072")
    assertTypeError("""coords"bogus"""")
  }

}
