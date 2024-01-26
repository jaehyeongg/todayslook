package com.example.todayslook
import com.example.todayslook.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun saveUser(userId: String, username: String, email: String) {
        val user = UserModel(userId, username, email)
        usersCollection.document(userId).set(user).await()
    }

    suspend fun getUser(userId: String): UserModel? {
        val documentSnapshot = usersCollection.document(userId).get().await()
        return documentSnapshot.toObject(UserModel::class.java)
    }
}