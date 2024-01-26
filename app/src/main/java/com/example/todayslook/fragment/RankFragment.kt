package com.example.todayslook.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.todayslook.AddPhotosActivity
import com.example.todayslook.CommentActivity
import com.example.todayslook.ImageCache
import com.example.todayslook.MainActivity
import com.example.todayslook.R
import com.example.todayslook.databinding.ActivityMainBinding
import com.example.todayslook.databinding.FragmentDetailViewBinding
import com.example.todayslook.databinding.ItemDetailBinding
import com.example.todayslook.model.AlarmModel
import com.example.todayslook.model.ContentModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RankFragment : Fragment() {
    lateinit var binding: FragmentDetailViewBinding
    lateinit var firestore: FirebaseFirestore
    lateinit var uid: String
    lateinit var auth:FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_view, container, false)
        uid = FirebaseAuth.getInstance().uid!!

        val adapter = DetailviewRecyclerviewAdapter()
        binding.detailviewRecycleView.adapter = adapter
        adapter.orderByFavoriteCountDescending()
        binding.detailviewRecycleView.layoutManager = LinearLayoutManager(activity)

        binding.feedBtn.setOnClickListener {
            startActivity(Intent(context, MainActivity::class.java))
        }


        return binding.root
    }




    inner class DetailViewHolder(var binding: ItemDetailBinding) :
        RecyclerView.ViewHolder(binding.root)


    inner class DetailviewRecyclerviewAdapter() : RecyclerView.Adapter<DetailViewHolder>() {

        var contentModels = arrayListOf<ContentModel>()
        var contentUidsList = arrayListOf<String>()

        fun orderByFavoriteCountDescending() {
            contentModels.sortByDescending { it.favoriteCount }
            notifyDataSetChanged()
        }
        init {
            firestore.collection("images").addSnapshotListener { value, error ->
                contentModels.clear()
                contentUidsList.clear()

                for (item in value!!.documents) {
                    var contentModel = item.toObject(ContentModel::class.java)
                    contentModels.add(contentModel!!)
                    contentUidsList.add(item.id)
                }
                // 데이터가 변경될 때마다 정렬 메서드 호출
                orderByFavoriteCountDescending()
                notifyDataSetChanged()
            }

            //데이터를 불러오는 코드를 넣음

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
            //행 하나에 어떤 디자인의 xml 넣을지 설정하는 코드
            var view = ItemDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)

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
            //상대방 user 페이지 이동
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
                //이미 좋아요를 누른 상태
                viewHolder.favoriteImageview.setImageResource(R.drawable.ic_favorite)
            } else {
                viewHolder.favoriteImageview.setImageResource(R.drawable.ic_favorite_border)
            }
            Glide.with(holder.itemView.context).load(contentModel.imageUrl)
                .into(viewHolder.contentImageview)

            //코멘트 이동하는 로직
            viewHolder.commentImageview.setOnClickListener {
                var i = Intent(activity, CommentActivity::class.java)
                i.putExtra("dUid",contentUidsList[position])
                //            i.putExtra("dUid",contentModel.uId)
                startActivity(i)
            }

            ImageCache.instance.getBitmap(activity!!,contentModel.uId!!,viewHolder.profileImageview)
        }


        fun favoriteAlarm(dUid: String){
            var alarmModel = AlarmModel()
            alarmModel.destinationUid=dUid
            alarmModel.userId=auth.currentUser?.email
            alarmModel.uid = auth.uid
            alarmModel.kind=0
            alarmModel.timestamp=System.currentTimeMillis()

            firestore.collection("alarms").document().set(alarmModel)
        }


        override fun getItemCount(): Int {

            return contentModels.size
        }

        fun eventFavorite(position: Int) {
            var docId = contentUidsList.get(position)
            var tsDoc = firestore.collection("images").document(docId)
            firestore.runTransaction { transition ->
                var contentDTO = transition.get(tsDoc).toObject(ContentModel::class.java)
                if (contentDTO!!.favorites.containsKey(uid)) {
                    // 좋아요 누른 상태
                    contentDTO.favoriteCount = contentDTO.favoriteCount - 1
                    contentDTO.favorites.remove(uid)

                } else {
                    //좋아유 누르지 않은 상태
                    contentDTO.favoriteCount = contentDTO.favoriteCount + 1
                    contentDTO.favorites[uid] = true
                    favoriteAlarm(contentDTO.uId!!)
                }
                transition.set(tsDoc, contentDTO)

            }
        }
    }




}
