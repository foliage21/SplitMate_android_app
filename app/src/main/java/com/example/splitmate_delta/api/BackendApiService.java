package com.example.splitmate_delta.api;

import com.example.splitmate_delta.models.User;
import com.example.splitmate_delta.models.bills.BillByUserId;
import com.example.splitmate_delta.models.signup.ConfirmSignupRequest;
import com.example.splitmate_delta.models.signup.SignupRequest;
import com.example.splitmate_delta.models.login.LoginRequest;
import com.example.splitmate_delta.models.login.LoginResponse;
import com.example.splitmate_delta.models.addphoto.GeneratePresignedUrlsRequest;
import com.example.splitmate_delta.models.addphoto.GeneratePresignedUrlsResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.GET;
import retrofit2.http.Path;

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
    }