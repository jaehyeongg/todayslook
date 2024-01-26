package com.example.todayslook


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.todayslook.fragment.AccFragment

import com.example.todayslook.fragment.BottomFragment
import com.example.todayslook.fragment.OutwearFragment
import com.example.todayslook.fragment.ShoesFragment
import com.example.todayslook.fragment.TopFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class ClosestActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_closest)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_top -> replaceFragment(TopFragment())
                R.id.nav_bottom -> replaceFragment(BottomFragment())
                R.id.nav_acc -> replaceFragment(AccFragment())
                R.id.nav_outwear -> replaceFragment(OutwearFragment())
                R.id.nav_shoes -> replaceFragment(ShoesFragment())
            }
            true
        }

        // 초기 화면 설정 (예: TopFragment를 초기로 보여줌)
        if (savedInstanceState == null) {
            replaceFragment(TopFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
