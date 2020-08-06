package com.company.bmobkotlin.test.testcases.ui;

import android.os.SystemClock;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import static androidx.test.InstrumentationRegistry.getInstrumentation;

import com.company.bmobkotlin.app.SAPWizardApplication;
import com.company.bmobkotlin.logon.ClientPolicyManager;
import com.company.bmobkotlin.logon.LogonActivity;
import com.company.bmobkotlin.test.core.BaseTest;
import com.company.bmobkotlin.test.core.Credentials;
import com.company.bmobkotlin.test.core.UIElements;
import com.company.bmobkotlin.test.core.Utils;
import com.company.bmobkotlin.test.core.WizardDevice;
import com.company.bmobkotlin.test.core.factory.PasscodePageFactory;
import com.company.bmobkotlin.test.pages.EntityListPage;
import com.company.bmobkotlin.test.pages.MasterPage;
import com.company.bmobkotlin.test.pages.PasscodePage;
import com.company.bmobkotlin.test.pages.SettingsListPage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PasscodeTests extends BaseTest {

    @Rule
    public ActivityTestRule<LogonActivity> activityTestRule = new ActivityTestRule<>(LogonActivity.class);

    @Test
    public void testPasscodeLockTimeOut() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        EntityListPage entityListPage = new EntityListPage(UIElements.EntityListScreen.entityList);
        // entityListPage.clickFirstElement();
        // entityListPage.leavePage();

        MasterPage masterPage = new MasterPage();

        // Get the lockTimeOut (in seconds) from the SecureStoreManager
        int lockTimeOut = ((SAPWizardApplication) getInstrumentation().getTargetContext().getApplicationContext())
                .getSecureStoreManager().getPasscodeLockTimeout();

        // We put the app into background
        WizardDevice.putApplicationBackground(3000, activityTestRule);

        // We reopen the app
        WizardDevice.reopenApplication();
        SystemClock.sleep(1000);

        // Put and reopen the app
        WizardDevice.putApplicationBackground((lockTimeOut + 1) * 1000, activityTestRule);
        WizardDevice.reopenApplication();

        // We should arrive in the Enter Passcode Page
        PasscodePage.EnterPasscodePage enterPasscodePage = new PasscodePage().new EnterPasscodePage();
        enterPasscodePage.enterPasscode(Credentials.PASSCODE);
        enterPasscodePage.clickSignIn();
        enterPasscodePage.leavePage();

        // Go Back from the Master page
        masterPage.clickBack();
        masterPage.leavePage();

        // We should arrive in the EntityListPage
        entityListPage.clickSettings();
        entityListPage.leavePage();

        SettingsListPage settingsListPage = new SettingsListPage(UIElements.SettingsScreen.settingsList);
        settingsListPage.clickResetApp();
        settingsListPage.checkConfirmationDialog();
        settingsListPage.leavePage();
    }

    @Test
    public void testManagePasscodeBackground() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        EntityListPage entityListPage = new EntityListPage(UIElements.EntityListScreen.entityList);

        int lockTimeOut = ((SAPWizardApplication) getInstrumentation().getTargetContext().getApplicationContext())
                .getSecureStoreManager().getPasscodeLockTimeout();

        WizardDevice.putApplicationBackground((lockTimeOut + 1) * 1000, activityTestRule);
        WizardDevice.reopenApplication();

        PasscodePage.EnterPasscodePage enterPasscodePage = new PasscodePage().new EnterPasscodePage();
        enterPasscodePage.enterPasscode(Credentials.PASSCODE);
        enterPasscodePage.clickSignIn();
        enterPasscodePage.leavePage();

        MasterPage masterPage = new MasterPage();
        masterPage.clickBack();
        masterPage.leavePage();

        entityListPage.clickSettings();
        entityListPage.leavePage();

        SettingsListPage settingsListPage = new SettingsListPage(UIElements.SettingsScreen.settingsList);
        settingsListPage.clickManagePasscode();
        settingsListPage.leavePage();

        enterPasscodePage.enterPasscode(Credentials.PASSCODE);
        enterPasscodePage.clickSecondNextButton();
        enterPasscodePage.leavePage();

        PasscodePageFactory.NewPasscodeFlow();

        WizardDevice.putApplicationBackground((lockTimeOut + 1) * 1000, activityTestRule);
        WizardDevice.reopenApplication();

        enterPasscodePage.enterPasscode(Credentials.NEWPASSCODE);
        enterPasscodePage.clickSignIn();
        enterPasscodePage.leavePage();

        settingsListPage.clickResetApp();
        settingsListPage.checkConfirmationDialog();
        settingsListPage.clickYes();
    }

    @Test
    public void testManagePasscodeCancelBackground() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        EntityListPage entityListPage = new EntityListPage(UIElements.EntityListScreen.entityList);
        entityListPage.clickSettings();
        entityListPage.leavePage();

        SettingsListPage settingsListPage = new SettingsListPage(UIElements.SettingsScreen.settingsList);
        settingsListPage.clickManagePasscode();

        PasscodePage.EnterPasscodePage enterPasscodePage = new PasscodePage().new EnterPasscodePage();
        enterPasscodePage.enterPasscode(Credentials.PASSCODE);
        enterPasscodePage.clickCancel();

        int lockTimeOut = ((SAPWizardApplication) getInstrumentation().getTargetContext().getApplicationContext())
                .getSecureStoreManager().getPasscodeLockTimeout();

        WizardDevice.putApplicationBackground((lockTimeOut + 1) * 1000, activityTestRule);
        WizardDevice.reopenApplication();

        enterPasscodePage.enterPasscode(Credentials.PASSCODE);
        enterPasscodePage.clickSignIn();

        settingsListPage.clickResetApp();
        settingsListPage.checkConfirmationDialog();
        settingsListPage.clickYes();
    }

    @Test
    public void testManagePasscodeDefaultBackground() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        EntityListPage entityListPage = new EntityListPage(UIElements.EntityListScreen.entityList);
        // entityListPage.clickFirstElement();

        SAPWizardApplication sapWizardApplication = (SAPWizardApplication) getInstrumentation().getTargetContext().getApplicationContext();
        int lockTimeOut = sapWizardApplication.getSecureStoreManager().getPasscodeLockTimeout();

        WizardDevice.putApplicationBackground((lockTimeOut + 1) * 1000, activityTestRule);
        WizardDevice.reopenApplication();

        PasscodePage.EnterPasscodePage enterPasscodePage = new PasscodePage().new EnterPasscodePage();
        enterPasscodePage.enterPasscode(Credentials.PASSCODE);
        enterPasscodePage.clickSignIn();
        enterPasscodePage.leavePage();

        MasterPage masterPage = new MasterPage();
        masterPage.clickBack();
        masterPage.leavePage();

        entityListPage.clickSettings();
        entityListPage.leavePage();

        SettingsListPage settingsListPage = new SettingsListPage(UIElements.SettingsScreen.settingsList);
        settingsListPage.clickManagePasscode();
        settingsListPage.leavePage();

        enterPasscodePage.enterPasscode(Credentials.PASSCODE);
        enterPasscodePage.clickSecondNextButton();
        enterPasscodePage.leavePage();

        PasscodePageFactory.NewPasscodeFlow();

        WizardDevice.putApplicationBackground((lockTimeOut + 1) * 1000, activityTestRule);
        WizardDevice.reopenApplication();

        enterPasscodePage.enterPasscode(Credentials.NEWPASSCODE);
        enterPasscodePage.clickSignIn();
        enterPasscodePage.leavePage();

        settingsListPage.clickResetApp();
        settingsListPage.clickYes();
    }


    @Test
    public void testPasscodeRetryLimitBackground() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        // Get the lockTimeOut (in seconds) from the SecureStoreManager
        int lockTimeOut = ((SAPWizardApplication) getInstrumentation().getTargetContext().getApplicationContext())
                .getSecureStoreManager().getPasscodeLockTimeout();

        // We put the app into background
        WizardDevice.putApplicationBackground((lockTimeOut + 1) * 1000, activityTestRule);

        // We reopen the app
        WizardDevice.reopenApplication();

        // Try the retry limit flow
        PasscodePage.EnterPasscodePage enterPasscodePage = new PasscodePage().new EnterPasscodePage();
        ClientPolicyManager clientPolicyManager = ((SAPWizardApplication) getInstrumentation().getTargetContext().getApplicationContext())
                .getClientPolicyManager();
        for (int i = 0; i < clientPolicyManager.getClientPolicy(false).getPasscodePolicy().getRetryLimit(); i++) {
            enterPasscodePage.enterPasscode(Credentials.WRONGPASSCODE);
            enterPasscodePage.clickSignIn();
        }
        enterPasscodePage.clickResetAppButton();
    }

    @Test
    public void testManagePasscodeRetryLimitBack() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        EntityListPage entityListPage = new EntityListPage(UIElements.EntityListScreen.entityList);
        entityListPage.clickSettings();
        entityListPage.leavePage();

        SettingsListPage settingsListPage = new SettingsListPage(UIElements.SettingsScreen.settingsList);
        settingsListPage.clickManagePasscode();
        settingsListPage.leavePage();

        // Try the retry limit flow
        PasscodePage.EnterPasscodePage enterPasscodePage = new PasscodePage().new EnterPasscodePage();
        ClientPolicyManager clientPolicyManager = ((SAPWizardApplication) getInstrumentation().getTargetContext().getApplicationContext())
                .getClientPolicyManager();
        int retryLimit = clientPolicyManager.getClientPolicy(false).getPasscodePolicy().getRetryLimit();
        for (int i = 0; i < retryLimit; i++) {
            enterPasscodePage.enterPasscode(Credentials.WRONGPASSCODE);
            enterPasscodePage.clickSecondNextButton();
        }
        Utils.pressBack();
        Utils.pressBack();
        enterPasscodePage.leavePage();

        settingsListPage.clickManagePasscode();

        enterPasscodePage.clickResetAppButton();
        }

    @Test
    public void testSetPasscodeBack() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboardingBack();

        EntityListPage entityListPage = new EntityListPage(UIElements.EntityListScreen.entityList);
        entityListPage.clickSettings();
        entityListPage.leavePage();

        SettingsListPage settingsListPage = new SettingsListPage(UIElements.SettingsScreen.settingsList);
        settingsListPage.clickManagePasscode();
        settingsListPage.leavePage();

        PasscodePage.EnterPasscodePage enterPasscodePage = new PasscodePage().new EnterPasscodePage();

        enterPasscodePage.enterPasscode(Credentials.PASSCODE);
        enterPasscodePage.clickSecondNextButton();
        enterPasscodePage.leavePage();

        PasscodePageFactory.NewPasscodeFlowBack();
    }

    @Test
    public void testEnterPasscodeBack() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        EntityListPage entityListPage = new EntityListPage(UIElements.EntityListScreen.entityList);

        // Get the lockTimeOut (in seconds) from the SecureStoreManager
        int lockTimeOut = ((SAPWizardApplication) getInstrumentation().getTargetContext().getApplicationContext())
                .getSecureStoreManager().getPasscodeLockTimeout();

        // We put the app into background
        WizardDevice.putApplicationBackground((lockTimeOut + 1) * 1000,activityTestRule);

        // We reopen the app
        WizardDevice.reopenApplication();

        PasscodePage.EnterPasscodePage enterPasscodePage = new PasscodePage().new EnterPasscodePage();
        Utils.pressBack();
        enterPasscodePage.leavePage();
        SystemClock.sleep(500);
        // We reopen the app
        WizardDevice.reopenApplication();
        SystemClock.sleep(500);
        enterPasscodePage.enterPasscode(Credentials.PASSCODE);
        enterPasscodePage.clickSignIn();
        SystemClock.sleep(500);
        enterPasscodePage.leavePage();

        entityListPage.clickSettings();
        entityListPage.leavePage();

        SettingsListPage settingsListPage = new SettingsListPage(UIElements.SettingsScreen.settingsList);
        settingsListPage.clickResetApp();
        settingsListPage.checkConfirmationDialog();
        settingsListPage.clickYes();
    }
}
