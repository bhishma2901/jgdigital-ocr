package com.jgdigital.ocr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 100;
    private static final int REQUEST_PICK_FOLDER = 101;
    private static final int REQUEST_PICK_ZIP = 102;
    private static final int REQUEST_PICK_TXT = 103;

    private Button btnSelectFolder, btnSelectZip, btnSelectTxt;
    private ExpandableListView expandableListView;
    private ExpandableListAdapter listAdapter;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        btnSelectFolder = findViewById(R.id.btnSelectFolder);
        btnSelectZip = findViewById(R.id.btnSelectZip);
        btnSelectTxt = findViewById(R.id.btnSelectTxt);
        expandableListView = findViewById(R.id.expandableListView);

        // Prepare expandable list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        expandableListView.setAdapter(listAdapter);

        // Set click listeners
        btnSelectFolder.setOnClickListener(v -> checkPermissionsAndOpenFolder());
        btnSelectZip.setOnClickListener(v -> checkPermissionsAndOpenZip());
        btnSelectTxt.setOnClickListener(v -> checkPermissionsAndOpenTxt());

        // ExpandableListView item click listener
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String selectedItem = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
            Toast.makeText(MainActivity.this, "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
            // Here you would handle the OCR type selection
            return false;
        });
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        // Adding headers
        listDataHeader.add("Extraction Type");

        // Adding child data
        List<String> extractionOptions = new ArrayList<>();
        extractionOptions.add("Extract all text in images");
        extractionOptions.add("Only text extract");
        extractionOptions.add("Only numbers extract");
        extractionOptions.add("7+ digit numbers extract");

        listDataChild.put(listDataHeader.get(0), extractionOptions);
    }

    private void checkPermissionsAndOpenFolder() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        } else {
            openFolderPicker();
        }
    }

    private void checkPermissionsAndOpenZip() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        } else {
            openZipPicker();
        }
    }

    private void checkPermissionsAndOpenTxt() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        } else {
            openTxtPicker();
        }
    }

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_PICK_FOLDER);
    }

    private void openZipPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        startActivityForResult(intent, REQUEST_PICK_ZIP);
    }

    private void openTxtPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, REQUEST_PICK_TXT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_FOLDER) {
                Uri treeUri = data.getData();
                // Process the selected folder
                Toast.makeText(this, "Folder selected: " + treeUri.toString(), Toast.LENGTH_SHORT).show();
            } else if (requestCode == REQUEST_PICK_ZIP) {
                Uri zipUri = data.getData();
                // Process the selected zip file
                Toast.makeText(this, "Zip file selected: " + zipUri.toString(), Toast.LENGTH_SHORT).show();
            } else if (requestCode == REQUEST_PICK_TXT) {
                Uri txtUri = data.getData();
                // Process the selected txt file
                Toast.makeText(this, "Text file selected: " + txtUri.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}