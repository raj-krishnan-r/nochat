package com.example.reshma.sockettest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    Context cs = null;
    EditText ed,msgspace;
    Button bt;
    ListView lv;
    boolean side = false;
    public ChatArrayAdapter adap;


    private Socket mSocket;
    {
        try
        {
            mSocket = IO.socket("http://rise-onebeat.rhcloud.com/");

        }catch (URISyntaxException w){
            Toast.makeText(getApplicationContext(),w.toString(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ed = (EditText) findViewById(R.id.msg);
        //msgspace = (EditText) findViewById(R.id.ed2);
        bt = (Button) findViewById(R.id.b1);
        lv = (ListView) findViewById(R.id.msglist);
        adap = new ChatArrayAdapter(getApplicationContext(),R.layout.right);
        lv.setAdapter(adap);



        cs=getApplicationContext();
        mSocket.connect();
         Emitter.Listener kettu = new Emitter.Listener() {

            @Override
            public void call(final Object... args) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       // msgspace.setText(msgspace.getText()+ "\n" +args[0].toString());
                        adap.add(new ChatMessage(true, args[0].toString()));
                        lv.setSelection(adap.getCount()-1);


                    }
                });
            }
        };
        mSocket.on("recmsg", kettu);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!ed.equals(""))
                {
                mSocket.emit("chat message", ed.getText());
                adap.add(new ChatMessage(false,ed.getText().toString()));
                    lv.setSelection(adap.getCount()-1);

                ed.setText("");
            }}
        });





    }



}
class ChatMessage {
    public boolean left;
    public String message;

    public ChatMessage(boolean left, String message) {
        super();
        this.left = left;
        this.message = message;
    }
}