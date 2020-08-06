package com.company.bmobkotlin.test.testcases.ui;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.company.bmobkotlin.app.ErrorMessage;
import com.company.bmobkotlin.app.SAPWizardApplication;
import com.company.bmobkotlin.logon.LogonActivity;
import com.company.bmobkotlin.test.core.BaseTest;
import com.company.bmobkotlin.test.pages.ErrorPage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ErrorHandlerTests extends BaseTest {

    @Rule
    public ActivityTestRule<LogonActivity> activityTestRule = new ActivityTestRule<>(LogonActivity.class);

    @Test
    public void errorDialogGetsShown() throws InterruptedException {
        String errorDetails = "This is the details of the error.";
        Exception exception = new Exception();
        ErrorMessage errorMessage = new ErrorMessage("Error Title", errorDetails, exception, false);
        ((SAPWizardApplication)getInstrumentation().getTargetContext().getApplicationContext())
                .getErrorHandler().sendErrorMessage(errorMessage);

        ErrorPage errorPage = new ErrorPage(activityTestRule.getActivity());
        assertEquals("Unexpected error message", errorDetails, errorPage.getErrorMessage());

        errorPage.dismiss();
    }

    @Test
    public void errorDialogsShownInOrder() throws InterruptedException {
        String errorDetails1 = "This is the details of the first error.";
        Exception exception = new Exception();
        ErrorMessage errorMessage1 = new ErrorMessage("Error Title", errorDetails1, exception, false);
        ((SAPWizardApplication)getInstrumentation().getTargetContext().getApplicationContext())
                .getErrorHandler().sendErrorMessage(errorMessage1);

        String errorDetails2 = "This is the details of the second error.";
        ErrorMessage errorMessage2 = new ErrorMessage("Error Title", errorDetails2, exception, false);
        ((SAPWizardApplication)getInstrumentation().getTargetContext().getApplicationContext())
                .getErrorHandler().sendErrorMessage(errorMessage2);

        ErrorPage errorPage = new ErrorPage(activityTestRule.getActivity());
        assertEquals("Unexpected error message", errorDetails1, errorPage.getErrorMessage());
        errorPage.dismiss();

        ErrorPage errorPage2 = new ErrorPage(activityTestRule.getActivity());
        assertEquals("Unexpected error message", errorDetails2, errorPage2.getErrorMessage());
        errorPage.dismiss();
    }
}
