package com.xyoye.storage_component.ui.fragment.storage_file

import androidx.core.view.isVisible
import com.xyoye.common_component.base.BaseFragment
import com.xyoye.common_component.extension.setData
import com.xyoye.common_component.extension.vertical
import com.xyoye.common_component.storage.file.StorageFile
import com.xyoye.storage_component.BR
import com.xyoye.storage_component.R
import com.xyoye.storage_component.databinding.FragmentStorageFileBinding
import com.xyoye.storage_component.ui.activities.storage_file.StorageFileActivity
import com.xyoye.storage_component.utils.storage.StorageSortOption

class StorageFileFragment :
    BaseFragment<StorageFileFragmentViewModel, FragmentStorageFileBinding>() {

    private val directory: StorageFile? by lazy { ownerActivity.directory }

    companion object {

        fun newInstance() = StorageFileFragment()
    }

    private val ownerActivity by lazy {
        requireActivity() as StorageFileActivity
    }

    override fun initViewModel() =
        ViewModelInit(
            BR.viewModel,
            StorageFileFragmentViewModel::class.java
        )

    override fun getLayoutId() = R.layout.fragment_storage_file

    override fun initView() {
        initRecyclerView()

        viewModel.storage = ownerActivity.storage

        viewModel.fileLiveData.observe(this) {
            dataBinding.loading.isVisible = false
            dataBinding.refreshLayout.isVisible = true
            dataBinding.refreshLayout.isRefreshing = false
            ownerActivity.onDirectoryOpened(it)
            dataBinding.storageFileRv.setData(it)
        }

        dataBinding.refreshLayout.setColorSchemeResources(R.color.theme)
        dataBinding.refreshLayout.setOnRefreshListener {
            viewModel.listFile(directory, refresh = true)
        }

        viewModel.listFile(directory)
    }

    private fun initRecyclerView() {
        dataBinding.storageFileRv.apply {
            layoutManager = vertical()

            adapter = StorageFileAdapter(ownerActivity, viewModel).create()
        }
    }

    /**
     * 再次展示再界面上
     */
    fun onReappear() {
        viewModel.updateHistory()
    }

    /**
     * 搜索
     */
    fun search(text: String) {
        //存在搜索条件时，不允许下拉刷新
        dataBinding.refreshLayout.isEnabled = text.isEmpty()
        viewModel.searchByText(text)
    }

    /**
     * 修改文件排序
     */
    fun sort(option: StorageSortOption) {
        viewModel.changeSortOption(option)
    }
}