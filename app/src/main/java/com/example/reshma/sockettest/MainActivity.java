package com.example.reshma.sockettest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;
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

    String socketid;

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
            //mSocket = IO.socket("http://192.168.43.203:3000");
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
        Emitter.Listener idregisteration = new Emitter.Listener() {

            @Override
            public void call(final Object... args) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        socketid = args[0].toString();
                    }
                });
            }
        };
        mSocket.on("clientsocketid",idregisteration);


        Emitter.Listener kettu = new Emitter.Listener() {

            @Override
            public void call(final Object... args) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       // msgspace.setText(msgspace.getText()+ "\n" +args[0].toString());
                        adap.add(new ChatMessage(true, args[0].toString(),socketid));

                        lv.setSelection(adap.getCount()-1);


                    }
                });
            }
        };
        Emitter.Listener ackserver = new Emitter.Listener() {

            @Override
            public void call(final Object... args) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //Toast.makeText(getApplicationContext(),args[0].toString(),Toast.LENGTH_SHORT).show();
                        for(int i = adap.getCount()-1;i>=0;i--)
                        {
                            //Toast.makeText(getApplicationContext(),adap.getItem(i).deliveryStatus,Toast.LENGTH_SHORT).show();

                            if(adap.getItem(i).timestmp==Long.parseLong(args[0].toString()))
                            {
                                String checkedMark = "\u2713";

                              // Toast.makeText(getApplicationContext(),"Matched at "+i,Toast.LENGTH_SHORT).show();
                                View alterView = lv.getChildAt(i);
                                TextView tv = (TextView) alterView.findViewById(R.id.deliverystatus);
                                tv.setText(checkedMark);
                                adap.getItem(i).setDeliveryStatus(checkedMark);
                                //adap.notifyDataSetChanged();
                               // adap.refresh(i);

                            }
                        }


                    }
                });
            }
        };

        mSocket.on("recmsg", kettu);
        mSocket.on("ackserver", ackserver);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(ed.getText()))
                {

                    ChatMessage msg = new ChatMessage(false,ed.getText().toString(),socketid);
                    JSONObject jmsg = new JSONObject();
                    try {
                        jmsg.put("id",msg.timestmp);
                        jmsg.put("msg",msg.message);
                        jmsg.put("clientsocketid",msg.clientsocketid);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mSocket.emit("intakemessage", jmsg.toString());


                adap.add(msg);lv.setSelection(adap.getCount()-1);
                ed.setText("");
            }}
        });

    }
}
class ChatMessage {
    public boolean left;
    public String message;
    long timestmp;
    public String clientsocketid;
    public String deliveryStatus;
    public ChatMessage(boolean left, String message, String clientsocketid) {
        super();
        this.left = left;
        this.message = message;
        this.timestmp=System.currentTimeMillis();
        this.clientsocketid = clientsocketid;
        this.deliveryStatus = "";
    }
    public void setDeliveryStatus(String st)
    {
        this.deliveryStatus =st;
    }
}