package com.company.bmobkotlin.viewmodel.file

import android.app.Application
import android.os.Parcelable

import com.company.bmobkotlin.viewmodel.EntityViewModel
import com.sap.cloud.android.odata.entitycontainer.File
import com.sap.cloud.android.odata.entitycontainer.EntityContainerMetadata.EntitySets

/*
 * Represents View model for File
 *
 * Having an entity view model for each <T> allows the ViewModelProvider to cache and return the view model of that
 * type. This is because the ViewModelStore of ViewModelProvider cannot not be able to tell the difference between
 * EntityViewModel<type1> and EntityViewModel<type2>.
 */
class FileViewModel(application: Application): EntityViewModel<File>(application, EntitySets.file, File.createdAt) {
    /**
     * Constructor for a specific view model with navigation data.
     * @param [navigationPropertyName] - name of the navigation property
     * @param [entityData] - parent entity (starting point of the navigation)
     */
    constructor(application: Application, navigationPropertyName: String, entityData: Parcelable): this(application) {
        EntityViewModel<File>(application, EntitySets.file, File.createdAt, navigationPropertyName, entityData)
    }
}
