package s.ide.compiler;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.android.sdklib.build.ApkBuilder;

import java.io.File;
import java.util.Objects;

import s.ide.compiler.incremental.IncrementalD8Compiler;
import s.ide.compiler.incremental.IncrementalECJCompiler;
import s.ide.signer.ApkSigner;
import s.ide.signer.model.Library;
import s.ide.signer.model.Project;
import s.ide.ui.dialog.CustomMaterialDialog;
import s.ide.utils.app.AppControl;
import s.ide.utils.settings.BuildAndRunSettings;

public class CompilerTask implements Runnable {

    private final Context mContext;
    private final Project project;
    private volatile boolean isCancelled = false;
    private volatile boolean isRunning = false;

    private Runnable onSuccess;
    private Runnable onError;

    public CompilerTask(Context context, Project project) {
        this.mContext = context;
        this.project = project;
    }

    public void setOnSuccessListener(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    public void setOnErrorListener(Runnable onError) {
        this.onError = onError;
    }

    public void cancel() {
        isCancelled = true;
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        isRunning = true;
        try {
            long startTime = System.currentTimeMillis();

            if (isCancelled) return;
            compileTask(new AAPT2Compiler(project));

            if (isCancelled) return;
            compileTask(new IncrementalECJCompiler(project));

            if (isCancelled) return;
            compileTask(new IncrementalD8Compiler(project));

            if (isCancelled) return;
            publishProgress("APK Builder", "Packaging APK...");
            packageAndSignApk();

            long time = System.currentTimeMillis() - startTime;
            if (BuildAndRunSettings.getInstance(Objects.requireNonNull(AppControl.applicationContext)).isDetailedLogsEnabled()) {
                project.getLogger().d("APK Builder", "Build success, took " + time + "ms");
            }

            if (onSuccess != null && !isCancelled) {
                onSuccess.run();
            }

        } catch (Exception e) {
            handleException(e);
        } finally {
            isRunning = false;
        }
    }

    private void compileTask(Compiler compiler) {
        try {
            if (isCancelled) return;
            compiler.setProgressListener(message -> publishProgress(compiler.getTag(), message));
            compiler.prepare();
            compiler.run();
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        cancel();
//        project.getLogger().d("Error", Log.getStackTraceString(e));
        showErrorDialog(mContext, e.getLocalizedMessage());
        if (onError != null) {
            onError.run();
        }
    }

    private void publishProgress(String tag, String... messages) {
        for (String message : messages) {
            if (isCancelled) return;
            project.getLogger().d(tag, message);
        }
    }

    private void showErrorDialog(Context context, String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            CustomMaterialDialog errorDialog = new CustomMaterialDialog(context);
            errorDialog.setTitle(R.string.some_error);
            errorDialog.setMessage(message);
            errorDialog.setPositiveButton(R.string.action_negative, null);
            errorDialog.show();
        });
    }

    private void packageAndSignApk() throws Exception {
        if (isCancelled) return;
        File binDir = new File(project.getOutputFile(), "bin");
        File apkPath = new File(binDir, "gen.apk");
        apkPath.createNewFile();

        File resPath = new File(binDir, "generated.apk.res");
        File dexFile = new File(binDir, "classes.dex");
        ApkBuilder builder = new ApkBuilder(apkPath, resPath, dexFile, null, null);

        File[] binFiles = binDir.listFiles();
        if (binFiles != null) {
            for (File file : binFiles) {
                if (isCancelled) return;
                if (!file.getName().equals("classes.dex") && file.getName().endsWith(".dex")) {
                    builder.addFile(file, Uri.parse(file.getAbsolutePath()).getLastPathSegment());
                    if (BuildAndRunSettings.getInstance(Objects.requireNonNull(AppControl.applicationContext)).isDetailedLogsEnabled()) {
                        project.getLogger().d("APK Builder", "Adding dex file " + file.getName() + " to APK.");
                    }
                }
            }
        }
        for (Library library : project.getLibraries()) {
            if (isCancelled) return;
            builder.addResourcesFromJar(new File(library.getPath(), "classes.jar"));
            if (BuildAndRunSettings.getInstance(Objects.requireNonNull(AppControl.applicationContext)).isDetailedLogsEnabled()) {
                project.getLogger().d("APK Builder", "Adding resources of " + library.getName() + " to the APK");
            }
        }

        File nativeLibs = project.getNativeLibrariesFile();
        if (nativeLibs != null && nativeLibs.exists()) {
            builder.addNativeLibraries(nativeLibs);
        }
        builder.setDebugMode(false);
        builder.sealApk();

        if (BuildAndRunSettings.getInstance(Objects.requireNonNull(AppControl.applicationContext)).isDetailedLogsEnabled()) {
            project.getLogger().d("APK Signer", "Signing APK");
        }

        // sign the app
        new ApkSigner(project, apkPath.getAbsolutePath(), binDir + "/signed.apk").sign();
    }
}
