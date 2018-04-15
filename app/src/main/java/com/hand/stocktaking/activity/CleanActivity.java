package com.hand.stocktaking.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.BRMicro.Tools;
import com.hand.stocktaking.R;
import com.hand.stocktaking.adapter.CleanBoxInfo;
import com.hand.stocktaking.adapter.CleanRcvAdapter;
import com.hand.stocktaking.retrofit.RetrofitRequestHelper;
import com.hand.stocktaking.utils.LogUtils;
import com.hand.stocktaking.utils.SPUtils;
import com.hand.stocktaking.utils.SoundUtil;
import com.hand.stocktaking.utils.ToastUtils;
import com.uhf.api.cls.Reader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class CleanActivity extends BaseActivity {

    @BindView(R.id.clean_btn_scan)
    Button mCleanBtnScan;
    @BindView(R.id.clean_edt_clean_type)
    EditText mCleanEdtCleanType;
    @BindView(R.id.clean_tv_count)
    TextView mCleanTvCount;
    @BindView(R.id.clean_rv)
    RecyclerView mCleanRv;
    @BindView(R.id.clean_tv_sum)
    TextView mCleanTvSum;
    @BindView(R.id.clean_btn_cleared)
    Button mCleanBtnCleared;
    private String mCleanType;

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
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mCleanTvCount.setText(String.valueOf(mFoundEpcStrList.size()));

                                    }
                                });
                            }
                        }
                        SoundUtil.play(1, 0);
                        if (list.size() > 0) {
                            getBoxInfo(list);
                        }
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
     * 盘存到的箱子的ID的集合
     */
    private List<String> mIdList = new ArrayList<>();
    /**
     * 适配器集合
     */
    private List<CleanBoxInfo> mCleanBoxInfoList = new ArrayList<>();
    /**
     * 适配器
     */
    private CleanRcvAdapter mEntryRcvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clean);
        ButterKnife.bind(this);
        mEntryRcvAdapter = new CleanRcvAdapter();
        mCleanRv.setLayoutManager(new LinearLayoutManager(CleanActivity.this));
        mCleanRv.setAdapter(mEntryRcvAdapter);
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

    @Override
    protected void onDestroy() {
        unregisterReceiver(keyReceiver);
        isRunning = false;
        isStart = false;
        super.onDestroy();
    }

    @OnClick({R.id.clean_btn_scan, R.id.clean_btn_cleared})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.clean_btn_scan:
                runInventory();
                break;
            case R.id.clean_btn_cleared:
                mCleanType = mCleanEdtCleanType.getText().toString();
                if (mCleanType.equals("")) {
                    ToastUtils.showShortToast("请输入清洁方式");
                    return;
                }
                commit();
                break;
        }
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
                mCleanBtnCleared.setClickable(false);
                HomeActivity.mUhfrManager.setCancleInventoryFilter();
                HomeActivity.mUhfrManager.setCancleFastMode();
                mCleanBtnScan.setText("停止扫描");
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
                mCleanBtnScan.setText("点击扫描");
                mCleanBtnCleared.setClickable(true);
            }
            keyControl = true;
        }
    }

    /**
     * 获取标签信息
     */
    private void getBoxInfo(final List<String> epc) {
        mRetrofitRequestHelper.cleanOrderRequest(epc, new RetrofitRequestHelper.RetrofitRequestListener() {
            @Override
            public void requestSuccess(Response response) {
                ResponseBody responseBody = (ResponseBody) response.body();
                try {
                    String body = responseBody.string();
                    JSONObject jsonObject = new JSONObject(body);
                    String code = jsonObject.getString("code");
                    LogUtils.e("getBoxInfo.code = " + code);
                    if (code.equals("200")) {
                        String userName = mSPUtils.getString(SPUtils.KEY_NAME);
                        JSONArray data = jsonObject.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jsonObject1 = (JSONObject) data.get(i);
                            CleanBoxInfo entryBoxInfo = new CleanBoxInfo();
                            entryBoxInfo.setTags(jsonObject1.getString("tags"));
                            entryBoxInfo.setSign(jsonObject1.getString("sign"));
                            mIdList.add(jsonObject1.getString("id"));
                            entryBoxInfo.setCleaner(userName);
                            entryBoxInfo.setCleanTime("");
                            mCleanBoxInfoList.add(entryBoxInfo);
                        }
                        mEntryRcvAdapter.setList(mCleanBoxInfoList);
                        mCleanTvSum.setText(String.valueOf(mCleanBoxInfoList.size()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void requestFail(Throwable t) {
                mFoundEpcStrList.removeAll(epc);
            }
        });
    }

    /**
     * 提交清洁信息并刷新界面
     */
    private void commit() {
        showProgressDialog("正在上传清洁信息....");
        String userId = mSPUtils.getString(SPUtils.KEY_USER_ID);
        mRetrofitRequestHelper.cleanUploadRequest(mIdList, userId, mCleanType, new RetrofitRequestHelper.RetrofitRequestListener() {
            @Override
            public void requestSuccess(Response response) {
                try {
                    ResponseBody body = (ResponseBody) response.body();
                    JSONObject jsonObject = new JSONObject(body.string());
                    String code = jsonObject.getString("code");
                    if (code.equals("200")) {
                        ToastUtils.showShortToast("提交成功");
                        // TODO: 2018/4/15 刷新界面
                        updateUi();
                    }else {
                        ToastUtils.showShortToast("提交失败，错误信息："+jsonObject.getString("message"));
                        hideProgressDialog();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void requestFail(Throwable t) {
                ToastUtils.showShortToast("提交失败，请检查网络设置");
                hideProgressDialog();
            }
        });
    }

    private void updateUi(){
        ArrayList<CleanBoxInfo> tempAdapterList = new ArrayList<>();
        long cleanTimeLong = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date = new Date(cleanTimeLong);
        String cleanTime = simpleDateFormat.format(date);
        for(CleanBoxInfo info:mCleanBoxInfoList){
            info.setCleanTime(cleanTime);
            tempAdapterList.add(info);
        }
        mCleanBoxInfoList.clear();
        mCleanBoxInfoList.addAll(tempAdapterList);
        mEntryRcvAdapter.setList(mCleanBoxInfoList);
        hideProgressDialog();
    }
}
