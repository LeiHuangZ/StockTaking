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

public class ReceiveRcvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ReceiveBoxInfo> mBoxInfoList = new ArrayList<>();

    private static int ITEM_HEADER = 0;
    private static int ITEM_VIEW = 1;

    private Context mContext;

    public void setList(List<ReceiveBoxInfo> list){
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
        mContext = parent.getContext();
        if (viewType == ITEM_HEADER){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_receive_rcv_header, parent ,false);
            return new HeaderViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_receive_rcv_item, parent, false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position != 0){
            ReceiveBoxInfo info = mBoxInfoList.get(position - 1);
            MyViewHolder viewHolder = (MyViewHolder) holder;
            viewHolder.mRfidTv.setText(info.getRfid());
            viewHolder.mBoxTypeTv.setText(info.getBoxType());
            viewHolder.mSendCompanyTv.setText(info.getSendCompany());
            viewHolder.mReceiveCompanyTv.setText(info.getReceiveCompany());
            viewHolder.mSendTimeTv.setText(info.getSendTime());
            viewHolder.mOperation.setText(info.getOperation());
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
        TextView mRfidTv;
        TextView mBoxTypeTv;
        TextView mSendCompanyTv;
        TextView mReceiveCompanyTv;
        TextView mSendTimeTv;
        TextView mOperation;
        MyViewHolder(View itemView) {
            super(itemView);
            mRfidTv = itemView.findViewById(R.id.receive_rv_tv_rfid);
            mBoxTypeTv = itemView.findViewById(R.id.receive_rv_tv_box_type);
            mSendCompanyTv = itemView.findViewById(R.id.receive_rv_tv_send_company);
            mReceiveCompanyTv = itemView.findViewById(R.id.receive_rv_tv_receive_company);
            mSendTimeTv = itemView.findViewById(R.id.receive_rv_tv_send_time);
            mOperation = itemView.findViewById(R.id.receive_rv_tv_operation);
        }
    }
}
