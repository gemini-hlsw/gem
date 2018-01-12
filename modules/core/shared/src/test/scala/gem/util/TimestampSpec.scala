// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package util

import gem.arb.ArbTime._

import cats.kernel.laws.discipline._
import cats.tests.CatsSuite


@SuppressWarnings(Array("org.wartremover.warts.Equals", "org.wartremover.warts.NonUnitStatements"))
final class TimestampSpec extends CatsSuite {

  // Laws
  checkAll("Timestamp", OrderTests[Timestamp].order)

  test("Construction should truncate Instant nanoseconds to microseconds") {
    forAll { (i: Timestamp) =>
      i.toInstant.getNano % 1000L == 0
    }
  }

}
