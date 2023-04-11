package com.xyoye.stream_component.ui.activities.storage_file

import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
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
import com.xyoye.common_component.storage.impl.FtpStorage
import com.xyoye.common_component.utils.SupervisorScope
import com.xyoye.common_component.weight.BottomActionDialog
import com.xyoye.data_component.bean.SheetActionBean
import com.xyoye.data_component.bean.StorageFilePath
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityStorageFileBinding
import com.xyoye.stream_component.ui.fragment.storage_file.StorageFileFragment
import com.xyoye.stream_component.ui.weight.StorageFileMenus
import com.xyoye.stream_component.utils.storage.StorageFilePathAdapter
import com.xyoye.stream_component.utils.storage.StorageFileStyleHelper
import com.xyoye.stream_component.utils.storage.StorageSortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Route(path = RouteTable.Stream.StorageFile)
class StorageFileActivity : BaseActivity<StorageFileViewModel, ActivityStorageFileBinding>() {

    @Autowired
    @JvmField
    var storageLibrary: MediaLibraryEntity? = null

    lateinit var storage: Storage
        private set

    // 当前所处文件夹
    var directory: StorageFile? = null
        private set

    // 标题栏菜单管理器
    private lateinit var mMenus: StorageFileMenus

    // 文件Fragment列表
    private val mRouteFragmentMap = mutableMapOf<StorageFilePath, StorageFileFragment>()

    // 标题栏样式工具
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
            super.onCreate(savedInstanceState)
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
        title = storageLibrary?.displayName
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

        dataBinding.quicklyPlayBt.setOnClickListener {
            viewModel.quicklyPlay(storage)
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

        if (storage is FtpStorage) {
            lifecycle.coroutineScope.launchWhenResumed {
                withContext(Dispatchers.IO) {
                    (storage as FtpStorage).completePending()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mMenus = StorageFileMenus.inflater(this, menu)
        mMenus.onSearchTextChanged { onSearchTextChanged(it) }
        mMenus.onSortTypeChanged { onSortOptionChanged(it) }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mMenus.onOptionsItemSelected(item)
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode != KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, event)
        }
        if (mMenus.onKeyDown()) {
            return true
        }
        if (popFragment()) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        if (this::storage.isInitialized) {
            SupervisorScope.IO.launch {
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
                viewModel.castItem(file, it)
            }
            return@BottomActionDialog true
        }.show()
    }

    /**
     * 通知当前展示的Fragment重新出现在界面上
     */
    private fun notifyFragmentReappear() {
        mRouteFragmentMap.values.lastOrNull()?.onReappear()
    }

    /**
     * 更新标题栏副标题
     */
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

    /**
     * 搜索文案
     */
    private fun onSearchTextChanged(text: String) {
        mRouteFragmentMap.values.lastOrNull()?.search(text)
    }

    /**
     * 改变文件排序
     */
    private fun onSortOptionChanged(option: StorageSortOption) {
        mRouteFragmentMap.values.onEach { it.sort(option) }
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
        viewModel.playItem(file)
    }

    fun castFile(file: StorageFile) {
        viewModel.castItem(file)
    }
}