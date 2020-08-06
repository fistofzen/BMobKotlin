package com.company.bmobkotlin.test.testcases.integration;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.company.bmobkotlin.app.ConfigurationData;
import com.company.bmobkotlin.app.ErrorHandler;
import com.company.bmobkotlin.app.SAPWizardApplication;
import com.sap.cloud.mobile.foundation.configurationprovider.DefaultPersistenceMethod;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ConfigurationDataTest {

    private static String GOOD_URL = "https://b9863320trial-dev-bmobkotlin.cfapps.eu10.hana.ondemand.com/";

    private static String SLASH_MISSING_URL = GOOD_URL.substring(0, GOOD_URL.length() - 1);

    private static String GOOD_URL_WITH_PORT = "https://mobilepreview-ad9a3f3b6.hana.ondemand.com:443/";
    private static String BAD_URL = "htxtps://mobilepreview-ad9a3f3b6.hana.ondemand.com/";
    private static String GOOD_SERVICE_URL_KEY = "ServiceUrl";
    private static String BAD_SERVICE_URL_KEY = "ServiceUrlx";
    private static String CONFIG_JSON_FORMAT = "{ \"%s\" : \"%s\" }";
    private static String CONFIG_BAD_JSON_FORMAT = "{ \"%s\"  \"%s\" }";
    private static String EMPTY_CONFIG = "{}";
    private static String GOOD_CONFIG = String.format(CONFIG_JSON_FORMAT, GOOD_SERVICE_URL_KEY, GOOD_URL);
    private static String BAD_KEY_CONFIG = String.format(CONFIG_JSON_FORMAT, BAD_SERVICE_URL_KEY, GOOD_URL);
    private static String BAD_URL_CONFIG = String.format(CONFIG_JSON_FORMAT, GOOD_SERVICE_URL_KEY, BAD_URL);
    private static String MALFORMED_CONFIG = String.format(CONFIG_BAD_JSON_FORMAT, GOOD_SERVICE_URL_KEY, GOOD_URL);
    private static String SLASH_MISSING_CONFIG = String.format(CONFIG_JSON_FORMAT, GOOD_SERVICE_URL_KEY, SLASH_MISSING_URL);
    private static String GOOD_DISC_SVC_DFLT =
            "{\n"
            + "    \"auth\": [\n"
            + "        {\n"
            + "            \"type\": \"basic.default\",\n"
            + "            \"config\": {},\n"
            + "            \"requireOtp\": false\n"
            + "        }\n"
            + "    ],\n"
            + "    \"host\": \"mobilepreview-ad9a3f3b6.hana.ondemand.com\",\n"
            + "    \"port\": 443,\n"
            + "    \"protocol\": \"https\",\n"
            + "    \"appID\": \"BMobKotlin\"\n"
            + "}\n";

    Context applicationContext;
    ErrorHandler errorHandler;

    @Before
    public void beforeEachTest() {
        SAPWizardApplication sapWizardApplication = (SAPWizardApplication)getInstrumentation().getTargetContext().getApplicationContext();
        applicationContext = sapWizardApplication;
        errorHandler = sapWizardApplication.getErrorHandler();
        ConfigurationData.resetPersistedConfiguration(sapWizardApplication);
    }

    /***
     * Validate the contents on a newly constructed ConfigurationData object
     */
    @Test
    public void newConfigurationDataTest() {
        ConfigurationData configurationData = new ConfigurationData(applicationContext, errorHandler);
        assertFalse("isLoaded", configurationData.isLoaded());
        assertNull("serviceURL", configurationData.getServiceUrl());
    }

    /***
     * Validate the ConfigurationData object behaviour when there is no persisted data
     */
    @Test
    public void loadNoConfigurationDataTest() {
        ConfigurationData configurationData = new ConfigurationData(applicationContext, errorHandler);
        configurationData.loadData();
        assertFalse("isLoaded", configurationData.isLoaded());
        assertNull("serviceURL", configurationData.getServiceUrl());
    }

    /***
     * Validate the ConfigurationData object behaviour when there is good persisted data
     */
    @Test
    public void loadGoodConfigurationTest() {
        persistConfiguration(GOOD_CONFIG);
        ConfigurationData configurationData = new ConfigurationData(applicationContext, errorHandler);
        configurationData.loadData();
        assertTrue("isLoaded", configurationData.isLoaded());
        assertEquals("serviceURL", GOOD_URL, configurationData.getServiceUrl());
    }

    /***
     * Validate the ConfigurationData object behaviour when URL ends with no "/"
     */
    @Test
    public void loadnoSlashConfigurationTest() {
        persistConfiguration(SLASH_MISSING_CONFIG);
        ConfigurationData configurationData = new ConfigurationData(applicationContext, errorHandler);
        configurationData.loadData();
        assertTrue("isLoaded", configurationData.isLoaded());
        assertEquals("serviceURL", GOOD_URL, configurationData.getServiceUrl());
    }

    /***
     * Validate the ConfigurationData object behaviour when there is good persisted data
     * in the form of the discovery service default
     */
    @Test
    public void loadGoodDiscSvcDfltConfigurationTest() {
        persistConfiguration(GOOD_DISC_SVC_DFLT);
        ConfigurationData configurationData = new ConfigurationData(applicationContext, errorHandler);
        configurationData.loadData();
        assertTrue("isLoaded", configurationData.isLoaded());
        assertEquals("serviceURL", GOOD_URL_WITH_PORT, configurationData.getServiceUrl());
    }

    /***
     * Validate the ConfigurationData object behaviour when there is empty persisted data
     */
    @Test
    public void loadEmptyConfigurationDataTest() {
        persistConfiguration(EMPTY_CONFIG);
        ConfigurationData configurationData = new ConfigurationData(applicationContext, errorHandler);
        configurationData.loadData();
        assertFalse("isLoaded", configurationData.isLoaded());
        assertNull("serviceURL", configurationData.getServiceUrl());
    }

    /***
     * Validate the ConfigurationData object behaviour when the persisted data has the wrong key
     */
    @Test
    public void loadBadKeyConfigurationDataTest() {
        persistConfiguration(BAD_KEY_CONFIG);
        ConfigurationData configurationData = new ConfigurationData(applicationContext, errorHandler);
        configurationData.loadData();
        assertFalse("isLoaded", configurationData.isLoaded());
        assertNull("serviceURL", configurationData.getServiceUrl());
    }

    /***
     * Validate the ConfigurationData object behaviour when the persisted data contains a malformed URL
     */
    @Test
    public void loadBadUrlConfigurationDataTest() {
        persistConfiguration(BAD_URL_CONFIG);
        ConfigurationData configurationData = new ConfigurationData(applicationContext, errorHandler);
        configurationData.loadData();
        assertFalse("isLoaded", configurationData.isLoaded());
        assertNull("serviceURL", configurationData.getServiceUrl());
    }

    /***
     * Validate the ConfigurationData object behaviour when the persisted data is malformed
     */
    @Test
    public void loadMalformedConfigurationDataTest() {
        persistConfiguration(MALFORMED_CONFIG);
        ConfigurationData configurationData = new ConfigurationData(applicationContext, errorHandler);
        configurationData.loadData();
        assertFalse("isLoaded", configurationData.isLoaded());
        assertNull("serviceURL", configurationData.getServiceUrl());
    }

    /***
     * Validate the ConfigurationData object behaviour after the persisted data is cleared
     * This tests the resetPersistedConfiguration method
     */
    @Test
    public void resetConfigurationTest() {
        persistConfiguration(GOOD_CONFIG);
        ConfigurationData configurationData = new ConfigurationData(applicationContext, errorHandler);
        configurationData.loadData();
        assertTrue("isLoaded", configurationData.isLoaded());
        assertEquals("serviceURL", GOOD_URL, configurationData.getServiceUrl());
        ConfigurationData.resetPersistedConfiguration(applicationContext);
        configurationData.resetData();
        configurationData.loadData();
        assertFalse("isLoaded", configurationData.isLoaded());
        assertNull("serviceURL", configurationData.getServiceUrl());
    }


    private void persistConfiguration(String configuration) {
        if (configuration != null) {
            SharedPreferences sharedPreferences = applicationContext.getSharedPreferences(DefaultPersistenceMethod.SHARED_PREFERENCES_NAME, 0);
            sharedPreferences.edit().putString(DefaultPersistenceMethod.CONFIGURATION_DATA_KEY, configuration).apply();
        }

    }

}
