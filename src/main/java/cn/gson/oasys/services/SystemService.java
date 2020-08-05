package cn.gson.oasys.services;

import cn.gson.oasys.model.entity.user.User;
import cn.gson.oasys.utils.HttpRequestUtil;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.UUID;

/**
 * 系统相关操作
 */
@Service
@Transactional
public class SystemService {

    private static final Logger logger = LoggerFactory.getLogger(SystemService.class);

    @Value("${APP_ID}")
    private String APP_ID;

    @Value("${APP_KEY}")
    private String APP_KEY;

    @Value("${BASE_URL}")
    private String BASE_URL;

    private static JSONObject TOKEN_JSON=new JSONObject();

    /**
     * 获取token
     * @return
     */
    public  String getToken(){
        String token= TOKEN_JSON.getString("token");
        if(token==null||(new Date().getTime()>TOKEN_JSON.getLong("timeout"))){//token为空或过期
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("appid", APP_ID);
            jsonObject.put("appkey",APP_KEY);
            logger.info("获取token入参："+jsonObject.toString());
            String jsonStr = HttpRequestUtil.sendPost(BASE_URL+"/csaas/api/access_token", jsonObject.toString());
            logger.info("获取token返参："+jsonStr);
            JSONObject jsonAccessToken = JSONObject.parseObject(jsonStr);
            String access_token = jsonAccessToken.getString("access_token");
            TOKEN_JSON.put("token",access_token);
            TOKEN_JSON.put("timeout",new Date().getTime()+7200000);//设置过期时间
        }
        token = TOKEN_JSON.getString("token");
        logger.info("获取token结果："+token);
        return token;
    }

    /**
     * UUID生成sysid
     * @return
     */
    public String getSysId(){
        return UUID.randomUUID().toString();
    }

    /**
     * 调用平台接口注册账号
     * @param user
     */
    public JSONObject registerUser(User user,String sysId) {
        JSONObject userObj = new JSONObject();
        userObj.put("sysid",sysId);
        userObj.put("loginName",user.getUserName());
        userObj.put("name", user.getRealName());
        userObj.put("password","123456" );
        userObj.put("register_time","2020-05-28");
        userObj.put("email",user.getEamil());
        userObj.put("gender","0" );
        userObj.put("birth","2017-04-12");
        userObj.put("mobile_phone",user.getUserTel());
        userObj.put("company","中服软件");
        userObj.put("province","陕西省");
        userObj.put("city","西安市");
        userObj.put("address","雁塔区高新二路");
        userObj.put("major","计算机科学");
        userObj.put("education","本科");
        String token=this.getToken();
        logger.info("调用平台接口注册用户入参："+userObj.toString());
        String jsonStr = HttpRequestUtil.sendPost(BASE_URL +"/csaas/api/addPtUser?access_token=" + token, userObj.toString());
        logger.info("调用平台接口注册用户返参："+jsonStr);
        JSONObject jsonObj = JSONObject.parseObject(jsonStr);
        return jsonObj;
    }

    /**
     * 调用平台接口记录日志
     * @param username
     * @param sysId
     */
    public JSONObject writeLog(String username,String sysId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("loginName",username);
        jsonObject.put("sysid",sysId);
        jsonObject.put("operation","查看");
        jsonObject.put("object","进入系统");
        jsonObject.put("data",username+"登录系统");
        String token=this.getToken();
        logger.info("调用平台接口记录日志入参："+jsonObject.toString());
        String jsonStr = HttpRequestUtil.sendPost(BASE_URL +"/csaas/api/writeLog?access_token=" + token, jsonObject.toString());
        logger.info("调用平台接口记录日志返参："+jsonStr);
        JSONObject jsonObj = JSONObject.parseObject(jsonStr);
        return jsonObj;
    }

    /**
     * 检查系统有效性
     * @return
     */
    public JSONObject checkSysStatus(String sysId){
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("sysid",sysId);
        String token = this.getToken();
        logger.info("检查系统有效性入参："+jsonObject.toString());
        String jsonStr = HttpRequestUtil.sendPost(BASE_URL + "/csaas/api/getSystemInfo?access_token=" + token, jsonObject.toString());
        logger.info("检查系统有效性出参："+jsonStr);
        JSONObject jsonObj = JSONObject.parseObject(jsonStr);
        return jsonObj;
    }
}
