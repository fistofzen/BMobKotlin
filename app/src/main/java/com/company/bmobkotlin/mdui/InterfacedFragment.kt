package com.company.bmobkotlin.mdui

import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import androidx.appcompat.widget.Toolbar
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.company.bmobkotlin.R

open class InterfacedFragment<T>: Fragment() {

    /** Hold the current context */
    internal lateinit var currentActivity: FragmentActivity

    /** Store the toolbar title of the actual fragment */
    internal var activityTitle: String = ""

    /** Store the toolbar menu resource of the actual fragment */
    internal var menu: Int = 0

    /** Navigation parameter: name of the link */
    internal var parentEntityData: Parcelable? = null

    /** Navigation parameter: starting entity */
    internal var navigationPropertyName: String? = null

    /** The progress bar */
    internal val secondaryToolbar: Toolbar?
        get() = currentActivity.findViewById<Toolbar>(R.id.secondaryToolbar)

    /** The progress bar */
    internal val progressBar : ProgressBar?
        get() = currentActivity.findViewById<ProgressBar>(R.id.indeterminateBar)

    /** The listener **/
    internal var listener: InterfacedFragmentListener<T>? = null

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            currentActivity = it
            if (it is InterfacedFragmentListener<*>) {
                listener = it as InterfacedFragmentListener<T>
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        secondaryToolbar?.let {
            it.menu.clear()
            it.inflateMenu(this.menu)
            it.setOnMenuItemClickListener(this::onOptionsItemSelected)
            return@onCreateOptionsMenu
        }
        inflater.inflate(this.menu, menu)
    }

    interface InterfacedFragmentListener<T> {
        fun onFragmentStateChange(evt: Int, entity: T?)
    }
}