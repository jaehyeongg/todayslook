package com.example.todayslook

import GridFragment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import androidx.fragment.app.Fragment
import com.example.todayslook.databinding.ActivityMainBinding
import com.example.todayslook.fragment.AlarmFragment
import com.example.todayslook.fragment.DetailViewFragment
import com.example.todayslook.fragment.UserFragment

import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        auth= FirebaseAuth.getInstance()
        binding.bottomNavigation.selectedItemId = R.id.action_home
        setContentView(binding.root)
        initNavigationBar()




    }




    private fun initNavigationBar() {
        binding.bottomNavigation.run {
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.action_home -> {
                        binding?.buttonLayout?.visibility=View.VISIBLE
                        binding?.toolbarLogo?.visibility = View.VISIBLE
                        binding?.toolbarUsername?.visibility = View.INVISIBLE
                        binding?.toolbarBtnBack?.visibility = View.INVISIBLE

                        changeFragment(DetailViewFragment())
                    }


                    R.id.action_search -> {
                        binding?.buttonLayout?.visibility=View.INVISIBLE
                        changeFragment(GridFragment())
                    }

                    R.id.action_photo -> {
                        binding?.buttonLayout?.visibility=View.INVISIBLE
                        startActivity(Intent(context, AddPhotosActivity::class.java))
                    }

                    R.id.action_favorite_alarm -> {
                        binding?.buttonLayout?.visibility=View.INVISIBLE
                        changeFragment(AlarmFragment())
                    }

                    R.id.action_account -> {

                        binding?.buttonLayout?.visibility=View.INVISIBLE
                        var f =UserFragment()
                        var b = Bundle()
                        b.putString("dUid",auth.uid)
                        f.arguments =b
                        supportFragmentManager.beginTransaction().replace(R.id.main_content,f).commit()
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

    fun moveToUserPage(userUid: String) {
        val userFragment = UserFragment()
        val bundle = Bundle()
        bundle.putString("dUid", userUid)
        userFragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.main_content, userFragment).commit()
    }
}