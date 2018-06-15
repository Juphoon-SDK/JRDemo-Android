package com.juphoon.rcs.jrdemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class LocationBaiduActivity extends Activity implements GeoBaiduSearch.IGeoBaiduSearch {

    public final static String LOCATION_PATH = "location_file";
    public final static String LOCATION_NAME = "locaton_name";
    public final static String LOCATION_LATITUDE = "location_latitude";
    public final static String LOCATION_LONGITUDE = "location_longitude";
    public final static String LOCATION_RADIUS = "location_radius";

    public final static String LOCATION_JSONSTRING = "location_jsonstring";

    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    BitmapDescriptor mCurrentMarker;

    MapView mMapView;
    BaiduMap mBaiduMap;

    OnCheckedChangeListener radioButtonListener;
    boolean isFirstLoc = true;

    LatLng mMyLoc = null;
    LatLng mLoc = null;
    float mRadius = 0.0f;
    String mLocationName = "";

    private TextView mLocationAddr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15.0f));

        mLocationAddr = (TextView) findViewById(R.id.location_address);

        mBaiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {

            @Override
            public void onMapStatusChangeStart(MapStatus arg0) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus arg0) {
                if (sHandler.hasMessages(MSG_MAP_STAT_CHANGED)) {
                    sHandler.removeMessages(MSG_MAP_STAT_CHANGED);
                }
                Message msg = new Message();
                msg.what = MSG_MAP_STAT_CHANGED;
                msg.obj = LocationBaiduActivity.this;
                sHandler.sendMessageDelayed(msg, 100);
            }

            @Override
            public void onMapStatusChange(MapStatus arg0) {
            }
        });

        mBaiduMap.setOnMapClickListener(new OnMapClickListener() {

            @Override
            public boolean onMapPoiClick(MapPoi location) {
                return false;
            }

            @Override
            public void onMapClick(LatLng location) {
                MapStatusUpdate u = MapStatusUpdateFactory
                        .newLatLng(new LatLng(location.latitude,
                                location.longitude));
                mBaiduMap.animateMapStatus(u);
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra(LOCATION_JSONSTRING)) {
            String s = intent.getStringExtra(LOCATION_JSONSTRING);
            if (s != null) {
                JSONObject obj;
                try {
                    obj = new JSONObject(s);
                    if (obj.opt(LOCATION_LATITUDE) != null && obj.opt(LOCATION_LONGITUDE) != null) {
                        mLoc = new LatLng(Double.parseDouble(obj.getString(LOCATION_LATITUDE))
                                , Double.parseDouble(obj.getString(LOCATION_LONGITUDE)));
                        MapStatusUpdate u = MapStatusUpdateFactory
                                .newLatLng(new LatLng(mLoc.latitude,
                                        mLoc.longitude));
                        mBaiduMap.setMapStatus(u);
                        findViewById(R.id.bottom_view).setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if (intent.hasExtra(LOCATION_NAME)) {
            mLocationAddr.setText(intent.getStringExtra(LOCATION_NAME));
        }

        mLocClient = new LocationClient(this);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.registerLocationListener(myListener);
        mLocClient.start();
    }

    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mMapView == null)
                return;
            mMyLoc = new LatLng(location.getLatitude(), location.getLongitude());
            mRadius = location.getRadius();
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius()).direction(100)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                if (mLoc == null) {
                    mLoc = mMyLoc;
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(mMyLoc);
                    mBaiduMap.animateMapStatus(u);
                } else {
                    MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(mLoc);
                    mBaiduMap.animateMapStatus(u);
                }
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }

//        @Override
//        public void onReceivePoi(BDLocation poiLocation) {
//        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mLocClient.unRegisterLocationListener(myListener);
        mLocClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    public void onSend(View view) {
        if (mLoc == null || TextUtils.isEmpty(mLocationName))
            return;
        Intent intent = new Intent();
        intent.putExtra(LOCATION_PATH, "");
        intent.putExtra(LOCATION_NAME, mLocationName);
        intent.putExtra(LOCATION_LATITUDE, mLoc.latitude);
        intent.putExtra(LOCATION_LONGITUDE, mLoc.longitude);
        intent.putExtra(LOCATION_RADIUS, mRadius);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onBackMyLoc(View view) {
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(mMyLoc);
        mBaiduMap.animateMapStatus(u);
    }

    @Override
    public void onAddress(boolean succ, String cookie, String address) {

        if (succ && mLoc.toString().equals(cookie)) {
            mLocationName = address;
            if (sHandler.hasMessages(MSG_UPDATE_ADDRESS)) {
                sHandler.removeMessages(MSG_UPDATE_ADDRESS);
            }
            Message msg = new Message();
            msg.what = MSG_UPDATE_ADDRESS;
            msg.obj = this;
            sHandler.sendMessageDelayed(msg, 100);
        }
    }

    private void updateAddress() {
        if (mLocationAddr != null) {
            mLocationAddr.setText(mLocationName);
        }
    }

    private void updateMapState() {
        mLoc = mBaiduMap.getProjection().fromScreenLocation(
                new Point(mMapView.getWidth() / 2,
                        mMapView.getHeight() / 2));
        GeoBaiduSearch.getInstance().addReverseGeoCodeSearch(
                mLoc.toString(), LocationBaiduActivity.this, mLoc);
    }

    private static final int MSG_UPDATE_ADDRESS = 0;
    private static final int MSG_MAP_STAT_CHANGED = MSG_UPDATE_ADDRESS + 1;
    private static Handler sHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE_ADDRESS) {
                ((LocationBaiduActivity) msg.obj).updateAddress();
            } else if (msg.what == MSG_MAP_STAT_CHANGED) {
                ((LocationBaiduActivity) msg.obj).updateMapState();
            }
        }
    };

}
