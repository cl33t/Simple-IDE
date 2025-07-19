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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlFormat {
    private static final Pattern TAG_PATTERN = Pattern.compile("<(/?\\w+)[^>]*>");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("(?s)<!--.*?-->");
    private static final Pattern EMPTY_LINE_PATTERN = Pattern.compile("\\n\\s*\\n");
    private final String newLine = "\n";

    public XmlFormat() {}

    public static String fmt(String xml) throws Exception {
        return new XmlFormat().formatAndFixXML(xml);
    }

    public static boolean isBalanced(String xml) {
        int balance = 0;
        String sanitizedXml = sanitizeComments(xml);
        Matcher matcher = TAG_PATTERN.matcher(sanitizedXml);

        while (matcher.find()) {
            String tag = matcher.group();
            if (tag.startsWith("<?xml") || tag.startsWith("<!")) continue;
            if (tag.startsWith("</")) balance--;
            else if (!tag.endsWith("/>")) balance++;
            if (balance < 0) return false;
        }
        return balance == 0;
    }

    private static String sanitizeComments(String xml) {
        return COMMENT_PATTERN.matcher(xml).replaceAll("");
    }

    private String removeExtraEmptyLines(String xml) {
        return EMPTY_LINE_PATTERN.matcher(xml).replaceAll(newLine);
    }

    private String formatAndFixXML(String xml) throws Exception {
        String sanitizedXml = sanitizeComments(xml).trim();

        if (!isBalanced(sanitizedXml)) {
            throw new Exception("The XML is not balanced.");
        }

        Matcher matcher = TAG_PATTERN.matcher(sanitizedXml);
        StringBuilder formatted = new StringBuilder();
        int indentLevel = 0;
        int lastMatchEnd = 0;

        String indentUnit = "    ";
        while (matcher.find()) {
            String textContent = sanitizedXml.substring(lastMatchEnd, matcher.start()).strip();
            if (!textContent.isBlank()) {
                formatted.append(indentUnit.repeat(indentLevel))
                        .append(textContent)
                        .append(newLine);
            }

            String tag = matcher.group();
            if (tag.startsWith("</")) {
                indentLevel = Math.max(indentLevel - 1, 0);
                formatted.append(indentUnit.repeat(indentLevel)).append(tag).append(newLine);
            } else if (tag.endsWith("/>")) {
                formatted.append(indentUnit.repeat(indentLevel)).append(tag).append(newLine);
            } else {
                formatted.append(indentUnit.repeat(indentLevel)).append(tag).append(newLine);
                indentLevel++;
            }

            lastMatchEnd = matcher.end();
        }

        String remainingContent = sanitizedXml.substring(lastMatchEnd).strip();
        if (!remainingContent.isBlank()) {
            formatted.append(indentUnit.repeat(indentLevel)).append(remainingContent).append(newLine);
        }

        return removeExtraEmptyLines(formatted.toString());
    }
}
