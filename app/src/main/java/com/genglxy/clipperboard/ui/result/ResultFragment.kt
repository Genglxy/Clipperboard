package com.genglxy.clipperboard.ui.result

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.genglxy.clipperboard.databinding.FragmentResultBinding
import com.genglxy.clipperboard.worker.Clipper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ResultFragment : Fragment() {
    private var _binding: FragmentResultBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it's null."
        }
    private val viewModel by lazy { ViewModelProvider(this)[ResultViewModel::class.java] }
    private val args: ResultFragmentArgs by navArgs()

    lateinit var clipperBinder: Clipper.ClipperBinder
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            clipperBinder = service as Clipper.ClipperBinder
            clipperBinder.startClip(viewModel.photoList)
            val job = Job()
            val scope = CoroutineScope(job)
            val mainJob = Job()
            val mainScope = CoroutineScope(mainJob)
            scope.launch {
                var progress: Pair<Int, Int>
                while (true) {
                    progress = clipperBinder.getProgress()
                    if (progress.second != 0) {
                        if (progress.first > progress.second) {
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.progress.visibility = View.GONE
                                binding.progressText.visibility = View.GONE
                            }
                            break
                        } else {
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.progress.max = progress.second
                                binding.progress.progress = progress.first
                                binding.progressText.text =
                                    "Working on the ${progress.first} image, ${progress.second - progress.first} more to go..."
                            }
                        }
                        delay(200)
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    //viewModel.fileUri.value = clipperBinder.getUri()
                    viewModel.fileBitmap.value = clipperBinder.getBitmap()
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.photoList.size != args.photoList.size) {
            viewModel.photoList.clear()
            viewModel.photoList.addAll(args.photoList)
        }
        val intent = Intent(context, Clipper::class.java)
        activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        /*  observe the already saved file's Uri via service
        viewModel.fileUri.observe(viewLifecycleOwner) { fileUri ->
            val params = imageView.layoutParams
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            imageView.layoutParams = params
            Glide.with(this).load(fileUri).into(imageView)
        }
         */

        viewModel.fileBitmap.observe(viewLifecycleOwner) {  //get the bitmap via service
            val windowsWidth = this.resources.displayMetrics.widthPixels
            val bitmap = it
            Log.d("debugInfo", "bitmap width ${bitmap.width} height${bitmap.height}")
            val params = binding.resultImage.layoutParams
            params.height =
                (bitmap.height.toDouble() / bitmap.width.toDouble() * windowsWidth.toDouble()).toInt()
            binding.resultImage.layoutParams = params
            binding.resultImage.setImageBitmap(bitmap)
            Log.d("debugInfo", "view width ${binding.resultImage.measuredWidth} height${binding.resultImage.measuredHeight}")
            binding.nestedScrollView.visibility = View.VISIBLE
        }

        /*   get the final image bitmap via this class
        val windowsWidth = this.resources.displayMetrics.widthPixels
        val bitmap = tempClip(viewModel.photoList)
        val params = imageView.layoutParams
        params.height =
            (bitmap.height.toDouble() / bitmap.width.toDouble() * windowsWidth.toDouble()).toInt()
        imageView.layoutParams = params
        imageView.setImageBitmap(bitmap)
         */


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}