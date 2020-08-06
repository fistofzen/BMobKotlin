package com.company.bmobkotlin.databinding.simplepropertyformcell

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.databinding.InverseMethod
import com.company.bmobkotlin.R
import com.sap.cloud.mobile.fiori.formcell.FormCell.CellValueChangeListener
import com.sap.cloud.mobile.fiori.formcell.SimplePropertyFormCell
import com.sap.cloud.mobile.odata.DayTimeDuration
import com.sap.cloud.mobile.odata.GeographyCollection
import com.sap.cloud.mobile.odata.GeographyLineString
import com.sap.cloud.mobile.odata.GeographyMultiLineString
import com.sap.cloud.mobile.odata.GeographyMultiPoint
import com.sap.cloud.mobile.odata.GeographyMultiPolygon
import com.sap.cloud.mobile.odata.GeographyPoint
import com.sap.cloud.mobile.odata.GeographyPolygon
import com.sap.cloud.mobile.odata.GeometryCollection
import com.sap.cloud.mobile.odata.GeometryLineString
import com.sap.cloud.mobile.odata.GeometryMultiLineString
import com.sap.cloud.mobile.odata.GeometryMultiPoint
import com.sap.cloud.mobile.odata.GeometryMultiPolygon
import com.sap.cloud.mobile.odata.GeometryPoint
import com.sap.cloud.mobile.odata.GeometryPolygon
import com.sap.cloud.mobile.odata.GlobalDateTime
import com.sap.cloud.mobile.odata.GuidValue
import com.sap.cloud.mobile.odata.LocalDate
import com.sap.cloud.mobile.odata.LocalDateTime
import com.sap.cloud.mobile.odata.LocalTime
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.charset.StandardCharsets

/**
 * Supports Android two way data binding
 * For each data type returned by the getter of the bound attribute, a pair of methods is required
 * to set the value of SimplePropertyFormCell and convert user's input (string) to respective
 * data type.
 */
object Converter {
    /*
     * Specifies method for retrieving the value of SimplePropertyFormCell
     */
    @InverseBindingAdapter(attribute = "value", event = "valueAttrChanged")
    @JvmStatic
    fun getValue(cell: SimplePropertyFormCell): String = cell.value!!.toString()

    /*
     * Associate inverse binding listener to the view's change listener
     */
    @BindingAdapter("valueAttrChanged")
    @JvmStatic
    fun setValueChanged(cell: SimplePropertyFormCell, listener: InverseBindingListener) {
        cell.cellValueChangeListener = object : CellValueChangeListener<CharSequence>() {
            override fun cellChangeHandler(charSequence: CharSequence?) {
                listener.onChange()
            }
        }
    }

    /*
     * For OData types: Edm.Int32, Edm.Byte: (int and Integer), Edm.Decimal: (Integer)
     * Handles two way data binding to and from Integer
     */
    @InverseMethod("toInteger")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: Int?, value: Int?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toInteger(cell: SimplePropertyFormCell, oldValue: Int?, value: String): Int? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                Integer.valueOf(value)
            }
        } catch (e: NumberFormatException) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.Decimal (Scale 0)
     * Handles two way data binding to and from BigInteger
     */
    @InverseMethod("toBigInteger")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: BigInteger?, value: BigInteger?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toBigInteger(cell: SimplePropertyFormCell, oldValue: BigInteger?, value: String): BigInteger? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                BigInteger.valueOf(java.lang.Long.parseLong(value))
            }
        } catch (e: NumberFormatException) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.Int64
     * Handles two way data binding to and from Long
     */
    @InverseMethod("toLong")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: Long?, value: Long?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toLong(cell: SimplePropertyFormCell, oldValue: Long?, value: String): Long? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                java.lang.Long.valueOf(value)
            }
        } catch (e: NumberFormatException) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.Int16
     * Handles two way data binding to and from Short
     */
    @InverseMethod("toShort")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: Short?, value: Short?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toShort(cell: SimplePropertyFormCell, oldValue: Short?, value: String): Short? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                java.lang.Short.valueOf(value)
            }
        } catch (e: NumberFormatException) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.SByte
     * Handles two way data binding to and from Byte
     */
    @InverseMethod("toSByte")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: Byte?, value: Byte?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toSByte(cell: SimplePropertyFormCell, oldValue: Byte?, value: String): Byte? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                java.lang.Byte.valueOf(value)
            }
        } catch (e: NumberFormatException) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.Decimal
     * Handles two way data binding to and from BigDecimal
     */
    @InverseMethod("toDecimal")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: BigDecimal?, value: BigDecimal?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toDecimal(cell: SimplePropertyFormCell, oldValue: BigDecimal?, value: String): BigDecimal? {
        try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                return oldValue
            }
            val viewText = cell.value!!.toString()
            return BigDecimal.valueOf(java.lang.Double.valueOf(viewText))
        } catch (e: NumberFormatException) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            return oldValue
        }
    }

    /*
     * For OData types: Edm.Binary
     * Handles two way data binding to and from byte[]
     */
    @InverseMethod("toBinary")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: ByteArray?, value: ByteArray?): String {
        return if (value == null) {
            cell.value!!.toString()
        } else {
            String(value, StandardCharsets.UTF_8)
        }
    }

    @JvmStatic
    fun toBinary(cell: SimplePropertyFormCell, oldValue: ByteArray?, value: String): ByteArray? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                value.toByteArray(StandardCharsets.UTF_8)
            }
        } catch (ex: NumberFormatException) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.Double
     * Handles two way data binding to and from Double
     */
    @InverseMethod("toDouble")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: Double?, value: Double?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toDouble(cell: SimplePropertyFormCell, oldValue: Double?, value: String): Double? {
        try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                return oldValue
            }
            val viewText = cell.value!!.toString()
            return java.lang.Double.valueOf(java.lang.Double.valueOf(viewText))
        } catch (e: NumberFormatException) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            return oldValue
        }
    }

    /*
     * For OData types: Edm.Single
     * Handles two way data binding to and from Single
     */
    @InverseMethod("toSingle")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: Float?, value: Float?): String =
        value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toSingle(cell: SimplePropertyFormCell, oldValue: Float?, value: String): Float? {
        try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                return oldValue
            }
            val viewText = cell.value!!.toString()
            return java.lang.Float.valueOf(java.lang.Float.valueOf(viewText))
        } catch (e: NumberFormatException) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            return oldValue
        }
    }

    /*
     * For OData types: Edm.DateTime (V2 only)
     * Handles two way data binding to and from LocalDateTime
     */
    @InverseMethod("toLocalDateTime")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: LocalDateTime?, value: LocalDateTime?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toLocalDateTime(cell: SimplePropertyFormCell, oldValue: LocalDateTime?, value: String): LocalDateTime? {
        cell.isErrorEnabled = false
        if (value.isEmpty()) {
            return oldValue
        }
        val newValue = LocalDateTime.parse(value)
        if (newValue == null) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            return oldValue
        }
        return newValue
    }

    /*
     * For OData types: Edm.DateTimeOffset
     * Handles two way data binding to and from GlobalDateTime
     */
    @InverseMethod("toGlobalDateTime")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: GlobalDateTime?, value: GlobalDateTime?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toGlobalDateTime(cell: SimplePropertyFormCell, oldValue: GlobalDateTime?, value: String): GlobalDateTime? {
        cell.isErrorEnabled = false
        if (value.isEmpty()) {
            return oldValue
        }
        val newValue = GlobalDateTime.parse(value)
        if (newValue == null) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            return oldValue
        }
        return newValue
    }

    /*
     * For OData types: Edm.Date
     * Handles two way data binding to and from LocalDate
     */
    @InverseMethod("toLocalDate")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: LocalDate?, value: LocalDate?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toLocalDate(cell: SimplePropertyFormCell, oldValue: LocalDate?, value: String): LocalDate? {
        cell.isErrorEnabled = false
        if (value.isEmpty()) {
            return oldValue
        }
        val newValue = LocalDate.parse(value)
        if (newValue == null) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            return oldValue
        }
        return newValue
    }

    /*
     * For OData types: Edm.TimeOfDay
     * Handles two way data binding to and from LocalTime
     */
    @InverseMethod("toLocalTime")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: LocalTime?, value: LocalTime?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toLocalTime(cell: SimplePropertyFormCell, oldValue: LocalTime?, value: String): LocalTime? {
        cell.isErrorEnabled = false
        if (value.isEmpty()) {
            return oldValue
        }
        val newValue = LocalTime.parse(value)
        if (newValue == null) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            return oldValue
        }
        return newValue
    }

    /*
     * For OData types: Edm.TimeOfDay
     * Handles two way data binding to and from LocalTime
     */
    // Edm.Duration: (DayTimeDuration)
    @InverseMethod("toDuration")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: DayTimeDuration?, value: DayTimeDuration?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toDuration(cell: SimplePropertyFormCell, oldValue: DayTimeDuration?, value: String): DayTimeDuration? {
        cell.isErrorEnabled = false
        if (value.isEmpty()) {
            return oldValue
        }
        val newValue = DayTimeDuration.parse(value)
        if (newValue == null) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            return oldValue
        }
        return newValue
    }

    /*
     * For OData types: Edm.Guid
     * Handles two way data binding to and from GuidValue
     */
    @InverseMethod("ToGuid")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: GuidValue?, value: GuidValue?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun ToGuid(cell: SimplePropertyFormCell, oldValue: GuidValue?, value: String): GuidValue? {
        cell.isErrorEnabled = false
        if (value.isEmpty()) {
            return oldValue
        }
        val newValue = GuidValue.parse(value)
        if (newValue == null) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            return oldValue
        }
        return newValue
    }

    /*
     * For OData types: Edm.Boolean
     * Handles two way data binding to and from Boolean
     */
    @InverseMethod("toBoolean")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: Boolean?, value: Boolean?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toBoolean(cell: SimplePropertyFormCell, oldValue: Boolean?, value: String): Boolean? =
            if (value.isEmpty()) oldValue else java.lang.Boolean.valueOf(value)

    /*
     * For OData types: Edm.GeographyPoint
     * Handles two way data binding to and from GeographyPoint
     */
    @InverseMethod("toGeographyPoint")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: GeographyPoint?, value: GeographyPoint?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toGeographyPoint(cell: SimplePropertyFormCell, oldValue: GeographyPoint?, value: String): GeographyPoint? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                GeographyPoint.parseWKT(value)
            }
        } catch (e: Exception) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.GeographyCollection
     * Handles two way data binding to and from GeographyCollection
     */
    @InverseMethod("toGeographyCollection")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: GeographyCollection?, value: GeographyCollection?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toGeographyCollection(cell: SimplePropertyFormCell, oldValue: GeographyCollection?, value: String): GeographyCollection? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                GeographyCollection.parseWKT(value)
            }
        } catch (e: Exception) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.GeographyMultiPoint
     * Handles two way data binding to and from GeographyMultiPoint
     */
    @InverseMethod("toGeographyMultipoint")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: GeographyMultiPoint?, value: GeographyMultiPoint?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toGeographyMultipoint(cell: SimplePropertyFormCell, oldValue: GeographyMultiPoint?, value: String): GeographyMultiPoint? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                GeographyMultiPoint.parseWKT(value)
            }
        } catch (e: Exception) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.GeographyMultiPoint
     * Handles two way data binding to and from GeographyMultiPoint
     */
    // Edm.GeographyLineString: (GeographyLineString)
    @InverseMethod("toGeographyLineString")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: GeographyLineString?, value: GeographyLineString?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toGeographyLineString(cell: SimplePropertyFormCell, oldValue: GeographyLineString?, value: String): GeographyLineString? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                GeographyLineString.parseWKT(value)
            }
        } catch (e: Exception) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.GeographyMultiLineString
     * Handles two way data binding to and from GeographyMultiLineString
     */
    @InverseMethod("toGeographyMultiLineString")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: GeographyMultiLineString?, value: GeographyMultiLineString?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toGeographyMultiLineString(cell: SimplePropertyFormCell, oldValue: GeographyMultiLineString?, value: String): GeographyMultiLineString? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                GeographyMultiLineString.parseWKT(value)
            }
        } catch (e: Exception) {
                cell.isErrorEnabled = true
                cell.error = cell.resources.getString(R.string.format_error)
                oldValue
        }
    }

    /*
     * For OData types: Edm.GeographyPolygon
     * Handles two way data binding to and from GeographyPolygon
     */
    @InverseMethod("toGeographyPolygon")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: GeographyPolygon?, value: GeographyPolygon?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toGeographyPolygon(cell: SimplePropertyFormCell, oldValue: GeographyPolygon?, value: String): GeographyPolygon? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                GeographyPolygon.parseWKT(value)
            }
        } catch (e: Exception) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.GeographyMultiPolygon
     * Handles two way data binding to and from GeographyMultiPolygon
     */
    @InverseMethod("toGeographyMultiPolygon")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: GeographyMultiPolygon?, value: GeographyMultiPolygon?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toGeographyMultiPolygon(cell: SimplePropertyFormCell, oldValue: GeographyMultiPolygon?, value: String): GeographyMultiPolygon? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                GeographyMultiPolygon.parseWKT(value)
            }
        } catch (e: Exception) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.GeometryPoint
     * Handles two way data binding to and from GeometryPoint
     */
    @InverseMethod("toGeometryPoint")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: GeometryPoint?, value: GeometryPoint?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toGeometryPoint(cell: SimplePropertyFormCell, oldValue: GeometryPoint?, value: String): GeometryPoint? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                GeometryPoint.parseWKT(value)
            }
        } catch (e: Exception) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.GeometryCollection
     * Handles two way data binding to and from GeometryCollection
     */
    @InverseMethod("toGeometryCollection")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: GeometryCollection?, value: GeometryCollection?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toGeometryCollection(cell: SimplePropertyFormCell, oldValue: GeometryCollection?, value: String): GeometryCollection? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                GeometryCollection.parseWKT(value)
            }
        } catch (e: Exception) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.GeometryMultiPoint
     * Handles two way data binding to and from GeometryMultiPoint
     */
    @InverseMethod("toGeometryMultipoint")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: GeometryMultiPoint?, value: GeometryMultiPoint?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toGeometryMultipoint(cell: SimplePropertyFormCell, oldValue: GeometryMultiPoint?, value: String): GeometryMultiPoint? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                GeometryMultiPoint.parseWKT(value)
            }
        } catch (e: Exception) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.GeometryLineString
     * Handles two way data binding to and from GeometryLineString
     */
    @InverseMethod("toGeometryLineString")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: GeometryLineString?, value: GeometryLineString?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toGeometryLineString(cell: SimplePropertyFormCell, oldValue: GeometryLineString?, value: String): GeometryLineString? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                GeometryLineString.parseWKT(value)
            }
        } catch (e: Exception) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.GeometryMultiLineString
     * Handles two way data binding to and from GeometryMultiLineString
     */
    @InverseMethod("toGeometryMultiLineString")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: GeometryMultiLineString?, value: GeometryMultiLineString?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toGeometryMultiLineString(cell: SimplePropertyFormCell, oldValue: GeometryMultiLineString?, value: String): GeometryMultiLineString? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                GeometryMultiLineString.parseWKT(value)
            }
        } catch (e: Exception) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.GeometryPolygon
     * Handles two way data binding to and from GeometryPolygon
     */
    @InverseMethod("toGeometryPolygon")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: GeometryPolygon?, value: GeometryPolygon?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toGeometryPolygon(cell: SimplePropertyFormCell, oldValue: GeometryPolygon?, value: String): GeometryPolygon? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                GeometryPolygon.parseWKT(value)
            }
        } catch (e: Exception) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }

    /*
     * For OData types: Edm.GeometryMultiPolygon
     * Handles two way data binding to and from GeometryMultiPolygon
     */
    @InverseMethod("toGeometryMultiPolygon")
    @JvmStatic
    @Suppress("UNUSED_PARAMETER")
    fun toString(cell: SimplePropertyFormCell, oldValue: GeometryMultiPolygon?, value: GeometryMultiPolygon?): String =
            value?.toString() ?: cell.value!!.toString()

    @JvmStatic
    fun toGeometryMultiPolygon(cell: SimplePropertyFormCell, oldValue: GeometryMultiPolygon?, value: String): GeometryMultiPolygon? {
        return try {
            cell.isErrorEnabled = false
            if (value.isEmpty()) {
                oldValue
            } else {
                GeometryMultiPolygon.parseWKT(value)
            }
        } catch (e: Exception) {
            cell.isErrorEnabled = true
            cell.error = cell.resources.getString(R.string.format_error)
            oldValue
        }
    }
}