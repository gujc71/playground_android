package com.gujc.playground9.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface  RetrofitAPI {
    //public String SERVER_URL ="http://192.168.35.154:3000/";
    public String SERVER_URL ="http://playground.cafe24app.com/";
    @GET("/show/myTownMapService")      // 우리동네
    Call<JsonArray> getMyTownMap(@Query("lat") double lat, @Query("lng") double lng);

    @GET("/show/courseListService")     // 추천코스
    Call<JsonArray> getCourseList();

    @GET("/show/courseDetailMService")     // 추천코스 상세 Master
    Call<JsonElement> getCourseDetailM(@Query("cmno") String cmno);

    @GET("/show/courseDetailDService")     // 추천코스 상세 detail
    Call<JsonArray> getCourseDetailD(@Query("cmno") String cmno);

}
