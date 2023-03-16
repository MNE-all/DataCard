package com.mne4.datacard

import org.json.JSONObject
import java.util.*

class CardInfo (obj: JSONObject) {
    val number = CardNumber(obj)
    val country = CardCountry(obj)
    val bank = CardBank(obj)
    var scheme: String = "?"
    var type: String = "?"
    var brand: String = "?"
    var prepaid: String = "?"

    init {
        try {
            scheme = obj.getString("scheme")
            scheme = scheme.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

            type = obj.getString("type")
            if (type == "null") type = "?"
            type = type.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            brand = obj.getString("brand")
            prepaid = obj.getString("prepaid")

            if (brand == "null") brand = "?"
            when (prepaid) {
                "null" -> prepaid = "?"
                "true" -> prepaid = "Yes"
                "false" -> prepaid = "No"
            }
        }
        catch (_: Exception) {

        }
    }
}

class CardNumber(obj: JSONObject) {
    var length: String = "?"
    var luhn: String = "?"

    init {
        try {
            val cardNumber = obj.getJSONObject("number")
            length = cardNumber.getString("length")
            luhn = cardNumber.getString("luhn")
            luhn = if (luhn == "true") "Yes"
            else "No"
        }
        catch (_: Exception) {

        }
    }
}

class CardCountry(obj: JSONObject) {
    var name: String = "?"
    var latitude: String = "?"
    var longitude: String = "?"
    var emoji: String = ""

    init {
        try {
            val country = obj.getJSONObject("country")
            name = country.getString("name")
            latitude = country.getString("latitude")
            longitude = country.getString("longitude")
            emoji = country.getString("emoji")
        }
        catch (_: Exception) {

        }
    }
}

class CardBank(obj: JSONObject) {
    var name :String = "?"
    var url: String = "?"
    var phone: String = "?"
    var city: String = "?"

    init {
        try {
            val bank = obj.getJSONObject("bank")
            name = bank.getString("name")
            url = bank.getString("url")
            phone = bank.getString("phone")
            city = bank.getString("city")
        }
        catch (_: Exception) {

        }
    }
}