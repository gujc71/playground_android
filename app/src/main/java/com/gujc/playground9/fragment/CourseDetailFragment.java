package com.gujc.playground9.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.gujc.playground9.R;
import com.gujc.playground9.common.CourseMstVO;
import com.gujc.playground9.common.MapVO;
import com.gujc.playground9.common.RetrofitAPI;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class CourseDetailFragment extends Fragment {
    private static final String ARG_CMNO = "cmno";

    private String cmno;
    private ArrayList<MapVO> courseList = new ArrayList<MapVO>();

    private TextView cmdesc;
    private TextView cmtitle;
    private RecyclerView recyclerView;

    private LinearLayout infoView;      // show for a item of mapList
    private TextView pgname;            // show for a item of mapList
    private TextView pgaddr;            // show for a item of mapList
    private TextView pgtype;            // show for a item of mapList
    private TextView homepageBtn;      // show for a item of mapList
    private TextView linkBtn;           // show for a item of mapList

    public CourseDetailFragment() {
    }

    public static CourseDetailFragment newInstance(String cmno) {
        CourseDetailFragment fragment = new CourseDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CMNO, cmno);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            return;
        }

        cmno = getArguments().getString(ARG_CMNO);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_detail, container, false);
        cmtitle = view.findViewById(R.id.cmtitle);
        cmdesc = view.findViewById(R.id.cmdesc);
        //cmdesc.setHorizontalScrollBarEnabled(false);
        cmdesc.setMovementMethod(LinkMovementMethod.getInstance());

        infoView = view.findViewById(R.id.infoView);
        pgname = view.findViewById(R.id.pgname);
        pgaddr = view.findViewById(R.id.pgaddr);
        pgtype = view.findViewById(R.id.pgtype);
        homepageBtn = view.findViewById(R.id.homepageBtn);
        linkBtn = view.findViewById(R.id.linkBtn);

        if (cmno == null) {
            return view;
        }

        Retrofit mRetrofit = new Retrofit.Builder()
                .baseUrl(RetrofitAPI.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitAPI mRetrofitAPI  = mRetrofit.create(RetrofitAPI.class);

        Call<JsonElement> mst = mRetrofitAPI.getCourseDetailM(cmno);
        mst.enqueue(mstRetrofitCallback);

        Call<JsonArray> list = mRetrofitAPI.getCourseDetailD(cmno);
        list.enqueue(listRetrofitCallback);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager( new GridLayoutManager((inflater.getContext()), 5));
        recyclerView.setAdapter(new RecyclerViewAdapter());

        return view;
    }

    private Callback<JsonElement> mstRetrofitCallback = new Callback<JsonElement>() {
        @Override
        public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
            JsonElement result = response.body();
            CourseMstVO courseInfo = new Gson().fromJson(result, CourseMstVO.class);
            cmtitle.setText(courseInfo.cmtitle);
            cmdesc.setText(Html.fromHtml(courseInfo.cmdesc));
            //cmdesc.loadData(courseInfo.cmdesc, "text/html", "UTF-8");
        }

        @Override
        public void onFailure(Call<JsonElement> call, Throwable t) {
            t.printStackTrace();
        }
    };

    private Callback<JsonArray> listRetrofitCallback = new Callback<JsonArray>() {
        @Override
        public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
            JsonArray result = response.body();
            Gson gson = new Gson();
            for(JsonElement je: result){
                MapVO clInfo = gson.fromJson(je, MapVO.class);
                courseList.add(clInfo);
            }
            recyclerView.getAdapter().notifyDataSetChanged();
            MytownMapFragment fragment = MytownMapFragment.newInstance(courseList);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mapFragment, fragment)
                    .commit();
        }

        @Override
        public void onFailure(Call<JsonArray> call, Throwable t) {
            t.printStackTrace();
        }
    };

    ////////////////////////////////////////////////////

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        final private RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop());

        public RecyclerViewAdapter() {
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_path, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final MapVO courseInfo = courseList.get(position);
            CustomViewHolder customViewHolder = (CustomViewHolder) holder;
            customViewHolder.course_title.setText(courseInfo.pgname);

            int markerType = R.drawable.icon_z;
            switch (courseInfo.pgtype1) {
                case "A":  markerType = R.drawable.icon_a; break;
                case "B":  markerType = R.drawable.icon_b; break;
                case "C":  markerType = R.drawable.icon_c; break;
                case "D":  markerType = R.drawable.icon_d; break;
                case "E":  markerType = R.drawable.icon_e; 
            }
            
            Glide.with(getActivity()).load(markerType)
                    .apply(requestOptions)
                    .into(customViewHolder.course_photo);

            holder.itemView.setOnClickListener( new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    showMapiNFO(courseInfo);
                }
            });
        }

        @Override
        public int getItemCount() {
            return courseList.size();
        }
    }

    public void showMapiNFO(final MapVO mapInfo) {
        infoView.setVisibility(View.VISIBLE);
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
    private class CustomViewHolder extends RecyclerView.ViewHolder {
        public ImageView course_photo;
        public TextView course_title;

        public CustomViewHolder(View view) {
            super(view);
            course_photo = view.findViewById(R.id.course_photo);
            course_title = view.findViewById(R.id.course_title);
        }
    }
}
