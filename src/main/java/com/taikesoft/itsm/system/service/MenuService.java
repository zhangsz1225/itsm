package com.taikesoft.itsm.system.service;

import com.taikesoft.itsm.model.SysMenu;

import java.util.ArrayList;
import java.util.List;

public class MenuService {

    public static final MenuService me = new MenuService();

    /**
     * 获取顶级菜单
     * @return
     */
    public List<SysMenu> getTopMenus() {
        String sql = "select * from sys_menu where parent_id is null and status=1 order by sort_number";
        return SysMenu.dao.find(sql);
    }

    /**
     * 根据父菜单id获取下级菜单
     * @param parentId
     * @return
     */
    public List<SysMenu> getMenusByParentId(String parentId) {
        String sql = "select m.*, (select count(t.id) from sys_menu t where t.status=1 and t.parent_id=m.id) child from sys_menu m where m.parent_id=? and m.status=1 order by m.sort_number, m.name";
        return SysMenu.dao.find(sql, parentId);
    }

    /**
     * 根据父菜单id集合获取对应的下级菜单集合
     * @param parentIds
     * @return
     */
    public List<SysMenu> getMenusByParentIds(List<String> parentIds) {
        List<SysMenu> result = new ArrayList<SysMenu>();
        if (parentIds.size() > 0) {
            StringBuilder in = new StringBuilder();
            for (String id : parentIds) {
                in.append("?,");
            }
            in.deleteCharAt(in.length() - 1);

            String sql = "select * from sys_menu where parent_id in (" + in.toString() + ") and status=1 order by sort_number, name";
            result = SysMenu.dao.find(sql, parentIds.toArray());
        }

        return result;
    }
}
