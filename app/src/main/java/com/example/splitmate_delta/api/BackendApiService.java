package com.example.splitmate_delta.api;

import com.example.splitmate_delta.models.User;
import com.example.splitmate_delta.models.bills.BillByUserId;
import com.example.splitmate_delta.models.bills.DownloadBillResponse;
import com.example.splitmate_delta.models.ble.DeviceStatusUpdate;
import com.example.splitmate_delta.models.permissions.ApproveOrRejectRequest;
import com.example.splitmate_delta.models.permissions.PendingRequest;
import com.example.splitmate_delta.models.permissions.Permission;
import com.example.splitmate_delta.models.permissions.PermissionRequest;
import com.example.splitmate_delta.models.pi.AssignDeviceRequest;
import com.example.splitmate_delta.models.signup.ConfirmSignupRequest;
import com.example.splitmate_delta.models.signup.SignupRequest;
import com.example.splitmate_delta.models.login.LoginRequest;
import com.example.splitmate_delta.models.login.LoginResponse;
import com.example.splitmate_delta.models.addphoto.GeneratePresignedUrlsRequest;
import com.example.splitmate_delta.models.addphoto.GeneratePresignedUrlsResponse;
import com.example.splitmate_delta.models.usage.UsageRecord;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BackendApiService {

    @POST("auth/signup")
    Call<ResponseBody> registerUser(@Body SignupRequest signupRequest);

    @POST("auth/confirm-signup")
    Call<ResponseBody> confirmSignup(@Body ConfirmSignupRequest confirmSignupRequest);

    @POST("auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    @GET("user/getOneUserById/{id}")
    Call<User> getUserById(@Path("id") int userId);

    @DELETE("auth/removeById/{id}")
    Call<ResponseBody> deleteUser(@Path("id") String id);

    @GET("user/getAllUsers")
    Call<List<User>> getAllUsers();

    @GET("bill/getBillByUserId/{userId}")
    Call<List<BillByUserId>> getBillsByUserId(@Path("userId") int userId);

    @POST("s3/generatePresignedUrls")
    Call<GeneratePresignedUrlsResponse> generatePresignedUrls(@Body GeneratePresignedUrlsRequest request);

    @GET("usage/byUserId/{userId}")
    Call<List<UsageRecord>> getUsageRecordsByUserId(@Path("userId") int userId);

    @GET("user/tenant/authorizedDevices")
    Call<List<Permission>> getAuthorizedDevices(@Query("userId") int userId);

    @POST("http://splitmate-app.us-east-1.elasticbeanstalk.com:5000/frontend/updateStatus")
    Call<ResponseBody> updateDeviceStatus(@Body DeviceStatusUpdate deviceStatusUpdate);

    @POST("/user/tenant/requestPermission")
    Call<ResponseBody> requestPermission(@Body PermissionRequest permissionRequest);

    @GET("/user/landlord/viewPendingRequests")
    Call<List<PendingRequest>> getPendingPermissionRequests();

    @POST("/user/landlord/approveOrReject")
    Call<ResponseBody> approveOrRejectPermission(@Body ApproveOrRejectRequest request);

    @POST("/frontend/assignDeviceToHouse")
    Call<ResponseBody> assignDeviceToHouse(@Body AssignDeviceRequest request);;

    @POST("bill/generateBillPdf/{userId}")
    Call<DownloadBillResponse> generateBillPdf(
            @Path("userId") int userId,
            @Header("User-Agent") String userAgent,
            @Header("Accept") String accept,
            @Header("Cache-Control") String cacheControl,
            @Header("Connection") String connection,
            @Header("Content-Length") String contentLength
    );
    }