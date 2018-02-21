// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package telnetd
package command

import gem.enum.Site

import atto._, Atto._
import cats.data.{ NonEmptyList, ValidatedNel }
import cats.implicits._

import com.monovore.decline.{ Command => _, _ }
import tuco._, Tuco._, tuco.shell._


object eph {

  def validateSite(s: String): ValidatedNel[String, Site] =
    gem.EnumParsers.site.parseOnly(s)
      .either
      .leftMap(_ => s"'$s' is not a Gemini site")
      .toValidatedNel

  val site: Opts[Site] =
    Opts.option[String]("site", help = "GN | GS", short = "s")
      .mapValidated(validateSite)

  val sites: Opts[Set[Site]] =
    Opts.options[String]("site", help = "GN | GS", short = "s")
      .mapValidated { _.map(validateSite).sequence }
      .withDefault(NonEmptyList.of(Site.GN, Site.GS))
      .map { nel => Set(nel.toList: _*) }

  def keys[K <: EphemerisKey](name: String, failMsg: String => String)(pf: PartialFunction[Option[EphemerisKey], K]): Opts[List[K]] =
    Opts.arguments[String](name)
      .orEmpty
      .mapValidated {
        _.foldMap { keyStr =>
          val k = EphemerisKey.fromString.getOption(keyStr)
          if (pf.isDefinedAt(k)) List(pf(k)).validNel[String]
          else failMsg(keyStr).invalidNel[List[K]]
        }
      }

  val exportKeys: Opts[List[EphemerisKey]] =
    keys("ephemeris-key", keyStr => s"'$keyStr' is not a valid ephemeris key") {
      case Some(k) => k
    }

  val updateKeys: Opts[List[EphemerisKey.Horizons]] =
    keys("horizons-key", keyStr => s"'$keyStr' is not a valid horizons key") {
      case Some(k: EphemerisKey.Horizons) => k
    }

  val exportCommand: GemCommand =
    Command(
      "eph-export", "Export ephemerides for the current night for use by the TCS",
      (site, exportKeys).mapN { (s: Site, ks: List[EphemerisKey]) => (d: GemState) =>
        for {
          _ <- writeLn(s"export: $s $ks")
        } yield d
      }
    ).zoom(Session.data[GemState])

  val updateCommand: GemCommand =
    Command(
      "eph-update", "Update ephemeris data from horizons",
      (sites, updateKeys).mapN { (ss: Set[Site], ks: List[EphemerisKey.Horizons]) => (d: GemState) =>
        for {
          _ <- writeLn(s"update: $ss $ks")
        } yield d

      }
    ).zoom(Session.data[GemState])

}
