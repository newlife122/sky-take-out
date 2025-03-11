package com.sky.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.properties.BaiDuProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author raoxin
 */
@Component
public class BaiDuLocationUtil {

    @Autowired
    private BaiDuProperties baiDuProperties;

    @Autowired
    private CacheManager cacheManager;



    public BaiDuLocationUtil(){

    }

    @PostConstruct
    public void clearLocalLocation(){
        cacheManager.getCache("location").clear();
    }

    public  Location getLocation(String address,String city) {
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("address",address);
        paramMap.put("output","json");
        paramMap.put("ak",baiDuProperties.getAk());
        if (!"".equals(city)){
            paramMap.put("city",city);
        }
        String string = HttpClientUtil.doGet(baiDuProperties.getLocationUrl(), paramMap);
        JSONObject jsonObject = JSONObject.parseObject(string);

        if (!(jsonObject.getInteger("status") == 0)){
            throw new RuntimeException("获取地址失败");
        }
        JSONObject result = jsonObject.getJSONObject("result");
        JSONObject location = result.getJSONObject("location");
        double lat = location.getDouble("lat");
        double lng = location.getDouble("lng");
        return new Location(lat,lng);
    }

    public  Integer getDistance(Location source,Location destination) {
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("ak",baiDuProperties.getAk());
        paramMap.put("output","json");
        paramMap.put("origin",source.lat+","+source.lng);
        paramMap.put("destination",destination.lat+","+destination.lng);
        String string = HttpClientUtil.doGet(baiDuProperties.getRouteUrl(), paramMap);
        JSONObject jsonObject = JSONObject.parseObject(string);

        if (!(jsonObject.getInteger("status") == 0)){
            throw new RuntimeException("获取地址失败");
        }

        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = result.getJSONArray("routes");
        Integer distance = null;
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject route = jsonArray.getJSONObject(i);
            if (distance == null){
                distance = route.getInteger("distance");
            }else {
                distance = Math.max(distance,route.getInteger("distance"));
            }
        }
        return distance;

    }

    // TODO 变更地址的时候清除缓存
    @Cacheable(cacheNames = "location",key = "#root.methodName")
    public Location getLocalLocation() {
        Map<String,String> paramMap = new HashMap<>();
        String address = baiDuProperties.getLocation();
        paramMap.put("address",address);
        paramMap.put("output","json");
        paramMap.put("ak",baiDuProperties.getAk());
        String string = HttpClientUtil.doGet(baiDuProperties.getLocationUrl(), paramMap);
        JSONObject jsonObject = JSONObject.parseObject(string);
        if (!(jsonObject.getInteger("status") == 0)){
            throw new RuntimeException("获取地址失败");
        }

        JSONObject result = jsonObject.getJSONObject("result");
        JSONObject location = result.getJSONObject("location");
        double lat = location.getDouble("lat");
        double lng = location.getDouble("lng");
        return new Location(lat,lng);
    }


    public static class Location implements Serializable {
        private double lat;
        private double lng;

        public Location(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }
    }
}
