package com.hand.stocktaking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hand.stocktaking.R;
import com.hand.stocktaking.retrofit.RetrofitRequestHelper;
import com.hand.stocktaking.retrofit.UserBean;
import com.hand.stocktaking.utils.LogUtils;
import com.hand.stocktaking.utils.SPUtils;
import com.hand.stocktaking.utils.SoundUtil;
import com.hand.stocktaking.utils.StringUtils;
import com.hand.stocktaking.utils.ToastUtils;
import com.hand.stocktaking.utils.Utils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Response;

/**
 * @author huang
 */
public class LoginActivity extends AppCompatActivity {

    public MaterialDialog mLoginDialog;
    /**
     * 登陆进度条
     */
    public MaterialDialog.Builder mLoginDialogBuilder;
    /**
     * SP存储工具
     */
    public SPUtils mSPUtils;
    @BindView(R.id.login_edt_username)
    EditText mLoginEdtUsername;
    @BindView(R.id.login_edt_password)
    EditText mLoginEdtPassword;
    @BindView(R.id.login_btn_login)
    Button mLoginBtnLogin;
    /**
     * 用户名和密码
     */
    private String mUserNameStr;
    private String mPasswordStr;
    /**
     * 登陆请求标记，10086-->成功，10087-->失败
     */
    private static final int LOGIN_SUCCESS = 10086;
    private static final int LOGIN_FAIL = 10087;
    /**
     * 登陆返回结果
     */
    private static String loginError = "login-error";
    private static String loginStatusError = "login.status-error";
    private static String failCode = "400";
    private static String successCode = "200";

    public RetrofitRequestHelper mRetrofitRequestHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        Utils.init(this);
        // 日志打印工具
        LogUtils.init(true, false, 'e', "Huang");
        // SP存储工具
        mSPUtils = new SPUtils(SPUtils.USER_FLAG);
        mLoginDialogBuilder = new MaterialDialog.Builder(this)
                .progress(true, 100)
                .cancelable(false);
        // 网络请求
        mRetrofitRequestHelper = RetrofitRequestHelper.getRetrofitRequestHelper();
        SoundUtil.initSoundPool(this);
        // 保持屏幕常亮
        //保持屏幕常亮
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null){
            supportActionBar.setTitle("登陆界面");
        }
    }

    private void initView() {
        // 获取上一次登陆用户名填充编辑框
        String lastUsername = mSPUtils.getString(SPUtils.KEY_USERNAME);
        mLoginEdtUsername.setText(lastUsername);
    }

    @Override
    protected void onResume() {
        mLoginEdtPassword.setText("");
        super.onResume();
    }

    /**
     * 获取用户名和密码
     */
    private void getNameAndPsd() {
        mUserNameStr = mLoginEdtUsername.getText().toString();
        mPasswordStr = mLoginEdtPassword.getText().toString();
    }

    /**
     * 登陆网络请求
     */
    private void loginRequest() {
        mRetrofitRequestHelper.loginRequest(mUserNameStr, mPasswordStr, new RetrofitRequestHelper.RetrofitRequestListener() {
            @Override
            public void requestSuccess(Response response) {
                UserBean userBean = (UserBean) response.body();
                LogUtils.e(userBean != null ? userBean.toString() : "userBean is null !");
                String message = userBean != null ? userBean.getMessage() : "login-error";
                String code = userBean != null ? userBean.getCode() : "500";
                if (successCode.equals(code)) {
                    List<UserBean.DataBean> beanData = userBean != null ? userBean.getData() : null;
                    UserBean.DataBean dataBean = beanData != null ? beanData.get(0) : null;
                    String username = dataBean != null ? dataBean.getUsername() : "未知";
                    ToastUtils.showShortToast("当前用户：" + username);
                    // 保存用户类型、密码、购买公司ID
                    mSPUtils.putString(SPUtils.KEY_USER_TYPE, dataBean != null ? dataBean.getType() : "未知");
                    mSPUtils.putString(SPUtils.KEY_USERNAME, mUserNameStr);
                    mSPUtils.putString(SPUtils.KEY_NAME, dataBean != null ? dataBean.getUsername() : "无");
                    mSPUtils.putString(SPUtils.KEY_COMPANY_ID, dataBean != null ? dataBean.getCompany_id() : "未知");
                    mSPUtils.putString(SPUtils.KEY_COMPANY_NAME, dataBean != null ? dataBean.getCompany_name() : "未知");
                    mSPUtils.putString(SPUtils.KEY_USER_ID, dataBean != null ? dataBean.getId() : "未知");
                    // 跳转至主界面
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    startActivity(new Intent(LoginActivity.this, MyApplication.class));
                                }
                            });
                        }
                    }).start();
                } else {
                    if ("login.jobnumber-error".equals(message)) {
                        ToastUtils.showShortToast("工号不存在");
                    } else if ("login.pwd-error".equals(message)) {
                        ToastUtils.showShortToast("密码不存在");
                    } else if ("login.pwd.length-error".equals(message)) {
                        ToastUtils.showShortToast("密码长度不是6-15");
                    }else if ("login.status-error".equals(message)) {
                        ToastUtils.showShortToast("该用户被禁");
                    }else if ("login-error".equals(message)) {
                        ToastUtils.showShortToast("登录失败");
                    }
                }
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
                if (error == null || timeout.equals(error) || error.contains(noAddress)) {
                    ToastUtils.showShortToast(R.string.login_net_error);
                }
                hideProgressDialog();
            }
        });
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
    @OnClick(R.id.login_btn_login)
    public void onViewClicked() {
        showProgressDialog("正在登陆....");
        getNameAndPsd();
        int passwordLength = StringUtils.length(mPasswordStr);
        int passwordMinLength = getResources().getInteger(R.integer.password_min_length);
        int passwordMaxLength = getResources().getInteger(R.integer.password_max_length);
        // 用户名和密码是否为空
        if (StringUtils.isEmpty(mUserNameStr) || StringUtils.isEmpty(mPasswordStr)) {
            ToastUtils.showShortToast(R.string.name_password_null);
            hideProgressDialog();
        }
        // 密码长度是否为6-15
        else if (passwordLength < passwordMinLength || passwordLength > passwordMaxLength) {
            ToastUtils.showShortToast(R.string.password_length_error);
            hideProgressDialog();
        } else {
            loginRequest();
        }
    }

}
