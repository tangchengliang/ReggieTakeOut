package com.tcl.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tcl.reggie.common.R;
import com.tcl.reggie.entity.User;
import com.tcl.reggie.service.UserService;
import com.tcl.reggie.utils.SMSUtils;
import com.tcl.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        // 获取手机号
        String phone = user.getPhone();
        if(StringUtils.hasText(phone)){
            // 生成随机4位验证码
//            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            String code = "1234";
            // 调用阿里云发送短信
//            SMSUtils.sendMessage("瑞吉外卖","",phone,code);
            // 将生成的验证码保存到session
            session.setAttribute(phone, code);

            return R.success("短信发送成功！！");
        }
        return R.error("短信发送失败");
    }

    /**
     * 移动端登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());
        // 获取手机号
        String phone = map.get("phone").toString();
        // 获取验证码
        String code = map.get("code").toString();
        // 从session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);
        // 进行比对
        if(codeInSession!=null && codeInSession.equals(code)){
            // 登录成功
            // 判断是否是新用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if(user==null){
                // 如果是新用户，自动完成注册
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }

        return R.error("登录失败！！！");
    }
}
