package com.hand.stocktaking.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.BRMicro.Tools;
import com.hand.stocktaking.R;
import com.hand.stocktaking.adapter.DoorBoxInfo;
import com.hand.stocktaking.adapter.DoorRcvAdapter;
import com.hand.stocktaking.adapter.OutBoxInfo;
import com.hand.stocktaking.adapter.StorageBoxInfo;
import com.hand.stocktaking.retrofit.CompanyBean;
import com.hand.stocktaking.retrofit.DoorBoxsBean;
import com.hand.stocktaking.retrofit.RetrofitRequestHelper;
import com.hand.stocktaking.utils.LogUtils;
import com.hand.stocktaking.utils.SPUtils;
import com.hand.stocktaking.utils.SoundUtil;
import com.hand.stocktaking.utils.ToastUtils;
import com.uhf.api.cls.Reader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pda.scan.ScanThread;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class DoorActivity extends BaseActivity {

    @BindView(R.id.door_btn_scan)
    Button mDoorBtnScan;
    @BindView(R.id.door_btn_inventory)
    Button mDoorBtnInventory;
    @BindView(R.id.door_tv_sum)
    TextView mDoorTvSum;
    @BindView(R.id.door_tv_count)
    TextView mDoorTvCount;
    @BindView(R.id.door_tv_box_null)
    TextView mDoorTvBoxNull;
    @BindView(R.id.door_tv_order_id)
    TextView mDoorTvOrderId;
    @BindView(R.id.door_tv_trans_company)
    TextView mDoorTvTransCompany;
    @BindView(R.id.door_tv_trans_company_plate)
    TextView mDoorTvTransCompanyPlate;
    @BindView(R.id.door_tv_result)
    TextView mDoorTvResult;
    @BindView(R.id.out_tv_result)
    TextView mOutTvResult;
    @BindView(R.id.door_rv)
    RecyclerView mDoorRv;
    @BindView(R.id.door_ll_second)
    LinearLayout mDoorLlSecond;
    private final DoorRcvAdapter mDoorRcvAdapter = new DoorRcvAdapter();
    /**
     * RecyclerView箱子信息集合
     */
    private List<DoorBoxInfo> mInfoList = new ArrayList<>();
    /**
     * 记录箱子型号和数量
     */
    private ArrayMap<String, Integer> mBoxTypeMap = new ArrayMap<>();
    private boolean isFinishInventory = false;

    public ScanThread scanThread;
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == ScanThread.SCAN) {
                String data = msg.getData().getString("data");
                LogUtils.e("data = " + data);
                showProgressDialog("正在获取订单信息....");
                codeToBoxInfo(data);
                SoundUtil.play(1, 0);
            } else if (msg.what == 1) {
                SoundUtil.play(1, 0);
                mDoorTvCount.setText(String.valueOf(mEpcList.size()));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        initView();
        new Thread(inventoryTask).start();
    }
    @Override
    public void setLayoutId() {
        mLayoutId = R.layout.activity_door;
    }

    @Override
    public void setTitle() {
        mTitle = "门卫检查";
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(keyReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (scanThread != null) {
            isStart = false;
            isRunning = false;
            scanThread.interrupt();
            scanThread.close();
        }
        super.onDestroy();
    }

    /**
     * 按键广播接收者 用于接受按键广播 触发扫描
     */
    private MyKeyReceiver keyReceiver = new MyKeyReceiver();

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
                        if (mQrOrRfid == 0) {
                            //开启扫描
                            scanThread.scan();
                        } else {
                            // uhf扫描
                            runInventory();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private boolean keyControl = true;
    private boolean isRunning = true;
    private boolean isStart = false;
    /**
     * 扫描到的EPC集合
     */
    private Map<String, Boolean> mEpcMap = new ArrayMap<>();
    private List<String> mEpcList = new ArrayList<>();

    /**
     * 多出的标签信息
     */
    private List<String> mRedundantEpcList = new ArrayList<>();

    @Override
    public void runInventory() {
        LogUtils.e("runInventory !");
        if (MyApplication.mUhfrManager == null){
            ToastUtils.showShortToast("RFID异常，请退出应用重启");
            return;
        }
        if (keyControl) {
            keyControl = false;
            if (!isStart) {
                // 屏蔽按钮
                MyApplication.mUhfrManager.setCancleInventoryFilter();
                MyApplication.mUhfrManager.setCancleFastMode();
                mDoorBtnInventory.setText("停止扫描");
                isStart = true;
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MyApplication.mUhfrManager.stopTagInventory();
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        isStart = false;

                        // 比对标签信息
                        compareTags();
                    }
                }).start();
                mDoorBtnInventory.setText("点击扫描");
            }
            keyControl = true;
        }
    }

    boolean isfirst = true;

    private void compareTags() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgressDialog("正在比对信息");
            }
        });
        if (mRedundantEpcList.size() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDoorTvResult.setText("确认完毕，扫描通过！");
                    hideProgressDialog();
                }
            });
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDoorTvResult.setText("确认完毕，扫描通过！");
            }
        });
        mRetrofitRequestHelper.outOrderRequest(mRedundantEpcList, new RetrofitRequestHelper.RetrofitRequestListener() {
            @Override
            public void requestSuccess(Response response) {
                ResponseBody responseBody = (ResponseBody) response.body();
                try {
                    String string = responseBody.string();
                    JSONObject jsonObject = new JSONObject(string);
                    LogUtils.e("code = " + jsonObject.getString("code"));
                    if ("200".equals(jsonObject.getString("code"))) {
                        JSONArray data = jsonObject.getJSONArray("data");
                        LogUtils.e("data.length = " + data.length());
                        List<String> tempEpcList = new ArrayList<>();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jsonObject1 = data.getJSONObject(i);
                            String sign = jsonObject1.getString("sign");
                            String tags = jsonObject1.getString("tags");
                            if (isfirst) {
                                mDoorTvResult.setText("扫描不通过，多出\n");
                                isfirst = false;
                            }
                            mDoorTvResult.append("型号");
                            mDoorTvResult.append(sign);
                            mDoorTvResult.append("，RF标签：");
                            mDoorTvResult.append(tags);
                            mDoorTvResult.append("箱子\n");
                            DoorBoxInfo doorBoxInfo = new DoorBoxInfo();
                            doorBoxInfo.setBoxType(sign);
                            doorBoxInfo.setRedundant(true);
                            doorBoxInfo.setRfid(tags);
                            mInfoList.add(doorBoxInfo);
                            mDoorRcvAdapter.setList(mInfoList);
                            tempEpcList.add(tags);
                        }
                        for (String epc:mRedundantEpcList) {
                            boolean isReachable = true;
                            for (String epc2:tempEpcList) {
                                if (epc.equals(epc2)){
                                    isReachable = false;
                                }
                            }
                            if (isReachable){
                                if (isfirst) {
                                    mDoorTvResult.setText("扫描不通过，多出\n");
                                    isfirst = false;
                                }
                                mDoorTvResult.append("未知标签：");
                                mDoorTvResult.append(epc);
                                mDoorTvResult.append("\n");
                            }
                        }
                        SoundUtil.pasue();
                        SoundUtil.play(2, 0);
                    }else {
                        if (mRedundantEpcList.size() != 0){
                            for(String epc:mRedundantEpcList){
                                if (isfirst) {
                                    mDoorTvResult.setText("扫描不通过，多出\n");
                                    isfirst = false;
                                }
                                mDoorTvResult.append("未知标签：");
                                mDoorTvResult.append(epc + "\n");
                            }
                            SoundUtil.pasue();
                            SoundUtil.play(2, 0);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void requestFail(Throwable t) {
            }
        });

        isfirst = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                    }
                });
            }
        }).start();
    }

    /**
     * inventory epc
     */
    private Runnable inventoryTask = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                if (isStart) {
                    LogUtils.e("RFID scan run !");
                    List<Reader.TAGINFO> list1;
                    list1 = MyApplication.mUhfrManager.tagInventoryByTimer((short) 50);
                    if (list1 != null && list1.size() > 0) {
                        for (Reader.TAGINFO tfs : list1) {
                            byte[] epcdata = tfs.EpcId;
                            String epc = Tools.Bytes2HexString(epcdata, epcdata.length);
                            if (!mEpcList.contains(epc)) {
                                if (!mEpcMap.containsKey(epc)) {
                                    mRedundantEpcList.add(epc);
                                } else {
                                    mEpcMap.put(epc, true);
                                }
                                mEpcList.add(epc);
                            }
                        }
                        mHandler.sendEmptyMessage(1);
                        Set<Map.Entry<String, Boolean>> entrySet = mEpcMap.entrySet();
                        isFinishInventory = true;
                        for (Map.Entry<String, Boolean> entry :
                                entrySet) {
                            if (!entry.getValue()) {
                                isFinishInventory = false;
                            }
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

    private void initView() {
        mDoorRv.setLayoutManager(new LinearLayoutManager(DoorActivity.this));
        mDoorRv.setAdapter(mDoorRcvAdapter);
    }

    @Override
    public void initUtils() {
        try {
            scanThread = new ScanThread(mHandler);
            scanThread.start();
            //注册按键广播接收者
            keyReceiver = new MyKeyReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.rfid.FUN_KEY");
            filter.addAction("android.intent.action.FUN_KEY");
            registerReceiver(keyReceiver, filter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 二维码获取订单信息
     *
     * @param codeStr 二维码字符
     */
    private void codeToBoxInfo(String codeStr) {
        mRetrofitRequestHelper.codeToBoxInfoRequest(codeStr.trim(), new RetrofitRequestHelper.RetrofitRequestListener() {
            @Override
            public void requestSuccess(Response response) {
                DoorBoxsBean doorBoxsBean = (DoorBoxsBean) response.body();
                String code = doorBoxsBean != null ? doorBoxsBean.getCode() : "null";
                LogUtils.e("DoorActivity, codeToBoxInfo return_code = " + code);
                if (code.equals("200")) {
                    mQrOrRfid = 1;
                    jumpToSecond(doorBoxsBean);
                } else {
                    ToastUtils.showShortToast("获取失败，请确认二维码是否有效");
                }
                hideProgressDialog();
            }

            @Override
            public void requestFail(Throwable t) {
                Log.e("huang", "requestFail, t = " + t.getMessage());
                if (t.getMessage().contains("Expected BEGIN_OBJECT but was BEGIN_ARRAY")){
                    ToastUtils.showShortToast("获取失败，请确认二维码是否有效");
                }else {
                    ToastUtils.showShortToast("获取失败，请确认网络连接是否正常");
                }
                hideProgressDialog();
            }
        });
    }

    /**
     * 订单信息获取成功 显示第二层界面
     */
    private void jumpToSecond(DoorBoxsBean doorBoxsBean) {
        DoorBoxsBean.DataBean data = doorBoxsBean.getData();
        int boxtotal = data.getBoxtotal();
        String in = data.getIs_emptybox();
        String isNull = "";
        if (in.equals("0")) {
            isNull = "非空箱";
        } else {
            isNull = "空箱";
        }
        mDoorTvBoxNull.setText(isNull);
        String orderid = data.getOrderid();
        mDoorTvOrderId.setText(orderid);
        String trans_company = data.getTrans_company();
        mDoorTvTransCompany.setText(trans_company);
        String trans_company_carnum = data.getTrans_company_carnum();
        mDoorTvTransCompanyPlate.setText(trans_company_carnum);
        mDoorTvSum.setText(String.valueOf(boxtotal));
        List<DoorBoxsBean.DataBean.BoxsBean> boxs = data.getBoxs();
        String send_company = data.getSend_company();
        String recv_company = data.getRecv_company();
        String send_time = data.getSend_time();
        for (int i = 0; i < boxs.size(); i++) {
            DoorBoxInfo doorBoxInfo = new DoorBoxInfo();
            DoorBoxsBean.DataBean.BoxsBean boxBean = boxs.get(i);
            String sign = boxBean.getSign();
            boolean containsKey = mBoxTypeMap.containsKey(sign);
            if (containsKey) {
                Integer num = mBoxTypeMap.get(sign);
                num += 1;
                mBoxTypeMap.put(sign, num);
            } else {
                mBoxTypeMap.put(sign, 1);
            }
            String tags = boxBean.getTags();
            mEpcMap.put(tags, false);
            doorBoxInfo.setRfid(tags);
            doorBoxInfo.setBoxType(sign);
            doorBoxInfo.setSendCompany(send_company);
            doorBoxInfo.setReceiveCompany(recv_company);
            doorBoxInfo.setSendTime(send_time);
            mInfoList.add(doorBoxInfo);
        }
        mDoorRcvAdapter.setList(mInfoList);
        Set<Map.Entry<String, Integer>> entrySet = mBoxTypeMap.entrySet();
        mOutTvResult.setText("");
        for (Map.Entry<String, Integer> entry :
                entrySet) {
            mOutTvResult.append(entry.getKey());
            mOutTvResult.append("型号箱子  ");
            mOutTvResult.append(String.valueOf(entry.getValue()));
            mOutTvResult.append("个\n");
        }
        mDoorBtnScan.setVisibility(View.GONE);
        mDoorLlSecond.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.door_btn_scan, R.id.door_btn_inventory})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.door_btn_scan:
                scanThread.scan();
                break;
            case R.id.door_btn_inventory:
                runInventory();
                break;
        }
    }

}
