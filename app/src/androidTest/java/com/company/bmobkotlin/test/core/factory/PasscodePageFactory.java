package com.company.bmobkotlin.test.core.factory;

import android.os.SystemClock;

import com.company.bmobkotlin.app.SAPWizardApplication;
import com.company.bmobkotlin.logon.ClientPolicy;
import com.company.bmobkotlin.test.core.Credentials;
import com.company.bmobkotlin.test.core.Utils;
import com.company.bmobkotlin.test.pages.PasscodePage;

import static androidx.test.InstrumentationRegistry.getInstrumentation;

public class PasscodePageFactory {

    public static void PasscodeFlow() {
        checkServiceUrl();

        // Get the current clientpolicy
        ClientPolicy clientPolicy = Utils.getClientPolicyManager().getClientPolicy(true);
        // If there is a passcode policy
        if (clientPolicy.isPasscodePolicyEnabled()) {
            // Actions on the passcode Page
            PasscodePage.CreatePasscodePage createPasscodePage = new PasscodePage().new CreatePasscodePage();
            createPasscodePage.createPasscode(Credentials.PASSCODE);
            createPasscodePage.clickNext();
            createPasscodePage.leavePage();

            // Actions on the verifypasscode Page
            PasscodePage.VerifyPasscodePage verifyPasscodePage = new PasscodePage().new VerifyPasscodePage();
            verifyPasscodePage.verifyPasscode(Credentials.PASSCODE);
            verifyPasscodePage.clickNext();
            verifyPasscodePage.leavePage();

            // Skip Fingerprint
            Utils.skipFingerprint();


        } else {
            // we skip the passcode flow
        }

    }

    public static void PasscodeFlowBack() {
        checkServiceUrl();

        // Get the current clientpolicy
        ClientPolicy clientPolicy = Utils.getClientPolicyManager().getClientPolicy(true);
        // If there is a passcode policy
        if (clientPolicy.isPasscodePolicyEnabled()) {
            // Actions on the passcode Page
            PasscodePage.CreatePasscodePage createPasscodePage = new PasscodePage().new CreatePasscodePage();
            createPasscodePage.createPasscode(Credentials.PASSCODE);
            createPasscodePage.clickNext();
            createPasscodePage.leavePage();

            // Actions on the verifypasscode Page
            PasscodePage.VerifyPasscodePage verifyPasscodePage = new PasscodePage().new VerifyPasscodePage();
            verifyPasscodePage.verifyPasscode(Credentials.PASSCODE);
            verifyPasscodePage.clickBack();
            verifyPasscodePage.leavePage();
            createPasscodePage.createPasscode(Credentials.PASSCODE);
            createPasscodePage.clickNext();
            createPasscodePage.leavePage();
            verifyPasscodePage.verifyPasscode(Credentials.PASSCODE);
            verifyPasscodePage.clickNext();
            verifyPasscodePage.leavePage();

            // Skip Fingerprint
            Utils.skipFingerprint();


        } else {
            // we skip the passcode flow
        }

    }

    public static void NewPasscodeFlow() {
        checkServiceUrl();

        // Get the current clientpolicy
        Utils.getClientPolicyManager().getClientPolicy(true);

        PasscodePage.CreatePasscodePage createPasscodePage = new PasscodePage().new CreatePasscodePage();
        createPasscodePage.createPasscode(Credentials.NEWPASSCODE);
        createPasscodePage.clickSignIn();
        createPasscodePage.leavePage();

        PasscodePage.VerifyPasscodePage verifyPasscodePage = new PasscodePage().new VerifyPasscodePage();
        verifyPasscodePage.verifyPasscode(Credentials.NEWPASSCODE);
        verifyPasscodePage.clickSignIn();
        verifyPasscodePage.leavePage();
    }

    public static void NewPasscodeFlowBack() {
        checkServiceUrl();

        // Get the current clientpolicy
        Utils.getClientPolicyManager().getClientPolicy(true);

        PasscodePage.CreatePasscodePage createPasscodePage = new PasscodePage().new CreatePasscodePage();
        createPasscodePage.createPasscode(Credentials.NEWPASSCODE);
        createPasscodePage.clickSignIn();
        createPasscodePage.leavePage();

        PasscodePage.VerifyPasscodePage verifyPasscodePage = new PasscodePage().new VerifyPasscodePage();
        verifyPasscodePage.verifyPasscode(Credentials.NEWPASSCODE);
        verifyPasscodePage.clickBack();
        verifyPasscodePage.leavePage();

        createPasscodePage.createPasscode(Credentials.NEWPASSCODE);
        createPasscodePage.clickSignIn();
        createPasscodePage.leavePage();

        verifyPasscodePage.verifyPasscode(Credentials.NEWPASSCODE);
        verifyPasscodePage.clickSignIn();
        verifyPasscodePage.leavePage();
    }

    private static void checkServiceUrl() {
        // Wait for service url until one minute
        int limit = 60;
        SAPWizardApplication application = (SAPWizardApplication) getInstrumentation().getTargetContext().getApplicationContext();
        while (limit > 0 && application.getConfigurationData().getServiceUrl() == null) {
            SystemClock.sleep(1000);
            limit--;
        }
    }

}
