package com.company.bmobkotlin.mdui

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.company.bmobkotlin.app.ErrorHandler
import com.company.bmobkotlin.app.ErrorMessage
import com.company.bmobkotlin.app.SAPWizardApplication
import com.company.bmobkotlin.offline.OfflineODataSyncService
import com.company.bmobkotlin.service.SAPServiceManager
import com.sap.cloud.mobile.fiori.indicator.FioriProgressBar
import com.sap.cloud.mobile.odata.core.Action0
import com.sap.cloud.mobile.odata.core.Action1
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.content.Context
import android.content.Intent
import android.view.Menu
import android.view.MenuItem

import java.util.ArrayList
import java.util.HashMap
import com.company.bmobkotlin.mdui.file.FileActivity
import com.company.bmobkotlin.mdui.phoneregistry.PhoneRegistryActivity
import com.company.bmobkotlin.mdui.sayac.SayacActivity
import org.slf4j.LoggerFactory
import com.company.bmobkotlin.R

import kotlinx.android.synthetic.main.activity_entity_set_list.*
import kotlinx.android.synthetic.main.element_entity_set_list.view.*

/*
 * An activity to display the list of all entity types from the OData service
 */
class EntitySetListActivity : AppCompatActivity() {
    private val entitySetNames = ArrayList<String>()
    private val entitySetNameMap = HashMap<String, EntitySetName>()


    /* Application Error handler for reporting errors */
    internal lateinit var errorHandler: ErrorHandler

    /*
     * Android Bound Service to handle offline synchronization operations. Service runs in foreground mode to maximize
     * resiliency.
     */
    private var syncService: OfflineODataSyncService? = null

    /** Flag to indicate that current activity is bound to the Offline Sync Service */
    internal var isBound = false

    /* Fiori progress bar for busy indication if either update or delete action is clicked upon */
    private var progressBar: FioriProgressBar? = null

    /* Service connection object callbacks when service is bound or lost */
    private lateinit var serviceConnection: ServiceConnection

    private lateinit var sapServiceManager: SAPServiceManager

    enum class EntitySetName constructor(val entitySetName: String, val titleId: Int, val iconId: Int) {
        File("File", R.string.eset_file,
            BLUE_ANDROID_ICON),
        PhoneRegistry("PhoneRegistry", R.string.eset_phoneregistry,
            WHITE_ANDROID_ICON),
        Sayac("Sayac", R.string.eset_sayac,
            BLUE_ANDROID_ICON)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        errorHandler = (application as SAPWizardApplication).errorHandler
        sapServiceManager = (application as SAPWizardApplication).sapServiceManager
        setContentView(R.layout.activity_entity_set_list)
        val toolbar = findViewById<Toolbar>(R.id.toolbar) // to avoid ambiguity
        setSupportActionBar(toolbar)

        entitySetNames.clear()
        entitySetNameMap.clear()
        for (entitySet in EntitySetName.values()) {
            val entitySetTitle = resources.getString(entitySet.titleId)
            entitySetNames.add(entitySetTitle)
            entitySetNameMap[entitySetTitle] = entitySet
        }

        val listView = entity_list
        val adapter = EntitySetListAdapter(this, R.layout.element_entity_set_list, entitySetNames)

        listView.adapter = adapter

        listView.setOnItemClickListener listView@{ _, _, position, _ ->
            val entitySetName = entitySetNameMap[adapter.getItem(position)!!]
            val context = this@EntitySetListActivity
            val intent: Intent = when (entitySetName) {
                EntitySetListActivity.EntitySetName.File -> Intent(context, FileActivity::class.java)
                EntitySetListActivity.EntitySetName.PhoneRegistry -> Intent(context, PhoneRegistryActivity::class.java)
                EntitySetListActivity.EntitySetName.Sayac -> Intent(context, SayacActivity::class.java)
                else -> return@listView
            }
            context.startActivity(intent)
        }
    }

    inner class EntitySetListAdapter internal constructor(context: Context, resource: Int, entitySetNames: List<String>)
                    : ArrayAdapter<String>(context, resource, entitySetNames) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            val entitySetName = entitySetNameMap[getItem(position)!!]
            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.element_entity_set_list, parent, false)
            }
            val entitySetCell = view!!.entity_set_name
            entitySetCell.headline = entitySetName?.titleId?.let {
                context.resources.getString(it)
            }
            entitySetName?.iconId?.let { entitySetCell.setDetailImage(it) }
            return view
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, SETTINGS_SCREEN_ITEM, 0, R.string.menu_item_settings)
        menu.add(0, SYNC_ACTION_ITEM, 1, R.string.synchronize_action)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        LOGGER.debug("onOptionsItemSelected: " + item.title)
        return when (item.itemId) {
            SETTINGS_SCREEN_ITEM -> {
                LOGGER.debug("settings screen menu item selected.")
                val intent = Intent(this, SettingsActivity::class.java)
                this.startActivityForResult(intent, SETTINGS_SCREEN_ITEM)
                true
            }
            SYNC_ACTION_ITEM -> {
                synchronize(Action0 { synchronizeConclusion() })
                true
            }
            else -> false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LOGGER.debug("EntitySetListActivity::onActivityResult, request code: $requestCode result code: $resultCode")
        if (requestCode == SETTINGS_SCREEN_ITEM) {
            LOGGER.debug("Calling AppState to retrieve settings after settings screen is closed.")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            syncService = null
        }
    }

    private fun synchronize(syncCompleteHandler: Action0) {
        if (progressBar == null) {
            progressBar = window.decorView.findViewById(R.id.sync_indeterminate)
        }

        progressBar!!.visibility = View.VISIBLE
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                syncService = (service as OfflineODataSyncService.LocalBinder).service
                isBound = true
                sapServiceManager.synchronize(syncService!!,
                    Action0 {
                        this@EntitySetListActivity.runOnUiThread {
                            progressBar!!.visibility = View.INVISIBLE
                            syncCompleteHandler.call()
                        }
                    },
                    Action1 {
                        this@EntitySetListActivity.runOnUiThread {
                            progressBar!!.visibility = View.INVISIBLE
                            val res = resources
                            val errorMessage = ErrorMessage(res.getString(R.string.synchronize_failure),
                                    res.getString(R.string.synchronize_failure_detail))
                            errorHandler.sendErrorMessage(errorMessage)
                        }
                    })
            }

            override fun onServiceDisconnected(className: ComponentName) {
                syncService = null
                isBound = false
            }
        }

        if (bindService(Intent(this, OfflineODataSyncService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)) {
        } else {
            unbindService(serviceConnection)
            LOGGER.error("Bind service failure")
        }
    }

    private fun synchronizeConclusion() {
        unbindService(serviceConnection)
        isBound = false
        syncService = null
    }

    companion object {
        private const val SETTINGS_SCREEN_ITEM = 200
          private const val SYNC_ACTION_ITEM = 300
        private val LOGGER = LoggerFactory.getLogger(EntitySetListActivity::class.java)
        private const val BLUE_ANDROID_ICON = R.drawable.ic_android_blue
        private const val WHITE_ANDROID_ICON = R.drawable.ic_android_white
    }
}
