// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem.util

import cats._
import cats.implicits._
import cats.effect.IO

import java.time.{ Instant, ZonedDateTime }
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit.MICROS


/** InstantMicros wraps a `java.util.Instant` that is truncated to microsecond
  * resolution.  This allows InstantMicros to roundtrip to/from the database
  * where timestamps support only microsecond resolution.  In addition the min
  * and max instants that are supported are determined by th postgres limit for
  * timestamps.
  *
  * @param toInstant
  */
final class InstantMicros private (val toInstant: Instant) extends AnyVal {

  /** Gets the number of seconds from the Java epoch of 1970-01-01T00:00:00Z. */
  def epochSecond: Long =
    toInstant.getEpochSecond

  /** Gets the number of microseconds after the start of the second returned
    * by `epochSecond`.
    */
  def µs: Long =
    toInstant.getNano / 1000L

  /** Converts this instant to the number of milliseconds from the epoch of
    * 1970-01-01T00:00:00Z.
    */
  def toEpochMilli: Long =
    toInstant.toEpochMilli

  /** Creates an updated instance of InstantMicros by applying the given
    * function to its wrapped Instant.  The computed Instant is truncated to
    * microsecond precision and validated.
    */
  private def mod(f: Instant => Instant): Option[InstantMicros] =
    InstantMicros.clip(f(toInstant))

  def plusMillis(millisToAdd: Long): Option[InstantMicros] =
    mod(_.plusMillis(millisToAdd))

  def plusMicros(microsToAdd: Long): Option[InstantMicros] =
    mod(_.plusNanos(microsToAdd * 1000))

  def plusSeconds(secondsToAdd: Long): Option[InstantMicros] =
    mod(_.plusSeconds(secondsToAdd))

  override def toString: String =
    toInstant.toString
}

object InstantMicros {

  /** Minimum time that can be stored in a postgres timestamp. */
  val Min: InstantMicros =
    truncate(ZonedDateTime.of( -4712, 1, 1, 0, 0, 0, 0, UTC).toInstant)

  /** Maximum time that can be stored in a postgres timestamp. */
  val Max: InstantMicros =
    truncate(ZonedDateTime.of(294275, 12, 31, 23, 59, 59, 999999000, UTC).toInstant)

  /** `Instant.EPOCH` transformed to `InstantMicros`. */
  val Epoch: InstantMicros =
    truncate(Instant.EPOCH)


  /** Creates an InstantMicro from the given Instant, assuring that the time
    * value recorded has a round number of microseconds.
    *
    * @group Constructors
    */
  private def truncate(i: Instant): InstantMicros =
    new InstantMicros(i.truncatedTo(MICROS))

  /** Creates an InstantMicro from the given Instant, assuring that the time
    * value recorded has a round number of microseconds and that it is within
    * the range supported by postgres.
    *
    * @group Constructors
    */
  def clip(i: Instant): Option[InstantMicros] = {
    val iʹ = truncate(i)
    if ((iʹ < Min) || (Max < iʹ)) None else Some(iʹ)
  }

  def unsafeClip(i: Instant): InstantMicros =
    clip(i).getOrElse(sys.error(s"$i out of InstantMicros range"))

  /** Creates an InstantMicro representing the current time, truncated to the
    * last integral number of microseconds.
    *
    * @group Constructors
    */
  def now: IO[InstantMicros] =
    IO {
      truncate(Instant.now())
    }

  /** Creates an InstantMicro representing the current time using milliseconds
    * from the Java time epoch.
    *
    * @group Constructors
    */
  def ofEpochMilli(epochMilli: Long): Option[InstantMicros] =
    clip(Instant.ofEpochMilli(epochMilli))

  implicit val OrderingInstantMicros: Ordering[InstantMicros] =
    Ordering.by(_.toInstant)

  implicit val OrderInstantMicros: Order[InstantMicros] =
    Order.fromOrdering

  implicit val ShowInstantMicros: Show[InstantMicros] =
    Show.fromToString
}
