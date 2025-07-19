package s.ide.compiler;


import android.system.ErrnoException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import s.ide.compiler.exception.CompilerException;
import s.ide.signer.util.Decompress;
import s.ide.utils.app.AppControl;

public abstract class Compiler {

    public interface OnProgressUpdateListener {
        void onProgressUpdate(String... update);
    }

    protected OnProgressUpdateListener listener;
    private static final ConcurrentHashMap<String, File> fileCache = new ConcurrentHashMap<>();

    public void setProgressListener(OnProgressUpdateListener listener) {
        this.listener = listener;
    }

    public void onProgressUpdate(String... update) {
        if (listener != null) {
            listener.onProgressUpdate(update);
        }
    }

    public abstract String getTag();

    abstract public void prepare() throws CompilerException, IOException;

    abstract public void run() throws CompilerException, IOException, ErrnoException;

    private static File getFileFromAssets(String fileName, String zipName) {
        return fileCache.computeIfAbsent(fileName, key -> {
            File file = new File(Objects.requireNonNull(AppControl.applicationContext).getFilesDir() + "/temp/" + fileName);
            if (!file.exists()) {
                Decompress.unzipFromAssets(AppControl.applicationContext, zipName, Objects.requireNonNull(file.getParentFile()).getAbsolutePath());
            }
            return file;
        });
    }

    public static File getAndroidJarFile() {
        return getFileFromAssets("android.jar", "android.jar.zip");
    }

    public static File getLambdaFactoryFile() {
        return getFileFromAssets("core-lambda-stubs.jar", "core-lambda-stubs.zip");
    }

}
