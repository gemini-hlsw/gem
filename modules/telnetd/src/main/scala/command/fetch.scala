// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package telnetd
package command

import cats.implicits._

/**
  *
  */
object fetch {

  val host: Opts[String] =
    Opts.option[String](
      help    = s"ODB hostname (default localhost)",
      metavar = "host",
      short   = "h",
      long    = "host"
    ).withDefault("localhost")

  val id: Opts[Either[Program.Id, Observation.Id]]
    Opts.argument[String](
      metavar = "id"
    ).mapValidated { s =>
      (Observation.Id.fromString(s).map(Either.right) orElse ProgramId.fromString(s).map(Either.left))
        .fold(Validated.invalidNel(s"Could not parse '$s' as an observation or program id")) { e =>
          Validated.valid(e)
        }
    }

}
