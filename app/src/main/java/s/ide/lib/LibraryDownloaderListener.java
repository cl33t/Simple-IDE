package s.ide.lib;

import java.io.IOException;

public interface LibraryDownloaderListener {
    void onDownloadRequested(String dependency, boolean skipSubDependencies) throws IOException;
}


