package com.company.bmobkotlin.databinding.keyvaluecell

import com.sap.cloud.mobile.fiori.misc.KeyValueCell

/*
 * Binding adapter for Fiori KeyValueCell UI component.
 * Android data binding library invokes its methods to set value for the KeyValueCell
 * In one way databinding, layout file has binding expression to convert entity properties to string
 */
object DataBindingAdapter {
    /*
     * For OData types: Edm.String
     * Getter of attribute bound returns String
     */
    @androidx.databinding.BindingAdapter("valueText")
    @JvmStatic fun setValueText(keyValueCell: KeyValueCell, stringValue: String?) {
        if (stringValue == null) {
            keyValueCell.value = ""
        } else {
            keyValueCell.value = stringValue
        }
    }
}