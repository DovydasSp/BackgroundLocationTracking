package speckauskas.dovydas.backgroundlocationtracking.rest;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import speckauskas.dovydas.backgroundlocationtracking.DBClass;

public class RestRequest {
    private static final String TAG = "RestRequest";

    public void postTrip(String url, List<DBClass> lastTrip) {
        OkHttpClient client = new OkHttpClient();
        String json = tripDataToJson(lastTrip);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", Credentials.basic("testas", "testas"))
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: ", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "onResponse: request failed");
                } else {
                    Log.d(TAG, String.format("onResponse: Response-%s, body-%s, message-%s",
                            response.toString(), response.body().toString(), response.message()));
                }
            }
        });
    }

    private String tripDataToJson(List<DBClass> lastTrip) {
        JSONObject jsonTRIP = new JSONObject();
        JSONArray list = new JSONArray();
        JSONArray tasksList = new JSONArray();
        if(lastTrip != null && !lastTrip.isEmpty()){
            for (DBClass dbClass : lastTrip) {
                try {
                    JSONObject jsonCoords = new JSONObject();
                    jsonCoords.put("longitude", dbClass.getLongitude());
                    jsonCoords.put("latitude", dbClass.getLatitude());
                    jsonCoords.put("datetime", dbClass.getDateTime());
                    jsonCoords.put("uid", java.util.UUID.randomUUID());
                    list.put(jsonCoords);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            jsonTRIP.put("Tasks", tasksList);
            jsonTRIP.put("Coordinates", list);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("JSON", "makeJson: "+ jsonTRIP.toString());
        return jsonTRIP.toString();
    }
}
