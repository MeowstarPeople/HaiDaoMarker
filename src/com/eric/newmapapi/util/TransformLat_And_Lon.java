package com.eric.newmapapi.util;

import android.util.Log;

public class TransformLat_And_Lon {
	final static double pi = 3.14159265358979324;
	final static double a = 6378245.0;
	final static double ee = 0.00669342162296594323;
	final static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
	public static double ggLat;
	public static double ggLon;
	public static double bd_lon;
	public static double bd_lat;
	

	/**
	 * GPS转google坐标进行转换,google坐标转百度
	 * @param wgLat latitude纬度
	 * @param wgLon longitude经度
	 */
	public static LatLon transform(double wgLat, double wgLon)
	{
//		if (outOfChina(wgLat, wgLon))
//		{
//			mgLat = wgLat;
//			mgLon = wgLon;
//		}
		double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
		double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
		double radLat = wgLat / 180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
		dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
		ggLat = wgLat + dLat;
		ggLon = wgLon + dLon;
		transfromToBD(ggLat,ggLon);
		LatLon latlon=new LatLon();
		latlon.setLatitude(bd_lat);
		latlon.setLongitude(bd_lon);
		return latlon;
	}
	/**
	 * 判断是否超出中国边界
	 * @param lat
	 * @param lon
	 * @return
	 */
	public boolean outOfChina(double lat, double lon)
	{
		if (lon < 72.004 || lon > 137.8347)
			return true;
		if (lat < 0.8293 || lat > 55.8271)
			return true;
		return false;
	}
	/**
	 * gps坐标转谷歌坐标————
	 * @param x
	 * @param y
	 * @return
	 */
	public static double transformLat(double x, double y)
	{
		double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
		return ret;
	}
	/**
	 * gps坐标转谷歌坐标————
	 * @param x
	 * @param y
	 * @return
	 */
    public static double transformLon(double x, double y)
    {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }
    /**
     * 谷歌坐标转百度坐标
     * @param gg_lat
     * @param gg_lon
     */
   public static void transfromToBD(double gg_lat, double gg_lon)
    {
        double x = gg_lon, y = gg_lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        bd_lon = z * Math.cos(theta) + 0.0065;
        bd_lat = z * Math.sin(theta) + 0.006;
        Log.d("TAG", bd_lon+"---------"+bd_lat);
    }
}
