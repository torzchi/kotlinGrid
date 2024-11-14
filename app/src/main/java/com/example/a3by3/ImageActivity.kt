package com.example.a3by3

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class ImageActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private val photos = mutableListOf<Bitmap>()
    private lateinit var photoAdapter: PhotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        val layoutManager = GridLayoutManager(this, 3, GridLayoutManager.HORIZONTAL, false)
        // Initialize RecyclerView and Adapter
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        photoAdapter = PhotoAdapter(photos)
        recyclerView.adapter = photoAdapter

        // Start downloading photos
        displayDownloaded()
    }

    private fun displayDownloaded() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val i =0
                val photosUrls = (0..9).map { i ->
                    "http://cti.ubm.ro/cmo/digits/img$i.jpg"
                }

                for (url in photosUrls) {
                    Log.d("PhotoDownload", "Downloading URL: $url")
                    val photoData = downloadPhoto(url)
                    if (photoData != null) {
                        val bitmap = BitmapFactory.decodeByteArray(photoData, 0, photoData.size)
                        // Add to photos list and notify adapter on main thread
                        withContext(Dispatchers.Main) {
                            photos.add(bitmap)
                            photoAdapter.notifyItemInserted(photos.size - 1)
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("PhotoDownload", "Error downloading photos: ${e.message}")
            }
        }
    }

    private suspend fun downloadPhoto(url: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response: Response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.bytes()
                } else {
                    Log.e("PhotoDownload", "Failed to download: ${response.code}")
                    null
                }
            } catch (e: IOException) {
                Log.e("PhotoDownload", "Download error: ${e.message}")
                null
            }
        }
    }
}

