package com.labs.applabs.administrator.operator

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.administrator.operator.Adapter.AdapterShowAllOperator
import com.labs.applabs.firebase.OperatorItem
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch

class ShowAllOperatorSemester : AppCompatActivity() {

    private val provider = Provider()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterShowAllOperator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_all_operator_semester)

        recyclerView = findViewById(R.id.recycleViewShowAllOperator)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AdapterShowAllOperator(emptyList())

        //Event click
        adapter.setOnItemClickListener { userId ->
            val intent = Intent(this, viewInformationOperator::class.java)
            //Send type activity and userId
            intent.putExtra("type","show")
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
        val title = findViewById<TextView>(R.id.titleTextShowAllOperator)

        //Get data from activity HistoryOperatorSemesters
        val userIdList = intent.getStringArrayListExtra("listUsers") ?: emptyList<String>()
        val semester = intent.getStringExtra("semester") ?: ""
        title.text = semester

        //Load data for each user
        lifecycleScope.launch {
            val users = mutableListOf<OperatorItem>()
            for (userId in userIdList) {
                try {
                    val userInfo = provider.getUserInfo(userId)
                    if (userInfo != null) {
                        users.add(OperatorItem(userId, userInfo))  //Load userId for each one
                    }
                } catch (e: Exception) {
                    Log.e("ShowAllOperatorSemester", "Error con $userId: ${e.message}")
                }
            }
            adapter.update(users)
        }
    }
}