package com.example.todayslook.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.todayslook.AddPhotosActivity
import com.example.todayslook.CommentActivity
import com.example.todayslook.ImageCache
import com.example.todayslook.R
import com.example.todayslook.RankActivity
import com.example.todayslook.databinding.FragmentDetailViewBinding
import com.example.todayslook.databinding.ItemDetailBinding
import com.example.todayslook.model.AlarmModel
import com.example.todayslook.model.ContentModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DetailViewFragment : Fragment() {
    lateinit var binding: FragmentDetailViewBinding
    lateinit var firestore: FirebaseFirestore
    lateinit var uid: String
    lateinit var auth: FirebaseAuth
    private val topItems = mutableListOf<ContentModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_view, container, false)
        uid = FirebaseAuth.getInstance().uid!!
        binding.detailviewRecycleView.adapter = DetailviewRecyclerviewAdapter()

        binding.detailviewRecycleView.layoutManager = LinearLayoutManager(activity)
        binding.rankBtn.setOnClickListener {
            startActivity(Intent(context, RankActivity::class.java))
        }

        return binding.root
    }

    inner class DetailViewHolder(var binding: ItemDetailBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class DetailviewRecyclerviewAdapter() : RecyclerView.Adapter<DetailViewHolder>() {

        var contentModels = arrayListOf<ContentModel>()
        var contentUidsList = arrayListOf<String>()
        private val followingUidsList = mutableListOf<String>()

        init {
            fetchTopItems()
        }

        private fun fetchTopItems() {
            firestore.collection("images")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    contentModels.clear()
                    contentUidsList.clear()
                    for (item in result.documents) {
                        val contentModel = item.toObject(ContentModel::class.java)
                        contentModels.add(contentModel!!)
                        contentUidsList.add(item.id)
                    }
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
            var view =
                ItemDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return DetailViewHolder(view)
        }

        override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
            var contentModel = contentModels.get(position)
            var viewHolder = holder.binding
            viewHolder.likeTextview.text = "Likes " + contentModel.favoriteCount
            viewHolder.profileTextview.text = contentModel.userId
            viewHolder.explainTextview.text = contentModel.explain
            viewHolder.favoriteImageview.setOnClickListener {
                eventFavorite(position)
            }

            viewHolder.profileTextview.setOnClickListener {
                var userFragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("dUid", contentModel.uId)
                bundle.putString("userId", contentModel.userId)
                userFragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.main_content, userFragment)?.commit()
            }

            if (contentModel.favorites.containsKey(uid)) {
                viewHolder.favoriteImageview.setImageResource(R.drawable.ic_favorite)
            } else {
                viewHolder.favoriteImageview.setImageResource(R.drawable.ic_favorite_border)
            }

            Glide.with(holder.itemView.context).load(contentModel.imageUrl)
                .into(viewHolder.contentImageview)

            viewHolder.commentImageview.setOnClickListener {
                var i = Intent(activity, CommentActivity::class.java)
                i.putExtra("dUid", contentUidsList[position])
                startActivity(i)
            }

            ImageCache.instance.getBitmap(activity!!, contentModel.uId!!, viewHolder.profileImageview)
        }

        fun favoriteAlarm(dUid: String) {
            var alarmModel = AlarmModel()
            alarmModel.destinationUid = dUid
            alarmModel.userId = auth.currentUser?.email
            alarmModel.uid = auth.uid
            alarmModel.kind = 0
            alarmModel.timestamp = System.currentTimeMillis()

            firestore.collection("alarms").document().set(alarmModel)
        }

        override fun getItemCount(): Int {
            return contentModels.size
        }

         fun eventFavorite(position: Int) {
            val docId = contentUidsList[position]
            val tsDoc = firestore.collection("images").document(docId)

            firestore.runTransaction { transition ->
                val contentDTO = transition.get(tsDoc).toObject(ContentModel::class.java)

                if (contentDTO!!.favorites.containsKey(uid)) {
                    contentDTO.favoriteCount = contentDTO.favoriteCount - 1
                    contentDTO.favorites.remove(uid)
                } else {
                    contentDTO.favoriteCount = contentDTO.favoriteCount + 1
                    contentDTO.favorites[uid] = true
                    favoriteAlarm(contentDTO.uId!!)
                }

                transition.set(tsDoc, contentDTO)
                contentModels[position] = contentDTO // 리스트 업데이트
            }.addOnCompleteListener {
                notifyDataSetChanged() // 데이터 변경 후 어댑터에 알림
            }
        }
    }
}