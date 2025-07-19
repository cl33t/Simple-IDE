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

package s.ide.utils.projects

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import s.ide.utils.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files

object MakeProject {
    private fun getPath(which: String): String {
        return when (which) {
            "res" -> "/app/src/main/res"
            "java" -> "/app/src/main/java"
            "assets" -> "/app/src/main/assets"
            "manifest" -> "/app/src/main/AndroidManifest.xml"
            "output" -> "/build"
            "localLibs" -> "/app/libs"
            else -> throw IllegalArgumentException("Unknown path type: $which")
        }
    }

    @Throws(IOException::class)
    fun copyProject(
        context: Context,
        projectName: String,
        versionCode: String?,
        versionName: String?,
        minSdkVersion: String?,
        targetSdkVersion: String?,
        packageName: String
    ) {
        val destinationPath = context.filesDir.toString() + File.separator + context.getString(
            R.string.app_name
        ) + File.separator + projectName
        val destinationFolder = File(destinationPath)
        createDirectory(destinationFolder)
        val packageDir = File(
            destinationFolder,
            "/app/src/main/java/" + packageName.replace(".", File.separator)
        )
        val valuesDir = File(destinationFolder, "/app/src/main/res/values/")
        val manifestDir = File(destinationFolder, "app/src/main/")

        createDirectory(packageDir)
        writeToFile(
            File(packageDir, "MainActivity.java"), String.format(
                """
                package %s;
                
                import android.os.Bundle;
                
                import androidx.appcompat.app.AppCompatActivity;
                
                public class MainActivity extends AppCompatActivity {
                
                    @Override
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                
                        setContentView(R.layout.activity_main);
                
                    }
                }
                """.trimIndent(), packageName
            )
        )

        writeToFile(
            File(manifestDir, "AndroidManifest.xml"), String.format(
                """
                <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                
                  package="%s">
                
                     <application
                       android:icon="@mipmap/ic_launcher"
                       android:label="@string/app_name"
                       android:roundIcon="@mipmap/ic_launcher_round"
                       android:supportsRtl="true"
                       android:theme="@style/AppTheme" >
                          <activity
                           android:name=".MainActivity"
                           android:exported="true">
                              <intent-filter>
                                  <action android:name="android.intent.action.MAIN" />
                
                                  <category android:name="android.intent.category.LAUNCHER" />
                              </intent-filter>
                           </activity>
                     </application>
                
                </manifest>
                
                """.trimIndent(), packageName
            )
        )

        copyFilesFromAssets(context, destinationFolder)

        writeToFile(
            File(valuesDir, "strings.xml"), String.format(
                """
                <resources>
                  <string name="app_name">%s</string>
                </resources>
                """.trimIndent(), projectName
            )
        )

        val compileSdkVersion = "34"
        writeProjectConfig(
            destinationPath,
            projectName,
            versionCode,
            versionName,
            minSdkVersion,
            targetSdkVersion,
            compileSdkVersion,
            packageName
        )
    }

    @Throws(IOException::class)
    private fun writeProjectConfig(
        destinationPath: String?,
        projectName: String?,
        versionCode: String?,
        versionName: String?,
        minSdkVersion: String?,
        targetSdkVersion: String?,
        compileSdkVersion: String?,
        packageName: String?
    ) {
        val config: MutableMap<String?, String?> = HashMap()
        config["projectName"] = projectName
        config["versionCode"] = versionCode
        config["versionName"] = versionName
        config["minSdkVersion"] = minSdkVersion
        config["targetSdkVersion"] = targetSdkVersion
        config["compileSdkVersion"] = compileSdkVersion
        config["packageName"] = packageName
        config["librariesPath"] = destinationPath + getPath("localLibs")
        config["outputPath"] = destinationPath + getPath("output")
        config["manifestPath"] = destinationPath + getPath("manifest")
        config["resourcesPath"] = destinationPath + getPath("res")
        config["javaPath"] = destinationPath + getPath("java")
        config["assetsPath"] = destinationPath + getPath("assets")

        val objectMapper = ObjectMapper()
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT) // Enable pretty-printing
        objectMapper.writeValue(File(destinationPath, "compiler_config.json"), config)
    }

    @Throws(IOException::class)
    private fun writeToFile(file: File?, data: ByteArray?) {
        FileOutputStream(file).use { outputStream ->
            outputStream.write(data)
        }
    }

    @Throws(IOException::class)
    private fun writeToFile(file: File?, data: String) {
        writeToFile(file, data.toByteArray(StandardCharsets.UTF_8))
    }

    @Throws(IOException::class)
    private fun copyFilesFromAssets(context: Context, destinationFolder: File) {
        createDirectory(destinationFolder) // Create the root folder
        copyFilesFromAssetsRecursive(context, "template", destinationFolder)
    }

    @Throws(IOException::class)
    private fun copyFilesFromAssetsRecursive(
        context: Context,
        assetsFolderPath: String,
        destinationFolder: File
    ) {
        val files = context.assets.list(assetsFolderPath)

        if (files.isNullOrEmpty()) {
            // If the folder is empty, create it in the destination
            createDirectory(destinationFolder)
            return
        }

        for (file in files) {
            val fullPath: String? =
                if (assetsFolderPath.isEmpty()) file else assetsFolderPath + File.separator + file
            val destinationFile = File(destinationFolder, file)

            val childFiles = context.assets.list(fullPath!!)
            if (childFiles.isNullOrEmpty()) {
                // This is a file
                context.assets.open(fullPath).use { `in` ->
                    Files.newOutputStream(destinationFile.toPath()).use { out ->
                        copyFile(`in`, out)
                    }
                }
            } else {
                // This is a folder
                createDirectory(destinationFile)
                copyFilesFromAssetsRecursive(context, fullPath, destinationFile)
            }
        }
    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }

    private fun createDirectory(directory: File) {
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }

    @Throws(IOException::class)
    fun createAndAddProject(
        context: Context, projectName: String, versionCode: String?,
        versionName: String?, minSdkVersion: String?, targetSdkVersion: String?,
        packageName: String, projectController: ProjectController
    ) {
        // Копируем проект
        copyProject(context, projectName, versionCode, versionName, minSdkVersion, targetSdkVersion, packageName)

        // Получаем путь к проекту
        val destinationPath = context.filesDir.toString() + File.separator + context.getString(R.string.app_name) + File.separator + projectName
        val configPath = destinationPath + File.separator + "compiler_config.json"

        // Добавляем проект в ProjectController
        projectController.addProject(projectName, destinationPath, configPath)
    }
}
