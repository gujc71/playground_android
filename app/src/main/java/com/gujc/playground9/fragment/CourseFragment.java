package com.gujc.playground9.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.gujc.playground9.R;
import com.gujc.playground9.common.CourseMstVO;
import com.gujc.playground9.common.RetrofitAPI;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class CourseFragment extends Fragment {

    public CourseFragment() {
    }

    public static CourseFragment newInstance() {
        CourseFragment fragment = new CourseFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_course, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager( new LinearLayoutManager((inflater.getContext())));
        //recyclerView.setLayoutManager( new GridLayoutManager((inflater.getContext()), 3));
        recyclerView.setAdapter(new RecyclerViewAdapter());

        return view;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        final private RequestOptions requestOptions = new RequestOptions().transforms(new CenterCrop());
        private ArrayList<CourseMstVO> courseList = new ArrayList<CourseMstVO>();
        ProgressDialog loadingDialog;

        public RecyclerViewAdapter() {
            loadingDialog = ProgressDialog.show(getActivity(), "", getString(R.string.msg_wait), true);

            Retrofit mRetrofit = new Retrofit.Builder()
                    .baseUrl(RetrofitAPI.SERVER_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            RetrofitAPI mRetrofitAPI  = mRetrofit.create(RetrofitAPI.class);
            Call<JsonArray> mCallMoviewList = mRetrofitAPI.getCourseList();

            mCallMoviewList.enqueue(mRetrofitCallback);
        }

        private Callback<JsonArray> mRetrofitCallback = new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                JsonArray result = response.body();
                Gson gson = new Gson();
                for(JsonElement je: result){
                    CourseMstVO clInfo = gson.fromJson(je, CourseMstVO.class);
                    courseList.add(clInfo);
                }
                notifyDataSetChanged();
                loadingDialog.dismiss();
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                loadingDialog.dismiss();
                Toast.makeText(getContext(), R.string.msg_networkError, Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        };

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final CourseMstVO courseInfo = courseList.get(position);
            CustomViewHolder customViewHolder = (CustomViewHolder) holder;
            customViewHolder.course_title.setText(courseInfo.cmtitle);

            if (courseInfo.cmimage==null) {
                Glide.with(getActivity()).load(R.drawable.noimage2)
                        .apply(requestOptions)
                        .into(customViewHolder.course_photo);
            } else{
                Glide.with(getActivity())
                        .load(RetrofitAPI.SERVER_URL + "common/getimage/"+courseInfo.cmimage)
                        .apply(requestOptions)
                        .into(customViewHolder.course_photo);
            }

            holder.itemView.setOnClickListener( new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    CourseDetailFragment fragment = CourseDetailFragment.newInstance(courseInfo.cmno);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.mainFragment, fragment).addToBackStack(null).commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return courseList.size();
        }
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
