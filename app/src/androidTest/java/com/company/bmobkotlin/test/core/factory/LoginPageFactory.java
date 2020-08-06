package com.company.bmobkotlin.test.core.factory;

import androidx.annotation.NonNull;

import com.company.bmobkotlin.test.core.AbstractLoginPage;
import com.company.bmobkotlin.test.pages.LoginPage;

import static com.company.bmobkotlin.test.core.Constants.APPLICATION_AUTH_TYPE;

public class LoginPageFactory {

    @NonNull
    public static AbstractLoginPage getLoginPage() {

        switch (APPLICATION_AUTH_TYPE) {
            case BASIC:
                return new LoginPage.BasicAuthPage();
            case OAUTH:
                return new LoginPage.WebviewPage();
            case SAML:
                return new LoginPage.WebviewPage();
            case NOAUTH:
                return new LoginPage.NoAuthPage();
            default:
                return new LoginPage.NoAuthPage();
        }
    }
}
