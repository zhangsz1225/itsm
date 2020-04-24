package com.taikesoft.itsm.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.Ret;
import com.taikesoft.itsm.wechat.LoginService;

/**
 * 微信小程序接口wxid验证, 必须是系统中的账号才可调用接口
 */
public class WeiXinInterceptor implements Interceptor {

    @Override
    public void intercept(Invocation inv) {
        Controller c = inv.getController();
        String wxid = c.getPara("wxid");

        if (c.getRequest().getMethod().equalsIgnoreCase("POST")) {
            String data = HttpKit.readData(c.getRequest());
            try {
                JSONObject d = JSON.parseObject(data);
                wxid = d.getString("wxid");
            } catch (Exception e) {
                e.printStackTrace();
                c.renderJson(Ret.fail("msg", "请求参数解析失败"));
                return;
            }
        }

        if (!LoginService.me.validateWxid(wxid)) {
            inv.getController().renderJson(Ret.fail("msg", "微信id无效"));
            return;
        }
        inv.invoke();
    }
}
