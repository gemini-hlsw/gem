// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits._
import java.time._
import scala.math.floor
import scala.sys.process._

case class ImageManifest(history: NonEmptyList[String], timestamp: Instant, unstable: Boolean, postgresImage: String) {
  import ImageManifest.Keys

  /** Commit hash for this version. */
  def commit: String =
    history.head

  /** Timestamp as LocalDateTime. */
  def localDateTime: LocalDateTime =
    LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC)

  /** Timestamp as fractional Julian day. */
  def julianDay: Double = {
    val d = localDateTime
    val a = floor((14.0 - d.getMonthValue) / 12.0)
    val y = d.getYear + 4800.0 - a
    val m = d.getMonthValue + 12 * a - 3.0
    d.getDayOfMonth +
    floor((153.0 * m + 2.0) / 5.0) +
    365 * y +
    floor(y / 4.0) -
    floor(y / 100.0) +
    floor(y / 400.0) -
    32045.0
  }

  /** Timestamp as a J2000 fractional epoch year. */
  def j2000: Double = {
    val jd = julianDay
    val yearBasis:    Double = 2000.0
    val julianBasis:  Double = 2451545.0
    val lengthOfYear: Double = 365.25
    yearBasis + (jd - julianBasis) / lengthOfYear
  }

  /** Format a plausibly human-consumable "version" string for artifact naming. */
  def formatVersion: String =
    if (unstable) f"$j2000%8.4f-$commit-UNSTABLE"
    else          f"$j2000%8.4f-$commit"

  /** Docker labels for this ImageManifest, in a format sbt-native-packager likes. */
  def labels: Map[String, String] =
    Map(
      Keys.Commit   -> commit,
      Keys.History  -> history.intercalate(","),
      Keys.Unstable -> unstable.toString,
      Keys.Version  -> formatVersion,
      Keys.Postgres -> postgresImage
    )

  /**
   * True if this version is compatible with the given commit; that is, true if and only if the
   * given commit is in this version's history. If true then there is an upgrade path. No version
   * is ever compatible with an unstable version so we do not consider this case.
   */
  def isCompatibleWith(commit: String): Boolean =
    history.exists(_ === commit)

}

object ImageManifest {

  /** Module of docker label keys. */
  object Keys {
    val Commit   = "gem.commit"
    val History  = "gem.history"
    val Unstable = "gem.unstable"
    val Version  = "gem.version"
    val Postgres = "gem.postgres"
  }

  /**
   * Calculate the full commit history (a list of hashes) for determinig whether an olderversion's
   * data can be read. The head of this list is the release hash.
   */
  def history: IO[NonEmptyList[String]] =
    IO("git rev-list HEAD".!!)
      .map(_.split("\n").toList)
      .map(NonEmptyList.fromList)
      .map(_.getOrElse(sys.error("git history is empty")))

  /** Determine whether there are local changes past the current commit. */
  def unstable: IO[Boolean] =
    IO("git diff-index --quiet HEAD --".!).map(_ =!= 0)

  /** Compute the Unix epoch time in SECONDS for the last commit. */
  def instant: IO[Instant] =
    IO(s"git show -s --pretty=format:%ct HEAD".!!).map(_.trim.toLong).map(Instant.ofEpochSecond)

  /** Compute a manifest based on the local Git situation and the provided postgres image name. */
  def current(postgresImage: String): IO[ImageManifest] =
    (history, instant, unstable).mapN(apply(_, _, _, postgresImage))

}
