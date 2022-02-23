package com.example.messages;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;


public class NewContact extends AppCompatActivity {
Button b;
EditText et1;
TextView text;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch(Exception e){}
        setContentView(R.layout.activity_new_contact);
        b=findViewById(R.id.buttonb);
        et1=findViewById(R.id.editext1);
        text=findViewById(R.id.invaliderr);
        text.setVisibility(View.INVISIBLE);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String t1= et1.getText().toString();
                boolean ok=false;
                if(t1.contains("@") && (t1.contains(".com") || t1.contains(".org") || t1.contains(".edu"))){
                    ok=true;
                }
                else{
                    text.setVisibility(View.VISIBLE);
                    CountDownTimer countDownTimer= new CountDownTimer(5000,1000) {
                        @Override
                        public void onTick(long l) {

                        }

                        @Override
                        public void onFinish() {
                            text.setVisibility(View.INVISIBLE);
                        }
                    };
                    countDownTimer.start();
                }
                if(ok) {
                    ok=false;
                    Intent intent = new Intent();
                    intent.putExtra("return", t1);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

    }
}
