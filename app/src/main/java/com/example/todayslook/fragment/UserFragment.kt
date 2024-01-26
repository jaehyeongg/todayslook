package com.example.todayslook.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.todayslook.ClosestActivity
import com.example.todayslook.LoginActivity
import com.example.todayslook.MainActivity
import com.example.todayslook.PersonListActivty
import com.example.todayslook.R
import com.example.todayslook.databinding.ActivityMainBinding
import com.example.todayslook.databinding.FragmentUserBinding
import com.example.todayslook.databinding.ItemImageviewBinding
import com.example.todayslook.model.ContentModel
import com.example.todayslook.model.FollowModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class UserFragment : Fragment() {
    private lateinit var binding: FragmentUserBinding
    lateinit var binding2: ActivityMainBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var dUid: String? = null
    private var userId: String? = null
    private var currentUid: String? = null
    private var followModel = FollowModel()
    private val myPhotoResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleImageUploadResult(result.data?.data)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {




        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false)
        currentUid = FirebaseAuth.getInstance().uid
        dUid = arguments?.getString("dUid")
        userId = arguments?.getString("userId")
        binding.accountRecylerview.adapter = UserFragmentRecyclerviewAdapter()
        binding.accountRecylerview.layoutManager = GridLayoutManager(activity, 3)

        binding.closest.setOnClickListener {
            startActivity(Intent(context, ClosestActivity::class.java))
        }
        if (currentUid == dUid) {
            setupForCurrentUser()
        } else {
            setupForOtherUser()
        }

        getProfileImage()
        getFollowingCount()

        binding.followerLinearlayout.setOnClickListener {
            startActivityWithFollowMode(false)
        }

        binding.followingLinearlayout.setOnClickListener {
            startActivityWithFollowMode(true)
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getProfileImage()
    }

    fun getFollowingCount() {
        firestore.collection("users").document(dUid!!)
            .addSnapshotListener { value, error ->
                if (value == null) return@addSnapshotListener
                var followModel = value.toObject(FollowModel::class.java)
                if (followModel?.followerCount != null) {
                    // 연예인이 스토커 관리하는 부분
                    binding.accountFollowerTextview.text = followModel?.followerCount.toString()

                    // 현재 로그인한 사용자의 UID가 선택한 사용자를 팔로우한 경우
                    if (currentUid != dUid) {
                        if (followModel.followers.containsKey(currentUid!!)) {
                            binding.accountBtnFollowSignout.text = activity?.getText(R.string.follow_cancel)
                        } else {
                            binding.accountBtnFollowSignout.text = activity?.getText(R.string.follow)
                        }
                    }
                }

                if (followModel?.followingCount != null) {
                    // 스토커가 연예인 카운트
                    binding.accountFollowingTextview.text = followModel.followingCount.toString()
                }
            }
    }

    private fun setupForCurrentUser() {
        val mainActivity = activity as? MainActivity
        mainActivity?.binding?.apply {
            toolbarLogo.visibility = View.VISIBLE
            toolbarUsername.visibility = View.INVISIBLE
            toolbarBtnBack.visibility = View.INVISIBLE
        }

        binding.accountBtnFollowSignout.text = activity?.getText(R.string.signout)
        binding.accountBtnFollowSignout.setOnClickListener {
            auth.signOut()
            activity?.finish()
            startActivity(Intent(activity, LoginActivity::class.java))
        }

        binding.accountIvProfile.setOnClickListener {
            openImagePicker()
        }
    }

    private fun setupForOtherUser() {
        val mainActivity = activity as? MainActivity
        mainActivity?.binding?.apply {
            toolbarLogo.visibility = View.INVISIBLE
            toolbarUsername.visibility = View.VISIBLE
            toolbarBtnBack.visibility = View.VISIBLE
            toolbarUsername.text = userId
            toolbarBtnBack.setOnClickListener {
                mainActivity.binding.bottomNavigation.selectedItemId = R.id.action_home
            }
        }

        binding.accountBtnFollowSignout.text = activity?.getText(R.string.follow)
        binding.accountBtnFollowSignout.setOnClickListener {
            requestFollwerAndFollowing()
        }
    }

    fun requestFollwerAndFollowing() {
        // 현재 로그인한 사용자의 UID와 이메일
        val currentUid = auth.currentUser?.uid
        val currentEmail = auth.currentUser?.email

        // 현재 로그인한 사용자가 팔로우하려는 대상 사용자의 UID와 이메일
        val targetUid = dUid
        val targetEmail = userId

        if (currentUid == null || currentEmail == null || targetUid == null || targetEmail == null) {
            // 사용자 정보가 올바르게 가져와지지 않으면 함수 종료
            return
        }

        // 현재 로그인한 사용자의 문서 참조
        val tsDocFollowing = firestore.collection("users").document(currentUid)

        firestore.runTransaction { transition ->
            // 현재 로그인한 사용자의 FollowModel 가져오기
            var followingModel = transition.get(tsDocFollowing).toObject(FollowModel::class.java)

            if (followingModel == null) {
                // FollowModel이 없는 경우 새로 생성
                followingModel = FollowModel()
                followingModel.followingCount = 1
                followingModel.followings[targetUid] = targetEmail
            } else if (followingModel.followings.containsKey(targetUid)) {
                // 이미 팔로우한 경우, 언팔로우
                followingModel.followingCount = followingModel.followingCount - 1
                followingModel.followings.remove(targetUid)
            } else {
                // 아직 팔로우하지 않은 경우, 팔로우
                followingModel.followingCount = followingModel.followingCount + 1
                followingModel.followings[targetUid] = targetEmail
            }

            // 업데이트된 FollowModel을 Firestore에 저장
            transition.set(tsDocFollowing, followingModel)
        }

        // 팔로우 대상 사용자의 문서 참조
        val tsDocFollower = firestore.collection("users").document(targetUid)

        firestore.runTransaction { transition ->
            // 팔로우 대상 사용자의 FollowModel 가져오기
            var followModel = transition.get(tsDocFollower).toObject(FollowModel::class.java)

            if (followModel == null) {
                // FollowModel이 없는 경우 새로 생성
                followModel = FollowModel()
                followModel.followerCount = 1
                followModel.followers[currentUid] = currentEmail
            } else if (followModel.followers.containsKey(currentUid)) {
                // 이미 팔로우한 경우, 언팔로우
                followModel.followerCount = followModel.followerCount - 1
                followModel.followers.remove(currentUid)
            } else {
                // 아직 팔로우하지 않은 경우, 팔로우
                followModel.followerCount = followModel.followerCount + 1
                followModel.followers[currentUid] = currentEmail
            }

            // 업데이트된 FollowModel을 Firestore에 저장
            transition.set(tsDocFollower, followModel)
        }
    }
    private fun openImagePicker() {
        val picker = Intent(Intent.ACTION_PICK)
        picker.type = "image/*"
        myPhotoResultLauncher.launch(picker)
    }

    private fun handleImageUploadResult(imageUrl: android.net.Uri?) {
        if (imageUrl == null) return

        val storageRef = storage.reference.child("userProfileImages").child(currentUid!!)
        storageRef.putFile(imageUrl).continueWithTask { task ->
            return@continueWithTask storageRef.downloadUrl
        }.addOnCompleteListener { imageUri ->
            val map = HashMap<String, Any>()
            map["image"] = imageUri.result.toString()

            firestore.collection("profileImages").document(currentUid!!).set(map)
        }
    }

    private fun startActivityWithFollowMode(isFollowing: Boolean) {
        val intent = Intent(activity, PersonListActivty::class.java)
        intent.putExtra("FM", followModel)
        intent.putExtra("Mode", isFollowing)
        startActivity(intent)
    }

    private fun getProfileImage() {
        if (isAdded&& !isDetached) {
            firestore.collection("profileImages").document(dUid!!)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        // Handle error
                        return@addSnapshotListener
                    }

                    if (value != null && value.exists()) {
                        val imageUrl = value.getString("image")
                        if (!imageUrl.isNullOrBlank()) {
                            Glide.with(this).load(imageUrl)
                                .apply(RequestOptions().circleCrop())
                                .into(binding.accountIvProfile)
                        } else {
                            // Handle case when imageUrl is null or blank
                        }
                    } else {
                        // Handle case when DocumentSnapshot is empty
                    }
                }
        }
    }

    inner class CellImageViewHolder(val binding: ItemImageviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class UserFragmentRecyclerviewAdapter : RecyclerView.Adapter<CellImageViewHolder>() {

        var contentModels: ArrayList<ContentModel> = arrayListOf()

        init {
            firestore.collection("images").whereEqualTo("uid", dUid)
                .addSnapshotListener { value, error ->

                    for (item in value!!.documents) {
                        contentModels.add(item.toObject(ContentModel::class.java)!!)
                    }
                    binding.accountPostTextView.text = contentModels.size.toString()
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellImageViewHolder {
            val width = resources.displayMetrics.widthPixels / 3

            val view =
                ItemImageviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            view.cellImageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CellImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: CellImageViewHolder, position: Int) {
            val contentModel = contentModels[position]

            if (!contentModel.imageUrl.isNullOrBlank()) {
                Glide.with(holder.itemView.context)
                    .load(contentModel.imageUrl)
                    .into(holder.binding.cellImageview)
            }

            holder.binding.cellImageview.setOnClickListener {
                (activity as? MainActivity)?.moveToUserPage(contentModel.userId!!)
            }
        }

        override fun getItemCount(): Int {
            return contentModels.size
        }
    }
}