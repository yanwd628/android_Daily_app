package com.example.dailyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.MyLocationStyle;
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
import java.util.Calendar;
import java.util.HashMap;

import static android.app.AlertDialog.THEME_HOLO_LIGHT;

public class MainActivity extends AppCompatActivity implements WeatherSearch.OnWeatherSearchListener {
    private Miui10Calendar miui10Calendar;
    private TextView tv_result;
    private TextView tv_data;
    private TextView tv_desc;
    private TextView tv_weather;
    private TextView tv_location;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_result = findViewById(R.id.tv_result);
        tv_data = findViewById(R.id.tv_data);
        tv_desc = findViewById(R.id.tv_desc);
        tv_weather = findViewById(R.id.tv_weather);
        tv_location =findViewById(R.id.tv_location);
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



        tv_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Main2Activity.class));
            }
        });
        tv_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Main2Activity.class));
            }
        });


        AMapLocationClient aMapLocationClient = new AMapLocationClient(this);
        aMapLocationClient.startLocation();
        aMapLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {

                System.out.println("aaaaaaaa");
                System.out.println(aMapLocation);
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
                System.out.println(weatherlive.getReportTime()+"发布");
                System.out.println(weatherlive.getWeather());
                System.out.println(weatherlive.getTemperature()+"°");
                System.out.println(weatherlive.getWindDirection()+"风     "+weatherlive.getWindPower()+"级");
                System.out.println("湿度         "+weatherlive.getHumidity()+"%");
                tv_weather.setText(weatherlive.getWeather()+"\n"+weatherlive.getTemperature()+"°"+weatherlive.getWindDirection()+"风     "+weatherlive.getWindPower()+"级");
                tv_location.setText(weatherlive.getReportTime()+"发布"+"\n"+""+weatherlive.getCity());
                set_icon(weatherlive.getWeather());
            }
        }
    }

    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {

    }




    public void set_icon(String weather)
    {
        HashMap<String,Integer> weatherMap = new HashMap<>();
        weatherMap.put("晴",R.drawable.vector_drawable_qing);
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

        ImageView weatherImage = findViewById(R.id.image_weather);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            weatherImage.setImageDrawable(getDrawable(weatherMap.get(weather)));
        }
    }

}

