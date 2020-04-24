package com.taikesoft.itsm.system.weixin;

import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;

/**
 * 微信小程序服务接口
 */
public class WeiXinController extends Controller {

    /**
     * 登录
     */
    public void login() {
        String username = getPara("username");
        String password = getPara("password");


    }

    /**
     * 查询计划
     */
    public void getPlans() {
        // 用户id
        String userId = getPara("userId");
        if (StrKit.isBlank(userId)) {

            return;
        }

        // 用户名
        String name = getPara("name");

    }


}
