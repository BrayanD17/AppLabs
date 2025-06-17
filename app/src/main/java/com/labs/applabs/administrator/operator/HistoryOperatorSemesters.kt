package com.labs.applabs.administrator.operator

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.administrator.operator.Adapter.AdapterHistorySemesterOperator
import com.labs.applabs.firebase.Provider
import kotlinx.coroutines.launch

class HistoryOperatorSemesters : AppCompatActivity() {
    private val provider : Provider = Provider()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterHistorySemesterOperator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_operator_semesters)

        //Initialize adapter
        recyclerView = findViewById(R.id.recycleViewHistoryOperator)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = AdapterHistorySemesterOperator(emptyList())
        recyclerView.adapter = adapter

        //Events
        adapter.setOnItemClickListener { item ->
            val intent = Intent(this, ShowAllOperatorSemester::class.java).apply {
                putStringArrayListExtra("listUsers", ArrayList(item.userId))
                putExtra("semester", "${item.semester} ${item.year}")
            }
            startActivity(intent)
        }

        lifecycleScope.launch {
            val historyData = provider.getHistoryOperatorSemesters()
            adapter.updateList(historyData)
        }
    }

    fun backViewHistoryOperator(view: android.view.View) {finish()}
}
