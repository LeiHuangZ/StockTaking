package com.hand.stocktaking.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hand.stocktaking.R;
import com.hand.stocktaking.retrofit.RetrofitRequestHelper;
import com.hand.stocktaking.utils.LogUtils;
import com.hand.stocktaking.utils.SPUtils;
import com.hand.stocktaking.utils.SoundUtil;
import com.hand.stocktaking.utils.Utils;

import java.io.IOException;

import cn.pda.scan.ScanThread;

/**
 * @author huang
 */
public class BaseActivity extends AppCompatActivity {
    /**
     * SP存储工具
     */
    public SPUtils mSPUtils;
    /**
     * 登陆进度条
     */
    public MaterialDialog.Builder mLoginDialogBuilder;
    public MaterialDialog mLoginDialog;
    public RetrofitRequestHelper mRetrofitRequestHelper;

    private KeyReceiver keyReceiver = new KeyReceiver();
    /**
     * 设备使用标记，0--二维,1--TFID
     */
    public int mQrOrRfid = 0;

    public ScanThread scanThread;
    /**
     * 按键广播接收者 用于接受按键广播 触发扫描
     */
    public class KeyReceiver extends BroadcastReceiver {

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
                            if (scanThread!=null) {
                                scanThread.scan();
                            }
                        }else {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.init(BaseActivity.this);
        // 日志打印工具
        LogUtils.init(true, false, 'e', "Huang");
        // SP存储工具
        mSPUtils = new SPUtils(SPUtils.USER_FLAG);
        mLoginDialogBuilder = new MaterialDialog.Builder(BaseActivity.this)
                .progress(true, 100)
                .cancelable(false);
        // 网络请求
        mRetrofitRequestHelper = RetrofitRequestHelper.getRetrofitRequestHelper();
        SoundUtil.initSoundPool(BaseActivity.this);
        // 保持屏幕常亮
        //保持屏幕常亮
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initUtils();
    }

    @Override
    protected void onDestroy() {//注销广播接收者
        try {
            unregisterReceiver(keyReceiver);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (scanThread != null) {
            scanThread.interrupt();
            scanThread.close();
        }
        super.onDestroy();
    }

    public void initUtils() {
            //注册按键广播接收者
            keyReceiver = new KeyReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.rfid.FUN_KEY");
            filter.addAction("android.intent.action.FUN_KEY");
            registerReceiver(keyReceiver, filter);
    }
    /**
     * 显示进度框
     */
    public void showProgressDialog(String msg) {
        if (mLoginDialogBuilder != null) {
            mLoginDialog = mLoginDialogBuilder.content(msg).build();
            mLoginDialog.show();
        }
    }

    /**
     * 隐藏
     */
    public void hideProgressDialog() {
        if (mLoginDialog != null) {
            mLoginDialog.dismiss();
        }
    }

    public void runInventory() {
        LogUtils.e("baseActivity runInventory !");
    }
}
