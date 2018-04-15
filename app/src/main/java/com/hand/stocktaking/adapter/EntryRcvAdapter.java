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

public class EntryRcvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<EntryBoxInfo> mBoxInfoList = new ArrayList<>();
    private static int ITEM_HEADER = 0;
    private static int ITEM_VIEW = 1;

    private Context mContext;

    public void setList(List<EntryBoxInfo> list){
        mBoxInfoList.clear();
        mBoxInfoList.addAll(list);
        notifyDataSetChanged();
    }

    public List<EntryBoxInfo> getBoxInfoList() {
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_entry_rcv_header, parent ,false);
            return new HeaderViewHolder(view);
        }else{
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_entry_rcv_item, parent, false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (position != 0){
            final EntryBoxInfo info = mBoxInfoList.get(position - 1);
            MyViewHolder viewHolder = (MyViewHolder) holder;
            viewHolder.mItemCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ArrayList<EntryBoxInfo> entryBoxInfos = new ArrayList<>();
                    for (int i = 0; i < mBoxInfoList.size(); i++) {
                        EntryBoxInfo boxInfo = mBoxInfoList.get(i);
                        if (i == position -1) {
                            boxInfo.setIsChecked(isChecked);
                        }
                        entryBoxInfos.add(boxInfo);
                    }
                    mBoxInfoList.clear();
                    mBoxInfoList.addAll(entryBoxInfos);
                }
            });
            viewHolder.mSignTv.setText(info.getSign());
            viewHolder.mTagsTv.setText(info.getTags());
            viewHolder.mMattertypeTv.setText(info.getMattertype());
            viewHolder.mMatternameTv.setText(info.getMattername());
            viewHolder.mMatternumTv.setText(info.getMatternum());
            viewHolder.mItemCb.setChecked(info.getIsChecked());
        }else {
            HeaderViewHolder holder1 = (HeaderViewHolder) holder;
            holder1.mHeaderCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ArrayList<EntryBoxInfo> entryBoxInfos = new ArrayList<>();
                    for (int i = 0; i < mBoxInfoList.size(); i++) {
                        EntryBoxInfo boxInfo = mBoxInfoList.get(i);
                        boxInfo.setIsChecked(isChecked);
                        entryBoxInfos.add(boxInfo);
                    }
                    mBoxInfoList.clear();
                    mBoxInfoList.addAll(entryBoxInfos);
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        Log.e("Huang", "getItemCount: " + mBoxInfoList.size());
        return mBoxInfoList.size() + 1;
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder{
        CheckBox mHeaderCb;
        HeaderViewHolder(View itemView) {
            super(itemView);
            mHeaderCb = itemView.findViewById(R.id.entry_title_cb);
        }
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        LinearLayout mLinearLayout;
        CheckBox mItemCb;
        TextView mTagsTv;
        TextView mSignTv;
        TextView mMattertypeTv;
        TextView mMatternumTv;
        TextView mMatternameTv;
        MyViewHolder(View itemView) {
            super(itemView);
            mItemCb = itemView.findViewById(R.id.entry_item_cb);
            mLinearLayout = itemView.findViewById(R.id.entry_item_view);
            mTagsTv = itemView.findViewById(R.id.entry_rv_tv_rfid);
            mSignTv = itemView.findViewById(R.id.entry_rv_tv_box_type);
            mMattertypeTv = itemView.findViewById(R.id.entry_rv_tv_material_type);
            mMatternumTv = itemView.findViewById(R.id.entry_rv_tv_material_num);
            mMatternameTv = itemView.findViewById(R.id.entry_rv_tv_material_name);
        }
    }
}
