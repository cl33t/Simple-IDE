package s.ide.compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import s.ide.compiler.exception.CompilerException;
import s.ide.compiler.util.BinaryExecutor;
import s.ide.signer.model.Library;
import s.ide.signer.model.Project;
import s.ide.utils.app.AppControl;
import s.ide.utils.settings.BuildAndRunSettings;

public class AAPT2Compiler extends Compiler {

    private static final String TAG = "Resources Compiler";
    private final Project mProject;
    private List<Library> mLibraries;
    private List<Library> mLibrariesCopy;
    private File binDir;
    private File genDir;
    private File resPath;
    private BinaryExecutor executor;

    public AAPT2Compiler(Project project) {
        mProject = project;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void prepare() {
        log("Preparing");
        mLibraries = new ArrayList<>(mProject.getLibraries());
        mLibrariesCopy = new ArrayList<>(mLibraries);
        binDir = new File(mProject.getOutputFile(), "bin");
        genDir = new File(mProject.getOutputFile(), "gen");
        genDir.delete();
        removeExistingLibraries();
        cleanBinDir();
        binDir.mkdir();
        genDir.mkdir();
    }

    @Override
    public void run() throws CompilerException, IOException {
        executor = new BinaryExecutor();
        onProgressUpdate("Running...");
        log("Compiling project resources");
        compileProjectResources();
        compileLibraries();
        linkResources();
    }

    private void compileProjectResources() throws CompilerException, IOException {
        ArrayList<String> args = new ArrayList<>();
        args.add(getAAPT2File().getAbsolutePath());
        args.add("compile");
        args.add("--dir");
        args.add(mProject.getResourcesFile().getAbsolutePath());
        args.add("-o");
        resPath = new File(binDir, "res");
        resPath.mkdir();
        args.add(createNewFile(resPath, "project.zip").getAbsolutePath());
        execute(args);
    }

    private void compileLibraries() throws CompilerException, IOException {
        ArrayList<String> args = new ArrayList<>();
        for (Library library : mLibraries) {
            if (!library.getResourcesFile().exists()) continue;
            log("Compiling library: " + library.getName());
            args.clear();
            args.add(getAAPT2File().getAbsolutePath());
            args.add("compile");
            args.add("--dir");
            args.add(library.getResourcesFile().getAbsolutePath());
            args.add("-o");
            args.add(createNewFile(resPath, library.getName() + ".zip").getAbsolutePath());
            execute(args);
        }
    }

    private void linkResources() throws CompilerException, IOException {
        ArrayList<String> args = new ArrayList<>();
        log("Linking resources");
        args.add(getAAPT2File().getAbsolutePath());
        args.add("link");
        args.add("--allow-reserved-package-id");
        args.add("--no-version-vectors");
        args.add("--no-version-transitions");
        args.add("--auto-add-overlay");
        args.add("--min-sdk-version");
        args.add(String.valueOf(mProject.getMinSdk()));
        args.add("--target-sdk-version");
        args.add(String.valueOf(mProject.getTargetSdk()));
        args.add("--version-code");
        args.add(String.valueOf(mProject.getVersionCode()));
        args.add("--version-name");
        args.add(String.valueOf(mProject.getVersionName()));
        args.add("-I");
        args.add(getAndroidJarFile().getAbsolutePath());
        addAssetsAndResources(args);
        args.add("--java");
        args.add(genDir.getAbsolutePath() + "/");
        args.add("--manifest");
        args.add(mProject.getManifestFile().getAbsolutePath());
        addExtraPackages(args);
        args.add("-o");
        args.add(createNewFile(binDir, "generated.apk.res").getAbsolutePath());
        execute(args);
    }

    private void addAssetsAndResources(ArrayList<String> args) {
        if (mProject.getAssetsFile() != null) {
            args.add("-A");
            args.add(mProject.getAssetsFile().getAbsolutePath());
        }
        File[] resources = resPath.listFiles();
        if (resources != null) {
            for (File file : resources) {
                if (!file.isDirectory() && !file.getName().equals("project.zip")) {
                    args.add("-R");
                    args.add(file.getAbsolutePath());
                }
            }
        }
        File projectZip = new File(resPath, "project.zip");
        if (projectZip.exists()) {
            args.add("-R");
            args.add(projectZip.getAbsolutePath());
        }
    }

    private void addExtraPackages(ArrayList<String> args) {
        StringBuilder sb = new StringBuilder();
        for (Library library : mLibrariesCopy) {
            if (library.requiresResourceFile()) {
                log("Adding extra package: " + library.getPackageName());
                sb.append(library.getPackageName()).append(":");
            }
        }
        if (!sb.toString().isEmpty()) {
            args.add("--extra-packages");
            args.add(sb.toString().substring(0, sb.toString().length() - 1));
        }
    }

    private void execute(ArrayList<String> args) throws CompilerException {
        executor.setCommands(args);
        if (!executor.execute().isEmpty()) {
            throw new CompilerException(executor.getLog());
        }
    }

    private void removeExistingLibraries() {
        for (Library library : new ArrayList<>(mLibraries)) {
            if (new File(binDir, "/res/" + library.getName() + ".zip").exists()) {
                mLibraries.remove(library);
                log("Removing " + library.getName() + " to speed up compilation");
            }
        }
    }

    private void cleanBinDir() {
        File[] childs = binDir.listFiles();
        if (childs != null) {
            for (File child : childs) {
                if (!child.getName().equals("res")) {
                    child.delete();
                }
            }
        }
    }

    private File createNewFile(File parent, String name) throws IOException {
        File createdFile = new File(parent, name);
        parent.mkdirs();
        createdFile.createNewFile();
        return createdFile;
    }

    private File getAAPT2File() throws CompilerException {
        File nativeLibrary = new File(AppControl.applicationContext.getApplicationInfo().nativeLibraryDir + "/libaapt2.so");
        if (!nativeLibrary.exists()) {
            throw new CompilerException("AAPT2 binary not found");
        }
        return nativeLibrary;
    }

    private void log(String message) {
        if (BuildAndRunSettings.getInstance(AppControl.applicationContext).isDetailedLogsEnabled()) {
            mProject.getLogger().d(TAG, message);
        }
    }
}
