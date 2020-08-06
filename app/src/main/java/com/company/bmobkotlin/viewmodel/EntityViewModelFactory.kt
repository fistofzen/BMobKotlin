package com.company.bmobkotlin.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.os.Parcelable

import com.company.bmobkotlin.viewmodel.file.FileViewModel
import com.company.bmobkotlin.viewmodel.phoneregistry.PhoneRegistryViewModel
import com.company.bmobkotlin.viewmodel.sayac.SayacViewModel

/**
 * Custom factory class, which can create view models for entity subsets, which are
 * reached from a parent entity through a navigation property.
 *
 * @param application parent application
 * @param navigationPropertyName name of the navigation link
 * @param entityData parent entity
 */
class EntityViewModelFactory (
        val application: Application, // name of the navigation property
        val navigationPropertyName: String, // parent entity
        val entityData: Parcelable) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass.simpleName) {
			"FileViewModel" -> FileViewModel(application, navigationPropertyName, entityData) as T
            			"PhoneRegistryViewModel" -> PhoneRegistryViewModel(application, navigationPropertyName, entityData) as T
             else -> SayacViewModel(application, navigationPropertyName, entityData) as T
        }
    }
}
