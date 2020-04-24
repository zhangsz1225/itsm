package com.taikesoft.itsm.route;

import com.jfinal.config.Routes;
import com.taikesoft.itsm.system.controller.*;

/**
 * 后台管理路由
 * 注意：自 jfinal 3.0 开始，baseViewPath 改为在 Routes 中独立配置
 *      并且支持 Routes 级别的 Interceptor，这类拦截器将拦截所有
 *      在此 Routes 中添加的 Controller，行为上相当于 class 级别的拦截器
 *      Routes 级别的拦截器特别适用于后台管理这样的需要统一控制权限的场景
 *      减少了代码冗余
 */
public class SystemRoutes extends Routes {
    @Override
    public void config() {
        setBaseViewPath("/_view/system");

        add("/", IndexController.class);
        add("/login", LoginController.class, "/");
        add("/main", MainController.class, "/");
        add("/center", CenterController.class, "/");
        add("/role", RoleController.class, "/");
        add("/menu", MenuController.class, "/");
        add("/user", UserController.class, "/");
    }
}
