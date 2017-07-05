// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem.ctl.hi

import gem.ctl.free.ctl._
import gem.ctl.low.git._
import gem.ctl.low.docker._

import cats.implicits._

/** Constructors for some common `CtlIO` operations that are shared by other commands. */
object common {

  def getUniqueRunningContainerWithLabel(label: String): CtlIO[Container] =
    findRunningContainersWithLabel(label) flatMap {
      case c :: Nil => c.pure[CtlIO]
      case Nil      => error("No running container found.")       *> exit(-1)
      case _        => error("Multiple running container found.") *> exit(-1)
    }

  def getRunningGemContainer: CtlIO[Container] =
    gosub("Finding current running Gem container.") {
      getUniqueRunningContainerWithLabel("edu.gemini.gem")
    }

  def getRunningPostgresContainer: CtlIO[Container] =
    gosub("Finding current running Postgres container.") {
      getUniqueRunningContainerWithLabel("edu.gemini.db")
    }

  def getDeployCommitForContainer(k: Container): CtlIO[DeployCommit] =
    gosub(s"Getting commit for container ${k.hash}.") {
      for {
        s <- getLabelValue("edu.gemini.commit", k)
        _ <- info(s"Commit is $s")
      } yield {
        val Suffix = "-UNCOMMITTED"
        if (s.endsWith(Suffix)) DeployCommit(Commit(s.dropRight(Suffix.length)), true)
        else                    DeployCommit(Commit(s),                          false)
      }
    }

  final case class DeployCommit(commit: Commit, uncommitted: Boolean) {
    def imageVersion: String = if (uncommitted) s"${commit.hash}-UNCOMMITTED" else commit.hash
  }

}
