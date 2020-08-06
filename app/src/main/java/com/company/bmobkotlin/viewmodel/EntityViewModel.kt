package com.company.bmobkotlin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.os.Parcelable
import com.sap.cloud.mobile.odata.core.Action1
import com.sap.cloud.mobile.odata.core.Action0
import com.company.bmobkotlin.R
import com.company.bmobkotlin.app.ErrorMessage
import com.company.bmobkotlin.app.SAPWizardApplication
import com.company.bmobkotlin.archcomp.SingleLiveEvent
import com.company.bmobkotlin.repository.OperationResult
import com.company.bmobkotlin.repository.Repository
import com.sap.cloud.mobile.odata.EntitySet
import com.sap.cloud.mobile.odata.EntityValue
import com.sap.cloud.mobile.odata.Property
import com.sap.cloud.mobile.odata.StreamBase
import java.util.ArrayList


/**
 * Generic type representing the view model with type being one of the entity types.
 *
 * Each entity type has its own view model. An entity set is a collection of entities of an entity set. View model
 * exposed the list of entities as LiveData and four events (CRUD) as SingleLiveEvent acquired from the repository for
 * the entity type. This class extends AndroidViewModel so it has access to the application object.
 *
 * @param [application] required as it extends AndroidViewModel
 * @param [entitySet] entity set that this view represents
 * @param [orderByProperty] if specified, collection will be sorted ascending with this property
 */
open class EntityViewModel<T : EntityValue>(application: Application, entitySet: EntitySet, orderByProperty: Property?) : AndroidViewModel(application) {
    /** Event to notify of async completion of read/query operation */
    val readResult: SingleLiveEvent<OperationResult<T>>

    /** Event to notify of async completion of create operation */
    val createResult: SingleLiveEvent<OperationResult<T>>

    /** Event to notify of async completion of update operation */
    val updateResult: SingleLiveEvent<OperationResult<T>>

    /** Event to notify of async completion of delete operation */
    val deleteResult: SingleLiveEvent<OperationResult<T>>

    /** LiveData of entities for this entity set */
    val observableItems: LiveData<List<T>>

    /** Repository of type of the entity type for a specified entity set */
    @Suppress("UNCHECKED_CAST")
    private val repository: Repository<T> = (application as SAPWizardApplication)
        .repositoryFactory
        .getRepository(entitySet, orderByProperty) as Repository<T>

    /*
     * Selected items via long press action
     */
    private val selectedItems = ArrayList<T>()

    /*
     * Identifier of item in focus via click action
     */
    var inFocusId: Long = 0
    
    /*
     * Flag for avoiding continuous retry
     */
    private var isDownloadError = false

    init {
        observableItems = repository.observableEntities
        readResult = repository.readResult
        createResult = repository.createResult
        updateResult = repository.updateResult
        deleteResult = repository.deleteResult
    }

    /**
     * Creates a view model with navigation information
     * @param application required as it extends AndroidViewModel
     * @param entitySet entity set that this view represents
     * @param orderByProperty property used for ordering the entity list
     * @param navigationPropertyName name of the navigation property
     * @param entityData parent entity
     */
    constructor(application: Application, entitySet: EntitySet, orderByProperty: Property?, navigationPropertyName: String, entityData: Parcelable) : this (application, entitySet, orderByProperty) {
        repository.clear()
        repository.read(entityData as EntityValue, navigationPropertyName)
    }

    fun create(entity: T) {
        repository.create(entity)
    }

    fun create(entity: T, media: StreamBase) {
        repository.create(entity, media)
    }

    fun refresh() {
        repository.read()
    }
    
    fun clear() {
        repository.clear()
    }

    fun refresh(parent: EntityValue, navPropName: String) {
        repository.read(parent, navPropName)
    }

    fun update(entity: T) {
        repository.update(entity)
    }

    fun delete(entities: MutableList<T>) {
        repository.delete(entities)
    }

    fun deleteSelected() {
        repository.delete(selectedItems)
    }

    fun downloadMedia(entity: T, successHandler: Action1<ByteArray>, failureHandler: Action1<RuntimeException>) {
        repository.downloadMedia(entity, successHandler, failureHandler)
    }

    /**
     * Perform initial read of repository. However, if data is already available, read is not be performed.
     */
    public fun initialRead() {
        if (!isDownloadError) {
            repository.initialRead(
                Action0 { isDownloadError = false},
                Action1 { error ->
                    isDownloadError = true
                    val errorHandler = (getApplication<Application>() as SAPWizardApplication).errorHandler
                    val errorMessage: ErrorMessage
                    val resources = getApplication<Application>().resources
                    errorMessage = ErrorMessage(resources.getString(R.string.read_failed),
                    resources.getString(R.string.read_failed_detail), error, false)
                    errorHandler.sendErrorMessage(errorMessage)
                }
            )
        }
    }

    /*
     * For management of items selected via long press action
     */
    fun addSelected(selected: T) {
        var found = false
        for (item in selectedItems) {
            if (item.readLink == selected.readLink) {
                found = true
                break
            }
        }
        if (!found) {
            selectedItems.add(selected)
        }
    }

    fun removeSelected(selected: T) {
        if (selectedItems.contains(selected)) {
            selectedItems.remove(selected)
        }
    }

    fun getSelected(index: Int): T? {
        return if (index >= selectedItems.size || index < 0) {
            null
        } else selectedItems[index]
    }

    fun removeAllSelected() {
        selectedItems.clear()
    }

    fun numberOfSelected(): Int {
        return selectedItems.size
    }

    fun selectedContains(member: T): Boolean {
        return selectedItems.contains(member)
    }

    /** The observable data for the selection in the list */
    val selectedEntity: MutableLiveData<T> = MutableLiveData<T>()
    fun setSelectedEntity(v: T) {
        selectedEntity.value = v
    }
}