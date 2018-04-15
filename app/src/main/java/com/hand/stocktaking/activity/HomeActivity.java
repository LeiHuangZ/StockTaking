package com.hand.stocktaking.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hand.stocktaking.R;
import com.hand.stocktaking.utils.SPUtils;
import com.hand.stocktaking.utils.ToastUtils;
import com.hand.stocktaking.utils.Utils;
import com.handheld.uhfr.UHFRManager;
import com.uhf.api.cls.Reader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author huang
 */
public class HomeActivity extends BaseActivity {

    @BindView(R.id.home_btn_storage)
    Button mHomeBtnStorage;
    @BindView(R.id.home_btn_delivery)
    Button mHomeBtnDelivery;
    @BindView(R.id.home_btn_doorkeeper)
    Button mHomeBtnDoorkeeper;
    @BindView(R.id.home_btn_harvest)
    Button mHomeBtnHarvest;
    @BindView(R.id.home_btn_inventory)
    Button mHomeBtnInventory;
    @BindView(R.id.home_btn_clean)
    Button mHomeBtnClean;
    @BindView(R.id.home_btn_clean_check)
    Button mHomeBtnCleanCheck;
    @BindView(R.id.home_btn_sap)
    Button mHomeBtnSap;
    private SPUtils mSPUtils;
    private String mUserType;
    private int mUserTypeInt;
    public static UHFRManager mUhfrManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        initUtils();
        initView();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (mUhfrManager != null) {
            mUhfrManager.close();
            mUhfrManager = null;
        }
        super.onDestroy();
    }

    public void initUtils() {
        Utils.init(HomeActivity.this);
        mSPUtils = new SPUtils(SPUtils.USER_FLAG);
        mUserType = mSPUtils.getString(SPUtils.KEY_USER_TYPE);
        mUserTypeInt = Integer.valueOf(mUserType);
        mUhfrManager = UHFRManager.getIntance(HomeActivity.this);
        if(mUhfrManager!=null){
            mUhfrManager.setRegion(Reader.Region_Conf.valueOf(1));
            mUhfrManager.setPower(25, 25);
            ToastUtils.showShortToast(R.string.init_rfid_success);
        }else {
            ToastUtils.showShortToast(R.string.init_rfid_fail);
        }
    }

    private void initView() {
        switch (mUserTypeInt) {
            case 0:
            case 1:
            case 2:
                break;
            case 3:
                initViewForDoorkeeper();
                break;
            case 4:
                initViewForClean();
                break;
            default:
                break;

        }
    }

    private void initViewForDoorkeeper(){
        mHomeBtnStorage.setVisibility(View.GONE);
        mHomeBtnDelivery.setVisibility(View.GONE);
        mHomeBtnClean.setVisibility(View.GONE);
        mHomeBtnHarvest.setVisibility(View.GONE);
        mHomeBtnInventory.setVisibility(View.GONE);
        mHomeBtnCleanCheck.setVisibility(View.GONE);
        mHomeBtnSap.setVisibility(View.GONE);
    }

    private void initViewForClean(){
        mHomeBtnStorage.setVisibility(View.GONE);
        mHomeBtnDelivery.setVisibility(View.GONE);
        mHomeBtnDoorkeeper.setVisibility(View.GONE);
        mHomeBtnHarvest.setVisibility(View.GONE);
        mHomeBtnInventory.setVisibility(View.GONE);
        mHomeBtnCleanCheck.setVisibility(View.GONE);
        mHomeBtnSap.setVisibility(View.GONE);
    }
    @OnClick({R.id.home_btn_storage, R.id.home_btn_delivery, R.id.home_btn_doorkeeper, R.id.home_btn_harvest, R.id.home_btn_inventory, R.id.home_btn_clean, R.id.home_btn_clean_check, R.id.home_btn_sap})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.home_btn_storage:
                startActivity(new Intent(HomeActivity.this, StorageActivity.class));
                break;
            case R.id.home_btn_delivery:
                startActivity(new Intent(HomeActivity.this, OutboundActivity.class));
                break;
            case R.id.home_btn_doorkeeper:
                startActivity(new Intent(HomeActivity.this, DoorActivity.class));
                break;
            case R.id.home_btn_harvest:
                startActivity(new Intent(HomeActivity.this, ReceiveActivity.class));
                break;
            case R.id.home_btn_inventory:
                startActivity(new Intent(HomeActivity.this, EntryActivity.class));
                break;
            case R.id.home_btn_clean:
                startActivity(new Intent(HomeActivity.this, CleanActivity.class));
                break;
            case R.id.home_btn_clean_check:
                startActivity(new Intent(HomeActivity.this, CleanEnsuerActivity.class));
                break;
            case R.id.home_btn_sap:
                break;
            default:
                break;
        }
    }
}
