package com.company.bmobkotlin.logon

import android.content.Context

import com.sap.cloud.mobile.foundation.authentication.OAuth2Configuration

/**
 * This class provides the OAuth configuration object for the application.
 *
 */
object SAPOAuthConfigProvider {

    private val OAUTH_REDIRECT_URL = "https://b9863320trial-dev-bmobkotlin.cfapps.eu10.hana.ondemand.com"
    private val OAUTH_CLIENT_ID = "96d9c71a-fdc1-4061-aea3-9a48c464f85d"
    private val AUTH_END_POINT = "https://b9863320trial-dev-bmobkotlin.cfapps.eu10.hana.ondemand.com/oauth2/api/v1/authorize"
    private val TOKEN_END_POINT = "https://b9863320trial-dev-bmobkotlin.cfapps.eu10.hana.ondemand.com/oauth2/api/v1/token"

    @JvmStatic fun getOAuthConfiguration(context: Context): OAuth2Configuration {

        return OAuth2Configuration.Builder(context)
            .clientId(OAUTH_CLIENT_ID)
            .responseType("code")
            .authUrl(AUTH_END_POINT)
            .tokenUrl(TOKEN_END_POINT)
            .redirectUrl(OAUTH_REDIRECT_URL)
            .build()
    }


}
