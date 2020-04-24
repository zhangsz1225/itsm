package com.taikesoft.itsm.system.controller;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.taikesoft.itsm.interceptor.AuthInterceptor;
import com.taikesoft.itsm.model.SysMenu;
import com.taikesoft.itsm.system.service.MenuService;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页面控制器
 */
@Before(AuthInterceptor.class)
public class MainController extends BaseController {

    /**
     * 首页 外部大边框
     */
    public void index() {
        // 顶部菜单
        List<SysMenu> topMenus = MenuService.me.getTopMenus();
        setAttr("menus", topMenus);

        // 组装第一个iframe
        if (topMenus.size() > 0) {
            setAttr("iframe", "/main/left/" + topMenus.get(0).getId());
        }

        render("index.html");
    }

    /**
     * 获取左侧菜单
     */
    public void left() {
        String id = getPara();
        if (StrKit.notBlank(id)) {
            // 查询二级菜单
            List<SysMenu> menus = MenuService.me.getMenusByParentId(id);
            setAttr("menus", menus);

            // 查询三级菜单
            List<String> menuIds = new ArrayList<String>();
            for (SysMenu menu : menus) {
                if (menu != null)
                    menuIds.add(menu.getId());
            }
            List<SysMenu> thirdMenus = MenuService.me.getMenusByParentIds(menuIds);
            setAttr("thirdMenus", thirdMenus);
        }

        render("left.html");
    }

}
