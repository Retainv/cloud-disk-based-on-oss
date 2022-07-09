package top.retain.nd.util;


import cn.hutool.json.JSONException;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
public class SmsUtil {

    public static final String SMS_SUCCESS = "OK";
    private final static int appid = 1400489215;
    private final static String appkey = "*";
    private static String profiles;


    @Value("${spring.profiles.active}")
    public void setProfiles(String profile) {
        profiles = profile;
//        profiles = Constants.TEST_PROFILE;
    }

    // 876859:第一条通知模板   877079：第二条通知模板  879648: 绑定验证码模板
    public static boolean sendSms(int templateId, String[] phoneNumbers, String[] params) {
        if ("dev".equals(profiles)) {
            log.info("SmsUtil工具类发送验证码到" + Arrays.toString(phoneNumbers));
        } else {
            log.info("SmsUtil工具类发送验证码到" + Arrays.toString(phoneNumbers));
            String smsSign = "*";
            try {
                SmsSingleSender sender = new SmsSingleSender(appid, appkey);
                for (String number : phoneNumbers) {
                    SmsSingleSenderResult result = sender.sendWithParam("86", number,
                            templateId, params, smsSign, "", "");
                    if (!SMS_SUCCESS.equalsIgnoreCase(result.errMsg)) {
                        log.info("发送失败！" + number+"原因："+result.errMsg);
                    }
                    log.info("发送成功！" + number);
                }
            } catch (HTTPException | JSONException | IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
//        String[] phoneNumbers = {"13551796434","17762474097"};

//    public static String[] getNoticeFields(JSONObject jsonObject, int type){
//        List<String> params=new ArrayList<>();
//        // todo: 不好拓展, 有待重构
//        switch (type){
//            case 0:
//                getSpecificFields(params,jsonObject,CONVENE_FIELDS);
//                break;
//            case 1:
//                getSpecificFields(params,jsonObject,CLUE_FIELDS);
//                break;
//            case 2:
//                getSpecificFields(params,jsonObject,EMERGENCY_FIELDS);
//                break;
//            case 3 :
//                getSpecificFields(params,jsonObject,END_ACTION_FIELD);
//                break;
//            default :
//                System.err.println("未匹配到短信参数！");
//                break;
//        }
//        return ArrayUtil.toArray(params.listIterator(),String.class);
//    }

//    private static void getSpecificFields(List<String> list, JSONObject jsonObject, List<String> fields){
//        if (jsonObject.keySet().containsAll(fields)){
//            fields.forEach(v->{
//                Object o = jsonObject.get(v);
//                list.add(o.toString());
//            });
//        }
//    }
}
