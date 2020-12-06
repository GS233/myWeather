package com.example.myweather;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class favorCityList extends AppCompatActivity {
    private String cityCode; //返回到城市编码
    private myFavorDatabaseHelper dbHelper;
    private List<cityMini> mCityList;
    private ListView mCityListView;//城市管理界面的ListView
    private ArrayAdapter<String> cityAdapter;//所有城市的适配器
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        dbHelper = new myFavorDatabaseHelper(this,"favorCity.db",null,1);
        initData();//初始化数据
        //更新ListView
        updateListView();

        Log.d("SelectCity", "SelectCity->oncreate");
    }

    private List<cityMini> getCityList() {

        List<cityMini> cityList = new ArrayList<cityMini>();
        cityList.clear();
        //装入list
        int i = 0;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("favorCity", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex("cityName"));
                String code = cursor.getString(cursor.getColumnIndex("cityCode"));
                Log.d("m", name + "");
                Log.d("m", code + "");
                cityMini c = new cityMini(name,code);
                cityList.add(c);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return cityList;
    }

    //初始化数据
    private void initData() {
        mCityList = getCityList();//获取城市列表
    }


    //用适配器更新ListView
    void updateListView() {
        /*ListView三种适配器的使用*/
        //--level1
        final String[] viewList = new String[mCityList.size()];//显示在ListView中的数据
        int i = 0;
        for (cityMini city : mCityList) {
            viewList[i] = city.getName();
            i++;
        }
        mCityListView = (ListView) findViewById(R.id.list_view);
        cityAdapter = new ArrayAdapter<String>(//适配器
                favorCityList.this,
                android.R.layout.simple_list_item_1,
                viewList);//适配器
        mCityListView.setAdapter(cityAdapter);
        //ListView适配器的单击响应事件
        mCityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//单击ListView的响应事件
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cityMini city;

                city = mCityList.get(position);
                cityCode = city.getCode();
                Toast.makeText(favorCityList.this, "你单击了" + position + "：" + city.getName() + "，编码为" + cityCode, Toast.LENGTH_SHORT).show();
                Intent intent =new Intent(favorCityList.this,MainActivity3.class);

                //用Bundle携带数据
                Bundle bundle=new Bundle();
                //传递name参数为tinyphp
                bundle.putString("cityCode", cityCode);
                intent.putExtras(bundle);

                startActivity(intent);

                finish();//结束当前的activity的生命周期
            }
        });

    }




}



class cityMini{
    String name,code;

    public cityMini(String name, String code) {
        this.code = code;
        this.name = name;
    }

    public cityMini(){

    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }
}