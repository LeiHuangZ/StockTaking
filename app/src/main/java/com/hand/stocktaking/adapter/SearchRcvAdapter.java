package com.hand.stocktaking.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
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

public class SearchRcvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SearchBoxInfo> mBoxInfoList = new ArrayList<>();
    private static int ITEM_HEADER = 0;
    private static int ITEM_VIEW = 1;

    private Context mContext;

    public void setList(List<SearchBoxInfo> list){
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_search_rcv_header, parent ,false);
            return new HeaderViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_search_rcv_item, parent, false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (position != 0){
            final SearchBoxInfo info = mBoxInfoList.get(position - 1);
            MyViewHolder viewHolder = (MyViewHolder) holder;
            viewHolder.mSignTv.setText(info.getSign());
            viewHolder.mTagsTv.setText(info.getTags());
            viewHolder.mMattertypeTv.setText(info.getMattertype());
            viewHolder.mMatternameTv.setText(info.getMattername());
            viewHolder.mMatternumTv.setText(info.getMatternum());
            if (info.getIsMatch()){
                viewHolder.mLinearLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            }else {
                viewHolder.mLinearLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorDefault));
            }
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
        TextView mMattertypeTv;
        TextView mMatternumTv;
        TextView mMatternameTv;
        MyViewHolder(View itemView) {
            super(itemView);
            mLinearLayout = itemView.findViewById(R.id.search_item_view);
            mTagsTv = itemView.findViewById(R.id.search_rv_tv_rfid);
            mSignTv = itemView.findViewById(R.id.search_rv_tv_box_type);
            mMattertypeTv = itemView.findViewById(R.id.search_rv_tv_material_type);
            mMatternumTv = itemView.findViewById(R.id.search_rv_tv_material_num);
            mMatternameTv = itemView.findViewById(R.id.search_rv_tv_material_name);
        }
    }
}
