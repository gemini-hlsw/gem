// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem.ocs2

import cats.effect.IO
import doobie.Transactor

trait DevTransactor {

  val Url  = "jdbc:postgresql:gem"
  val User = "postgres"
  val Pass = ""

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", Url, User, Pass
  )

}
