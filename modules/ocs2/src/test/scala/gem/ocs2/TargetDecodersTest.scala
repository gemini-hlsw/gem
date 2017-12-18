// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem.ocs2

import gem.{ EphemerisKey, Target, Track }
import gem.math._
import gem.ocs2.Decoders._
import gem.ocs2.pio._
import gem.ocs2.pio.PioError.{ MissingKey, ParseError }

import cats.tests.CatsSuite

import scala.xml._

// Basic sanity checks for target decoding

@SuppressWarnings(Array("org.wartremover.warts.ToString", "org.wartremover.warts.Equals", "org.wartremover.warts.OptionPartial"))
final class TargetDecodersTest extends CatsSuite {
  import TargetDecodersTest._

  test("Fully specified ProperMotion") {
    PioDecoder[ProperMotion].decode(SiderealNode) match {
      case Right(actual) => assert(actual == SiderealProperMotion)
      case Left(err)     => fail(err.toString)
    }
  }

  test("Missing redshift") {
    val xml = delete("redshift", SiderealNode)
    val exp = SiderealProperMotion.copy(radialVelocity = None)
    PioDecoder[ProperMotion].decode(xml) match {
      case Right(actual) => assert(actual == exp)
      case Left(err)     => fail(err.toString)
    }
  }

  test("Missing proper-motion") {
    val xml = delete("proper-motion", SiderealNode)
    val exp = SiderealProperMotion.copy(properVelocity = None)
    PioDecoder[ProperMotion].decode(xml) match {
      case Right(actual) => assert(actual == exp)
      case Left(err)     => fail(err.toString)
    }
  }

  test("Missing delta-ra") {
    val xml = delete("delta-ra", SiderealNode)
    PioDecoder[ProperMotion].decode(xml) match {
      case Left(MissingKey(m)) => assert(m.contains("delta-ra"))
      case Right(_)            => fail("delta-ra is required if proper-motion is present")
      case other               => fail(s"unexpected: $other")
    }
  }

  test("Unexpected epoch year") {
    val xml = setValue("epoch", "2017", SiderealNode)
    PioDecoder[ProperMotion].decode(xml) match {
      case Left(ParseError("2017", "Epoch")) => succeed
      case Right(_)                          => fail("cannot handle epoch 2017")
      case other                             => fail(s"unexpected: $other")
    }
  }

  test("Sidereal target") {
    PioDecoder[Target].decode(SiderealNode) match {
      case Right(actual) => assert(actual == SiderealTarget)
      case Left(err)     => fail(err.toString)
    }
  }

  test("Nonsidereal target") {
    PioDecoder[Target].decode(NonsiderealNode) match {
      case Right(actual) => assert(actual == NonsiderealTarget)
      case Left(err)     => fail(err.toString)
    }
  }
}

@SuppressWarnings(Array("org.wartremover.warts.Equals", "org.wartremover.warts.OptionPartial"))
object TargetDecodersTest {
  val SiderealNode: Elem =
    <paramset name="target">
      <param name="name" value="Example"/>
      <param name="redshift" value="4.0"/>
      <param name="parallax" value="1.0"/>
      <paramset name="magnitude">
        <param name="value" value="17.0"/>
        <param name="band" value="r"/>
        <param name="system" value="AB"/>
      </paramset>
      <paramset name="coordinates">
        <param name="ra" value="55.291666666666686"/>
        <param name="dec" value="11.550000000000011"/>
      </paramset>
      <paramset name="proper-motion">
        <param name="delta-ra" value="2.0"/>
        <param name="delta-dec" value="3.0"/>
        <param name="epoch" value="2000.0"/>
      </paramset>
      <param name="tag" value="sidereal"/>
    </paramset>

  val SiderealProperMotion: ProperMotion = {
    val ra  = RightAscension.parse("03:41:10.000").get
    val dec = Declination.parse("11:33:00.00").get
    val c   = Coordinates(ra, dec)

    val off = Offset(
                Offset.P(Angle.fromMilliarcseconds(2)),
                Offset.Q(Angle.fromMilliarcseconds(3))
              )

    val rv  = RadialVelocity.fromRedshift(4.0)

    val px  = Angle.fromMilliarcseconds(1)

    ProperMotion(c, Epoch.J2000, Some(off), Some(rv), Some(px))
  }

  val SiderealTarget: Target =
    Target("Example", Track.Sidereal(SiderealProperMotion))

  val NonsiderealNode: Elem =
    <paramset name="target">
      <param name="name" value="Oumuamua"/>
      <paramset name="horizons-designation">
        <param name="des" value="C/1937 P1"/>
        <param name="tag" value="comet"/>
      </paramset>
      <param name="tag" value="nonsidereal"/>
    </paramset>

  val NonsiderealTarget: Target =
    Target("Oumuamua", Track.Nonsidereal(EphemerisKey.Comet("C/1937 P1"), Map.empty))

  // Deletes all instances of params and paramsets that have the given name.
  private def delete(name: String, e: Elem): Elem = {
    val children = e.child.filter(c => (c \ "@name").text != name)
    e.copy(child = children.map {
      case c: Elem => delete(name, c)
      case other   => other
    })
  }

  // Sets the value of a named "param" anywhere within the given element
  private def setValue(name: String, value: String, e: Elem): Elem = {
    val children = e.child.map {
      case e: Elem if e.label == "param" && (e \ "@name").text == name =>
          e.copy(attributes = new UnprefixedAttribute("name", name, new UnprefixedAttribute("value", value, Null)))

      case otherChild: Elem =>
        setValue(name, value, otherChild)

      case otherNode        =>
        otherNode
    }
    e.copy(child = children)
  }
}
