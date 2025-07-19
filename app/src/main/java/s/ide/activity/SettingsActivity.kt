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

package s.ide.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import s.ide.R
import s.ide.databinding.ActivitySettingsBinding
import s.ide.ui.preference.MaterialListPreference
import s.ide.ui.preference.MaterialMultiSelectListPreference
import s.ide.utils.app.AppControl.restartActivity
import s.ide.utils.settings.BuildAndRunSettings
import s.ide.utils.settings.EditorSettings
import s.ide.utils.settings.ThemeSettings

@Suppress("UNCHECKED_CAST")
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editorSettings = EditorSettings.getInstance(applicationContext)
        buildAndRunSettings = BuildAndRunSettings.getInstance(applicationContext)
        themeSettings = ThemeSettings.getInstance(applicationContext)

        val binding = ActivitySettingsBinding.inflate(layoutInflater)

        this.enableEdgeToEdge()

        setContentView(binding.getRoot())

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.getRoot()
        ) { v: View?, insets: WindowInsetsCompat? ->
            val systemBars = insets!!.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.settings, AllSettingsFragment())
                .commit()
        }

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        val fragmentManager = supportFragmentManager

        if (fragmentManager.findFragmentById(R.id.settings) is AllSettingsFragment) {
            finish()
            return true
        }

        fragmentManager.popBackStack()
        return true
    }

    class AllSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.all_settings, rootKey)

            val buildAndRunPreference = findPreference<Preference?>("build_and_run")
            buildAndRunPreference?.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (savedInstanceState == null) {
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.settings, BuildAndRunSettingsFragment())
                            .addToBackStack(null).commit()
                    }
                    true
                }

            val editorPreference = findPreference<Preference?>("editor")
            editorPreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                if (savedInstanceState == null) {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.settings, EditorSettingsFragment()).addToBackStack(null)
                        .commit()
                }
                true
            }

            val themePreference = findPreference<Preference?>("theme")
            themePreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                if (themePreference is MaterialListPreference) {
                    themePreference.onPreferenceChangeListener =
                        Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any? ->
                            themeSettings.theme = (newValue as String).toInt()
                            Handler(Looper.getMainLooper()).postDelayed({
                                restartActivity()
                            }, 300)
                            true
                        }
                }
                true
            }
        }
    }

    class BuildAndRunSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.build_and_run_settings, rootKey)

            findPreference<SwitchPreferenceCompat>("autorun")?.apply {
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    buildAndRunSettings.isAutoInstallEnabled = newValue as Boolean
                    true
                }
            }

            findPreference<SwitchPreferenceCompat>("logs")?.apply {
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    buildAndRunSettings.isDetailedLogsEnabled = newValue as Boolean
                    true
                }
            }

            findPreference<SwitchPreferenceCompat>("desugar")?.apply {
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    buildAndRunSettings.isDesugarEnabled = newValue as Boolean
                    true
                }
            }
        }
    }

    class EditorSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.editor_settings, rootKey)

            findPreference<SeekBarPreference>("font_size")?.apply {
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    editorSettings.fontSize = newValue as Int
                    true
                }
            }

            findPreference<MaterialMultiSelectListPreference>("symbols")?.apply {
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    val selectedSymbols = newValue as Set<String>
                    editorSettings.selectedSymbols = selectedSymbols.toMutableSet()
                    true
                }
            }

            findPreference<MaterialListPreference>("tab_size")?.apply {
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    editorSettings.tabSize = (newValue as String).toInt()
                    true
                }
            }

            findPreference<SwitchPreferenceCompat>("sticky_scroll")?.apply {
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    editorSettings.isStickyScrollEnabled = newValue as Boolean
                    true
                }
            }

            findPreference<SwitchPreferenceCompat>("word_wrap")?.apply {
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    editorSettings.isWordWrapEnabled = newValue as Boolean
                    true
                }
            }

            findPreference<SwitchPreferenceCompat>("scroll_bar")?.apply {
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    editorSettings.isScrollBarEnabled = newValue as Boolean
                    true
                }
            }

            findPreference<SwitchPreferenceCompat>("font_ligatures")?.apply {
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    editorSettings.isFontLigaturesEnabled = newValue as Boolean
                    true
                }
            }

            findPreference<SwitchPreferenceCompat>("google_java_format_style")?.apply {
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    editorSettings.isGoogleJavaFormatStyle = newValue as Boolean
                    true
                }
            }

            findPreference<MaterialMultiSelectListPreference>("google_java_format_options")?.apply {
                onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                    val options = newValue as Set<String>
                    editorSettings.googleJavaFormatOptions = options.toMutableSet()
                    true
                }
            }
        }
    }

    companion object {
        private lateinit var editorSettings: EditorSettings
        private lateinit var buildAndRunSettings: BuildAndRunSettings
        private lateinit var themeSettings: ThemeSettings
    }
}

