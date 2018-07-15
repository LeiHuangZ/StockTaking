package com.hand.stocktaking.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.BRMicro.Tools;
import com.hand.stocktaking.R;
import com.hand.stocktaking.adapter.CleanBoxInfo;
import com.hand.stocktaking.adapter.CleanRcvAdapter;
import com.hand.stocktaking.adapter.OutDialogBoxInfo;
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
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Response;
import zpSDK.zpSDK.zpBluetoothPrinter;

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
    @BindView(R.id.clean_btn_print)
    Button mCleanBtnPrint;
    private String mCleanType;

    private KeyReceiver keyReceiver = new KeyReceiver();
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
    private String mUserName;
    private String mCleanTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        mEntryRcvAdapter = new CleanRcvAdapter();
        mCleanRv.setLayoutManager(new LinearLayoutManager(CleanActivity.this));
        mCleanRv.setAdapter(mEntryRcvAdapter);
        mQrOrRfid = 1;
        mUserName = mSPUtils.getString(SPUtils.KEY_NAME);
    }

    @Override
    public void setLayoutId() {
        mLayoutId = R.layout.activity_clean;
    }

    @Override
    public void setTitle() {
        mTitle = "箱子清洁";
    }

    @Override
    public void initUtils() {
        new Thread(inventoryTask).start();
        //注册按键广播接收者
        keyReceiver = new KeyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        filter.addAction("android.intent.action.FUN_KEY");
        registerReceiver(keyReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(keyReceiver);
        if (scanThread != null) {
            isStart = false;
            isRunning = false;
            scanThread.interrupt();
            scanThread.close();
        }
        super.onDestroy();
    }

    @OnClick({R.id.clean_btn_scan, R.id.clean_btn_cleared, R.id.clean_btn_print})
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
            case R.id.clean_btn_print:
                startPrint(mUserName, mCleanType, mCleanTime, String.valueOf(mCleanBoxInfoList.size()), String.valueOf(mFoundEpcStrList.size()), mCleanBoxInfoList);
                break;
                default:
                    break;
        }
    }

    /**
     * 开始盘存EPC
     */
    @Override
    public void runInventory() {
        if (MyApplication.mUhfrManager == null) {
            ToastUtils.showShortToast("RFID异常，请退出应用重启");
            return;
        }
        LogUtils.e("runInventory !");
        if (keyControl) {
            keyControl = false;
            if (!isStart) {
                // 屏蔽按钮
                mCleanBtnCleared.setClickable(false);
                MyApplication.mUhfrManager.setCancleInventoryFilter();
                MyApplication.mUhfrManager.setCancleFastMode();
                mCleanBtnScan.setText("停止扫描");
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
                        JSONArray data = jsonObject.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jsonObject1 = (JSONObject) data.get(i);
                            String tags = jsonObject1.getString("tags");
                            CleanBoxInfo entryBoxInfo = new CleanBoxInfo();
                            entryBoxInfo.setTags(tags);
                            entryBoxInfo.setSign(jsonObject1.getString("sign"));
                            mIdList.add(jsonObject1.getString("id"));
                            entryBoxInfo.setCleaner(mUserName);
                            entryBoxInfo.setCleanTime("");
                            mCleanBoxInfoList.add(entryBoxInfo);
                        }
                        mEntryRcvAdapter.setList(mCleanBoxInfoList);
                        mCleanTvSum.setText(String.valueOf(mCleanBoxInfoList.size()));
                    }else {
                        mFoundEpcStrList.removeAll(epc);
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
                        updateUi();
                        // 开放打印按键
                        mCleanBtnPrint.setEnabled(true);
                    } else {
                        String message = jsonObject.getString("message");
                        String error = "";
                        if ("clean.ids-error".equals(message)) {
                            error = "箱子id数组不存在";
                        } else if ("clean.userid-error".equals(message)) {
                            error = "用户id不存在";
                        } else if ("clean-error".equals(message)) {
                            error = "清洁失败";
                        }
                        ToastUtils.showShortToast("提交失败，错误信息：" + error);
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

    private void updateUi() {
        ArrayList<CleanBoxInfo> tempAdapterList = new ArrayList<>();
        long cleanTimeLong = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date date = new Date(cleanTimeLong);
        mCleanTime = simpleDateFormat.format(date);
        for (CleanBoxInfo info : mCleanBoxInfoList) {
            info.setCleanTime(mCleanTime);
            tempAdapterList.add(info);
        }
        mCleanBoxInfoList.clear();
        mCleanBoxInfoList.addAll(tempAdapterList);
        mEntryRcvAdapter.setList(mCleanBoxInfoList);
        hideProgressDialog();
    }

    private void startPrint(String person, String cleanType, String cleanTime, String boxSum, String tagSum, List<CleanBoxInfo> cleanBoxInfoList){
        if (listbluetoothdevice()){
            print(person, cleanType, cleanTime, boxSum, tagSum, cleanBoxInfoList);
        }
    }
    public static BluetoothAdapter myBluetoothAdapter;
    zpBluetoothPrinter printer = new zpBluetoothPrinter(CleanActivity.this);
    private boolean listbluetoothdevice() {
        if ((myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()) == null) {
            Toast.makeText(this, "没有找到蓝牙适配器", Toast.LENGTH_LONG).show();
            return false;
        }

        if (!myBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "蓝牙未开启", Toast.LENGTH_LONG).show();
            openSetting();
            return false;
        }

        Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() <= 0) {
            Toast.makeText(this, "请先连接蓝牙打印机", Toast.LENGTH_LONG).show();
            openSetting();
            return false;
        }else {
            for (BluetoothDevice device :
                    pairedDevices) {
                String address = device.getAddress();
                printer.connect(address);
            }
        }
        return true;
    }

    private void openSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void print(String person, String cleanType, String cleanTime, String boxSum, String tagSum, List<CleanBoxInfo> cleanBoxInfoList){
        int size = cleanBoxInfoList.size();
        printer.pageSetup(700, 172 + 24 * size + 140);
        int pixel = 60;
//        printer.drawText(60, 5, "清洁人员：", 2, 0, 0, false, false);
//        printer.drawText(180, 5, person, 2, 0, 0, false, false);
//        printer.drawText(60, 5 + 48 - 24, "清洁方式：", 2, 0, 0, false, false);
//        printer.drawText(180, 5 + 48 - 24, cleanType, 2, 0, 0, false, false);
//        printer.drawText(60, 5 + 72 - 24, "清洁时间：", 2, 0, 0, false, false);
//        printer.drawText(180, 5 + 72 - 24, cleanTime, 2, 0, 0, false, false);
//        printer.drawText(60, 5 + 96 - 24, "箱子总数：", 2, 0, 0, false, false);
//        printer.drawText(180, 5 + 96 - 24, boxSum, 2, 0, 0, false, false);
//        printer.drawText(60, 5 + 120 - 24, "标签总数：", 2, 0, 0, false, false);
//        printer.drawText(180, 5 + 120 - 24, tagSum, 2, 0, 0, false, false);
        printer.drawText(60 + pixel, 5, "清洁人员：", 2, 0, 0, false, false);
        printer.drawText(180 + pixel, 5, person, 2, 0, 0, false, false);
        printer.drawText(60 + pixel, 5 + 48 - 24, "清洁方式：", 2, 0, 0, false, false);
        printer.drawText(180 + pixel, 5 + 48 - 24, cleanType, 2, 0, 0, false, false);
        printer.drawText(60 + pixel, 5 + 72 - 24, "清洁时间：", 2, 0, 0, false, false);
        printer.drawText(180 + pixel, 5 + 72 - 24, cleanTime, 2, 0, 0, false, false);
        printer.drawText(60 + pixel, 5 + 96 - 24, "箱子总数：", 2, 0, 0, false, false);
        printer.drawText(180 + pixel, 5 + 96 - 24, boxSum, 2, 0, 0, false, false);
        printer.drawText(60 + pixel, 5 + 120 - 24, "标签总数：", 2, 0, 0, false, false);
        printer.drawText(180 + pixel, 5 + 120 - 24, tagSum, 2, 0, 0, false, false);
        printer.drawText(120, 5 + 156 - 24, "RFID", 2, 0, 0, false, false);
        printer.drawText(330, 5 + 156 - 24, "箱子型号", 2, 0, 0, false, false);
        for (int i = 0; i < cleanBoxInfoList.size(); i++) {
            CleanBoxInfo info = cleanBoxInfoList.get(i);
            printer.drawText(120, 5 + 180 - 24 + (24*i), info.getTags(), 2, 0, 0, false, false);
            printer.drawText(330, 5 + 180 - 24+ (24*i), info.getSign(), 2, 0, 0, false, false);
        }
//        for (int i = 0; i < cleanBoxInfoList.size(); i++) {
//            CleanBoxInfo info = cleanBoxInfoList.get(i);
//            printer.drawText(120, 5 + 192 - 24 + (24*i) + (24*cleanBoxInfoList.size()), info.getTags(), 2, 0, 0, false, false);
//            printer.drawText(330, 5 + 192 - 24+ (24*i) + (24*cleanBoxInfoList.size()), info.getSign(), 2, 0, 0, false, false);
//        }
//        for (int i = 0; i < cleanBoxInfoList.size(); i++) {
//            CleanBoxInfo info = cleanBoxInfoList.get(i);
//            printer.drawText(120, 5 + 192 - 24 + (24*i) + (24*cleanBoxInfoList.size()*2), info.getTags(), 2, 0, 0, false, false);
//            printer.drawText(330, 5 + 192 - 24+ (24*i) + (24*cleanBoxInfoList.size()*2), info.getSign(), 2, 0, 0, false, false);
//        }
        printer.print(0, 0);
//        int size = cleanBoxInfoList.size();
//        if (size == 0) {
//            printer.print(0, 0);
//            return;
//        }
//        for (int i = 0; i < 7; i++) {
//            if (i > size){
//                printer.print(0, 0);
//                return;
//            }
//            CleanBoxInfo info = cleanBoxInfoList.get(i);
//            printer.drawText(120, 5 + 192 - 24 + (24*i), info.getTags(), 2, 0, 0, false, false);
//            printer.drawText(330, 5 + 192 - 24+ (24*i), info.getSign(), 2, 0, 0, false, false);
//        }
//        printer.print(0, 0);
//        int secondSize = size - 7;
//        if (secondSize == 0){
//            return;
//        }
//        int count;
//        if (secondSize >= 11) {
//            float i = secondSize % 11;
//            count = secondSize /11;
//            if (i > 0){
//                count++;
//            }
//        }else {
//            count = 1;
//        }
//        boolean out =false;
//        for (int i = 0; i < count; i++) {
//            printer.pageSetup(700, 410);
//            printer.drawText(120, 5, "RFID", 2, 0, 0, false, false);
//            printer.drawText(330, 5, "箱子型号", 2, 0, 0, false, false);
//            for (int j = 0; j < 11; j++) {
//                int index = (11 * i) + j + 7;
//                if (index > size - 1){
//                    printer.print(0, 0);
//                    out = true;
//                    return;
//                }
//                printer.drawText(120 - 8, 5 + 30 * (j+1), cleanBoxInfoList.get(index).getTags(), 2, 0, 0, false, false);
//                printer.drawText(330 + 4, 5 + 30 * (j+1), cleanBoxInfoList.get(index).getSign(), 2, 0, 0, false, false);
//                if (j == 10) {
//                    printer.print(0, 0);
//                    out = true;
//                    break;
//                }
//            }
//        }
//        if (!out){
//            printer.print(0, 0);
//        }
    }
}
