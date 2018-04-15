package com.hand.stocktaking.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hand.stocktaking.R;

import java.util.ArrayList;
import java.util.List;

public class OutRcvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<OutBoxInfo> mBoxInfoList = new ArrayList<>();

    private static int ITEM_HEADER = 0;
    private static int ITEM_VIEW = 1;

    public void setList(List<OutBoxInfo> list){
        mBoxInfoList.clear();
        mBoxInfoList.addAll(list);
        notifyDataSetChanged();
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
        if (viewType == ITEM_HEADER){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_out_rcv_header, parent ,false);
            return new HeaderViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_out_rcv_item, parent, false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position != 0){
            OutBoxInfo info = mBoxInfoList.get(position - 1);
            MyViewHolder viewHolder = (MyViewHolder) holder;
            viewHolder.mRfidTv.setText(info.getRfid());
            viewHolder.mBoxTypeTv.setText(info.getBoxType());
        }
    }

    @Override
    public int getItemCount() {
        return mBoxInfoList.size() + 1;
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder{
        HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView mRfidTv;
        TextView mBoxTypeTv;
        MyViewHolder(View itemView) {
            super(itemView);
            mRfidTv = itemView.findViewById(R.id.out_tv_rfid);
            mBoxTypeTv = itemView.findViewById(R.id.out_tv_box_type);
        }
    }
}
