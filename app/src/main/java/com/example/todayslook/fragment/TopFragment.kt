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

class TopFragment : Fragment() {
    private val GALLERY_REQUEST_CODE = 123
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddTop: Button

    private val topItems = mutableListOf<ClothModel>()
    private val firestore = FirebaseFirestore.getInstance()

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { selectedImageUri ->
                    // 선택한 이미지의 Uri를 이용하여 Firestore에 추가
                    addTopItem("New Top", selectedImageUri.toString())
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_top, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewTop)
        btnAddTop = view.findViewById(R.id.btnAddTop)

        // RecyclerView 설정
        recyclerView.layoutManager = GridLayoutManager(activity, 3)
        val adapter = TopAdapter(topItems)
        recyclerView.adapter = adapter

        // Firestore에서 옷 데이터 가져오기
        fetchTopItems()

        // 추가하기 버튼 클릭 이벤트
        btnAddTop.setOnClickListener {
            // 갤러리에서 이미지를 선택하기 위한 Intent
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            launcher.launch(galleryIntent)
        }

        return view
    }

    private fun fetchTopItems() {
        firestore.collection("tops").get().addOnSuccessListener { result ->
            topItems.clear()
            for (document in result) {
                val cloth = document.toObject(ClothModel::class.java)
                topItems.add(cloth)
            }
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private class TopAdapter(private val items: List<ClothModel>) :
        RecyclerView.Adapter<TopAdapter.TopViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_top, parent, false)
            return TopViewHolder(view)
        }

        override fun onBindViewHolder(holder: TopViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item)
        }

        override fun getItemCount(): Int {
            return items.size
        }

        inner class TopViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(item: ClothModel) {

                // 이미지 로딩은 Glide 또는 Picasso 등을 활용하세요
                Glide.with(itemView)
                    .load(item.imageUrl)
                    .into(itemView.findViewById(R.id.imgTopItem))
            }
        }
    }

    private fun addTopItem(name: String, imageUrl: String) {
        val cloth = ClothModel(name = name, imageUrl = imageUrl)
        firestore.collection("tops").add(cloth)
    }
}