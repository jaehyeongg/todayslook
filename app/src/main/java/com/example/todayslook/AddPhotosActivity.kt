package com.example.todayslook

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.example.todayslook.databinding.ActivityAddPhotosBinding
import com.example.todayslook.model.ContentModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date

class AddPhotosActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddPhotosBinding
    lateinit var storage: FirebaseStorage
    lateinit var  auth : FirebaseAuth
    lateinit var firestore : FirebaseFirestore
    var photoUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = FirebaseStorage.getInstance()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_photos)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        var i = Intent(Intent.ACTION_PICK)
        i.type = "image/*"
        photoResult.launch(i)
        binding.addphotoBtnUpload.setOnClickListener {
            contentUpload()
        }
    }

    fun contentUpload() {
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMAGE+"+ timestamp+".png"

        var storagePath = storage.reference?.child("images")?.child(imageFileName)


        storagePath?.putFile(photoUri!!)?.continueWithTask {
            return@continueWithTask storagePath.downloadUrl
        }?.addOnCompleteListener {
                downloadUrl->

            var contentModel = ContentModel()

            contentModel.imageUrl = downloadUrl.result.toString()
            contentModel.explain =binding.addphotoEditExplain.text.toString()
            contentModel.uId = auth?.uid
            contentModel.userId = auth?.currentUser?.email
            contentModel.timestamp = System.currentTimeMillis()

            firestore.collection("images").document().set(contentModel)

            Toast.makeText(this,"업로드에 성공했습니다.",Toast.LENGTH_LONG).show()
            finish()

        }
        }


    var photoResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        //사진 받는 부분
            result ->
        photoUri = result.data?.data
        binding.uploadImageview.setImageURI(photoUri)
    }
}