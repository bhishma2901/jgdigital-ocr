package com.jgdigital.ocr

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropView
import java.io.File

class CropActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private var cropRect: android.graphics.Rect? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)
        imageUri = intent.getStringExtra("imageUri")?.let { Uri.parse(it) }
        val cropButton = findViewById<Button>(R.id.btn_crop)
        cropButton.setOnClickListener {
            // For demo, just return a dummy rect
            val resultIntent = Intent()
            resultIntent.putExtra("cropRect", android.graphics.Rect(100, 100, 500, 500))
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
