package com.example.todayslook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.example.todayslook.databinding.ActivityLoginBinding
import com.example.todayslook.model.FindidModel
import com.example.todayslook.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.emailLoginButton.setOnClickListener {
            signinAndSignup()
        }

        binding.findIdPasswordButton.setOnClickListener {
            startActivity(Intent(this, FindActivity::class.java))
        }
    }

    private fun signinAndSignup() {
        val id = binding.edittextId.text.toString()
        val password = binding.edittextPassword.text.toString()
        saveFindIdData(id,password,id)

        auth.createUserWithEmailAndPassword(id, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 사용자 생성 성공 시 UID를 가져와서 saveFindIdData() 호출
                val user = task.result?.user
                if (user != null) {
                    val userModel = UserModel(user.uid, id, /*다른 필드에 해당하는 값*/)
                  saveUserData(userModel)

                }
                moveMain(user)
            } else {
                // 이미 아이디가 있을 경우
                signinEmail()
            }
        }
    }


    fun saveUserData(userModel: UserModel) {
        // Firestore의 "users" 컬렉션에 UserModel 데이터를 저장
        val uid = userModel.uid ?: return // uid가 null이면 함수 종료
        firestore.collection("users").document(uid).set(userModel)
            .addOnSuccessListener {
                Log.d("Firestore", "User information added to users successfully!")
                // 로그인 성공 시 메인 화면으로 이동
                moveMain2(userModel.uid)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding user information to users", e)
            }
    }

    private fun saveFindIdData(uid: String, email: String?,id:String?) {
        // FindidModel 객체를 생성하고 findids 컬렉션에 추가하는 코드
        val findidModel = FindidModel(uid, email,id)
        firestore.collection("findids").document(uid).set(findidModel)
            .addOnSuccessListener {
                Log.d("Firestore", "User information added to findids successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding user information to findids", e)
            }

    }

    fun moveMain(user: FirebaseUser?) {
        if (user != null) {
            if (user.isEmailVerified){

            }else
            {
                user.sendEmailVerification()
            }
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
    fun moveMain2(uid: String?) {
        // 사용자 정보를 Firestore에서 가져오는 코드
        firestore.collection("users").document(uid.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Firestore에서 사용자 정보를 성공적으로 가져왔을 때의 처리
                    val userModel = document.toObject(UserModel::class.java)
                    if (userModel != null) {
                        // userModel이 null이 아니라면 MainActivity로 이동
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("userModel", userModel)
                        startActivity(intent)
                        finish()
                    } else {
                        // userModel이 null인 경우에 대한 처리
                        Log.e("moveMain", "UserModel is null")
                        // 예: 사용자에게 알림 등
                    }
                } else {
                    // 해당 사용자의 문서가 없을 때의 처리
                    Log.e("moveMain", "User document does not exist")
                    // 예: 사용자에게 알림 등
                }
            }
            .addOnFailureListener { exception ->
                // Firestore에서 데이터를 가져오는 도중 에러가 발생한 경우의 처리
                Log.e("Firestore", "Error getting user information", exception)
                // 예: 사용자에게 알림 등
            }
    }

    private fun signinEmail() {
        val id = binding.edittextId.text.toString()
        val password = binding.edittextPassword.text.toString()
        auth.signInWithEmailAndPassword(id, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                moveMain(task.result?.user)
            }
        }
    }
}