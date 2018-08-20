package io.github.rajdeep1008.apkextractor

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import io.github.rajdeep1008.adapters.ApkListAdapter
import io.github.rajdeep1008.extras.Utilities
import io.github.rajdeep1008.models.Apk
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.uiThread

class MainActivity : AppCompatActivity(), ApkListAdapter.OnContextItemClickListener {

    private lateinit var progressBar: ProgressBar
    private val apkList = ArrayList<Apk>()
    private lateinit var contextItemPackageName: String

    private lateinit var mAdapter: ApkListAdapter
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    lateinit var mRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressBar = find(R.id.progress)
        Utilities.checkPermission(this)

        mRecyclerView = find(R.id.apk_list_rv)
        mLinearLayoutManager = LinearLayoutManager(this)
        mAdapter = ApkListAdapter(apkList, this)

        mRecyclerView.layoutManager = mLinearLayoutManager
        mRecyclerView.adapter = mAdapter

        loadApk()
    }

    private fun loadApk() {
        doAsync {
            val allPackages: List<PackageInfo> = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

            allPackages.forEach {
                val applicationInfo: ApplicationInfo = it.applicationInfo

                val userApk = Apk(
                        applicationInfo,
                        packageManager.getApplicationLabel(applicationInfo).toString(),
                        it.packageName,
                        it.versionName)
                apkList.add(userApk)
            }

            uiThread {
                mAdapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            Utilities.STORAGE_PERMISSION_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Utilities.makeAppDir()
                } else {
                    Snackbar.make(find(android.R.id.content), "Permission required to extract apk", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_launch -> {
                try {
                    startActivity(packageManager.getLaunchIntentForPackage(contextItemPackageName))
                } catch (e: Exception) {
                    Snackbar.make(find(android.R.id.content), "Can't open this app", Snackbar.LENGTH_SHORT).show()
                }
            }
            R.id.action_playstore -> {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$contextItemPackageName")))
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$contextItemPackageName")))
                }

            }
        }
        return true
    }

    override fun onItemClicked(packageName: String) {
        contextItemPackageName = packageName
    }
}
