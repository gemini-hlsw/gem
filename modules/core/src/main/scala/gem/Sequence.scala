package gem

import scalaz._
import Scalaz._

case class Sequence[S](
  id: Sequence.Id,
  steps: List[S])

object Sequence {

  trait Id {
    def oid: Observation.Id
    def name: String
  }

  object Id {
    def fromName(o: Observation.Id, n: String): Option[Sequence.Id] =
      !(n.contains('-') || "" === n.trim) option new Sequence.Id {
        override val oid: Observation.Id = o
        override val name: String        = n.trim

        override def equals(o: Any): Boolean =
          o match {
            case id: Id => id.oid === oid && id.name === name
            case _      => false
          }

        override def hashCode: Int =
          41 * (41 + oid.hashCode) +  name.hashCode

        override def toString: String =
          s"${oid.toString}-$n"
      }

    def unsafeFromName(o: Observation.Id, n: String): Sequence.Id =
      fromName(o, n) | sys.error(s"Illegal sequence name '$n'")

    def fromString(s: String): Option[Sequence.Id] =
      s.lastIndexOf('-') match {
        case -1 => None
        case  n =>
          val (a, b) = s.splitAt(n)
          Observation.Id.fromString(a).flatMap { Id.fromName(_, b.drop(1)) }
      }

    def unsafeFromString(s: String): Sequence.Id =
      fromString(s) | sys.error(s"Malformed Sequence.Id: $s")

    def unapply(arg: Id): Option[(Observation.Id, String)] =
      Some((arg.oid, arg.name))
  }

  implicit def SequenceTraverse[T]: Traverse[Sequence[?]] =
    new Traverse[Sequence[?]] {
      def traverseImpl[G[_]: Applicative, A, B](fa: Sequence[A])(f: A => G[B]): G[Sequence[B]] =
        fa.steps.traverse(f).map(ss => fa.copy(steps = ss))
    }

  implicit val EqualSequenceId: Equal[Sequence.Id] = Equal.equalA
}
