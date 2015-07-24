package com.eric.newmapapi.map;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.eric.newmapapi.R;
import com.eric.newmapapi.activity.MarkerClusterActivity;
import com.eric.newmapapi.util.MapUtils;

public class Cluster{
	private static final String TAG_ADD_Cluster = "AddCluster_method";
	private MarkerClusterActivity context;
	private MapView mMapView;
	private Boolean isAverageCenter;
	private int mGridSize;
	private double mDistance;
	private List<ClusterMarker> mClusterMarkers;
	public Cluster(MarkerClusterActivity context,MapView mapView
			,int minClusterSize,Boolean isAverageCenter
			,int mGridSize,double mDistance) {
		this.context = context;
		this.mMapView = mapView;
		this.isAverageCenter = isAverageCenter;
		this.mGridSize = mGridSize;
		this.mDistance = mDistance;
		mClusterMarkers = new ArrayList<ClusterMarker>();
	}
	/**
	 * 将list中的每一个item,创建或添加到最合适的ClusterMarker中,判断逻辑在addCluster()实现。
	 * 根据每个ClusterMarker的大小,设置显示的图片资源,通过setClusterDrawable()实现。
	 * 将ClusterMarker转换成OverlayItem,并返回
	 * @param markerList
	 * @return
	 */
	public ArrayList<OverlayItem> createCluster(List<OverlayItem> markerList){
		this.mClusterMarkers.clear();
		ArrayList<OverlayItem> itemList = new ArrayList<OverlayItem>();
		for(int i=0;i<markerList.size();i++){
			addCluster(markerList.get(i));
		}
		for(int i=0;i<mClusterMarkers.size();i++){
			ClusterMarker cmarker = mClusterMarkers.get(i);
			setClusterDrawable(cmarker);
			OverlayItem oi = new OverlayItem(cmarker.getmCenter(),cmarker.getTitle(),cmarker.getSnippet());
			oi.setMarker(cmarker.getMarker());
			itemList.add(oi);
		}
		return itemList;
	}
	/**
	 * 聚合物添加、判断的逻辑和方法
	 * @param marker
	 */
	private void addCluster(OverlayItem marker){
		GeoPoint markGeo = marker.getPoint();
		//判断当前的聚合物列表中有没有聚合物,第一次肯定是没有的,根据标注点信息，生成一个ClusterMarker.
		if(mClusterMarkers.size()==0){ 
			ClusterMarker clusterMarker = new ClusterMarker(marker.getPoint(), marker.getTitle(), marker.getSnippet());
			clusterMarker.setMarker(marker.getMarker());
			//向ClusterMarker中添加一个聚合物,isAveragerCenter,表示每次有新的标注物进来,是否要重新计算中心点坐标，一般都是要重新计算的
			clusterMarker.AddMarker(marker, isAverageCenter);
			//每个覆盖物都有一定的覆盖范围,通过MBound(左下，右上两个顶点的经纬度坐标 )
			MBound bound = new MBound(markGeo.getLatitudeE6(),markGeo.getLongitudeE6(),markGeo.getLatitudeE6(),markGeo.getLongitudeE6());
			bound = MapUtils.getExtendedBounds(mMapView, bound, mGridSize);//扩大MBound的覆盖范围
			clusterMarker.setmGridBounds(bound);
			mClusterMarkers.add(clusterMarker);//将建好的聚合物添加到聚合物列表中 
		}else{
			//如果聚合物列表中已经存在聚合物，就要逐个判断当前聚合物列表中哪个最适合将当前的标注物聚合进去
			ClusterMarker clusterContain = null;
			double distance = mDistance;
			//遍历判断,标注点到列表中聚合物中心点的距,如果两个点的物理距离超过聚合距离,就不再到聚合列表中进行遍历判断
			for(int i=0;i<mClusterMarkers.size();i++){
				ClusterMarker clusterMarker = mClusterMarkers.get(i);
				GeoPoint center = clusterMarker.getmCenter();
				double d = DistanceUtil.getDistance(center, marker.getPoint());
				if(d<distance){//选择clusterMarker中最近的clusterMarker
					distance = d;
					clusterContain = clusterMarker;
				}
			}
			 //现存的clusterMarker 没有符合条件的,创建一个新的ClusterMarker 
			if(clusterContain == null||!isMarkersInCluster(markGeo, clusterContain.getmGridBounds())){
				ClusterMarker clusterMarker = new ClusterMarker(marker.getPoint(), marker.getTitle(), marker.getSnippet());
				clusterMarker.setMarker(marker.getMarker());
				clusterMarker.AddMarker(marker, isAverageCenter);
				MBound bound = new MBound(markGeo.getLatitudeE6(),markGeo.getLongitudeE6(),markGeo.getLatitudeE6(),markGeo.getLongitudeE6());				
				bound = MapUtils.getExtendedBounds(mMapView, bound, mGridSize);
				clusterMarker.setmGridBounds(bound);
				mClusterMarkers.add(clusterMarker);
			}else{
				clusterContain.AddMarker(marker, isAverageCenter);//创建ClusterMarker 成功，添加到聚合物列表中
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private void setClusterDrawable(ClusterMarker clusterMarker){
		View drawableView = LayoutInflater.from(context).inflate(
				R.layout.drawable_mark, null);
		TextView text = (TextView) drawableView.findViewById(R.id.drawble_mark);
		text.setPadding(3, 3, 3, 3);
		int markNum = clusterMarker.getmMarkers().size();
		if(markNum>=2){
			text.setText(markNum+"");
			if(markNum<11){
				text.setBackgroundResource(R.drawable.m0);
			}else if(markNum>10&&markNum<21){
				text.setBackgroundResource(R.drawable.m1);
			}else if(markNum>20&&markNum<31){
				text.setBackgroundResource(R.drawable.m2);
			}else if(markNum>30&&markNum<41){
				text.setBackgroundResource(R.drawable.m3);
			}else{
				text.setBackgroundResource(R.drawable.m4);
			}
			Bitmap bitmap = MapUtils.convertViewToBitmap(drawableView);
			clusterMarker.setMarker(new BitmapDrawable(bitmap));
		}else{
			clusterMarker.setMarker(context.getResources().getDrawable(R.drawable.nav_turn_via_1));
		}
	}
	/**
	 * 判断坐标点是否在MBound 覆盖区域内
	 * @param markerGeo
	 * @param bound
	 * @return
	 */
	private Boolean isMarkersInCluster(GeoPoint markerGeo,MBound bound){
		if(markerGeo.getLatitudeE6()>bound.getLeftBottomLat()
				&&markerGeo.getLatitudeE6()<bound.getRightTopLat()
				&&markerGeo.getLongitudeE6()>bound.getLeftBottomLng()
				&&markerGeo.getLongitudeE6()<bound.getRightTopLng()){
			return true;
		}
		return false;
	}
	
}
