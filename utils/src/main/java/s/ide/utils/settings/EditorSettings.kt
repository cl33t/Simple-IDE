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

class EditorSettings private constructor(context: Context) {
    @JvmField
    var selectedSymbols: MutableSet<String?>? = HashSet()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var fontSize: Int
        get() = sharedPreferences.getInt(KEY_FONT_SIZE, 18)
        set(fontSize) {
            sharedPreferences.edit {
                putInt(KEY_FONT_SIZE, fontSize)
            }
        }

    var isThemePatchingEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_ADAPT_THEME, true)
        set(enabled) {
            sharedPreferences.edit {
                putBoolean(KEY_ADAPT_THEME, enabled)
            }
        }

    var isStickyScrollEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_STICKY_SCROLL, true)
        set(enabled) {
            sharedPreferences.edit {
                putBoolean(KEY_STICKY_SCROLL, enabled)
            }
        }

    var isWordWrapEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_WORD_WRAP, false)
        set(enabled) {
            sharedPreferences.edit {
                putBoolean(KEY_WORD_WRAP, enabled)
            }
        }

    var isScrollBarEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_SCROLL_BAR, false)
        set(enabled) {
            sharedPreferences.edit {
                putBoolean(KEY_SCROLL_BAR, enabled)
            }
        }

    var isFontLigaturesEnabled: Boolean
        get() = sharedPreferences.getBoolean(KEY_FONT_LIGATURES, false)
        set(enabled) {
            sharedPreferences.edit {
                putBoolean(KEY_FONT_LIGATURES, enabled)
            }
        }

    var tabSize: Int
        get() = sharedPreferences.getInt(KEY_TAB_SIZE, 4)
        set(tabSize) {
            sharedPreferences.edit {
                putInt(KEY_TAB_SIZE, tabSize)
            }
        }

    var isGoogleJavaFormatStyle: Boolean
        get() = sharedPreferences.getBoolean(
            KEY_GOOGLE_JAVA_FORMAT_STYLE, false
        )
        set(enabled) {
            sharedPreferences.edit {
                putBoolean(KEY_GOOGLE_JAVA_FORMAT_STYLE, enabled)
            }
        }

    var googleJavaFormatOptions: MutableSet<String?>?
        get() = sharedPreferences.getStringSet(
            KEY_GOOGLE_JAVA_FORMAT_OPTIONS, HashSet<String?>()
        )
        set(options) {
            sharedPreferences.edit {
                putStringSet(KEY_GOOGLE_JAVA_FORMAT_OPTIONS, options)
            }
        }

    companion object {

        private const val PREFS_NAME = "editor_settings"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_STICKY_SCROLL = "sticky_scroll"
        private const val KEY_WORD_WRAP = "word_wrap"
        private const val KEY_SCROLL_BAR = "scroll_bar"
        private const val KEY_FONT_LIGATURES = "font_ligatures"
        private const val KEY_TAB_SIZE = "tab_size"
        private const val KEY_ADAPT_THEME = "adapt_theme"
        private const val KEY_GOOGLE_JAVA_FORMAT_STYLE = "google_java_format_style"
        private const val KEY_GOOGLE_JAVA_FORMAT_OPTIONS = "google_java_format_options"
        private var instance: EditorSettings? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): EditorSettings {
            if (instance == null) {
                instance = EditorSettings(context)
            }
            return instance!!
        }
    }
}
