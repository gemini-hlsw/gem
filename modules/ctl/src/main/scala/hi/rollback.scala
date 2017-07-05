// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem.ctl.hi

import gem.ctl.free.ctl._
import gem.ctl.low.docker._
import gem.ctl.hi.common._
import gem.ctl.hi.deploy._

import cats.implicits._

/** Constructors for `CtlIO` operations related to the `rollback` command. */
object rollback {

  val awaitNetRetries: Int = 9

  def getPreviousContainer(name: String, k: Container): CtlIO[Container] =
    gosub(s"Finding previous $name.") {
      for {
        p <- getLabelValue("edu.gemini.prev", k).map(Container(_))
        _ <- info(s"Previous container was ${p.hash}")
      } yield p
    }

  def stopContainers(ks: Container*): CtlIO[Unit] =
    gosub(s"Stopping current deployment") {
      ks.toList.traverse_(stopContainer)
    }

  val rollback: CtlIO[Unit] =
    gosub("Attempting rollback to previous version.") {
      for {
        kGemC <- getRunningGemContainer
        kGem  <- getPreviousContainer("Gem container", kGemC)
        kPgC  <- getRunningPostgresContainer
        kPg   <- getPreviousContainer("Postgres container", kPgC)
        _     <- stopContainers(kGemC, kPgC)
        _     <- gosub("Starting previous Postgres container.") {
                   startContainer(kPg) *> awaitHealthy(kPg)
                 }
        _     <- gosub("Starting previous Postgres container.") {
                   for {
                     _     <- startContainer(kGem)
                     h     <- serverHostName
                     _     <- awaitNet(h, Port, awaitNetRetries)
                   } yield ()
                 }
      } yield ()
    }

}
