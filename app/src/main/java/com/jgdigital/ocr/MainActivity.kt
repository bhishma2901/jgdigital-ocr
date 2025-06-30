package com.jgdigital.ocr

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {
    private fun updateImagePreviews() {
        val container = findViewById<LinearLayout>(R.id.image_preview_container)
        container.removeAllViews()
        val size = resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
        for (uri in imageUris) {
            val imageView = ImageView(this)
            imageView.layoutParams = LinearLayout.LayoutParams(size, size).apply {
                setMargins(8, 8, 8, 8)
            }
            imageView.setImageURI(uri)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setBackgroundResource(android.R.drawable.picture_frame)
            container.addView(imageView)
        }
    }
    private lateinit var expandableListView: ExpandableListView
    private lateinit var adapter: ExpandableListAdapter
    private lateinit var listDataHeader: List<String>
    private lateinit var listDataChild: HashMap<String, List<String>>
    private var selectedExtractionType: Int = 0
    private val PICK_IMAGES_CODE = 1001
    private val PICK_ZIP_CODE = 1002
    private val PICK_TXT_CODE = 1003
    private var imageUris: MutableList<Uri> = mutableListOf()
    private var cropRect: android.graphics.Rect? = null
    private var outputTxtUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        expandableListView = findViewById(R.id.expandableListView)
        prepareListData()
        adapter = ExpandableListAdapter(this, listDataHeader, listDataChild)
        expandableListView.setAdapter(adapter)

        expandableListView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            selectedExtractionType = childPosition
            false
        }

        findViewById<Button>(R.id.btn_upload_images).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(intent, PICK_IMAGES_CODE)
        }
        findViewById<Button>(R.id.btn_upload_zip).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/zip"
            startActivityForResult(intent, PICK_ZIP_CODE)
        }
        findViewById<Button>(R.id.btn_crop_image).setOnClickListener {
            if (imageUris.isNotEmpty()) {
                val intent = Intent(this, CropActivity::class.java)
                intent.putExtra("imageUri", imageUris[0].toString())
                startActivityForResult(intent, 2001)
            } else {
                Toast.makeText(this, "No images uploaded", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.btn_apply_crop_all).setOnClickListener {
            if (cropRect != null && imageUris.isNotEmpty()) {
                batchCropImages()
            } else {
                Toast.makeText(this, "Crop area or images missing", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.btn_extract_text).setOnClickListener {
            if (imageUris.isNotEmpty()) {
                extractTextFromImages()
            } else {
                Toast.makeText(this, "No images uploaded", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.btn_fix_txt_file).setOnClickListener {
            if (outputTxtUri != null) {
                fixTxtFileFor7PlusDigits()
            } else {
                Toast.makeText(this, "No TXT file selected", Toast.LENGTH_SHORT).show()
            }
        }
    private fun batchCropImages() {
        // This is a simple synchronous implementation for demonstration. In production, use background threads.
        val croppedUris = mutableListOf<Uri>()
        for (uri in imageUris) {
            val bitmap = android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val rect = cropRect
            if (rect != null) {
                val cropped = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
                // Save cropped bitmap to cache and get Uri
                val file = File.createTempFile("cropped_", ".jpg", cacheDir)
                val out = java.io.FileOutputStream(file)
                cropped.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()
                croppedUris.add(Uri.fromFile(file))
            }
        }
        imageUris = croppedUris
        Toast.makeText(this, "Batch cropping done", Toast.LENGTH_SHORT).show()
    }

    private fun extractTextFromImages() {
        // This is a simple synchronous implementation for demonstration. In production, use background threads.
        val results = mutableListOf<Pair<String, String>>()
        for (uri in imageUris) {
            val bitmap = android.provider.MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val fileName = FileUtils.getFileName(this, uri) ?: "image"
            var extractedText = ""
            val latch = java.util.concurrent.CountDownLatch(1)
            OcrUtils.recognizeText(this, bitmap) { text ->
                extractedText = filterTextByType(text, selectedExtractionType)
                results.add(Pair(fileName, extractedText))
                latch.countDown()
            }
            latch.await()
        }
        // Sort by file name
        val sorted = results.sortedBy { it.first }
        // Output to file
        val outputPath = findViewById<EditText>(R.id.et_output_path).text.toString()
        val outputFileName = findViewById<EditText>(R.id.et_output_file_name).text.toString().ifEmpty { "output.txt" }
        val file = File(outputPath, outputFileName)
        file.printWriter().use { out ->
            for ((name, text) in sorted) {
                out.println("$name\t$text")
            }
        }
        Toast.makeText(this, "Extraction done. Output: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }

    private fun filterTextByType(text: String, type: Int): String {
        return when (type) {
            0 -> text // All text
            1 -> text.replace("[0-9]+".toRegex(), "") // Only text
            2 -> text.replace("[^0-9]+".toRegex(), " ").trim() // Only numbers
            3 -> text.split("\\s+", "\n").filter { it.matches("\\d{7,}".toRegex()) }.joinToString(" ") // 7+ digit numbers
            else -> text
        }
    }

    private fun fixTxtFileFor7PlusDigits() {
        // Read the txt file, extract numbers with 7+ digits, and output to a new file
        val inputStream = contentResolver.openInputStream(outputTxtUri!!)
        val text = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
        val numbers = text.split("\\s+", "\n").filter { it.matches("\\d{7,}".toRegex()) }
        val outputPath = findViewById<EditText>(R.id.et_output_path).text.toString()
        val outputFileName = findViewById<EditText>(R.id.et_output_file_name).text.toString().ifEmpty { "fixed_output.txt" }
        val file = File(outputPath, outputFileName)
        file.printWriter().use { out ->
            for (num in numbers) {
                out.println(num)
            }
        }
        Toast.makeText(this, "TXT fix done. Output: ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }
        findViewById<Button>(R.id.btn_select_txt_file).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "text/plain"
            startActivityForResult(intent, PICK_TXT_CODE)
        }
    }

    private fun prepareListData() {
        listDataHeader = listOf(getString(R.string.extract_text))
        listDataChild = hashMapOf(
            listDataHeader[0] to listOf(
                getString(R.string.extract_all),
                getString(R.string.extract_text_only),
                getString(R.string.extract_numbers_only),
                getString(R.string.extract_7plus_digits)
            )
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGES_CODE -> {
                    imageUris.clear()
                    data?.let {
                        if (it.clipData != null) {
                            val count = it.clipData!!.itemCount
                            for (i in 0 until count) {
                                val imageUri = it.clipData!!.getItemAt(i).uri
                                imageUris.add(imageUri)
                            }
                        } else if (it.data != null) {
                            imageUris.add(it.data!!)
                        }
                    }
                    Toast.makeText(this, "${imageUris.size} images selected", Toast.LENGTH_SHORT).show()
                    updateImagePreviews()
                }
                PICK_ZIP_CODE -> {
                    // TODO: Unzip and add image URIs
                    Toast.makeText(this, "ZIP upload not yet implemented", Toast.LENGTH_SHORT).show()
                }
                2001 -> {
                    // Crop result from CropActivity
                    val rect = data?.getParcelableExtra<android.graphics.Rect>("cropRect")
                    cropRect = rect
                    Toast.makeText(this, "Crop area set", Toast.LENGTH_SHORT).show()
                }
                PICK_TXT_CODE -> {
                    outputTxtUri = data?.data
                    Toast.makeText(this, "TXT file selected", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
