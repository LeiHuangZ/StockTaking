package com.hand.stocktaking.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.BRMicro.Tools;
import com.hand.stocktaking.R;
import com.hand.stocktaking.adapter.EntryBoxInfo;
import com.hand.stocktaking.adapter.EntryRcvAdapter;
import com.hand.stocktaking.retrofit.MatterBean;
import com.hand.stocktaking.retrofit.RetrofitRequestHelper;
import com.hand.stocktaking.utils.LogUtils;
import com.hand.stocktaking.utils.SPUtils;
import com.hand.stocktaking.utils.SoundUtil;
import com.hand.stocktaking.utils.ToastUtils;
import com.uhf.api.cls.Reader;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class EntryActivity extends BaseActivity {


    @BindView(R.id.entry_btn_scan)
    Button mEntryBtnScan;
    @BindView(R.id.entry_btn_entry)
    Button mEntryBtnEntry;
    @BindView(R.id.entry_btn_search)
    Button mEntryBtnSearch;
    @BindView(R.id.entry_tv_count)
    TextView mEntryTvCount;
    @BindView(R.id.entry_rv)
    RecyclerView mEntryRv;
    @BindView(R.id.entry_tv_sum)
    TextView mEntryTvSum;
    private String mMattertype;
    private String mMattername;
    private String mMatternum;

    /**
     * 按键广播接收者 用于接受按键广播 触发扫描
     */
    private class MyKeyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int keyCode = intent.getIntExtra("keyCode", 0);
            // 为兼容早期版本机器
            if (keyCode == 0) {
                keyCode = intent.getIntExtra("keycode", 0);
            }
            boolean keyDown = intent.getBooleanExtra("keydown", false);
            if (!keyDown) {
                // 根据需要在对应的按键的键值中开启扫描,
                switch (keyCode) {
                    case KeyEvent.KEYCODE_F3:
                    case KeyEvent.KEYCODE_F4:
                        // uhf扫描
                        runInventory();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private MyKeyReceiver keyReceiver = new MyKeyReceiver();
    /**
     * 盘存EPC的子线程
     */
    private Runnable inventoryTask = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                if (isStart) {
                    LogUtils.e("RFID scan run !");
                    List<Reader.TAGINFO> list1;
                    list1 = HomeActivity.mUhfrManager.tagInventoryByTimer((short) 50);
                    ArrayList<String> list = new ArrayList<>();
                    if (list1 != null && list1.size() > 0) {
                        for (Reader.TAGINFO tfs : list1) {
                            byte[] epcdata = tfs.EpcId;
                            String epc = Tools.Bytes2HexString(epcdata, epcdata.length);
                            LogUtils.e("Found epc = " + epc);
                            if (!mFoundEpcStrList.contains(epc)) {
                                list.add(epc);
                                mFoundEpcStrList.add(epc);
                            }
                        }
                        SoundUtil.play(1, 0);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mEntryTvSum.setText(String.valueOf(mFoundEpcStrList.size()));
                            }
                        });
                        getBoxInfo(list);
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    };
    /**
     * 盘存线程所需的控制标志
     */
    private boolean keyControl = true;
    private boolean isRunning = true;
    private boolean isStart = false;
    /**
     * 盘存到的epc集合
     */
    private List<String> mFoundEpcStrList = new ArrayList<>();
    /**
     * 主线程和子线程的通讯桥梁
     */
    private EntryHandler mHandler = new EntryHandler(EntryActivity.this);

    private static class EntryHandler extends Handler {
        private WeakReference<EntryActivity> mWeakReference;

        EntryHandler(EntryActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        public void handleMessage(Message msg) {
            EntryActivity activity = mWeakReference.get();
            if (msg.what == WHAT_INVENTORY) {
                activity.mEntryRcvAdapter.setList(activity.mEntryBoxInfoList);
                activity.mEntryTvCount.setText(String.valueOf(activity.mEntryBoxInfoList.size()));
                SoundUtil.play(1, 0);
                activity.mQrOrRfid = 1;
            }
        }
    }

    /***
     * 通讯标签
     */
    private static final int WHAT_INVENTORY = 0;
    /**
     * 适配器集合
     */
    private List<EntryBoxInfo> mEntryBoxInfoList = new ArrayList<>();
    /**
     * 适配器
     */
    private EntryRcvAdapter mEntryRcvAdapter;

    /**
     * 上传的物料id集合
     */
    private List<String> mUploadIdList = new ArrayList<>();

    /**
     * 录入物料信息对话框
     */
    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initUtils() {
        new Thread(inventoryTask).start();
        //注册按键广播接收者
        keyReceiver = new MyKeyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        filter.addAction("android.intent.action.FUN_KEY");
        registerReceiver(keyReceiver, filter);
    }

    @OnClick({R.id.entry_btn_scan, R.id.entry_btn_entry, R.id.entry_btn_search})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.entry_btn_scan:
                runInventory();
                break;
            case R.id.entry_btn_entry:
                uploadInfo();
                break;
            case R.id.entry_btn_search:
//                if (mSPUtils.getBoolean(SPUtils.KEY_IS_ENTRY)) {
                    startActivity(new Intent(EntryActivity.this, SearchActivity.class));
//                } else {
//                    ToastUtils.showShortToast("请先录入物料信息");
//                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        mSPUtils.putBoolean(SPUtils.KEY_IS_ENTRY, false);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(keyReceiver);
        super.onDestroy();
    }

    private void initView() {
        mEntryRcvAdapter = new EntryRcvAdapter();
        mEntryRv.setLayoutManager(new LinearLayoutManager(EntryActivity.this));
        mEntryRv.setAdapter(mEntryRcvAdapter);
    }

    /**
     * 开始盘存EPC
     */
    public void runInventory() {
        LogUtils.e("runInventory !");
        if (keyControl) {
            keyControl = false;
            if (!isStart) {
                // 屏蔽按钮
                mEntryBtnEntry.setClickable(false);
                mEntryBtnSearch.setClickable(false);
                HomeActivity.mUhfrManager.setCancleInventoryFilter();
                HomeActivity.mUhfrManager.setCancleFastMode();
                mEntryBtnScan.setText("停止扫描");
                isStart = true;
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HomeActivity.mUhfrManager.stopTagInventory();
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        isStart = false;

                        // 比对标签信息

                    }
                }).start();
                mEntryBtnScan.setText("点击扫描");
                mEntryBtnEntry.setClickable(true);
                mEntryBtnSearch.setClickable(true);
            }
            keyControl = true;
        }
    }

    /**
     * 获取标签信息
     */
    private void getBoxInfo(List<String> epc) {
        mRetrofitRequestHelper.entryOrderRequest(epc, new RetrofitRequestHelper.RetrofitRequestListener() {
            @Override
            public void requestSuccess(Response response) {
                MatterBean matterBean = (MatterBean) response.body();
                String code = matterBean.getCode();
                LogUtils.e("getBoxInfo.code = " + code);
                if (code.equals("200")) {
                    List<MatterBean.DataBean> data = matterBean.getData();
                    for (int i = 0; i < data.size(); i++) {
                        MatterBean.DataBean dataBean = data.get(i);
                        EntryBoxInfo entryBoxInfo = new EntryBoxInfo();
                        entryBoxInfo.setId(dataBean.getId());
                        entryBoxInfo.setTags(dataBean.getTags());
                        entryBoxInfo.setSign(dataBean.getSign());
                        entryBoxInfo.setMattertype(((String) dataBean.getMattertype()));
                        entryBoxInfo.setMattername((String) dataBean.getMattername());
                        entryBoxInfo.setMatternum(dataBean.getMatternum());
                        entryBoxInfo.setIsChecked(false);
                        mEntryBoxInfoList.add(entryBoxInfo);
                    }
                    mHandler.sendEmptyMessage(WHAT_INVENTORY);
                }

            }

            @Override
            public void requestFail(Throwable t) {

            }
        });
    }

    /**
     * 上传物料信息
     */
    private void uploadInfo() {
        if (mEntryBoxInfoList.size() == 0) {
            ToastUtils.showShortToast("没有可录入的物料信息");
            return;
        }
        mAlertDialog = new AlertDialog.Builder(EntryActivity.this)
                .create();
        View view2 = View.inflate(this, R.layout.layout_entry_dialog, null);
        final EditText edtMatterType = (EditText) view2.findViewById(R.id.entry_dialog_mattertype);
        final EditText edtMatterName = (EditText) view2.findViewById(R.id.entry_dialog_mattername);
        final EditText edtMatterNum = (EditText) view2.findViewById(R.id.entry_dialog_matternum);
        view2.findViewById(R.id.entry_dialog_btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
            }
        });
        view2.findViewById(R.id.entry_dialog_btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog("正在录入物料信息");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mUploadIdList.clear();
                        List<EntryBoxInfo> boxInfoList = mEntryRcvAdapter.getBoxInfoList();
                        for (int i = 0; i < boxInfoList.size(); i++) {
                            EntryBoxInfo entryBoxInfo = boxInfoList.get(i);
                            if (entryBoxInfo.getIsChecked()) {
                                mUploadIdList.add(entryBoxInfo.getId());
                            }
                        }
                        mUploadIdList.size();
                        String userid = mSPUtils.getString(SPUtils.KEY_USER_ID);
                        mMattertype = edtMatterType.getText().toString();
                        mMattername = edtMatterName.getText().toString();
                        mMatternum = edtMatterNum.getText().toString();
                        mRetrofitRequestHelper.uploadRequest(mUploadIdList, userid, mMattertype, mMatternum, mMattername, new RetrofitRequestHelper.RetrofitRequestListener() {
                            @Override
                            public void requestSuccess(Response response) {
                                ResponseBody responseBody = (ResponseBody) response.body();
                                try {
                                    String responseStr = responseBody.string();
                                    JSONObject jsonObject = new JSONObject(responseStr);
                                    String code = jsonObject.getString("code");
                                    if (code.equals("200")) {
                                        ToastUtils.showShortToast("录入成功");
                                        // TODO: 2018/4/15 刷新界面
                                        updateUi(mUploadIdList);
                                    } else {
                                        ToastUtils.showShortToast("录入失败，" + jsonObject.getString("message"));
                                        hideProgressDialog();
                                    }
                                } catch (Exception e) {
                                    ToastUtils.showShortToast("录入失败，未知错误");
                                    hideProgressDialog();
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void requestFail(Throwable t) {

                            }
                        });
                    }
                }).start();
            }
        });
        mAlertDialog.setView(view2);
        mAlertDialog.show();
    }

    private void updateUi(List<String> list){
        List<EntryBoxInfo> adapterList = mEntryRcvAdapter.getBoxInfoList();
        List<EntryBoxInfo> tempAdapterList = new ArrayList<>(adapterList);
        for (String id: list) {
            List<EntryBoxInfo> tempList = new ArrayList<>();
            for(EntryBoxInfo adapterInfo:tempAdapterList){
                if (adapterInfo.getId().equals(id)){
                    adapterInfo.setMatternum(mMatternum);
                    adapterInfo.setMattername(mMattername);
                    adapterInfo.setMattertype(mMattertype);
                    adapterInfo.setIsChecked(false);
                }
                tempList.add(adapterInfo);
            }
            tempAdapterList.clear();
            tempAdapterList.addAll(tempList);
        }
        mEntryRcvAdapter.setList(tempAdapterList);
        hideProgressDialog();
        mAlertDialog.dismiss();
    }
}
