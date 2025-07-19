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

package s.ide.logging;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import s.ide.utils.app.AppControl;

public class LogPrinter {

  public static void start(Logger logger) {
    // Reset the log file
    File logFile =
        new File(AppControl.applicationContext.getExternalFilesDir(null), "logs.txt");
    try {
      FileUtils.write(logFile, "", "UTF-8", false);
    } catch (Exception e) {
      e.getStackTrace();
    }

    try {
      PrintStream ps =
          new PrintStream(new FileOutputStream(logFile, true)) {
            @Override
            public void write(byte[] b, int off, int len) {
              super.write(b, off, len);

              // Assuming that each line is terminated with a newline character
              String logLine = new String(b, off, len);
              logger.d("System.out", logLine);
            }
          };

      System.setOut(ps);
      System.setErr(ps);
    } catch (Exception e) {
      e.getStackTrace();
    }
  }
}
