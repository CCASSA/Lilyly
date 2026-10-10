package com.lilyly.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class SpotifyCallbackActivity:Activity() {
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        val accepted=runCatching {SpotifySession(this).acceptCallback(intent.dataString.orEmpty())}.getOrDefault(false)
        if(accepted) startActivity(Intent(this,MainActivity::class.java).putExtra("spotify",true).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        finish()
    }
}
