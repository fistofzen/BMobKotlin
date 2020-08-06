package com.company.bmobkotlin.app

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.provider.Settings
import android.webkit.CookieManager

import com.company.bmobkotlin.R
import com.company.bmobkotlin.logon.ClientPolicyManager
import com.company.bmobkotlin.logon.LogonActivity
import com.company.bmobkotlin.logon.SecureStoreManager
import com.company.bmobkotlin.service.SAPServiceManager
import com.sap.cloud.mobile.foundation.authentication.AppLifecycleCallbackHandler
import com.sap.cloud.mobile.foundation.common.ClientProvider
import com.sap.cloud.mobile.foundation.common.SettingsProvider
import com.sap.cloud.mobile.foundation.common.SettingsParameters
import com.sap.cloud.mobile.foundation.configurationprovider.ConfigurationLoader
import com.sap.cloud.mobile.foundation.networking.AppHeadersInterceptor
import com.sap.cloud.mobile.foundation.networking.WebkitCookieJar
import com.sap.cloud.mobile.foundation.mobileservices.MobileService
import com.sap.cloud.mobile.foundation.mobileservices.MobileServices
import com.company.bmobkotlin.repository.RepositoryFactory
import com.company.bmobkotlin.logon.SAPOAuthConfigProvider
import com.company.bmobkotlin.logon.SAPOAuthTokenStore
import com.sap.cloud.mobile.foundation.authentication.OAuth2Configuration
import com.sap.cloud.mobile.foundation.authentication.OAuth2Interceptor
import com.sap.cloud.mobile.foundation.authentication.OAuth2WebViewProcessor
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Level
import com.sap.cloud.mobile.foundation.logging.Logging
import com.sap.cloud.mobile.foundation.logging.LogService
import com.sap.cloud.mobile.foundation.settings.SettingService;
import com.sap.cloud.mobile.foundation.settings.Settings.SettingTarget;
import java.net.MalformedURLException

/**
 * This class extends the [Application] class. Its purpose is to configure application-wide services such as error
 * handling and data access and provide access to them. It maintains an [ActivityLifecycleCallbacks] instance, as well.
 * By extending the callback's default implementation the application will be able to react on lifecycle events of the
 * contained activities.
 */
class SAPWizardApplication: Application() {

    /** Manages and provides access to OData stores providing data for the app. */
    lateinit var sapServiceManager: SAPServiceManager
        private set

    /** Manages and provides access to secure key-value-stores used to persist settings and user data. */
    lateinit var secureStoreManager: SecureStoreManager
        private set

    /**
     * Manages and provides access to local and server-provided client policies, including but not limited to passcode
     * requirements, retry count during unlocking etc.
     */
    lateinit var clientPolicyManager: ClientPolicyManager
        private set

    /** Global error handler displaying error messages to the user */
    lateinit var errorHandler: ErrorHandler
        private set

    /** Lifecycle observer, listens for foreground-background state changes. */
    private lateinit var sapWizardLifecycleObserver: SAPWizardLifecycleObserver

    /**
     * Utility class for Log Level
     */
    lateinit var logUtil: LogUtil
        private set

    /** Persistent credential store for [OAuth2Interceptor], which authenticates HTTP sessions. */
    lateinit var oauthTokenStore: SAPOAuthTokenStore
        private set


    /** Provides access to locally persisted configuration that is loaded via [ConfigurationLoader]. */
    lateinit var configurationData: ConfigurationData
        private set

    /** Application-wide RepositoryFactory */
    lateinit var repositoryFactory: RepositoryFactory
        private set

    /** The Android device ID*/    
    val deviceId: String by lazy {
        Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    }

    var isOnboarded: Boolean
        set(value) {
            secureStoreManager.isOnboarded = value
        }
        get() = secureStoreManager.isOnboarded

    override fun onCreate() {
        super.onCreate()
        startErrorHandler()
        registerActivityLifecycleCallbacks(AppLifecycleCallbackHandler.getInstance())
        secureStoreManager = SecureStoreManager(this)
        configurationData = ConfigurationData(this, errorHandler)
        oauthTokenStore = SAPOAuthTokenStore(secureStoreManager);

        if (isOnboarded) {
            configurationData.loadData()
            initSettingsParameters()
        }
        initHttpClient()
        if (isOnboarded) {
            initServices()
        }  

        sapServiceManager = SAPServiceManager(configurationData, applicationContext, errorHandler)
        clientPolicyManager = ClientPolicyManager(this)
        sapWizardLifecycleObserver = SAPWizardLifecycleObserver(secureStoreManager)

        repositoryFactory = RepositoryFactory(sapServiceManager)
    }

    internal fun initServices() {
        val services = arrayListOf<Class<out MobileService>>()

        logUtil = LogUtil(this)
        Logging.setConfigurationBuilder(Logging.ConfigurationBuilder().initialLevel(Level.WARN).logToConsole(true).build())
        services.add(LogService::class.java)
        SettingService.setSettingsChangeListener(SettingTarget.APPLICATION) { settings ->
            clientPolicyManager.setClientPolicy(settings,secureStoreManager)
        }
        services.add(SettingService::class.java)
        MobileServices.start(this, ClientProvider.get(), SettingsProvider.get(), * services.toTypedArray())
    }

    private fun initHttpClient() {
        val oAuth2Configuration = SAPOAuthConfigProvider.getOAuthConfiguration(this);

        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(OAuth2Interceptor(OAuth2WebViewProcessor(oAuth2Configuration), oauthTokenStore))
                .addInterceptor(AppHeadersInterceptor(APPLICATION_ID, deviceId, APPLICATION_VERSION))
                .cookieJar(WebkitCookieJar())
                .connectTimeout(30, TimeUnit.SECONDS)
                .build()
        ClientProvider.set(okHttpClient)
    }

    /**
    * Configures the shared application parameter object, including the application ID, version and Mobile Services
    * URL.
    */
    fun initSettingsParameters() {
        try {
            val serviceUrl = configurationData.serviceUrl ?: ""
            val sp = SettingsParameters(serviceUrl, APPLICATION_ID, deviceId, APPLICATION_VERSION)
            SettingsProvider.set(sp)
        } catch (e: MalformedURLException){
            errorHandler.sendErrorMessage(ErrorMessage(
                resources.getString(R.string.configuration_invalid),
                String.format(resources.getString(R.string.configuration_contained_malformed_url), e.message),
                e,
                false
            ))
        }
    }

    /** Creates a global error handler shared by all app components and starts its background thread. */
    private fun startErrorHandler() {
        errorHandler = ErrorHandler( "SAPWizardErrorHandler" )
        errorHandler.presenter = ErrorPresenterByNotification(this)
        errorHandler.start()
    }

    /**
     * Clears all user-specific data and configuration from the application, essentially resetting it to its initial
     * state. Restarting the application at the end.
     *
     * @param [activity] Activity from which the request originates
     */
    fun resetApplication(activity: Activity) {
        isOnboarded = false

        clientPolicyManager.resetLogLevelChangeListener()

        secureStoreManager.resetStores()
        configurationData.resetConfigurations(applicationContext)
        clearCookies(activity)
        repositoryFactory.reset()
        sapServiceManager.reset()
        restartApplication(activity)
    }

    /**
     * Asks confirmation from the user if the application data should be reset, and resets the app if the user confirms
     * the prompt.
     */
    fun resetApplicationWithUserConfirmation() {
        val activity = AppLifecycleCallbackHandler.getInstance().activity!!
        Intent(activity, ResetApplicationActivity::class.java).run {
            this.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            activity.startActivity(this)
        }
    }

    /**
     * Clears all cookies, making sure no sessions remain in the HTTP client.
     *
     * @param [activity] Activity from which the request originates
     */
    private fun clearCookies(activity: Activity) {
        val webkitCookieManager = CookieManager.getInstance()

        activity.runOnUiThread {
            webkitCookieManager.removeAllCookies {  success ->
                if (success!!) {
                    LOGGER.info("Cookies are deleted.")
                } else {
                    LOGGER.error("Cookies couldn't be removed!")
                }
            }
        }
    }

    /**
     * Restarts the application by presenting the logon screen.
     *
     * @param [activity] Activity from which the request originates
     */
    private fun restartApplication(activity: Activity) {
        val intent = Intent(activity, LogonActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SAPWizardApplication::class.java)

        /** ID of the Mobile Services endpoint configured for this application. */
        const val APPLICATION_ID = "BMobKotlin"

        /** Application version sent to Mobile Services, which may be used to control access from outdated clients. */
        const val APPLICATION_VERSION = "1.0"
    }
}
