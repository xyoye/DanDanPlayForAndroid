package com.xyoye.stream_component.ui.activities.remote_file

import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.xyoye.common_component.adapter.BaseAdapter
import com.xyoye.common_component.adapter.addItem
import com.xyoye.common_component.adapter.buildAdapter
import com.xyoye.common_component.base.BaseActivity
import com.xyoye.common_component.config.RouteTable
import com.xyoye.common_component.databinding.ItemFileManagerPathBinding
import com.xyoye.common_component.extension.*
import com.xyoye.common_component.utils.dp2px
import com.xyoye.common_component.utils.view.FilePathItemDecoration
import com.xyoye.common_component.weight.ToastCenter
import com.xyoye.data_component.bean.FilePathBean
import com.xyoye.data_component.data.remote.RemoteVideoData
import com.xyoye.data_component.entity.MediaLibraryEntity
import com.xyoye.stream_component.BR
import com.xyoye.stream_component.R
import com.xyoye.stream_component.databinding.ActivityRemoteFileBinding
import com.xyoye.stream_component.ui.fragment.remote_file.RemoteFileFragment
import java.util.*

@Route(path = RouteTable.Stream.RemoteFile)
class RemoteFileActivity : BaseActivity<RemoteFileViewModel, ActivityRemoteFileBinding>() {

    @Autowired
    @JvmField
    var remoteData: MediaLibraryEntity? = null

    private val fragmentStack = Stack<RemoteFileFragment>()
    private val pathList = mutableListOf<FilePathBean>()

    private lateinit var pathAdapter: BaseAdapter

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            RemoteFileViewModel::class.java
        )

    override fun getLayoutId() = R.layout.activity_remote_file

    override fun initView() {
        ARouter.getInstance().inject(this)

        if (remoteData == null) {
            ToastCenter.showError("媒体库数据错误，请重试")
            title = "远程媒体库"
            return
        }
        title = remoteData!!.displayName

        initRv()

        initObserver()

        viewModel.openStorage(remoteData!!)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_remote, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_remote_control) {
            ARouter.getInstance()
                .build(RouteTable.Stream.RemoteControl)
                .navigation()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (backFragment()) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        fragmentStack.clear()
        super.onDestroy()
    }

    private fun initRv() {
        pathAdapter = buildAdapter {

            addItem<FilePathBean, ItemFileManagerPathBinding>(R.layout.item_file_manager_path) {
                initView { data, position, _ ->
                    itemBinding.apply {
                        dirNameTv.text = data.name
                        dirNameTv.setTextColorRes(
                            if (data.isOpened) R.color.text_black else R.color.text_gray
                        )
                        dirNameTv.setOnClickListener {
                            backFragment(pathList[position])
                        }
                    }
                }
            }
        }

        dataBinding.pathRv.apply {
            layoutManager = horizontal()

            adapter = pathAdapter

            val dividerSize = dp2px(16)
            val divider = R.drawable.ic_file_manager_arrow.toResDrawable()
            if (divider != null) {
                addItemDecoration(FilePathItemDecoration(divider, dividerSize))
            }
        }
    }

    private fun initObserver() {
        viewModel.folderLiveData.observe(this) {
            listFolder("根目录", "/", it)
        }
    }

    private fun backFragment(pathBean: FilePathBean? = null): Boolean {
        return when {
            pathBean != null -> {
                while (fragmentStack.isNotEmpty()) {
                    val fragment = fragmentStack.peek()
                    if (pathBean.path != fragment.tag) {
                        val index = pathList.indexOfLast { it.path == fragment.tag }
                        if (index >= 0) {
                            pathList.removeAt(index)
                            pathAdapter.setData(pathList)
                        }
                        supportFragmentManager.removeFragment(fragment, true)
                        fragmentStack.pop()
                    } else {
                        break
                    }
                }
                true
            }
            fragmentStack.size > 1 -> {
                val fragment = fragmentStack.pop()
                supportFragmentManager.removeFragment(fragment, true)

                pathList.removeLast()
                pathAdapter.setData(pathList)
                true
            }
            else -> {
                false
            }
        }
    }

    fun listFolder(name: String, path: String, fileData: MutableList<RemoteVideoData>) {
        val pathBean = FilePathBean(name, path, true)
        pathList.find { it.isOpened }?.isOpened = false
        pathList.add(pathBean)
        pathAdapter.setData(pathList)
        dataBinding.pathRv.apply {
            (layoutManager as? LinearLayoutManager)?.scrollToPosition(pathList.lastIndex)
        }
        val childFragment = RemoteFileFragment.newInstance(fileData)
        fragmentStack.push(childFragment)
        supportFragmentManager.addFragment(R.id.container, childFragment, path, true)
    }
}