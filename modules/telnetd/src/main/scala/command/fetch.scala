// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package telnetd
package command

import cats.implicits._
import com.monovore.decline.{ Command => _, _ }
import tuco._
import Tuco._
import tuco.shell._

/** Command to fetch an observation or program from an OCS2 ODB.
  */
object fetch {

  type EitherId = Either[Program.Id, Observation.Id]

  val host: Opts[String] =
    Opts.option[String](
      help    = s"ODB hostname (default localhost)",
      metavar = "host",
      short   = "h",
      long    = "host"
    ).withDefault("localhost")

  val id: Opts[EitherId] =
    Opts.argument[String](
      metavar = "id"
    ).mapValidated { s =>
      val id = Observation.Id.fromString(s).map(_.asRight[Program.Id]) orElse
                 ProgramId.fromString(s).map(_.asLeft[Observation.Id])

      id.toValidNel(s"Could not parse '$s' as an observation or program id")
    }

  val command: GemCommand =
    Command(
      "fetch", "Fetch an observation or program from an OCS2 ODB.",
      (host, id).mapN { (h: String, id: EitherId) => (d: GemState) => {
        for {
          _ <- writeLn(s"fetch $h $id")
        } yield d
      }}
    ).zoom(Session.data[GemState])
}
