package com.smartprotech.workmanager.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.work.*
import com.smartprotech.workmanager.utils.KEY_IMAGE_PATH
import com.smartprotech.workmanager.utils.KEY_IMAGE_URL
import kotlinx.android.synthetic.main.main_fragment.*
import java.util.concurrent.TimeUnit
import com.smartprotech.workmanager.R


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by lazy {
        ViewModelProviders.of(this).get(MainViewModel::class.java)
    }
    private val mutableListWorkRequest: MutableList<WorkRequest> = mutableListOf()
    private var totalQuantityOfImagesProcessed: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.listUrlsDownload.observe(this, Observer { listUrls ->
            toImage.text = listUrls.count().toString()
            listUrls.forEach { url ->
                mutableListWorkRequest.add(
                    OneTimeWorkRequestBuilder<WordManagerDownloadImage>()
                        .setInputData(workDataOf(KEY_IMAGE_URL to url))
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                        .setBackoffCriteria(
                            BackoffPolicy.LINEAR,
                            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                            TimeUnit.MILLISECONDS
                        ).build()
                )
            }
            WorkManager.getInstance().enqueue(mutableListWorkRequest)
            mutableListWorkRequest.forEach { workRequest ->
                WorkManager.getInstance().getWorkInfoByIdLiveData(workRequest.id)
                    .observe(this, Observer { workInfo ->
                        if (workInfo != null && workInfo.state.isFinished) {
                            totalQuantityOfImagesProcessed += 1
                            fromImage.text = totalQuantityOfImagesProcessed.toString()
                            progress_bar_2.progress = totalQuantityOfImagesProcessed

                            if (progress_bar_2.progress==100){
                                tvDownload.setText("Downloaded")
                            }

                            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                val storagePath = workInfo.outputData.getString(KEY_IMAGE_PATH)
                                message.text = storagePath

                            } else {
                                getString(R.string.error_dowload_image)
                                tvDownload.setText("Error")

                            }


                        }
//                        else {
//                            tvDownload.setText("Downloaded")
//                        }
                    })
            }
        })
    }

}