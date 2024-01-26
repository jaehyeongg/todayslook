package com.example.todayslook.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.todayslook.R
import com.example.todayslook.databinding.FragmentAlarmBinding
import com.example.todayslook.databinding.ItemPersonBinding
import com.example.todayslook.model.AlarmModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AlarmFragment : Fragment() {
    lateinit var binding: FragmentAlarmBinding
    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_alarm, container, false)
        binding.alarmRecyclerview.adapter = AlarmAdapter()
        binding.alarmRecyclerview.layoutManager = LinearLayoutManager(activity)
        return binding.root
    }

    inner class ItemPersonViewHolder(var binding: ItemPersonBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class AlarmAdapter : RecyclerView.Adapter<ItemPersonViewHolder>() {
        var alarmList = arrayListOf<AlarmModel>()

        init {
            val uid = auth.uid
            firestore.collection("alarms").whereEqualTo("destinationUid", uid)
                .addSnapshotListener { value, error ->
                    alarmList.clear()
                    for (item in value!!.documents) {
                        val alarmModel = item.toObject(AlarmModel::class.java)
                        alarmModel?.userUid = item.getString("userUid")
                        alarmList.add(alarmModel!!)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemPersonViewHolder {
            val view = ItemPersonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ItemPersonViewHolder(view)
        }

        override fun onBindViewHolder(holder: ItemPersonViewHolder, position: Int) {
            val view = holder.binding
            val alarmModel = alarmList[position]

            view.messageTextview.visibility = View.INVISIBLE
            when (alarmModel.kind) {
                0 -> {
                    val m = alarmModel.userId + "가 좋아요를 눌렀습니다."
                    view.profileTextview.text = m
                }
                1 -> {
                    val m_1 =
                        alarmModel.userId + "가" + (alarmModel.message ?: "") + "라는 메세지를 남겼습니다."
                    view.profileTextview.text = m_1
                }
                2 -> {
                    val m_2 = alarmModel.userId + "가 나를 팔로우 했습니다."
                    view.profileTextview.text = m_2
                }
            }

            // imageUrl이 null이 아닌 경우에만 Glide 사용
            alarmModel.userUid?.let { userUid ->
                getProfileImage(userUid) { imageUrl ->
                    if (!imageUrl.isNullOrBlank()) {
                        Glide.with(holder.itemView.context).load(imageUrl)
                            .apply(RequestOptions().circleCrop()).into(view.profileImageview)
                    }
                }
            }
        }

        fun getProfileImage(userUid: String, callback: (String?) -> Unit) {
            firestore.collection("profileImages").document(userUid).get()
                .addOnSuccessListener { documentSnapshot ->
                    val imageUrl = documentSnapshot.getString("image")
                    Log.d("ProfileImage", "Profile image URL: $imageUrl")
                    callback(imageUrl)
                }
                .addOnFailureListener { e ->
                    Log.e("ProfileImageError", "Error getting profile image", e)
                    callback(null)
                }
        }

        override fun getItemCount(): Int {
            return alarmList.size
        }
    }
}