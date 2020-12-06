package com.example.myweather;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private myFavorDatabaseHelper dbHelper;
    private MyApplication mApplication;
    private String mCurCityCode = 101010100 + "";//当前选择的城市编码
    private String cityNameForDB, cityCodeForDB;//为关注准备的
    //显示七天天气用到的声明
    private List<View> views;
    private ImageView dots[];
    private WeekWeather[] weekWeather;//一周天气
    //通过消息机制，将解析的天气对象发给主线程，主线程接收后调用updateTodayWeather来更新UI界面
    private static final int UPDATE_TODAY_WEATHER = 1;// ---weather07
    private SharedPreferenceUtil mSpUtil;
    private Button btn_refresh, btn_city, btn_favor, btn_cityCodeSearch, btn_favoring;
    private TextView tv_city, tv_tmp, tv_wind, tv_weather, tv_rain,
            week1_dayTv, week1_temTv, week1_cliTv, week1_windTv,
            week2_dayTv, week2_temTv, week2_cliTv, week2_windTv,
            week3_dayTv, week3_temTv, week3_cliTv, week3_windTv,
            week4_dayTv, week4_temTv, week4_cliTv, week4_windTv,
            week5_dayTv, week5_temTv, week5_cliTv, week5_windTv,
            week6_dayTv, week6_temTv, week6_cliTv, week6_windTv;
    private Handler mHandler = new Handler() { // ---weather07 //Handler主要有两个用途:首先是可以定时处理或者分发消息，其次是可以添加一个执行的行为在其它线程中执行
        /*消息android.os.Message：
        是定义一个Messge包含必要的描述和属性数据，并且此对象可以被发送给android.os.Handler处理。
        属性字段：arg1、arg2、what、obj、replyTo等；其中arg1和arg2是用来存放整型数据的；what是用来保存消息标示的；obj是Object类型的任意对象；replyTo是消息管理器，
        会关联到一个handler，handler就是处理其中的消息。通常对Message对象不是直接new出来的，只要调用handler中的obtainMessage方法来直接获得Message对象。
        https://www.cnblogs.com/to-creat/p/4964458.html*/
        public void handleMessage(android.os.Message msg) {//覆盖handleMessage方法
            switch (msg.what) {//根据收到的消息的what类型处理
                //更新今日天气
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new myFavorDatabaseHelper(this, "favorCity.db", null, 1);

        btn_favoring = (Button) findViewById(R.id.btn_favoring);
        btn_favoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("cityName", cityNameForDB);
                values.put("cityCode", cityCodeForDB);
                db.insert("favorCity", null, values);
            }
        });
        btn_favor = (Button) findViewById(R.id.btn_favor);
        btn_favor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, favorCityList.class);//Intent调用另一个Activity
                startActivity(i);
            }
        });
        btn_cityCodeSearch = (Button) findViewById(R.id.btn_cityCodeSearch);
        btn_cityCodeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, codeSearch.class);//Intent调用另一个Activity
                startActivity(i);
            }
        });
        btn_city = (Button) findViewById(R.id.btn_city);
        btn_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, MainActivity2.class);//Intent调用另一个Activity
                startActivity(i);
            }
        });
        btn_refresh = (Button) findViewById(R.id.btn_refresh);
        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryWeatherCode(mCurCityCode);

                Toast.makeText(MainActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
            }
        });
        tv_city = (TextView) findViewById(R.id.tv_city);
        tv_tmp = (TextView) findViewById(R.id.tmp);
        tv_wind = (TextView) findViewById(R.id.tv_wind);
        tv_weather = (TextView) findViewById(R.id.tv_weather);
        tv_rain = (TextView) findViewById(R.id.tv_rain);
        week1_dayTv = (TextView) findViewById(R.id.week1_day);
        week1_temTv = (TextView) findViewById(R.id.week1_tmp);
        week1_cliTv = (TextView) findViewById(R.id.week1_climate);
        week1_windTv = (TextView) findViewById(R.id.week1_wind);

        week2_dayTv = (TextView) findViewById(R.id.week2_day);
        week2_temTv = (TextView) findViewById(R.id.week2_tmp);
        week2_cliTv = (TextView) findViewById(R.id.week2_climate);
        week2_windTv = (TextView) findViewById(R.id.week2_wind);

        week3_dayTv = (TextView) findViewById(R.id.week3_day);
        week3_temTv = (TextView) findViewById(R.id.week3_tmp);
        week3_cliTv = (TextView) findViewById(R.id.week3_climate);
        week3_windTv = (TextView) findViewById(R.id.week3_wind);

        week4_dayTv = (TextView) findViewById(R.id.week4_day);
        week4_temTv = (TextView) findViewById(R.id.week4_tmp);
        week4_cliTv = (TextView) findViewById(R.id.week4_climate);
        week4_windTv = (TextView) findViewById(R.id.week4_wind);

        week5_dayTv = (TextView) findViewById(R.id.week5_day);
        week5_temTv = (TextView) findViewById(R.id.week5_tmp);
        week5_cliTv = (TextView) findViewById(R.id.week5_climate);
        week5_windTv = (TextView) findViewById(R.id.week5_wind);

        week6_dayTv = (TextView) findViewById(R.id.week6_day);
        week6_temTv = (TextView) findViewById(R.id.week6_tmp);
        week6_cliTv = (TextView) findViewById(R.id.week6_climate);
        week6_windTv = (TextView) findViewById(R.id.week6_wind);


        initData();
        //新页面接收数据
        Bundle bundle = this.getIntent().getExtras();

        cityCodeForDB = mCurCityCode;
        queryWeatherCode(mCurCityCode);

    }


    //初始化weekW
    private void initData() {
        //mApplication = MyApplication.getInstance();
        //mSpUtil = mApplication.getSharedPreferenceUtil();
        weekWeather = new WeekWeather[6];
        for (int i = 0; i < weekWeather.length; i++) {
            weekWeather[i] = new WeekWeather();
        }
        //weekWeather[0].setDate("今天");
        //Log.d("myWeather", weekWeather[0].getDate());
    }


    //获取天气信息
    private void queryWeatherCode(String citycode) {

        if (citycode == "-1")//
            return;
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + citycode;//why：URL，主页已经报废了，但还可以用
        Log.d("myWeather", address);

        //子线程：处理除UI之外较费时的操作，如从网上下载数据或者访问数据库
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;//HttpURLConnection是访问HTTP协议的基本功能的类，继承自URLConnection，可用于向指定网站发送GET请求、POST请求。
                TodayWeather todayWeather = null;// ---weather07
                try {
                    URL url = new URL(address);//定义URL
                    con = (HttpURLConnection) url.openConnection();//到URL所引用的远程对象的链接
                    con.setRequestMethod("GET");//GET是从服务器上获取数据，POST是向服务器传送数据
                    con.setConnectTimeout(8000);//设置连接超时：建立连接的时间。如果到了指定的时间，还没建立连接，则报异常
                    con.setReadTimeout(8000);//设置读取超时：已经建立连接，并开始读取服务端资源。如果到了指定的时间，没有可能的数据被客户端读取，则报异常。
                    InputStream in = con.getInputStream();//得到网络返回的输入流
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));//BufferedReader从字符输入流中读取文本，缓冲各个字符，从而实现字符、数组和行的高效读取。
                    StringBuilder response = new StringBuilder();//StringBuilder适用于单线程下在字符缓冲区进行大量操作的情况
                    String str;
                    while ((str = reader.readLine()) != null)//读取网络数据并连接成字符串
                    {
                        response.append(str);//字符串连接
                        Log.d("myWeather", str);//why：得到的是XML语句
                    }
                    String responseStr = response.toString();//返回一个与构建器或缓冲器内容相同的字符串，这个字符串就是读取网络数据得到的信息
                    Log.d("myWeather", responseStr);

                    //parseXML(responseStr);//获取网络数据后，调用解析函数 ---Weather06
                    todayWeather = parseXML(responseStr);//解析网络数据 ---Weather07
                    if (todayWeather != null)//这里更新今日天气信息 ---Weather07
                    {
                        Log.d("myWeather", todayWeather.toString());

                        //使用Message机制主要是为了保证线程之间操作安全，同时不需要关心具体的消息接收者，使消息本身和线程剥离开，这样就可以方便的实现定时、异步等操作。
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;//what是用来保存消息标示的
                        msg.obj = todayWeather;//obj是Object类型的任意对象
                        mHandler.sendMessage(msg);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }


    //官方的解析函数
    //数据存在week里面了
    private TodayWeather parseXML(String xmldata) {
        TodayWeather todayWeather = null;
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dataCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;

        try {
            //Android中解析XML的方式主要有三种:sax,dom和pull，这里使用pull方法
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();//创建生产XML的pull解析器的工厂
            XmlPullParser xmlPullParser = fac.newPullParser();//使用工厂获取pull解析器
            xmlPullParser.setInput(new StringReader(xmldata));//使用解析器读取当前的xml流，传入InputStream对象 并且设置解码规则需和XML文档中设置的一致
            int eventType = xmlPullParser.getEventType();//获取当前事件的状态
            Log.d("myWeather", "parseXML");
            /* pull解析是以事件为单位解析的，因此要获取一开始的解析标记type，之后通过type判断循环来读取文档
            注意：当解析器开始读取is的时候已经开始了，指针type在xml的第一行开始。
            pull解析是指针从第一行开始读取到最后一行以事件为单位读取的解析方式*/
            while (eventType != XmlPullParser.END_DOCUMENT) {//通过while循环判断是否读取到了文档结束
                switch (eventType) {
                    //判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                        //判断当前遇到的元素名称是否为resp（这个<resp>在xml文件里起始的地方）
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                        }
                        if (todayWeather != null)//已有初始化的TodayWeather对象，开始解析下面的数据
                        {
                            //判断当前遇到的元素名称是否为city
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();//获取下一个事件的状态
                                //Log.d("myWeather", "city: " + xmlPullParser.getText());
                                todayWeather.setCity(xmlPullParser.getText());//将数据封装到TodayWeather类中
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                //Log.d("myWeather", "updatetime: " + xmlPullParser.getText());
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang")) {
                                switch (fengxiangCount) {
                                    case 0:
                                        eventType = xmlPullParser.next();
                                        todayWeather.setFengxiang(xmlPullParser.getText());
                                        weekWeather[1].setFengxiang(xmlPullParser.getText());
                                        fengxiangCount++;
                                        break;
                                    case 1:
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setFengxiang(xmlPullParser.getText());
                                        fengxiangCount++;
                                        break;
                                    case 2:
                                        eventType = xmlPullParser.next();
                                        weekWeather[3].setFengxiang(xmlPullParser.getText());
                                        fengxiangCount++;
                                        break;
                                    case 3:
                                        eventType = xmlPullParser.next();
                                        weekWeather[4].setFengxiang(xmlPullParser.getText());
                                        fengxiangCount++;
                                        break;
                                    case 4:
                                        eventType = xmlPullParser.next();
                                        weekWeather[5].setFengxiang(xmlPullParser.getText());
                                        fengxiangCount++;
                                        break;
                                }
                            } else if (xmlPullParser.getName().equals("fengli")) {
                                switch (fengliCount) {
                                    case 0:
                                        eventType = xmlPullParser.next();
                                        todayWeather.setFengli(xmlPullParser.getText());
                                        weekWeather[1].setFengli(xmlPullParser.getText());
                                        fengliCount++;
                                        break;
                                    case 1:
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setFengli(xmlPullParser.getText());
                                        fengliCount++;
                                        break;
                                    case 2:
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setFengli(xmlPullParser.getText());
                                        fengliCount++;
                                        break;
                                    case 3:
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setFengli(xmlPullParser.getText());
                                        fengliCount++;
                                        break;
                                    case 4:
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setFengli(xmlPullParser.getText());
                                        fengliCount++;
                                        break;
                                }
                            } else if (xmlPullParser.getName().equals("date")) {
                                switch (dataCount) {
                                    case 0://今日日期
                                        eventType = xmlPullParser.next();
                                        todayWeather.setDate(xmlPullParser.getText());
                                        weekWeather[1].setDate(xmlPullParser.getText());
                                        dataCount++;//让dataCount不为零，也就是这些只处理一次
                                        break;
                                    case 1://第2天日期
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setDate(xmlPullParser.getText());
                                        dataCount++;
                                        break;
                                    case 2://第3天日期
                                        eventType = xmlPullParser.next();
                                        weekWeather[3].setDate(xmlPullParser.getText());
                                        dataCount++;
                                        break;
                                    case 3://第4天日期
                                        eventType = xmlPullParser.next();
                                        weekWeather[4].setDate(xmlPullParser.getText());
                                        dataCount++;
                                        break;
                                    case 4://第5天日期
                                        eventType = xmlPullParser.next();
                                        weekWeather[5].setDate(xmlPullParser.getText());
                                        dataCount++;
                                        break;

                                }
                            } else if (xmlPullParser.getName().equals("high")) {
                                switch (highCount) {
                                    case 0:
                                        eventType = xmlPullParser.next();
                                        todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());//
                                        weekWeather[1].setHigh(xmlPullParser.getText().substring(2).trim());
                                        highCount++;
                                        break;
                                    case 1:
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setHigh(xmlPullParser.getText().substring(2).trim());
                                        highCount++;
                                        break;
                                    case 2:
                                        eventType = xmlPullParser.next();
                                        weekWeather[3].setHigh(xmlPullParser.getText().substring(2).trim());
                                        highCount++;
                                        break;
                                    case 3:
                                        eventType = xmlPullParser.next();
                                        weekWeather[4].setHigh(xmlPullParser.getText().substring(2).trim());
                                        highCount++;
                                        break;
                                    case 4:
                                        eventType = xmlPullParser.next();
                                        weekWeather[5].setHigh(xmlPullParser.getText().substring(2).trim());
                                        highCount++;
                                        break;
                                }
                            } else if (xmlPullParser.getName().equals("low")) {
                                switch (lowCount) {
                                    case 0:
                                        eventType = xmlPullParser.next();
                                        todayWeather.setLow(xmlPullParser.getText().substring(2).trim());//
                                        weekWeather[1].setLow(xmlPullParser.getText().substring(2).trim());
                                        lowCount++;
                                        break;
                                    case 1:
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setLow(xmlPullParser.getText().substring(2).trim());
                                        lowCount++;
                                        break;
                                    case 2:
                                        eventType = xmlPullParser.next();
                                        weekWeather[3].setLow(xmlPullParser.getText().substring(2).trim());
                                        lowCount++;
                                        break;
                                    case 3:
                                        eventType = xmlPullParser.next();
                                        weekWeather[4].setLow(xmlPullParser.getText().substring(2).trim());
                                        lowCount++;
                                        break;
                                    case 4:
                                        eventType = xmlPullParser.next();
                                        weekWeather[5].setLow(xmlPullParser.getText().substring(2).trim());
                                        lowCount++;
                                        break;
                                }
                            } else if (xmlPullParser.getName().equals("type")) {
                                switch (typeCount) {
                                    case 0:
                                        eventType = xmlPullParser.next();
                                        todayWeather.setType(xmlPullParser.getText());
                                        weekWeather[1].setType(xmlPullParser.getText());
                                        typeCount++;
                                        break;
                                    case 1:
                                        eventType = xmlPullParser.next();
                                        weekWeather[2].setType(xmlPullParser.getText());
                                        typeCount++;
                                        break;
                                    case 2:
                                        eventType = xmlPullParser.next();
                                        weekWeather[3].setType(xmlPullParser.getText());
                                        typeCount++;
                                        break;
                                    case 3:
                                        eventType = xmlPullParser.next();
                                        weekWeather[4].setType(xmlPullParser.getText());
                                        typeCount++;
                                        break;
                                    case 4:
                                        eventType = xmlPullParser.next();
                                        weekWeather[5].setType(xmlPullParser.getText());
                                        typeCount++;
                                        break;
                                }
                            }//昨天日期
                            else if (xmlPullParser.getName().equals("date_1")) {
                                eventType = xmlPullParser.next();
                                Log.d("myWeather", xmlPullParser.getText());//
                                weekWeather[0].setDate(xmlPullParser.getText());
                            }
                            //昨天高温
                            else if (xmlPullParser.getName().equals("high_1")) {
                                eventType = xmlPullParser.next();
                                weekWeather[0].setHigh(xmlPullParser.getText().substring(2).trim());
                            }
                            //昨天低温
                            else if (xmlPullParser.getName().equals("low_1")) {
                                eventType = xmlPullParser.next();
                                weekWeather[0].setLow(xmlPullParser.getText().substring(2).trim());//
                            }
                            //昨天天气状况
                            else if (xmlPullParser.getName().equals("type_1")) {
                                eventType = xmlPullParser.next();
                                weekWeather[0].setType(xmlPullParser.getText());
                            }
                            //昨天风向
                            else if (xmlPullParser.getName().equals("fx_1")) {
                                eventType = xmlPullParser.next();
                                weekWeather[0].setFengxiang(xmlPullParser.getText());
                            }
                            //昨天风力
                            else if (xmlPullParser.getName().equals("fl_1")) {
                                eventType = xmlPullParser.next();
                                weekWeather[0].setFengli(xmlPullParser.getText());
                            }
                        }
                        break;
                    //判断当前事件是否为标签元素结束事件
                    case XmlPullParser.END_TAG:
                        break;
                }
                //进入下一个元素并触发相应事件
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.d("myWeather", weekWeather[0].getDate());
        for (WeekWeather w : weekWeather) {
            Log.d("myWeather", w.getDate() + ", " + w.getType() + ", " + w.getHigh() + ", " + w.getLow() + ", " + w.getFengli() + ", " + w.getFengxiang());
        }
        return todayWeather;//
    }

    //更新天气信息
    void updateTodayWeather(TodayWeather todayWeather) {

        cityNameForDB = todayWeather.getCity();
        //文字控件--根据网络数据刷新UI的文字
        tv_city.setText(todayWeather.getCity() + "天气");//红条上的，北京天气
        //timeTv.setText(todayWeather.getUpdatetime() + "发布");//布局上左侧的，时间
        tv_rain.setText("湿度：" + todayWeather.getShidu());//布局上左侧的，湿度
        tv_tmp.setText(todayWeather.getHigh() + "~" + todayWeather.getLow());//布局中间的，温度
        tv_weather.setText(todayWeather.getType());//布局中间的，天气情况
        tv_wind.setText("风力：" + todayWeather.getFengli());//布局中间的，风力

        week1_dayTv.setText(weekWeather[0].getDate());
        week1_cliTv.setText(weekWeather[0].getType());
        week1_temTv.setText(weekWeather[0].getHigh() + "~" + weekWeather[0].getLow());
        week1_windTv.setText(weekWeather[0].getFengxiang() + weekWeather[0].getFengli());
        week2_dayTv.setText(weekWeather[1].getDate());
        week2_cliTv.setText(weekWeather[1].getType());
        week2_temTv.setText(weekWeather[1].getHigh() + "~" + weekWeather[1].getLow());
        week2_windTv.setText(weekWeather[1].getFengxiang() + weekWeather[1].getFengli());
        week3_dayTv.setText(weekWeather[2].getDate());
        week3_cliTv.setText(weekWeather[2].getType());
        week3_temTv.setText(weekWeather[2].getHigh() + "~" + weekWeather[2].getLow());
        week3_windTv.setText(weekWeather[2].getFengxiang() + weekWeather[2].getFengli());
        week4_dayTv.setText(weekWeather[3].getDate());
        week4_cliTv.setText(weekWeather[3].getType());
        week4_temTv.setText(weekWeather[3].getHigh() + "~" + weekWeather[3].getLow());
        week4_windTv.setText(weekWeather[3].getFengxiang());
        week5_dayTv.setText(weekWeather[4].getDate());
        week5_cliTv.setText(weekWeather[4].getType());
        week5_temTv.setText(weekWeather[4].getHigh() + "~" + weekWeather[4].getLow());
        week5_windTv.setText(weekWeather[4].getFengxiang());
        week6_dayTv.setText(weekWeather[5].getDate());
        week6_cliTv.setText(weekWeather[5].getType());
        week6_temTv.setText(weekWeather[5].getHigh() + "~" + weekWeather[5].getLow());
        week6_windTv.setText(weekWeather[5].getFengxiang());
    }

}
