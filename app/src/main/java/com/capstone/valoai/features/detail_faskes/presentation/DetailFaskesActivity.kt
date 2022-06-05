package com.capstone.valoai.features.detail_faskes.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.capstone.valoai.databinding.ActivityDetailFaskesBinding
import com.capstone.valoai.features.detail_faskes.data.models.FaskesModel

class DetailFaskesActivity : AppCompatActivity() {
    private var faskesData: FaskesModel? = null

    private lateinit var binding: ActivityDetailFaskesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailFaskesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        faskesData = intent.getParcelableExtra(FASKES_EXTRA_NAME)

        with(binding){
            faskesTitle.text = faskesData?.name
            faskesName.text = faskesData?.name

            btnShowRute.setOnClickListener {
                val mapsRuteUri =
                    Uri.parse("google.navigation:q=${faskesData?.latitude},${faskesData?.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, mapsRuteUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }

            btnBack.setOnClickListener {
                finish()
            }
        }

    }


    companion object{
        const val FASKES_EXTRA_NAME = "faskes data"
    }
}