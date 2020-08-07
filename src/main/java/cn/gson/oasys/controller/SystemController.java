package cn.gson.oasys.controller;

import cn.gson.oasys.model.dao.roledao.RoleDao;
import cn.gson.oasys.model.dao.user.DeptDao;
import cn.gson.oasys.model.dao.user.PositionDao;
import cn.gson.oasys.model.dao.user.UserDao;
import cn.gson.oasys.model.entity.role.Role;
import cn.gson.oasys.model.entity.user.Dept;
import cn.gson.oasys.model.entity.user.LoginRecord;
import cn.gson.oasys.model.entity.user.Position;
import cn.gson.oasys.model.entity.user.User;
import cn.gson.oasys.services.SystemService;
import cn.gson.oasys.services.user.UserLongRecordService;
import com.alibaba.fastjson.JSONObject;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.UserAgent;
import eu.bitwalker.useragentutils.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统相关操作
 */
@Controller
@RequestMapping("/")
public class SystemController {

    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

    @Value("${THIRD_APP_ID}")
    private String THIRD_APP_ID;

    @Value("${THIRD_APP_KEY}")
    private String THIRD_APP_KEY;

    @Value("${BASE_URL}")
    private String BASE_URL;

    @Value("${SSO_BASE_URL}")
    private String SSO_BASE_URL;

    @Autowired
    private SystemService systemService;

    @Autowired
    DeptDao deptdao;

    @Autowired
    PositionDao pdao;

    @Autowired
    RoleDao rdao;

    @Autowired
    UserDao udao;

    @Autowired
    UserLongRecordService ulService;

    /**
     * 用户注册接口（即系统开通接口）
     * @param userInfo
     * @param request
     * @return
     */
    @PostMapping("/registUser.jhtml")
    @ResponseBody
    public Map<String,Object> registUser(@RequestBody String userInfo, HttpServletRequest request){
        logger.info("/registUser.jhtml-用户注册接口-请求入参："+userInfo);
        Map<String,Object> result = new HashMap<String, Object>();
        try{
            JSONObject userObj = null;
            try {
                if (StringUtils.isEmpty(userInfo)){
                    result.put("success",false);
                    result.put("resultMessage","请求内容为空");
                    logger.info("/registUser.jhtml-模拟创建用户-响应结果："+JSONObject.toJSONString(result));
                    return result;
                }
                userObj = JSONObject.parseObject(userInfo);
            }catch (Exception e){
                logger.info("/registUser.jhtml-用户注册接口-请求内容解析异常："+e);
                result.put("success",false);
                result.put("resultMessage","请求内容解析异常");
                logger.info("/registUser.jhtml-用户注册接口-响应结果："+JSONObject.toJSONString(result));
                return result;
            }
            String thirdAppid = userObj.getString("thirdAppid");
            String thirdAppkey = userObj.getString("thirdAppkey");
            String loginName = userObj.getString("loginName");
            if(!THIRD_APP_ID.equals(thirdAppid)||!THIRD_APP_KEY.equals(thirdAppkey)){
                result.put("success",false);
                result.put("resultMessage","凭证不合法");
                logger.info("/registUser.jhtml-用户注册接口-响应结果："+JSONObject.toJSONString(result));
                return result;
            }

            if(StringUtils.isEmpty(loginName)){
                result.put("success",false);
                result.put("resultMessage","用户登录名(手机号或邮箱)不能为空");
                logger.info("/registUser.jhtml-用户注册接口-响应结果："+JSONObject.toJSONString(result));
                return result;
            }

            String sysId = systemService.getSysId();//获取系统id
            //新建部门
            Dept dept = new Dept();
            dept.setDeptName("总经办");
            dept.setDeptAddr("中服总部");
            dept.setEmail(userObj.getString("email"));
            dept.setDeptTel(userObj.getString("mobilePhone"));
            dept.setSysId(sysId);
            dept.setDeptmanager(0L);
            Dept adddept = deptdao.save(dept);
            //职位
            Position position = pdao.findOne(2L);
            //角色
            Role role = rdao.findOne(2L);
            //新建用户
            User user = new User();
            user.setUserName(loginName);
            user.setRealName(userObj.getString("realName"));
            user.setPassword("123456");
            user.setPinyin(loginName);
            user.setUserTel(userObj.getString("mobilePhone"));
            user.setEamil(userObj.getString("email"));
            user.setFatherId(adddept.getDeptmanager());
            user.setThemeSkin("blue");
            user.setSchool("西安");
            user.setUserEdu("本科");
            user.setDept(adddept);
            user.setRole(role);
            user.setPosition(position);
            udao.save(user);
            //返回成功信息
            result.put("success",true);
            result.put("sysid",sysId);
            result.put("resultMessage","用户添加成功");
            result.put("accessUrl","");
            logger.info("/registUser.jhtml-用户注册接口-响应结果："+JSONObject.toJSONString(result));
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.put("success",false);
            result.put("resultMessage","系统异常，请稍后再试！");
            logger.info("/registUser.jhtml-用户注册接口-异常信息："+e);
            return result;
        }
    }

    /**
     * 登录地址
     * @param info
     * @param request
     * @return
     */
    @GetMapping("/login.jhtml")
    public String login(String info, HttpServletRequest request, ModelMap modelMapt){
        logger.info("/login.jhtml-登录地址-请求入参："+info);
        String domainUrl = request.getRequestURL().toString().replace(request.getRequestURI(),"");
        String accesstoken=systemService.getToken();//获取token
        //封装数据,returnUrl为用户信息返回地址
        JSONObject jsoninfo = new JSONObject();
        jsoninfo.put("returnUrl", domainUrl+"/loginCserver.jhtml");
        jsoninfo.put("access_token", accesstoken);
        String infoString = "";
        try{
            infoString = URLEncoder.encode(jsoninfo.toJSONString(),"UTF-8");//必须转码
        }catch (Exception e){
            e.printStackTrace();
            logger.info("/login.jhtml-登录地址-编码异常："+e);
        }
        logger.info("/login.jhtml-登录地址-响应结果："+SSO_BASE_URL+"/sso.web/loginCserver?info="+jsoninfo.toJSONString());
        return "redirect:"+SSO_BASE_URL+"/sso.web/loginCserver?info="+infoString;
    }

    /**
     * 登录
     * @param info
     * @param req
     * @return
     */
    @GetMapping("/loginCserver.jhtml")
    public String loginCserver(String info, HttpSession session, HttpServletRequest req, ModelMap modelMap) throws UnknownHostException {
        logger.info("/loginCserver.jhtml-用户登录-请求入参："+info);
        try {
            if (StringUtils.isEmpty(info)){
                return "redirect:/login.jhtml";
            }else {
                JSONObject jsonget = JSONObject.parseObject(info);
                String access_token = jsonget.getString("access_token");
                //判断token是否改变
                if (access_token == null || !access_token.equals(systemService.getToken())) {
                    modelMap.put("msg", "非法访问,token不合法");
                    return "error/error";
                }
                String username = jsonget.getString("username");
                if (StringUtils.isEmpty(username)) {
                    modelMap.put("msg", "非法访问用户");
                    return "error/error";
                }
                User user = udao.findid(username);
                if (user == null) {
                    modelMap.put("msg", "未注册用户");
                    //跳转页面
                    return "error/error";
                }
                //系统有效性校验
                JSONObject jsonObj = systemService.checkSysStatus(user.getDept().getSysId());
                if(!jsonObj.getBoolean("success")){//禁止用户访问
                    modelMap.put("msg",jsonObj.getString("resultMessage"));//仅为示例
                    return "error/error";
                }
                Boolean usable = jsonObj.getBoolean("usable");
                if(!usable){//禁止用户访问
                    modelMap.put("msg","系统已过期，请续费");//仅为示例
                    return "error/error";
                }
                //登录日志写入
                try{
                    systemService.writeLog(username,user.getDept().getSysId());
                }catch (Exception e){
                    e.printStackTrace();
                    logger.info("/loginCserver.jhtml-用户登录-添加日志异常："+e);
                }
                session.setAttribute("userId", user.getUserId());
                Browser browser = UserAgent.parseUserAgentString(req.getHeader("User-Agent")).getBrowser();
                Version version = browser.getVersion(req.getHeader("User-Agent"));
                String info2 = browser.getName() + "/" + version.getVersion();
                String ip = InetAddress.getLocalHost().getHostAddress();
                /*新增登录记录*/
                ulService.save(new LoginRecord(ip, new Date(), info2, user));
                logger.info("/loginCserver.jhtml-用户登录-正常-ip："+ip);
                return "redirect:/index";
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.info("/loginCserver.jhtml-用户登录-异常："+e);
        }
        return "redirect:/index";
    }
}
