package com.taikesoft.itsm.system.controller;

/**
 * 首页控制器
 */
public class CenterController extends BaseController {

    /**
     * 系统首页
     */
    public void index() {

        render("center_index.html");
    }
}
