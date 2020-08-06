package com.company.bmobkotlin.test.core;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Looper;
import android.os.SystemClock;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.uiautomator.UiDevice;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.TextView;

import com.company.bmobkotlin.app.SAPWizardApplication;
import com.company.bmobkotlin.logon.ClientPolicy;
import com.company.bmobkotlin.logon.ClientPolicyManager;
import com.company.bmobkotlin.test.core.factory.LoginPageFactory;
import com.company.bmobkotlin.test.core.factory.PasscodePageFactory;
import com.company.bmobkotlin.test.pages.ActivationPage;
import com.company.bmobkotlin.test.pages.PasscodePage;
import com.company.bmobkotlin.test.pages.WelcomePage;
import com.company.bmobkotlin.test.pages.OfflineScreen;

import org.hamcrest.Matcher;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyString;

public class Utils {

    public static void doOnboarding() {
        checkCredentials();
        WelcomePage welcomePage = new WelcomePage();
        welcomePage.clickGetStarted();

        if (Constants.ONBOARDING_TYPE == Constants.OnboardingType.DISCOVERY_SERVICE) {
            ActivationPage activationPage = new ActivationPage();
            activationPage.clickStart();
        }
        SystemClock.sleep(2000);
        AbstractLoginPage loginPage = LoginPageFactory.getLoginPage();
        loginPage.authenticate();
        PasscodePageFactory.PasscodeFlow();

        OfflineScreen offlineScreen = new OfflineScreen();
        offlineScreen.waitUntilPageIsOver();
        offlineScreen.leavePage();
    }

    public static void doOnboardingBack() {
        checkCredentials();
        WelcomePage welcomePage = new WelcomePage();
        welcomePage.clickGetStarted();

        if (Constants.ONBOARDING_TYPE == Constants.OnboardingType.DISCOVERY_SERVICE) {
            ActivationPage activationPage = new ActivationPage();
            activationPage.clickStart();
        }
        SystemClock.sleep(2000);
        AbstractLoginPage loginPage = LoginPageFactory.getLoginPage();
        loginPage.authenticate();
        PasscodePageFactory.PasscodeFlowBack();

        OfflineScreen offlineScreen = new OfflineScreen();
        offlineScreen.waitUntilPageIsOver();
        offlineScreen.leavePage();
    }

    public static void clearSessionCookies() throws InterruptedException {
        CountDownLatch clearSessionCookiesLatch = new CountDownLatch(1);
        Looper.prepare();
        CookieManager.getInstance().removeSessionCookies(value -> clearSessionCookiesLatch.countDown());
        clearSessionCookiesLatch.await(1000, TimeUnit.MILLISECONDS);
    }

    public static String getStringFromUiWithId(int resourceId) throws InterruptedException {
        final String[] uiString = new String[1];
        CountDownLatch countDownLatch = new CountDownLatch(1);
        onView(withId(resourceId)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Getting description.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TextView textView = (TextView) view;
                synchronized (uiString) {
                    uiString[0] = textView.getText().toString();
                }
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        synchronized (uiString) {
            return uiString[0];
        }
    }

    public static String getResourceString(int id) {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        return targetContext.getResources().getString(id);
    }

    public static void skipFingerprint() {
        // Get application context
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();

        // Get the current clientpolicy
        ClientPolicy clientPolicy = getClientPolicyManager().getClientPolicy(true);

        boolean isFingerprintAllowed = clientPolicy.getPasscodePolicy().allowsFingerprint();

        //is Device supports Fingerprint
        FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        if (fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints()) {
            //skip fingerprint
            if (isFingerprintAllowed == true) {
                PasscodePage.SetFingerprintPage setFingerprintPage = new PasscodePage().new SetFingerprintPage();
                setFingerprintPage.skipFingerprint();
                setFingerprintPage.leavePage();
            }
        }
    }

    public static ClientPolicyManager getClientPolicyManager() {
        return ((SAPWizardApplication)getInstrumentation().getTargetContext().getApplicationContext())
                .getClientPolicyManager();
    }

    public static void checkCredentials() {
        Assert.assertThat("Credentials are not defined in the Credentials class!", Credentials.USERNAME.trim(), not(isEmptyString()));
    }

    public static void pressBack() {
        UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.pressBack();
        SystemClock.sleep(500);
    }
}
