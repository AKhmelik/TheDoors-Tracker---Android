package ru.eqbeat.thedoorstracker;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;


/**
 * Created by A.khmelik 02.04.2016.
 */
public interface AuthInterface {


//    @FormUrlEncoded
//    @POST("/api/user")
//    UserApi getHashByPassword(@Field("username") String username, @Field("password") String password);

    @Headers({
            "X_TRACKER_USERNAME: rest",
            "X_TRACKER_PASSWORD: rest",
    })
    @FormUrlEncoded
    @POST("/api/user")
    Call<Object> userLogin(@Field("username") String username, @Field("password") String password);

    @Headers({
            "X_TRACKER_USERNAME: rest",
            "X_TRACKER_PASSWORD: rest",
    })
    @FormUrlEncoded
    @PUT("/api/user")
    Call<Object> userRegistration(@Field("username") String username, @Field("password") String password, @Field("email") String email);

    @Headers({
            "X_TRACKER_USERNAME: rest",
            "X_TRACKER_PASSWORD: rest",
    })
    @FormUrlEncoded
    @PUT("/api/oauth")
    Call<Object> oauthRegistration(@Field("loginProviderIdentifier") String loginProviderIdentifier, @Field("oauth") String oauth);


    @Headers({
            "X_TRACKER_USERNAME: rest",
            "X_TRACKER_PASSWORD: rest",
    })
    @FormUrlEncoded
    @PUT("/api/oauth")
    Call<Object> oauthRegistrationStage2(@Field("loginProviderIdentifier") String loginProviderIdentifier,
                                         @Field("oauth") String oauth,
                                         @Field("username") String username,
                                         @Field("email") String email

    );


}
