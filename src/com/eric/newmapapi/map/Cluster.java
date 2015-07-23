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
	
	public ArrayList<OverlayItem> createCluster(List<OverlayItem> markerList){
		this.mClusterMarkers.clear();
		ArrayList<OverlayItem> itemList = new ArrayList<OverlayItem>();
		for(int i=0;i<markerList.size();i++){
			addCluster(markerList.get(i));
		}
		for(int i=0;i<mClusterMarkers.size();i++){
			ClusterMarker cm = mClusterMarkers.get(i);
			setClusterDrawable(cm);
			OverlayItem oi = new OverlayItem(cm.getmCenter(),cm.getTitle(),cm.getSnippet());
			oi.setMarker(cm.getMarker());
			itemList.add(oi);
		}
		return itemList;
	}
	
	private void addCluster(OverlayItem marker){
		GeoPoint markGeo = marker.getPoint();
		if(mClusterMarkers.size()==0){
			ClusterMarker clusterMarker = new ClusterMarker(marker.getPoint(), marker.getTitle(), marker.getSnippet());
			clusterMarker.setMarker(marker.getMarker());
			clusterMarker.AddMarker(marker, isAverageCenter);
			MBound bound = new MBound(markGeo.getLatitudeE6(),markGeo.getLongitudeE6(),markGeo.getLatitudeE6(),markGeo.getLongitudeE6());
			bound = MapUtils.getExtendedBounds(mMapView, bound, mGridSize);
			clusterMarker.setmGridBounds(bound);
			mClusterMarkers.add(clusterMarker);
		}else{
			ClusterMarker clusterContain = null;
			double distance = mDistance;
			for(int i=0;i<mClusterMarkers.size();i++){
				ClusterMarker clusterMarker = mClusterMarkers.get(i);
				Log.e(TAG_ADD_Cluster, "in mClusterMarker.size  size = = "+mClusterMarkers.size());
				GeoPoint center = clusterMarker.getmCenter();
				double d = DistanceUtil.getDistance(center, marker.getPoint());
				
				if(d<distance){
					distance = d;
					clusterContain = clusterMarker;
				}else{
				}
			}
			if(clusterContain == null||!isMarkersInCluster(markGeo, clusterContain.getmGridBounds())){
				ClusterMarker clusterMarker = new ClusterMarker(marker.getPoint(), marker.getTitle(), marker.getSnippet());
				clusterMarker.setMarker(marker.getMarker());
				clusterMarker.AddMarker(marker, isAverageCenter);
				MBound bound = new MBound(markGeo.getLatitudeE6(),markGeo.getLongitudeE6(),markGeo.getLatitudeE6(),markGeo.getLongitudeE6());				
				bound = MapUtils.getExtendedBounds(mMapView, bound, mGridSize);
				clusterMarker.setmGridBounds(bound);
				mClusterMarkers.add(clusterMarker);
			}else{
				clusterContain.AddMarker(marker, isAverageCenter);
				Log.e(TAG_ADD_Cluster, "添加到选中 clusterMarker:--->clusterContain.size:---->"+clusterContain.getmMarkers().size());
			}
		}
	}
	
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
