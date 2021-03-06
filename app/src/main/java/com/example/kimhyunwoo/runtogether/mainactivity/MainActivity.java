package com.example.kimhyunwoo.runtogether.mainactivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.kimhyunwoo.runtogether.BackPressCloseHandler;
import com.example.kimhyunwoo.runtogether.MapUtil;
import com.example.kimhyunwoo.runtogether.R;
import com.example.kimhyunwoo.runtogether.RealTimeDataTransfer;
import com.example.kimhyunwoo.runtogether.UserInfo;
import com.example.kimhyunwoo.runtogether.bluetoothmanagement.PolarBleReceiver;
import com.example.kimhyunwoo.runtogether.upperactivity.LogoutRequest;
import com.example.kimhyunwoo.runtogether.upperactivity.UpperFragment;
import com.example.kimhyunwoo.runtogether.usermanagement.LoginActivity;
import com.example.kimhyunwoo.runtogether.usermanagement.StartActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //  뒤로가기 버튼을 2번 누르면 종료시키는 클레스
    private BackPressCloseHandler backPressCloseHandler;

    LocationManager manager;
    ViewPager pager;
    TabLayout tab;

    android.support.v4.app.Fragment historys, main, friends;

    MapUtil mapUtil;

    public LocationManager getLocationManager() {
        return manager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mapUtil = new MapUtil();
        backPressCloseHandler = new BackPressCloseHandler(this);

        Configuration config = new Configuration();
        config = mapUtil.setLocaleResources();
        getBaseContext().getResources().updateConfiguration(
                config,getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.activity_main);

        //  상단바 연결
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frag_upper, new UpperFragment());
        fragmentTransaction.commit();

        //  Context에 있는 location 상수 알려주기
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //  ViewPager 위젯 연결
        pager = (ViewPager) findViewById(R.id.vp_pager);
        //  TabLayout 연결
        tab = (TabLayout) findViewById(R.id.tl_tab);
        tab.addTab(tab.newTab().setText("History"));
        tab.addTab(tab.newTab().setText("Main"));
        tab.addTab(tab.newTab().setText("Friends"));

        //  각각 프래그먼트 생성
        historys = new HistoryFragment();
        main = new MainFragment();
        friends = new FriendsFragment();

        //  프래그먼트 리스트에 넣음
        //  Fragment를 v4로 사용함
        //  버그생기면 머리 아파짐 ㅠ
        List<android.support.v4.app.Fragment> datas = new ArrayList<>();
        datas.add(historys);
        datas.add(main);
        datas.add(friends);

        //  프래그먼트 매니저, 어뎁터에 전달
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), datas);

        //  어뎁터를 페이저 위젯 연결
        pager.setAdapter(adapter);
        pager.setCurrentItem(adapter.getCount()-2);
        //  페이저 변경 됬을 때 변경해주는 리스너
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tab));
        pager.setOffscreenPageLimit(3);

        //  탭이 변경 됬을 때 변경해주는 리스너
        tab.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(pager));

        //  권한 실행
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkPermission();
        }

        activatePolar();
        Toast.makeText(MainActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
    }

    //  권한 체크
    private final int REQ_PERMISSION = 100;

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        // 권한을 가지고 있는지 시스템에 확인
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        } else {
            //  권한이 없으면 사용자에 권한을 달라고 요청
            String permissions[] = {Manifest.permission.ACCESS_FINE_LOCATION};
            //  권한을 요구하는 팝업
            requestPermissions(permissions, REQ_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSION) {
            //  사용자의 승인
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                //  거절했을 경우 띄움
                cancel();
            }
        }
    }
    
    public void cancel() {
        Toast.makeText(this, "The user rejects GPS rights.", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "The App don't work normally.", Toast.LENGTH_SHORT).show();
        finish();

    }

    @Override
    public void onBackPressed(){
        backPressCloseHandler.onBackPressed();;
    }


    class PagerAdapter extends FragmentStatePagerAdapter {

        List<android.support.v4.app.Fragment> datas;

        public PagerAdapter(FragmentManager frag, List<android.support.v4.app.Fragment> datas) {
            super(frag);
            this.datas = datas;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return datas.get(position);
        }

        @Override
        public int getCount() {
            return datas.size();
        }
    }

    private final PolarBleReceiver mPolarBleUpdateReceiver = new PolarBleReceiver() {};

    protected void activatePolar() {
        Log.w("User's Log", "activatePolar()");
        registerReceiver(mPolarBleUpdateReceiver, makePolarGattUpdateIntentFilter());
    }

    protected void deactivatePolar() {
        unregisterReceiver(mPolarBleUpdateReceiver);
    }

    private static IntentFilter makePolarGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PolarBleReceiver.ACTION_GATT_CONNECTED);
        intentFilter.addAction(PolarBleReceiver.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(PolarBleReceiver.ACTION_HR_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onDestroy()
    {
        Log.v("User's Log","OnDestroy before response");
        if(!UserInfo.getUserToken().isEmpty())
        {
            Log.v("User's Log","OnDestroy before response after 'if'command");
            Response.Listener<String> reponseListener = new Response.Listener<String>() {
                // Volley 를 통해서 정상적으로 웹서버와 통신이 되면 실행되는 함수
                @Override
                public void onResponse(String response)
                {
                }
            };
            LogoutRequest logoutRequest = new LogoutRequest(reponseListener,MainActivity.this);           // 위에서 작성한 리스너를 기반으로 요청하는 클래스를 선언.(LoginRequest참고)
            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);            // Volley의 사용법으로 request queue로 queue를 하나 선언하고
            queue.add(logoutRequest);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                }
            }, 2000);
        }
        Log.v("User's Log","OnDestroy after response");
        super.onDestroy();
    }
}
