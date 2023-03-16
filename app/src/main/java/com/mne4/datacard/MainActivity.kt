package com.mne4.datacard

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.mne4.datacard.databinding.ActivityMainBinding
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fillDropDownList(null)

        binding.autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var text = ""
                binding.autoCompleteTextView.text.toString().split(' ').forEach { text += it }
                val digits = text.length
                if (s?.length!! < 10) {
                    if (binding.autoCompleteTextView.text.length == 5 && digits == 5) {
                        val numbers = binding.autoCompleteTextView.text.toString()
                        val result = "${numbers[0]}${numbers[1]}${numbers[2]}${numbers[3]} ${numbers[4]}"

                        binding.autoCompleteTextView.setText(result)
                        binding.autoCompleteTextView.setSelection(binding.autoCompleteTextView.text.length)
                    }
                    else if (digits == 4 && binding.autoCompleteTextView.text.length > 4) {
                        var txt = binding.autoCompleteTextView.text.toString()
                        txt = txt.replace(" ", "")
                        binding.autoCompleteTextView.setText(txt)
                        binding.autoCompleteTextView.setSelection(binding.autoCompleteTextView.text.length)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_rule -> {
                startActivity(Intent(this, RuleActivity::class.java))
            }
        }
        return true
    }


    fun onClickSearchButton(view: View) {
        var bin = binding.autoCompleteTextView.text.toString()
        fillDropDownList(bin)
        if (!bin.isNullOrEmpty()) {
            getData(bin)
        }
        else {
            zeroing()
        }


    }

    fun onPhoneClick(view: View) {
        // Открыть номер в "Звонки"
        val tel = binding.textViewBankPhone.text.split("OR")[0]
        val dial = "tel: $tel"
        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(dial)))
    }

    fun onSiteClick(view: View) {
        // Открыть ссылку
        val url = "http://${binding.textViewBankLink.text}"
        val openPage = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(openPage)
    }

    private fun getData(bin: String){
        val url = "https://lookup.binlist.net/${bin.split(' ').joinToString("")}"
        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->

                val obj = JSONObject(response)
                val card = CardInfo(obj)

                binding.textViewError.text = ""
                binding.prepaidTextView.text = card.prepaid
                binding.brandTextView.text = card.brand
                binding.typeTextView.text = card.type
                binding.schemeTextView.text = card.scheme
                binding.lengthTextView.text = card.number.length
                binding.luhinTextView.text = card.number.luhn
                binding.textViewBankName.text = "${card.bank.name}, ${card.bank.city}"
                binding.textViewBankLink.text = card.bank.url

                binding.buttonSiteBank.isEnabled = card.bank.url != "?"
                binding.buttonPhoneBank.isEnabled = card.bank.phone != "?"

                binding.textViewBankPhone.text = card.bank.phone
                binding.textViewCountryName.text = card.country.name
                binding.textViewLatLong.text = "(latitude: ${card.country.latitude}, longitude: ${card.country.longitude})"
                binding.textViewCountryText.text = "Country ${card.country.emoji}"

            },
            {
                binding.textViewError.text = getString(R.string.notFound)

                zeroing()
            })
        queue.add(stringRequest)
    }

    private fun zeroing(){
        binding.prepaidTextView.text = "?"
        binding.brandTextView.text = "?"
        binding.typeTextView.text = "?"
        binding.schemeTextView.text = "?"
        binding.lengthTextView.text = "?"
        binding.luhinTextView.text = "?"
        binding.textViewBankName.text = "?"
        binding.textViewBankLink.text = "?"
        binding.textViewBankPhone.text = "?"
        binding.textViewCountryName.text = "?"
        binding.textViewLatLong.text = "(latitude: ?, longitude: ?)"
        binding.textViewCountryText.text = "Country"

        binding.buttonSiteBank.isEnabled = false
        binding.buttonPhoneBank.isEnabled = false
    }

    private fun fillDropDownList (bin: String?) {
        val db = baseContext.openOrCreateDatabase("app_data_card.db", MODE_PRIVATE, null)
        db.execSQL("CREATE TABLE IF NOT EXISTS numbers (num TEXT, UNIQUE(num))")
        if (bin != null) {
            db.execSQL("INSERT OR IGNORE INTO numbers VALUES (\"$bin\");")
        }
        val query: Cursor = db.rawQuery("SELECT * FROM numbers;", null)
        var bins = mutableListOf<String>()
        while (query.moveToNext()) {
            val number: String = query.getString(0)
            bins.add(number)
        }
        query.close()
        db.close()
        val adapter = ArrayAdapter<String>(
            this, android.R.layout.simple_dropdown_item_1line, bins
        )
        binding.autoCompleteTextView.setAdapter(adapter)
    }
}