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

package s.ide.utils.app.theme;

import static s.ide.utils.app.AppControl.applicationContext;

import android.content.res.Configuration;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.MaterialColors;

public class ThemeUtils {

    @NonNull
    public static String getConvertedColor(@NonNull View view, int colorAttribute) {
        int color = MaterialColors.getColor(view, colorAttribute);
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public static int getColor(@NonNull View view, int colorAttribute) {
        return MaterialColors.getColor(view, colorAttribute);
    }

    public static boolean isDarkThemeEnabled() {
        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            return true;
        } else if (currentNightMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            int systemNightMode = applicationContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            return systemNightMode == Configuration.UI_MODE_NIGHT_YES;
        }
        return false;
    }

}
