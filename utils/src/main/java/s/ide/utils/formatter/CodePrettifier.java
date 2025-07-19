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

package s.ide.utils.formatter;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CodePrettifier {

    private static String formatXmlCode(String code) throws Exception {
        return XmlFormat.fmt(code);
    }

    private static String formatJsonCode(String code) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Object json = gson.fromJson(code, Object.class); // Parse JSON into a generic object
        return gson.toJson(json); // Convert the object back to a pretty-printed JSON string
    }

    private static String formatJavaCode(Context context, String code) {
        return GoogleJavaFormat.formatCode(context, code);
    }

    private static String readCodeFromFile(Path filePath) throws IOException {
        return new String(Files.readAllBytes(filePath));
    }

    public static String getFormattedCode(Context context, Path filePath) throws Exception {
        String code = readCodeFromFile(filePath);
        String fileName = filePath.getFileName().toString();
        if (fileName.endsWith(".xml")) {
            return formatXmlCode(code);
        } else if (fileName.endsWith(".json")) {
            return formatJsonCode(code);
        } else if (fileName.endsWith(".java")) {
            return formatJavaCode(context, code);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + fileName);
        }
    }
}
