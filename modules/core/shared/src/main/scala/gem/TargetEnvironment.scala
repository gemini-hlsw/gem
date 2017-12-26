// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem

/** Collection of targets associated with an observation.
  */
final case class TargetEnvironment(
  /* asterism, */
  /* guide stars, */
  userTargets: Set[UserTarget]
)