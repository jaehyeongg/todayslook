package com.example.todayslook.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.todayslook.R
import com.example.todayslook.model.ClothModel
import com.google.firebase.firestore.FirebaseFirestore

class AccFragment : Fragment() {
    private val GALLERY_REQUEST_CODE = 123
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddAcc: Button

    private val accItems = mutableListOf<ClothModel>()
    private val firestore = FirebaseFirestore.getInstance()

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { selectedImageUri ->
                    // 선택한 이미지의 Uri를 이용하여 Firestore에 추가
                    addAccItem("New Accessory", selectedImageUri.toString())
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_acc, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewAcc)
        btnAddAcc = view.findViewById(R.id.btnAddAcc)

        // RecyclerView 설정
        recyclerView.layoutManager = GridLayoutManager(activity, 3)
        val adapter = AccAdapter(accItems)
        recyclerView.adapter = adapter

        // Firestore에서 옷 데이터 가져오기
        fetchAccItems()

        // 추가하기 버튼 클릭 이벤트
        btnAddAcc.setOnClickListener {
            // 갤러리에서 이미지를 선택하기 위한 Intent
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            launcher.launch(galleryIntent)
        }

        return view
    }

    private fun fetchAccItems() {
        firestore.collection("accessories").get().addOnSuccessListener { result ->
            accItems.clear()
            for (document in result) {
                val cloth = document.toObject(ClothModel::class.java)
                accItems.add(cloth)
            }
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private class AccAdapter(private val items: List<ClothModel>) :
        RecyclerView.Adapter<AccAdapter.AccViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_acc, parent, false)
            return AccViewHolder(view)
        }

        override fun onBindViewHolder(holder: AccViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item)
        }

        override fun getItemCount(): Int {
            return items.size
        }

        inner class AccViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(item: ClothModel) {
                // 이미지 로딩은 Glide 또는 Picasso 등을 활용하세요
                Glide.with(itemView)
                    .load(item.imageUrl)
                    .into(itemView.findViewById(R.id.imgAccItem))
            }
        }
    }

    private fun addAccItem(name: String, imageUrl: String) {
        val cloth = ClothModel(name = name, imageUrl = imageUrl)
        firestore.collection("accessories").add(cloth)
    }
}
