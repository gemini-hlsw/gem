// Copyright (c) 2016-2017 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package gem.sql
package enum

import doobie._, doobie.implicits._
import shapeless.record._

object GnirsEnums {
  import Angle.Arcseconds

  val enums: List[ConnectionIO[EnumDef]] =
    List(

      EnumDef.fromQuery("GnirsCamera", "GNIRS Camera") {
        type R = Record.`'tag -> String, 'shortName -> String, 'longName -> String, 'pixelScale -> BigDecimal`.T
        sql"""SELECT id, id tag, short_name, long_name, pixel_scale FROM e_gnirs_camera""".query[(String, R)]
      },

      EnumDef.fromQuery("GnirsDecker", "GNRIS Decker") {
        type R = Record.`'tag -> String, 'shortName -> String, 'longName -> String, 'obsolete -> Boolean`.T
        sql"""SELECT id, id tag, short_name, long_name, obsolete FROM e_gnirs_decker""".query[(String, R)]
      },

      EnumDef.fromQuery("GnirsDisperser", "GNIRS Disperser") {
        type R = Record.`'tag -> String, 'shortName -> String, 'longName -> String, 'rulingDensity -> Int`.T
        sql"""SELECT id, id tag, short_name, long_name, ruling_density FROM e_gnirs_disperser""".query[(String, R)]
      },

      EnumDef.fromQuery("GnirsDisperserOrder", "GNIRS Disperser Order") {
        type R = Record.`'tag -> String, 'shortName -> String, 'longName -> String, 'count -> Int, 'defaultWavelength -> Wavelength.Um, 'minWavelength -> Wavelength.Um, 'maxWavelength -> Wavelength.Um, 'deltaWavelength -> Wavelength.Um, 'band -> Option[MagnitudeBand], 'cross_dispersed -> Boolean`.T
        sql"""SELECT id, id tag, short_name, long_name, count, default_wavelength, min_wavelength, max_wavelength, delta_wavelength, band, cross_dispersed FROM e_gnirs_disperser_order""".query[(String, R)]
      },

      EnumDef.fromQuery("GnirsFilter", "GNIRS Filter") {
        type R = Record.`'tag -> String, 'shortName -> String, 'longName -> String, 'waveLength -> Option[Wavelength.Um]`.T
        sql"""SELECT id, id tag, short_name, long_name, wavelength FROM e_gnirs_filter""".query[(String, R)]
      },

      EnumDef.fromQuery("GnirsFpuSlit", "GNIRS FPU Slit") {
        type R = Record.`'tag -> String, 'shortName -> String, 'longName -> String, 'slitWidth -> Arcseconds, 'obsolete -> Boolean`.T
        sql"""SELECT id, id tag, short_name, long_name, slit_width, obsolete FROM e_gnirs_fpu_slit""".query[(String, R)]
      },

      EnumDef.fromQuery("GnirsFpuOther", "GNIRS FPU Other") {
        type R = Record.`'tag -> String, 'shortName -> String, 'longName -> String, 'obsolete -> Boolean`.T
        sql"""SELECT id, id tag, short_name, long_name, obsolete FROM e_gnirs_fpu_other""".query[(String, R)]
      },

      EnumDef.fromQuery("GnirsWellDepth", "GNRIS Well Depth") {
        type R = Record.`'tag -> String, 'shortName -> String, 'longName -> String, 'bias_level -> Int`.T
        sql"""SELECT id, id tag, short_name, long_name, bias_level FROM e_gnirs_well_depth""".query[(String, R)]
      }

    )

}
