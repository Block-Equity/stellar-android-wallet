package com.blockeq.stellarwallet.remote;

import com.blockeq.stellarwallet.models.SupportedAsset;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SupportedAssetsApi {
    @GET("directory/assets")
    Call<Map<String, SupportedAsset>> getAssetsDirectory();
}
