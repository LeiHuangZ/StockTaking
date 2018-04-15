package com.hand.stocktaking.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hand.stocktaking.R;

import java.util.ArrayList;
import java.util.List;

public class CleanRcvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<CleanBoxInfo> mBoxInfoList = new ArrayList<>();
    private static int ITEM_HEADER = 0;
    private static int ITEM_VIEW = 1;

    private Context mContext;

    public void setList(List<CleanBoxInfo> list){
        mBoxInfoList.clear();
        mBoxInfoList.addAll(list);
        notifyDataSetChanged();
    }

    public List<CleanBoxInfo> getBoxInfoList() {
        return mBoxInfoList;
    }



    @Override
    public int getItemViewType(int position) {
        if (position== 0){
            return ITEM_HEADER;
        }else {
            return ITEM_VIEW;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        if (viewType == ITEM_HEADER){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_clean_rcv_header, parent ,false);
            return new HeaderViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_clean_rcv_item, parent, false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (position != 0){
            final CleanBoxInfo info = mBoxInfoList.get(position - 1);
            MyViewHolder viewHolder = (MyViewHolder) holder;
            viewHolder.mSignTv.setText(info.getSign());
            viewHolder.mTagsTv.setText(info.getTags());
            viewHolder.mCleanerTv.setText(info.getCleaner());
            viewHolder.mCleanTimeTv.setText(info.getCleanTime());
        }
    }

    @Override
    public int getItemCount() {
        Log.e("Huang", "getItemCount: " + mBoxInfoList.size());
        return mBoxInfoList.size() + 1;
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder{
        HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        LinearLayout mLinearLayout;
        TextView mTagsTv;
        TextView mSignTv;
        TextView mCleanerTv;
        TextView mCleanTimeTv;
        MyViewHolder(View itemView) {
            super(itemView);
            mLinearLayout = itemView.findViewById(R.id.clean_item_view);
            mTagsTv = itemView.findViewById(R.id.clean_rv_tv_rfid);
            mSignTv = itemView.findViewById(R.id.clean_rv_tv_box_type);
            mCleanerTv = itemView.findViewById(R.id.clean_rv_tv_clean_id);
            mCleanTimeTv = itemView.findViewById(R.id.clean_rv_tv_clean_time);
        }
    }
}
