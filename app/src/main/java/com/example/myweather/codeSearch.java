package com.example.myweather;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class codeSearch extends AppCompatActivity {

    private Button btn_ok;
    private EditText et_in;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_search);

        et_in = (EditText)findViewById(R.id.et_in);

        btn_ok = (Button) findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent =new Intent(codeSearch.this,MainActivity3.class);

                //用Bundle携带数据
                Bundle bundle=new Bundle();
                //传递name参数为tinyphp
                bundle.putString("cityCode", et_in.getText().toString());
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });
    }
}