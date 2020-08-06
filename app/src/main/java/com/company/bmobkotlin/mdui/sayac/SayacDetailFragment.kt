package com.company.bmobkotlin.mdui.sayac

import androidx.lifecycle.Observer
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import android.widget.ImageView
import com.sap.cloud.mobile.odata.core.Action1
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import com.company.bmobkotlin.R
import com.company.bmobkotlin.app.ErrorHandler
import com.company.bmobkotlin.app.ErrorMessage
import com.company.bmobkotlin.app.SAPWizardApplication
import com.company.bmobkotlin.databinding.FragmentSayacDetailBinding
import com.company.bmobkotlin.mdui.BundleKeys
import com.company.bmobkotlin.mdui.EntityKeyUtil
import com.company.bmobkotlin.mdui.InterfacedFragment
import com.company.bmobkotlin.mdui.UIConstants
import com.company.bmobkotlin.repository.OperationResult
import com.company.bmobkotlin.viewmodel.sayac.SayacViewModel
import com.sap.cloud.android.odata.entitycontainer.EntityContainerMetadata.EntitySets;
import com.sap.cloud.android.odata.entitycontainer.Sayac
import com.sap.cloud.mobile.fiori.indicator.FioriProgressBar
import com.sap.cloud.mobile.fiori.`object`.ObjectHeader
import kotlinx.android.synthetic.main.activity_entityitem.view.*

import com.company.bmobkotlin.mdui.phoneregistry.PhoneRegistryActivity
import com.company.bmobkotlin.mdui.file.FileActivity

/**
 * A fragment representing a single Sayac detail screen.
 * This fragment is contained in an SayacActivity.
 */
class SayacDetailFragment : InterfacedFragment<Sayac>() {

    /** Generated data binding class based on layout file */
    private lateinit var binding: FragmentSayacDetailBinding

    /** Sayac entity to be displayed */
    private lateinit var sayacEntity: Sayac

    /** Fiori ObjectHeader component used when entity is to be displayed on phone */
    private var objectHeader: ObjectHeader? = null

    /** View model of the entity type that the displayed entity belongs to */
    private lateinit var viewModel: SayacViewModel

    /** Error handler to display message should error occurs */
    private lateinit var errorHandler: ErrorHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menu = R.menu.itemlist_view_options
        errorHandler = (currentActivity.application as SAPWizardApplication).errorHandler
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return setupDataBinding(inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            currentActivity = it
            viewModel = ViewModelProviders.of(it).get(SayacViewModel::class.java)
            viewModel.deleteResult.observe(this, Observer { result ->
                onDeleteComplete(result!!)
            })

            viewModel.selectedEntity.observe(this, Observer { entity ->
                sayacEntity = entity
                binding.setSayac(entity)
                setupObjectHeader()
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.update_item -> {
                listener?.onFragmentStateChange(UIConstants.EVENT_EDIT_ITEM, sayacEntity)
                true
            }
            R.id.delete_item -> {
                listener?.onFragmentStateChange(UIConstants.EVENT_ASK_DELETE_CONFIRMATION,null)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Notify user of error encountered during operation execution
     *
     * @param [exception] which encountered
     */
    private fun handleError(exception: Exception) {
        val errorMessage = ErrorMessage(currentActivity.resources.getString(R.string.delete_failed),
                currentActivity.resources.getString(R.string.delete_failed_detail), exception, false)
        errorHandler.sendErrorMessage(errorMessage)
    }

    /**
     * Completion callback for delete operation
     *
     * @param [result] of the operation
     */
    private fun onDeleteComplete(result: OperationResult<Sayac>) {
        progressBar?.let {
            it.visibility = View.INVISIBLE
        }
        viewModel.removeAllSelected()
        result.error?.let {
            handleError(it)
            return
        }
        listener?.onFragmentStateChange(UIConstants.EVENT_DELETION_COMPLETED, sayacEntity)
    }


    @Suppress("UNUSED", "UNUSED_PARAMETER") // parameter is needed because of the xml binding
    fun onNavigationClickedToPhoneRegistry_phone(view: View) {
        val intent = Intent(currentActivity, PhoneRegistryActivity::class.java)
        intent.putExtra("parent", sayacEntity)
        intent.putExtra("navigation", "phone")
        startActivity(intent)
    }

    @Suppress("UNUSED", "UNUSED_PARAMETER") // parameter is needed because of the xml binding
    fun onNavigationClickedToFile_files(view: View) {
        val intent = Intent(currentActivity, FileActivity::class.java)
        intent.putExtra("parent", sayacEntity)
        intent.putExtra("navigation", "files")
        startActivity(intent)
    }

    /**
     * Set up databinding for this view
     *
     * @param [inflater] layout inflater from onCreateView
     * @param [container] view group from onCreateView
     *
     * @return [View] rootView from generated databinding code
     */
    private fun setupDataBinding(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentSayacDetailBinding.inflate(inflater, container, false)
        binding.handler = this
        return binding.root
    }

    /**
     * Set detail image of ObjectHeader.
     * When the entity does not provides picture, set the first character of the masterProperty.
     */
    private fun setDetailImage(objectHeader: ObjectHeader, sayacEntity: Sayac) {
        viewModel.downloadMedia(sayacEntity, Action1 { media ->
            objectHeader.prepareDetailImageView().scaleType = ImageView.ScaleType.FIT_CENTER
            val image = BitmapDrawable(currentActivity.resources, BitmapFactory.decodeByteArray(media, 0, media.size))
            objectHeader.detailImage = image
        }, Action1 {
            if (sayacEntity.getDataValue(Sayac.createdAt) != null && !sayacEntity.getDataValue(Sayac.createdAt).toString().isEmpty()) {
                objectHeader.detailImageCharacter = sayacEntity.getDataValue(Sayac.createdAt).toString().substring(0, 1)
            } else {
                objectHeader.detailImageCharacter = "?"
            }
        })
    }

    /**
     * Setup ObjectHeader with an instance of sayacEntity
     */
    private fun setupObjectHeader() {
        val secondToolbar = currentActivity.findViewById<Toolbar>(R.id.secondaryToolbar)
        if (secondToolbar != null) {
            secondToolbar.setTitle(sayacEntity.entityType.localName)
        } else {
            currentActivity.setTitle(sayacEntity.entityType.localName)
        }

        // Object Header is not available in tablet mode
        objectHeader = currentActivity.findViewById(R.id.objectHeader)
        val dataValue = sayacEntity.getDataValue(Sayac.createdAt)

        objectHeader?.let {
            it.apply {
                headline = dataValue?.toString()
                subheadline = EntityKeyUtil.getOptionalEntityKey(sayacEntity)
                body = "You can set the header body text here."
                footnote = "You can set the header footnote here."
                description = "You can add a detailed item description here."
            }
            it.setTag("#tag1", 0)
            it.setTag("#tag3", 2)
            it.setTag("#tag2", 1)

            setDetailImage(it, sayacEntity)
            it.visibility = View.VISIBLE
        }
    }
}
