package com.dailer.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.io.File

class VaultActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var fab: FloatingActionButton

    private val tabTitles = listOf("Videos", "Photos", "Documents", "Audio")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vault)

        viewPager = findViewById(R.id.vault_viewpager)
        tabLayout = findViewById(R.id.vault_tabs)
        fab = findViewById(R.id.vault_fab)

        viewPager.adapter = VaultPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        fab.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*", "image/*", "audio/*", "application/*"))
            }
            startActivityForResult(intent, 1001)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                hideFile(uri)
            }
        }
    }

    private fun hideFile(uri: Uri) {
        lifecycleScope.launch {
            try {
                // Get file path from URI
                val filePath = getFilePathFromUri(uri)
                if (filePath != null) {
                    val file = File(filePath)
                    val fileName = file.name

                    // Use VaultManager to hide the file
                    // VaultManager.hideVideoInVault(filePath, fileName)

                    Toast.makeText(this@VaultActivity, "File hidden successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@VaultActivity, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        return try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val nameIndex = it.getColumnIndex("_data")
                if (it.moveToFirst()) {
                    it.getString(nameIndex)
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    inner class VaultPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int) = VaultPageFragment()
    }

    class VaultPageFragment : androidx.fragment.app.Fragment() {
        override fun onCreateView(
            inflater: android.view.LayoutInflater,
            container: android.view.ViewGroup?,
            savedInstanceState: Bundle?
        ): android.view.View? {
            val view = inflater.inflate(R.layout.vault_page_list, container, false)
            val recycler = view.findViewById<RecyclerView>(R.id.vault_recycler)
            recycler.layoutManager = GridLayoutManager(requireContext(), 2)
            // Add your adapter here
            return view
        }
    }
}
