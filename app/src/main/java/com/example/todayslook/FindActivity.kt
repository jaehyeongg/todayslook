package com.example.todayslook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.todayslook.databinding.ActivityFindBinding
import com.example.todayslook.model.FindidModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FindActivity : AppCompatActivity() {

    lateinit var binding: ActivityFindBinding
    lateinit var firestore: FirebaseFirestore
    lateinit var auth :FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_find)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        binding.findIdButton.setOnClickListener {
            readMyId()
        }
        binding.findPwButton.setOnClickListener {
            var number = binding.edittextEmail.text.toString()
            auth.sendPasswordResetEmail(number)
        }
        binding.dismissbutton.setOnClickListener {
            finish()

        }
    }

    fun readMyId() {
        var number = binding.edittextPhonenumber.text.toString()
        firestore.collection("findids").whereEqualTo("phoneNumber", number).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var findIdModel =
                        task.result?.documents?.first()!!.toObject(FindidModel::class.java)
                    Toast.makeText(this, findIdModel!!.id, Toast.LENGTH_LONG).show()
                }
            }
    }
}