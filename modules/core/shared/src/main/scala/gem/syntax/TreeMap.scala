// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem.syntax

import cats.Foldable

import scala.collection.immutable.TreeMap

final class TreeMapCompanionOps(val self: TreeMap.type) extends AnyVal {

  /** Creates a `TreeMap` from a `List[(A, B)]`, provided an `Ordering[A]`
    * is available.
    */
  def fromList[A: Ordering, B](lst: List[(A, B)]): TreeMap[A, B] =
    TreeMap(lst: _*)

  /** Creates a `TreeMap` from a `Foldable[(A, B)]`, provided an `Ordering[A]`
    * is available.
    */
  def fromFoldable[F[_], A, B](fab: F[(A, B)])(implicit F: Foldable[F], A: Ordering[A]): TreeMap[A, B] =
    fromList(F.toList(fab))
}

trait ToTreeMapCompanionOps {
  implicit def ToTreeMapCompanionOps(c: TreeMap.type): TreeMapCompanionOps =
    new TreeMapCompanionOps(c)
}

final class TreeMapOps[A, B](val self: TreeMap[A, B]) extends AnyVal {

  def merge[C, D](that: Map[A, C])(f: (B, Option[C]) => D)(implicit ev: Ordering[A]): TreeMap[A, D] =
    self.foldLeft(TreeMap.empty[A, D]) { case (m, (a, b)) =>
      m.updated(a, f(b, that.get(a)))
    }
}

trait ToTreeMapOps {
  implicit def ToTreeMapOps[A: Ordering, B](m: TreeMap[A, B]): TreeMapOps[A, B] =
    new TreeMapOps(m)
}

object treemap extends ToTreeMapCompanionOps with ToTreeMapOps