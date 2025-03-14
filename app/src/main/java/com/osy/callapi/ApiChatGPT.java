package com.osy.callapi;

import android.content.Context;
import android.util.Log;

import com.osy.notifyreply.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiChatGPT {
    Context context;
    public  ApiChatGPT(Context context){
        this.context = context;
    }
    final String TAG = "ApiChatGPT";
    public String getCompletions(ArrayList<String> keywordList){
        String apiKey = context.getResources().getString(R.string.chatGPT);
        String endpointUrl = "https://api.openai.com/v1/chat/completions";
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");


        String requestBody = null;
        try {
            requestBody = createGPTRequestBody(keywordList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(requestBody==null){
            Log.i(TAG, "API요청 실패요.ㅜㅜ");
            return null;
        }
        Request request = new Request.Builder()
                .url(endpointUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(mediaType, requestBody))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JSONObject result1 = new JSONObject(responseBody);
                JSONObject result2 = result1.getJSONArray("choices").getJSONObject(0);
                String result3 = result2.getJSONObject("message").getString("content");
                Log.i(TAG, responseBody);
                Log.i(TAG, result3);
                return result3;
            } else {
                Log.i(TAG, "API요청 실패요.");
                return null;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    // 질문을 strList에 추가하면서, 최대 5개까지만 유지

    // GPT API 요청 바디 생성
    public static String createGPTRequestBody(ArrayList<String> strList) throws JSONException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-4o");
        requestBody.put("max_tokens", 150);

        JSONArray messages = new JSONArray();

        // 시스템 메시지 (고정)
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "넌 대한민국에 최적화된 인공지능이야. " +
                "너의 직업은 해양경찰이야. 배에 승선하는 경찰관이야. 경비구역을 지키는 일을 수행해. 직업질문 아닌 일상적인 질문에는 대한민국에 최적화해서 답변해줘." +
                "메신저에서 대화하듯 짧고 간결하게 답변해. 절대로 답변이 10줄이 넘어가면 안돼. 길어질 것 같으면 부분적으로 나눠서 대답하고, 사용자가 이해했는지 확인한 후 이어서 설명해.");
        messages.put(systemMessage);

        // 사용자 질문을 messages 배열에 추가
        for (String question : strList) {
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", question);
            messages.put(userMessage);
        }
        // messages 배열을 최종 requestBody에 추가
        requestBody.put("messages", messages);

        return requestBody.toString();
    }
}


