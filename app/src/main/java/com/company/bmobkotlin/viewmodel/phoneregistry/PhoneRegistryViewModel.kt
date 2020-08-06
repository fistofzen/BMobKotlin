package com.company.bmobkotlin.viewmodel.phoneregistry

import android.app.Application
import android.os.Parcelable

import com.company.bmobkotlin.viewmodel.EntityViewModel
import com.sap.cloud.android.odata.entitycontainer.PhoneRegistry
import com.sap.cloud.android.odata.entitycontainer.EntityContainerMetadata.EntitySets

/*
 * Represents View model for PhoneRegistry
 *
 * Having an entity view model for each <T> allows the ViewModelProvider to cache and return the view model of that
 * type. This is because the ViewModelStore of ViewModelProvider cannot not be able to tell the difference between
 * EntityViewModel<type1> and EntityViewModel<type2>.
 */
class PhoneRegistryViewModel(application: Application): EntityViewModel<PhoneRegistry>(application, EntitySets.phoneRegistry, PhoneRegistry.createdAt) {
    /**
     * Constructor for a specific view model with navigation data.
     * @param [navigationPropertyName] - name of the navigation property
     * @param [entityData] - parent entity (starting point of the navigation)
     */
    constructor(application: Application, navigationPropertyName: String, entityData: Parcelable): this(application) {
        EntityViewModel<PhoneRegistry>(application, EntitySets.phoneRegistry, PhoneRegistry.createdAt, navigationPropertyName, entityData)
    }
}
