package app.laiki.api;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import app.laiki.BuildConfig;
import app.laiki.api.model.GsonAnswers;
import app.laiki.api.model.GsonAuth;
import app.laiki.api.model.GsonProfileResponse;
import app.laiki.api.model.GsonQuestionResponse;
import app.laiki.api.model.GsonResponse;
import app.laiki.diagnostics.Logger;
import app.laiki.model.types.Choice;
import app.laiki.toolkit.http.HttpHeaders;
import app.laiki.toolkit.http.LoginRequiredException;
import app.laiki.utils.Utils;
import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static app.laiki.App.app;
import static app.laiki.App.prefs;


public interface ApiService {

    static String serialize(Enum<?> value) {
        try {
            return value.getClass().getField(value.name()).getAnnotation(SerializedName.class).value();
        } catch (NoSuchFieldException e) {
            return value.name();
        }
    }

    /**
     * Запрос кода по СМС.
     *
     * @param phone длина строго 11 символов, digits
     * @return
     */
    @GET("code/{phone}")
    Call<GsonResponse> login(@Path("phone") String phone);

    /**
     * Запрос токена авторизации
     *
     * @param phone длина строго 11 символов, digits
     * @param code  длина строго 4 символа, digits
     * @return
     */
    @GET("token/{phone}/{code}")
    Call<GsonAuth> auth(
            @Path("phone") String phone,
            @Path("code") String code
    );

    @POST("user/fcm_token")
    @FormUrlEncoded
    Call<GsonResponse> subscribeFcm(@Field("fcm_token") String token);

    /**
     * Заполнение информации пользователя
     *
     * @param name   max длина 30 символов
     * @param age    max длина 25 символов
     * @param gender длина 1 символ
     * @return
     */
    @POST("info")
    @FormUrlEncoded
    Call<GsonProfileResponse> saveProfile(
            @Field("name") String name,
            @Field("education") String age,
            @Field("sex") String gender
    );

    /**
     * Заполнение информации пользователя
     *
     * @param token  "Bearer" + accessToken
     * @param name   max длина 30 символов
     * @param age    max длина 25 символов
     * @param gender длина 1 символ
     * @return
     */
    @POST("info")
    @FormUrlEncoded
    Call<GsonProfileResponse> saveProfile(
            @Header("Authorization") String token,
            @Field("name") String name,
            @Field("education") String age,
            @Field("sex") String gender
    );

    /**
     * Получение вопроса
     *
     * @return
     */
    @GET("question")
    Call<GsonQuestionResponse> nextQuestion();


    /**
     * @param questionId      обязательно
     * @param selectedVariant обязательно, enum: A,B,C,D,E
     * @param variantA        обязательно, MD5, длина 32 символа
     * @param variantB        обязательно, MD5, длина 32 символа
     * @param variantC        обязательно, MD5, длина 32 символа
     * @param variantD        обязательно, MD5, длина 32 символа
     * @return
     */
    @POST("answer")
    @FormUrlEncoded
    Call<GsonResponse> answer(
            @Field("question_id") String questionId,
            @Field("selected_variant") Choice selectedVariant,
            @Field("selected_name") String selectedName,
            @Field("variantA") String variantA,
            @Field("variantB") String variantB,
            @Field("variantC") String variantC,
            @Field("variantD") String variantD
    );


    /**
     * Список ответов пользователя
     *
     * @return
     */
    @GET("answer")
    Call<GsonAnswers> getAnswers();


    /**
     * Просмотр ответа
     * Выставляет поле статуса просмотра is_viewed = 1
     *
     * @param answerId обязательно
     * @return
     */
    @GET("view/{answer_id}")
    Call<GsonResponse> viewAnswer(@Path("answer_id") String answerId);

    @GET("user")
    Call<Void> ping();

    @GET("user")
    Call<GsonProfileResponse.GsonUser> getProfile(@Header("Authorization") String token);

    @GET("user")
    Call<GsonProfileResponse.GsonUser> getProfile();

    @GET("/api/invite/{phone}")
    Call<GsonResponse> invite(@Path("phone") String phone, @Query("name") String name, @Query("sex") String gender, @Query("age") String age);

    @GET("answer/reset")
    Call<GsonResponse> resetAnswers();

    @GET("stat/open")
    Call<GsonResponse> statOpen();

    class Creator {

        public static ApiService newService(ApiSet apiSet, Context context) {
            MyAuthenticator authenticator = new MyAuthenticator(context);

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .addInterceptor(new MyRequestInterceptor(context))
                    .authenticator(authenticator)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);


            if (Logger.LOG_API) {
                HttpLoggingInterceptor logger = new HttpLoggingInterceptor(Logger.createApiLogger());
                logger.setLevel(HttpLoggingInterceptor.Level.BODY);
                builder.addInterceptor(logger);
            }
            OkHttpClient client = builder.build();

            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(apiSet.baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            ApiService service = retrofit.create(ApiService.class);

            authenticator.setServiceInstance(service);

            return service;
        }

        /**
         * works only when gets 401. Tries to get new access token using refresh token
         * and resend the request RIGHT away. Tries 2 times.
         */
        private static class MyAuthenticator implements Authenticator {

            private ApiService service;
            private final String deviceId;

            private MyAuthenticator(Context context) {
                deviceId = Utils.getDeviceId(context);
            }

            @Override
            public Request authenticate(@NonNull Route route, @NonNull Response response) throws IOException {
                if (response.priorResponse() != null)
                    return null;

                String failedToken = response.request().header(HttpHeaders.AUTHORIZATION);
                if (failedToken == null) {
                    throw new IOException("Unauthorized");
                }

                app().logout();
                throw new LoginRequiredException(failedToken);
            }

            void setServiceInstance(ApiService serviceInstance) {
                this.service = serviceInstance;
            }
        }

        private static class MyRequestInterceptor implements Interceptor {
            final String deviceId;
            final String version;

            private MyRequestInterceptor(Context context) {
                deviceId = Utils.getDeviceId(context);
                version = String.valueOf(BuildConfig.VERSION_CODE);
            }

            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {

                if (prefs().config().emulateSlowConnection) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();
                requestBuilder.method(original.method(), original.body());
                requestBuilder.header("Accept", "application/json");
                requestBuilder.header("X-From", deviceId);
                requestBuilder.header("X-App-Id", "android");
                requestBuilder.header("X-Client-Version", version);

                String token = prefs().getAccessToken();
                if (token != null) {
                    requestBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                }

                Request request = requestBuilder.build();
                return chain.proceed(request);

            }
        }
    }

}
