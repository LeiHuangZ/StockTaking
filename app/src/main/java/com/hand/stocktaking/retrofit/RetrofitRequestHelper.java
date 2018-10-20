package com.hand.stocktaking.retrofit;

import android.support.annotation.NonNull;

import com.hand.stocktaking.utils.LogUtils;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;

public class RetrofitRequestHelper {
    public static RetrofitRequestHelper sRetrofitRequestHelper;
    private static RetrofitInterface sRetrofitInterface;
    private RetrofitRequestHelper(){}
    public static RetrofitRequestHelper getRetrofitRequestHelper(){
        // 创建Retrofit对象
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://wechat.geek-q.cc/stock/App/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().addNetworkInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(@NonNull String message) {
                        LogUtils.e(message);
                    }
                })).build())
                .build();
        // 创建 网络请求接口 的实例
        sRetrofitInterface = retrofit.create(RetrofitInterface.class);
        if (sRetrofitRequestHelper == null){
            sRetrofitRequestHelper = new RetrofitRequestHelper();
        }
        return sRetrofitRequestHelper;
    }

    /**
     * 购买入库界面获取预定订单号
     * @param companyId 公司Id
     * @param listener 回掉
     */
        public void getPreorderRequest(String companyId, final RetrofitRequestListener listener){
        // 对 发送请求 进行封装
        Call<PreorderBean> call = sRetrofitInterface.getPreorderCall(companyId);
        // 发送异步网络请求
        call.enqueue(new Callback<PreorderBean>() {
            // 请求成功时回掉
            @Override
            public void onResponse(@NonNull Call<PreorderBean> call, @NonNull Response<PreorderBean> response) {
                listener.requestSuccess(response);
            }

            // 请求失败时回掉
            @Override
            public void onFailure(@NonNull Call<PreorderBean> call, @NonNull Throwable t) {
                listener.requestFail(t);
            }
        });
    }

    /**
     * 购买入库界面获取订单详情
     * @param preorderId 预定订单号
     * @param listener 回掉
     */
    public void getPreorderDetailsRequest(String preorderId, final  RetrofitRequestListener listener){
        // 对 发送请求 进行封装
        Call<PreorderDetailsBean> call = sRetrofitInterface.getPreorderDetailsCall(preorderId);
        // 发送异步网络请求
        call.enqueue(new Callback<PreorderDetailsBean>() {
            // 请求成功时回掉
            @Override
            public void onResponse(@NonNull Call<PreorderDetailsBean> call, @NonNull Response<PreorderDetailsBean> response) {
                listener.requestSuccess(response);
            }

            // 请求失败时回掉
            @Override
            public void onFailure(@NonNull Call<PreorderDetailsBean> call, @NonNull Throwable t) {
                listener.requestFail(t);
            }
        });
    }

    /**
     * 购买入库界面记录订单信息
     * @param buyorderid 预定订单号
     * @param project 所为项目
     * @param sourcename 供应商
     * @param loss_rate 损耗率
     * @param lifetime 使用寿命
     * @param company_id 公司id（登陆返回个人信息获得）
     * @param user_id 用户id（登陆返回个人信息获得）
     * @param listener 回调监听
     */
    public void recordRoderCall(String buyorderid, String project, String sourcename, String loss_rate, String lifetime
            , String company_id, String user_id, final RetrofitRequestListener listener){
        Call<ResponseBody> call = sRetrofitInterface.recordRoderCall(buyorderid, project, sourcename, loss_rate, lifetime, company_id, user_id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.requestFail(t);
            }
        });
    }

    /**
     * 购买入库界面确认订单
     * @param orderid 预定订单号
     * @param userid 用户ID
     * @param rfidList RFID数组
     * @param type 类型 1正常确认 2 异常确认
     * @param listener 回调
     */
    public void ensureOrderRequest(String orderid, String userid, List<String> rfidList, String type, final RetrofitRequestListener listener){
        Call<ResponseBody> call = sRetrofitInterface.ensureOrderCall(orderid, userid, rfidList, type);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.requestFail(t);
            }
        });
    }

    /**
     * 出库界面根据epc获取箱子信息
     * @param epc epc
     * @param listener 回调
     */
    public void outOrderRequest(List<String> epc, final RetrofitRequestListener listener){
        Call<ResponseBody> call = sRetrofitInterface.outOrderCall(epc, "stockout");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.requestFail(t);
            }
        });
    }

    /**
     * 搜索界面根据EPC获取箱子信息
     * @param epc 标签信号
     * @param listener 回调
     */
    public void searchOrderRequest(String epc, final RetrofitRequestListener listener){
        Call<ResponseBody> call = sRetrofitInterface.outOrderCall3(epc, "stocksearch");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.requestFail(t);
            }
        });
    }

    /**
     * 获取公司名信息
     * @param type 1--普通公司，2--运输公司
     * @param listener 回调
     */
    public void getCompanyRequest(String type, final  RetrofitRequestListener listener){
        Call<CompanyBean> call = sRetrofitInterface.getCompanyCall(type);
        call.enqueue(new Callback<CompanyBean>() {
            @Override
            public void onResponse(Call<CompanyBean> call, Response<CompanyBean> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(Call<CompanyBean> call, Throwable t) {
                listener.requestFail(t);
            }
        });
    }

    /**
     * 物料出库
     * @param send_company_id 发货公司ID
     * @param recv_company_id 收货公司ID
     * @param send_userid 发货用户ID
     * @param is_emptybox 是否空箱 0--否 1--是
     * @param trans_company_id 运输公司ID
     * @param trans_company_carnum 运输公司车牌号
     * @param box_ids 箱子ID集合，以,分割
     * @param listener 回调
     */
    public void stockOutRequest(String send_company_id, String recv_company_id, String send_userid, String is_emptybox
            , String trans_company_id , String trans_company_carnum, String box_ids, final RetrofitRequestListener listener){
        Call<ResponseBody> call = sRetrofitInterface.stockOutCall(send_company_id, recv_company_id, send_userid, is_emptybox, trans_company_id, trans_company_carnum, box_ids);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.requestFail(t);
            }
        });
    }

    /**
     * 二维获取订单信息
     * @param codeStr 二维码
     * @param listener 回调
     */
    public void codeToBoxInfoRequest(String codeStr, final RetrofitRequestListener listener){
        Call<DoorBoxsBean> call = sRetrofitInterface.codeToBoxInfo(codeStr, "guard");
        call.enqueue(new Callback<DoorBoxsBean>() {
            @Override
            public void onResponse(Call<DoorBoxsBean> call, Response<DoorBoxsBean> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(Call<DoorBoxsBean> call, Throwable t) {
                listener.requestFail(t);
            }
        });
    }

    /**
     * 收货界面二维获取订单信息
     * @param codeStr 二维码
     * @param listener 回调
     */
    public void codeToBoxRcvInfoRequest(String codeStr, final RetrofitRequestListener listener){
        Call<DoorBoxsBean> call = sRetrofitInterface.codeToBoxRcvInfo(codeStr, "recv");
        call.enqueue(new Callback<DoorBoxsBean>() {
            @Override
            public void onResponse(Call<DoorBoxsBean> call, Response<DoorBoxsBean> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(Call<DoorBoxsBean> call, Throwable t) {
                listener.requestFail(t);
            }
        });
    }
    public void loginRequest(String username, String password, final RetrofitRequestListener listener){
        // 对 发送请求 进行封装
        Call<UserBean> call = sRetrofitInterface.loginCall(username, password);
        // 发送异步网络请求
        call.enqueue(new Callback<UserBean>() {
            // 请求成功时回掉
            @Override
            public void onResponse(@NonNull Call<UserBean> call, @NonNull Response<UserBean> response) {
                listener.requestSuccess(response);
            }

            // 请求失败时回掉
            @Override
            public void onFailure(@NonNull Call<UserBean> call, @NonNull Throwable t) {
                listener.requestFail(t);
            }
        });
    }

    /***
     * 收货接口
     * @param type 订单类型（1：一键收货   2：异常收货  3：系统退运  4：系统补运）
     * @param userid 用户id
     * @param tags 标签列表
     * @param orderid 订单号
     * @param listener 回调
     */
    public void receiveRequest(String type, String userid, List<String> tags, String orderid, final RetrofitRequestListener listener){
        Call<ResponseBody> call = sRetrofitInterface.receiveCall(type, userid, tags, orderid);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.requestFail(t);
            }
        });
    }

    /**
     * 网络请求回掉接口
     */
    public interface RetrofitRequestListener{
        /**
         * 请求成功
         * @param response 返回的回复体
         */
        void requestSuccess(Response response);
        /**
         * 请求失败
         * @param t 抛出的异常信息
         */
        void requestFail(Throwable t);
    }

    /**
     * 物料录入根据epc获取箱子信息
     * @param epc epc
     * @param listener 回调
     */
    public void entryOrderRequest(List<String> epc, final RetrofitRequestListener listener){
        Call<MatterBean> call = sRetrofitInterface.outOrderCall2(epc, "stockstore");
        call.enqueue(new Callback<MatterBean>() {
            @Override
            public void onResponse(Call<MatterBean> call, Response<MatterBean> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(Call<MatterBean> call, Throwable t) {
                listener.requestFail(t);
            }
        });
    }

    /**
     * 上传物料信息
     * @param idList 物料ID列表
     * @param userid 用户ID
     * @param mattertype 物料类型
     * @param matternum 物料号
     * @param mattername 物料名称
     * @param listener 回调
     */
    public void uploadRequest(List<String> idList, String userid, String mattertype, String matternum, String mattername, final RetrofitRequestListener listener){
        Call<ResponseBody> call = sRetrofitInterface.uploadMatterCall(idList, userid, mattertype, matternum, mattername);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.requestFail(t);
            }
        });
    }

    /**
     * 清洁根据epc获取箱子信息
     * @param epc epc
     * @param listener 回调
     */
    public void cleanOrderRequest(List<String> epc, final RetrofitRequestListener listener){
        Call<ResponseBody> call = sRetrofitInterface.outOrderCall(epc, "cleansearch");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.requestFail(t);
            }
        });
    }

    /***
     * 上传清洁信息
     * @param ids 清洁箱子的ID
     * @param userid 用户ID
     * @param cleantype 清洁类型
     * @param listener 回调
     */
    public void cleanUploadRequest(List<String> ids, String userid, String cleantype, final RetrofitRequestListener listener){
        Call<ResponseBody> call = sRetrofitInterface.cleanUploadCall(ids, userid, cleantype);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.requestFail(t);
            }
        });
    }
    /**
     * 清洁根据epc获取箱子信息
     * @param epc epc
     * @param listener 回调
     */
    public void cleanEnsureRequest(List<String> epc, final RetrofitRequestListener listener){
        Call<ResponseBody> call = sRetrofitInterface.outOrderCall(epc, "cleancheck");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.requestFail(t);
            }
        });
    }

}
