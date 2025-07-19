package s.ide.compiler.incremental;

import androidx.annotation.NonNull;

import org.eclipse.jdt.internal.compiler.batch.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kotlin.io.FilesKt;
import s.ide.compiler.Compiler;
import s.ide.compiler.exception.CompilerException;
import s.ide.compiler.incremental.file.JavaFile;
import s.ide.signer.model.Library;
import s.ide.signer.model.Project;
import s.ide.utils.app.AppControl;
import s.ide.utils.settings.BuildAndRunSettings;

public class IncrementalECJCompiler extends Compiler {

    private static final String TAG = "Java Compiler";

    private final Project mProject;

    private List<File> filesToCompile;

    public IncrementalECJCompiler(Project project) {
        mProject = project;
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public void prepare() {

        List<JavaFile> oldFiles = findJavaFiles(new File(mProject.getOutputFile() + "/intermediate/java"));
        List<JavaFile> newFiles = new ArrayList<>();
        newFiles.addAll(findJavaFiles(mProject.getJavaFile()));
        newFiles.addAll(findJavaFiles(new File(mProject.getOutputFile() + "/gen")));

        filesToCompile = getModifiedFiles(oldFiles, newFiles);
    }

    @Override
    public void run() throws CompilerException {


        if (filesToCompile.isEmpty()) {
            mProject.getLogger().d(TAG, "Files are up to date, skipping compilation.");
            return;
        }

        onProgressUpdate("Running...");

        CompilerOutputStream errorOutputStream = new CompilerOutputStream(new StringBuffer());
        PrintWriter errWriter = new PrintWriter(errorOutputStream);

        CompilerOutputStream outputStream = new CompilerOutputStream(new StringBuffer());
        PrintWriter outWriter = new PrintWriter(outputStream);

        ArrayList<String> args = getStrings();
        Main main = new Main(outWriter, errWriter, false, null, null);

        main.compile(args.toArray(new String[0]));

        if (main.globalErrorsCount > 0) {
            throw new CompilerException(errorOutputStream.buffer.toString());
        }

        if (BuildAndRunSettings.getInstance(Objects.requireNonNull(AppControl.applicationContext)).isDetailedLogsEnabled()) {
            mProject.getLogger().d(TAG, "Merging modified java files");
        }
        mergeClasses(filesToCompile);
    }

    @NonNull
    private ArrayList<String> getStrings() {
        ArrayList<String> args = new ArrayList<>();
        //TODO: make this user changeable
        args.add("-11");
        args.add("-nowarn");
        args.add("-d");
        args.add(mProject.getOutputFile() + "/intermediate/classes");
        args.add("-proc:none");

        args.add("-cp");
        StringBuilder libraryString = new StringBuilder();

        libraryString.append(getAndroidJarFile().getAbsolutePath());
        libraryString.append(":");
        for (Library library : mProject.getLibraries()) {

            File classFile = library.getClassJarFile();
            if (classFile.exists()) {
                libraryString.append(classFile.getAbsolutePath());
                libraryString.append(":");
            }
        }

        libraryString.append(getLambdaFactoryFile().getAbsolutePath());
        libraryString.append(":");
        libraryString.append(mProject.getJavaFile());
        args.add(libraryString.toString());

        args.add("-sourcepath");
        //workaround to allow ecj to compile multiple paths
        args.add(" ");
        for (File file : filesToCompile) {

            args.add(file.getAbsolutePath());
        }
        return args;
    }

    /**
     * Finds all Java source code files in a given directory.
     *
     * @param input input directory
     * @return returns a list of java files
     */
    private List<JavaFile> findJavaFiles(File input) {
        List<JavaFile> foundFiles = new ArrayList<>();

        if (input.isDirectory()) {
            File[] contents = input.listFiles();
            if (contents != null) {
                for (File child : contents) {
                    foundFiles.addAll(findJavaFiles(child));
                }
            }
        } else {
            if (input.getName().endsWith(".java")) {
                foundFiles.add(new JavaFile(input.getPath()));
            }
        }
        return foundFiles;
    }

    /**
     * Compares two list of Java source code files, and outputs ones, that are modified.
     */
    private List<File> getModifiedFiles(List<JavaFile> oldFiles, List<JavaFile> newFiles) {
        List<File> modifiedFiles = new ArrayList<>();

        for (JavaFile newFile : newFiles) {
            if (!oldFiles.contains(newFile)) {
                modifiedFiles.add(newFile);
            } else {
                File oldFile = oldFiles.get(oldFiles.indexOf(newFile));
                if (contentModified(oldFile, newFile)) {
                    modifiedFiles.add(newFile);
                    if (oldFile.delete()) {
                        if (BuildAndRunSettings.getInstance(Objects.requireNonNull(AppControl.applicationContext)).isDetailedLogsEnabled()) {
                            mProject.getLogger().d(TAG, oldFile.getName() + ": Removed old class file that has been modified");
                        }
                    }
                }
                oldFiles.remove(oldFile);
            }
        }
        //we delete the removed classes from the original path
        for (JavaFile removedFile : oldFiles) {
            if (BuildAndRunSettings.getInstance(Objects.requireNonNull(AppControl.applicationContext)).isDetailedLogsEnabled()) {
                mProject.getLogger().d(TAG, "Class no longer exists, deleting file: " + removedFile.getName());
            }
            if (!removedFile.delete()) {
                if (BuildAndRunSettings.getInstance(AppControl.applicationContext).isDetailedLogsEnabled()) {
                    mProject.getLogger().w(TAG, "Failed to delete file " + removedFile.getAbsolutePath());
                }
            } else {
                String name = removedFile.getName().substring(0, removedFile.getName().indexOf("."));
                deleteClassInDir(name, new File(mProject.getOutputFile() + "/intermediate/classes"));
            }
        }
        return modifiedFiles;
    }

    /**
     * merges the modified classes to the non modified files so that we can compare it next compile
     */
    public void mergeClasses(List<File> files) {

        for (File file : files) {

            String pkg = getPackageName(file);
            if (pkg == null) {
                continue;
            }

            String packagePath = mProject.getOutputFile() + "/intermediate/java/" + pkg;
            FilesKt.copyTo(file, new File(packagePath), true, 1024);

        }
    }

    private void deleteClassInDir(String name, File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteClassInDir(name, child);
                }
            }
        } else {
            String dirName = dir.getName().substring(0, dir.getName().indexOf("."));
            if (dirName.contains("$")) {
                dirName = dirName.substring(0, dirName.indexOf("$"));
            }
            if (dirName.equals(name)) {
                dir.delete();
            }
        }

    }

    /**
     * checks if contents of the file has been modified
     */
    private boolean contentModified(File old, File newFile) {
        if (old.isDirectory() || newFile.isDirectory()) {
            throw new IllegalArgumentException("Given file must be a Java file");
        }

        if (!old.exists() || !newFile.exists()) {
            return true;
        }

        if (newFile.length() != old.length()) {
            return true;
        }

        return newFile.lastModified() > old.lastModified();
    }

    /**
     * Gets the package name of a specific Java source code file.
     *
     * @param file The Java file
     * @return The file's package name, in Java format
     */
    private String getPackageName(File file) {
        String packageName = "";

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            while (!packageName.contains("package")) {
                packageName = reader.readLine();

                if (packageName == null) {
                    packageName = "";
                }
            }

        } catch (IOException e) {
            mProject.getLogger().e(TAG, e.getMessage());
            return null;
        }

        if (packageName.contains("package")) {

            packageName = packageName.replace("package ", "").replace(";", ".").replace(".", "/");

            if (!packageName.endsWith("/")) {
                packageName = packageName.concat("/");
            }

            return packageName + file.getName();
        }

        return null;
    }

    private class CompilerOutputStream extends OutputStream {

        public StringBuffer buffer;

        public CompilerOutputStream(StringBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void write(int b) {

            if (b == '\n') {
                mProject.getLogger().d(TAG, buffer.toString());
                buffer = new StringBuffer();
                return;
            }
            buffer.append((char) b);
        }
    }

}
