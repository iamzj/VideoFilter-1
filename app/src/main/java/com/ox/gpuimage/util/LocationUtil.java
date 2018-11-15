package com.ox.gpuimage.util;

import android.location.Location;
import android.text.TextUtils;

/**
 * Created by winn on 17/4/15.
 */

public class LocationUtil {
    /**
     * location 的格式是类似于-90.0000+180.0000
     * @param location
     * @return 如果返回值是空的说明经纬度不存在 结果是一个String数组  第一个是纬度 第二个是经度
     */
    public static String[] parseLocation(String location){
        if(TextUtils.isEmpty(location)){
            return null;
        } else {
            // TODO: 2018/11/15 location中存在非法字符时将抛出异常 
            try {
                double longitude = 0.0;
                double latitude = 0.0;
                int index1 = location.lastIndexOf("-");
                int index2 = location.lastIndexOf("+");
                if(index1 != 0 && index1 != -1){
                    latitude = Double.valueOf(location.substring(0, index1));
                    longitude = Double.valueOf(location.substring(index1, location.length()));
                } else if(index2 != 0 && index2 != -1){
                    latitude = Double.valueOf(location.substring(0, index2));
                    longitude = Double.valueOf(location.substring(index2, location.length()));
                } else {
                    return null;
                }
                String[] result = new String[2];
                //纬度
                result[0] = Location.convert(latitude, Location.FORMAT_DEGREES);
                //经度
                result[1] = Location.convert(longitude, Location.FORMAT_DEGREES);
                return result;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
