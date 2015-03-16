package de.nenick.quacc.ct;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;

import de.nenick.quacc.BuildConfig;
import de.nenick.quacc.addaccounting.AddAccountingActivity;

public class AndroidStudioAwareRobolectricTestRunner extends RobolectricTestRunner {

    public AndroidStudioAwareRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        String buildVariant = (BuildConfig.FLAVOR.isEmpty() ? "" : BuildConfig.FLAVOR+ "/") + BuildConfig.BUILD_TYPE;
        String intermediatesPath = AddAccountingActivity.class.getResource("").toString().replace("file:", "");
        System.out.println(intermediatesPath);
        intermediatesPath = intermediatesPath.substring(0, intermediatesPath.indexOf("/classes"));

        System.setProperty("android.package", BuildConfig.APPLICATION_ID);
        System.setProperty("android.manifest", intermediatesPath + "/manifests/full/" + buildVariant + "/AndroidManifest.xml");
        System.setProperty("android.resources", intermediatesPath + "/res/" + buildVariant);
        System.setProperty("android.assets", intermediatesPath + "/assets/" + buildVariant);
    }
}
