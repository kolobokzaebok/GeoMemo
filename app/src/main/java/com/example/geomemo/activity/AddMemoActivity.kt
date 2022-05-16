package com.example.geomemo.activity

import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.geomemo.GeoMemoApp
import com.example.geomemo.R
import com.example.geomemo.dao.GeoMemoDao
import com.example.geomemo.databinding.ActivityAddMemoBinding
import com.example.geomemo.model.GeoMemoModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import kotlinx.coroutines.launch

class AddMemoActivity : AppCompatActivity() {

    private var imagePath: Uri? = null
    private var memoLatitude: Double? = null
    private var memoLongitude: Double? = null

    companion object {
        private const val NAVBAR_TITLE: String = "Add New Memo"
        private const val DIALOG_DEFAULT_TITLE: String = "Add Memo App"
        private const val IMAGE_DIRECTORY = "GeoMemoImages"

        private const val STORAGE_ACCESS_RATIONALE: String =
            "Your permission to access image storage is required"
        private const val DENIED_READ_STORAGE_PERMISSION: String =
            "Permission to read from storage was denied."

        private const val CAMERA_ACCESS_RATIONALE: String =
            "Your permission to access camera is required"
        private const val DENIED_CAMERA_PERMISSION: String =
            "Permission to use camera was denied."
    }

    private var binding: ActivityAddMemoBinding? = null

    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                @Suppress("DEPRECATION")
                val bitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, result.data!!.data)
                imagePath = saveImageToInternalStorage(bitmap)
                binding?.ivPlaceImage?.setImageURI(imagePath)
            }
        }

    private val openCameraLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                imagePath = saveImageToInternalStorage(result.data?.extras?.get("data") as Bitmap)
                binding?.ivPlaceImage?.setImageURI(imagePath)
            }
        }

    private val mapsLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val place: Place = Autocomplete.getPlaceFromIntent(result.data!!)
                binding?.etLocation?.setText(place.address)
                memoLatitude = place.latLng!!.latitude
                memoLongitude = place.latLng!!.longitude
            }
        }

    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                when (permissionName) {
                    Manifest.permission.READ_EXTERNAL_STORAGE -> {
                        if (isGranted) {
                            this.openGalleryLauncher.launch(
                                Intent(
                                    Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                )
                            )
                        } else {
                            Toast.makeText(this, DENIED_READ_STORAGE_PERMISSION, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                    Manifest.permission.CAMERA -> {
                        if (isGranted) {
                            this.openCameraLauncher.launch(
                                Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            )
                        } else {
                            Toast.makeText(this, DENIED_CAMERA_PERMISSION, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMemoBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val geoMemoDao: GeoMemoDao = (application as GeoMemoApp).db.geoMemoDao()

        setToolBar()
        chooseDate()
        chooseImage()
        chooseLocation()
        saveGeoMemo(geoMemoDao)

        if (!Places.isInitialized()) {
            Places.initialize(this, resources.getString(R.string.gmaps_api))
        }
    }

    private fun chooseLocation() {
        val listOfFields =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent: Intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, listOfFields)
                .build(this@AddMemoActivity)
        binding?.etLocation?.setOnClickListener {
            mapsLauncher.launch(intent)
        }
    }

    private fun saveGeoMemo(geoMemoDao: GeoMemoDao) {
        binding?.btnSave?.setOnClickListener {
            when {
                binding?.etTitle?.text!!.isBlank() -> Toast.makeText(
                    this,
                    "Please, provide Memo title",
                    Toast.LENGTH_SHORT
                ).show()
                binding?.etDescription?.text!!.isBlank() -> Toast.makeText(
                    this,
                    "Please, provide Memo description",
                    Toast.LENGTH_SHORT
                ).show()
                binding?.etDate?.text!!.isBlank() -> Toast.makeText(
                    this,
                    "Please, provide Memo date",
                    Toast.LENGTH_SHORT
                ).show()
                binding?.etLocation?.text!!.isBlank() -> Toast.makeText(
                    this,
                    "Please, provide Memo location",
                    Toast.LENGTH_SHORT
                ).show()
                imagePath == null -> Toast.makeText(
                    this,
                    "Please, select Memo image",
                    Toast.LENGTH_SHORT
                ).show()
                else -> {
                    saveMemo(
                        geoMemoDao, GeoMemoModel(
                            title = binding?.etTitle?.text.toString(),
                            imagePath = imagePath.toString(),
                            description = binding?.etDescription?.text.toString(),
                            date = binding?.etDate?.text.toString(),
                            location = binding?.etLocation?.text.toString(),
                            latitude = memoLatitude ?: 0.0,
                            longitude = memoLongitude ?: 0.0
                        )
                    )
                }
            }
        }
    }

    private fun saveMemo(geoMemoDao: GeoMemoDao, geoMemoModel: GeoMemoModel) {
        lifecycleScope.launch {
            geoMemoDao.insert(geoMemoModel)
        }
    }

    private fun setToolBar() {
        setSupportActionBar(binding?.tbAddMemo)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = NAVBAR_TITLE
        }
        binding?.tbAddMemo?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun chooseImage() {
        binding?.tvAddImage?.setOnClickListener {
            val imageDialog = AlertDialog.Builder(this)
            imageDialog.setTitle("Select Image")
            imageDialog.setItems(arrayOf("Select from Gallery", "Take photo")) { _, which ->
                when (which) {
                    0 -> requestStoragePermission()
                    1 -> requestCameraPermission()
                }
            }
            imageDialog.show()
        }
    }

    private fun chooseDate() {
        binding?.etDate?.setOnClickListener {
            val myCalendar: Calendar = Calendar.getInstance()
            val year: Int = myCalendar.get(Calendar.YEAR)
            val month: Int = myCalendar.get(Calendar.MONTH)
            val day: Int = myCalendar.get(Calendar.DAY_OF_MONTH)
            var selectedDate: String

            DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    selectedDate = "${selectedMonth + 1}/$selectedDay/$selectedYear"
                    binding?.etDate?.setText(selectedDate)
                },
                year,
                month,
                day
            ).show()
        }
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            showRationaleDialog(STORAGE_ACCESS_RATIONALE)
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            showRationaleDialog(CAMERA_ACCESS_RATIONALE)
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    private fun showRationaleDialog(message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle(DIALOG_DEFAULT_TITLE)
            .setMessage(message)
            .setPositiveButton("cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            throw e
        }

        return Uri.parse(file.absolutePath)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}