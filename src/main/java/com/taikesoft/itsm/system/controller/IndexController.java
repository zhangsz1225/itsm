package com.taikesoft.itsm.system.controller;

import com.jfinal.core.Controller;

public class IndexController extends Controller {

    /**
     * 显示登录界面
     */
    public void index() {
        keepPara("returnUrl");  // 保持住 returnUrl 这个参数，以便在登录成功后跳转到该参数指向的页面
        render("login_index.html");
    }
}
