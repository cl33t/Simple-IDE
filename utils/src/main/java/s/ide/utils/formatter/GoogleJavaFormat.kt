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

package s.ide.utils.formatter

import android.content.Context
import com.google.googlejavaformat.java.Main
import s.ide.utils.settings.EditorSettings
import java.io.OutputStreamWriter
import java.io.PrintWriter
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object GoogleJavaFormat {
    @JvmStatic
    fun formatCode(context: Context, code: String): String {
        val settings = EditorSettings.getInstance(context)
        val file = createTempFile("file", ".java")
        file.writeText(code)
        println("Formatting code...")
        println("Formatting with options: ${settings.googleJavaFormatOptions}, style: ${settings.isGoogleJavaFormatStyle}")

        val args = mutableListOf<String>().apply {
            if (!settings.isGoogleJavaFormatStyle) {
                add("-aosp")
            }
            settings.googleJavaFormatOptions?.let { addAll(it.filterNotNull()) }
            add("-r")
            add(file.absolutePathString())
        }

        Main(
            PrintWriter(OutputStreamWriter(System.out)),
            PrintWriter(OutputStreamWriter(System.out)),
            System.`in`
        ).format(*args.toTypedArray())

        val formattedCode = file.readText()
        file.deleteIfExists()

        return formattedCode
    }
}
