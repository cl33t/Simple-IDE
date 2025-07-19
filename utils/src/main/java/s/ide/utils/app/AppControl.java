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

package s.ide.utils.app;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.material.color.DynamicColors;

import s.ide.utils.settings.ThemeSettings;

public class AppControl extends Application {

    public static Context applicationContext;

    public static void restartActivity() {
        Intent intent = applicationContext.getPackageManager().getLaunchIntentForPackage(applicationContext.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            System.exit(0);
            try {
                pendingIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.getLocalizedMessage();
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        applicationContext = this;

        DynamicColors.applyToActivitiesIfAvailable(this);

        ThemeSettings themeSettings = ThemeSettings.getInstance(applicationContext);
        themeSettings.applyTheme(themeSettings.getTheme());

    }

}
