package com.example.todayslook.fragment
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.todayslook.R
import com.example.todayslook.model.ClothModel
import com.google.firebase.firestore.FirebaseFirestore

class BottomFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddBottom: Button
    private val bottomItems = mutableListOf<ClothModel>()
    private val firestore = FirebaseFirestore.getInstance()
    private val GALLERY_REQUEST_CODE = 123

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bottom, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewBottom)
        btnAddBottom = view.findViewById(R.id.btnAddBottom)

        // RecyclerView 설정
        recyclerView.layoutManager = GridLayoutManager(activity, 3)
        val adapter = BottomAdapter(bottomItems)
        recyclerView.adapter = adapter

        // Firestore에서 옷 데이터 가져오기
        fetchBottomItems()

        // 추가하기 버튼 클릭 이벤트
        btnAddBottom.setOnClickListener {
            // 갤러리에서 이미지를 선택하기 위한 Intent
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // 이미지를 선택했을 때의 처리
            val selectedImageUri: Uri = data.data!!
            // 선택한 이미지의 Uri를 이용하여 Firestore에 추가
            addBottomItem("New Bottom", selectedImageUri.toString())
        }
    }

    private fun fetchBottomItems() {
        firestore.collection("bottoms").get().addOnSuccessListener { result ->
            bottomItems.clear()
            for (document in result) {
                val cloth = document.toObject(ClothModel::class.java)
                bottomItems.add(cloth)
            }
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private class BottomAdapter(private val items: List<ClothModel>) :
        RecyclerView.Adapter<BottomAdapter.BottomViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_bottom, parent, false)
            return BottomViewHolder(view)
        }

        override fun onBindViewHolder(holder: BottomViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item)
        }

        override fun getItemCount(): Int {
            return items.size
        }

        inner class BottomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(item: ClothModel) {
                // 이미지 로딩은 Glide 또는 Picasso 등을 활용하세요
                Glide.with(itemView)
                    .load(item.imageUrl)
                    .into(itemView.findViewById(R.id.imgBottomItem))
            }
        }
    }

    private fun addBottomItem(name: String, imageUrl: String) {
        val cloth = ClothModel(name = name, imageUrl = imageUrl)
        firestore.collection("bottoms").add(cloth)
    }
}
