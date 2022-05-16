package com.example.geomemo.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.geomemo.R
import com.example.geomemo.databinding.ActivityMapBinding
import com.example.geomemo.model.GeoMemoModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val DEFAULT_ZOOM: Float = 10f
    }

    var binding: ActivityMapBinding? = null
    var model: GeoMemoModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        model = intent.getSerializableExtra(MainActivity.EXTRA_MODEL) as GeoMemoModel
        setToolBar()
        supportMapFragment()
    }

    private fun setToolBar() {
        setSupportActionBar(binding?.tbMap)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = model?.title
        }
        binding?.tbMap?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun supportMapFragment() {
        val smf: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        smf.getMapAsync(this)
    }

    override fun onMapReady(gmap: GoogleMap) {
        val latLng = LatLng(model!!.latitude, model!!.longitude)
        gmap.addMarker(MarkerOptions().position(latLng).title(model!!.location))
        gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
    }
}