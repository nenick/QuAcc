package de.nenick.quacc.apptest.espresso;

import android.test.ActivityInstrumentationTestCase2;

import de.nenick.quacc.R;
import de.nenick.quacc.apptest.DummyLauncherActivity_;
import de.nenick.quacc.apptest.pages.EspressoDummyLauncherPage;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class BaseEspressoSpec extends ActivityInstrumentationTestCase2<DummyLauncherActivity_> {
    public BaseEspressoSpec() {
        super(DummyLauncherActivity_.class);
    }

    EspressoDummyLauncherPage launcherPage = new EspressoDummyLauncherPage();

    public void startApp() {
        getActivity();
        launcherPage.clickStartApp();
    }

    @Override
    protected void tearDown() throws Exception {
        CloseAllActivitiesFunction.apply(getInstrumentation());
        super.tearDown();
    }
}