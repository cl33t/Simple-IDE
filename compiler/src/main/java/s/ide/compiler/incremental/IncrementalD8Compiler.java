package s.ide.compiler.incremental;


import com.android.tools.r8.D8;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import s.ide.compiler.Compiler;
import s.ide.compiler.exception.CompilerException;
import s.ide.signer.model.Library;
import s.ide.signer.model.Project;
import s.ide.utils.app.AppControl;
import s.ide.utils.settings.BuildAndRunSettings;

public class IncrementalD8Compiler extends Compiler {

    private static final String TAG = "D8 Compiler";
    private final Project mProject;
    private List<String> classpath;

    public IncrementalD8Compiler(Project project) {
        mProject = project;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void prepare() {
        // No preparation needed
    }

    @Override
    public void run() throws CompilerException {

        onProgressUpdate("Running...");

        List<String> args = new ArrayList<>();
        args.add("--intermediate");
        args.add("--min-api");
        args.add(String.valueOf(mProject.getMinSdk()));
        args.add("--lib");
        args.add(getAndroidJarFile().getAbsolutePath());
        args.add("--output");
        args.add(mProject.getOutputFile() + "/bin/");

        addClassFiles(args, new File(mProject.getOutputFile() + "/intermediate/classes/"));
        addLibraryDexFiles(args);

        executeD8(args);


    }

    private void dexLibrary(Library library) throws CompilerException {

        log("Library " + library.getName() + " does not have a dex file, generating one");

        List<String> args = new ArrayList<>();
        args.add("--release");
        args.add("--lib");
        args.add(getAndroidJarFile().getAbsolutePath());
        args.add("--output");
        args.add(library.getPath().getAbsolutePath());

        if (!BuildAndRunSettings.getInstance(AppControl.applicationContext).isDesugarEnabled()) {
            args.add("--min-api");
            args.add(String.valueOf(mProject.getNDMinSdk()));
            args.add("--no-desugaring");
            args.add(library.getClassJarFile().getAbsolutePath());
        } else {
            args.add("--min-api");
            args.add(String.valueOf(mProject.getMinSdk()));
            args.addAll(classpath());
        }

        executeD8(args);

    }

    public List<String> classpath() {
        if (classpath != null) {
            return classpath;
        }

        classpath = new ArrayList<>();


        for (Library library : mProject.getLibraries()) {
            classpath.add("--classpath");
            classpath.add(library.getClassJarFile().getAbsolutePath());
        }

        return classpath;
    }

    private void addClassFiles(List<String> args, File dir) {
        for (File file : getClassFiles(dir)) {
            args.add(file.getAbsolutePath());
        }
    }

    private void addLibraryDexFiles(List<String> args) throws CompilerException {
        for (Library library : mProject.getLibraries()) {

            if (library.getDexFiles().isEmpty()) {
                dexLibrary(library);
            }
            for (File dexFile : library.getDexFiles()) {

                args.add(dexFile.getAbsolutePath());
            }
        }
    }

    private void executeD8(List<String> args) throws CompilerException {

        try {
            D8.main(args.toArray(new String[0]));
        } catch (Exception e) {
            throw new CompilerException(e.getMessage());
        }


    }

    private List<File> getClassFiles(File dir) {
        List<File> files = new ArrayList<>();
        File[] fileArr = dir.listFiles();
        if (fileArr != null) {
            for (File file : fileArr) {

                if (file.isDirectory()) {
                    files.addAll(getClassFiles(file));
                } else {
                    files.add(file);
                }
            }
        }
        return files;
    }


    private void log(String message) {
        if (BuildAndRunSettings.getInstance(Objects.requireNonNull(AppControl.applicationContext)).isDetailedLogsEnabled()) {
            mProject.getLogger().d(TAG, message);
        }
    }
}
