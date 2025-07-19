package s.ide.lib;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eup.dependency.haven.api.CachedLibrary;
import eup.dependency.haven.model.Coordinates;

/**
 * Representation of a local library in Sketchware Pro.
 *
 * <p>Typically a local library may consist of any or all. - jni (folder) - res (folder) - config
 * (file containing library packaging) - assets (folder) - classes.jar (file) - classes.dex (file) -
 * proguard.txt (file) - AndroidManifest.xml (file) with exception to classes.jar,classes.dex and
 * config as they are constants
 *
 * @author EUP
 */
public class LocalLibrary extends CachedLibrary {

  private File sourcePath;
  private String libraryName;

  public LocalLibrary() {}

  public void setSourcePath(File sourcePath) {
    this.sourcePath = new File(sourcePath, libraryName);
  }

  public File getSourcePath() {
    return this.sourcePath;
  }

  @Override
  public String getLibraryName() {
    return this.libraryName;
  }

  @Override
  public void setLibraryName(String declaration) {
    Coordinates coordinates = Coordinates.valueOf(declaration);
    this.libraryName =
        String.format("%s-v%s", coordinates.getArtifactId(), coordinates.getVersion());
  }

  public boolean isAar() {
    return FilenameUtils.getExtension(getSourceFile().getName()).equals("aar");
  }

  public boolean isJar() {
    return FilenameUtils.getExtension(getSourceFile().getName()).equals("jar");
  }

  public File getJarFile() {
    return new File(sourcePath, "classes.jar");
  }

  /**
   * Finds the package name for an aar file.
   *
   * @return the packaging
   */
  public String findPackageName() throws IOException {
    // List all files in the directory, if true search all subdirectories
    Collection<File> fileCollection = FileUtils.listFiles(sourcePath, new String[] {"xml"}, true);
    // Step 1: Check the AndroidManifest.xml file
    for (File manifestFile : fileCollection) {
      if (manifestFile.getName().equals("AndroidManifest.xml")) {
        String content = FileUtils.readFileToString(manifestFile, StandardCharsets.UTF_8);
        Pattern packagingPattern = Pattern.compile("<manifest.*package=\"(.*?)\"", Pattern.DOTALL);
        Matcher m = packagingPattern.matcher(content);
        if (m.find()) {
          if (m.group(1) != null) return m.group(1);
        }
      }
    }
    // Step 2: Use dependency coordinates as a fallback
    return getLibraryPom().getCoordinates().getGroupId();
  }

  public void deleteUnnecessaryFiles() throws IOException {
    // Define the list of valid top-level files and directories
    List<String> validTopLevel =
        Arrays.asList(
            "res",
            "classes.dex",
            "classes.jar",
            "config",
            "AndroidManifest.xml",
            "jni",
            "assets",
            "proguard.txt");

    // List all files in the top-level directory (not recursively)
    File[] topLevelFiles = sourcePath.listFiles();

    if (topLevelFiles != null) {
      for (File file : topLevelFiles) {
        if (!validTopLevel.contains(file.getName())) {
          if (file.isFile()) {
            FileUtils.forceDelete(file);
          } else if (file.isDirectory()) {
            FileUtils.deleteDirectory(file);
          }
        }
      }
    }
  }
}
