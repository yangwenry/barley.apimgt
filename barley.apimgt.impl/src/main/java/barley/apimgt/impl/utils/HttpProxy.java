package barley.apimgt.impl.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
 
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
 
public class HttpProxy {
	
    /**
     * POST 요청
     * @param url       요청할 url
     * @param params    파라메터
     * @param encoding  파라메터 Encoding
     * @return 서버 응답결과 문자열
     */
    public String post(String url, Map params, String encoding){
        HttpClient client = new DefaultHttpClient();
         
        try {
            HttpPost post = new HttpPost(url);
            System.out.println("POST : " + post.getURI());
             
            List<NameValuePair> paramList = convertParam(params);
            post.setEntity(new UrlEncodedFormEntity(paramList, encoding));
             
            ResponseHandler<String> rh = new BasicResponseHandler();
 
            return client.execute(post, rh);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            client.getConnectionManager().shutdown();
        }
         
        return "error";
    }
     
    public String post(String url, Map params){
        return post(url, params, "UTF-8");
    }
     
     
    /**
     * GET 요청
     * POST 와 동일
     */
    public String get(String url, Map params, String encoding){
        HttpClient client = new DefaultHttpClient();
 
        try {
            List<NameValuePair> paramList = convertParam(params);
            HttpGet get = new HttpGet(url+"?"+URLEncodedUtils.format(paramList, encoding));
            System.out.println("GET : " + get.getURI());
             
            ResponseHandler<String> rh = new BasicResponseHandler();
             
            return client.execute(get, rh);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            client.getConnectionManager().shutdown();
        }
         
        return "error";
    }
     
    public String get(String url, Map params){
        return get(url, params, "UTF-8");
    }

    private List<NameValuePair> convertParam(Map params){
        List<NameValuePair> paramList = new ArrayList<NameValuePair>();
        Iterator<String> keys = params.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            if(params.get(key) != null) {
            	paramList.add(new BasicNameValuePair(key, params.get(key).toString()));
            }
        }
         
        return paramList;
    }
     
    // Test
    public static void main(String[] args) {
        HttpProxy p = new HttpProxy();
         
        Map params = new HashMap();
        params.put("w", "tot");
        params.put("q", "한예슬");
         
        System.out.println(p.get("http://m.search.daum.net/search", params));
    }
}



