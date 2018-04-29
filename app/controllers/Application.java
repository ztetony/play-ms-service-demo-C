package controllers;

import com.alibaba.fastjson.JSON;
import com.tonysfriend.ms.bean.Result;
import com.tonysfriend.ms.bean.http.Header;
import com.tonysfriend.ms.bean.http.Param;
import com.tonysfriend.ms.util.HttpClient;
import com.tonysfriend.ms.util.RestTemplate;
import play.Logger;
import play.mvc.*;

import java.util.UUID;

public class Application extends Controller {

    public static void index() {

        render();
    }

    /**
     * 通过 RestTemplate 访问
     */
    public static void demo() {
        long start = System.currentTimeMillis();
//      http://localhost:8080/proxy/client/call.action?msapiurl=http://ZIOT.DMS.1.0.0.DEV/api/ziot/dms/device/get&method=POST&contentType=application/json;charset=UTF-8&headers={%22tenantId%22:0}&params={%22propertyId%22:12380,%22tenantId%22:456}
        String proxy = "http://localhost:8080/proxy/client/call.action";
        String msapiurl = "http://ZIOT.DMS.1.0.0.DEV/api/ziot/dms/device/get";
        String method = "POST";
        String contentType = "application/json"; //"application/json;charset=UTF-8"
        String headers = "{\"tenantId\":456}";
        String params = "{\"propertyId\":12380,\"tenantId\":456}";

        Result result = new Result();

        try {
            Header header1 = new Header();
            header1.put("tenantId", "456");

            Header header2 = new Header();
            header2.put("tenantId", "456");

            Param<String, Object> param2 = new Param<String, Object>();
            param2.put("propertyId", 12380);
            param2.put("tenantId", 456);
            param2.put("msapiurl", msapiurl);

            /**
             *  public Render call(@Read(key = "msapiurl", defaultValue = "http://computer-service:8888/api/group/xxx") String msapiurl,
             @Read(key = "method") String method,
             @Read(key = "contentType") String contentType,
             @Read(key = "headers", sampleValue = "{\"tenantId\":0}") String headers,
             @Read(key = "params") String params) {
             */
            Param<String, Object> param1 = new Param<String, Object>();
            param1.put("msapiurl", msapiurl);
            param1.put("method", method);
            param1.put("contentType", contentType);
            param1.put("headers", JSON.toJSONString(header2));
            param1.put("params", JSON.toJSONString(param2));

            RestTemplate restTemplate = new RestTemplate();
            result = restTemplate.invokeForEntity(proxy, method, contentType, header1, param1, 10 * 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }

        long costTime = System.currentTimeMillis() - start;
        Logger.info("cost time: %s ", costTime);
        renderJSON(result);
    }

    /**
     * http 直连
     */
    public static void proxy1() {

//        http://localhost:8080/proxy/client/call.action?msapiurl=http://ZIOT.DMS.1.0.0.DEV/api/ziot/dms/device/get&method=POST&contentType=application/json;charset=UTF-8&headers={%22tenantId%22:0}&params={%22propertyId%22:12380,%22tenantId%22:456}
        String proxy_url = "http://localhost:8080/proxy/client/call.action?";
        String proxy_method = "POST";
        String proxy_contentType = "application/json;charset=UTF-8";
        String proxy_headers = "{\"tenantId\":456}";
        String paramsJson = "{\"msapiurl\":\"http://ZIOT.DMS.1.0.0.DEV/api/ziot/dms/device/get\",\"method\":\"POST\",\"contentType\":\"application/json\",\"headers\":\"{\\\"tenantId\\\":456}\",\"params\":\"{\\\"propertyId\\\":12380,\\\"tenantId\\\":456}\"}";

        Result result = new Result();
        try {
            //String urlstr, String method, String contentType, String params, int timeout
            result = HttpClient.invoke(proxy_url, proxy_method, proxy_contentType, paramsJson, 10 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        renderJSON(result);
    }

    public static void health() {

        Result result = new Result();
        result.setData("from C: "+UUID.randomUUID().toString());
        renderJSON(result);
    }

    public static void testGetParam(String p1){
        Result result = new Result();
        result.setData("p1="+p1);
        renderJSON(result);
    }

}