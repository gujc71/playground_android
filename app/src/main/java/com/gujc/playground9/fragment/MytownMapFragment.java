package com.gujc.playground9.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.gujc.playground9.R;
import com.gujc.playground9.common.MapVO;
import com.gujc.playground9.common.RetrofitAPI;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MytownMapFragment extends Fragment {

    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";
    private static final String ARG_LIST = "list";

    private ArrayList<MapVO> mapList = new ArrayList<MapVO>();
    private ProgressDialog loadingDialog;

    private MapView mapView;
    private LinearLayout infoView;      // show for a item of mapList
    private TextView pgname;            // show for a item of mapList
    private TextView pgaddr;            // show for a item of mapList
    private TextView pgtype;            // show for a item of mapList
    private TextView homepageBtn;      // show for a item of mapList
    private TextView linkBtn;           // show for a item of mapList

    public MytownMapFragment() {
        // Required empty public constructor
    }

    public static MytownMapFragment newInstance(double latitude, double longitude) {
        MytownMapFragment fragment = new MytownMapFragment();

        Bundle args = new Bundle();
        args.putDouble(ARG_LATITUDE, latitude);
        args.putDouble(ARG_LONGITUDE, longitude);
        fragment.setArguments(args);

        return fragment;
    }

    public static MytownMapFragment newInstance(ArrayList<MapVO> list) {
        MytownMapFragment fragment = new MytownMapFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_LIST, list);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mytownmap, container, false);

        infoView = view.findViewById(R.id.infoView);
        pgname = view.findViewById(R.id.pgname);
        pgaddr = view.findViewById(R.id.pgaddr);
        pgtype = view.findViewById(R.id.pgtype);
        homepageBtn = view.findViewById(R.id.homepageBtn);
        linkBtn = view.findViewById(R.id.linkBtn);

        /*FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });*/
        /////////////////////////////////////////////////////////////////
        double latitude = 37.57590409118165;
        double longitude = 126.97684292625047;

        Bundle args = getArguments();

        ArrayList<MapVO> argList = (ArrayList<MapVO>) args.getSerializable(ARG_LIST);
        if (argList!=null){//.size() > 0) {
            mapList = argList;
            MapVO mapInfo = mapList.get(0);
            latitude = mapInfo.pglat;
            longitude= mapInfo.pglon;
            setMapView((ViewGroup) view.findViewById(R.id.map_view), MapPoint.mapPointWithGeoCoord(latitude, longitude) );
            SetMapPoints();
            return view;
        }

        latitude = args.getDouble(ARG_LATITUDE);
        longitude = args.getDouble(ARG_LONGITUDE);

        setMapView((ViewGroup) view.findViewById(R.id.map_view), MapPoint.mapPointWithGeoCoord(latitude, longitude) );

        // //////////////////////////////////////////

        loadingDialog = ProgressDialog.show(getActivity(), "", getString(R.string.msg_wait), true);
        Retrofit mRetrofit = new Retrofit.Builder()
                .baseUrl(RetrofitAPI.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI  mRetrofitAPI  = mRetrofit.create(RetrofitAPI.class);
        Call<JsonArray> list = mRetrofitAPI.getMyTownMap(latitude, longitude);

        list.enqueue(mRetrofitCallback);

        return view;
    }

    private void setMapView(ViewGroup mapViewContainer, MapPoint centerPoint) {
        mapView = new MapView(getActivity());

        mapViewContainer.addView(mapView);
        mapView.setMapViewEventListener(mMapViewEventListener);
        //mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());
        mapView.setPOIItemEventListener(mPOIItemEventListener);
        mapView.setZoomLevel(4, true);
        mapView.setMapCenterPoint(centerPoint, true);
    }

    private Callback<JsonArray> mRetrofitCallback = new Callback<JsonArray>() {
        @Override
        public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
            JsonArray result = response.body();
            Gson gson = new Gson();
            for(JsonElement je: result){
                MapVO mapInfo = gson.fromJson(je, MapVO.class);
                mapList.add(mapInfo);
            }
            SetMapPoints();
            loadingDialog.dismiss();
        }

        @Override
        public void onFailure(Call<JsonArray> call, Throwable t) {
            loadingDialog.dismiss();
            Toast.makeText(getContext(), R.string.msg_networkError, Toast.LENGTH_LONG).show();
            t.printStackTrace();
        }
    };

    private void SetMapPoints() {
        int markerType=0;
        boolean markerType2 = mapList.size() > 30;

        for(int i=0; i< mapList.size(); i++){
            MapVO mapInfo = mapList.get(i);

            String a = mapInfo.pgno;

            MapPoint mp = MapPoint.mapPointWithGeoCoord(mapInfo.pglat, mapInfo.pglon);
            MapPOIItem  marker = new MapPOIItem();
            marker.setItemName(a);
            marker.setTag(i);
            marker.setMapPoint(mp);
            marker.setShowCalloutBalloonOnTouch(false);

            if (markerType2) {
                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);

                markerType = R.drawable.marker_z;
                switch (mapInfo.pgtype1) {
                    case "A":  markerType = R.drawable.marker_a; break;
                    case "B":  markerType = R.drawable.marker_b; break;
                    case "C":  markerType = R.drawable.marker_c; break;
                    case "D":  markerType = R.drawable.marker_d; break;
                    case "E":  markerType = R.drawable.marker_e;
                }
                marker.setCustomImageResourceId(markerType);
            } else {
                marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
                marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
            }
            mapView.addPOIItem(marker);
        }
    }

    // ////////////////////////////////////////////
    /*/ CalloutBalloonAdapter 인터페이스 구현
    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            ((ImageView) mCalloutBalloon.findViewById(R.id.badge)).setImageResource(R.mipmap.ic_launcher);
            ((TextView) mCalloutBalloon.findViewById(R.id.title)).setText(poiItem.getItemName());
            ((TextView) mCalloutBalloon.findViewById(R.id.desc)).setText("Custom CalloutBalloon");
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }
    }*/
    // ////////////////////////////////////////////
    MapView.MapViewEventListener mMapViewEventListener = new MapView.MapViewEventListener() {

        @Override
        public void onMapViewInitialized(MapView mapView) {}

        @Override
        public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {}

        @Override
        public void onMapViewZoomLevelChanged(MapView mapView, int i) {}

        @Override
        public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
            infoView.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {}

        @Override
        public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {}

        @Override
        public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {}

        @Override
        public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {}

        @Override
        public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {}
    };
    //////////////////////////////////////////////
    MapView.POIItemEventListener mPOIItemEventListener = new MapView.POIItemEventListener() {
        private MapVO mapInfo = null;
        @Override
        public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
            //Toast.makeText(getContext(), "Clicked " + mapPOIItem.getItemName() + " onPOIItemSelected", Toast.LENGTH_SHORT).show();
            infoView.setVisibility(View.VISIBLE);
            mapInfo = mapList.get(mapPOIItem.getTag());
            if (mapInfo==null) return;
            pgname.setText(mapInfo.pgname);
            pgaddr.setText(mapInfo.pgaddr);

            String markerType="";
            switch (mapInfo.pgtype1) {
                case "A":  markerType = "실내 놀이터"; break;
                case "B":  markerType = "실외 놀이터"; break;
                case "C":  markerType = "박물관/미술관"; break;
                case "D":  markerType = "도서관"; break;
                case "E":  markerType = "분수"; break;
            }
            if (!"".equals(mapInfo.pgtype2nm) & (mapInfo.pgtype2nm !=null))  markerType += "[" + mapInfo.pgtype2nm +"]";
            pgtype.setText(markerType);

            if ("".equals(mapInfo.pgurl) || mapInfo.pgurl==null) {
                homepageBtn.setVisibility(View.GONE);
            } else {
                homepageBtn.setVisibility(View.VISIBLE);
                homepageBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(mapInfo.pgurl));
                        startActivity(intent);
                    }
                });
            }
            linkBtn.setVisibility(View.VISIBLE);
            linkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://map.daum.net/link/to/"+ mapInfo.pgname + "," + mapInfo.pglat + "," + mapInfo.pglon));
                    startActivity(intent);
                }
            });
        }

        @Override
        public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {}

        @Override
        public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {}

        @Override
        public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {}
    };

}
