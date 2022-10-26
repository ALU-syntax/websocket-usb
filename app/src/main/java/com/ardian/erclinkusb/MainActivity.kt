package com.ardian.erclinkusb

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.facebook.stetho.websocket.SimpleEndpoint
import com.facebook.stetho.websocket.SimpleSession

class MainActivity : AppCompatActivity() {

    private val ADDRESS = "msg-test"
    private lateinit var mMessageView : MessageView
    private lateinit var mMessageEditText : EditText
    private lateinit var mServer: LocalWebSocketServer
    private lateinit var mEndpoint : MessageEndpoint
    private lateinit var mButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mMessageView = findViewById(R.id.messageTv)
        mMessageEditText = findViewById(R.id.messageEt)
        mButton = findViewById(R.id.sendBtn)

        mButton.setOnClickListener {
            if (mEndpoint != null){
                mEndpoint.broadcast(mMessageEditText.text.toString())
                mMessageEditText.setText("")
            }
        }
        mEndpoint = MessageEndpoint()
        startServer()
    }

    private fun startServer() {
        mServer = LocalWebSocketServer.createAndStart(this,ADDRESS, mEndpoint)
        mMessageView.appendSystemMessage(getString(R.string.msg_server_started, ADDRESS))
    }

    override fun onDestroy() {
        mServer.stop()
        super.onDestroy()
    }

    inner class MessageEndpoint : SimpleEndpoint {

        private var sessions : ArrayList<SimpleSession> = ArrayList()

        fun broadcast(message : String){
            for (session: SimpleSession in sessions){
                session.sendText(message)
            }
            runOnUiThread {
                mMessageView.appendServerMessage(message)
            }
        }
        override fun onOpen(session: SimpleSession?) {
            sessions.add(session!!)
            runOnUiThread {
                mMessageView.appendSystemMessage(getString(R.string.msg_client_connected))
            }

        }

        override fun onMessage(session: SimpleSession?, message: String?) {
            runOnUiThread {
                mMessageView.appendClientMessage(message)
            }
        }


        override fun onMessage(session: SimpleSession?, message: ByteArray?, messageLen: Int) {
            runOnUiThread {
                mMessageView.appendSystemMessage(getString(R.string.msg_client_ignored))
            }
        }

        override fun onClose(
            session: SimpleSession?,
            closeReasonCode: Int,
            closeReasonPhrase: String?
        ) {
            sessions.remove(session)
            runOnUiThread {
                mMessageView.appendSystemMessage(getString(R.string.msg_client_disconnected))
            }
        }

        override fun onError(session: SimpleSession?, t: Throwable?) {
            runOnUiThread {
                mMessageView.appendSystemMessage(getString(R.string.msg_client_error))
            }
        }
    }
}