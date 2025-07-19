package s.ide.signer;

import com.android.apksigner.ApkSignerTool;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import s.ide.signer.model.Project;
import s.ide.signer.util.Decompress;
import s.ide.utils.app.AppControl;


public class ApkSigner {

    private final ArrayList<String> commands;
    private final String mApkInputPath;
    private final String mApkOutputPath;
    private final Project mProject;

    public ApkSigner(Project project, String inputPath, String outputPath) {
        commands = new ArrayList<>();
        mProject = project;
        mApkInputPath = inputPath;
        mApkOutputPath = outputPath;

    }

    public void sign() throws Exception {
        commands.add("sign");
        commands.add("--key");
        commands.add(getTestKeyFilePath());
        commands.add("--cert");
        commands.add(getTestCertFilePath());
        commands.add("--min-sdk-version");
        commands.add(String.valueOf(mProject.getMinSdk()));
        commands.add("--max-sdk-version");
        commands.add(String.valueOf(mProject.getTargetSdk()));
        commands.add("--out");
        commands.add(mApkOutputPath);
        commands.add("--in");
        commands.add(mApkInputPath);
        ApkSignerTool.main(commands.toArray(new String[commands.size()]));

    }


    private String getTestKeyFilePath() {
        File check = new File(Objects.requireNonNull(AppControl.applicationContext).getFilesDir() + "/temp/testkey.pk8");

        if (check.exists()) {
            return check.getAbsolutePath();
        }

        Decompress.unzipFromAssets(AppControl.applicationContext, "testkey.pk8.zip", Objects.requireNonNull(check.getParentFile()).getAbsolutePath());

        return check.getAbsolutePath();
    }

    private String getTestCertFilePath() {
        File check = new File(Objects.requireNonNull(AppControl.applicationContext).getFilesDir() + "/temp/testkey.x509.pem");

        if (check.exists()) {
            return check.getAbsolutePath();
        }

        Decompress.unzipFromAssets(AppControl.applicationContext, "testkey.x509.pem.zip", Objects.requireNonNull(check.getParentFile()).getAbsolutePath());

        return check.getAbsolutePath();
    }

}
