package com.example.todayslook

import GridFragment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.todayslook.databinding.ActivityRankBinding
import com.example.todayslook.fragment.AlarmFragment
import com.example.todayslook.fragment.DetailViewFragment

import com.example.todayslook.fragment.RankFragment
import com.example.todayslook.fragment.UserFragment

import com.google.firebase.auth.FirebaseAuth

class RankActivity : AppCompatActivity() {
    lateinit var binding: ActivityRankBinding
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRankBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        binding.bottomNavigation.selectedItemId = R.id.action_home
        setContentView(binding.root)
        initNavigationBar()
        //  ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),0) 사진권한 안된다,.



    }


    private fun initNavigationBar() {
        binding.bottomNavigation.run {
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.action_home -> {
                        binding?.buttonLayout?.visibility = View.VISIBLE
                        binding?.toolbarLogo?.visibility = View.VISIBLE
                        binding?.toolbarUsername?.visibility = View.INVISIBLE
                        binding?.toolbarBtnBack?.visibility = View.INVISIBLE

                        changeFragment(RankFragment())
                    }


                    R.id.action_search -> {
                        binding?.buttonLayout?.visibility = View.INVISIBLE
                        changeFragment(GridFragment())
                    }

                    R.id.action_photo -> {
                        binding?.buttonLayout?.visibility = View.INVISIBLE
                        startActivity(Intent(context, AddPhotosActivity::class.java))
//                        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STOARAGE)==PackageManager.PERMISSION_GRANTED){
//                            //사진 읽기 (권한 있을때)
                        //                        }else{
//                            //사진 읽기 (권한 없을때)
//                            Toast.makeText(this,, "사진 읽기 권한이 없습니다.", Toast.LENGTH_SHORT).show()
//                        }
                    }

                    R.id.action_favorite_alarm -> {
                        binding?.buttonLayout?.visibility = View.INVISIBLE
                        changeFragment(AlarmFragment())
                    }

                    R.id.action_account -> {
                        binding?.buttonLayout?.visibility = View.INVISIBLE
                        var f = UserFragment()
                        var b = Bundle()
                        b.putString("dUid", auth.uid)
                        f.arguments = b
                        supportFragmentManager.beginTransaction().replace(R.id.main_content, f)
                            .commit()
                        //  changeFragment(UserFragment())
                    }
                }
                true
            }
            selectedItemId = R.id.action_home
        }

    }

    fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.mainContent.id, fragment).commit()
    }
}