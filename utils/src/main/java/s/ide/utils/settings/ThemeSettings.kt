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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

class ThemeSettings private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var theme: Int
        get() = sharedPreferences.getInt(
            KEY_THEME,
            FOLLOW_SYSTEM
        )
        set(value) {
            require(isValidTheme(value)) { "Invalid appearance value" }
            sharedPreferences.edit {
                putInt(KEY_THEME, value)
            }
            applyTheme(value)
        }

    private fun isValidTheme(theme: Int): Boolean {
        return theme == FOLLOW_SYSTEM || theme == DARK || theme == LIGHT
    }

    fun applyTheme(theme: Int) {
        when (theme) {
            DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            FOLLOW_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    companion object {
        private const val PREFS_NAME = "theme_settings"

        private const val KEY_THEME = "theme"
        const val FOLLOW_SYSTEM: Int = 0
        const val DARK: Int = 1
        const val LIGHT: Int = 2

        private var instance: ThemeSettings? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): ThemeSettings {
            if (instance == null) {
                instance = ThemeSettings(context)
            }
            return instance!!
        }
    }
}
