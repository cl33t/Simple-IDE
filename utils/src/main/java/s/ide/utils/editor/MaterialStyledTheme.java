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

package s.ide.utils.editor;

import static s.ide.utils.app.theme.ThemeUtils.getColor;
import static s.ide.utils.app.theme.ThemeUtils.isDarkThemeEnabled;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.langs.java.JavaLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula;

public class MaterialStyledTheme {

    @NonNull
    public static EditorColorScheme get(@NonNull CodeEditor editor) {

        EditorColorScheme scheme = editor.getColorScheme();

        int primary = getColor(editor, com.google.android.material.R.attr.colorPrimary);
        int surface = getColor(editor, com.google.android.material.R.attr.colorSurface);
        int onSurface = getColor(editor, com.google.android.material.R.attr.colorOnSurface);
        int onSurfaceVariant = getColor(editor, com.google.android.material.R.attr.colorOnSurfaceVariant);

        scheme.setColor(EditorColorScheme.KEYWORD, primary);
        scheme.setColor(EditorColorScheme.FUNCTION_NAME, primary);
        scheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, surface);
        scheme.setColor(EditorColorScheme.CURRENT_LINE, surface);
        scheme.setColor(EditorColorScheme.LINE_NUMBER_PANEL, surface);
        scheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, surface);
        scheme.setColor(EditorColorScheme.TEXT_NORMAL, onSurface);
        scheme.setColor(EditorColorScheme.SELECTION_INSERT, onSurfaceVariant);

        return scheme;
    }

    public static void loadJavaConfig(@NonNull final CodeEditor editor) {

        editor.setEditorLanguage(new JavaLanguage());

        if (isDarkThemeEnabled()) {
            editor.setColorScheme(new SchemeDarcula());
        } else {
            editor.setColorScheme(new EditorColorScheme());
        }

        editor.setColorScheme(get(editor));
    }

}
