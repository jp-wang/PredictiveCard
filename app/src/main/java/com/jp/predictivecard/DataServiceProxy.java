package com.jp.predictivecard;

import com.telenav.api.data.api.v2.CheckForUpdateRequest;
import com.telenav.api.data.api.v2.CheckForUpdateResponse;
import com.telenav.api.data.api.v2.DataRetrieveRequest;
import com.telenav.api.data.api.v2.DataRetrieveResponse;
import com.telenav.dataservice.DataService;
import com.telenav.predictivecards.util.PredCardLogger;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * @author jpwang
 * @since 5/9/16
 */
public class DataServiceProxy implements DataService {
    IDataService dataService;

    public DataServiceProxy(String dataServiceURL) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(dataServiceURL)
                .addConverterFactory(new Converter.Factory() {
                    @Override
                    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                        return new Converter<ResponseBody, DataRetrieveResponse>() {
                            @Override
                            public DataRetrieveResponse convert(ResponseBody value) throws IOException {
                                return DataRetrieveResponse.buildFromJson(value.string());
                            }
                        };
                    }

                    @Override
                    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
                        return new Converter<DataRetrieveRequest, RequestBody>() {
                            @Override
                            public RequestBody convert(DataRetrieveRequest value) throws IOException {
                                return RequestBody.create(MediaType.parse("application/json; charset=UTF-8"), value.toJsonString());
                            }
                        };
                    }

                    @Override
                    public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                        return super.stringConverter(type, annotations, retrofit);
                    }
                })
                .build();
        dataService = retrofit.create(IDataService.class);
    }

    @Override
    public void initializeService(URL url, Map<String, String> map) {

    }

    @Override
    public CheckForUpdateResponse checkForUpdate(CheckForUpdateRequest checkForUpdateRequest) {
        return null;
    }

    @Override
    public DataRetrieveResponse retrieve(DataRetrieveRequest retrieveRequest) {
        try {
            return dataService.retrieve(retrieveRequest).execute().body();
        } catch (Exception e) {
            PredCardLogger.logI(DataServiceProxy.class, "exception in dataRetrieveResponse " + e.getMessage());
            return null;
        }
    }

    interface IDataService {
        @Headers({
                "x-tn-api_key: f05a47ec-2111-478f-9c73-a95aa0e7ad89",
                "x-tn-api_signature: f05a47ec-2111-478f-9c73-a95aa0e7ad89:1439329300:628aa0addaf55a91f60214e89cf04e6e",
                "Content-Type: application/json"
        })
        @POST("v2/domain/retrieve")
        Call<DataRetrieveResponse> retrieve(@Body DataRetrieveRequest retrieveRequest);
    }
}