package com.hand.stocktaking.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.ArrayMap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.BRMicro.Tools;
import com.hand.stocktaking.R;
import com.hand.stocktaking.adapter.DoorBoxInfo;
import com.hand.stocktaking.adapter.ReceiveBoxInfo;
import com.hand.stocktaking.adapter.ReceiveRcvAdapter;
import com.hand.stocktaking.adapter.StorageBoxInfo;
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
import java.lang.ref.WeakReference;
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

public class ReceiveActivity extends BaseActivity {

    @BindView(R.id.receive_btn_scan)
    Button mReceiveBtnScan;
    @BindView(R.id.receive_btn_fast_receive)
    Button mReceiveBtnFastReceive;
    @BindView(R.id.receive_btn_inventory)
    Button mReceiveBtnInventory;
    @BindView(R.id.receive_btn_ensure)
    Button mReceiveBtnEnsure;
    @BindView(R.id.receive_tv_sum)
    TextView mReceiveTvSum;
    @BindView(R.id.receive_tv_count)
    TextView mReceiveTvCount;
    @BindView(R.id.receive_tv_box_count)
    TextView mReceiveTvBoxCount;
    @BindView(R.id.receive_tv_result)
    TextView mReceiveTvResult;
    @BindView(R.id.receive_rv)
    RecyclerView mReceiveRv;
    @BindView(R.id.receive_ll_second)
    LinearLayout mReceiveLlSecond;
    @BindView(R.id.receive_tv_order_id)
    TextView mReceiveTvOrderId;

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

    /**
     * 主线程和子线程的通讯桥梁
     */
    private static class ReceiveHandler extends Handler {
        private WeakReference<ReceiveActivity> mWeakReference;

        ReceiveHandler(ReceiveActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ReceiveActivity receiveActivity = mWeakReference.get();
            if (msg.what == ScanThread.SCAN) {
                String data = msg.getData().getString("data");
                LogUtils.e("data = " + data);
                receiveActivity.showProgressDialog("正在获取订单信息....");
                receiveActivity.codeToBoxInfo(data);
                SoundUtil.play(1, 0);
            } else if (msg.what == 1) {
                SoundUtil.play(1, 0);
                receiveActivity.mReceiveTvCount.setText(String.valueOf(receiveActivity.mFoundEpcStrList.size()));
            }
        }
    }

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
                    list1 = MyApplication.mUhfrManager.tagInventoryByTimer((short) 50);
                    if (list1 != null && list1.size() > 0) {
                        for (Reader.TAGINFO tfs : list1) {
                            byte[] epcdata = tfs.EpcId;
                            String epc = Tools.Bytes2HexString(epcdata, epcdata.length);
                            if (!mFoundEpcStrList.contains(epc)) {
                                if (!mRedundantEpcStrList.contains(epc) && !mServerEpcStrList.contains(epc)) {
                                    mRedundantEpcStrList.add(epc);
                                }else if (mServerEpcStrList.contains(epc)){
                                    List<ReceiveBoxInfo> list = new ArrayList<>();
                                    for (int i = 0; i < mServerBoxInfoList.size(); i++) {
                                        ReceiveBoxInfo info = mServerBoxInfoList.get(i);
                                        if (mServerBoxInfoList.get(i).getRfid().equals(epc)) {
                                            info.setResolved(true);
                                        }
                                        list.add(info);
                                    }
                                    mServerBoxInfoList.clear();
                                    mServerBoxInfoList.addAll(list);
                                }
                                mFoundEpcStrList.add(epc);
                            }
                        }
                        mHandler.sendEmptyMessage(1);
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

    private MyKeyReceiver keyReceiver = new MyKeyReceiver();
    private ReceiveHandler mHandler = new ReceiveHandler(ReceiveActivity.this);
    /**
     * 扫描到的EPC号集合
     */
    private List<String> mFoundEpcStrList = new ArrayList<>();

    /**
     * 服务器获取的箱子的型号和数量Map
     */
    private ArrayMap<String, Integer> mServerSignMap = new ArrayMap<>();

    /**
     * 从服务器获取到的箱子的信息实体类集合
     */
    private List<ReceiveBoxInfo> mServerBoxInfoList = new ArrayList<>();

    /**
     * RecyclerView的Adapter
     */
    private ReceiveRcvAdapter mAdapter;

    /**
     * 订单号
     */
    private String mOrderId;

    /**
     * 根据订单号获取到的服务器的EPC集合
     */
    private List<String> mServerEpcStrList = new ArrayList<>();
    /**
     * 多出的标签的EPC集合
     */
    private List<String> mRedundantEpcStrList = new ArrayList<>();
    /***
     * 收货的类型 1--一键收货 2--异常收货 3--系统退运 4--系统补运
     */
    private String mReceiveType = "0";

    /**
     * 盘存线程所需的控制标志
     */
    private boolean keyControl = true;
    private boolean isRunning = true;
    private boolean isStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        initView();
    }
    @Override
    public void setLayoutId() {
        mLayoutId = R.layout.activity_receive;
    }

    @Override
    public void setTitle() {
        mTitle = "收货界面";
    }

    @OnClick({R.id.receive_btn_scan, R.id.receive_btn_fast_receive, R.id.receive_btn_inventory, R.id.receive_btn_ensure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.receive_btn_scan:
                scanThread.scan();
                break;
            case R.id.receive_btn_fast_receive:
                fastReceive();
                break;
            case R.id.receive_btn_inventory:
                runInventory();
                break;
            case R.id.receive_btn_ensure:
                if (mReceiveType.equals("0")){
                    ToastUtils.showShortToast("箱子数量缺失，请继续扫描！");
                    return;
                }
                if (mReceiveType.equals("1")){
                    receive();
                    return;
                }
                String str = "";
                if (mReceiveType.equals("2")){
                    str = "是否进行异常收货？";
                }else if (mReceiveType.equals("3")){
                    str = "是否进行系统退运？";
                }else if (mReceiveType.equals("4")){
                    str = "是否进行系统补运？";
                }
                final AlertDialog alertDialog = new AlertDialog.Builder(ReceiveActivity.this)
                        .setMessage(str)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                receive();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                alertDialog.show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(keyReceiver);
        isStart = false;
        isRunning = false;
        if (scanThread != null) {
            isStart = false;
            scanThread.interrupt();
            scanThread.close();
        }
        super.onDestroy();
    }

    @Override
    public void initUtils() {
        try {
            new Thread(inventoryTask).start();
            mAdapter = new ReceiveRcvAdapter();
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

    private void initView(){
        mReceiveRv.setLayoutManager(new LinearLayoutManager(ReceiveActivity.this));
        mReceiveRv.setAdapter(mAdapter);
    }

    /**
     * 二维码获取订单信息
     *
     * @param codeStr 二维码字符
     */
    private void codeToBoxInfo(String codeStr) {
        mRetrofitRequestHelper.codeToBoxRcvInfoRequest(codeStr.trim(), new RetrofitRequestHelper.RetrofitRequestListener() {
            @Override
            public void requestSuccess(Response response) {
                DoorBoxsBean doorBoxsBean = (DoorBoxsBean) response.body();
                String code = doorBoxsBean != null ? doorBoxsBean.getCode() : "null";
                LogUtils.e("DoorActivity, codeToBoxInfo return_code = " + code);
                if (code.equals("200")) {
                    mQrOrRfid = 1;
                    jumpToSecond(doorBoxsBean);
                } else {
                    ToastUtils.showShortToast("获取失败，错误信息："+ (doorBoxsBean != null ? doorBoxsBean.getMessage() : "未知"));
                }
                hideProgressDialog();
            }

            @Override
            public void requestFail(Throwable t) {
                if ( t== null || t.getMessage().contains("java.lang.IllegalStateException")){
                    ToastUtils.showShortToast("获取失败，无效的二维码参数");
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
        mOrderId = data.getOrderid();
        mReceiveTvOrderId.setText(mOrderId);
        mReceiveTvSum.setText(String.valueOf(boxtotal));
        List<DoorBoxsBean.DataBean.BoxsBean> boxs = data.getBoxs();
        String send_company = data.getSend_company();
        String recv_company = data.getRecv_company();
        String send_time = data.getSend_time();
        for (int i = 0; i < boxs.size(); i++) {
            ReceiveBoxInfo doorBoxInfo = new ReceiveBoxInfo();
            DoorBoxsBean.DataBean.BoxsBean boxBean = boxs.get(i);
            String sign = boxBean.getSign();
            boolean containsKey = mServerSignMap.containsKey(sign);
            if (containsKey) {
                Integer num = mServerSignMap.get(sign);
                num += 1;
                mServerSignMap.put(sign, num);
            } else {
                mServerSignMap.put(sign, 1);
            }
            String tags = boxBean.getTags();
            doorBoxInfo.setRfid(tags);
            if (!mServerEpcStrList.contains(tags)){
                mServerEpcStrList.add(tags);
            }
            doorBoxInfo.setBoxType(sign);
            doorBoxInfo.setSendCompany(send_company);
            doorBoxInfo.setReceiveCompany(recv_company);
            doorBoxInfo.setSendTime(send_time);
            mServerBoxInfoList.add(doorBoxInfo);
        }
        mAdapter.setList(mServerBoxInfoList);
        Set<Map.Entry<String, Integer>> entrySet = mServerSignMap.entrySet();
        mReceiveTvBoxCount.setText("");
        for (Map.Entry<String, Integer> entry :
                entrySet) {
            mReceiveTvBoxCount.append(entry.getKey());
            mReceiveTvBoxCount.append("型号箱子  ");
            mReceiveTvBoxCount.append(String.valueOf(entry.getValue()));
            mReceiveTvBoxCount.append("个\n");
        }
        mReceiveBtnScan.setVisibility(View.GONE);
        mReceiveLlSecond.setVisibility(View.VISIBLE);
    }
    /**
     * 一键收货
     */
    private void fastReceive(){
        showProgressDialog("正在提交收货信息");
        String userId = mSPUtils.getString(SPUtils.KEY_USER_ID);
        LogUtils.e("userid = " + userId + ", " + "mOrderId = " + mOrderId);
        for (String string:mServerEpcStrList) {
            LogUtils.e("serverEpcStr = " + string);
        }
        mRetrofitRequestHelper.receiveRequest("1", userId, mServerEpcStrList, mOrderId, new RetrofitRequestHelper.RetrofitRequestListener() {
            @Override
            public void requestSuccess(Response response) {
                ResponseBody responseBody = (ResponseBody) response.body();
                try {
                    String responseStr = responseBody.string();
                    JSONObject jsonObject = new JSONObject(responseStr);
                    String code = jsonObject.getString("code");
                    if (code.equals("200")){
                        ToastUtils.showShortToast("收货成功");
                        mReceiveBtnEnsure.setClickable(false);
                        mReceiveBtnFastReceive.setClickable(false);
                        mReceiveBtnInventory.setClickable(false);
                        hideProgressDialog();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                    finish();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }else {
                        ToastUtils.showShortToast("收货失败，" + jsonObject.getString("message"));
                        hideProgressDialog();
                    }
                } catch (Exception e) {
                    ToastUtils.showShortToast("收货失败，未知错误");
                    hideProgressDialog();
                    e.printStackTrace();
                }
            }

            @Override
            public void requestFail(Throwable t) {
                ToastUtils.showShortToast("收货失败，请检查网络设置");
                hideProgressDialog();
            }
        });
    }

    /**
     * 开始盘存EPC
     */
    @Override
    public void runInventory() {
        if (MyApplication.mUhfrManager == null){
            ToastUtils.showShortToast("RFID异常，请退出应用重启");
            return;
        }
        LogUtils.e("runInventory !");
        if (keyControl) {
            keyControl = false;
            if (!isStart) {
                // 屏蔽按钮
                mReceiveBtnEnsure.setClickable(false);
                MyApplication.mUhfrManager.setCancleInventoryFilter();
                MyApplication.mUhfrManager.setCancleFastMode();
                mReceiveBtnInventory.setText("停止扫描");
                isStart = true;
            } else {
                isStart = false;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            MyApplication.mUhfrManager.stopTagInventory();
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // 比对标签信息
                            compareTags();
                        }
                    }).start();
                mReceiveBtnInventory.setText("点击扫描");
                mReceiveBtnEnsure.setClickable(true);
            }
            keyControl = true;
        }
    }

    /**
     * 比较盘存到的EPC和服务器获取的EPC
     */
    private boolean isFirst = true;
    private boolean isFirst2 = true;
    boolean isRemix = false;
    private void compareTags() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgressDialog("正在比对信息");
            }
        });
        if (mRedundantEpcStrList.size() == 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mServerEpcStrList.size() == mFoundEpcStrList.size()){
                        mReceiveType = "1";
                        mReceiveTvResult.setText("确认完毕，扫描通过！");
                        hideProgressDialog();
                        return;
                    }else if (mServerEpcStrList.size() > mFoundEpcStrList.size()){
                        mReceiveType = "3";
                    }
                }
            });
        }else {
            if (mServerEpcStrList.size() < mFoundEpcStrList.size()){
                mReceiveType = "4";
            }else if (mServerEpcStrList.size() == mFoundEpcStrList.size()){
                mReceiveType = "2";
            }else if (mServerEpcStrList.size() > mFoundEpcStrList.size()){
                mReceiveType = "3";
            }

        }
        for (int i = 0; i < mServerBoxInfoList.size(); i++) {
            if (!mServerBoxInfoList.get(i).isResolved()){
                final int finalI = i;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFirst2) {
                            mReceiveTvResult.setText("扫描不通过，缺少\n");
                            isFirst2 = false;
                        }
                        mReceiveTvResult.append("型号");
                        mReceiveTvResult.append(mServerBoxInfoList.get(finalI).getBoxType());
                        mReceiveTvResult.append("，RF标签：");
                        mReceiveTvResult.append(mServerBoxInfoList.get(finalI).getRfid());
                        mReceiveTvResult.append("箱子\n");
                        SoundUtil.pasue();
                        SoundUtil.play(2, 0);
                    }
                });
                isRemix = true;
            }
        }

            mRetrofitRequestHelper.outOrderRequest(mRedundantEpcStrList, new RetrofitRequestHelper.RetrofitRequestListener() {
                @Override
                public void requestSuccess(Response response) {
                    ResponseBody responseBody = (ResponseBody) response.body();
                    try {
                        String string = responseBody.string();
                        JSONObject jsonObject = new JSONObject(string);
                        LogUtils.e("code = " + jsonObject.getString("code"));
                        if ("200".equals(jsonObject.getString("code"))) {
                            JSONArray data = jsonObject.getJSONArray("data");
                            int length = data.length();
                            LogUtils.e("code = " + length);
                            List<String> tempEpcList = new ArrayList<>();
                            for (int i = 0; i < length; i++) {
                                JSONObject jsonObject1 = data.getJSONObject(i);
                                String sign = jsonObject1.getString("sign");
                                String tags = jsonObject1.getString("tags");
                                if (mRedundantEpcStrList.size() != 0 && isRemix){
                                    if (isFirst) {
                                        mReceiveTvResult.append("多出\n");
                                        isFirst = false;
                                    }

                                }else {
                                    if (isFirst && isFirst2) {
                                        mReceiveTvResult.setText("扫描不通过，多出\n");
                                        isFirst = false;
                                    }
                                }
                                tempEpcList.add(tags);
                                mReceiveTvResult.append("型号");
                                mReceiveTvResult.append(sign);
                                mReceiveTvResult.append("，RF标签：");
                                mReceiveTvResult.append(tags);
                                mReceiveTvResult.append("箱子\n");
                            }
                            for (String epc:mRedundantEpcStrList) {
                                boolean isReachable = true;
                                for (String epc2:tempEpcList) {
                                    if (epc.equals(epc2)){
                                        isReachable = false;
                                    }
                                }
                                if (isReachable){
                                    if (mRedundantEpcStrList.size() != 0 && isRemix){
                                        if (isFirst) {
                                            mReceiveTvResult.append("多出\n");
                                            isFirst = false;
                                        }

                                    }else {
                                        if (isFirst && isFirst2) {
                                            mReceiveTvResult.setText("扫描不通过，多出\n");
                                            isFirst = false;
                                        }
                                    }
                                    mReceiveTvResult.append("未知标签：");
                                    mReceiveTvResult.append(epc);
                                    mReceiveTvResult.append("\n");
                                }
                            }
                            SoundUtil.pasue();
                            SoundUtil.play(2, 0);
                        }else {
                            if (mRedundantEpcStrList.size() != 0){
                                if (isRemix){
                                    if (isFirst) {
                                        mReceiveTvResult.append("多出\n");
                                        isFirst = false;
                                    }

                                }else {
                                    if (isFirst && isFirst2) {
                                        mReceiveTvResult.setText("扫描不通过，多出\n");
                                        isFirst = false;
                                    }
                                }
                                for(String epc:mRedundantEpcStrList){
                                    mReceiveTvResult.append("未知标签：");
                                    mReceiveTvResult.append(epc);
                                    mReceiveTvResult.append("\n");
                                }
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
        isFirst2 = true;
        isFirst = true;

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
     * 收货
     */
    private void receive(){
        showProgressDialog("正在提交收货信息");
        String userId = mSPUtils.getString(SPUtils.KEY_USER_ID);
        LogUtils.e("userid = " + userId + ", " + "mOrderId = " + mOrderId);
        for (String string:mServerEpcStrList) {
            LogUtils.e("serverEpcStr = " + string);
        }
        mRetrofitRequestHelper.receiveRequest(mReceiveType, userId, mFoundEpcStrList, mOrderId, new RetrofitRequestHelper.RetrofitRequestListener() {
            @Override
            public void requestSuccess(Response response) {
                ResponseBody responseBody = (ResponseBody) response.body();
                try {
                    String responseStr = responseBody.string();
                    JSONObject jsonObject = new JSONObject(responseStr);
                    String code = jsonObject.getString("code");
                    if (code.equals("200")){
                        ToastUtils.showShortToast("收货成功");
                        mReceiveBtnEnsure.setClickable(false);
                        mReceiveBtnFastReceive.setClickable(false);
                        mReceiveBtnInventory.setClickable(false);
                        hideProgressDialog();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                    finish();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }else {
                        String message = jsonObject.getString("message");
                        String error = "";
                        if ("recvOrder.type-error".equals(message)){
                            error = "订单类型不存在";
                        }else if ("recvOrder.userid-error".equals(message)){
                            error = "用户id不存在";
                        }else if ("recvOrder.orderid-error".equals(message)){
                            error = "订单号不存在";
                        }else if ("recvOrder.tags-error".equals(message)){
                            error = "标签列表不存在";
                        }else if ("recvOrder.orderinfo-error".equals(message)){
                            error = "订单不存在";
                        }else if ("recvOrder.tags.unknown-error".equals(message)){
                            error = "未知标签";
                        }else if ("recvOrder.type.content-error".equals(message)){
                            error = "订单类型错误";
                        }else if ("recvOrder-error".equals(message)){
                            error = "收货失败";
                        }
                        ToastUtils.showShortToast("收货失败，错误信息：" + error);
                        hideProgressDialog();
                    }
                } catch (Exception e) {
                    ToastUtils.showShortToast("收货失败，未知错误");
                    hideProgressDialog();
                    e.printStackTrace();
                }
            }

            @Override
            public void requestFail(Throwable t) {
                ToastUtils.showShortToast("收货失败，请检查网络设置");
                hideProgressDialog();
            }
        });
    }
}
