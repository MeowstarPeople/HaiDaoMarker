package com.eric.newmapapi.activity;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Window;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.eric.newmapapi.R;
import com.eric.newmapapi.map.Cluster;
import com.eric.newmapapi.util.LatLon;
import com.eric.newmapapi.util.TransformLat_And_Lon;
public class MarkerClusterActivity extends Activity {
	protected static final String TAG_onMapMoveFinish = "onMapMoveFinish";
	private MapView mMapView;
	private Boolean isAverageCenter=false;
	private Integer mMaxZoom = 12;
	private Integer mGridSize = 60;
	private ArrayList<OverlayItem> mMarkers;
	private Cluster mCluster;
	private double mDistance = 500000;
	private DemoApplication app;
	public LocationClient mLocationClient = null;
	public BDLocationListener myLocationListener = null;
	//public  ItemizedOverlay<OverlayItem> mOverlay;
	public  MyOverlay mOverlay;
	private MyLocationOverlay myLocationOverlay;
	private GeoPoint mCenter ;
	private int LatSpan;// 纬度范围
	private int LngSpan;// 经度范围
	public SQLiteDatabase database;
	public double northlatitude,eastlongitude,latTrans,lonTrans;
	public String islandName;

	public MapView getmMapView() {
		return mMapView;
	}
	public void setmMapView(MapView mMapView) {
		this.mMapView = mMapView;
	}
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.new_map);
		app = (DemoApplication) this.getApplication();
		if (app.mBMapManager == null) {
			app.mBMapManager = new BMapManager(this);
			app.mBMapManager.init(DemoApplication.strKey,null);
		}
		init();//初始化
		addMarkers();//生成海岛经纬度坐标,设置Marker
		this.isAverageCenter=false;
		setListener();// 监听MapView
		LocationOrientate();
	}
	@Override
	protected void onDestroy() {
		mMapView.destroy();//MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
		super.onDestroy();
	}
	/**
	 * 初始化地图相关的代码，创建覆盖物图层，自定义图层，定位图层
	 */
	@SuppressWarnings("deprecation")
	private void init() {
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.setSatellite(true);
		mMapView.getController().setZoom(7);
		mMapView.getController().enableClick(true);
		mMapView.setBuiltInZoomControls(true);
		mCluster = new Cluster(this, mMapView, mGridSize, isAverageCenter, mGridSize, mDistance);
		//mOverlay = new ItemizedOverlay<OverlayItem>(getResources().getDrawable(R.drawable.m0),mMapView);
		mOverlay = new MyOverlay(getResources().getDrawable(R.drawable.m0),mMapView);
		mMapView.getOverlays().clear();
		mMapView.getOverlays().add(mOverlay);
		myLocationOverlay = new MyLocationOverlay(mMapView);//定位图层
	}
	/* 
	 * 要处理overlay点击事件时需要继承ItemizedOverlay，不处理点击事件时可直接生成ItemizedOverlay. 
	 */  
	public class MyOverlay extends ItemizedOverlay<OverlayItem>{
		public MyOverlay(Drawable arg0, MapView arg1) {
			super(arg0, arg1);
		}
		@SuppressWarnings("deprecation")
		@Override
		//标注物点击触发事件
		protected boolean onTap(int position) {
			OverlayItem overlay=getItem(position);
			BitmapDrawable bd=(BitmapDrawable) overlay.getMarker();
			int mlenMarker =bd.getBitmap().getByteCount();//获取marker大小
			BitmapDrawable	bdnav =(BitmapDrawable)(getResources().getDrawable(R.drawable.nav_turn_via_1));//资源中图片大小
			int mlen =bdnav.getBitmap().getByteCount();
			if(mlenMarker==mlen){//判断是否是海岛标注图标，如果是则跳转到详情页
				String islad_name=overlay.getTitle();
				Intent intent=new Intent(getApplicationContext(), IslandinfoActivity.class);
				intent.putExtra("island_name", islad_name);
				startActivity(intent);
			}
			return super.onTap(position);
		}
	}

	/**
	 * 生成海岛经纬度坐标，并设置OverlayItem
	 */
	@SuppressWarnings("deprecation")
	private void addMarkers(){
		database=DemoApplication.database;
		Cursor cursor = database.query("information_db", 
				new String[]{"name","eastlongitude","northlongitude"}, null, null, null, null, null, null);  
		mMarkers = new ArrayList<OverlayItem>();
		//利用游标遍历所有数据对象  
		while(cursor.moveToNext()){  
			islandName = cursor.getString(cursor.getColumnIndex("name"));
			northlatitude=cursor.getDouble(cursor.getColumnIndex("northlongitude"));
			eastlongitude=cursor.getDouble(cursor.getColumnIndex("eastlongitude"));
			LatLon latlon=TransformLat_And_Lon.transform(northlatitude, eastlongitude);
			latTrans=latlon.getLatitude();
			lonTrans=latlon.getLongitude();
			GeoPoint pt = new GeoPoint((int)(latTrans*1e6),(int)(lonTrans*1e6));
			OverlayItem item = new OverlayItem(pt, null, null);
			item.setTitle(islandName);
			item.setMarker(getResources().getDrawable(R.drawable.nav_turn_via_1));
			mMarkers.add(item);
		}
	}
	/**
	 * 标注物显示，刷新MapView
	 */
	private void pinMarkers(ArrayList<OverlayItem> list){
		this.mOverlay.removeAll();
		for(int i=0;i<list.size();i++){
			this.mOverlay.addItem(list.get(i));
		}
		mMapView.refresh();
	}
	/**
	 * 监听MapView的状态，有三个重要的回调函数
	 */
	private void setListener(){
		MKMapViewListener mapViewListener = new MKMapViewListener() {
			@Override
			public void onMapMoveFinish() {
				//判断当前地图的比例尺，如果大于最大比例尺，就不再聚合，直接显示屏幕经纬度范围内的覆盖物 
				if(mMapView.getZoomLevel()>=MarkerClusterActivity.this.mMaxZoom){
					mOverlay.removeAll();
					pinMarkers(refreshVersionClusterMarker(mMarkers));
				}else{
					ArrayList<OverlayItem> clusters = mCluster.createCluster(refreshVersionClusterMarker(mMarkers));
					mOverlay.removeAll();
					pinMarkers(clusters);
				}
			}
			@Override
			public void onMapAnimationFinish() {
				if(mMapView.getZoomLevel()>=MarkerClusterActivity.this.mMaxZoom){
					mOverlay.removeAll();
					pinMarkers(refreshVersionClusterMarker(mMarkers));
				}else{
					ArrayList<OverlayItem> clusters = mCluster.createCluster(refreshVersionClusterMarker(mMarkers));
					mOverlay.removeAll();
					pinMarkers(clusters);
				}
			}
			@Override
			public void onMapLoadFinish() {
			}
			@Override
			public void onClickMapPoi(MapPoi arg0) {
			}
			@Override
			public void onGetCurrentMap(Bitmap arg0) {
			}
		};
		mMapView.regMapViewListener(app.mBMapManager, mapViewListener);
	}
	/**
	 * 用户GPS位置定位
	 */
	private void LocationOrientate() {
		mLocationClient = new LocationClient(MarkerClusterActivity.this);//声明LocationClient类  
		myLocationListener =new MyLocationListener();
		mLocationClient.registerLocationListener(myLocationListener);//注册监听函数
		LocationClientOption option = new LocationClientOption();//该类用来设置定位SDK的定位方式
		option.setAddrType("all");   
		option.setPoiExtraInfo(true);  		         
		option.setProdName("定位GPS");  		         
		option.setOpenGps(true);  		        
		option.setPoiDistance(1500);  
		option.disableCache(true);  		       
		option.setCoorType("bd09ll");      
		option.setPoiNumber(3);  
		option.setPriority(LocationClientOption.NetWorkFirst); 
		mLocationClient.setLocOption(option);
		mLocationClient.start();//启动定位SDK
		LocationData locData = new LocationData();
		myLocationOverlay.setData(locData);
		mMapView.getOverlays().add(myLocationOverlay);
		mMapView.refresh();
	}
	/**
	 * 实现BDLocationListener接口
	 */
	public class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null){
				return;
			}
			double latitude  = location.getLatitude();
			double longitude = location.getLongitude();
			if(latitude==4.9E-324||longitude==4.9E-324){
				latitude = 39.999111;
				longitude = 168.999011;
			}
			LocationData locData = new LocationData();
			locData.latitude = latitude;
			locData.longitude = longitude;
			locData.direction = 2.0f;
			myLocationOverlay.setData(locData);
			mMapView.refresh();
			mMapView.getController().animateTo(new GeoPoint((int)(locData.latitude*1e6),
					(int)(locData.longitude*1e6)));
		}
		public void onReceivePoi(BDLocation poiLocation) {
		}
	}
	/**
	 * 屏幕范围内覆盖物刷新方法，判断标注物在屏幕经纬度范围内
	 * @param list
	 * @return 返回当前屏幕内的所有标注物
	 */
	private ArrayList<OverlayItem> refreshVersionClusterMarker(ArrayList<OverlayItem> list){
		mCenter = mMapView.getMapCenter();
		int lat = mCenter.getLatitudeE6();
		int lng = mCenter.getLongitudeE6();
		LatSpan = mMapView.getLatitudeSpan();
		LngSpan = mMapView.getLongitudeSpan();
		int topLat = lat+LatSpan/2;
		int bottomLat = lat-LatSpan/2;
		int leftLng = lng-LngSpan/2;
		int rightLng = lng+LngSpan/2;
		ArrayList<OverlayItem> result = new ArrayList<OverlayItem>();
		for(int i = 0 ;i<list.size();i++){
			if(list.get(i).getPoint().getLatitudeE6()>bottomLat && 
					list.get(i).getPoint().getLatitudeE6()< topLat &&
					list.get(i).getPoint().getLongitudeE6()>leftLng&&
					list.get(i).getPoint().getLongitudeE6()<rightLng){
				result.add(list.get(i));
			}
		}
		return result;
	}
}
