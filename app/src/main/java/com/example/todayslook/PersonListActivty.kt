package com.example.todayslook


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todayslook.databinding.ActivityPersonListActivtyBinding
import com.example.todayslook.databinding.ItemPersonBinding
import com.example.todayslook.fragment.UserFragment
import com.example.todayslook.model.FollowModel

class PersonListActivty : AppCompatActivity() {
    lateinit var binding: ActivityPersonListActivtyBinding
    var following = false
    lateinit var followModel: FollowModel
    lateinit var ids: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        following = intent.getBooleanExtra("Mode", false)
        val followModels: List<FollowModel> = intent.getParcelableArrayListExtra("FM", FollowModel::class.java) ?: emptyList()



        followModel = followModels?.get(0) ?: FollowModel()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_person_list_activty)
        binding.personRecyclerview.adapter = PersonListAdapter()
        binding.personRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.toolbarBtnBack.setOnClickListener {
            finish()
        }
        if (following) {
            ids = followModel.followings.values.toTypedArray()
        } else {
            ids = followModel.followers.values.toTypedArray()
        }
    }

    inner class ItemPersonViewHolder(var binding: ItemPersonBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class PersonListAdapter : RecyclerView.Adapter<ItemPersonViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemPersonViewHolder {
            val view = ItemPersonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ItemPersonViewHolder(view)
        }

        override fun getItemCount(): Int {
            return ids.size
        }

        override fun onBindViewHolder(holder: ItemPersonViewHolder, position: Int) {
            val userId = ids[position]

            // 설정된 userId에 대한 클릭 이벤트 추가
            holder.itemView.setOnClickListener {
                // 사용자 페이지로 이동
                moveToUserPage(userId)
            }

            // 사용자 아이디 설정
            holder.binding.profileTextview.text = userId
            holder.binding.messageTextview.visibility = View.INVISIBLE
        }

        private fun moveToUserPage(userId: String) {
            val userFragment = UserFragment()
            val bundle = Bundle()
            bundle.putString("userId", userId)
            userFragment.arguments = bundle

            // 사용자 페이지로 이동
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_content, userFragment)
                .addToBackStack(null)  // 뒤로 가기 스택에 추가
                .commit()
        }
    }
}