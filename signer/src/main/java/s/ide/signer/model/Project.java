package s.ide.signer.model;

import java.util.List;
import java.io.File;

import s.ide.logging.Logger;

public class Project {
    
	private File mOutputFile;
	
	private File mResourcesFile;
	
	private File mJavaFile;
	
	private File mManifestFile;
	
	private int mMinSdk;
	
	private int mTargetSdk;
	
	private int mVersionCode = 1;
	
	private String mVersionName = "1.0";

	private int mCompileSdk = 33;
	
	private List<Library> mLibraries;
	
	private File mAssetsFile;
	
	private File mNativeFile;
	
	private Logger mLogger;
	
	public Project() {
		
	}

	public File getResourcesFile() {
		return mResourcesFile;
	}
	
	public void setResourcesFile(File file) {
		mResourcesFile = file;
	}
	
	public File getOutputFile() {
		return mOutputFile;
	}
	
	public void setOutputFile(File file) {
		mOutputFile = file;
	}
	
	public File getManifestFile() {
		return mManifestFile;
	}

	public void setManifestFile(File file) {
		mManifestFile = file;
	}
	
	public File getJavaFile() {
		return mJavaFile;
	}
	
	public void setJavaFile(File file) {
		mJavaFile = file;
	}
	
	public int getMinSdk() {
		return mMinSdk;
	}

	public int getNDMinSdk() {
		return 26;
	}

	public void setMinSdk(int sdk) {
		mMinSdk = sdk;
	}

	public int getCompileSdk() {
		return mCompileSdk;
	}

	public void setCompileSdk(int compileSdk) {
		mCompileSdk = compileSdk;
	}

	public int getTargetSdk() {
		return mTargetSdk;
	}

	public void setTargetSdk(int sdk) {
		mTargetSdk = sdk;
	}

	public List<Library> getLibraries() {
		return mLibraries;
	}
	
	public void setLibraries(List<Library> libraries) {
		mLibraries = libraries;
	}
	
	public int getVersionCode() {
		return mVersionCode;
	}
	
	public void setVersionCode(int code) {
		mVersionCode = code;
	}
	
	public String getVersionName() {
		return mVersionName;
	}
	
	public void setVersionName(String str) {
		mVersionName = str;
	}
	
	public File getAssetsFile() {
		return mAssetsFile;
	}
	
	public void setAssetsFile(File file) {
		mAssetsFile = file;
	}
	
	public File getNativeLibrariesFile() {
		return mNativeFile;
	}
	
	public void setNativeLibraries(File file) {
		mNativeFile = file;
	}
	
	public Logger getLogger() {
		return mLogger;
	}
	
	public void setLogger(Logger logger) {
		mLogger = logger;
	}

}