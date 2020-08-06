package com.company.bmobkotlin.mdui.phoneregistry

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import com.company.bmobkotlin.R
import com.company.bmobkotlin.app.ErrorHandler
import com.company.bmobkotlin.app.ErrorMessage
import com.company.bmobkotlin.app.SAPWizardApplication
import com.company.bmobkotlin.databinding.FragmentPhoneregistryCreateBinding
import com.company.bmobkotlin.mdui.BundleKeys
import com.company.bmobkotlin.mdui.InterfacedFragment
import com.company.bmobkotlin.mdui.UIConstants
import com.company.bmobkotlin.repository.OperationResult
import com.company.bmobkotlin.viewmodel.phoneregistry.PhoneRegistryViewModel
import com.sap.cloud.android.odata.entitycontainer.PhoneRegistry
import com.sap.cloud.android.odata.entitycontainer.EntityContainerMetadata.EntityTypes
import com.sap.cloud.android.odata.entitycontainer.EntityContainerMetadata.EntitySets
import com.sap.cloud.mobile.fiori.formcell.SimplePropertyFormCell
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader
import com.sap.cloud.mobile.odata.Property
import org.slf4j.LoggerFactory

/**
 * A fragment that is used for both update and create for users to enter values for the properties. When used for
 * update, an instance of the entity is required. In the case of create, a new instance of the entity with defaults will
 * be created. The default values may not be acceptable for the OData service.
 * This fragment is either contained in a [PhoneRegistryListActivity] in two-pane mode (on tablets) or a
 * [PhoneRegistryDetailActivity] on handsets.
 *
 * Arguments: Operation: [OP_CREATE | OP_UPDATE]
 *            PhoneRegistry if Operation is update
 */
class PhoneRegistryCreateFragment : InterfacedFragment<PhoneRegistry>() {

    /** PhoneRegistry object and it's copy: the modifications are done on the copied object. */
    private lateinit var phoneRegistryEntity: PhoneRegistry
    private lateinit var phoneRegistryEntityCopy: PhoneRegistry

    /** DataBinding generated class */
    private lateinit var binding: FragmentPhoneregistryCreateBinding

    /** Indicate what operation to be performed */
    private lateinit var operation: String

    /** phoneRegistryEntity ViewModel */
    private lateinit var viewModel: PhoneRegistryViewModel

    /** The update menu item */
    private lateinit var updateMenuItem: MenuItem

    /** Application error handler to report error */
    private lateinit var errorHandler: ErrorHandler

    private val isPhoneRegistryValid: Boolean
        get() {
            var isValid = true
            view?.findViewById<LinearLayout>(R.id.create_update_phoneregistry)?.let { linearLayout ->
                for (i in 0 until linearLayout.childCount) {
                    val simplePropertyFormCell = linearLayout.getChildAt(i) as SimplePropertyFormCell
                    val propertyName = simplePropertyFormCell.tag as String
                    val property = EntityTypes.phoneRegistry.getProperty(propertyName)
                    val value = simplePropertyFormCell.value.toString()
                    if (!isValidProperty(property, value)) {
                        simplePropertyFormCell.setTag(R.id.TAG_HAS_MANDATORY_ERROR, true)
                        val errorMessage = resources.getString(R.string.mandatory_warning)
                        simplePropertyFormCell.isErrorEnabled = true
                        simplePropertyFormCell.error = errorMessage
                        isValid = false
                    } else {
                        if (simplePropertyFormCell.isErrorEnabled) {
                            val hasMandatoryError = simplePropertyFormCell.getTag(R.id.TAG_HAS_MANDATORY_ERROR) as Boolean
                            if (!hasMandatoryError) {
                                isValid = false
                            } else {
                                simplePropertyFormCell.isErrorEnabled = false
                            }
                        }
                        simplePropertyFormCell.setTag(R.id.TAG_HAS_MANDATORY_ERROR, false)
                    }
                }
            }
            return isValid
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menu = R.menu.itemlist_edit_options
        errorHandler = (currentActivity.application as SAPWizardApplication).errorHandler
        setHasOptionsMenu(true)

        arguments?.let {
            (it.getString(BundleKeys.OPERATION))?.let { operationType ->
                operation = operationType
                activityTitle = when (operationType) {
                    UIConstants.OP_CREATE -> resources.getString(R.string.title_create_fragment, EntityTypes.phoneRegistry.localName)
                    else -> resources.getString(R.string.title_update_fragment) + " " + EntityTypes.phoneRegistry.localName

                }
            }
        }

        activity?.let {
            (it as PhoneRegistryActivity).isNavigationDisabled = true
            viewModel = ViewModelProviders.of(it).get(PhoneRegistryViewModel::class.java)
            viewModel.createResult.observe(this, Observer { result -> onComplete(result!!) })
            viewModel.updateResult.observe(this, Observer { result -> onComplete(result!!) })

            if (operation == UIConstants.OP_CREATE) {
                phoneRegistryEntity = createPhoneRegistry()
            } else {
                phoneRegistryEntity = viewModel.selectedEntity.value!!
            }

            val workingCopy = savedInstanceState?.getParcelable<PhoneRegistry>(KEY_WORKING_COPY)
            if (workingCopy == null) {
                phoneRegistryEntityCopy = phoneRegistryEntity.copy() as PhoneRegistry
                phoneRegistryEntityCopy.setEntityTag(phoneRegistryEntity.getEntityTag())
                phoneRegistryEntityCopy.setOldEntity(phoneRegistryEntity)
                phoneRegistryEntityCopy.editLink = phoneRegistryEntity.editLink
            } else {
                phoneRegistryEntityCopy = workingCopy
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currentActivity.findViewById<ObjectHeader>(R.id.objectHeader)?.let {
            it.visibility = View.GONE
        }
        val rootView = setupDataBinding(inflater, container)
        return rootView
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save_item -> {
                updateMenuItem = item
                enableUpdateMenuItem(false)
                onSaveItem()
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(secondaryToolbar != null) secondaryToolbar!!.setTitle(activityTitle) else activity?.setTitle(activityTitle)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_WORKING_COPY, phoneRegistryEntityCopy)
        super.onSaveInstanceState(outState)
    }

    /** Enables the update menu item based on [enable] */
    private fun enableUpdateMenuItem(enable : Boolean = true) {
        updateMenuItem.also {
            it.isEnabled = enable
            it.icon.alpha = if(enable) 255 else 130
        }
    }

    /** Saves the entity */
    private fun onSaveItem(): Boolean {
        if (!isPhoneRegistryValid) {
            return false
        }
        (currentActivity as PhoneRegistryActivity).isNavigationDisabled = false
        progressBar?.visibility = View.VISIBLE
        when (operation) {
            UIConstants.OP_CREATE -> {
                viewModel.create(phoneRegistryEntityCopy)
            }
            UIConstants.OP_UPDATE -> viewModel.update(phoneRegistryEntityCopy)
        }
        return true
    }

    /**
     * Create a new PhoneRegistry instance and initialize properties to its default values
     * Nullable property will remain null
     * For offline, keys will be unset to avoid collision should more than one is created locally
     * @return new PhoneRegistry instance
     */
    private fun createPhoneRegistry(): PhoneRegistry {
        val entity = PhoneRegistry(true)
        entity.unsetDataValue(PhoneRegistry.id)
        return entity
    }

    /** Callback function to complete processing when updateResult or createResult events fired */
    private fun onComplete(result: OperationResult<PhoneRegistry>) {
        progressBar?.visibility = View.INVISIBLE
        enableUpdateMenuItem(true)
        if (result.error != null) {
            (currentActivity as PhoneRegistryActivity).isNavigationDisabled = true
            handleError(result)
        } else {
            if (operation == UIConstants.OP_UPDATE && !currentActivity.resources.getBoolean(R.bool.two_pane)) {
                viewModel.selectedEntity.value = phoneRegistryEntityCopy
            }
            if (currentActivity.resources.getBoolean(R.bool.two_pane)) {
                val listFragment = currentActivity.supportFragmentManager.findFragmentByTag(UIConstants.LIST_FRAGMENT_TAG)
                (listFragment as PhoneRegistryListFragment).refreshListData()
            }
            (currentActivity as PhoneRegistryActivity).onBackPressed()
        }
    }

    /** Simple validation: checks the presence of mandatory fields. */
    private fun isValidProperty(property: Property, value: String): Boolean {
        return !(!property.isNullable && value.isEmpty())
    }

    /**
     * Set up data binding for this view
     *
     * @param [inflater] layout inflater from onCreateView
     * @param [container] view group from onCreateView
     *
     * @return rootView from generated data binding code
     */
    private fun setupDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentPhoneregistryCreateBinding.inflate(inflater, container, false)
        binding.setPhoneRegistry(phoneRegistryEntityCopy)
        return binding.root
    }

    /**
     * Notify user of error encountered while execution the operation
     *
     * @param [result] operation result with error
     */
    private fun handleError(result: OperationResult<PhoneRegistry>) {
        val errorMessage = when (result.operation) {
            OperationResult.Operation.UPDATE -> ErrorMessage(resources.getString(R.string.update_failed),
                    resources.getString(R.string.update_failed_detail), result.error, false)
            OperationResult.Operation.CREATE -> ErrorMessage(resources.getString(R.string.create_failed),
                    resources.getString(R.string.create_failed_detail), result.error, false)
            else -> throw AssertionError()
        }
        errorHandler.sendErrorMessage(errorMessage)
    }


    companion object {
        private val KEY_WORKING_COPY = "WORKING_COPY"
        private val LOGGER = LoggerFactory.getLogger(PhoneRegistryActivity::class.java)
    }
}
