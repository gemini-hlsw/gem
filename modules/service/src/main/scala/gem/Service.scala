// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem

import cats.implicits._
import cats.effect._
import doobie._, doobie.implicits._
import gem.dao._
import gem.enum._
import gem.horizons.EphemerisContext
import gem.util.Timestamp
import monocle.Lens

import java.nio.file.Path


final class Service[M[_]: Sync: LiftIO] private (private val xa: Transactor[M], val log: Log[M], val user: User[ProgramRole]) {

  /**
   * Construct a program that yields a list of `Program` whose name or id contains the given
   * substring (case-insensitive), up to a provided maximum length.
   */
  def queryProgramsByName(substr: String, max: Int): M[List[Program[Nothing]]] =
    log.log(user, s"""queryProgramsByName("$substr", $max)""") {
      ProgramDao.selectBySubstring(s"%$substr%", max).transact(xa)
    }

  /**
   * Construct a program that attempts to change the user's password, yielding `true` on success.
   */
  def changePassword(oldPassword: String, newPassword: String): M[Boolean] =
    log.log(user, "changePassword(***, ***)") {
      UserDao.changePassword(user.id, oldPassword, newPassword).transact(xa)
    }

  object ephemeris {

    private val exporter = new gem.horizons.tcs.TcsEphemerisExport(xa)
    private val updater  = gem.horizons.HorizonsEphemerisUpdater(xa)

    /** Constructs a program that writes an ephemeris file, returning the path
      * of the file written.
      */
    def export(key: EphemerisKey, site: Site, start: Timestamp, end: Timestamp, dir: Path): M[Path] =
      log.log(user, s"ephemeris.export($key, $site, $start, $end, $dir)") {
        exporter.exportOne(key, site, start, end, dir).as(exporter.resolve(key, dir))
      }

    /** Constructs a program that obtains ephemeris context information. */
    def report(key: EphemerisKey.Horizons, site: Site): M[EphemerisContext] =
      log.log(user, s"ephemeris.status($key, $site)") {
        updater.report(key, site)
      }

    /** Constructs a program that attempts to update ephemeris information,
      * yielding ephemeris context.
      */
    def update(key: EphemerisKey.Horizons, site: Site): M[EphemerisContext] =
      log.log(user, s"ephemeris.update($key, $site)") {
        updater.update(key, site) *> updater.report(key, site)
      }
  }
}

object Service {

  def user[M[_]: Sync: LiftIO]: Lens[Service[M], User[ProgramRole]] =
    Lens[Service[M], User[ProgramRole]](_.user)(a => b => new Service(b.xa, b.log, a))

  def apply[M[_]: Sync: LiftIO](xa: Transactor[M], log: Log[M], user: User[ProgramRole]): Service[M] =
    new Service(xa, log, user)

  /**
   * Construct a program that verifies a user's id and password and returns a `Service`.
   */
  def tryLogin[M[_]: Sync: LiftIO](
    user: User.Id, pass: String, xa: Transactor[M], log: Log[M]
  ): M[Option[Service[M]]] =
    xa.trans.apply(UserDao.selectUserʹ(user, pass)).map {
      case None    => Option.empty[Service[M]]
      case Some(u) => Some(Service[M](xa, log, u))
    }

  /**
   * Like `tryLogin`, but for previously-authenticated users.
   */
  def service[M[_]: Sync: LiftIO](
    user: User.Id, xa: Transactor[M], log: Log[M]
  ): M[Option[Service[M]]] =
    xa.trans.apply(UserDao.selectUser(user)).map {
      case None    => Option.empty[Service[M]]
      case Some(u) => Some(Service[M](xa, log, u))
    }

}
