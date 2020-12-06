package com.example.myweather;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity2 extends Activity {

    private ImageView mBackBtn;//返回按钮
    private String cityCode; //返回到城市编码
    private TextView titleName;//顶部标题的文字控件
    private EditText mSearchEditText;//搜索框
    private CityDB myCityDB;
    private ArrayAdapter<String> cityAdapter;//所有城市的适配器
    private List<City> mCityList;//城市列表
    private ListView mCityListView;//城市管理界面的ListView
    private SharedPreferenceUtil mSpUtil;
    private MyApplication mApplication;
    private HashMap<String, City> cityCode_cityHashMap;

    private static final String CITY_TABLE_NAME = "city";
    // 首字母对应的位置
    private Map<String, Integer> mIndexer;
    // 根据首字母存放数据
    private Map<String, List<City>> mMap;


    /*ListView三种适配器的使用例子*/
    //level1
   /* private String[] data={"第1组","第2组","第3组","第4组","第5组","第6组","第7组","第8组",
            "第9组","第10组","第11组","第12组","第13组","第14组","第15组","第16组","第17组","第18组","第19组","第20组"};*/

    //level2
/*    private String[] name={"第1组","第2组","第3组","第4组"};
    private String[] desc={"田野、樊茂华、陈伟强、郭⼀娇、赵亚洪",
            "曹露阳、胡先军、彭俊伟、李粉英、段伟帝",
            "刘动、龚帅、王思可、赵林欣、陈佳佩",
            "魏红枪、张威、赵海洋、潘辉、袁金瑶"};
    private int[] imageids={R.drawable.base_action_bar_action_city, R.drawable.title_city,
            R.drawable.title_share,R.drawable.title_update};*/
    //level3
/*    String[] projection = null;
    String selection = null;
    String[] selectionArgs = null;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;//权限*/

    /*@RequiresApi(api = Build.VERSION_CODES.M)*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initData();//初始化数据
        //更新ListView
        updateListView();

        Log.d("SelectCity", "SelectCity->oncreate");
    }

    private List<City> getCityList() {
        String path="/data" //数据库city.db的路径 /data/data/com.example.annora.weather/databases1/city.db
                + Environment.getDataDirectory().getAbsolutePath()//Environment.getDataDirectory()手机内部存储，getAbsolutePath绝对路径
                + File.separator + getPackageName()
                + File.separator + "databases1"
                + File.separator
                + CityDB.CITY_DB_NAME;
        //代开数据库
        SQLiteDatabase database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

        List<City> list = new ArrayList<City>();
        Cursor c = database.rawQuery("SELECT * from " + CITY_TABLE_NAME, null);//from后面必须有个空格
        while (c.moveToNext()) {//移动光标到下一行
            //读数据库中的一行数据
            String province = c.getString(c.getColumnIndex("province"));//getColumnIndex返回指定列的名称，如果不存在返回-1
            String city = c.getString(c.getColumnIndex("city"));
            String number = c.getString(c.getColumnIndex("number"));
            String allPY = c.getString(c.getColumnIndex("allpy"));
            String allFirstPY = c.getString(c.getColumnIndex("allfirstpy"));
            String firstPY = c.getString(c.getColumnIndex("firstpy"));
            City item = new City(province, city, number, firstPY, allPY, allFirstPY);//调用City的构造函数创建一个City对象
            list.add(item);

        }
        return list;
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
        for (City city : mCityList) {
            viewList[i] = city.getCity();
            i++;
        }
        mCityListView = (ListView) findViewById(R.id.list_view);
        cityAdapter = new ArrayAdapter<String>(//适配器
                MainActivity2.this,
                android.R.layout.simple_list_item_1,
                viewList);//适配器
        mCityListView.setAdapter(cityAdapter);
        //ListView适配器的单击响应事件
        mCityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//单击ListView的响应事件
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                City city;

                city = mCityList.get(position);
                cityCode = city.getNumber();
                Toast.makeText(MainActivity2.this, "你单击了" + position + "：" + city.getCity() + "，编码为" + cityCode, Toast.LENGTH_SHORT).show();
                Intent intent =new Intent(MainActivity2.this,MainActivity3.class);

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
