/*
 * Simple IDE
 * Repository: https://github.com/vxhjsd/Simple-IDE
 * Developer: vxhjsd <vxhjsd@gmail.com>
 *
 * Copyright (C) 2025  vxhjsd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package s.ide.utils.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class BuildAndRunSettings private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isAutoInstallEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_AUTO_INSTALL, true)
        set(enabled) {
            sharedPreferences.edit {
                putBoolean(KEY_AUTO_INSTALL, enabled)
            }
        }

    var isDetailedLogsEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_DETAILED_LOGS, false)
        set(enabled) {
            sharedPreferences.edit {
                putBoolean(KEY_DETAILED_LOGS, enabled)
            }
        }

    var isDesugarEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_DESUGAR, true)
        set(enabled) {
            sharedPreferences.edit {
                putBoolean(KEY_DESUGAR, enabled)
            }
        }

    companion object {
        private const val PREFS_NAME = "all_settings"

        private const val KEY_AUTO_INSTALL = "auto_install"
        private const val KEY_DETAILED_LOGS = "detailed_logs"
        private const val KEY_DESUGAR = "desugar"

        private var instance: BuildAndRunSettings? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): BuildAndRunSettings {
            if (instance == null) {
                instance = BuildAndRunSettings(context)
            }
            return instance!!
        }
    }
}
