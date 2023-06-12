package com.genglxy.clipperboard.ui.clipper

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
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.genglxy.clipperboard.R
import com.genglxy.clipperboard.databinding.FragmentClipperBinding
import com.genglxy.clipperboard.logic.model.Photo
import com.genglxy.clipperboard.ui.editor.EditorFragment
import com.genglxy.clipperboard.worker.Clipper
import github.leavesczy.matisse.Matisse
import github.leavesczy.matisse.MatisseContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Collections

class ClipperFragment : Fragment() {

    private var _binding: FragmentClipperBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it's null."
        }
    private val viewModel by lazy { ViewModelProvider(this)[ClipperViewModel::class.java] }
    private lateinit var adapter: ClipperAdapter

    lateinit var clipperBinder: Clipper.ClipperBinder
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            clipperBinder = service as Clipper.ClipperBinder
            clipperBinder.startClip(viewModel.photoList)
            val job = Job()
            val scope = CoroutineScope(job)
            scope.launch {
                var progress: Pair<Int, Int>
                while (true) {
                    progress = clipperBinder.getProgress()
                    if (progress.second != 0) {
                        if (progress.first > progress.second) {
                            break
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

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Matisse>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(EditorFragment.REQUEST_KEY_HEIGHT) { _, bundle ->
            val height = bundle.getInt(EditorFragment.BUNDLE_KEY_HEIGHT)
            val order = bundle.getInt(EditorFragment.BUNDLE_KEY_ORDER)
            viewModel.photoList[order].fixedHeight = height
            viewModel.photoList[order].fixed = true
            viewModel.photoList[order].autoFixed = false
            if (viewModel.fixedHeight.isEmpty()) {
                for (photo in viewModel.photoList) {
                    if (photo.fixedHeight == photo.height) {
                        if (photo.fixedHeight > height) {
                            photo.fixedHeight = height
                        }
                    }
                }
            }
            if (!viewModel.fixedHeight.contains(height)) {
                viewModel.fixedHeight.add(height)
            }
            if (viewModel.currentFixedHeightIndex == -1) {
                viewModel.currentFixedHeightIndex = 0
                for (photo in viewModel.photoList) {
                    if (photo.fixedHeight == photo.height) {
                        photo.fixedHeight = getFixHeight(photo.height)
                    }
                }
            }
            adapter.notifyItemChanged(order)
        }
        setFragmentResultListener(HeightPickerFragment.REQUEST_KEY_INDEX) { _, bundle ->
            viewModel.currentFixedHeightIndex = bundle.getInt(HeightPickerFragment.BUNDLE_KEY_INDEX)
            for ((order, photo) in viewModel.photoList.withIndex()) {
                if (photo.autoFixed) {
                    photo.fixedHeight = viewModel.fixedHeight[viewModel.currentFixedHeightIndex]
                    adapter.notifyItemChanged(order)
                }
            }
        }
        setFragmentResultListener(DeleteDialogFragment.REQUEST_KEY_DELETE) { _, bundle ->
            val index = bundle.getInt(DeleteDialogFragment.BUNDLE_KEY_DELETE)
            viewModel.photoList.removeAt(index)
            //adapter.notifyDataSetChanged()
            adapter.notifyItemRemoved(index)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClipperBinding.inflate(layoutInflater, container, false)
        activity?.let { WindowCompat.setDecorFitsSystemWindows(it.window, false) }
        val layoutManager = LinearLayoutManager(context)
        binding.clipperRecyclerView.layoutManager = layoutManager
        adapter = ClipperAdapter(this, viewModel.photoList, {
            if (it) {
                if (--viewModel.addButtonVisibility == 0) {
                    binding.floatingActionButton.visibility = View.VISIBLE
                }
            } else {
                if (viewModel.addButtonVisibility++ == 0) {
                    binding.floatingActionButton.visibility = View.GONE
                }
            }
        }, { photo, position ->
            findNavController().navigate(
                ClipperFragmentDirections.openEditor(
                    photo, viewModel.fixedHeight.toIntArray(), position
                )
            )
        }, {
            var flag = true
            for (photo in viewModel.photoList) {
                if (photo.checked) {
                    binding.floatingActionButton.visibility = View.VISIBLE
                    flag = false
                    break
                }
            }
            if (flag) {
                binding.floatingActionButton.visibility = View.GONE
            }
        }, { position ->
            findNavController().navigate(
                ClipperFragmentDirections.openDeleteDialog(position)
            )
        }, { imageView ->
            findNavController().navigate(ClipperFragmentDirections.openHistory())

            /*
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
                val params = imageView.layoutParams
                params.height =
                    (bitmap.height.toDouble() / bitmap.width.toDouble() * windowsWidth.toDouble()).toInt()
                imageView.layoutParams = params
                imageView.setImageBitmap(bitmap)
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

             */

        })
        binding.clipperRecyclerView.adapter = adapter
        imagePickerLauncher = registerForActivityResult(MatisseContract()) {
            if (it.isNotEmpty()) {
                for (mediaResource in it) {
                    viewModel.photoList.add(
                        Photo(
                            mediaResource.uri,
                            mediaResource.width,
                            mediaResource.height,
                            getFixHeight(mediaResource.height)
                        )
                    )
                }
                Log.d("debugInfo", "list changed")
                adapter.notifyDataSetChanged()
                binding.placeholder.visibility = View.GONE
                binding.clipperRecyclerView.visibility = View.VISIBLE
                binding.clipperTopAppBar.menu.findItem(R.id.add_image_button).isVisible = true
                binding.floatingActionButton.setIconResource(R.drawable.preview_icon)
                binding.floatingActionButton.text = "Preview"
            }
        }
        val itemTouchHelperCallback = ItemTouchHelperCallback()
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.clipperRecyclerView)

        binding.floatingActionButton.setOnClickListener {
            if (viewModel.photoList.isEmpty()) {
                openImagePicker()
            } else {
                findNavController().navigate(
                    ClipperFragmentDirections.openResult(viewModel.photoList.toTypedArray())
                )
            }
        }

        binding.clipperTopAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.open_height_picker_button -> {
                    findNavController().navigate(
                        ClipperFragmentDirections.openHeightPicker(
                            viewModel.fixedHeight.toIntArray(), viewModel.currentFixedHeightIndex
                        )
                    )
                    true
                }

                R.id.add_image_button -> {
                    openImagePicker()
                    true
                }

                else -> false
            }
        }
        return binding.root
    }

    private fun openImagePicker() {
        val matisse = Matisse(maxSelectable = 99)
        imagePickerLauncher.launch(matisse)
    }

    override fun onStart() {
        super.onStart()
        Log.d("debugInfo", "onStart called")
        if (viewModel.photoList.isNotEmpty()) {
            binding.placeholder.visibility = View.GONE
            binding.clipperRecyclerView.visibility = View.VISIBLE
            binding.clipperTopAppBar.menu.findItem(R.id.add_image_button).isVisible = true
            binding.floatingActionButton.setIconResource(R.drawable.preview_icon)
            binding.floatingActionButton.text = "Preview"
        } else {
            binding.clipperTopAppBar.menu.findItem(R.id.add_image_button).isVisible = false
            binding.floatingActionButton.setIconResource(R.drawable.add_icon)
            binding.floatingActionButton.text = "Add"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            adapter.cancelJob()
            for (photo in viewModel.photoList) {
                photo.clicked = 0
            }
        } catch (_: RuntimeException) {
        }
        viewModel.addButtonVisibility = 0
        _binding = null
    }

    private fun getFixHeight(height: Int): Int {
        return if (viewModel.currentFixedHeightIndex == -1) {
            height
        } else {
            if (viewModel.fixedHeight[viewModel.currentFixedHeightIndex] < height) {
                viewModel.fixedHeight[viewModel.currentFixedHeightIndex]
            } else {
                height
            }
        }
    }

    /*
    private var progressTo = 0
    private var progress = 0
    private fun tempClip(photoList: List<Photo>): Bitmap {
        var bitmapWidth = 0
        for ((i, photo) in photoList.withIndex()) {
            if (photo.width > bitmapWidth && photo.checked) {
                bitmapWidth = photo.width
            }
        }
        var bitmapHeight = 0
        for (photo in photoList) {
            if (photo.checked) {
                bitmapHeight += scaledHeight(photo, bitmapWidth)
                progressTo++
            }
        }
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        var tempHeight = 0
        for (photo in photoList) {
            val thisHeight = tempHeight
            if (photo.checked) {
                val scaledHeight = scaledHeight(photo, bitmapWidth)
                val height = if (photo.fixed) {
                    photo.fixedHeight
                } else {
                    photo.height
                }
                Glide.with(this).asBitmap().load(photo.uri).apply(
                    RequestOptions.bitmapTransform(
                        CropTransformation(
                            photo.width, height, CropTransformation.CropType.BOTTOM
                        )
                    )
                ).into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap>?
                    ) {
                        val newBitmap = scaleBitmap(resource, bitmapWidth, scaledHeight)
                        canvas.drawBitmap(newBitmap, 0F, thisHeight.toFloat(), null)
                        progress++
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // this is called when imageView is cleared on lifecycle call or for
                        // some other reason.
                        // if you are referencing the bitmap somewhere else too other than this imageView
                        // clear it here as you can no longer have the bitmap
                    }
                })
                tempHeight += scaledHeight
            }
        }

        val job = Job()
        val scope = CoroutineScope(job)
        scope.launch {
            while (progress != progressTo) {
                delay(5000)
            }
            val fileUri = bitmap2Cache(ClipperboardApplication.context, bitmap)
        }
        return bitmap
    }

    private fun scaleBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val scaleWidth: Float = width.toFloat() / bitmap.width
        val scaleHeight: Float = height.toFloat() / bitmap.height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun scaledHeight(
        photo: Photo, maxIndex: Int
    ) = if (photo.fixed) {
        photo.fixedHeight * maxIndex / photo.width
    } else {
        photo.height * maxIndex / photo.width
    }

    private fun bitmap2Cache(context: Context, bitmap: Bitmap): Uri {
        val format =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.PNG
        val extension = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) "webp" else "png"
        val path =
            File("${context.externalCacheDir}${File.separator}${System.currentTimeMillis()}.$extension")
        try {
            val os = FileOutputStream(path)
            bitmap.compress(format, 100, os)
            os.close()
            return Uri.fromFile(path)
        } catch (_: Exception) {
        }
        return Uri.EMPTY
    }

     */

    inner class ItemTouchHelperCallback : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
        ): Int {
            val layoutManager = recyclerView.layoutManager
            return if (layoutManager is LinearLayoutManager) {
                if (layoutManager.orientation == LinearLayoutManager.VERTICAL) {
                    makeMovementFlags((ItemTouchHelper.UP or ItemTouchHelper.DOWN), 0)
                } else {
                    makeMovementFlags((ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT), 0)
                }
            } else if (layoutManager is GridLayoutManager) {
                makeMovementFlags(
                    (ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT),
                    0
                )
            } else {
                makeMovementFlags(0, 0)
            }
        }

        override fun isLongPressDragEnabled(): Boolean = true

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val size = viewModel.photoList.size
            val srcPosition = viewHolder.adapterPosition
            val targetPosition = target.adapterPosition
            if (size > 1 && srcPosition < size && targetPosition < size) {
                Collections.swap(viewModel.photoList, srcPosition, targetPosition)
                recyclerView.adapter?.notifyItemMoved(srcPosition, targetPosition)
                return true
            }
            return false
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            when (actionState) {
                ItemTouchHelper.ACTION_STATE_DRAG -> {
                    if (viewHolder != null) {
                        viewHolder.itemView.alpha = 0.5F
                    }
                }
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            viewHolder.itemView.alpha = 1F
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    }
}