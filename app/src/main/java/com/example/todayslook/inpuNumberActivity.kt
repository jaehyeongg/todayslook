package com.example.todayslook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.todayslook.databinding.ActivityInpuNumberBinding
import com.example.todayslook.model.FindidModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class inpuNumberActivity : AppCompatActivity() {
    lateinit var firestore: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityInpuNumberBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_inpu_number)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        binding.applyButton.setOnClickListener {
            savePhoneNumber()
        }
    }

    fun savePhoneNumber() {
        var findIdModel = FindidModel()
        findIdModel.id = auth.currentUser?.email
        findIdModel.phoneNumber=binding.edittextPhonenumber.text.toString()
        firestore.collection("findids").document().set(findIdModel).addOnCompleteListener {
            task->
            if(task.isSuccessful){
                finish()
                auth.currentUser?.sendEmailVerification()
                startActivity(Intent(this,LoginActivity::class.java))
            }
        }
    }

}