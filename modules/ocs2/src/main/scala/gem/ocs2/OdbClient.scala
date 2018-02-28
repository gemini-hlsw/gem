// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem
package ocs2

import gem.ocs2.Decoders._

import gem.ocs2.pio.{ PioDecoder, PioError }
import gem.ocs2.pio.PioError._

import doobie._
import doobie.implicits._
import cats.effect._
import cats.implicits._
import fs2.Stream
import org.http4s.client.blaze.Http1Client
import org.http4s.scalaxml.xml

import java.net.URLEncoder

import scala.xml.Elem


/** ODB observation/program fetch client.
  */
object OdbClient {

  /** Fetches an observation from an ODB. */
  def fetchObservation[M[_]: Effect](
    host: String,
    id:   Observation.Id
  ): M[Either[String, (Observation.Full, List[Dataset])]] =
    fetch[Observation.Full, M](host, id.format)

  /** Fetches a program from the ODB. */
  def fetchProgram[M[_]: Effect](
    host: String,
    id:   Program.Id
  ): M[Either[String, (Program[Observation.Full], List[Dataset])]] =
    fetch[Program[Observation.Full], M](host, id.format)

  /** Fetches an observation from the ODB and stores it in the database. */
  def importObservation[M[_]: Effect](
    host: String,
    id:   Observation.Id,
    xa:   Transactor[M]
  ): M[Either[String, Unit]] =
    fetchObservation(host, id).flatMap { _.traverse { case (o, ds) =>
      Importer.importObservation(id, o, ds).transact(xa)
    }}

  /** Fetches a program from the ODB and stores it in the database. */
  def importProgram[M[_]: Effect](
    host: String,
    id:   Program.Id,
    xa:   Transactor[M]
  ): M[Either[String, Unit]] =
    fetchProgram(host, id).flatMap { _.traverse { case (p, ds) =>
      Importer.importProgram(p, ds).transact(xa)
    }}

  private def fetchServiceUrl(host: String): String =
    s"http://$host:8442/ocs3/fetch"

  private def uri(host: String, id: String): String =
    s"${fetchServiceUrl(host)}/${URLEncoder.encode(id, "UTF-8")}"

  private def errorMessage(e: PioError): String =
    e match {
      case MissingKey(name)            => s"missing '$name'"
      case ParseError(value, dataType) => s"could not parse '$value' as '$dataType'"
    }

  private def fetch[A: PioDecoder, M[_]: Effect](
    host: String,
    id:   String
  ): M[Either[String, (A, List[Dataset])]] = {

    val s = Http1Client.stream().flatMap { c =>
      Stream.eval {
        c.expect[Elem](uri(host, id))
         .map(PioDecoder[(A, List[Dataset])].decode(_).leftMap(errorMessage))
         .attempt
         .map(_.leftMap(ex => s"Problem fetching '$id': ${ex.getMessage}").flatten)
      }
    }

    s.compile
     .last
     .map(_.getOrElse("Impossible empty stream".asLeft[(A, List[Dataset])]))
  }

}
