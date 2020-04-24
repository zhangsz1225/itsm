package com.taikesoft.itsm.system.controller;

import com.jfinal.kit.JsonKit;
import com.taikesoft.itsm.model.SysMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单控制器
 */
public class MenuController extends BaseController {

    /**
     * 菜单首页
     */
    public void index() {

        render("menu_index.html");
    }

    /**
     * 获取菜单数据
     */
    public void getMenus() {
        List<SysMenu> menus = SysMenu.dao.find("select * from sys_menu where status=1 order by sort_number, name");

        List<SysMenu> result = buildMenu(null, menus);

        renderJson(JsonKit.toJson(result));
    }

    private List<SysMenu> buildMenu(String parentId, List<SysMenu> allMenus) {
        List<SysMenu> result = new ArrayList<>();
        if (parentId == null) {
            for (SysMenu menu : allMenus) {
                if (menu.getParentId() == null) {
                    result.add(menu);

                    result.addAll(buildMenu(menu.getId(), allMenus));
                }
            }
        } else {
            for (SysMenu menu : allMenus) {
                if (parentId.equals(menu.getParentId())) {
                    result.add(menu);

                    result.addAll(buildMenu(menu.getId(), allMenus));
                }
            }
        }
        return result;
    }
}
