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

import static android.graphics.Typeface.createFromAsset;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static s.ide.compiler.Compiler.getAndroidJarFile;
import static s.ide.compiler.Compiler.getLambdaFactoryFile;
import static s.ide.utils.app.theme.ThemeUtils.isDarkThemeEnabled;
import static s.ide.utils.editor.MaterialStyledTheme.loadJavaConfig;
import static s.ide.utils.editor.TextMateThemeGenerator.generate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.io.FileUtils;
import org.eclipse.tm4e.core.registry.IThemeSource;
import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import eup.dependency.haven.api.CachedLibrary;
import eup.dependency.haven.callback.DependencyResolutionCallback;
import eup.dependency.haven.callback.DownloadCallback;
import eup.dependency.haven.model.Coordinates;
import eup.dependency.haven.model.Dependency;
import eup.dependency.haven.model.Pom;
import eup.dependency.haven.repository.LocalStorageFactory;
import eup.dependency.haven.repository.RemoteRepository;
import eup.dependency.haven.repository.Repository;
import eup.dependency.haven.resolver.DependencyResolver;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.EditorSearcher;
import io.github.rosemoe.sora.widget.SymbolInputView;
import kotlin.io.FilesKt;
import s.ide.BuildConfig;
import s.ide.R;
import s.ide.compiler.CompilerTask;
import s.ide.databinding.ActivityEditorBinding;
import s.ide.lib.LibraryDownloaderDialogFragment;
import s.ide.lib.LibraryDownloaderListener;
import s.ide.lib.LocalLibraryManager;
import s.ide.logging.LogAdapter;
import s.ide.logging.LogPrinter;
import s.ide.logging.LogViewModel;
import s.ide.logging.Logger;
import s.ide.signer.model.Library;
import s.ide.signer.model.Project;
import s.ide.ui.dialog.CustomMaterialDialog;
import s.ide.ui.menu.BottomListMenu;
import s.ide.ui.treeview.TreeViewList;
import s.ide.util.other.BaseUtil;
import s.ide.utils.formatter.CodePrettifier;
import s.ide.utils.projects.ProjectController;
import s.ide.utils.settings.BuildAndRunSettings;
import s.ide.utils.settings.EditorSettings;

/**
 * @noinspection rawtypes, ResultOfMethodCallIgnored
 */
public class EditorActivity extends AppCompatActivity implements LibraryDownloaderListener {

    private static final String[] SYMBOLS = {"->", "{", "}", "(", ")", ",", ".", ";", "\"", "?", "+", "-", "*", "/", "<", ">", "[", "]", ":"};
    private static final String[] SYMBOL_INSERT_TEXT = {"\t", "{}", "}", "(", ")", ",", ".", ";", "\"", "?", "+", "-", "*", "/", "<", ">", "[", "]", ":"};
    private final List<TabData> openedFiles = new ArrayList<>();
    private ActivityEditorBinding binding;
    private SymbolInputView symbols;
    private RecyclerView compiler;
    private CodeEditor editor;
    private LinearLayout searchLayout;
    private MaterialButton prev;
    private MaterialButton next;
    private MaterialButton replace;
    private MaterialButton replaceAll;
    private MaterialButton searchMenu;
    private TextInputEditText query;
    private TextInputEditText replacement;
    private EditorSettings editorSettings;
    private BuildAndRunSettings buildAndRunSettings;
    private int selectedTabIndex = -1;
    private Logger logger;
    private LogAdapter logAdapter;
    private boolean caseSensitive = false;
    private boolean useRegex = false;
    private Menu menu;
    private CompilerTask compilerTask = null;
    private Coordinates coordinates = null;
    private LocalStorageFactory storageFactory;
    private LocalLibraryManager libraryManager;
    private DependencyResolver resolver;
    private String currentEditorExtension = "";
    private Map<String, String> config;
    private String projectPath;
    private String projectName;
    private String folderPath;
    private final ActivityResultLauncher<Intent> importFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            Uri fileUri = result.getData().getData();
            if (fileUri != null) {
                String fileName = null;
                Cursor cursor = getContentResolver().query(fileUri, null, null, null, null);
                if (cursor != null) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex);
                    }
                    cursor.close();
                }
                if (fileName == null) {
                    fileName = new File(Objects.requireNonNull(fileUri.getPath())).getName();
                }

                try {
                    InputStream inputStream = getContentResolver().openInputStream(fileUri);
                    File destinationFile = new File(folderPath, fileName);
                    FileOutputStream outputStream = new FileOutputStream(destinationFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = Objects.requireNonNull(inputStream).read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    inputStream.close();
                    outputStream.close();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.some_error), Toast.LENGTH_SHORT).show();
                }
            }
        }
    });

    public static void listDir(String path, ArrayList<String> list) {
        File dir = new File(path);
        if (!dir.exists() || dir.isFile()) return;

        File[] listFiles = dir.listFiles();
        if (listFiles == null || listFiles.length == 0) return;

        if (list == null) return;
        list.clear();
        for (File file : listFiles) {
            list.add(file.getAbsolutePath());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditorBinding.inflate(getLayoutInflater());

        symbols = binding.body.findViewById(R.id.symbolInput);
        editor = binding.body.findViewById(R.id.editor);
        compiler = binding.body.findViewById(R.id.recyclerView);
        searchLayout = binding.body.findViewById(R.id.search_panel);

        prev = searchLayout.findViewById(R.id.btn_goto_prev);
        next = searchLayout.findViewById(R.id.btn_goto_next);
        replace = searchLayout.findViewById(R.id.btn_replace);
        replaceAll = searchLayout.findViewById(R.id.btn_replace_all);
        query = searchLayout.findViewById(R.id.search_editor);
        replacement = searchLayout.findViewById(R.id.replace_editor);
        searchMenu = searchLayout.findViewById(R.id.search_options);

        EdgeToEdge.enable(this);

        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(EditorActivity.this, binding.drawer, binding.toolbar, R.string.app_name, R.string.app_name) {

            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
                hideKeyboard(EditorActivity.this);

            }
        };

        binding.drawer.addDrawerListener(toggle);
        toggle.syncState();

        Typeface font = createFromAsset(getAssets(), "font/mono.ttf");

        symbols.setBackgroundColor(Color.TRANSPARENT);
        symbols.bindEditor(editor);
        symbols.addSymbols(SYMBOLS, SYMBOL_INSERT_TEXT);

        editorSettings = EditorSettings.getInstance(getApplicationContext());
        buildAndRunSettings = BuildAndRunSettings.getInstance(getApplicationContext());

        LogViewModel model = new ViewModelProvider(this).get(LogViewModel.class);
        logAdapter = new LogAdapter();
        compiler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        compiler.setAdapter(logAdapter);

        model.getLogs().observe(this, data -> {
            logAdapter.submitList(data);
            scrollToLastItem();
        });

        logger = new Logger();
        logger.attach(this);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
                    binding.drawer.closeDrawer(GravityCompat.START);
                } else {
                    CustomMaterialDialog exit = new CustomMaterialDialog(EditorActivity.this);
                    exit.setTitle(R.string.exit_title);
                    exit.setMessage(R.string.exit_message);
                    exit.setNegativeButton(R.string.exit_negative, (_dialog, _which) -> {
                    });
                    exit.setPositiveButton(R.string.exit_positive, (_dialog, _which) -> {
                        if (compilerTask != null && compilerTask.isRunning()) compilerTask.cancel();
                        tryToSave(false);
                        finish();
                    });
                    exit.show();
                }

            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);

        ProjectController projectController = new ProjectController(this);

        projectName = getIntent().getStringExtra("projectName");
        Map<String, String> projectInfo = projectController.getProject(projectName);
        String configFilePath = projectInfo.get("configPath");
        projectPath = projectInfo.get("projectPath");

        try {
            config = readConfigFile(configFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        treeViewUpdate();
        setupTabLayoutListeners();

        editor.setTypefaceText(font);
        editor.setTypefaceLineNumber(font);
        editor.setEdgeEffectColor(Color.TRANSPARENT);
        editor.subscribeAlways(ContentChangeEvent.class, (event) -> {
            updateDoIcon(menu);
            tryToSave(false);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        treeViewUpdate();
        reloadEditorParams();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        tryToSave(false);
        editor.release();

        if (compilerTask != null && compilerTask.isRunning()) {
            compilerTask.cancel();
            updateRunIcon(menu, true);
        }

    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.popup_menu_2, menu);

        this.menu = menu;
        MenuBuilder menuBuilder = (MenuBuilder) menu;
        menuBuilder.setOptionalIconsVisible(true);

        updateFormatIcon(menu, false);
        updateDoIcon(menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final String _title = Objects.requireNonNull(item.getTitle()).toString();

        if (_title.equals(getApplicationContext().getString(R.string.action_reundo))) {

            if (editor.canUndo()) {
                editor.undo();
            } else if (editor.canRedo()) {
                editor.redo();
            }

            updateDoIcon(menu);

        }

        if (_title.equals(getApplicationContext().getString(R.string.action_run))) {
            getUiReady();
            compile();
        }

        if (_title.equals(getApplicationContext().getString(R.string.action_save))) {
            tryToSave(true);
        }

        if (_title.equals(getApplicationContext().getString(R.string.settings))) {
            Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(settings);
        }

        if (_title.equals(getApplicationContext().getString(R.string.menu_libs))) {

            if (getSupportFragmentManager().findFragmentByTag("library_downloader_dialog") != null) {
                return true;
            }

            LibraryDownloaderDialogFragment fragment = new LibraryDownloaderDialogFragment();
            fragment.setLibraryDownloaderListener(EditorActivity.this);
            fragment.show(getSupportFragmentManager(), "library_downloader_dialog");
            return true;

        }

        if (_title.equals(getApplicationContext().getString(R.string.menu_search))) {
            item.setChecked(!item.isChecked());

            if (item.isChecked()) {
                if (searchLayout != null) {
                    searchLayout.setVisibility(View.VISIBLE);
                    hideKeyboard(EditorActivity.this);
                }

                if (searchMenu != null) {
                    searchMenu.setOnClickListener(v -> {
                        PopupMenu popup = new PopupMenu(v.getContext(), v);
                        popup.getMenuInflater().inflate(R.menu.popup_menu_3, popup.getMenu());

                        popup.getMenu().findItem(R.id.case_sensitive).setChecked(caseSensitive);
                        popup.getMenu().findItem(R.id.use_regex).setChecked(useRegex);

                        popup.setOnMenuItemClickListener(item1 -> {
                            String title = Objects.requireNonNull(item1.getTitle()).toString();

                            if (title.equals(v.getContext().getString(R.string.case_sensitive))) {
                                caseSensitive = !item1.isChecked();
                                item1.setChecked(caseSensitive);
                                query.setText("");
                                editor.getSearcher().stopSearch();
                                return true;
                            } else if (title.equals(v.getContext().getString(R.string.use_regex))) {
                                useRegex = !item1.isChecked();
                                item1.setChecked(useRegex);
                                query.setText("");
                                editor.getSearcher().stopSearch();
                                return true;
                            }
                            return false; // Return false for unhandled items
                        });

                        popup.show();
                    });
                }

                if (query != null) {
                    query.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            String queryText = Objects.requireNonNull(query.getText()).toString();
                            if (!queryText.isEmpty()) {
                                EditorSearcher.SearchOptions searchOptions = new EditorSearcher.SearchOptions(!caseSensitive, !useRegex);
                                editor.getSearcher().search(queryText, searchOptions);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    });
                }

                if (prev != null) {
                    prev.setOnClickListener(v -> {
                        String queryText = Objects.requireNonNull(query.getText()).toString();
                        if (!queryText.isEmpty()) {
                            editor.getSearcher().gotoPrevious();
                        }
                    });
                }

                if (next != null) {
                    next.setOnClickListener(v -> {
                        String queryText = Objects.requireNonNull(query.getText()).toString();
                        if (!queryText.isEmpty()) {
                            editor.getSearcher().gotoNext();
                        }
                    });
                }

                if (replace != null) {
                    replace.setOnClickListener(v -> {
                        String replacementText = Objects.requireNonNull(replacement.getText()).toString();
                        if (!replacementText.isEmpty()) {
                            editor.getSearcher().replaceCurrentMatch(replacementText);
                        }
                    });
                }

                if (replaceAll != null) {
                    replaceAll.setOnClickListener(v -> {
                        String replacementText = Objects.requireNonNull(replacement.getText()).toString();
                        if (!replacementText.isEmpty()) {
                            editor.getSearcher().replaceAll(replacementText);
                        }
                    });
                }


            } else {
                Log.d("MenuSearch", "Search menu item is unchecked");
                if (searchLayout != null) {
                    searchLayout.setVisibility(View.GONE);
                }
            }
        }

        if (_title.equals(getApplicationContext().getString(R.string.menu_format_code))) {

            try {
                editor.setText(CodePrettifier.getFormattedCode(getApplicationContext(), java.nio.file.Paths.get(openedFiles.get(selectedTabIndex).filePath)));
            } catch (Exception e) {
                Snackbar.make(binding.getRoot(), Objects.requireNonNull(e.getLocalizedMessage()), Snackbar.LENGTH_LONG).setAnchorView(symbols).show();
            }

        }

        return super.onOptionsItemSelected(item);
    }

    public void reloadEditorParams() {
        editor.setTabWidth(editorSettings.getTabSize());
        editor.setLigatureEnabled(editorSettings.isFontLigaturesEnabled());
        editor.setHorizontalScrollBarEnabled(editorSettings.isScrollBarEnabled());
        editor.setVerticalScrollBarEnabled(editorSettings.isScrollBarEnabled());
        editor.setWordwrap(editorSettings.isWordWrapEnabled());
        editor.setNonPrintablePaintingFlags(getNonPrintablePaintingFlags(Objects.requireNonNull(editorSettings.selectedSymbols)));
        editor.getProps().stickyScroll = editorSettings.isStickyScrollEnabled();
    }

    private int getNonPrintablePaintingFlags(Set<String> selectedSymbols) {
        int flags = 0;
        if (selectedSymbols.contains("leading")) {
            flags |= CodeEditor.FLAG_DRAW_WHITESPACE_LEADING;
        }
        if (selectedSymbols.contains("trailing")) {
            flags |= CodeEditor.FLAG_DRAW_WHITESPACE_TRAILING;
        }
        if (selectedSymbols.contains("inner")) {
            flags |= CodeEditor.FLAG_DRAW_WHITESPACE_INNER;
        }
        if (selectedSymbols.contains("empty_lines")) {
            flags |= CodeEditor.FLAG_DRAW_WHITESPACE_FOR_EMPTY_LINE;
        }
        if (selectedSymbols.contains("line_breaks")) {
            flags |= CodeEditor.FLAG_DRAW_LINE_SEPARATOR;
        }
        return flags;
    }

    private void scrollToLastItem() {
        int itemCount = logAdapter.getItemCount();
        if (itemCount > 0) {
            compiler.scrollToPosition(itemCount - 1);
        }
    }

    public void openFile(String filePath) throws IOException {
        if (openedFiles.stream().noneMatch(data -> data.filePath.equals(filePath))) {
            openedFiles.add(new TabData(filePath));
            updateTabs();
            updateFormatIcon(menu, true);
        } else {
            int index = -1;
            for (int i = 0; i < openedFiles.size(); i++) {
                if (openedFiles.get(i).filePath.equals(filePath)) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                Objects.requireNonNull(binding.tabs.getTabAt(index)).select();
            }
        }
    }

    public void closeFile(CloseFileMode closeFileMode) {
        switch (closeFileMode) {
            case CLOSE_SELECTED_ONLY:
                if (selectedTabIndex >= 0) {
                    openedFiles.remove(selectedTabIndex);
                }
                selectedTabIndex = Math.max(selectedTabIndex - 1, -1);
                break;
            case CLOSE_ALL_EXCEPT_CURRENT:
                if (selectedTabIndex >= 0) {
                    openedFiles.removeIf(data -> data != openedFiles.get(selectedTabIndex));
                } else {
                    openedFiles.clear();
                }
                break;
            case CLOSE_ALL:
                openedFiles.clear();
                selectedTabIndex = -1;
                break;
        }
        updateTabs();

    }

    private void updateTabs() {
        binding.tabs.removeAllTabs();
        for (TabData data : openedFiles) {
            String fileName = Paths.get(data.filePath).getFileName().toString();
            TabLayout.Tab tab = binding.tabs.newTab().setText(fileName);
            tab.setTag(data);
            binding.tabs.addTab(tab);
            if (!tab.isSelected()) {
                binding.tabs.selectTab(tab);
                binding.tabs.post(() -> {
                    int selectedTabPosition = tab.getPosition();
                    binding.tabs.scrollTo(selectedTabPosition * tab.view.getWidth(), 0);
                });
            }
        }
        updateEditorContent();
    }

    private void updateEditorContent() {
        String content = selectedTabIndex >= 0 ? FilesKt.readText(Paths.get(openedFiles.get(selectedTabIndex).filePath).toFile(), StandardCharsets.UTF_8) : "";
        editor.setText(content);
    }

    private void setupTabLayoutListeners() {
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTabIndex = tab.getPosition();

                try {

                    String tabName = Paths.get(openedFiles.get(selectedTabIndex).filePath).getFileName().toString();
                    String extension = tabName.substring(tabName.lastIndexOf(".") + 1);

                    binding.tabs.setVisibility(VISIBLE);
                    compiler.setVisibility(GONE);

                    try {
                        configureEditor(extension);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    updateEditorContent();

                    TabData data = (TabData) Objects.requireNonNull(tab.getTag());

                    if (data.cursorLine != null && data.cursorColumn != null) {
                        editor.setSelection(data.cursorLine, data.cursorColumn);
                    } else {
                        editor.setSelection(0, 0);
                    }

                    editor.setTextSizePx(data.textSizePx);

                } catch (Exception e) {
                    Snackbar.make(binding.getRoot(), Objects.requireNonNull(e.getLocalizedMessage()), Snackbar.LENGTH_LONG).setAnchorView(symbols).show();
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TabData data = (TabData) Objects.requireNonNull(tab.getTag());
                int cursorLine = editor.getCursor().getLeftLine();
                int cursorColumn = editor.getCursor().getLeftColumn();
                data.cursorLine = cursorLine;
                data.cursorColumn = cursorColumn;
                data.textSizePx = editor.getTextSizePx();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                binding.tabs.setVisibility(VISIBLE);
                PopupMenu popupMenu = new PopupMenu(EditorActivity.this, binding.tabs);
                popupMenu.inflate(R.menu.context_menu);
                popupMenu.setOnMenuItemClickListener(item -> {
                    final String _title = (String) item.getTitle();
                    if (String.valueOf(_title).equals(getApplicationContext().getString(R.string.action_close))) {
                        closeFile(CloseFileMode.CLOSE_SELECTED_ONLY);
                        if (selectedTabIndex == -1) {
                            if (editor.getVisibility() == VISIBLE && symbols.getVisibility() == VISIBLE && binding.tabs.getVisibility() == VISIBLE) {
                                editor.setVisibility(GONE);
                                symbols.setVisibility(GONE);
                                binding.tabs.setVisibility(GONE);
                                updateFormatIcon(menu, false);
                            }
                        }
                    }
                    if (String.valueOf(_title).equals(getApplicationContext().getString(R.string.action_close_others))) {
                        closeFile(CloseFileMode.CLOSE_ALL_EXCEPT_CURRENT);
                    }
                    if (String.valueOf(_title).equals(getApplicationContext().getString(R.string.action_close_all))) {
                        closeFile(CloseFileMode.CLOSE_ALL);
                        if (selectedTabIndex == -1) {
                            if (editor.getVisibility() == VISIBLE && symbols.getVisibility() == VISIBLE) {
                                editor.setVisibility(GONE);
                                symbols.setVisibility(GONE);
                                binding.tabs.setVisibility(GONE);
                                updateFormatIcon(menu, false);
                            }
                        }
                    }
                    return true;
                });
                popupMenu.show();
            }
        });
    }

    @Override
    public void onDownloadRequested(String dependency, boolean skipSubDependencies) {
        getDownloaderReady();
        clearLogs();
        resolveDependencies(dependency, skipSubDependencies);
    }

    private void getDownloaderReady() {

        storageFactory = new LocalStorageFactory();

        storageFactory.setCacheDirectory(getApplicationContext().getExternalFilesDir("cache"));

        libraryManager = new LocalLibraryManager(new File(Objects.requireNonNull(config.get("librariesPath"))));

        libraryManager.setCompileResourcesClassPath(getAndroidJarFile(), getLambdaFactoryFile());

        getUiReady();

    }

    private void resolveDependencies(String dep, boolean skipInnerDependencies) {
        try {
            coordinates = Coordinates.valueOf(dep);
        } catch (Exception e) {
            logger.e("ERROR", e.getMessage());
        }
        resolver = new DependencyResolver(storageFactory, coordinates);
        storageFactory.attach(resolver);

        configureRepositories(resolver, logger);

        resolver.skipInnerDependencies(skipInnerDependencies);


        runOnUiThread(() -> {
            resolver.resolve(new DependencyResolutionCallback() {
                @Override
                public void onDependenciesResolved(String message, List<Dependency> resolvedDependencies, long totalTime) {
                    int resolutionSize = resolvedDependencies.size();
                    // additional info
                    StringBuilder sb = new StringBuilder();
                    sb.append("[");
                    resolvedDependencies.forEach(dependency -> {
                        String artifact = dependency.toString();
                        sb.append("\n");
                        sb.append("> ");
                        sb.append(artifact);
                    });
                    sb.append("]");
                    logger.p("Resolved Dependencies", sb.toString());

                    double totalTimeSeconds = totalTime / 1000.0;
                    String pluralized = (resolutionSize == 1) ? " dependency" : " dependencies";
                    Locale userLocale = Locale.getDefault();
                    String sbt = "\n" + "Resolved dependencies in " + String.format(userLocale, "%.3f", totalTimeSeconds) + "s" + "\n" + "Successfully resolved " + resolutionSize + pluralized;
                    logger.p("SUMMARY", sbt);
                    // add resolved dependencies to pom for download initialization
                    Pom resolvedPom = new Pom();
                    resolvedPom.setDependencies(resolvedDependencies);
                    storageFactory.downloadLibraries(resolvedPom);
                }

                @Override
                public void onDependencyNotResolved(String message, List<Dependency> unresolvedDependencies) {
                    // TODO: Handle
                }

                @Override
                public void info(String message) {
                    logger.p("INFO", message);
                }

                @Override
                public void verbose(String message) {
                    logger.d("VERBOSE", message);
                }

                @Override
                public void error(String message) {
                    logger.e("ERROR", message);
                }

                @Override
                public void warning(String message) {
                    logger.w("WARNING", message);
                }
            });

            storageFactory.setDownloadCallback(new DownloadCallback() {
                @Override
                public void info(String message) {
                    logger.p("INFO", message);
                }

                @Override
                public void error(String message) {
                    logger.e("ERROR", message);
                }

                @Override
                public void warning(String message) {
                    logger.w("WARNING", message);
                }

                @Override
                public void done(List<CachedLibrary> cachedLibraryList) {
                    libraryManager.copyCachedLibrary(cachedLibraryList);
                    treeViewUpdate();
                }
            });

            libraryManager.setTaskListener(new LocalLibraryManager.TaskListener() {
                @Override
                public void info(String message) {
                    logger.p("INFO", message);

                }

                @Override
                public void error(String message) {
                    logger.e("ERROR", message);
                }
            });
        });
    }

    private void configureRepositories(DependencyResolver resolver, Logger logger) {

        boolean useDefault = false;

        if (BaseUtil.Path.REPOSITORIES_JSON.exists()) {
            try {
                List<RemoteRepository> repositories = Repository.Manager.readRemoteRepositoryConfig(BaseUtil.Path.REPOSITORIES_JSON, false);
                for (RemoteRepository repository : repositories) {
                    resolver.addRepository(repository);
                }
            } catch (IOException | JSONException ignored) {
                useDefault = true;
            }
        } else {
            useDefault = true;
        }

        if (useDefault) {
            for (RemoteRepository repository : Repository.Manager.DEFAULT_REMOTE_REPOSITORIES) {
                resolver.addRepository(repository);
            }
            logger.d("INFO", "Custom Repositories configuration file couldn't be read from. Using default repositories for now");
            try {
                // write default to file
                FileUtils.write(BaseUtil.Path.REPOSITORIES_JSON, Repository.Manager.generateJSON(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                logger.e("ERROR", "Failed to create " + BaseUtil.Path.REPOSITORIES_JSON.getName() + e.getMessage());
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void clearLogs() {
        if (logger != null) {
            logger.clear();
            logAdapter.notifyDataSetChanged();
        }
    }

    private void tryToSave(boolean notify) {
        try {
            if (editor != null) {
                saveContent(openedFiles.get(selectedTabIndex).filePath, editor.getText().toString(), notify);
            }
        } catch (Exception e) {
            Log.d(getString(R.string.some_error), e.toString());
        }
    }

    private void saveContent(String filePath, String text, boolean notify) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(text);

            if (notify) {
                hideKeyboard(EditorActivity.this);
                Snackbar s = Snackbar.make(editor, R.string.message_saved, Snackbar.LENGTH_SHORT);
                if (symbols.getVisibility() == VISIBLE) s.setAnchorView(symbols);
                s.show();
            }

        } catch (Exception e) {
            Log.d(getString(R.string.some_error), e.toString());
        }
    }

    public void treeViewUpdate() {
        if (!TextUtils.isEmpty(projectPath)) {
            loadFilesToTree(projectPath, binding.drawer.findViewById(R.id.recyclerview1), projectName);
            setTitle(projectName);
            Log.d("dbg treeview", projectPath);
        }

    }

    public void loadFilesToTree(String path, final RecyclerView recycler, String rootFolderName) {

        TreeViewList.isPath = true;
        List<TreeViewList.TreeNode> nodes2 = new ArrayList<>();
        TreeViewList.TreeNode<TreeViewList.Dir> node = new TreeViewList.TreeNode<>(new TreeViewList.Dir(rootFolderName));
        nodes2.add(node);
        initData(path, node);
        recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        TreeViewList.TreeViewAdapter adapter = new TreeViewList.TreeViewAdapter(nodes2, Arrays.asList(new TreeViewList.FileNodeBinder(), new TreeViewList.DirectoryNodeBinder()));
        //adapter.ifCollapseChildWhileCollapseParent(true);
        adapter.setOnTreeNodeListener(new TreeViewList.TreeViewAdapter.OnTreeNodeListener() {

            @Override
            public boolean onClick(String clickedPath, TreeViewList.TreeNode node, RecyclerView.ViewHolder holder) {
                if (!node.isLeaf()) {
                    onToggle(!node.isExpand(), holder);
                }
                java.nio.file.Path p = Paths.get(clickedPath);
                if (Files.isRegularFile(p)) {
                    fileClicked(clickedPath);

                } else {
                    Files.isDirectory(p);
                }
                return false;
            }

            @Override
            public void onToggle(boolean isExpand, RecyclerView.ViewHolder holder) {
                TreeViewList.DirectoryNodeBinder.ViewHolder dirViewHolder = (TreeViewList.DirectoryNodeBinder.ViewHolder) holder;
                final ImageView ivArrow = dirViewHolder.getIvArrow();
                int rotateDegree = isExpand ? 90 : -90;
                ivArrow.animate().rotationBy(rotateDegree).start();
            }


            @Override
            public void onLongClick(String clickedPath) {
                Path clicked = Paths.get(clickedPath);

                if (Files.isRegularFile(clicked)) {

                    longClickFile(clickedPath);

                } else {
                    if (Files.isDirectory(clicked)) {

                        longClickFolder(clickedPath);
                    }
                }

            }

        });
        recycler.setAdapter(adapter);

        Button collapse = binding.nav.findViewById(R.id.collapse);
        if (collapse != null) collapse.setOnClickListener(v -> treeViewUpdate());

    }

    private void fileClicked(String clickedPath) {
        String extension = getFileExtension(clickedPath);

        if ("apk".equals(extension)) {
            installApkFile(clickedPath);
        } else if (isImageExtension(extension)) {
            previewImage(clickedPath);
        } else if (!isIgnoredExtension(extension)) {
            try {
                openFile(clickedPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        hideKeyboard(EditorActivity.this);
        binding.drawer.closeDrawer(GravityCompat.START);
    }

    private boolean isImageExtension(String extension) {
        return extension.matches("jpg|jpeg|png|gif|bmp|webp|heic|heif");
    }

    private boolean isIgnoredExtension(String extension) {
        return extension.matches("dex|class|zip|res|aidl|jar|idsig|so");
    }

    private String getFileExtension(String filePath) {
        return filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
    }

    private void previewImage(String imagePath) {
        File imageFile = new File(imagePath);
        Uri imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", imageFile);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(imageUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void longClickFile(String clickedPath) {
        if (!clickedPath.contains("compiler_config.json")) options("file", clickedPath);
    }

    private void longClickFolder(String clickedPath) {
        options("folder", clickedPath);
    }

    public void configureEditor(@NonNull final String ext) {
        String currentExtension = getCurrentEditorExtension();

        if (!Objects.equals(currentExtension, ext)) {

            if (editor.getVisibility() == GONE || symbols.getVisibility() == GONE) {
                editor.setVisibility(VISIBLE);
                symbols.setVisibility(VISIBLE);
            }

            if (compiler.getVisibility() == VISIBLE) {
                compiler.setVisibility(GONE);
            }

            setCurrentEditorExtension(ext);

            switch (ext) {
                case "java" -> {
                    loadJavaConfig(editor);
                    return;
                }
                case "gradle" -> loadLang("source", "groovy");
                case "html" -> loadLang("text.html", "basic");
                case "xml" -> loadLang("text", "xml");
                case "md" -> loadLang("text.html", "markdown");
                default -> {
                    try {
                        loadLang("source", ext);
                    } catch (Exception e) {
                        Log.d(getString(R.string.some_error), getString(R.string.file_extension_error));
                        editor.setVisibility(GONE);
                        symbols.setVisibility(GONE);
                    }
                }
            }

            try {
                applyTheme();
            } catch (Exception e) {
                Log.w("tmTheme", e.toString());
            }

            reloadEditorParams();

        }

    }

    private String getCurrentEditorExtension() {
        return currentEditorExtension;
    }

    private void setCurrentEditorExtension(String ext) {
        currentEditorExtension = ext;
    }

    public void loadLang(String scope, String lang) {
        FileProviderRegistry fileProviderRegistry = FileProviderRegistry.getInstance();
        fileProviderRegistry.addFileProvider(new AssetsFileResolver(getAssets()));

        GrammarRegistry.getInstance().loadGrammars("textmate/lang/languages.json");
        TextMateLanguage language = TextMateLanguage.create(scope + "." + lang, true);
        editor.setEditorLanguage(language);

    }

    private void applyTheme() throws Exception {
        File themeFile = generate(binding.getRoot(), "S-IDE Material3");
        String themeAssetsPath = themeFile.getAbsolutePath();
        FileProviderRegistry fileProviderRegistry = FileProviderRegistry.getInstance();
        fileProviderRegistry.addFileProvider(path -> null);
        ThemeRegistry themeRegistry = ThemeRegistry.getInstance();
        InputStream themeStream = fileProviderRegistry.tryGetInputStream(themeAssetsPath);
        ThemeModel model = createDefaultTheme(themeStream, themeAssetsPath);
        themeRegistry.loadTheme(model);
        themeRegistry.setTheme(model.getName());
        editor.setColorScheme(TextMateColorScheme.create(themeRegistry));
    }

    private ThemeModel createDefaultTheme(InputStream themeStream, String themeAssetsPath) {
        ThemeModel model = new ThemeModel(IThemeSource.fromInputStream(themeStream, themeAssetsPath, null), "my_dynamic_theme");
        model.setDark(isDarkThemeEnabled());
        return model;
    }

    public void getUiReady() {

        hideKeyboard(EditorActivity.this);

        if (editor.getVisibility() == VISIBLE && symbols.getVisibility() == VISIBLE && binding.tabs.getVisibility() == VISIBLE) {
            tryToSave(true);
            editor.setVisibility(GONE);
            symbols.setVisibility(GONE);
            binding.tabs.setVisibility(GONE);
        }
        compiler.setVisibility(VISIBLE);

        closeFile(CloseFileMode.CLOSE_ALL); //todo: use just separate tab instead of closing all

        treeViewUpdate();
        clearLogs();

    }

    public void updateRunIcon(Menu menu, boolean shouldShowRun) {
        if (menu != null) {
            MenuItem actionRunItem = menu.findItem(R.id.action_run);
            if (actionRunItem != null) {
                if (shouldShowRun) {
                    actionRunItem.setIcon(R.drawable.ic_run);
                } else {
                    actionRunItem.setIcon(R.drawable.ic_stop);
                }
            }
        }
    }

    public void updateDoIcon(Menu menu) {
        if (menu != null) {

            MenuItem actionDoItem = menu.findItem(R.id.action_do);

            if (actionDoItem != null) {

                actionDoItem.setVisible(true);

                if (editor.canRedo()) {
                    actionDoItem.setIcon(R.drawable.ic_redo);

                } else if (editor.canUndo()) {
                    actionDoItem.setIcon(R.drawable.ic_undo);

                } else {
                    actionDoItem.setVisible(false);
                }
            }


        }
    }

    public void updateFormatIcon(Menu menu, boolean shouldShowIcon) {
        if (menu != null) {
            MenuItem actionFormatItem = menu.findItem(R.id.menu_format);

            if (actionFormatItem != null) {
                actionFormatItem.setVisible(shouldShowIcon);
            }

        }
    }

    public void compile() {

        if (compilerTask != null && compilerTask.isRunning()) {
            compilerTask.cancel();
            updateRunIcon(menu, true);
            return;
        }

        updateRunIcon(menu, false);
        updateFormatIcon(menu, false);

        try {

            Project project = createProjectFromConfig(config);

            LogPrinter.start(logger);
            project.setLogger(logger);

            compilerTask = new CompilerTask(EditorActivity.this, project);
            String apkPath = config.get("outputPath") + "/bin/signed.apk";

            compilerTask.setOnSuccessListener(() -> runOnUiThread(() -> {
                treeViewUpdate();
                updateRunIcon(menu, true);
                if (buildAndRunSettings.isAutoInstallEnabled()) {
                    installApkFile(apkPath);
                }
            }));

            compilerTask.setOnErrorListener(() -> runOnUiThread(() -> updateRunIcon(menu, true)));

            Thread compilerThread = new Thread(compilerTask);
            compilerThread.start();

        } catch (Exception e) {
            Log.d(getString(R.string.some_error), e.toString());
        }

    }

    public Map<String, String> readConfigFile(String configFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(new File(configFilePath));

        Map<String, String> config = new HashMap<>();
        rootNode.fieldNames().forEachRemaining(fieldName -> config.put(fieldName, rootNode.get(fieldName).asText()));

        return config;
    }

    private Project createProjectFromConfig(Map<String, String> config) {
        Project project = new Project();
        project.setLibraries(Library.fromFile(new File(Objects.requireNonNull(config.get("librariesPath")))));
        project.setOutputFile(new File(Objects.requireNonNull(config.get("outputPath"))));
        project.setManifestFile(new File(Objects.requireNonNull(config.get("manifestPath"))));
        project.setResourcesFile(new File(Objects.requireNonNull(config.get("resourcesPath"))));
        project.setJavaFile(new File(Objects.requireNonNull(config.get("javaPath"))));

        if (config.containsKey("assetsPath")) {
            File assetsFile = new File(Objects.requireNonNull(config.get("assetsPath")));
            if (!assetsFile.exists()) assetsFile.mkdirs();
            project.setAssetsFile(assetsFile);
        }

        project.setMinSdk(Integer.parseInt(Objects.requireNonNull(config.get("minSdkVersion"))));
        project.setTargetSdk(Integer.parseInt(Objects.requireNonNull(config.get("targetSdkVersion"))));
        project.setVersionCode(Integer.parseInt(Objects.requireNonNull(config.get("versionCode"))));
        project.setVersionName(config.get("versionName"));
        return project;
    }

    private void installApkFile(String apkFilePath) {
        try {
            Uri apkUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", new File(apkFilePath));

            PackageInstaller packageInstaller = getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            int sessionId = packageInstaller.createSession(params);

            PackageInstaller.Session session = packageInstaller.openSession(sessionId);
            OutputStream out = session.openWrite("my_app_session", 0, -1);

            FileInputStream in = new FileInputStream(apkFilePath);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            session.fsync(out);
            out.close();
            in.close();

            // Use ACTION_VIEW instead of ACTION_INSTALL_PACKAGE
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installIntent.setData(apkUri);
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            installIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Optional: Clear top activity

            startActivity(installIntent);

        } catch (Exception e) {
            Log.d(getString(R.string.some_error), e.toString());
        }
    }

    public void initData(String path, final TreeViewList.TreeNode<TreeViewList.Dir> dir) {

        final String[] pathStr = {path};

        new Thread(() -> {
            Looper.prepare();

            ArrayList<String> rootDir = new ArrayList<>();

            listDir(pathStr[0], rootDir);

            for (String one : rootDir) {
                Path path1 = Paths.get(one);
                if (Files.isRegularFile(path1)) {
                    dir.addChild(new TreeViewList.TreeNode<>(new TreeViewList.File(one)));
                } else if (Files.isDirectory(path1)) {
                    TreeViewList.TreeNode<TreeViewList.Dir> dirTree = new TreeViewList.TreeNode<>(new TreeViewList.Dir(one));
                    dir.addChild(dirTree);
                    initData(one, dirTree);
                }
            }

        }).start();
    }

    private void showRenameDialog(String str) {
        CustomMaterialDialog renameDialog = new CustomMaterialDialog(EditorActivity.this);
        renameDialog.setTitle(R.string.rename_dialog_title);
        renameDialog.setInputEditTextHint(R.string.hint_edittext_choose);
        renameDialog.setPositiveButton(R.string.action_rename, (_dialog, _which) -> {

            String newName = Objects.requireNonNull(renameDialog.getTextInputEditText().getText()).toString();
            renameContent(newName, str);

        });
        renameDialog.setNegativeButton(R.string.action_negative, (_dialog, _which) -> {
        });
        renameDialog.show();
    }

    private void showDeleteDialog(File f) {

        CustomMaterialDialog deleteDialog = new CustomMaterialDialog(EditorActivity.this);
        deleteDialog.setTitle(R.string.confirm_action);
        deleteDialog.setMessage(R.string.confirm_delete);
        deleteDialog.setPositiveButton(R.string.action_delete, (_dialog, _which) -> {

            deleteContent(f, true);
            treeViewUpdate();

        });
        deleteDialog.setNegativeButton(R.string.action_negative, (_dialog, _which) -> {
        });
        deleteDialog.show();

    }

    private void showMakeSmthDialog(String str, boolean config) {

        CustomMaterialDialog makeSmthDialog = new CustomMaterialDialog(EditorActivity.this);
        makeSmthDialog.setInputEditTextHint(R.string.hint_edittext_choose);
        makeSmthDialog.setPositiveButton(R.string.action_create, (_dialog, _which) -> {

            String newName = Objects.requireNonNull(makeSmthDialog.getTextInputEditText().getText()).toString();
            makeContent(newName, str, config);

        });
        makeSmthDialog.setNegativeButton(R.string.action_negative, (_dialog, _which) -> {
        });
        makeSmthDialog.show();
    }

    public void options(final String opt, final String path) {
        if (String.valueOf(opt).equals("file")) {
            File file = new File(path);
            BottomListMenu blm = new BottomListMenu();
            blm.addItem(0, getString(R.string.action_rename), AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_edit));
            blm.addItem(1, getString(R.string.action_remove), AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_delete));
            blm.addItem(2, getString(R.string.action_copy_path), AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_copy));
            blm.setOnMenuItemClickListener(position -> {
                switch (position) {
                    case 0:
                        showRenameDialog(path);
                        break;
                    case 1:
                        showDeleteDialog(file);
                        break;
                    case 2:
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText(getString(R.string.app_name), path);
                        clipboardManager.setPrimaryClip(clipData);
                        Toast.makeText(getApplicationContext(), getString(R.string.message_copied), Toast.LENGTH_SHORT).show();
                        break;
                }
            });
            blm.show(EditorActivity.this);
        } else if (String.valueOf(opt).equals("folder")) {
            File file = new File(path);
            BottomListMenu blm = new BottomListMenu();
            blm.addItem(0, getString(R.string.action_rename), AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_edit));
            blm.addItem(1, getString(R.string.action_remove), AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_delete));
            blm.addItem(2, getString(R.string.action_create_file), AppCompatResources.getDrawable(getApplicationContext(), s.ide.ui.R.drawable.ic_file));
            blm.addItem(3, getString(R.string.action_create_folder), AppCompatResources.getDrawable(getApplicationContext(), s.ide.ui.R.drawable.ic_folder));
            blm.addItem(4, getString(R.string.action_copy_path), AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_copy));
            blm.addItem(5, getString(R.string.action_import_file), AppCompatResources.getDrawable(getApplicationContext(), R.drawable.ic_import));
            blm.setOnMenuItemClickListener(position -> {
                switch (position) {
                    case 0:
                        showRenameDialog(path);
                        break;
                    case 1:
                        showDeleteDialog(file);
                        break;
                    case 2:
                        showMakeSmthDialog(path, false);
                        break;
                    case 3:
                        showMakeSmthDialog(path, true);
                        break;
                    case 4:
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText(getString(R.string.app_name), path);
                        clipboardManager.setPrimaryClip(clipData);
                        Toast.makeText(getApplicationContext(), getString(R.string.message_copied), Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        folderPath = path;
                        importFileToPath();
                        break;
                }
            });
            blm.show(EditorActivity.this);
        }
    }

    private void importFileToPath() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        importFileLauncher.launch(Intent.createChooser(intent, getString(R.string.action_import_file)));
    }

    public void hideKeyboard(Activity activity) {
        if (activity == null || activity.getCurrentFocus() == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void renameContent(String newName, String path) {
        if (!TextUtils.isEmpty(newName)) {
            assert path != null;
            File file = new File(path);
            String oldName = file.getName();
            int dotIndex = oldName.lastIndexOf(".");
            String extension = "";
            if (dotIndex != -1) {
                extension = oldName.substring(dotIndex);
            }
            if (!newName.contains(".") && !extension.isEmpty()) {
                newName += extension;
            }
            File file2 = new File(file.getParent(), newName);
            try {
                if (file2.exists()) {
                    binding.drawer.close();
                    Snackbar s = Snackbar.make(editor, R.string.message_already_exist, Snackbar.LENGTH_LONG);
                    if (symbols.getVisibility() == VISIBLE) s.setAnchorView(symbols);
                    s.show();
                } else {
                    boolean success = file.renameTo(file2);
                    binding.drawer.close();
                    Snackbar s;
                    if (success) {
                        s = Snackbar.make(editor, R.string.message_renamed, Snackbar.LENGTH_LONG);
                        if (symbols.getVisibility() == VISIBLE) s.setAnchorView(symbols);
                        s.show();
                        treeViewUpdate();
                    } else {
                        s = Snackbar.make(editor, R.string.some_error, Snackbar.LENGTH_LONG);
                        if (symbols.getVisibility() == VISIBLE) s.setAnchorView(symbols);
                        s.show();
                    }
                }
            } catch (Exception e) {
                Log.d(getString(R.string.some_error), e.toString());
            }
        }
    }

    public void deleteContent(File content, boolean withParent) {
        if (content.exists()) {
            File[] files = content.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteContent(file, withParent);
                        if (withParent) {
                            boolean deleted = file.delete();
                            if (!deleted) {
                                Log.w("DeleteContent", "Failed to delete directory: " + file.getAbsolutePath());
                            }
                        }
                    } else {
                        boolean deleted = file.delete();
                        if (!deleted) {
                            Log.w("DeleteContent", "Failed to delete file: " + file.getAbsolutePath());
                        }
                    }
                }
            }
            if (withParent) {
                boolean deleted = content.delete();
                if (!deleted) {
                    Log.w("DeleteContent", "Failed to delete content: " + content.getAbsolutePath());
                }
            }
        }
    }

    public void makeContent(String name, String path, boolean isDirectory) {
        File newContent = new File(path, name);
        if (isDirectory) {
            if (!newContent.exists()) {
                boolean created = newContent.mkdirs();
                if (created) {
                    treeViewUpdate();
                } else {
                    Log.w("MakeContent", "Failed to create directory: " + newContent.getAbsolutePath());
                }
            }
        } else {
            try {
                if (!newContent.exists()) {
                    boolean created = newContent.createNewFile();
                    if (created) {
                        treeViewUpdate();
                    } else {
                        Log.w("MakeContent", "Failed to create file: " + newContent.getAbsolutePath());
                    }
                }
            } catch (IOException e) {
                Log.d(getString(R.string.some_error), e.toString());
            }
        }
    }

    public float sp2px(float sp) {
        float density = getApplicationContext().getResources().getDisplayMetrics().scaledDensity;

        return sp * density;
    }

    public enum CloseFileMode {
        CLOSE_SELECTED_ONLY, CLOSE_ALL_EXCEPT_CURRENT, CLOSE_ALL
    }

    private class TabData {
        Float textSizePx;
        String filePath;
        Integer cursorLine;
        Integer cursorColumn;

        TabData(String filePath) {
            this.filePath = filePath;
            this.cursorLine = null;
            this.cursorColumn = null;
            this.textSizePx = sp2px(editorSettings.getFontSize());
        }
    }

}