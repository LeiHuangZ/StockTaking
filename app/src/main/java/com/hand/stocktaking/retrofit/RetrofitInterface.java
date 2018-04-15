package com.hand.stocktaking.retrofit;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * @author huang
 */
public interface RetrofitInterface {
    /**
     * 登陆网络请求
     * @return Call对象
     * @param jobNum 工号（用户名）
     * @param psd 密码
     */
    @FormUrlEncoded
    @POST("Login/login")
    Call<UserBean> loginCall(@Field("jobnumber") String jobNum,@Field("pwd") String psd);
    /**
     * 获取预定订单号
     */
    @FormUrlEncoded
    @POST("Buyorder/getPreorder")
    Call<PreorderBean> getPreorderCall(@Field("company_id") String companyId);
    /**
     * 获取预定订单详情
     */
    @FormUrlEncoded
    @POST("Buyorder/preOrderinfo")
    Call<PreorderDetailsBean> getPreorderDetailsCall(@Field("orderid") String orderid);
    /**
     * 记录订单信息
     */
    @FormUrlEncoded
    @POST("Buyorder/editOrderinfo")
    Call<ResponseBody> recordRoderCall(@Field("buyorderid") String buyorderid, @Field("project") String project, @Field("sourcename") String sourcename, @Field("loss_rate") String loss_rate, @Field("lifetime") String lifetime
    , @Field("company_id") String company_id, @Field("user_id") String user_id);
    /**
     * 确认订单
     */
    @FormUrlEncoded
    @POST("Buyorder/preOrdersub")
    Call<ResponseBody> ensureOrderCall(@Field("orderid") String orderid, @Field("userid") String userid, @Field("tags[]")List<String> rfidList,@Field("type")String type );

    /**
     * 根据标签数组获取物料信息
     */
    @FormUrlEncoded
    @POST("Tags/tagsTobox")
    Call<ResponseBody> outOrderCall(@Field("tags[]") List<String> tag, @Field("type") String type);
    /**
     * 根据标签数组获取物料信息
     */
    @FormUrlEncoded
    @POST("Tags/tagsTobox")
    Call<MatterBean> outOrderCall2(@Field("tags[]") List<String> tag, @Field("type") String type);
    /**
     * 根据标签数组获取物料信息
     */
    @FormUrlEncoded
    @POST("Tags/tagsTobox")
    Call<ResponseBody> outOrderCall3(@Field("tags") String tag, @Field("type") String type);
    /**
     * 获取出库地址和收货地址
     */
    @FormUrlEncoded
    @POST("Company/getCompanyname")
    Call<CompanyBean> getCompanyCall(@Field("type") String type);
    /**
     * 出库
     */
    @FormUrlEncoded
    @POST("Order/stockout")
    Call<ResponseBody> stockOutCall(@Field("send_company_id") String send_company_id, @Field("recv_company_id") String recv_company_id, @Field("send_userid") String send_userid, @Field("is_emptybox") String is_emptybox
    , @Field("trans_company_id") String trans_company_id ,@Field("trans_company_carnum") String trans_company_carnum, @Field("box_ids") String box_ids);
    /**
     * 二维码获取订单信息
     */
    @FormUrlEncoded
    @POST("Order/codeDoororderinfo")
    Call<DoorBoxsBean> codeToBoxInfo(@Field("code") String code);
    /**
     * 收货界面二维码获取订单信息
     */
    @FormUrlEncoded
    @POST("Order/codeDoororderinfo")
    Call<DoorBoxsBean> codeToBoxRcvInfo(@Field("code") String code, @Field("type") String type);
    /**
     * 物料收货接口
     */
    @FormUrlEncoded
    @POST("Order/recvOrder")
    Call<ResponseBody> receiveCall(@Field("type") String type, @Field("userid") String userid, @Field("tags[]") List<String> tags, @Field("orderid") String orderid);
    /**
     * 上传物料信息
     */
    @FormUrlEncoded
    @POST("Box/store")
    Call<ResponseBody> uploadMatterCall(@Field("ids[]") List<String> idList, @Field("userid") String userid, @Field("mattertype") String mattertype, @Field("matternum") String matternum, @Field("mattername") String mattername);
    /**
     * 清洁上传接口
     */
    @FormUrlEncoded
    @POST("Clean/clean")
    Call<ResponseBody> cleanUploadCall(@Field("ids[]") List<String> ids, @Field("userid") String userid, @Field("cleantype") String cleantype);


}
