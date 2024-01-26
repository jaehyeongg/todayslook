import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.todayslook.MainActivity
import com.example.todayslook.R
import com.example.todayslook.databinding.FragmentGridBinding
import com.example.todayslook.databinding.ItemImageviewBinding
import com.example.todayslook.fragment.UserFragment
import com.example.todayslook.model.ContentModel
import com.example.todayslook.model.FindidModel
import com.google.firebase.firestore.FirebaseFirestore

class GridFragment : Fragment() {
    private lateinit var binding: FragmentGridBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGridBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        binding.gridRecyclerview.adapter = GridFragmentRecyclerviewAdapter()
        binding.gridRecyclerview.layoutManager = GridLayoutManager(activity, 3)
        return binding.root
    }







    inner class CellImageViewHolder(val binding: ItemImageviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class GridFragmentRecyclerviewAdapter : RecyclerView.Adapter<CellImageViewHolder>() {

        private var contentModels: ArrayList<ContentModel> = arrayListOf()

        init {
            firestore.collection("images").addSnapshotListener { value, error ->
                contentModels.clear()
                for (item in value!!.documents) {
                    contentModels.add(item.toObject(ContentModel::class.java)!!)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellImageViewHolder {
            val width = resources.displayMetrics.widthPixels / 3

            val view =
                ItemImageviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            view.cellImageview.layoutParams = ViewGroup.LayoutParams(width, width)
            return CellImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: CellImageViewHolder, position: Int) {
            val contentModel = contentModels[position]
            Glide.with(holder.itemView.context).load(contentModel.imageUrl)
                .into(holder.binding.cellImageview)

            holder.binding.cellImageview.setOnClickListener {
                // 사용자 페이지로 이동
                val userUid = contentModel.uId
                if (userUid != null) {
                    (activity as? MainActivity)?.moveToUserPage(userUid)
                } else {
                    // userUid가 null인 경우에 대한 처리
                    // 예: 로그 또는 사용자에게 알림
                    Log.e("GridFragment", "User UID is null.")
                }
            }
        }

        override fun getItemCount(): Int {
            return contentModels.size
        }

    }
}