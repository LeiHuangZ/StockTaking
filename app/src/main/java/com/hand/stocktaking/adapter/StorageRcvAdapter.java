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

public class StorageRcvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<StorageBoxInfo> mBoxInfoList = new ArrayList<>();
    private Context mContext;
    private boolean mIsCompare;

    private static int ITEM_HEADER = 0;
    private static int ITEM_VIEW = 1;

    public void setList(List<StorageBoxInfo> list, boolean isCompare){
        mIsCompare = isCompare;
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_storage_rcv_header, parent ,false);
            return new HeaderViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_storage_rcv_item, parent, false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position != 0){
            StorageBoxInfo info = mBoxInfoList.get(position - 1);
            MyViewHolder viewHolder = (MyViewHolder) holder;
            if (mIsCompare && !info.isResolved()){
                viewHolder.mLinearLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            }else {
                viewHolder.mLinearLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorDefault));
            }
            viewHolder.mRfidTv.setText(info.getRfid());
            viewHolder.mBuyCompanyTv.setText(info.getBuyCompany());
            viewHolder.mBoxTypeTv.setText(info.getBoxType());
            viewHolder.mBoxPriceTv.setText(info.getBoxPrice());
            viewHolder.mBoxNumTv.setText(info.getBoxNum());
            viewHolder.mPreTimeTv.setText(info.getPreTime());
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
        TextView mRfidTv;
        TextView mBuyCompanyTv;
        TextView mBoxTypeTv;
        TextView mBoxPriceTv;
        TextView mBoxNumTv;
        TextView mPreTimeTv;
        MyViewHolder(View itemView) {
            super(itemView);
            mLinearLayout = itemView.findViewById(R.id.storage_item_view);
            mRfidTv = itemView.findViewById(R.id.storage_tv_rfid);
            mBuyCompanyTv = itemView.findViewById(R.id.storage_tv_buy_company);
            mBoxTypeTv = itemView.findViewById(R.id.storage_tv_box_type);
            mBoxPriceTv = itemView.findViewById(R.id.storage_tv_box_price);
            mBoxNumTv = itemView.findViewById(R.id.storage_tv_box_num);
            mPreTimeTv = itemView.findViewById(R.id.storage_tv_pre_time);
        }
    }
}
