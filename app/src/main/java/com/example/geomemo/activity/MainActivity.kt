package com.example.geomemo.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geomemo.GeoMemoApp
import com.example.geomemo.adapter.GeoMemoAdapter
import com.example.geomemo.dao.GeoMemoDao
import com.example.geomemo.databinding.ActivityMainBinding
import com.example.geomemo.model.GeoMemoModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODEL: String = "extra_model"
    }

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.fabAddMemo?.setOnClickListener {
            startActivity(Intent(this, AddMemoActivity::class.java))
        }

        val geoMemoDao: GeoMemoDao = (application as GeoMemoApp).db.geoMemoDao()
        showMemos(geoMemoDao)
    }

    private fun showMemos(geoMemoDao: GeoMemoDao) {
        lifecycleScope.launch {
            geoMemoDao.getAllMemos().collect { memos ->
                if (memos.isEmpty()) {
                    binding?.rvMemos?.visibility = View.GONE
                    binding?.tvNoMemos?.visibility = View.VISIBLE
                } else {
                    binding?.rvMemos?.visibility = View.VISIBLE
                    binding?.tvNoMemos?.visibility = View.GONE
                    binding?.rvMemos?.layoutManager =
                        LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
                    binding?.rvMemos?.adapter = GeoMemoAdapter(memos as ArrayList<GeoMemoModel>)
                    (binding?.rvMemos?.adapter as GeoMemoAdapter).setOnClickListener(object :
                        GeoMemoAdapter.OnClickListener {
                        override fun onClick(position: Int, model: GeoMemoModel) {
                            val intent: Intent =
                                Intent(this@MainActivity, SingleMemoActivity::class.java)
                            intent.putExtra(EXTRA_MODEL, model)
                            startActivity(intent)
                        }
                    })
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}