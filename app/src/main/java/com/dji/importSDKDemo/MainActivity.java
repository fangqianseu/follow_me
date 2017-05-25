package com.dji.importSDKDemo;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.OnMapClickListener;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import dji.common.flightcontroller.FlightControllerState;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.common.error.DJIError;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

public class MainActivity extends FragmentActivity implements OnMapClickListener {

    protected static final String TAG = "MainActivity";

    private MapView mapView;
    private AMap aMap;

    private TextView mstate, mLat, mLng, mAlt;
    private Button mbltooth, locate, add, clear;
    private Button config, upload, start, stop;

    private boolean isAdd = false;

    private double droneLocationLat = 181, droneLocationLng = 181;
    private double realLocationLat = 181, realLocationLng = 181;
    private double realLocationAlt = -1;

    private final Map<Integer, Marker> mMarkers = new ConcurrentHashMap<Integer, Marker>();
    private Marker droneMarker = null;

    private float altitude = 200.0f;
    private float mSpeed = 10.0f;

    private List<Waypoint> waypointList = new ArrayList<>();
    private List<LatLng> linedraw = new ArrayList<>();
    private List<Polyline> Polylinelist = new ArrayList<>();


    public static WaypointMission.Builder waypointMissionBuilder;
    private FlightController mFlightController;
    private WaypointMissionOperator instance;
    private WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
    private WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.AUTO;

    private int delay = 6000, repeat = 1000, followradius = -1;

    static String BlueToothAddress = "null";
    private BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

    private Timer myTimer;
    private TimerTask myTask;

    /* 一些常量，代表服务器的名称 */
    public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";

    private serverThread startServerThread = null;
    private BluetoothSocket socket = null;
    private BluetoothServerSocket mserverSocket = null;

    @Override
    protected void onResume() {
        super.onResume();
        initFlightController();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        removeListener();
        super.onDestroy();
    }

    /**
     * @Description : RETURN Button RESPONSE FUNCTION
     */
    public void onReturn(View view) {
        Log.d(TAG, "onReturn");
        this.finish();
    }

    private void setResultToToast(final String string) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void initUI() {

        mstate = (TextView) findViewById(R.id.ConnectStatusTextView);
//        message = (TextView) findViewById(R.id.message);

        mLat = (TextView) findViewById(R.id.Lat);
        mLng = (TextView) findViewById(R.id.Lng);
        mAlt = (TextView) findViewById(R.id.Alt);

//        mbltooth = (Button) findViewById(R.id.bluetooth);
//        locate = (Button) findViewById(R.id.locate);
//        add = (Button) findViewById(R.id.add);
//        clear = (Button) findViewById(R.id.clear);
//        config = (Button) findViewById(R.id.config);
//        upload = (Button) findViewById(R.id.upload);
//        start = (Button) findViewById(R.id.start);
//        stop = (Button) findViewById(R.id.stop);
//
//        mbltooth.setOnClickListener(this);
//        locate.setOnClickListener(this);
//        add.setOnClickListener(this);
//        clear.setOnClickListener(this);
//        config.setOnClickListener(this);
//        upload.setOnClickListener(this);
//        start.setOnClickListener(this);
//        stop.setOnClickListener(this);

        leftLowerButton();
        rightLowerButton();
    }

    private void leftLowerButton() {
        int redActionButtonSize = getResources().getDimensionPixelSize(
                R.dimen.red_action_button_size);
        int redActionButtonMargin = getResources().getDimensionPixelOffset(
                R.dimen.action_button_margin);
        int redActionButtonContentSize = getResources().getDimensionPixelSize(
                R.dimen.red_action_button_content_size);
        int redActionButtonContentMargin = getResources()
                .getDimensionPixelSize(R.dimen.red_action_button_content_margin);

        int redActionMenuRadius = getResources().getDimensionPixelSize(
                R.dimen.red_action_menu_radius);
        int blueSubActionButtonSize = getResources().getDimensionPixelSize(
                R.dimen.blue_sub_action_button_size);
        int blueSubActionButtonContentMargin = getResources()
                .getDimensionPixelSize(
                        R.dimen.blue_sub_action_button_content_margin);

        ImageView fabIconStar = new ImageView(this);
//        fabIconStar.setImageResource(R.drawable.ic_action_camera);

        // 设置菜单按钮Button的宽、高，边距
        FloatingActionButton.LayoutParams starParams = new FloatingActionButton.LayoutParams(
                redActionButtonSize, redActionButtonSize);
        starParams.setMargins(redActionButtonMargin, redActionButtonMargin,
                redActionButtonMargin, redActionButtonMargin);
        fabIconStar.setLayoutParams(starParams);

        // 设置菜单按钮Button里面图案的宽、高，边距
        FloatingActionButton.LayoutParams fabIconStarParams = new FloatingActionButton.LayoutParams(
                redActionButtonContentSize, redActionButtonContentSize);
        fabIconStarParams.setMargins(redActionButtonContentMargin,
                redActionButtonContentMargin, redActionButtonContentMargin,
                redActionButtonContentMargin);

        final FloatingActionButton leftCenterButton = new FloatingActionButton.Builder(
                this).setContentView(fabIconStar, fabIconStarParams)
                .setPosition(FloatingActionButton.POSITION_BOTTOM_LEFT)
                .setLayoutParams(starParams).build();

        SubActionButton.Builder lCSubBuilder = new SubActionButton.Builder(this);

        //设置菜单中图标的参数
        FrameLayout.LayoutParams blueContentParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        blueContentParams.setMargins(blueSubActionButtonContentMargin,
                blueSubActionButtonContentMargin,
                blueSubActionButtonContentMargin,
                blueSubActionButtonContentMargin);

        final ImageView rlIcon1 = new ImageView(this);
        final ImageView rlIcon2 = new ImageView(this);
        final ImageView rlIcon3 = new ImageView(this);
        final ImageView rlIcon4 = new ImageView(this);
        // 设置弹出菜单的图标
        rlIcon1.setImageResource(R.drawable.bluetooth);
        rlIcon2.setImageResource(R.drawable.locate);
        rlIcon3.setImageResource(R.drawable.add);
        rlIcon4.setImageResource(R.drawable.clean);

        final FloatingActionMenu leftCenterMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(lCSubBuilder.setContentView(rlIcon1, blueContentParams).build())
                .addSubActionView(lCSubBuilder.setContentView(rlIcon2, blueContentParams).build())
                .addSubActionView(lCSubBuilder.setContentView(rlIcon3, blueContentParams).build())
                .addSubActionView(lCSubBuilder.setContentView(rlIcon4, blueContentParams).build())
                .setRadius(redActionMenuRadius).setStartAngle(-100).setEndAngle(20)
                .attachTo(leftCenterButton).build();

        leftCenterMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu floatingActionMenu) {

                rlIcon1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startserver();
                    }
                });

                rlIcon2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateDroneLocation();
                        cameraUpdate();
                    }
                });

                rlIcon3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enableDisableAdd();
                    }
                });

                rlIcon4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                aMap.clear();
                            }

                        });
                        waypointList.clear();
                        waypointMissionBuilder.waypointList(waypointList);
                        updateDroneLocation();
                    }
                });
            }

            @Override
            public void onMenuClosed(FloatingActionMenu floatingActionMenu) {

            }
        });

    }

    private void rightLowerButton() {
        int redActionButtonSize = getResources().getDimensionPixelSize(
                R.dimen.red_action_button_size);
        int redActionButtonMargin = getResources().getDimensionPixelOffset(
                R.dimen.action_button_margin);
        int redActionButtonContentSize = getResources().getDimensionPixelSize(
                R.dimen.red_action_button_content_size);
        int redActionButtonContentMargin = getResources()
                .getDimensionPixelSize(R.dimen.red_action_button_content_margin);

        int redActionMenuRadius = getResources().getDimensionPixelSize(
                R.dimen.red_action_menu_radius);
        int blueSubActionButtonSize = getResources().getDimensionPixelSize(
                R.dimen.blue_sub_action_button_size);
        int blueSubActionButtonContentMargin = getResources()
                .getDimensionPixelSize(
                        R.dimen.blue_sub_action_button_content_margin);

        ImageView fabIconStar = new ImageView(this);
//        fabIconStar.setImageResource(R.drawable.ic_action_camera);

        // 设置菜单按钮Button的宽、高，边距
        FloatingActionButton.LayoutParams starParams = new FloatingActionButton.LayoutParams(
                redActionButtonSize, redActionButtonSize);
        starParams.setMargins(redActionButtonMargin, redActionButtonMargin,
                redActionButtonMargin, redActionButtonMargin);
        fabIconStar.setLayoutParams(starParams);

        // 设置菜单按钮Button里面图案的宽、高，边距
        FloatingActionButton.LayoutParams fabIconStarParams = new FloatingActionButton.LayoutParams(
                redActionButtonContentSize, redActionButtonContentSize);
        fabIconStarParams.setMargins(redActionButtonContentMargin,
                redActionButtonContentMargin, redActionButtonContentMargin,
                redActionButtonContentMargin);

        final FloatingActionButton leftCenterButton = new FloatingActionButton.Builder(
                this).setContentView(fabIconStar, fabIconStarParams)
                .setPosition(FloatingActionButton.POSITION_BOTTOM_RIGHT)
                .setLayoutParams(starParams).build();

        SubActionButton.Builder lCSubBuilder = new SubActionButton.Builder(this);

        //设置菜单中图标的参数
        FrameLayout.LayoutParams blueContentParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        blueContentParams.setMargins(blueSubActionButtonContentMargin,
                blueSubActionButtonContentMargin,
                blueSubActionButtonContentMargin,
                blueSubActionButtonContentMargin);

        final ImageView rlIcon1 = new ImageView(this);
        final ImageView rlIcon2 = new ImageView(this);
        final ImageView rlIcon3 = new ImageView(this);
        final ImageView rlIcon4 = new ImageView(this);
        // 设置弹出菜单的图标
        rlIcon1.setImageResource(R.drawable.set);
        rlIcon2.setImageResource(R.drawable.update);
        rlIcon3.setImageResource(R.drawable.start);
        rlIcon4.setImageResource(R.drawable.stop);

        final FloatingActionMenu leftCenterMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(lCSubBuilder.setContentView(rlIcon1, blueContentParams).build())
                .addSubActionView(lCSubBuilder.setContentView(rlIcon2, blueContentParams).build())
                .addSubActionView(lCSubBuilder.setContentView(rlIcon3, blueContentParams).build())
                .addSubActionView(lCSubBuilder.setContentView(rlIcon4, blueContentParams).build())
                .setRadius(redActionMenuRadius).setStartAngle(-80).setEndAngle(-200)
                .attachTo(leftCenterButton).build();

        leftCenterMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu floatingActionMenu) {

                rlIcon1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSettingDialog();
                    }
                });

                rlIcon2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadWayPointMission();
                    }
                });

                rlIcon3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startWaypointMission();
                    }
                });

                rlIcon4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopWaypointMission();
                    }
                });
            }

            @Override
            public void onMenuClosed(FloatingActionMenu floatingActionMenu) {

            }
        });
    }

    private void initMapView() {

        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.setOnMapClickListener(this);// add the listener for click for amap object
        }

//        LatLng shenzhen = new LatLng(22.5362, 113.9454);
//        aMap.addMarker(new MarkerOptions().position(shenzhen).title("Marker in Shenzhen"));
//        aMap.moveCamera(CameraUpdateFactory.newLatLng(shenzhen));
    }

    private void initbuletooth() {

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                BlueToothAddress = device.getAddress();
                mstate.setText(device.getName() + ":" + BlueToothAddress);
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When the compile and target version is higher than 22, please request the
        // following permissions at runtime to ensure the
        // SDK work well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }

        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIDemoApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        initMapView();
        initUI();
        addListener();

        initbuletooth();

    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    private void onProductConnectionChange() {
        initFlightController();
    }

    private void initFlightController() {

        BaseProduct product = DJIDemoApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }

        if (mFlightController != null) {

            mFlightController.setStateCallback(
                    new FlightControllerState.Callback() {
                        @Override
                        public void onUpdate(FlightControllerState
                                                     djiFlightControllerCurrentState) {
                            realLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                            realLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                            realLocationAlt = djiFlightControllerCurrentState.getAircraftLocation().getAltitude();

                            LatLng after = CoordinateUtil.toGCJ02Point(realLocationLat, realLocationLng);
                            droneLocationLat = after.latitude;
                            droneLocationLng = after.longitude;

                            updateDroneLocation();
                        }
                    });

        }
    }

    //Add Listener for WaypointMissionOperator
    private void addListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().addListener(eventNotificationListener);
        }
    }

    private void removeListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().removeListener(eventNotificationListener);
        }
    }

    private WaypointMissionOperatorListener eventNotificationListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(WaypointMissionDownloadEvent downloadEvent) {

        }

        @Override
        public void onUploadUpdate(WaypointMissionUploadEvent uploadEvent) {

        }

        @Override
        public void onExecutionUpdate(WaypointMissionExecutionEvent executionEvent) {

        }

        @Override
        public void onExecutionStart() {

        }

        @Override
        public void onExecutionFinish(@Nullable final DJIError error) {
            setResultToToast("任务结束: " + (error == null ? "成功!" : error.getDescription()));

            stopTimer();
        }
    };

    public WaypointMissionOperator getWaypointMissionOperator() {
        if (instance == null) {
            instance = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
        }
        return instance;
    }

    @Override
    public void onMapClick(LatLng point) {
        if (isAdd == true) {
            markWaypoint(point);

            LatLng after = CoordinateUtil.toWGS84Point(point.latitude, point.longitude);

            Waypoint mWaypoint = new Waypoint(after.latitude, after.longitude, altitude);

//            Waypoint mWaypoint = new Waypoint(point.latitude, point.longitude, altitude);
            //Add Waypoints to Waypoint arraylist;
            if (waypointMissionBuilder != null) {
                waypointList.add(mWaypoint);
                waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
            } else {
                waypointMissionBuilder = new WaypointMission.Builder();
                waypointList.add(mWaypoint);
                waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
            }
        } else {
            setResultToToast("Cannot Add Waypoint");
        }
    }

    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    // Update the drone location based on states from MCU.
    private void updateDroneLocation() {

        java.text.DecimalFormat df = new java.text.DecimalFormat("#.######");

        String mylat = df.format(realLocationLat);
        String myLng = df.format(realLocationLng);
        String myAlt = df.format(realLocationAlt);

        Message msg2 = new Message();
        String info = myLng + "-" + mylat + "-" + myAlt;
        msg2.obj = info;
        msg2.what = 1;
        LinkDetectedHandler.sendMessage(msg2);

        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }

                if (checkGpsCoordination(droneLocationLat, droneLocationLng)) {
                    droneMarker = aMap.addMarker(markerOptions);
                }
            }
        });
    }

    private void markWaypoint(LatLng point) {
        //Create MarkerOptions object
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        Marker marker = aMap.addMarker(markerOptions);
        mMarkers.put(mMarkers.size(), marker);
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.bluetooth: {
//                startserver();
//                break;
//            }
//            case R.id.locate: {
//                updateDroneLocation();
//                cameraUpdate(); // Locate the drone's place
//                break;
//            }
//            case R.id.add: {
//                enableDisableAdd();
//                break;
//            }
//            case R.id.clear: {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        aMap.clear();
//                    }
//
//                });
//                waypointList.clear();
//                waypointMissionBuilder.waypointList(waypointList);
//                updateDroneLocation();
//                break;
//            }
//            case R.id.config: {
//                showSettingDialog();
//                break;
//            }
//            case R.id.upload: {
//                uploadWayPointMission();
//                break;
//            }
//            case R.id.start: {
//                startWaypointMission();
//                break;
//            }
//            case R.id.stop: {
//                stopWaypointMission();
//
//                break;
//            }
//            default:
//                break;
//        }
//    }

    private void cameraUpdate() {
        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        float zoomlevel = (float) 18.0;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pos, zoomlevel);
        aMap.moveCamera(cu);

    }

    private void enableDisableAdd() {
        if (isAdd == false) {
            isAdd = true;
//            add.setText("Exit");
        } else {
            isAdd = false;
//            add.setText("Add");
        }
    }

    private void showSettingDialog() {
        LinearLayout wayPointSettings = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_waypointsetting, null);

        final TextView wpAltitude_TV = (TextView) wayPointSettings.findViewById(R.id.altitude);
        final TextView wpfllowradius_TV = (TextView) wayPointSettings.findViewById(R.id.Followradius);

        RadioGroup speed_RG = (RadioGroup) wayPointSettings.findViewById(R.id.speed);
        RadioGroup actionAfterFinished_RG = (RadioGroup) wayPointSettings.findViewById(R.id.actionAfterFinished);
        RadioGroup heading_RG = (RadioGroup) wayPointSettings.findViewById(R.id.heading);

        speed_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.lowSpeed) {
                    mSpeed = 1.0f;
                } else if (checkedId == R.id.MidSpeed) {
                    mSpeed = 3.0f;
                } else if (checkedId == R.id.HighSpeed) {
                    mSpeed = 5.0f;
                }
            }

        });

        actionAfterFinished_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "Select finish action");
                if (checkedId == R.id.finishNone) {
                    mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
                } else if (checkedId == R.id.finishGoHome) {
                    mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
                } else if (checkedId == R.id.finishAutoLanding) {
                    mFinishedAction = WaypointMissionFinishedAction.AUTO_LAND;
                } else if (checkedId == R.id.finishToFirst) {
                    mFinishedAction = WaypointMissionFinishedAction.GO_FIRST_WAYPOINT;
                }
            }
        });

        heading_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "Select heading");

                if (checkedId == R.id.headingNext) {
                    mHeadingMode = WaypointMissionHeadingMode.AUTO;
                } else if (checkedId == R.id.headingInitDirec) {
                    mHeadingMode = WaypointMissionHeadingMode.USING_INITIAL_DIRECTION;
                } else if (checkedId == R.id.headingRC) {
                    mHeadingMode = WaypointMissionHeadingMode.CONTROL_BY_REMOTE_CONTROLLER;
                } else if (checkedId == R.id.headingWP) {
                    mHeadingMode = WaypointMissionHeadingMode.USING_WAYPOINT_HEADING;
                }
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("")
                .setView(wayPointSettings)
                .setPositiveButton("完成", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        String altitudeString = wpAltitude_TV.getText().toString();
                        altitude = Integer.parseInt(nulltoIntegerDefalt(altitudeString));

                        delay = 2500;
                        repeat = 1500;

                        String followradiusString = wpfllowradius_TV.getText().toString();
                        followradius = Integer.parseInt(nulltoIntegerDefalt(followradiusString));


                        Log.e(TAG, "altitude " + altitude);
                        Log.e(TAG, "speed " + mSpeed);
                        Log.e(TAG, "mFinishedAction " + mFinishedAction);
                        Log.e(TAG, "mHeadingMode " + mHeadingMode);
                        Log.e(TAG, "mdelay " + delay);
                        Log.e(TAG, "repeat " + repeat);

                        configWayPointMission();
                    }

                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }

                })
                .create()
                .show();
    }

    String nulltoIntegerDefalt(String value) {
        if (!isIntValue(value)) value = "0";
        return value;
    }

    boolean isIntValue(String val) {
        try {
            val = val.replace(" ", "");
            Integer.parseInt(val);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void configWayPointMission() {

        if (waypointMissionBuilder == null) {

            waypointMissionBuilder = new WaypointMission.Builder().finishedAction(mFinishedAction)
                    .headingMode(mHeadingMode)
                    .autoFlightSpeed(mSpeed)
                    .maxFlightSpeed(mSpeed)
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

        } else {
            waypointMissionBuilder.finishedAction(mFinishedAction)
                    .headingMode(mHeadingMode)
                    .autoFlightSpeed(mSpeed)
                    .maxFlightSpeed(mSpeed)
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

        }

        if (waypointMissionBuilder.getWaypointList().size() > 0) {

            for (int i = 0; i < waypointMissionBuilder.getWaypointList().size(); i++) {
                waypointMissionBuilder.getWaypointList().get(i).altitude = altitude;
            }

            setResultToToast("设置飞行参数成功");
        }

        DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());
        if (error == null) {
            setResultToToast("下载飞行任务成功");
        } else {
            setResultToToast("下载飞行任务失败 " + error.getDescription());
        }

    }

    private void uploadWayPointMission() {

        getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null) {
                    setResultToToast("上传任务成功!");
                } else {
                    setResultToToast("上传任务失败, 原因: " + error.getDescription() + " 重试...");
                    getWaypointMissionOperator().retryUploadMission(null);
                }
            }
        });

    }

    private void startWaypointMission() {

        getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null) {
                    startTimer();
                }
                drawline();
                setResultToToast("任务开始: " + (error == null ? "成功" : error.getDescription()));
            }
        });

    }

    private void stopWaypointMission() {

        getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null) {
                    stopTimer();
                }
                setResultToToast("任务结束: " + (error == null ? "成功" : error.getDescription()));

            }
        });

    }

    private void drawline() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLng last = null;
                if (waypointMissionBuilder.getWaypointList().size() > 0) {
                    for (int i = 0; i < waypointMissionBuilder.getWaypointList().size(); i++) {
                        Waypoint point = waypointMissionBuilder.getWaypointList().get(i);
                        LatLng after = CoordinateUtil.toGCJ02Point(point.coordinate.getLatitude(), point.coordinate.getLongitude());

                        if (i == waypointMissionBuilder.getWaypointList().size() - 1) {
                            last = after;
                        }

                        linedraw.add(after);
                    }
                }

                Polyline polyline = aMap.addPolyline(new PolylineOptions().
                        addAll(linedraw).width(10).color(Color.argb(255, 255, 72, 56)));
                Polylinelist.add(polyline);

                linedraw.clear();
                linedraw.add(last);
            }
        });


    }

    private void startserver() {
        startServerThread = new serverThread();
        startServerThread.start();
    }

    // 开启线程作为服务端
    private class serverThread extends Thread {
        public void run() {
            try {
                // 创建一个蓝牙服务器 参数分别：服务器名称、UUID
                mserverSocket = mBtAdapter
                        .listenUsingRfcommWithServiceRecord(
                                PROTOCOL_SCHEME_RFCOMM,
                                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                Message msg = new Message();
                msg.obj = "等待客户端连接...";
                msg.what = 0;
                LinkDetectedHandler.sendMessage(msg);

                // 接受客户端的连接请求
                socket = mserverSocket.accept();

                Message msg2 = new Message();
                String info = "客户端已连接.";
                msg2.obj = info;
                msg2.what = 0;
                LinkDetectedHandler.sendMessage(msg2);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
    }

    private Handler LinkDetectedHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (0 == msg.what) {
                mstate.setText((String) msg.obj);
            } else {
                String[] s = ((String) msg.obj).split("-");

                mLng.setText(s[0]);
                mLat.setText(s[1]);
                mAlt.setText(s[2]);
            }
        }
    };

    // 发送数据
    private void sendMessageHandle(String msg) {
        if (socket == null) {
            Toast.makeText(getApplicationContext(), "当前无连接", Toast.LENGTH_LONG).show();
        }
        try {
            OutputStream out = socket.getOutputStream();
            out.write(msg.getBytes());
        } catch (Exception e) {
        }
    }

    private void send() {
        String str = realLocationLat + "-" + realLocationLng + "-" + realLocationAlt;
        sendMessageHandle(str);
    }

    private void startTimer() {

        String s = "speed-" + mSpeed + "-radius-" + followradius;
        sendMessageHandle(s);

        myTimer = new Timer();
        myTask = new TimerTask() {
            @Override
            public void run() {
                send();
            }
        };
        myTimer.schedule(myTask, delay, repeat); // 延时3秒后首次执行，每隔1秒执行1次
    }

    private void stopTimer() {

        if (myTask != null && myTimer != null) {
            myTask.cancel();
            myTimer.cancel();
            myTask = null;
            myTimer = null;
            sendMessageHandle("end");
        }
    }
}
