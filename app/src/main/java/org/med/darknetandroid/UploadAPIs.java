package org.med.darknetandroid;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

//This class is the list of APIs where we only need one which is the one that is responsible for the sending of images and parameters through to the server
public interface UploadAPIs {
    @Multipart
    @POST("uploadImages")
    Call<ResponseBody> uploadMultipleImages(@PartMap Map<String, RequestBody> map, @Part List<MultipartBody.Part> file);
}
