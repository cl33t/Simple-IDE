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

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import s.ide.R
import s.ide.databinding.ActivityCreateProjectBinding
import s.ide.utils.projects.MakeProject
import s.ide.utils.projects.ProjectController
import java.io.IOException
import java.util.Objects
import java.util.regex.Pattern

class CreateProjectActivity : AppCompatActivity() {
    private lateinit var projectController: ProjectController
    private lateinit var binding: ActivityCreateProjectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateProjectBinding.inflate(layoutInflater)

        this.enableEdgeToEdge()

        setContentView(binding.getRoot())

        setSupportActionBar(binding.toolbar)

        binding.btn1.setOnClickListener { view: View? -> finish() }

        binding.btn2.setOnClickListener { view: View? ->
            if (this.isAnyFieldEmpty) {
                showFieldErrors()
            } else {
                createProject()
            }
        }

        setAdapters()
        setTitle(getString(R.string.create_project_activity_title))
        addTextWatchers()

        projectController = ProjectController(this)

    }

    private val isAnyFieldEmpty: Boolean
        get() = TextUtils.isEmpty(binding.text1.text) || TextUtils.isEmpty(binding.text2.text) || TextUtils.isEmpty(
            binding.text3.text
        ) || TextUtils.isEmpty(binding.text4.text)

    private fun showFieldErrors() {
        if (TextUtils.isEmpty(binding.text1.text)) {
            binding.textfield1.error = getString(R.string.field1_error)
        }
        if (TextUtils.isEmpty(binding.text2.text)) {
            binding.textfield2.error = getString(R.string.field2_error)
        }
        if (TextUtils.isEmpty(binding.text3.text)) {
            binding.textfield5.error = getString(R.string.field5_error)
        }
        if (TextUtils.isEmpty(binding.text4.text)) {
            binding.textfield6.error = getString(R.string.field6_error)
        }
    }

    private fun createProject() {
        val projectName = Objects.requireNonNull<Editable>(binding.text1.text).toString()
        val packageName = Objects.requireNonNull<Editable>(binding.text2.text).toString()
        val versionCode = Objects.requireNonNull<Editable>(binding.text3.text).toString()
        val versionName = Objects.requireNonNull<Editable>(binding.text4.text).toString()
        val minSdkVersion = extractApiVersion(binding.auto1.text.toString())
        val targetSdkVersion = extractApiVersion(binding.auto2.text.toString())

        try {
            MakeProject.createAndAddProject(
                applicationContext,
                projectName,
                versionCode,
                versionName,
                minSdkVersion,
                targetSdkVersion,
                packageName,
                projectController
            )
            val intent = Intent(this, EditorActivity::class.java)
            intent.putExtra("projectName", projectName)
            startActivity(intent)
            finish()
        } catch (e: IOException) {
            Snackbar.make(
                binding.root,
                getString(R.string.some_error) + ": " + e.message,
                Snackbar.LENGTH_LONG
            ).setAnchorView(binding.linear1).show()
            Log.e("CreateProjectActivity", "Error creating project", e)
        }
    }

    private fun setAdapters() {
        val stringArray = resources.getStringArray(R.array.sdk)
        val adapter: ArrayAdapter<String?> =
            object : ArrayAdapter<String?>(this, android.R.layout.simple_list_item_1, stringArray) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val textView = super.getView(position, convertView, parent) as TextView
                    textView.setTextColor(
                        MaterialColors.getColor(
                            binding.auto1, com.google.android.material.R.attr.colorOnSurface
                        )
                    )
                    return textView
                }
            }
        binding.auto1.setAdapter(adapter)
        binding.auto2.setAdapter(adapter)
    }

    private fun addTextWatchers() {
        addTextWatcher(binding.text1, binding.textfield1, null)
        addTextWatcher(binding.text2, binding.textfield2, "^[a-z0-9]+(\\.[a-z0-9]+)+$")
        addTextWatcher(binding.text3, binding.textfield5, null)
        addTextWatcher(binding.text4, binding.textfield6, null)
    }

    private fun addTextWatcher(
        editText: TextInputEditText, textInputLayout: TextInputLayout, pattern: String?
    ) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (pattern == null || s.toString().matches(pattern.toRegex())) {
                    textInputLayout.error = null
                } else {
                    textInputLayout.error = getString(R.string.field2_error)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun extractApiVersion(text: String): String? {
        val matcher = Pattern.compile("API (\\d+):").matcher(text)
        return if (matcher.find()) matcher.group(1) else "Не найдено"
    }
}
