package s.ide.compiler;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import s.ide.compiler.exception.CompilerException;
import s.ide.compiler.util.JRELauncher;
import s.ide.signer.model.Library;
import s.ide.signer.model.Project;
import s.ide.utils.app.AppControl;
import s.ide.utils.settings.BuildAndRunSettings;

public class KotlinCompiler extends Compiler {

    private static final String TAG = "kotlinc";

    private final Project mProject;
    private JRELauncher jreLauncher;

    public KotlinCompiler(Project project) {
        mProject = project;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void prepare() throws CompilerException {
        if (!isOpenJDKInstalled()) {
            throw new CompilerException("OpenJDK binary is not installed.");
        }

        if (!isKotlinInstalled()) {
            throw new CompilerException("Kotlin compiler is not installed");
        }

        Map<String, String> env = new HashMap<>();

        env.put("HOME", AppControl.applicationContext.getFilesDir().getAbsolutePath() + "/workspace");
        env.put("JAVA_HOME", AppControl.applicationContext.getFilesDir() + "/openjdk");
        env.put("KOTLIN_HOME", AppControl.applicationContext.getFilesDir() + "/kotlinc");
        env.put("LD_LIBRARY_PATH", AppControl.applicationContext.getFilesDir() + "/openjdk/lib:"
                + AppControl.applicationContext.getFilesDir() + "/openjdk/lib/jli:"
                + AppControl.applicationContext.getFilesDir() + "/openjdk/lib/server:"
                + AppControl.applicationContext.getFilesDir() + "/openjdk/lib/hm:");
        jreLauncher = new JRELauncher(AppControl.applicationContext);
        jreLauncher.setEnvironment(env);
    }

    @Override
    public void run() throws CompilerException {

        if (BuildAndRunSettings.getInstance(Objects.requireNonNull(AppControl.applicationContext)).isDetailedLogsEnabled()) {
            //on progress
            mProject.getLogger().d(TAG, "Running...");
        }

        List<String> args = new ArrayList<>();
        args.add(AppControl.applicationContext.getFilesDir() + "/kotlinc/lib/kotlin-compiler.jar");
        args.add("-classpath");
        args.add(classpath());
        args.add("-d");
        args.add(mProject.getOutputFile() + "/bin/classes");
        //args.add("-Xplugin=$KOTLIN_HOME/lib/compose-compiler-1.0.0.jar");

        args.add(mProject.getJavaFile().getAbsolutePath());

        try {
            Process process = jreLauncher.launchJVM(args);
            loadStream(process.getInputStream(), false);
            loadStream(process.getErrorStream(), true);
            int rc = process.waitFor();

            if (rc != 0) {
                throw new CompilerException("Compilation failed, check output for more details.");
            }
        } catch (Exception e) {
            throw new CompilerException(e.getMessage());
        }
    }

    private boolean isOpenJDKInstalled() {
        File javaFile = new File("/data/data/com.apk.builder/files/openjdk/bin/java");
        if (!javaFile.exists()) {
            if (BuildAndRunSettings.getInstance(Objects.requireNonNull(AppControl.applicationContext)).isDetailedLogsEnabled()) {
                mProject.getLogger().w("JAVA", "Java file.exists returns false!");
            }
        }
        return true;
    }

    private boolean isKotlinInstalled() {
        File kotlinFile = new File(AppControl.applicationContext.getFilesDir(), "/kotlinc/lib/kotlin-compiler.jar");
        return true;
    }

    private String classpath() {
        StringBuilder sb = new StringBuilder();
        for (Library library : mProject.getLibraries()) {
            File classFile = library.getClassJarFile();
            if (classFile.exists()) {
                sb.append(classFile.getAbsolutePath());
                sb.append(":");
            }
        }
        sb.append(getAndroidJarFile().getAbsolutePath());
        sb.append(":");

        return sb.toString();
    }

    private void loadStream(InputStream s, boolean error) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(s));
        String line;
        while ((line = br.readLine()) != null) {

            if (BuildAndRunSettings.getInstance(Objects.requireNonNull(AppControl.applicationContext)).isDetailedLogsEnabled()) {
                if (error) {
                    mProject.getLogger().e(TAG, line);
                } else {
                    mProject.getLogger().d(TAG, line);
                }
            }
        }
    }

    private static void addToEnvIfPresent(Map<String, String> map, String env) {
        String value = System.getenv(env);
        if (value != null) {
            map.put(env, value);
        }
    }
}
