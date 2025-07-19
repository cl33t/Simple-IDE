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

package s.ide.util.other;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import s.ide.utils.app.AppControl;


/** Utility class to manage certain requirements of the app */
public class BaseUtil {

  public static void unzip(String sourceFilePath, String destinationFolder) throws IOException {
    FileInputStream fis = new FileInputStream(sourceFilePath);
    unzip(fis, destinationFolder);
  }

  public static void unzip(InputStream inputStream, String destinationFolder) throws IOException {
    int BYTE_SIZE = 10240; // 10MB Cap
    byte[] buffer = new byte[BYTE_SIZE];

    ZipInputStream zipInputStream = new ZipInputStream(inputStream);
    ZipEntry zipEntry = zipInputStream.getNextEntry();
    while (zipEntry != null) {
      String entryName = zipEntry.getName();
      File file = new File(destinationFolder + File.separator + entryName);

      if (zipEntry.isDirectory()) {
        file.mkdirs();
      } else {
        file.getParentFile().mkdirs();
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
          int len;
          while ((len = zipInputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, len);
          }
        }
      }
      // close zipInputStream to prevent memory leaks
      zipInputStream.closeEntry();
      zipEntry = zipInputStream.getNextEntry();
    }
    // just added below
    zipInputStream.close();
  }

  public static class Path {
    public static final File RESOURCE_FOLDER = AppControl.applicationContext.getExternalFilesDir("resources");
    public static final File REPOSITORIES_JSON = new File(RESOURCE_FOLDER, "repositories.json");
  }
}
