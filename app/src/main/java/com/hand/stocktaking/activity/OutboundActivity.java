package com.hand.stocktaking.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.BRMicro.Tools;
import com.hand.stocktaking.R;
import com.hand.stocktaking.adapter.OutBoxInfo;
import com.hand.stocktaking.adapter.OutDialogBoxInfo;
import com.hand.stocktaking.adapter.OutDialogRcvAdapter;
import com.hand.stocktaking.adapter.OutRcvAdapter;
import com.hand.stocktaking.adapter.StorageBoxInfo;
import com.hand.stocktaking.retrofit.CompanyBean;
import com.hand.stocktaking.retrofit.RetrofitRequestHelper;
import com.hand.stocktaking.retrofit.StockoutBean;
import com.hand.stocktaking.utils.LogUtils;
import com.hand.stocktaking.utils.NetPrinter;
import com.hand.stocktaking.utils.SPUtils;
import com.hand.stocktaking.utils.SoundUtil;
import com.hand.stocktaking.utils.ToastUtils;
import com.hand.stocktaking.utils.Utils;
import com.hand.stocktaking.utils.ZxingUtils;
import com.uhf.api.cls.Reader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * @author huang
 */
public class OutboundActivity extends BaseActivity {

    @BindView(R.id.out_btn_scan)
    Button mOutBtnScan;
    @BindView(R.id.out_spinner_send_addr)
    Spinner mOutSpinnerSendAddr;
    @BindView(R.id.out_spinner_receive_addr)
    Spinner mOutSpinnerReceiveAddr;
    @BindView(R.id.out_spinner_company)
    Spinner mOutSpinnerCompany;
    @BindView(R.id.out_edt_plate)
    EditText mOutEdtPlate;
    @BindView(R.id.out_btn_out)
    Button mOutBtnOut;
    @BindView(R.id.out_tv_sum)
    TextView mOutTvSum;
    @BindView(R.id.out_tv_count)
    TextView mOutTvCount;
    @BindView(R.id.out_db_null)
    CheckBox mOutDbNull;
    @BindView(R.id.out_tv_result)
    TextView mOutTvResult;
    @BindView(R.id.out_rcv)
    RecyclerView mOutRcv;
    private Object mBoxInfo;

    private OutRcvAdapter mOutRcvAdapter;

    private String mOutAddress;
    private String mReceiveAddress;
    private String mCompany;
    private String plate;
    private boolean isBoxNull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outbound);
        ButterKnife.bind(this);

        new Thread(inventoryTask).start();
        initData();
        initView();

        mQrOrRfid = 1;

        mOutBtnScan.setClickable(false);
        mOutBtnOut.setClickable(false);
    }

    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mRetrofitRequestHelper.getCompanyRequest("1", new RetrofitRequestHelper.RetrofitRequestListener() {
                    @Override
                    public void requestSuccess(Response response) {
                        CompanyBean companyBean = (CompanyBean) response.body();
                        if (companyBean.getCode().equals("200")) {
                            List<CompanyBean.DataBean> companyBeanData = companyBean.getData();
                            List<CompanyBean.DataBean> dataBeanList = companyBean.getData();
                            nomalArrayList.addAll(dataBeanList);
                            final List<String> arrayList = new ArrayList<>();
                            String companyName = mSPUtils.getString(SPUtils.KEY_COMPANY_NAME);
                            int selectedPosition = 0;
                            for (int i = 0; i < dataBeanList.size(); i++) {
                                String name = dataBeanList.get(i).getName();
                                if (name.equals(companyName)) {
                                    selectedPosition = i;
                                }
                                arrayList.add(name);
                            }
                            ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(OutboundActivity.this, R.layout.support_simple_spinner_dropdown_item, arrayList);
                            mOutSpinnerSendAddr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    mOutAddress = arrayList.get(position);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
                            mOutSpinnerReceiveAddr.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    mReceiveAddress = arrayList.get(position);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
                            mOutSpinnerSendAddr.setAdapter(stringArrayAdapter);
                            mOutSpinnerReceiveAddr.setAdapter(stringArrayAdapter);
                            mOutSpinnerSendAddr.setSelection(selectedPosition);
                        } else {
                            mHandler.sendEmptyMessage(WHAT_QUERY_FAIL);
                        }
                    }

                    @Override
                    public void requestFail(Throwable t) {
                        mHandler.sendEmptyMessage(WHAT_QUERY_FAIL);
                    }
                });
                mRetrofitRequestHelper.getCompanyRequest("2", new RetrofitRequestHelper.RetrofitRequestListener() {
                    @Override
                    public void requestSuccess(Response response) {
                        CompanyBean companyBean = (CompanyBean) response.body();
                        if (companyBean.getCode().equals("200")) {
                            List<CompanyBean.DataBean> dataBeanList = companyBean.getData();
                            transArrayList.addAll(dataBeanList);
                            final List<String> arrayList = new ArrayList<>();
                            for (int i = 0; i < dataBeanList.size(); i++) {
                                String name = dataBeanList.get(i).getName();
                                arrayList.add(name);
                            }
                            ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(OutboundActivity.this, R.layout.support_simple_spinner_dropdown_item, arrayList);
                            mOutSpinnerCompany.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    mCompany = arrayList.get(position);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
                            mOutSpinnerCompany.setSelection(0);
                            mOutSpinnerCompany.setAdapter(stringArrayAdapter);
                            hideProgressDialog();
                            mOutBtnOut.setClickable(true);
                            mOutBtnScan.setClickable(true);
                        } else {
                            mHandler.sendEmptyMessage(WHAT_INTI_FAIL);
                        }
                    }

                    @Override
                    public void requestFail(Throwable t) {
                        mHandler.sendEmptyMessage(WHAT_INTI_FAIL);
                    }
                });
            }
        }).start();
    }

    private final List<CompanyBean.DataBean> transArrayList = new ArrayList<>();
    private final List<CompanyBean.DataBean> nomalArrayList = new ArrayList<>();

    @Override
    protected void onDestroy() {
        isRunning = false;
        isStart = false;
        super.onDestroy();
    }

    private void initView() {
        mOutRcv.setLayoutManager(new LinearLayoutManager(OutboundActivity.this));
        mOutRcvAdapter = new OutRcvAdapter();
        mOutRcv.setAdapter(mOutRcvAdapter);
    }

    /**
     * --------------------------------------------------------------RFID--------------------------------------------------------------------
     */
    private boolean keyControl = true;
    private boolean isRunning = true;
    private boolean isStart = false;

    private static final int WHAT_QUERY_SUCCESS = 0;
    private static final int WHAT_QUERY_NULL = 2;
    private static final int WHAT_QUERY_FAIL = 1;
    private static final int WHAT_INTI_FAIL = 4;
    private static final int WHAT_SCAN = 3;


    String epc;
    List<String> boxIdList = new ArrayList<>();
    public void getBoxInfo(final List<String> epc) {
        LogUtils.e("ecpStr = " + epc);
        LogUtils.e("mEpdList.size = " + mEpcList.size());
        mRetrofitRequestHelper.outOrderRequest(epc, new RetrofitRequestHelper.RetrofitRequestListener() {
            @Override
            public void requestSuccess(Response response) {
                LogUtils.e("success");
                ResponseBody responseBody = (ResponseBody) response.body();
                try {
                    String string = responseBody.string();
                    JSONObject jsonObject = new JSONObject(string);
                    LogUtils.e("code = " + jsonObject.getString("code"));
                    if ("200".equals(jsonObject.getString("code"))) {
                        JSONArray data = jsonObject.getJSONArray("data");
                        LogUtils.e("data.length = " + data.length());
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject jsonObject1 = data.getJSONObject(i);
                            String sign = jsonObject1.getString("sign");
                            String tags = jsonObject1.getString("tags");
                            String id = jsonObject1.getString("id");
                            boxIdList.add(id);
                            Integer integer = mSignMap.get(sign);
                            if (integer == null) {
                                mSignMap.put(sign, 1);
                            } else {
                                integer++;
                                mSignMap.put(sign, integer);
                            }
                            OutBoxInfo outBoxInfo = new OutBoxInfo();
                            outBoxInfo.setRfid(tags);
                            outBoxInfo.setBoxType(sign);
                            mServerEpcList.add(tags);
                            mBoxInfoList.add(outBoxInfo);
                            mHandler.sendEmptyMessage(WHAT_QUERY_SUCCESS);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void requestFail(Throwable t) {
                LogUtils.e("fail = " + t.getMessage());
                mHandler.sendEmptyMessage(WHAT_QUERY_FAIL);
            }
        });
    }

    /**
     * 通讯Handler
     */
    private OutHandler mHandler = new OutHandler(OutboundActivity.this);

    private static class OutHandler extends Handler {
        private WeakReference<OutboundActivity> mWeakReference;

        OutHandler(OutboundActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            OutboundActivity activity = mWeakReference.get();
            switch (msg.what) {
                case WHAT_QUERY_SUCCESS:
                    activity.mOutRcvAdapter.setList(activity.mBoxInfoList);
                    activity.mOutTvSum.setText(String.valueOf(activity.mBoxInfoList.size()));
                    Set<Map.Entry<String, Integer>> entries = activity.mSignMap.entrySet();
                    activity.mOutTvResult.setText("");
                    for (Map.Entry<String, Integer> entry :
                            entries) {
                        activity.mOutTvResult.append(entry.getKey()+"型号箱子 " + entry.getValue() + "\n");
                    }
                    SoundUtil.play(1, 0);
                    break;
                case WHAT_QUERY_FAIL:
                    break;
                case WHAT_QUERY_NULL:
                    break;
                case WHAT_SCAN:
                    activity.mOutTvCount.setText(String.valueOf(activity.mEpcList.size()));
                    SoundUtil.play(1, 0);
                    break;
                case WHAT_INTI_FAIL:

                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 扫描到的EPC集合
     */
    private List<String> mEpcList = new ArrayList<>();
    /**
     * 查询到的EPC集合
     */
    private List<String> mServerEpcList = new ArrayList<>();
    /**
     * recyclerview的集合
     */
    private List<OutBoxInfo> mBoxInfoList = new ArrayList<>();
    /**
     * 箱子型号集合
     */
    private ArrayMap<String, Integer> mSignMap = new ArrayMap<>();
    /**
     * inventory epc
     */
    private Runnable inventoryTask = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                if (isStart) {
//                    LogUtils.e("RFID scan run !");
                    List<Reader.TAGINFO> list1;
                    list1 = HomeActivity.mUhfrManager.tagInventoryByTimer((short) 50);
                    ArrayList<String> list = new ArrayList<>();
                    if (list1 != null && list1.size() > 0) {
                        for (Reader.TAGINFO tfs : list1) {
                            byte[] epcdata = tfs.EpcId;
                            epc = Tools.Bytes2HexString(epcdata, epcdata.length).trim();
                            if (!mEpcList.contains(epc)) {
                                list.add(epc);
                                mEpcList.add(epc);
                            }
                        }
                        mHandler.sendEmptyMessage(WHAT_SCAN);
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

    public void runInventory() {
        LogUtils.e("runInventory !");
        if (keyControl) {
            keyControl = false;
            if (!isStart) {
                HomeActivity.mUhfrManager.setCancleInventoryFilter();
                HomeActivity.mUhfrManager.setCancleFastMode();
                mOutBtnScan.setText(getResources().getString(R.string.storage_btn_scan_stop));
                isStart = true;
            } else {
                mOutBtnScan.setText(getResources().getString(R.string.storage_btn_scan));
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
                    }
                }).start();
            }
            keyControl = true;
        }
    }

    private AlertDialog dialog;
    @OnClick({R.id.out_btn_scan, R.id.out_btn_out})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.out_btn_scan:
                runInventory();
                break;
            case R.id.out_btn_out:
                String plate = mOutEdtPlate.getText().toString().trim();
                isBoxNull = mOutDbNull.isChecked();
                String userId = mSPUtils.getString(SPUtils.KEY_USER_ID);
                String companyId = "";
                for (int i = 0; i < transArrayList.size(); i++) {
                    if (mCompany.equals(transArrayList.get(i).getName())){
                        companyId = transArrayList.get(i).getId();
                    }
                }
                String boxIds = "";
                for (int i = 0; i < boxIdList.size(); i++) {
                    if (i != 0){
                        boxIds = boxIds.concat("," + boxIdList.get(i));
                    }else {
                        boxIds = boxIds.concat(boxIdList.get(i));
                    }
                }
                String sendId = "";
                String receiveId = "";
                LogUtils.e("size = " + nomalArrayList.size());
                for (int i = 0; i < nomalArrayList.size(); i++) {
                    CompanyBean.DataBean dataBean = nomalArrayList.get(i);
                    if (mOutAddress.equals(dataBean.getName())){
                        sendId = dataBean.getId();
                    }
                    if (mReceiveAddress.equals(dataBean.getName())){
                        receiveId = dataBean.getId();
                    }
                }
                LogUtils.e(boxIds + "");
                LogUtils.e(mOutAddress + "," + mReceiveAddress);
                if (mOutAddress!=null && mReceiveAddress!=null && mCompany!=null && !plate.equals("")){
                    showProgressDialog("正在生成订单信息....");
                    mRetrofitRequestHelper.stockOutRequest(sendId, receiveId, userId, !isBoxNull?"0":"1", companyId, plate, boxIds, new RetrofitRequestHelper.RetrofitRequestListener() {
                        @Override
                        public void requestSuccess(Response response) {
                            try {
                                final ResponseBody body1 = (ResponseBody) response.body();
                                final JSONObject body = new JSONObject(body1.string());
                                if (body.getString("code").equals("200")) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                final View view2 = View.inflate(OutboundActivity.this, R.layout.layout_out_dialog_ensure, null);
                                                final TextView orderIdTv = view2.findViewById(R.id.storage_ensure_tv_order_id);
                                                final TextView boxNullTv = view2.findViewById(R.id.out_ensure_tv_null_box);
                                                final TextView orderStatusTv = view2.findViewById(R.id.out_ensure_tv_status);
                                                final TextView sendTimeTv = view2.findViewById(R.id.out_ensure_tv_send_time);
                                                final TextView sendPersonTv = view2.findViewById(R.id.out_ensure_tv_send_person);
                                                final TextView sendAddressTv = view2.findViewById(R.id.out_ensure_tv_send_address);
                                                final TextView recvAddressTv = view2.findViewById(R.id.out_ensure_tv_recv_company);
                                                final TextView transCompanyTv = view2.findViewById(R.id.out_ensure_tv_trans_company);
                                                final TextView transPlateTv = view2.findViewById(R.id.out_ensure_tv_trans_plate);
                                                JSONObject dataBean = body.getJSONObject("data");
                                                final String orderId = dataBean.getString("orderid");
                                                final String boxNull;
                                                int bn = Integer.valueOf(dataBean.getString("is_emptybox"));
                                                if (bn == 0) {
                                                    boxNull = "否";
                                                } else {
                                                    boxNull = "是";
                                                }
                                                int os = Integer.valueOf(dataBean.getString("status"));
                                                String orderStatus = "未知";
                                                switch (os) {
                                                    case 0:
                                                        orderStatus = "已发货";
                                                        break;
                                                    case 2:
                                                        orderStatus = "已接收";
                                                        break;
                                                    case 1:
                                                        orderStatus = "异常处理中";
                                                        break;
                                                    case 4:
                                                        orderStatus = "异常处理成功";
                                                        break;
                                                    default:
                                                        break;
                                                }
                                                final String sendTime = dataBean.getString("send_time");
                                                final String sendPerson = mSPUtils.getString(SPUtils.KEY_USERNAME);
                                                final String sendAddress = dataBean.getString("send_company");
                                                final String recvAddress = dataBean.getString("recv_company");
                                                final String transCompany = dataBean.getString("trans_company");
                                                final String transPlate = dataBean.getString("trans_company_carnum");
                                                final String qrStr = dataBean.getString("code");
                                                final Bitmap bitmap = ZxingUtils.createBitmap(qrStr);
                                                LogUtils.e("qrStr = " + qrStr);
                                                final String finalOrderStatus = orderStatus;
                                                Set<Map.Entry<String, Integer>> entries = mSignMap.entrySet();
                                                final ArrayList<OutDialogBoxInfo> outBoxInfos = new ArrayList<>();
                                                for (Map.Entry<String, Integer> entry :
                                                        entries) {
                                                    OutDialogBoxInfo outBoxInfo = new OutDialogBoxInfo();
                                                    outBoxInfo.setBoxType(entry.getKey());
                                                    String num = String.valueOf(entry.getValue());
                                                    LogUtils.e("num = " + num);
                                                    outBoxInfo.setRfid(num);
                                                    outBoxInfos.add(outBoxInfo);
                                                }
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        dialog = new AlertDialog.Builder(OutboundActivity.this).create();
                                                        orderIdTv.setText(orderId);
                                                        boxNullTv.setText(boxNull);
                                                        orderStatusTv.setText(finalOrderStatus);
                                                        sendTimeTv.setText(sendTime);
                                                        sendPersonTv.setText(sendPerson);
                                                        sendAddressTv.setText(sendAddress);
                                                        recvAddressTv.setText(recvAddress);
                                                        transCompanyTv.setText(transCompany);
                                                        transPlateTv.setText(transPlate);
                                                        ImageView img = view2.findViewById(R.id.out_dialog_img);
                                                        img.setImageBitmap(bitmap);
                                                        RecyclerView recyclerView = view2.findViewById(R.id.out_dialog_rcv);
                                                        recyclerView.setLayoutManager(new LinearLayoutManager(OutboundActivity.this));
                                                        OutDialogRcvAdapter outDialogRcvAdapter = new OutDialogRcvAdapter();
                                                        recyclerView.setAdapter(outDialogRcvAdapter);
                                                        outDialogRcvAdapter.setList(outBoxInfos);
                                                        dialog.setView(view2);
                                                        dialog.show();
                                                        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
                                                        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                                        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                                                        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                ToastUtils.showShortToast("订单生成失败，未知错误");
                                            }
                                        }
                                    }).start();
                                } else {
                                    ToastUtils.showShortToast("订单生成失败，错误信息：" + body.getString("message"));
                                }
                                hideProgressDialog();
                            } catch (Exception e) {
                                e.printStackTrace();
                                ToastUtils.showShortToast("订单生成失败，未知错误");
                            }
                        }

                        @Override
                        public void requestFail(Throwable t) {
                            ToastUtils.showShortToast("订单生成失败，检查网络后重试");
                            hideProgressDialog();
                        }
                    });
                }else {
                    ToastUtils.showShortToast("请将信息填写完整");
                }
                break;
        }
    }
}
