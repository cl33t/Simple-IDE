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

package s.ide.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import s.ide.R;
import s.ide.databinding.ActivityMainBinding;
import s.ide.ui.dialog.CustomMaterialDialog;
import s.ide.ui.menu.BottomListMenu;
import s.ide.utils.projects.ProjectController;

public class MainActivity extends AppCompatActivity {

    private final List<Map<String, Object>> data = new ArrayList<>();
    private final Intent intent = new Intent();
    private ActivityMainBinding binding;
    private ListView listView;
    private SwipeRefreshLayout swipeView;
    private ProjectController projectController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);

        setSupportActionBar(binding.toolbar);

        swipeView = binding.getRoot().findViewById(R.id.swipe_view);
        listView = binding.getRoot().findViewById(R.id.list_view);

        projectController = new ProjectController(this);

        setupRefreshLayout();
        setupFloatingActionButton();

    }

    @Override
    public void onStart() {
        super.onStart();
        refreshList();
    }

    private void setupRefreshLayout() {
        swipeView.setOnRefreshListener(this::refreshList);
    }

    private void setupFloatingActionButton() {
        binding.fab.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), CreateProjectActivity.class);
            startActivity(intent);
        });
    }

    private void refreshList() {
        data.clear();
        String path = getFilesDir() + File.separator + getString(R.string.app_name);
        File folder = new File(path);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                Map<String, Object> info = new HashMap<>();
                info.put("path", file.getAbsolutePath());
                info.put("name", file.getName());
                info.put("date", file.lastModified());
                data.add(info); // Ensure data is a List<Map<String, Object>>
            }
        }

        data.sort((a, b) -> ((Long) Objects.requireNonNull(b.get("date"))).compareTo((Long) Objects.requireNonNull(a.get("date"))));
        listView.setAdapter(new Adapter(data));
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
        swipeView.setRefreshing(false);
    }

    private void showRenameDialog(Map<String, Object> getter) {
        CustomMaterialDialog renameDialog = new CustomMaterialDialog(MainActivity.this);
        renameDialog.setTitle(R.string.rename_dialog_title);
        renameDialog.setInputEditTextHint(R.string.hint_edittext_choose);
        renameDialog.setPositiveButton(R.string.action_rename, (_dialog, _which) -> {
            String newName = Objects.requireNonNull(renameDialog.getTextInputEditText().getText()).toString();
            if (!TextUtils.isEmpty(newName)) {
                renameProject(getter, newName);
            } else {
                showSnackbar(R.string.some_error);
            }
        });
        renameDialog.setNegativeButton(R.string.action_negative, (_dialog, _which) -> {
        });
        renameDialog.show();
    }

    private void renameProject(Map<String, Object> getter, String newName) {
        String oldPath = (String) getter.get("path");
        File oldFile = new File(Objects.requireNonNull(oldPath));
        File newFile = new File(oldFile.getParent(), newName);

        if (newFile.exists()) {
            showSnackbar(R.string.message_already_exist);
        } else if (oldFile.renameTo(newFile)) {
            Log.d("dbg rename", newFile.getAbsolutePath());
            updateProjectNameAndConfig(oldFile.getName(), newName);
            showSnackbar(R.string.message_renamed);
            refreshList();
        } else {
            showSnackbar(R.string.some_error);
        }
    }

    private void updateProjectNameAndConfig(String oldName, String newName) {
        Map<String, String> projectInfo = projectController.getProject(oldName);
        String projectPath = projectInfo.get("projectPath");
        String configFilePath = projectInfo.get("configPath");

        String newProjectPath = Objects.requireNonNull(projectPath).replace(oldName, newName);
        String newConfigFilePath = Objects.requireNonNull(configFilePath).replace(oldName, newName);

        projectController.updateProject(oldName, newName, newProjectPath, newConfigFilePath);

        updateConfigFile(newConfigFilePath, oldName, newName);
    }

    private void updateConfigFile(String configFilePath, String oldProjectName, String newProjectName) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            BufferedReader reader = new BufferedReader(new FileReader(configFilePath));
            Map<String, String> config = gson.fromJson(reader, Map.class);
            reader.close();

            config.replaceAll((key, value) -> value.replace(oldProjectName, newProjectName));

            FileWriter writer = new FileWriter(configFilePath);
            gson.toJson(config, writer);
            writer.close();
        } catch (IOException e) {
            Log.d(getString(R.string.some_error), e.toString());
        }
    }

    private void showDeleteDialog(Map<String, Object> getter) {
        CustomMaterialDialog deleteDialog = new CustomMaterialDialog(MainActivity.this);
        deleteDialog.setTitle(R.string.confirm_action);
        deleteDialog.setMessage(R.string.confirm_delete);
        deleteDialog.setPositiveButton(R.string.action_delete, (_dialog, _which) -> {
            String path = (String) getter.get("path");
            if (path != null) {
                deleteDirectory(new File(path));
            }
        });
        deleteDialog.setNegativeButton(R.string.action_negative, (_dialog, _which) -> {
        });
        deleteDialog.show();
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        if (dir.delete()) {
            showSnackbar(R.string.message_removed);
            refreshList();
        }
    }

    private void showSnackbar(int messageId) {
        Snackbar.make(binding.getRoot(), messageId, Snackbar.LENGTH_LONG).setAnchorView(binding.fab).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.popup_menu_1, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {

        final String opt = (String) item.getTitle();

        if (Objects.equals(opt, getApplicationContext().getString(R.string.settings))) {
            Intent settings = new Intent();
            settings.setClass(getApplicationContext(), SettingsActivity.class);
            startActivity(settings);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return false;
    }

    public class Adapter extends BaseAdapter {

        private final List<Map<String, Object>> smth;

        public Adapter(List<Map<String, Object>> data) {
            this.smth = data;
        }

        @Override
        public int getCount() {
            return smth.size();
        }

        @Override
        public Map<String, Object> getItem(int position) {
            return smth.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                convertView = inflater.inflate(R.layout.projects_list_item, parent, false);
                holder = new ViewHolder();
                holder.v1 = convertView.findViewById(R.id.v_1);
                holder.tv1 = convertView.findViewById(R.id.tv_1);
                holder.tv2 = convertView.findViewById(R.id.tv_2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Map<String, Object> info = smth.get(position);
            holder.tv1.setText((String) info.get("name"));
            holder.tv2.setText((String) info.get("path"));

            holder.v1.setOnLongClickListener(v -> {
                BottomListMenu bottomListMenu = new BottomListMenu();
                bottomListMenu.addItem(0, getString(R.string.action_rename), AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_edit));
                bottomListMenu.addItem(1, getString(R.string.action_remove), AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_delete));
                bottomListMenu.setOnMenuItemClickListener(menuItemPosition -> {
                    switch (menuItemPosition) {
                        case 0:
                            showRenameDialog(info);
                            break;
                        case 1:
                            showDeleteDialog(info);
                            break;
                    }
                });

                bottomListMenu.show(MainActivity.this);
                return true;
            });

            holder.v1.setOnClickListener(v -> {
                intent.putExtra("projectName", (String) info.get("name"));
                intent.setClass(getApplicationContext(), EditorActivity.class);
                startActivity(intent);
            });

            return convertView;
        }

        static class ViewHolder {
            MaterialCardView v1;
            TextView tv1;
            TextView tv2;
        }
    }

}