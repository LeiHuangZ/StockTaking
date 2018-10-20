package com.hand.stocktaking.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.BRMicro.Tools;
import com.hand.stocktaking.R;
import com.hand.stocktaking.adapter.StorageBoxInfo;
import com.hand.stocktaking.adapter.StorageRcvAdapter;
import com.hand.stocktaking.retrofit.PreorderBean;
import com.hand.stocktaking.retrofit.PreorderDetailsBean;
import com.hand.stocktaking.retrofit.RecordBean;
import com.hand.stocktaking.retrofit.RetrofitRequestHelper;
import com.hand.stocktaking.utils.LogUtils;
import com.hand.stocktaking.utils.SPUtils;
import com.hand.stocktaking.utils.SoundUtil;
import com.hand.stocktaking.utils.ToastUtils;
import com.uhf.api.cls.Reader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * @author huang
 */
public class StorageActivity extends BaseActivity {

    @BindView(R.id.storage_spinner_order_number)
    Spinner mStorageSpinnerOrderNumber;
    @BindView(R.id.storage_btn_scan)
    Button mStorageBtnScan;
    @BindView(R.id.storage_btn_record)
    Button mStorageBtnRecord;
    @BindView(R.id.storage_btn_ensure)
    Button mStorageBtnEnsure;
    @BindView(R.id.storage_tv_sum)
    TextView mStorageTvSum;
    @BindView(R.id.storage_tv_count)
    TextView mStorageTvCount;
    @BindView(R.id.storage_rcv)
    RecyclerView mStorageRcv;
    @BindView(R.id.storage_tv_result)
    TextView mStorageTvResult;
    TextView mStorageEnsureTvBuyCompany;
    TextView mStorageEnsureTvNeedTime;
    TextView mStorageEnsureTvBuyTime;
    EditText mStorageEnsureEdtForProject;
    EditText mStorageEnsureEdtSupplier;
    EditText mStorageEnsureEdtAttritionRate;
    EditText mStorageEnsureEdtUseLife;

    /**
     * 箱子总数
     */
    private int mSum;

    /**
     * 预定订单号集合
     */
    private List<String> mPreorderList = new ArrayList<>();

    /**
     * 箱子详情集合
     */
    private List<StorageBoxInfo> mInfoList = new ArrayList<>();
    /**
     * 从服务器获取箱子EPC号的集合
     */
    private List<String> mServerEpcList = new ArrayList<>();
    private List<ServerEpcInfo> mServerEpcInfoList = new ArrayList<>();
    private String mProject1;
    private String mSourcename;
    private String mLoss_rate;
    private String mLifetime;

    private static class ServerEpcInfo {
        private String epc;
        private int num;

        public String getEpc() {
            return epc;
        }

        public void setEpc(String epc) {
            this.epc = epc;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }
    }

    private StorageRcvAdapter mStorageRcvAdapter;

    private String mBuyOrderId;
    private String mProject;
    private String mSourceName;
    private String mLossRate;
    private String mLifeTime;
    private String mBuyCompany;
    private String mCompanyId;
    private String mUserId;
    private String mPreTime;
    private String mBuyTime;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        initView();

        mQrOrRfid = 1;

        new Thread(inventoryTask).start();
    }
    @Override
    public void setLayoutId() {
        mLayoutId = R.layout.activity_storage;
    }

    @Override
    public void setTitle() {
        mTitle = "购买入库";
    }

    @Override
    protected void onDestroy() {
        isRunning = false;
        isStart = false;
        super.onDestroy();
    }

    @SuppressLint("InflateParams")
    private void initView() {
        try {
            mStorageRcvAdapter = new StorageRcvAdapter();
            mStorageRcv.setLayoutManager(new LinearLayoutManager(StorageActivity.this));
            mStorageRcv.setAdapter(mStorageRcvAdapter);
            showProgressDialog(getString(R.string.getting_order_id));
            getPreorderSum();
            mStorageSpinnerOrderNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    showProgressDialog(getString(R.string.getting_order_detail_id));
                    String preorderId = mPreorderList.get(position);
                    mBuyOrderId = preorderId;
                    // 获取预定订单详情
                    getPreorderDetail(preorderId);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    ToastUtils.showShortToast("请选择预定订单号");
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void getPreorderSum() {
        try {
            // 重置箱子总数
            mSum = 0;
            mInfoList.clear();
            mServerEpcList.clear();
            mServerEpcInfoList.clear();
            mEpcList.clear();
            mRedundantEpcList.clear();
            mStorageTvCount.setText(String.valueOf(mEpcList.size()));
            mStorageTvResult.setText("");
            mPreorderList.clear();
            mStorageRcvAdapter.setList(mInfoList, false);
            String companyId = mSPUtils.getString(SPUtils.KEY_COMPANY_ID);
            mRetrofitRequestHelper.getPreorderRequest(companyId, new RetrofitRequestHelper.RetrofitRequestListener() {
                @Override
                public void requestSuccess(Response response) {
                    PreorderBean preorderBean = (PreorderBean) response.body();
                    List<PreorderBean.DataBean> dataBeanList = preorderBean != null ? preorderBean.getData() : new ArrayList<PreorderBean.DataBean>();
                    for (PreorderBean.DataBean dataBean :
                            dataBeanList) {
                        String buyorderid = dataBean.getBuyorderid();
                        mPreorderList.add(buyorderid);
                    }
                    ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(StorageActivity.this, R.layout.support_simple_spinner_dropdown_item, mPreorderList);
                    mStorageSpinnerOrderNumber.setAdapter(stringArrayAdapter);
                    hideProgressDialog();
                }

                @Override
                public void requestFail(Throwable t) {
                    if (t == null) {
                        ToastUtils.showShortToast(R.string.login_net_error);
                        return;
                    }
                    String error = t.getMessage();
                    String timeout = "timeout";
                    String noAddress = "Unable to resolve host";
                    LogUtils.e("loginRequest fail error = " + error);
                    if (error == null || timeout.equals(error) || error.contains(noAddress)) {
                        ToastUtils.showShortToast(R.string.login_net_error);
                    } else {
                        ToastUtils.showShortToast("未找到可用订单");
                    }
                    hideProgressDialog();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getPreorderDetail(String preorderId) {
        try {
            mInfoList.clear();
            mSum = 0;
            mRetrofitRequestHelper.getPreorderDetailsRequest(preorderId, new RetrofitRequestHelper.RetrofitRequestListener() {
                @Override
                public void requestSuccess(Response response) {
                    PreorderDetailsBean preorderDetailsBean = (PreorderDetailsBean) response.body();
                    PreorderDetailsBean.DataBean data = preorderDetailsBean != null ? preorderDetailsBean.getData() : null;
                    String buyCompany = data != null ? data.getBuycompanyname() : "无";
                    String pretime = data != null ? data.getPretime() : "无";
                    mProject1 = (String) data.getProject();
                    mSourcename = data.getSourcename();
                    mLoss_rate = (String) data.getLoss_rate();
                    mLifetime = (String) data.getLifetime();
                    mBuyCompany = buyCompany;
                    mPreTime = pretime;
                    mBuyTime = (String) (data != null ? data.getBuytime() : "无");
                    List<PreorderDetailsBean.DataBean.BoxnumBean> boxnumBeanList = data != null ? data.getBoxnum() : new ArrayList<PreorderDetailsBean.DataBean.BoxnumBean>();

                    for (int i = 0; i < boxnumBeanList.size(); i++) {
                        PreorderDetailsBean.DataBean.BoxnumBean boxnumBean = boxnumBeanList.get(i);
                        String boxNumStr = boxnumBean.getBox_num();
                        int boxNum = Integer.valueOf(boxNumStr);
                        mSum += boxNum;
                        String boxPrice = boxnumBean.getBox_price();
                        String boxType = boxnumBean.getBox_sign();
                        List<String> tags = boxnumBean.getTags();
                        for (String tag :
                                tags) {
                            StorageBoxInfo info = new StorageBoxInfo();
                            info.setRfid(tag);
                            if (mServerEpcList.contains(tag)) {
                                List<ServerEpcInfo> serverEpcInfoList = new ArrayList<>();
                                int num = 1;
                                for (int j = 0; j < mServerEpcInfoList.size(); j++) {
                                    ServerEpcInfo epcInfo = new ServerEpcInfo();
                                    if (tag.equals(mServerEpcInfoList.get(j).getEpc())) {
                                        num = mServerEpcInfoList.get(j).getNum() + 1;
                                    }
                                    epcInfo.setNum(num);
                                    serverEpcInfoList.add(epcInfo);
                                }
                                mServerEpcInfoList.clear();
                                mServerEpcInfoList.addAll(serverEpcInfoList);
                                info.setBoxNum(num + "");
                            } else {
                                ServerEpcInfo epcInfo = new ServerEpcInfo();
                                epcInfo.setEpc(tag);
                                epcInfo.setNum(1);
                                mServerEpcInfoList.add(epcInfo);
                                info.setBoxNum("1");
                                mServerEpcList.add(tag);
                            }
                            info.setBuyCompany(buyCompany);
                            info.setBoxPrice(boxPrice);
                            info.setBoxType(boxType);
                            info.setPreTime(pretime);
                            mInfoList.add(info);
                        }
                    }
                    LogUtils.e("sum = " + mSum);
                    mStorageTvSum.setText(String.valueOf(mSum));
                    mStorageRcvAdapter.setList(mInfoList, false);
                    hideProgressDialog();
                }

                @Override
                public void requestFail(Throwable t) {
                    if (t == null){
                        ToastUtils.showShortToast(R.string.login_net_error);
                        return;
                    }
                    String error = t.getMessage();
                    String timeout = "timeout";
                    String noAddress = "Unable to resolve host";
                    LogUtils.e("loginRequest fail error = " + error);
                    if (timeout.equals(error) || error.contains(noAddress)) {
                        ToastUtils.showShortToast(R.string.login_net_error);
                    } else {
                        ToastUtils.showShortToast(R.string.get_details_erro);
                    }
                    mStorageTvSum.setText(String.valueOf(mSum));
                    mStorageRcvAdapter.setList(mInfoList, false);
                    hideProgressDialog();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * --------------------------------------------------------------RFID--------------------------------------------------------------------
     */
    private boolean keyControl = true;
    private boolean isRunning = true;
    private boolean isStart = false;

    private static int WHAT_RECORD_SUCCESS = 3;
    private static int WHAT_RECORD_FAIL = 4;
    private static int WHAT_ENSURE_FAIL = 5;
    private static int WHAT_ENSURE_SUCCESS = 6;
    String epc;
    /**
     * 通讯Handler
     */
    private StorageHandler mHandler = new StorageHandler(StorageActivity.this);

    private static class StorageHandler extends Handler {
        private WeakReference<StorageActivity> mWeakReference;

        StorageHandler(StorageActivity storageActivity) {
            mWeakReference = new WeakReference<>(storageActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            final StorageActivity storageActivity = mWeakReference.get();
            if (msg.what == 2) {
                String result = (String) msg.obj;
                storageActivity.mStorageTvResult.setText(result);
                storageActivity.hideProgressDialog();
                storageActivity.mStorageBtnScan.setText(storageActivity.getResources().getString(R.string.storage_btn_scan));
                storageActivity.mStorageRcvAdapter.setList(storageActivity.mInfoList, true);
                SoundUtil.play(1, 0);
            }else if (msg.what == WHAT_RECORD_SUCCESS){
                storageActivity.hideProgressDialog();
                ToastUtils.showShortToast("记录订单信息成功");
                storageActivity.dialog.dismiss();
                storageActivity.isRecord = true;
            }else if (msg.what == WHAT_RECORD_FAIL){
                storageActivity.hideProgressDialog();
                String obj = (String) msg.obj;
                if (obj == null){
                    ToastUtils.showShortToast("订单信息记录失败，请检查网络及输入信息后重试");
                }else {
                    ToastUtils.showShortToast("订单信息记录失败，错误信息：" + obj);
                }
            }
            else if (msg.what == WHAT_ENSURE_FAIL){
                storageActivity.hideProgressDialog();
                String obj = (String) msg.obj;
                if (obj == null){
                    ToastUtils.showShortToast("订单信息确认失败，请检查网络及输入信息后重试");
                }else {
                    ToastUtils.showShortToast("订单信息确认失败，错误信息：" + obj);
                }
            }
            else if (msg.what == WHAT_ENSURE_SUCCESS){
                ToastUtils.showShortToast("订单信息确认成功");
                storageActivity.hideProgressDialog();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            storageActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    storageActivity.getPreorderSum();
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            else {
                storageActivity.mStorageTvCount.setText(String.valueOf(storageActivity.mEpcList.size()));
                SoundUtil.play(1, 0);
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 扫描到的EPC集合
     */
    private List<String> mEpcList = new ArrayList<>();
    /**
     * 多出的EPC集合
     */
    private List<String> mRedundantEpcList = new ArrayList<>();
    /**
     * 订单信息是否已经记录
     */
    private boolean isRecord = false;
    /**
     * 是否异常
     */
    private boolean isException = true;
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
                            epc = Tools.Bytes2HexString(epcdata, epcdata.length).trim();
                            if (!mEpcList.contains(epc)) {
                                mEpcList.add(epc);
                                if (mServerEpcList.contains(epc)) {
                                    List<StorageBoxInfo> list = new ArrayList<>();
                                    for (int i = 0; i < mInfoList.size(); i++) {
                                        StorageBoxInfo info = mInfoList.get(i);
                                        if (mInfoList.get(i).getRfid().equals(epc)) {
                                            info.setResolved(true);
                                        }
                                        list.add(info);
                                    }
                                    mInfoList.clear();
                                    mInfoList.addAll(list);
                                } else {
                                    mRedundantEpcList.add(epc);
                                }
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
                mStorageBtnEnsure.setClickable(false);
                mStorageBtnRecord.setClickable(false);
                MyApplication.mUhfrManager.setCancleInventoryFilter();
                MyApplication.mUhfrManager.setCancleFastMode();
                mStorageBtnScan.setText(getResources().getString(R.string.storage_btn_scan_stop));
                isStart = true;
            } else {
                isStart = false;
                showProgressDialog(getString(R.string.comparing_order_info));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MyApplication.mUhfrManager.stopTagInventory();
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // 比对扫描到的标签和从服务器获取的标签数
                        compareTags();
                    }
                }).start();
                mStorageBtnRecord.setClickable(true);
                mStorageBtnEnsure.setClickable(true);
            }
            keyControl = true;
        }
    }

    private void compareTags() {
        String string = "";
        if (mRedundantEpcList.size() != 0) {
            if (mServerEpcList.size() == (mEpcList.size() - mRedundantEpcList.size())) {
                string = "扫描不通过！\n多出：";
                for (int i = 0; i < mRedundantEpcList.size(); i++) {
                    string = string.concat("\n");
                    string = string.concat(mRedundantEpcList.get(i));
                }
            } else {
                string = "扫描不通过！\n缺少：";
                for (int i = 0; i < mInfoList.size(); i++) {
                    StorageBoxInfo info = mInfoList.get(i);
                    if (!info.isResolved()) {
                        string = string.concat("\n");
                        string = string.concat(mInfoList.get(i).getRfid());
                    }
                }
                string = string.concat("\n");
                string = string.concat("多出");
                for (int i = 0; i < mRedundantEpcList.size(); i++) {
                    string = string.concat("\n");
                    string = string.concat(mRedundantEpcList.get(i));
                }
            }
        } else {
            if (mServerEpcList.size() == (mEpcList.size() - mRedundantEpcList.size())) {
                string = "扫描通过！";
                isException = false;
            } else {
                string = "扫描不通过！\n缺少：";
                for (int i = 0; i < mInfoList.size(); i++) {
                    StorageBoxInfo info = mInfoList.get(i);
                    if (!info.isResolved()) {
                        string = string.concat("\n");
                        string = string.concat(mInfoList.get(i).getRfid());
                    }
                }
            }
        }
        Message message = new Message();
        message.obj = string;
        message.what = 2;
        mHandler.sendMessage(message);
    }

    @OnClick({R.id.storage_btn_scan, R.id.storage_btn_record, R.id.storage_btn_ensure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.storage_btn_scan:
                runInventory();
                break;
            case R.id.storage_btn_record:
                if (mEpcList.size() == 0) {
                    ToastUtils.showShortToast("请先进行扫描！");
                    return;
                }
                dialog = new AlertDialog.Builder(StorageActivity.this).create();
                View view2 = View.inflate(this, R.layout.layout_storage_dialog_ensure, null);
                mStorageEnsureTvBuyCompany = view2.findViewById(R.id.storage_ensure_tv_buy_company);
                mStorageEnsureTvNeedTime = view2.findViewById(R.id.storage_ensure_tv_need_time);
                mStorageEnsureTvBuyTime = view2.findViewById(R.id.storage_ensure_tv_buy_time);
                mStorageEnsureEdtForProject = view2.findViewById(R.id.storage_ensure_edt_for_project);
                mStorageEnsureEdtSupplier = view2.findViewById(R.id.storage_ensure_edt_supplier);
                mStorageEnsureEdtAttritionRate = view2.findViewById(R.id.storage_ensure_edt_attrition_rate);
                mStorageEnsureEdtUseLife = view2.findViewById(R.id.storage_ensure_edt_use_life);
                view2.findViewById(R.id.storage_ensure_btn_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showProgressDialog("正在记录订单信息");
                        recordInfo();
                    }
                });
                view2.findViewById(R.id.storage_ensure_btn_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                mStorageEnsureTvBuyCompany.setText(mBuyCompany);
                mStorageEnsureTvBuyTime.setText(mBuyTime);
                mStorageEnsureTvNeedTime.setText(mPreTime);
                mStorageEnsureEdtForProject.setText(mProject1);
                mStorageEnsureEdtSupplier.setText(mSourcename);
                mStorageEnsureEdtAttritionRate.setText(mLoss_rate);
                mStorageEnsureEdtUseLife.setText(mLifetime);
                if (mProject != null){
                    mStorageEnsureEdtForProject.setText(mProject);
                    mStorageEnsureEdtSupplier.setText(mSourceName);
                    mStorageEnsureEdtAttritionRate.setText(mLossRate);
                    mStorageEnsureEdtUseLife.setText(mLifeTime);
                }
                dialog.setView(view2);
                dialog.show();
                break;
            case R.id.storage_btn_ensure:
                ensureOrder();
                break;
            default:
                break;
        }
    }

    private void ensureOrder() {
        LogUtils.e(mBuyOrderId+","+mUserId);
        if (isException){
            final AlertDialog alertDialog = new AlertDialog.Builder(StorageActivity.this)
                    .setMessage("是否异常入库？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (isRecord) {
                                showProgressDialog("正在确认订单....");
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = 0; i < mEpcList.size(); i++) {
                                            LogUtils.e("epc = " + mEpcList.get(i));
                                        }
                                        String[] epcStrings = new String[mEpcList.size()];
                                        String[] strings = mEpcList.toArray(epcStrings);
                                        mRetrofitRequestHelper.ensureOrderRequest(mBuyOrderId, mUserId, mEpcList, "2", new RetrofitRequestHelper.RetrofitRequestListener() {
                                            @Override
                                            public void requestSuccess(Response response) {
                                                ResponseBody body = ((ResponseBody) response.body());
                                                try {
                                                    String jsonStr = body.string();
                                                    JSONObject jsonObject = new JSONObject(jsonStr);
                                                    String code = jsonObject.getString("code");
                                                    String message = jsonObject.getString("message");
                                                    LogUtils.e("ensure, code = " + code);
                                                    LogUtils.e("ensure, message = " + message);
                                                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                                                    if (code.equals("200")){
                                                        mHandler.sendEmptyMessage(WHAT_ENSURE_SUCCESS);
                                                    }else {
                                                        String erro = "";
                                                        if (message.equals("preOrdersub.orderid-error")){
                                                            erro = "订单号不存在";
                                                        }else if (message.equals("preOrdersub.tagslist-error")){
                                                            erro = "标签数组不存在";
                                                        }else if (message.equals("preOrdersub.type-error")){
                                                            erro = "订单类型不存在";
                                                        }else if (message.equals("preOrdersub.userid-error")){
                                                            erro = "用户id不存在";
                                                        }else if (message.equals("preOrdersub.taglist.unique-error")){
                                                            erro = "有重复标签";
                                                        }else if (message.equals("preOrdersub.orderinfo-error")){
                                                            erro = "没有这个订单";
                                                        }else if (message.equals("preOrdersub.tags-error")){
                                                            erro = "后台未录入标签";
                                                        }else if (message.equals("preOrdersub.tag.type-error")){
                                                            erro = "订单类型与标签数组之间验证错误";
                                                        }else if (message.equals("preOrdersub.type.num-error")){
                                                            erro = "订单类型错误";
                                                        }else if (message.equals("preOrdersub-error")){
                                                            erro = "订单提交失败";
                                                        }else if (message.equals("preOrdersub.tag.buy-error")){
                                                            erro = "该标签已被购买：";
                                                            int length = jsonArray.length();
                                                            LogUtils.e("length = " + length);
                                                            for (int i = 0; i < length; i++) {
                                                                erro = erro.concat((String) jsonArray.get(i));
                                                            }
                                                        }else if (message.equals("preOrdersub.tag.prebuy-error")){
                                                            erro = "该标签已被预订";
                                                            int length = jsonArray.length();
                                                            LogUtils.e("length = " + length);
                                                            for (int i = 0; i < length; i++) {
                                                                erro = erro.concat((String) jsonArray.get(i));
                                                            }
                                                        }
                                                        Message message1 = new Message();
                                                        message1.obj = erro;
                                                        message1.what = WHAT_ENSURE_FAIL;
                                                        mHandler.sendMessage(message1);
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    mHandler.sendEmptyMessage(WHAT_ENSURE_FAIL);
                                                }
                                            }

                                            @Override
                                            public void requestFail(Throwable t) {
                                                if (t == null){
                                                    mHandler.sendEmptyMessage(WHAT_ENSURE_FAIL);
                                                    return;
                                                }
                                                LogUtils.e(t.getMessage());
                                                mHandler.sendEmptyMessage(WHAT_ENSURE_FAIL);
                                            }
                                        });

                                    }
                                }).start();
                            }else {
                                ToastUtils.showShortToast("请先记录订单信息");
                            }
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
        }else {
            if (isRecord) {
                showProgressDialog("正在确认订单....");
                for (int i = 0; i < mEpcList.size(); i++) {
                    LogUtils.e("确认订单数组"+ mEpcList.get(i));
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String[] epcStrings = new String[mEpcList.size()];
                        String[] strings = mEpcList.toArray(epcStrings);
                        mRetrofitRequestHelper.ensureOrderRequest(mBuyOrderId, mUserId, mEpcList, "1", new RetrofitRequestHelper.RetrofitRequestListener() {
                            @Override
                            public void requestSuccess(Response response) {
                                ResponseBody body = ((ResponseBody) response.body());
                                try {
                                    String jsonStr = body.string();
                                    JSONObject jsonObject = new JSONObject(jsonStr);
                                    String code = jsonObject.getString("code");
                                    if (code.equals("200")){
                                        mHandler.sendEmptyMessage(WHAT_ENSURE_SUCCESS);
                                    }else {
                                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                                        String message = jsonObject.getString("message");
                                        String erro = "";
                                        if (message.equals("preOrdersub.orderid-error")){
                                            erro = "订单号不存在";
                                        }else if (message.equals("preOrdersub.tagslist-error")){
                                            erro = "标签数组不存在";
                                        }else if (message.equals("preOrdersub.type-error")){
                                            erro = "订单类型不存在";
                                        }else if (message.equals("preOrdersub.userid-error")){
                                            erro = "用户id不存在";
                                        }else if (message.equals("preOrdersub.taglist.unique-error")){
                                            erro = "有重复标签";
                                        }else if (message.equals("preOrdersub.orderinfo-error")){
                                            erro = "没有这个订单";
                                        }else if (message.equals("preOrdersub.tags-error")){
                                            erro = "后台未录入标签";
                                        }else if (message.equals("preOrdersub.tag.type-error")){
                                            erro = "订单类型与标签数组之间验证错误";
                                        }else if (message.equals("preOrdersub.type.num-error")){
                                            erro = "订单类型错误";
                                        }else if (message.equals("preOrdersub-error")){
                                            erro = "订单提交失败";
                                        }else if (message.equals("preOrdersub.tag.buy-error")){
                                            erro = "该标签已被购买：";
                                            int length = jsonArray.length();
                                            LogUtils.e("length = " + length);
                                            for (int i = 0; i < length; i++) {
                                                erro = erro.concat((String) jsonArray.get(i));
                                            }
                                        }else if (message.equals("preOrdersub.tag.prebuy-error")){
                                            erro = "该标签已被预订";
                                            int length = jsonArray.length();
                                            LogUtils.e("length = " + length);
                                            for (int i = 0; i < length; i++) {
                                                erro = erro.concat((String) jsonArray.get(i));
                                            }
                                        }
                                        Message message1 = new Message();
                                        message1.obj = erro;
                                        message1.what = WHAT_ENSURE_FAIL;
                                        mHandler.sendMessage(message1);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mHandler.sendEmptyMessage(WHAT_ENSURE_FAIL);
                                }
                            }

                            @Override
                            public void requestFail(Throwable t) {
                                if (t == null){
                                    mHandler.sendEmptyMessage(WHAT_ENSURE_FAIL);
                                    return;
                                }
                                LogUtils.e(t.getMessage());
                                mHandler.sendEmptyMessage(WHAT_ENSURE_FAIL);
                            }
                        });

                    }
                }).start();
            }else {
                ToastUtils.showShortToast("请先记录订单信息");
            }
        }

    }

    private void recordInfo() {
        mProject = mStorageEnsureEdtForProject.getText().toString();
        mSourceName = mStorageEnsureEdtSupplier.getText().toString();
        mLossRate = mStorageEnsureEdtAttritionRate.getText().toString();
        mLifeTime = mStorageEnsureEdtUseLife.getText().toString();
        mCompanyId = mSPUtils.getString(SPUtils.KEY_COMPANY_ID);
        mUserId = mSPUtils.getString(SPUtils.KEY_USER_ID);
        LogUtils.e(mBuyOrderId+","+mProject+","+mSourceName+","+mLossRate+","+mLifeTime+","+mCompanyId+","+mUserId);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mRetrofitRequestHelper.recordRoderCall(mBuyOrderId, mProject, mSourceName, mLossRate, mLifeTime, mCompanyId, mUserId, new RetrofitRequestHelper.RetrofitRequestListener() {
                    @Override
                    public void requestSuccess(Response response) {
                        ResponseBody responseBody = (ResponseBody) response.body();
                        try {
                            String body = responseBody.string();
                            JSONObject jsonObject = new JSONObject(body);
                            String code = jsonObject.getString("code");
                            if ("200".equals(code)){
                                mHandler.sendEmptyMessage(WHAT_RECORD_SUCCESS);
                            }else {
                                String str = jsonObject.getString("message");
                                String error = "";
                                if (str.equals("editOrderinfo.buyorderid-error")){
                                    error = "购买订单号不存在";
                                }else if (str.equals("editOrderinfo.project-error")){
                                    error = "所为项目不存在";
                                }else if (str.equals("editOrderinfo.sourcename-error")){
                                    error = "供应商不存在";
                                }else if (str.equals("editOrderinfo.loss_rate-error")){
                                    error = "损耗率不存在";
                                }else if (str.equals("editOrderinfo.lifetime-error")){
                                    error = "使用寿命不存在";
                                }else if (str.equals("editOrderinfo.company_id-error")){
                                    error = "公司id不存在";
                                }else if (str.equals("editOrderinfo.user_id-error")){
                                    error = "用户id不存在";
                                }else if (str.equals("editOrderinfo.loss_rate.type-error")){
                                    error = "损耗率错误（范围0-100）";
                                }else if (str.equals("editOrderinfo.lifetime.type-error")){
                                    error = "使用寿命错误";
                                }else if (str.equals("editOrderinfo-error")){
                                    error = "记录信息失败";
                                }
                                LogUtils.e("记录订单信息失败，原因：" + error);
                                Message message = new Message();
                                message.what = WHAT_RECORD_FAIL;
                                message.obj = error;
                                mHandler.sendMessage(message);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void requestFail(Throwable t) {
                        if (t == null){
                            return;
                        }
                        LogUtils.e(t.getMessage());
                        mHandler.sendEmptyMessage(WHAT_RECORD_FAIL);
                    }
                });
            }
        }).start();
    }
}
