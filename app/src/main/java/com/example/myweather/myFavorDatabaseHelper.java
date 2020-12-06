package com.example.myweather;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class myFavorDatabaseHelper extends SQLiteOpenHelper{

        private static final int SHORT_DELAY = 2000; // 2 seconds
        public static final String CREATE_DATE = "CREATE TABLE favorCity(" +
                "id integer primary key autoincrement, " +
                "cityName text unique," +
                "cityCode text unique)";
        private Context mContext;

        public myFavorDatabaseHelper (Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
            super(context,name,factory,version);
            mContext = context;


        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DATE);
            Toast.makeText(mContext,"Create succeeded", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }


}
