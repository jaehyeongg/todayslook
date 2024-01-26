package com.example.todayslook
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.todayslook.databinding.ActivityAddClothBinding
import com.example.todayslook.model.ClothModel
import com.google.firebase.firestore.FirebaseFirestore


import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
import java.util.*
    class AddClothActivity : AppCompatActivity() {

        private lateinit var selectedImageUri: Uri
        private lateinit var imageView: ImageView
        private lateinit var btnChoose: Button
        private lateinit var btnUpload: Button

        private lateinit var storage: FirebaseStorage
        private lateinit var firestore: FirebaseFirestore

        private val getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data: Intent? = result.data
                    data?.data?.let {
                        selectedImageUri = it
                        imageView.setImageURI(it)
                    }
                }
            }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_add_cloth)

            imageView = findViewById(R.id.imageView)
            btnChoose = findViewById(R.id.btnChoose)
            btnUpload = findViewById(R.id.btnUpload)

            storage = FirebaseStorage.getInstance()
            firestore = FirebaseFirestore.getInstance()

            btnChoose.setOnClickListener {
                openImageChooser()
            }

            btnUpload.setOnClickListener {
                uploadImageToFirestore()
            }
        }

        private fun openImageChooser() {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            getContent.launch(intent)
        }

        private fun uploadImageToFirestore() {
            val storageRef: StorageReference =
                storage.reference.child("clothImages").child("unique_image_name.jpg")

            storageRef.putFile(selectedImageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // 이미지 업로드 성공
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        // 업로드한 이미지의 다운로드 URL 획득
                        val imageUrl = uri.toString()

                        // TODO: Firestore에 imageUrl을 저장하는 작업 수행
                        saveImageUrlToFirestore(imageUrl)
                    }
                }
                .addOnFailureListener { exception ->
                    // 이미지 업로드 실패
                    // 실패에 대한 처리를 추가하세요.
                }
        }

        private fun saveImageUrlToFirestore(imageUrl: String) {
            val clothModel = ClothModel(imageUrl)

            firestore.collection("clothImages")
                .add(clothModel)
                .addOnSuccessListener {
                    // 이미지 데이터 저장 성공
                    // 성공에 대한 처리를 추가하세요.
                }
                .addOnFailureListener { exception ->
                    // 이미지 데이터 저장 실패
                    // 실패에 대한 처리를 추가하세요.
                }
        }
    }