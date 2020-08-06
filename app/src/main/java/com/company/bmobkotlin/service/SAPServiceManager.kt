package com.company.bmobkotlin.service

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Base64

import com.company.bmobkotlin.R
import com.company.bmobkotlin.app.ConfigurationData
import com.company.bmobkotlin.app.ErrorHandler
import com.company.bmobkotlin.app.ErrorMessage
import com.company.bmobkotlin.app.SAPWizardApplication
import com.company.bmobkotlin.offline.OfflineODataSyncService
import com.sap.cloud.android.odata.entitycontainer.EntityContainer
import com.sap.cloud.mobile.foundation.common.ClientProvider
import com.sap.cloud.mobile.foundation.common.EncryptionUtil
import com.sap.cloud.mobile.odata.core.Action0
import com.sap.cloud.mobile.odata.core.Action1
import com.sap.cloud.mobile.odata.core.AndroidSystem
import com.sap.cloud.mobile.odata.offline.OfflineODataDefiningQuery
import com.sap.cloud.mobile.odata.offline.OfflineODataException
import com.sap.cloud.mobile.odata.offline.OfflineODataParameters
import com.sap.cloud.mobile.odata.offline.OfflineODataProvider
import java.net.URL
import java.util.Arrays
import org.slf4j.LoggerFactory

/**
 * This class represents the Mobile Application backed by an OData service for offline use.
 *
 * @param [configurationData] Configuration data from Config Provider
 * @param [context] Application context for use by OfflineProvider
 * @param [errorHandler] Error Handler to report initialization errors
 */
class SAPServiceManager(
    private val configurationData: ConfigurationData,
    private val context: Context,
    private val errorHandler: ErrorHandler) {

    /*
     * Offline line OData Provider
     */
    private var provider: OfflineODataProvider? = null

    internal var application: SAPWizardApplication = context as SAPWizardApplication


    /** Service root-- OData service proxy to interact with local offline OData provider */
    var serviceRoot: String = ""
        private set
        get() {
            return configurationData.serviceUrl + CONNECTION_ID_ENTITYCONTAINER + "/"
        }


    /** OData service for interacting with local OData Provider */
    var entityContainer: EntityContainer? = null
        private set
        get() {
            return field ?: throw IllegalStateException("SAPServiceManager was not initialized")
        }

    /**
     * This call can only be made when the user is authenticated (if required) as it depends
     * on application store for encryption keys and ClientProvider
     * @return OfflineODataProvider
     */
    fun retrieveProvider(): OfflineODataProvider? {
        if (provider == null) {
            initializeOffline(false)
        }
        return provider
    }

    /*
     * Create OfflineODataProvider
     * This is a blocking call, no data will be transferred until open, download, upload
     * @param forReset true initializing the offline provider for reset purpose. This is because reset can occur
     * when we have not been onboarded.
     */
    private fun initializeOffline(forReset: Boolean) {
        AndroidSystem.setContext(context)
        var serviceUrl = configurationData.serviceUrl
        if (serviceUrl == null) {
            if (forReset) {
                serviceUrl = "http://localhost/"
            } else {
                LOGGER.error("ServerURL is null when attempting to create offline provider")
            }

        }
        try {
            val url = URL(serviceUrl!! + CONNECTION_ID_ENTITYCONTAINER)

            val offlineODataParameters = OfflineODataParameters()
            offlineODataParameters.isEnableRepeatableRequests = true
            offlineODataParameters.storeName = OFFLINE_DATASTORE
            val encryptionKeyBytes = EncryptionUtil.getEncryptionKey(OFFLINE_DATASTORE_ENCRYPTION_KEY_ALIAS)
            val key = Base64.encodeToString(encryptionKeyBytes, Base64.NO_WRAP)
            offlineODataParameters.storeEncryptionKey = key
            Arrays.fill(encryptionKeyBytes, 0.toByte())

            // Set the default application version
            val customheaders = offlineODataParameters.customHeaders
            customheaders.put(APP_VERSION_HEADER, SAPWizardApplication.APPLICATION_VERSION)
            // In case of offlineODataParameters.customHeaders returning a new object if customHeaders from offlineODataParameters is null, set again as below
            offlineODataParameters.setCustomHeaders(customheaders)

            provider = OfflineODataProvider(url, offlineODataParameters, ClientProvider.get(), null, null)
            val fileQuery = OfflineODataDefiningQuery("File", "File", false)
            provider!!.addDefiningQuery(fileQuery)
            val phoneRegistryQuery = OfflineODataDefiningQuery("PhoneRegistry", "PhoneRegistry", false)
            provider!!.addDefiningQuery(phoneRegistryQuery)
            val sayacQuery = OfflineODataDefiningQuery("Sayac", "Sayac", false)
            provider!!.addDefiningQuery(sayacQuery)


            entityContainer = EntityContainer(provider!!)

        } catch (e: Exception) {
            LOGGER.error("Exception encountered setting up offline store: " + e.message)
            val res = context.resources
            val errorMessage = ErrorMessage(res.getString(R.string.offline_provider_error),
                res.getString(R.string.offline_provider_error_detail))
            errorHandler.sendErrorMessage(errorMessage)
        }
    }

    /**
     * Synchronize local offline data store with Server
     * Upload - local changes
     * Download - server changes
     * @param syncService
     * @param syncSuccessHandler
     * @param syncFailureHandler
     */
    fun synchronize(syncService: OfflineODataSyncService, syncSuccessHandler: Action0, syncFailureHandler: Action1<OfflineODataException>) {
        syncService.uploadStore(provider!!,
            Action0 {
                syncService.downloadStore(provider!!,
                    Action0 {
                        application.repositoryFactory.reset()
                        syncSuccessHandler.call()
                    },
                    Action1 { error ->
                        application.repositoryFactory.reset()
                        LOGGER.error("Exception encountered uploading from local store: " + error.message)
                        syncFailureHandler.call(error)
                    })
            },
            Action1 { error ->
                application.repositoryFactory.reset()
                LOGGER.error("Exception encountered downloading to local store: " + error.message)
                syncFailureHandler.call(error)
            })
    }

    /*
     * Close and remove offline data store
     */
    fun reset() {
        try {
            if (provider == null) {
                initializeOffline(true)
            }
            provider!!.clear()
            provider = null
        } catch (e: OfflineODataException) {
            LOGGER.error("Unable to reset Offline Data Store. Encountered exception: " + e.message)
            val res = context.resources
            val errorMessage = ErrorMessage(res.getString(R.string.offline_reset_store_error),
            res.getString(R.string.offline_reset_store_error_detail))
            errorHandler.sendErrorMessage(errorMessage)
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().clear().commit()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SAPServiceManager::class.java)

        /* Name of the offline data file on the application file space */
        private const val OFFLINE_DATASTORE = "OfflineDataStore"
        private const val OFFLINE_DATASTORE_ENCRYPTION_KEY_ALIAS = "Offline_DataStore_EncryptionKey_Alias"

        /* Header name for application version */
        private const val APP_VERSION_HEADER = "X-APP-VERSION"

        /*
         * Connection ID of Mobile Application
         */
        const val CONNECTION_ID_ENTITYCONTAINER = "BMobKotlin"
    }
}
