package com.company.bmobkotlin.mdui

import android.content.Intent
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

import com.company.bmobkotlin.R
import com.company.bmobkotlin.app.SAPWizardApplication
import com.company.bmobkotlin.logon.ClientPolicyManager
import com.company.bmobkotlin.logon.SecureStoreManager

import com.sap.cloud.mobile.onboarding.passcode.ChangePasscodeActivity
import com.sap.cloud.mobile.onboarding.passcode.EnterPasscodeSettings
import com.sap.cloud.mobile.onboarding.passcode.SetPasscodeActivity
import com.sap.cloud.mobile.onboarding.passcode.SetPasscodeSettings

import androidx.preference.ListPreference
import ch.qos.logback.classic.Level
import com.sap.cloud.mobile.foundation.logging.Logging
import android.widget.Toast
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import com.company.bmobkotlin.app.ErrorMessage
import com.company.bmobkotlin.app.ErrorHandler

/** This fragment represents the settings screen. */
class SettingsFragment : PreferenceFragmentCompat(), ClientPolicyManager.LogLevelChangeListener, Logging.UploadListener {

    private lateinit var sapWizardApplication: SAPWizardApplication
    private lateinit var secureStoreManager: SecureStoreManager
    private lateinit var clientPolicyManager: ClientPolicyManager
    private lateinit var logLevelPreference: ListPreference
    private lateinit var errorHandler: ErrorHandler

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        sapWizardApplication = activity?.application as SAPWizardApplication
        secureStoreManager = sapWizardApplication.secureStoreManager
        clientPolicyManager = sapWizardApplication.clientPolicyManager
        errorHandler = sapWizardApplication.errorHandler

        addPreferencesFromResource(R.xml.preferences)

        val logUtil = sapWizardApplication.logUtil
        logLevelPreference = findPreference(getString(R.string.log_level)) as ListPreference

        // IMPORTANT - This is where set entries...
        logLevelPreference.entries = logUtil.levelStrings
        logLevelPreference.entryValues = logUtil.levelValues
        logLevelPreference.isPersistent = true

        val logLevelStored: Level? = secureStoreManager.getWithPasscodePolicyStore { passcodePolicyStore ->
            passcodePolicyStore.getSerializable(ClientPolicyManager.KEY_CLIENT_LOG_LEVEL)
        }
        logLevelPreference.summary = logUtil.getLevelString(logLevelStored)
        logLevelPreference.value = logLevelStored?.levelInt.toString()
        logLevelPreference.setOnPreferenceChangeListener { preference, newValue ->
            // Get the new value
            val logLevel = Level.toLevel(Integer.valueOf(newValue as String))

            //Write the new value to Secure Store
            secureStoreManager.doWithPasscodePolicyStore { passcodePolicyStore ->
                passcodePolicyStore.put(ClientPolicyManager.KEY_CLIENT_LOG_LEVEL, logLevel)
            }

            // Initialize logging
            Logging.getRootLogger().level = logLevel
            preference.summary = logUtil.getLevelString(logLevel)

            true
        }
        clientPolicyManager.setLogLevelChangeListener(this)

        // Upload log
        val logUploadPreference = findPreference(getString(R.string.upload_log))
        logUploadPreference.setOnPreferenceClickListener {
            logUploadPreference.isEnabled = false
            Logging.upload()
            false
        }

        val changePassCodePreference = findPreference(getString(R.string.manage_passcode))
        if (secureStoreManager.isPasscodePolicyEnabled) {
            changePassCodePreference.setOnPreferenceClickListener {
                val intent: Intent
                if (secureStoreManager.isUserPasscodeSet) {
                    intent = Intent(this@SettingsFragment.activity, ChangePasscodeActivity::class.java)
                    val setPasscodeSettings = SetPasscodeSettings()
                    setPasscodeSettings.skipButtonText = getString(R.string.skip_passcode)
                    setPasscodeSettings.saveToIntent(intent)
                    val currentRetryCount: Int? = secureStoreManager.getWithPasscodePolicyStore { passcodePolicyStore ->
                        passcodePolicyStore.getInt(ClientPolicyManager.KEY_RETRY_COUNT)
                    }
                    val retryLimit = clientPolicyManager.getClientPolicy(false).passcodePolicy?.retryLimit
                    if (retryLimit!! <= currentRetryCount!!) {
                        var enterPasscodeSettings = EnterPasscodeSettings()
                        enterPasscodeSettings.isFinalDisabled = true
                        enterPasscodeSettings.saveToIntent(intent)
                    }
                    this@SettingsFragment.activity?.startActivity(intent)
                } else {
                    intent = Intent(this@SettingsFragment.activity, SetPasscodeActivity::class.java)
                    val setPasscodeSettings = SetPasscodeSettings()
                    setPasscodeSettings.skipButtonText = getString(R.string.skip_passcode)
                    setPasscodeSettings.saveToIntent(intent)
                    this@SettingsFragment.activity?.startActivity(intent)
                }
                false
            }
        } else {
            changePassCodePreference.isEnabled = false
            preferenceScreen.removePreference(changePassCodePreference)
        }

        // Reset App
        val resetAppPreference = findPreference(getString(R.string.reset_app))
        resetAppPreference.setOnPreferenceClickListener {
            sapWizardApplication.resetApplicationWithUserConfirmation()
            false
        }
    }

    override fun onResume() {
        super.onResume()
        Logging.addUploadListener(this)
    }

    override fun onPause() {
        super.onPause()
        Logging.removeUploadListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        clientPolicyManager.removeLogLevelChangeListener(this)
    }

    override fun logLevelChanged(level: Level) {
        logLevelPreference.callChangeListener(Integer.toString(level.levelInt))
    }

    override fun onSuccess() {
        enableLogUploadButton()
        Toast.makeText(activity, R.string.log_upload_ok, Toast.LENGTH_LONG).show()
        LOGGER.info("Log is uploaded to the server.")
    }

    override fun onError(throwable: Throwable) {
        enableLogUploadButton()
        val errorHandler = (activity?.application as SAPWizardApplication).errorHandler
        val errorCause = throwable.localizedMessage
        errorHandler.sendErrorMessage(
            ErrorMessage(
                getString(R.string.log_upload_failed),
                errorCause,
                Exception(throwable),
                false
            )
        )
        LOGGER.error("Log upload failed with error message: $errorCause")
    }

    override fun onProgress(i: Int) {
        // You could add a progress indicator and update it from here
    }

    private fun enableLogUploadButton() {
        val logUploadPreference = findPreference(getString(R.string.upload_log))
        logUploadPreference.isEnabled = true
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(SettingsFragment::class.java)
    }
}
