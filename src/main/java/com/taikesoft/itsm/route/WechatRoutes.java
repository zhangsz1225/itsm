package com.taikesoft.itsm.route;

import com.jfinal.config.Routes;
import com.taikesoft.itsm.wechat.ApiController;

/**
 * 微信相关路由
 */
public class WechatRoutes extends Routes {
    @Override
    public void config() {
        add("/api", ApiController.class);
    }
}
