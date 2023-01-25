package com.pcs.apptoko

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pcs.apptoko.LoginActivity.Companion.sessionManager
import com.pcs.apptoko.adapter.ProdukAdapter
import com.pcs.apptoko.api.BaseRetrofit
import com.pcs.apptoko.response.produk.ProdukResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProdukFragment : Fragment() {

    private val api by lazy { BaseRetrofit().endPoint }
    private val rvAdapter = ProdukAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_produk, container, false)

        (view.findViewById(R.id.rv_produk) as RecyclerView).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = rvAdapter
        }

        getProduk(view)

        val btnTambah = view.findViewById<Button>(R.id.btnTambah)
        val btnSort = view.findViewById<TextView>(R.id.btn_sort)

        btnTambah.setOnClickListener {
            Toast.makeText(activity?.applicationContext, "Click", Toast.LENGTH_LONG).show()

            val bundle = Bundle()
            bundle.putString("status", "tambah")

            findNavController().navigate(R.id.produkFormFragment, bundle)
        }

        btnSort.setOnClickListener {
            showSortDialog(view)
        }

        val searchView = view.findViewById<SearchView>(R.id.filter)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    searchProduct(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    getProduk(view)
                }
                return true
            }
        })

        return view
    }

    fun searchProduct(query: String) {
        val token = sessionManager.getString("TOKEN")
        api.searchProduk(token.toString(), query).enqueue(object : Callback<ProdukResponse> {
            override fun onResponse(
                call: Call<ProdukResponse>,
                response: Response<ProdukResponse>
            ) {
                response.body()?.data?.let {
                    rvAdapter.setData(it.produk)
                }
            }

            override fun onFailure(call: Call<ProdukResponse>, t: Throwable) {

            }
        })
    }

    fun getProduk(view: View) {
        val token = sessionManager.getString("TOKEN")

        api.getProduk(token.toString()).enqueue(object : Callback<ProdukResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<ProdukResponse>,
                response: Response<ProdukResponse>,
            ) {
                if (response.body() == null) {
                    logout()
                    return
                }
                if (!response.body()!!.success) {
                    logout()
                    return
                }
                Log.d("ProdukData", response.body().toString())

                val txtTotalProduk = view.findViewById(R.id.txtTotalProduk) as TextView

                response.body()?.data?.produk?.let {
                    txtTotalProduk.text = "${it.size} Item"
                    rvAdapter.setData(it)
                }
            }

            override fun onFailure(call: Call<ProdukResponse>, t: Throwable) {
                Log.e("ProdukError", t.toString())
            }

        })
    }

    private fun showSortDialog(view: View) {
        val btnSort = view.findViewById<TextView>(R.id.btn_sort)
        val dialog = Dialog(this.requireContext())
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.view_sort)
        }
        val btnOK = dialog.findViewById<Button>(R.id.btn_ok)
        val radGroup = dialog.findViewById<RadioGroup>(R.id.rad_group)

        btnOK.setOnClickListener {
            when (radGroup.checkedRadioButtonId) {
                R.id.nama_asc -> {
                    rvAdapter.sortNama(true)
                    btnSort.text = "Urutkan : Nama ASC"
                }
                R.id.nama_desc -> {
                    rvAdapter.sortNama(false)
                    btnSort.text = "Urutkan : Nama DESC"
                }
                R.id.stok_asc -> {
                    rvAdapter.sortStok(true)
                    btnSort.text = "Urutkan : Stok ASC"
                }
                R.id.stok_desc -> {
                    rvAdapter.sortStok(false)
                    btnSort.text = "Urutkan : Stok DESC"
                }
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun logout() {
        sessionManager.clearSession()
        val moveIntent = Intent(activity, LoginActivity::class.java)
        startActivity(moveIntent)
        activity?.finish()
    }

}

















