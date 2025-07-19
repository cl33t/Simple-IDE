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

import static s.ide.utils.app.AppControl.applicationContext;
import static s.ide.utils.app.theme.ThemeUtils.getConvertedColor;

import android.util.Log;
import android.view.View;

import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class TextMateThemeGenerator {
    private static String tagColor;
    private static String tagContentColor;
    private static String attributeColor;
    private static String stringColor;
    private static String punctuationColor;
    private static String constantColor;
    private static String commentColor;
    private static String functionNameColor;
    private static String namespaceColor;

    public static File generate(View rootView, String themeName) {

        getColors(rootView);

        var background = getConvertedColor(rootView, com.google.android.material.R.attr.colorSurface);
        var foreground = getConvertedColor(rootView, com.google.android.material.R.attr.colorOnSurface);
        var lineHighlightColor = adjustBrightness(background, 1.5f);
        var selectionColor = adjustBrightness(background, 2.5f);

        File themeFile = new File(applicationContext.getFilesDir(), themeName + ".json");

        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(themeFile), StandardCharsets.UTF_8))) {
            writer.setIndent("  ");
            writer.beginObject();
            writer.name("name").value("S-IDE Material3");
            writer.name("settings").beginArray();

            addBasicSettings(writer, background, foreground, foreground, lineHighlightColor, selectionColor);
            addSyntaxHighlighting(writer);

            writer.endArray();
            writer.endObject();
        } catch (IOException e) {
            Log.e("TextMateThemeGenerator", "Error creating theme file: " + e.getMessage());
        }

        return themeFile;
    }

    private static void getColors(View rootView) {
        tagColor = getConvertedColor(rootView, com.google.android.material.R.attr.colorPrimaryVariant);
        tagContentColor = getConvertedColor(rootView, com.google.android.material.R.attr.colorOnBackground);
        attributeColor = getConvertedColor(rootView, com.google.android.material.R.attr.colorSecondary);
        stringColor = getConvertedColor(rootView, com.google.android.material.R.attr.colorOnTertiaryContainer);
        punctuationColor = tagColor;
        constantColor = getConvertedColor(rootView, com.google.android.material.R.attr.colorPrimary);
        commentColor = getConvertedColor(rootView, com.google.android.material.R.attr.colorSurfaceVariant);
        functionNameColor = getConvertedColor(rootView, com.google.android.material.R.attr.colorOnSurface);
        namespaceColor = getConvertedColor(rootView, com.google.android.material.R.attr.colorTertiary);
    }

    private static void addBasicSettings(JsonWriter writer, String background, String caretColor, String foreground, String lineHighlightColor, String selectionColor) throws IOException {
        writer.beginObject();
        writer.name("settings").beginObject();
        writer.name("background").value(background);
        writer.name("caret").value(caretColor);
        writer.name("foreground").value(foreground);
        writer.name("lineHighlight").value(lineHighlightColor);
        writer.name("selection").value(selectionColor);
        writer.endObject();
        writer.endObject();
    }

    private static void addSyntaxHighlighting(JsonWriter writer) throws IOException {
        addScope(writer, new String[]{"entity.name.tag"}, tagColor, "");
        addScope(writer, new String[]{"punctuation.separator.namespace.xml", "entity.other.attribute-name.namespace.xml"}, namespaceColor, "");
        addScope(writer, new String[]{"meta.tag.xml"}, tagContentColor, "");
        addScope(writer, new String[]{"entity.other.attribute-name"}, attributeColor, "");
        addScope(writer, new String[]{"string.quoted.double", "string.quoted.single"}, stringColor, "italic");
        addScope(writer, new String[]{"punctuation.definition.tag", "punctuation.separator.key-value.json"}, punctuationColor, "");
        addScope(writer, new String[]{"meta.structure.array.json", "meta.structure.dictionary.json"}, constantColor, "bold");
        addScope(writer, new String[]{"comment", "punctuation.definition.comment"}, commentColor, "italic");
        addScope(writer, new String[]{"entity.name.function", "support.function"}, functionNameColor, "bold");
        addScope(writer, new String[]{"variable"}, attributeColor, "italic");
    }

    private static void addScope(JsonWriter writer, String[] scopes, String color, String fontStyle) throws IOException {
        writer.beginObject();
        writer.name("scope").beginArray();
        for (String scope : scopes) {
            writer.value(scope);
        }
        writer.endArray();
        writer.name("settings").beginObject();
        writer.name("foreground").value(color);
        if (!fontStyle.isEmpty()) {
            writer.name("fontStyle").value(fontStyle);
        }
        writer.endObject();
        writer.endObject();
    }

    private static String adjustBrightness(String colorHex, float factor) {
        try {
            int color = Integer.parseInt(colorHex.substring(1), 16);
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;

            r = Math.min((int) (r * factor), 255);
            g = Math.min((int) (g * factor), 255);
            b = Math.min((int) (b * factor), 255);

            return String.format("#%02X%02X%02X", r, g, b);
        } catch (Exception e) {
            Log.e("TextMateThemeGenerator", "Invalid color format: " + colorHex);
            return colorHex;
        }
    }
}
