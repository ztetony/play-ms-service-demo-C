package controllers.jobs;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.tonysfriend.ms.bean.InstanceId;
import com.tonysfriend.ms.bean.Result;
import com.tonysfriend.ms.constant.Constants;
import com.tonysfriend.ms.constant.EurekaType;
import com.tonysfriend.ms.constant.MethodType;
import com.tonysfriend.ms.impl.ApplicationClientServiceImpl;
import com.tonysfriend.ms.util.IpAddressUtil;
import com.tonysfriend.ms.util.PropertiesUtil;
import play.Logger;
import play.db.jpa.JPA;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import play.jobs.OnApplicationStart;
import scala.collection.immutable.Stream;
import sun.net.util.IPAddressUtil;

/**
 *
 * @author
 */
//@OnApplicationStart(async = true)
//@On("0 0 12 * * ?")
//@Every("5min")
public class BootStrapSdk extends Job {
    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void doJob() {

        try {

            final ApplicationClientServiceImpl client = new ApplicationClientServiceImpl();

            final String eureka = PropertiesUtil.getProperty("eureka.client.serviceUrl.defaultZone");

            final InstanceId instanceId = new InstanceId();
            instanceId.setHost(IpAddressUtil.getLocalHostAddress());
            instanceId.setPort(49000);
            instanceId.setName("PLAY-MS-SERVICE-DEMO");

            String REGISTER_INSTANCE_URL = String.format(Constants.REGISTER_INSTANCE_URL_FORMATER, eureka, instanceId.getName().toUpperCase());
            Result registResult = client.registerAppInstance(REGISTER_INSTANCE_URL, "POST", "application/xml", instanceId, Constants.DEFAULT_TIMEOUT);

            System.out.println(registResult);

            if (registResult.checkBizSuccess(EurekaType.REGISTER_SUCCESS_204.getCode())) {

                //定时 heartbeat
                new Thread(new Runnable() {

                    public void run() {

                        for (; ; ) {
                            System.out.println("...send heartbeat ...");

                            String HEARTBEAT_URL = String.format(Constants.HEARTBEAT_URL_FORMATER, eureka, instanceId.getName().toUpperCase(), instanceId.getHost(), instanceId.getName(), instanceId.getPort());
                            Result hbResult = client.sendHeartbeat(HEARTBEAT_URL, MethodType.HB_PUT.getName(), "text/plain", "", Constants.DEFAULT_TIMEOUT);
                            System.out.println(hbResult);

                            try {
                                Thread.sleep(Constants.HEARTBEAT_INTERVAL);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
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