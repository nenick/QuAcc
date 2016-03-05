package de.nenick.robolectric;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;
import org.robolectric.res.FsFile;

import java.io.File;

import de.nenick.quacc.BuildConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * More dynamic path resolution.
 * <p/>
 * This workaround is only for Mac Users necessary and only if they don't use the $MODULE_DIR$
 * workaround. Follow this issue at https://code.google.com/p/android/issues/detail?id=158015
 */
public class AndroidStudioAwareRobolectricTestRunner extends RobolectricGradleTestRunner {

    public AndroidStudioAwareRobolectricTestRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        /*
        String intermediatesPath = BuildConfig.MODULE_PATH + "/build//intermediates";
        System.setProperty("java.io.tmpdir", intermediatesPath + "/test-data");
        mkdir(intermediatesPath + "/test-data");
        */
    }

    private void mkdir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            assertThat(file.mkdir()).isTrue();
        }
    }

    protected AndroidManifest getAppManifest(Config config) {
        AndroidManifest appManifest = super.getAppManifest(config);
        FsFile androidManifestFile = appManifest.getAndroidManifestFile();

        if (androidManifestFile.exists()) {
            return appManifest;
        } else {
            String moduleRoot = getModuleRootPath(config);
            androidManifestFile = FileFsFile.from(moduleRoot, appManifest.getAndroidManifestFile().getPath().replace("bundles", "manifests/full"));
            FsFile resDirectory = FileFsFile.from(moduleRoot, appManifest.getResDirectory().getPath());
            FsFile assetsDirectory = FileFsFile.from(moduleRoot, appManifest.getAssetsDirectory().getPath());
            return new AndroidManifest(androidManifestFile, resDirectory, assetsDirectory);
        }
    }

    private String getModuleRootPath(Config config) {
        String moduleRoot = config.constants().getResource("").toString().replace("file:", "").replace("jar:", "");
        return moduleRoot.substring(0, moduleRoot.indexOf("/build"));
    }
}
