package com.labs.applabs.administrator.operator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labs.applabs.R
import com.labs.applabs.administrator.operator.Adapter.AdapterShowAllOperator
import com.labs.applabs.elements.FiltroDialogFragment
import com.labs.applabs.elements.FiltroDialogMisconduct
import com.labs.applabs.elements.FiltroDialogShowAllOperator
import com.labs.applabs.firebase.FilterDataMisconduct
import com.labs.applabs.firebase.FilterShowAllOperator
import com.labs.applabs.firebase.OperatorItem
import com.labs.applabs.firebase.Provider
import com.labs.applabs.firebase.Solicitud
import kotlinx.coroutines.launch

class ShowAllOperatorSemester : AppCompatActivity(), FiltroDialogShowAllOperator.FilterListener{

    private val provider = Provider()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdapterShowAllOperator
    private lateinit var filters : ImageView
    private var listCompletOperator: List<OperatorItem> = emptyList()
    private var listFilterOperator: List<OperatorItem> = emptyList()

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
            listCompletOperator = users
            adapter.update(users)
        }

        filters = findViewById(R.id.filterIcon)
        filters.setOnClickListener {
            val filtroDialog = FiltroDialogShowAllOperator()
            filtroDialog.show(
                (this as FragmentActivity).supportFragmentManager,
                "filtroDialog"
            )
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

     override fun onFilterApply(filterData: FilterShowAllOperator) {
        Log.d("FiltroShowAllOperator", "Datos del filtro: $filterData")

        val filter = listCompletOperator.filter { dataOperator ->
            val carnet = filterData.carnetOperator.isNullOrBlank() || dataOperator.data.studentInfo.studentCard.trim() == filterData.carnetOperator.trim()
            val email = filterData.emailOperator.isNullOrBlank() || dataOperator.data.studentInfo.studentEmail.trim() == filterData.emailOperator.trim()
            val phone = filterData.phoneOperator.isNullOrBlank() || dataOperator.data.studentInfo.studentPhone.trim() == filterData.phoneOperator.trim()


            Log.d("Filtro Realizado", """
            ---
            Falta: ${dataOperator.data.studentInfo}
            Comparando:
            carnet: '${dataOperator.data.studentInfo.studentCard}' vs '${filterData.carnetOperator}' = $carnet
            email: '${dataOperator.data.studentInfo.studentEmail}' vs '${filterData.emailOperator}' = $email
            phone: '${dataOperator.data.studentInfo.studentPhone}' vs '${filterData.phoneOperator}' = $phone
        """.trimIndent())

            carnet && email && phone
        }

        Log.d("FiltroShowAllOperator", "Total filtradas: ${filter.size}")
        listFilterOperator = filter
        adapter.update(listFilterOperator)
    }

    override fun onFilterCancel() {
        adapter.update(listCompletOperator)
    }

    private fun filterSearch(text: String){
        listFilterOperator = if (text.isEmpty()){
            listCompletOperator
        }else{
            listCompletOperator.filter { solicitud ->
                solicitud.data.studentInfo.studentName.contains(text, ignoreCase = true) || solicitud.data.studentInfo.surNames.contains(text, ignoreCase = true)
            }
        }
        adapter.update(listFilterOperator)
    }

    fun backViewShowAllOperator(view: android.view.View) {finish()}
}