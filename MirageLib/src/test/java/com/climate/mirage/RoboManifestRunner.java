package com.climate.mirage;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;
import org.robolectric.res.FsFile;

public class RoboManifestRunner extends RobolectricGradleTestRunner {

    public RoboManifestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        AndroidManifest appManifest = super.getAppManifest(config);
        FsFile androidManifestFile = appManifest.getAndroidManifestFile();

        if (androidManifestFile.exists()) {
            return appManifest;
        } else {
            final int emulatedSdk = config.emulateSdk();
            String moduleRoot = getModuleRootPath(config);
            androidManifestFile = FileFsFile.from(moduleRoot, appManifest.getAndroidManifestFile().getPath());
            FsFile resDirectory = FileFsFile.from(moduleRoot, appManifest.getResDirectory().getPath());
            FsFile assetsDirectory = FileFsFile.from(moduleRoot, appManifest.getAssetsDirectory().getPath());


            // So this was working one day and the above was working the other
            // leave this in until I can figure out why the paths magically changed.
//            androidManifestFile = FileFsFile.from(moduleRoot, appManifest.getAndroidManifestFile().getPath().replace("bundles", "manifests/test"));
//            FsFile resDirectory = FileFsFile.from(moduleRoot, appManifest.getResDirectory().getPath().replace("/res", "").replace("bundles", "res"));
//            FsFile assetsDirectory = FileFsFile.from(moduleRoot, appManifest.getAssetsDirectory().getPath().replace("/assets", "").replace("bundles", "assets"));
            return new AndroidManifest(androidManifestFile, resDirectory, assetsDirectory) {
                @Override
                public int getTargetSdkVersion() {
                    return emulatedSdk > 2 ? emulatedSdk : 21;
                }
            };
        }
    }

    private String getModuleRootPath(Config config) {
        String moduleRoot = config.constants().getResource("").toString().replace("file:", "");
        return moduleRoot.substring(0, moduleRoot.indexOf("/build"));
    }
}