package com.example.geomemo.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.geomemo.databinding.ActivitySingleMemoBinding
import com.example.geomemo.model.GeoMemoModel

class SingleMemoActivity : AppCompatActivity() {

    private var binding: ActivitySingleMemoBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleMemoBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val model = intent.getSerializableExtra(MainActivity.EXTRA_MODEL) as GeoMemoModel
        setToolBar(model.title)
        populateTemplate(model)
        onViewMapClick(model)
    }

    private fun setToolBar(title: String) {
        setSupportActionBar(binding?.tbSingleMemo)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = title
        }
        binding?.tbSingleMemo?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun onViewMapClick(model: GeoMemoModel) {
        binding?.btnViewOnMap?.setOnClickListener {
            val intent = Intent(this@SingleMemoActivity, MapActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_MODEL, model)
            startActivity(intent)
        }
    }

    private fun populateTemplate(model: GeoMemoModel) {
        binding?.ivPlaceImage?.setImageURI(Uri.parse(model.imagePath))
        binding?.tvDescription?.text = model.description
        binding?.tvLocation?.text = model.location
    }
}