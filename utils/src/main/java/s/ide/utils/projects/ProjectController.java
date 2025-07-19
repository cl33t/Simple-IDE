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

package s.ide.utils.projects;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class ProjectController {
    private static final String PREFS_NAME = "ProjectsPrefs";
    private final SharedPreferences sharedPreferences;

    public ProjectController(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void addProject(String projectName, String projectPath, String configPath) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(projectName + "_projectPath", projectPath);
        editor.putString(projectName + "_configPath", configPath);
        editor.apply();
    }

    public Map<String, String> getProject(String projectName) {
        Map<String, String> projectInfo = new HashMap<>();
        projectInfo.put("projectPath", sharedPreferences.getString(projectName + "_projectPath", null));
        projectInfo.put("configPath", sharedPreferences.getString(projectName + "_configPath", null));
        return projectInfo;
    }

    public void updateProject(String oldName, String newName, String newProjectPath, String newConfigPath) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(newName + "_projectPath", newProjectPath);
        editor.putString(newName + "_configPath", newConfigPath);
        editor.remove(oldName + "_projectPath");
        editor.remove(oldName + "_configPath");
        editor.apply();
    }

    public void removeProject(String projectName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(projectName + "_projectPath");
        editor.remove(projectName + "_configPath");
        editor.apply();
    }
}
