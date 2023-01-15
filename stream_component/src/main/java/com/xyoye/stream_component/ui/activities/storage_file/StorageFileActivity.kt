package com.xyoye.stream_component.ui.activities.storage_file

import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.extension.addFragment
import com.xyoye.common_component.extension.horizontal
import com.xyoye.common_component.extension.removeFragment
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.services.ScreencastProvideService
import com.xyoye.common_component.storage.Storage
import com.xyoye.common_component.storage.StorageFactory
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.common_component.utils.SupervisorScope
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.bean.StorageFilePath
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityStorageFileBinding
import com.xyoye.stream_component.ui.fragment.storage_file.StorageFileFragment
import com.xyoye.stream_component.utils.storage.StorageFilePathAdapter
import com.xyoye.stream_component.utils.storage.StorageFileStyleHelper
import kotlinx.coroutines.launch

@Route(path = RouteTable.Stream.StorageFile)
class StorageFileActivity : BaseActivity<StorageFileViewModel, ActivityStorageFileBinding>() {

    @Autowired
    @JvmField
    var storageLibrary: MediaLibraryEntity? = null

    lateinit var storage: Storage
        private set

    var directory: StorageFile? = null
        private set

    private val releaseScope = SupervisorScope.IO

    private val mRouteFragmentMap = mutableMapOf<StorageFilePath, Fragment>()

    private val mToolbarStyleHelper: StorageFileStyleHelper by lazy {
        StorageFileStyleHelper(this, dataBinding)
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            StorageFileViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_storage_file

    override fun onCreate(savedInstanceState: Bundle?) {
        ARouter.getInstance().inject(this)

        if (checkBundle().not()) {
            finish()
            return
        }
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        notifyFragmentReappear()
    }

    override fun initView() {
        mToolbarStyleHelper.observerChildScroll()
        title = storageLibrary!!.displayName
        updateToolbarSubtitle(0, 0)

        initPathRv()
        initListener()
        openDirectory(null)
    }

    private fun initPathRv() {
        dataBinding.pathRv.apply {
            layoutManager = horizontal()

            adapter = StorageFilePathAdapter.build {
                backToRouteFragment(it)
            }
        }
    }

    private fun initListener() {
        mToolbar?.setNavigationOnClickListener {
            if (popFragment().not()) {
                finish()
            }
        }

        viewModel.playLiveData.observe(this) {
            ARouter.getInstance()
                .build(RouteTable.Player.Player)
                .navigation()
        }

        viewModel.castLiveData.observe(this) {
            ARouter.getInstance()
                .navigation(ScreencastProvideService::class.java)
                .startService(this, it)
        }

        viewModel.selectDeviceLiveData.observe(this) {
            showSelectDeviceDialog(it.first, it.second)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, event)
        }
        if (popFragment()) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        if (this::storage.isInitialized) {
            releaseScope.launch {
                storage.close()
            }
        }
        super.onDestroy()
    }

    private fun checkBundle(): Boolean {
        storageLibrary
            ?: return false
        val storage = StorageFactory.createStorage(storageLibrary!!)
            ?: return false

        this.storage = storage
        return true
    }

    private fun pushFragment(path: StorageFilePath) {
        val fragment = StorageFileFragment.newInstance()
        mRouteFragmentMap[path] = fragment

        supportFragmentManager.addFragment(
            dataBinding.fragmentContainer.id,
            fragment,
            path.route
        )
        onDisplayFragmentChanged()
    }

    private fun popFragment(): Boolean {
        if (mRouteFragmentMap.entries.size <= 1) {
            return false
        }
        val lastRoute = mRouteFragmentMap.keys.last()
        val fragment = mRouteFragmentMap.remove(lastRoute)
            ?: return true
        supportFragmentManager.removeFragment(fragment)
        onDisplayFragmentChanged()
        notifyFragmentReappear()
        return true
    }

    private fun backToRouteFragment(target: StorageFilePath) {
        val fragments = mutableListOf<Fragment>()
        for (path in mRouteFragmentMap.keys.reversed()) {
            if (path == target) {
                break
            }
            mRouteFragmentMap.remove(path)?.let { fragment ->
                fragments.add(fragment)
            }
        }
        supportFragmentManager.removeFragment(*fragments.toTypedArray())
        onDisplayFragmentChanged()
        notifyFragmentReappear()
    }

    private fun onDisplayFragmentChanged() {
        val newPathData = StorageFilePathAdapter.buildPathData(mRouteFragmentMap)
        dataBinding.pathRv.setData(newPathData)
        dataBinding.pathRv.post {
            dataBinding.pathRv.smoothScrollToPosition(newPathData.size - 1)
        }
    }

    private fun showSelectDeviceDialog(file: StorageFile, devices: List<MediaLibraryEntity>) {
        val drawable = com.xyoye.common_component.R.drawable.ic_screencast_device
        val actionData = devices.map {
            SheetActionBean(it.id, it.displayName, drawable, it.url)
        }
        BottomActionDialog(
            title = "选择投屏设备",
            activity = this,
            actionData = actionData
        ) { action ->
            devices.firstOrNull { it.id == action.actionId }?.let {
                viewModel.castItem(storage, file, it)
            }
            return@BottomActionDialog true
        }.show()
    }

    private fun notifyFragmentReappear() {
        val fragment = mRouteFragmentMap.values.lastOrNull() as? StorageFileFragment
            ?: return
        fragment.onReappear()
    }

    private fun updateToolbarSubtitle(videoCount: Int, directoryCount: Int) {
        supportActionBar?.subtitle = when {
            videoCount == 0 && directoryCount == 0 -> {
                "0视频"
            }
            directoryCount == 0 -> {
                "${videoCount}视频"
            }
            videoCount == 0 -> {
                "${directoryCount}文件夹"
            }
            else -> {
                "${videoCount}视频  ${directoryCount}文件夹"
            }
        }
    }

    fun openDirectory(file: StorageFile?) {
        directory = file

        val route = file?.filePath() ?: "/"
        val name = file?.fileName() ?: "根目录"
        pushFragment(StorageFilePath(name, route))
    }

    fun onDirectoryOpened(fileList: List<StorageFile>) {
        val videoCount = fileList.count { it.isFile() }
        val directoryCount = fileList.count { it.isDirectory() }
        updateToolbarSubtitle(videoCount, directoryCount)
    }

    fun openFile(file: StorageFile) {
        viewModel.playItem(storage, file)
    }

    fun castFile(file: StorageFile) {
        viewModel.castItem(storage, file)
    }
}