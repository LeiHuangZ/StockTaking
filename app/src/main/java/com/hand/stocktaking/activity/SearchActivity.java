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
import com.hand.stocktaking.adapter.SearchBoxInfo;
import com.hand.stocktaking.adapter.SearchRcvAdapter;
import com.hand.stocktaking.retrofit.MatterBean;
import com.hand.stocktaking.retrofit.RetrofitRequestHelper;
import com.hand.stocktaking.utils.LogUtils;
import com.hand.stocktaking.utils.SoundUtil;
import com.hand.stocktaking.utils.ToastUtils;
import com.uhf.api.cls.Reader;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class SearchActivity extends BaseActivity {

    @BindView(R.id.matter_btn_scan)
    Button mMatterBtnScan;
    @BindView(R.id.search_edt_matternum)
    EditText mSearchEdtMatternum;
    @BindView(R.id.search_rv)
    RecyclerView mSearchRv;
    @BindView(R.id.search_tv_count)
    TextView mSearchTvCount;
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
//                    LogUtils.e("RFID scan run !");
                    List<Reader.TAGINFO> list1;
                    list1 = HomeActivity.mUhfrManager.tagInventoryByTimer((short) 50);
                    if (list1 != null && list1.size() > 0) {
                        for (Reader.TAGINFO tfs : list1) {
                            byte[] epcdata = tfs.EpcId;
                            String epc = Tools.Bytes2HexString(epcdata, epcdata.length);
                            LogUtils.e("Found epc = " + epc);
                            SoundUtil.play(1, 0);
                            if (!mFoundEpcStrList.contains(epc)) {
                                mFoundEpcStrList.add(epc);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mSearchTvCount.setText(String.valueOf(mFoundEpcStrList.size()));
                                    }
                                });
                                getBoxInfo(epc);
                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
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
    private List<SearchBoxInfo> mSearchBoxInfoList = new ArrayList<>();
    /**
     * 适配器
     */
    private SearchRcvAdapter mSearchRcvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        mSearchRcvAdapter = new SearchRcvAdapter();
        mSearchRv.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        mSearchRv.setAdapter(mSearchRcvAdapter);

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

    @OnClick(R.id.matter_btn_scan)
    public void onViewClicked() {
        mMatternum = mSearchEdtMatternum.getText().toString();
        if (mMatternum.equals("")) {
            ToastUtils.showShortToast("请输入物料号");
            return;
        }
        runInventory();
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
                HomeActivity.mUhfrManager.setCancleInventoryFilter();
                HomeActivity.mUhfrManager.setCancleFastMode();
                mMatterBtnScan.setText("停止扫描");
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
                mMatterBtnScan.setText("点击扫描");
            }
            keyControl = true;
        }
    }

    /**
     * 获取标签信息
     */
    private void getBoxInfo(final String epc) {
        mRetrofitRequestHelper.searchOrderRequest(epc, new RetrofitRequestHelper.RetrofitRequestListener() {
            @Override
            public void requestSuccess(Response response) {
                ResponseBody responseBody = (ResponseBody) response.body();
                try {
                    String body = responseBody.string();
                    JSONObject jsonObject = new JSONObject(body);
                    String code = jsonObject.getString("code");
                    LogUtils.e("getBoxInfo.code = " + code);
                    if (code.equals("200")) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        SearchBoxInfo entryBoxInfo = new SearchBoxInfo();
                        entryBoxInfo.setId(data.getString("id"));
                        entryBoxInfo.setTags(data.getString("tags"));
                        entryBoxInfo.setSign(data.getString("sign"));
                        entryBoxInfo.setMattertype(data.getString("mattertype"));
                        entryBoxInfo.setMattername(data.getString("mattername"));
                        entryBoxInfo.setMatternum(data.getString("matternum"));
                        if (data.getString("matternum").equals(mMatternum)) {
                            entryBoxInfo.setIsMatch(true);
                            SoundUtil.play(2, 0);
                        } else {
                            entryBoxInfo.setIsMatch(false);
                        }
                        mSearchBoxInfoList.add(entryBoxInfo);
                        mSearchRcvAdapter.setList(mSearchBoxInfoList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void requestFail(Throwable t) {
                mFoundEpcStrList.remove(epc);
            }
        });
    }
}
