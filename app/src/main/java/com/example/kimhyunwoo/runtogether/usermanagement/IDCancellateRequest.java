package com.example.kimhyunwoo.runtogether.usermanagement;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import static com.example.kimhyunwoo.runtogether.ServerInfo.*;

public class IDCancellateRequest extends StringRequest
{
    final static private String URL = serverURL + idcancellationURL;                          // URL 헤더파일로 묶어서 수정해야함.
    private Map<String,String> parameters;                                                          // Map 형식으로 데이터를 저장(JSON 이기에 Stirng2개)

    // Volley의 StringRequest를 선언받아서 사용해야 하는데 내가 사용할 값들과 부모의 인자값까지 사용해야함. 앞서 선언한 map 에 필요한 값들을 JSON형식의 String 2개값을 넣어서 보냄.
    public IDCancellateRequest(String userPassword, Response.Listener<String> listener)
    {
        super(Request.Method.POST,URL,listener,null);
        parameters = new HashMap<>();
        parameters.put("userPassword",userPassword);
    }

    public Map<String,String>getParams()
    {
        return parameters;
    }
}