// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem.dao
package check

import gem.{ GuideGroup, GuideTarget, Target }
import gem.enum.Guider


class GuideTargetCheck extends Check {

  import GuideTargetDao.Statements._

  "GuideTargetDao" should
            "insert"      in check(insert(GuideGroup.Id(0), Target.Id(0), Guider.GmosSOi, Dummy.observationId, Dummy.instrument))
  it should "select"      in check(select(GuideTarget.Id(0)))
  it should "selectGroup" in check(selectGroup(GuideGroup.Id(0)))
  it should "selectObs"   in check(selectObs(Dummy.observationId))
  it should "selectProg"  in check(selectProg(Dummy.programId))

}
