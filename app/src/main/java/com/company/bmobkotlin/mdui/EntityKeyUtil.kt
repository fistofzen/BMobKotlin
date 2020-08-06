package com.company.bmobkotlin.mdui

import com.sap.cloud.mobile.odata.EntityValue

object EntityKeyUtil {
    /**
     * Check entity key and if set returns it in the format of key:value,...
     * For offline mode, an entity instance is first created locally without the primary key set.
     * This is to avoid key collision when multiple entity instances are created locally.
     * After synchronization with the server, latest version (primary key set by the server) will
     * be returned to replace the locally created instance.
     * This function makes sure that we handle the case where the primary key of a locally created
     * instance is not set.
     * EntityKey.toString() return in string format: {"key":value,"key2":value2}
     * @param entityValue containing the entity key
     * @return entity key as string in the format key:value, key:value, ... OR empty string
     */
    fun getOptionalEntityKey(entityValue: EntityValue): String {
        val key = entityValue.entityKey
        val dataValueList = key.map.values()
        for (dataValue in dataValueList) {
            if (dataValue == null) {
                return "{}"
            }
        }
        val keyString = entityValue.entityKey.toString().replace("\"", "").replace(",", ", ")
        return keyString.substring(1, keyString.length - 1)
    }
}