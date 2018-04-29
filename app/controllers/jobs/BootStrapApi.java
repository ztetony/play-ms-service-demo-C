package controllers.jobs;

import com.alibaba.fastjson.JSON;
import com.tonysfriend.ms.bean.InstanceId;
import com.tonysfriend.ms.bean.Result;
import com.tonysfriend.ms.bean.http.Param;
import com.tonysfriend.ms.constant.Constants;
import com.tonysfriend.ms.constant.EurekaType;
import com.tonysfriend.ms.constant.MethodType;
import com.tonysfriend.ms.impl.ApplicationClientServiceImpl;
import com.tonysfriend.ms.util.HttpClient;
import com.tonysfriend.ms.util.IpAddressUtil;
import com.tonysfriend.ms.util.PropertiesUtil;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * @author
 */
@OnApplicationStart(async = true)
//@On("0 0 12 * * ?")
//@Every("5min")
public class BootStrapApi extends Job {
    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void doJob() {

        try {

            final ApplicationClientServiceImpl client = new ApplicationClientServiceImpl();

            final String eureka = PropertiesUtil.getProperty("eureka.client.serviceUrl.defaultZone");

            final InstanceId instanceId = new InstanceId();
            instanceId.setHost(IpAddressUtil.getLocalIp());
            instanceId.setPort(49000);
            instanceId.setName("PLAY-MS-SERVICE-DEMO");

            final Param param = new Param();
            param.put("host", IpAddressUtil.getLocalIp());
            param.put("port", 49000);
            param.put("app", "PLAY-MS-SERVICE-DEMO");
            param.put("timeout", 10 * 1000);

            String REGISTER_INSTANCE_URL = "http://localhost:8080/service/register/register.action";
            Result<String> registResult = HttpClient.invoke(REGISTER_INSTANCE_URL, "POST", "application/json", JSON.toJSONString(param), 10 * 1000);

            System.out.println(registResult.getData());
            Result dataResult = JSON.parseObject(registResult.getData(), Result.class);
            if (dataResult.checkBizSuccess(EurekaType.REGISTER_SUCCESS_204.getCode())) {

                //定时 heartbeat
                new Thread(new Runnable() {

                    public void run() {

                        for (; ; ) {

                            try {
                                System.out.println("...send heartbeat ...");

                                String HEARTBEAT_URL = "http://localhost:8080/service/heartbeat/heartbeat.action";

                                Result hbResult = HttpClient.invoke(HEARTBEAT_URL, "POST", "application/json", JSON.toJSONString(param), 10 * 1000);

                                System.out.println(hbResult);


                                Thread.sleep(Constants.HEARTBEAT_INTERVAL);
                            } catch (Exception e) {
                                Logger.error("%s", e);
                            }

                        }//for

                    }//run

                }).start();

            }

        } catch (Exception e) {
            Logger.error("job error :{}", e);
        }

    }//doJob

}