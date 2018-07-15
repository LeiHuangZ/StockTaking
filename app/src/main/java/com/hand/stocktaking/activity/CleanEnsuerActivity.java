package com.hand.stocktaking.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.BRMicro.Tools;
import com.hand.stocktaking.R;
import com.hand.stocktaking.adapter.CleanBoxInfo;
import com.hand.stocktaking.adapter.CleanEnsureBoxInfo;
import com.hand.stocktaking.adapter.CleanEnsureRcvAdapter;
import com.hand.stocktaking.adapter.CleanRcvAdapter;
import com.hand.stocktaking.retrofit.RetrofitRequestHelper;
import com.hand.stocktaking.utils.LogUtils;
import com.hand.stocktaking.utils.SoundUtil;
import com.hand.stocktaking.utils.ToastUtils;
import com.uhf.api.cls.Reader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class CleanEnsuerActivity extends BaseActivity {

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
    @BindView(R.id.cleanensure_tv_result)
    TextView mCleanensureTvResult;
    private String mTime;

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
     * 适配器集合
     */
    private List<CleanEnsureBoxInfo> mCleanBoxInfoList = new ArrayList<>();
    /**
     * 适配器
     */
    private CleanEnsureRcvAdapter mEntryRcvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        mEntryRcvAdapter = new CleanEnsureRcvAdapter();
        mCleanRv.setLayoutManager(new LinearLayoutManager(CleanEnsuerActivity.this));
        mCleanRv.setAdapter(mEntryRcvAdapter);
        mQrOrRfid = 1;
    }

    @Override
    public void setLayoutId() {
        mLayoutId = R.layout.activity_clean_ensure;
    }

    @Override
    public void setTitle() {
        mTitle = "清洁检查";
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
        isStart = false;
        isRunning = false;
        super.onDestroy();
    }

    @OnClick(R.id.clean_btn_scan)
    public void onViewClicked() {
        runInventory();
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
        mTime = mCleanEdtCleanType.getText().toString();
        if (mTime.equals("") || mTime.equals("0") || Integer.valueOf(mTime) < 0) {
            ToastUtils.showShortToast("请输入正确的天数");
            return;
        }
        if (keyControl) {
            keyControl = false;
            if (!isStart) {
                // 屏蔽按钮
                MyApplication.mUhfrManager.setCancleInventoryFilter();
                MyApplication.mUhfrManager.setCancleFastMode();
                mCleanBtnScan.setText("停止扫描");
                isStart = true;
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MyApplication.mUhfrManager.stopTagInventory();
                        isStart = false;
                        try {
                            Thread.sleep(200);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // 比对清洁时间
                        compare();

                    }
                }).start();
                mCleanBtnScan.setText("点击扫描");
            }
            keyControl = true;
        }
    }

    /**
     * 获取标签信息
     */
    private void getBoxInfo(final List<String> epc) {
        mRetrofitRequestHelper.cleanEnsureRequest(epc, new RetrofitRequestHelper.RetrofitRequestListener() {
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
                            CleanEnsureBoxInfo entryBoxInfo = new CleanEnsureBoxInfo();
                            entryBoxInfo.setTags(jsonObject1.getString("tags"));
                            entryBoxInfo.setSign(jsonObject1.getString("sign"));
                            entryBoxInfo.setLastClearTime(jsonObject1.getString("clear_time"));
                            entryBoxInfo.setMatternum(jsonObject1.getString("matternum"));
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
     * 比对
     */
    private boolean isNormal = true;

    private void compare() {
        long l = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Integer integer = Integer.valueOf(mTime);
        long time = integer * 24 * 60 * 60 * 1000;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCleanensureTvResult.setText("");
            }
        });
        for (int i = 0; i < mCleanBoxInfoList.size(); i++) {
            final CleanEnsureBoxInfo cleanEnsureBoxInfo = mCleanBoxInfoList.get(i);
            String lastClearTime = cleanEnsureBoxInfo.getLastClearTime();
            try {
                if (lastClearTime.equals("null") || lastClearTime.equals("")) {
                    SoundUtil.pasue();
                    SoundUtil.play(2, 0);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCleanensureTvResult.append("型号" + cleanEnsureBoxInfo.getSign() + "," + "标签号" + cleanEnsureBoxInfo.getTags() + "箱子从未清洁\n");
                        }
                    });
                    isNormal = false;
                    continue;
                }
                Date date = simpleDateFormat.parse(lastClearTime);
                long dateTime = date.getTime();
                if (l - dateTime > time) {
                    SoundUtil.pasue();
                    SoundUtil.play(2, 0);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCleanensureTvResult.append("型号" + cleanEnsureBoxInfo.getSign() + "," + "标签号" + cleanEnsureBoxInfo.getTags() + "箱子超过" + mTime + "天未清洁\n");
                        }
                    });
                    isNormal = false;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (isNormal) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCleanensureTvResult.setText("确认完毕，清洁正常");

                }
            });
        }
        isNormal = true;
    }
}
