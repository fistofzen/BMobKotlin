package com.company.bmobkotlin.test.pages;

import android.app.Activity;
import androidx.test.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import com.company.bmobkotlin.test.core.UIElements;
import com.company.bmobkotlin.test.core.Utils;
import com.company.bmobkotlin.test.core.matcher.ToastMatcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class ErrorPage {
    private static final int WAIT_TIMEOUT = 2000;
    UiDevice device;
    Activity activity;

    public ErrorPage(Activity activity) {
        this.activity = activity;
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    public String getErrorMessage() throws InterruptedException {
        UiObject usernameField = device.findObject(new UiSelector()
                .resourceId(UIElements.ErrorScreen.messageResourceId));
        usernameField.waitForExists(WAIT_TIMEOUT);
        return Utils.getStringFromUiWithId(UIElements.ErrorScreen.messageId);
    }

    public void dismiss() {
        onView(withId(UIElements.ErrorScreen.okButton)).check(matches(isDisplayed())).perform(click());
    }

     public void checkCancelErrorToast(){
        onView(withText("Error: Authorization flow canceled by user")).inRoot(new ToastMatcher())
                .check(matches(withText("Error: Authorization flow canceled by user")));

     }
}
