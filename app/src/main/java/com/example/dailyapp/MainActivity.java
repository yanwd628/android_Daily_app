package com.example.dailyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.necer.calendar.BaseCalendar;
import com.necer.calendar.Miui10Calendar;
import com.necer.entity.CalendarDate;
import com.necer.entity.Lunar;
import com.necer.listener.OnCalendarChangedListener;
import com.necer.utils.CalendarUtil;
import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import static android.app.AlertDialog.THEME_HOLO_LIGHT;

public class MainActivity extends AppCompatActivity implements WeatherSearch.OnWeatherSearchListener {
    private Miui10Calendar miui10Calendar;
    private TextView tv_result;
    private TextView tv_data;
    private TextView tv_desc;
    private TextView tv_weather;
    private TextView tv_location;


    private Button tips_new;
    private ListView tipListView;
    private List<TipInfo> tiplist = new ArrayList<>();
    private ListAdapter mListAdapter;
    private TipsDdataBaseHelper dbHelper;
    private Tips tips;
    private String city_now;
    private TipInfo currentTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_result = findViewById(R.id.tv_result);
        tv_data = findViewById(R.id.tv_data);
        tv_desc = findViewById(R.id.tv_desc);
        tv_weather = findViewById(R.id.tv_weather);
        tv_location =findViewById(R.id.tv_location);
        tips_new=findViewById(R.id.tips_new);
        tipListView =findViewById(R.id.list_tips);
        //创建数据库
        dbHelper =new TipsDdataBaseHelper(this,"MyTips.db",null,1);
        mListAdapter=new com.example.dailyapp.ListAdapter(MainActivity.this,tiplist);
        setListViewHeightBasedOnChildren(tipListView);
        tipListView.setAdapter(mListAdapter);

        tips = Tips.getInstance();
        //测试添加一条数据
//        ContentValues values = new ContentValues();
//        values.put(Tips.location,"定位示例");
//        values.put(Tips.content,"内容示例");
//        Date date = new Date();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        values.put(Tips.time,sdf.format(date));
//        tips.insertTip(dbHelper,values);
        getTipList();
        Intent intent = getIntent();
        if (intent != null){
            getTipList();
           mListAdapter=new com.example.dailyapp.ListAdapter(MainActivity.this,tiplist);
            setListViewHeightBasedOnChildren(tipListView);
            tipListView.setAdapter(mListAdapter);
        }

//点击修改
        tipListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View view1=getLayoutInflater().inflate(R.layout.edittext,null);
                final EditText et=(EditText)view1.findViewById(R.id.tips_input);
                currentTip = tiplist.get(position);
                et.setText(currentTip.getContent());
                new AlertDialog.Builder(MainActivity.this).setTitle("便利贴")
                        .setIcon(R.drawable.ic_weather
                        ).setView(view1)
                        .setPositiveButton("修改", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String content =et.getText().toString();
                                new_tip(content,1);
                                getTipList();
                                mListAdapter=new com.example.dailyapp.ListAdapter(MainActivity.this,tiplist);
                                setListViewHeightBasedOnChildren(tipListView);
                                tipListView.setAdapter(mListAdapter);
                            }
                        }).setNegativeButton("取消",null).show();
            }
        });

//长按删除
        tipListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final TipInfo tipinfo = tiplist.get(position);
                String title ="警告";
                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(R.drawable.ic_weather)
                        .setTitle(title)
                        .setMessage("确定要删除吗?")
                        .setPositiveButton("我想好了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tips = Tips.getInstance();
                                tips.deleteTip(dbHelper,Integer.parseInt(tipinfo.getId()));
                                tiplist.remove(position);
                                getTipList();
                                mListAdapter=new com.example.dailyapp.ListAdapter(MainActivity.this,tiplist);
                                setListViewHeightBasedOnChildren(tipListView);
                                tipListView.setAdapter(mListAdapter);
                                Toast.makeText(MainActivity.this,"删除成功！",Toast.LENGTH_LONG).show();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
                return true;
            }
        });
 //日期选择时变化
        miui10Calendar = findViewById(R.id.miui10Calendar);
        miui10Calendar.setOnCalendarChangedListener(new OnCalendarChangedListener() {
            @Override
            public void onCalendarChange(BaseCalendar baseCalendar, int year, int month, LocalDate localDate) {
                tv_result.setText(year + "年" + month + "月" + "▼");
                if (localDate != null) {
                    CalendarDate calendarDate = CalendarUtil.getCalendarDate(localDate);
                    Lunar lunar = calendarDate.lunar;
                    tv_data.setText(localDate.toString("yyyy年MM月dd日"));
                    tv_desc.setText(lunar.chineseEra + lunar.animals + "年" + lunar.lunarMonthStr + lunar.lunarDayStr);
                } else {
                    tv_data.setText("");
                    tv_desc.setText("");
                }
            }
        });
//实现选择日期跳转
        tv_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(MainActivity.this, THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        miui10Calendar.jumpDate(year, (month + 1), dayOfMonth);
                    }
                }
                        , calendar.get(Calendar.YEAR)
                        , calendar.get(Calendar.MONTH)
                        , calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
//定位view跳转地图页面
        tv_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Main2Activity.class));
            }
        });
 //添加tips按钮事件：弹窗输入（写入数据库）
        tips_new.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view=getLayoutInflater().inflate(R.layout.edittext,null);
                final EditText et=(EditText)view.findViewById(R.id.tips_input);
                new AlertDialog.Builder(MainActivity.this).setTitle("便利贴")
                        .setIcon(R.drawable.ic_weather
                        ).setView(view)
                        .setPositiveButton("添加", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String content =et.getText().toString();
                        new_tip(content,0);
                        setListViewHeightBasedOnChildren(tipListView);
                        tipListView.setAdapter(mListAdapter);
                    }
                }).setNegativeButton("取消",null).show();
            }
        });
//天气模块
        AMapLocationClient aMapLocationClient = new AMapLocationClient(this);
        aMapLocationClient.startLocation();
        aMapLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                WeatherSearchQuery mquery = new WeatherSearchQuery(aMapLocation.getDistrict(), WeatherSearchQuery.WEATHER_TYPE_LIVE);
                WeatherSearch mweathersearch=new WeatherSearch(MainActivity.this);
                mweathersearch.setOnWeatherSearchListener(MainActivity.this);
                mweathersearch.setQuery(mquery);
                mweathersearch.searchWeatherAsyn(); //异步搜索
            }
        });
    }
    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult weatherLiveResult, int rCode) {
        if (rCode == 1000) {
            if (weatherLiveResult != null&&weatherLiveResult.getLiveResult() != null) {
                LocalWeatherLive weatherlive = weatherLiveResult.getLiveResult();
                Toast.makeText(this, "天气更新成功！",Toast.LENGTH_SHORT).show();
                tv_weather.setText(weatherlive.getWeather()+"\n"+weatherlive.getTemperature()+"°"+weatherlive.getWindDirection()+"风     "+weatherlive.getWindPower()+"级");
                tv_location.setText(weatherlive.getReportTime()+"发布"+"\n"+""+weatherlive.getCity());
                set_icon(weatherlive.getWeather());
                city_now=weatherlive.getCity();
            }
        }
    }

    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {
    }
//导入图标
    public void set_icon(String weather)
    {
        HashMap<String,Integer> weatherMap = new HashMap<>();
        weatherMap.put("晴",R.drawable.vector_drawable_qing);
        weatherMap.put("多云",R.drawable.vector_drawable_duoyun);
        weatherMap.put("阴",R.drawable.vector_drawable_yintian);
        weatherMap.put("阵雨",R.drawable.vector_drawable_zhenyu);
        weatherMap.put("雷阵雨",R.drawable.vector_drawable_leizhenyu);
        weatherMap.put("雷阵雨并伴有冰雹",R.drawable.vector_drawable_leizhenyubanyoubingbao);
        weatherMap.put("雨夹雪",R.drawable.vector_drawable_yujiaxue);
        weatherMap.put("小雨",R.drawable.vector_drawable_xiaoyu);
        weatherMap.put("中雨",R.drawable.vector_drawable_zhongyu);
        weatherMap.put("大雨",R.drawable.vector_drawable_dayu);
        weatherMap.put("暴雨",R.drawable.vector_drawable_baoyu);
        weatherMap.put("大暴雨",R.drawable.vector_drawable_dabaoyu);
        weatherMap.put("特大暴雨",R.drawable.vector_drawable_tedabaoyu);
        weatherMap.put("阵雪",R.drawable.vector_drawable_xiaoxue);
        weatherMap.put("小雪",R.drawable.vector_drawable_xiaoxue);
        weatherMap.put("中雪",R.drawable.vector_drawable_zhongxue);
        weatherMap.put("大雪",R.drawable.vector_drawable_daxue);
        weatherMap.put("雾",R.drawable.vector_drawable_wu);
        weatherMap.put("冻雨",R.drawable.vector_drawable_dongyu);
        weatherMap.put("沙尘暴",R.drawable.vector_drawable_shachenbao);
        weatherMap.put("小雨-中雨",R.drawable.vector_drawable_zhongyu);
        weatherMap.put("中雨-大雨",R.drawable.vector_drawable_dayu);
        weatherMap.put("大雨-暴雨",R.drawable.vector_drawable_dayu);
        weatherMap.put("暴雨-大暴雨",R.drawable.vector_drawable_baoyu);
        weatherMap.put("大暴雨-特大暴雨",R.drawable.vector_drawable_tedabaoyu);
        weatherMap.put("小雪-中雪",R.drawable.vector_drawable_xiaoxue);
        weatherMap.put("中雪-大雪",R.drawable.vector_drawable_zhongxue);
        weatherMap.put("大雪-暴雪",R.drawable.vector_drawable_daxue);
        weatherMap.put("浮尘",R.drawable.vector_drawable_unknown);
        weatherMap.put("扬沙",R.drawable.vector_drawable_unknown);
        weatherMap.put("强沙尘暴",R.drawable.vector_drawable_unknown);
        weatherMap.put("飑",R.drawable.vector_drawable_unknown);
        weatherMap.put("龙卷风",R.drawable.vector_drawable_unknown);
        weatherMap.put("弱高吹雪",R.drawable.vector_drawable_unknown);
        weatherMap.put("轻霾",R.drawable.vector_drawable_unknown);
        weatherMap.put("霾",R.drawable.vector_drawable_unknown);
//设置图标
        ImageView weatherImage = findViewById(R.id.image_weather);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            weatherImage.setImageDrawable(getDrawable(weatherMap.get(weather)));
        }
    }

    private void getTipList(){
        tiplist.clear();
        tips = Tips.getInstance();
        Cursor allNotes= tips.getALLTips(dbHelper);
        for (allNotes.moveToFirst(); !allNotes.isAfterLast(); allNotes.moveToNext()){
            TipInfo tipInfo = new TipInfo();
            tipInfo.setId(allNotes.getString(allNotes.getColumnIndex(Tips._id)));
            tipInfo.setLocation(allNotes.getString(allNotes.getColumnIndex(Tips.location)));
            tipInfo.setContent(allNotes.getString(allNotes.getColumnIndex(Tips.content)));
            tipInfo.setTime(allNotes.getString(allNotes.getColumnIndex(Tips.time)));
            tiplist.add(tipInfo);
        }
    }

//创建便利贴
    private  void new_tip(String content,int flag){
        tips = Tips.getInstance();
        ContentValues values = new ContentValues();
        values.put(Tips.location, city_now);
        values.put(Tips.content, content);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        values.put(Tips.time, sdf.format(date));
        if (flag==0) {
            tips.insertTip(dbHelper, values);
            Toast.makeText(this, "添加成功！",Toast.LENGTH_SHORT).show();
        }else{
            tips.updataTip(dbHelper,Integer.parseInt(currentTip.getId()),values);
            Toast.makeText(this, "修改成功！",Toast.LENGTH_SHORT).show();
        }
        getTipList();
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() *(listAdapter.getCount() - 1));
// listView.getDividerHeight()获取子项间分隔符占用的高度
// params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

}

