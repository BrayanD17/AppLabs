package com.labs.applabs.administrator.operator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.administrator.operator.Adapter.AdapterHistorySemesterOperator
import com.labs.applabs.firebase.OperatorItem
import com.labs.applabs.firebase.Provider
import com.labs.applabs.firebase.historySemesterOperator
import kotlinx.coroutines.launch

class HistoryOperatorSemesters : AppCompatActivity() {
    private val provider : Provider = Provider()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterHistorySemesterOperator
    private var listCompletOperator: List<historySemesterOperator> = emptyList()
    private var listFilterOperator: List<historySemesterOperator> = emptyList()

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
            listCompletOperator = historyData
        }

        val etSearch = findViewById<EditText>(R.id.searchEditText)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filterSearch(etSearch.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun filterSearch(text: String){
        listFilterOperator = if (text.isEmpty()){
            listCompletOperator
        }else{
            listCompletOperator.filter { data ->
                data.semester.contains(text, ignoreCase = true) || data.year.contains(text, ignoreCase = true) || data.date.contains(text, ignoreCase = true)
            }
        }
        adapter.updateList(listFilterOperator)
    }

    fun backViewHistoryOperator(view: android.view.View) {finish()}
}
