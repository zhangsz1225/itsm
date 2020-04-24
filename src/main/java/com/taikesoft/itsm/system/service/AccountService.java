package com.taikesoft.itsm.system.service;

import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.taikesoft.itsm.model.SysAccount;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户账号服务类
 */
public class AccountService {

    public static final AccountService me = new AccountService();
    public static final SysAccount dao = new SysAccount().dao();

    /**
     * 获取用户实体
     * @param id
     * @return
     */
    public SysAccount findById(Integer id) {
        return dao.findFirst("select * from sys_account where id=? limit 1 ", id);
    }

    /**
     * 获取用户账号
     * @return
     */
    public List<SysAccount> getUsers(Map<String, Object> equalQo, Map<String, Object> likeQo) {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from sys_account where 1=1 ");
        List<Object> args = new ArrayList<>();

        if (equalQo != null) {
            if (equalQo.containsKey("status")) {
                sql.append(" and status=? ");
                args.add(equalQo.get("status"));
            }
        }
        if (likeQo != null) {
            if (likeQo.containsKey("name")) {
                sql.append(" and name like ? ");
                args.add("%" + likeQo.get("name") + "%");
            }
        }

        sql.append(" order by sort_number, name ");
        return dao.find(sql.toString(), args.toArray());
    }

    /**
     * 解绑账号对应的微信
     * @param id 账号id
     * @return
     */
    public Ret unbind(String id) {
        if (StrKit.isBlank(id)) {
            return Ret.fail("msg", "参数id不正确");
        }

        SysAccount account = dao.findById(id);
        if (account == null) {
            return Ret.fail("msg", "未找到对应的账号");
        }

        account.setWxid(null);
        account.update();

        return Ret.ok();
    }

    /**
     * 更新账号状态
     * @param id 账号id
     * @param status 最新状态
     * @return
     */
    public Ret updateStatus(String id, Integer status) {
        if (StrKit.isBlank(id)) {
            return Ret.fail("msg", "参数id不正确");
        }

        SysAccount account = dao.findById(id);
        if (account == null) {
            return Ret.fail("msg", "未找到对应的账号");
        }

        account.setStatus(status);
        account.update();

        return Ret.ok();
    }

    public Ret add(SysAccount account) {
        account.save();
        return Ret.ok("msg","保存成功");
    }

    public Ret update(SysAccount account) {
        account.update();
        return Ret.ok("msg", "修改成功");
    }

    public Ret del(String ids) {
        Db.delete("delete from sys_account where id in (" + ids + ")");
        return Ret.ok("msg", "删除成功");
    }
}
