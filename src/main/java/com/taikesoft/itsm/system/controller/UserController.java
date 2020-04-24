package com.taikesoft.itsm.system.controller;

import com.jfinal.aop.Before;
import com.jfinal.kit.HashKit;
import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.taikesoft.itsm.model.SysAccount;
import com.taikesoft.itsm.system.service.AccountService;
import com.taikesoft.itsm.system.validator.UserValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
public class UserController extends BaseController {

    AccountService srv = AccountService.me;
    /**
     * 用户管理首页
     */
    public void index() {
        render("user_index.html");
    }

    /**
     * 获取所有用户账号
     */
    public void getUsers() {
        Map<String, Object> equalQo = new HashMap<>();
        Map<String, Object> likeQo = new HashMap<>();

        String name = getPara("name");
        if (StrKit.notBlank(name)) {
            likeQo.put("name", name);
        }
        Integer status = getParaToInt("status");
        if (status != null) {
            equalQo.put("status", status);
        }

        List<SysAccount> users = srv.getUsers(equalQo, likeQo);
        renderJson(users);
    }

    /**
     * 解绑微信账号
     */
    public void unbind() {
        String id = getPara("id");
        if (StrKit.isBlank(id)) {
            renderJson(Ret.fail().set("msg", "请求参数不正确"));
            return;
        }

        renderJson(srv.unbind(id));
    }

    /**
     * 锁定账号
     */
    public void ban() {
        String id = getPara("id");
        renderJson(srv.updateStatus(id, SysAccount.STATUS_LOCK_ID));
    }

    /**
     * 解锁账号
     */
    public void unban() {
        String id = getPara("id");
        renderJson(srv.updateStatus(id, SysAccount.STATUS_OK));
    }

    /**
     * 物理删除
     */
    public void del() {
        String ids = getPara("ids");
        Ret ret = srv.del(ids);
        renderJson(ret);
    }

    /**
     * 新增
     */
    @Before(Tx.class)
    public void add() {
        render("user_add.html");
    }

    /**
     * 修改
     */
    @Before(Tx.class)
    public void edit() {
        String id = getPara("id");
        SysAccount sysAccount =srv.findById(Integer.parseInt(id));
        setAttr("sysAccount", sysAccount);
        render("user_edit.html");
    }

    /**
     * 保存
     */
    @Before(UserValidator.class)
    public void save() {
        SysAccount tSysAccount = getModel(SysAccount.class);
        Integer id = tSysAccount.getId();
        String username = tSysAccount.getUsername();
        String name = tSysAccount.getName();
        String gender = tSysAccount.getGender();
        String phone = tSysAccount.getPhone();
        String email = tSysAccount.getEmail();
        String desc = tSysAccount.getDescription();
        Integer sortNumber = tSysAccount.getSortNumber();
        if ( id!= null) {
            //修改
            SysAccount entity = srv.findById(id);
            entity.setUsername(username);
            entity.setName(name);
            entity.setGender(gender);
            entity.setPhone(phone);
            entity.setEmail(email);
            entity.setDescription(desc);
            entity.setSortNumber(sortNumber);
            Ret ret = srv.update(entity);
            renderJson(ret);
        }
        else {
            //新增
            SysAccount entity = new SysAccount();
            entity.setUsername(username);
            entity.setPassword(HashKit.md5("11111"));
            entity.setName(name);
            entity.setGender(gender);
            entity.setPhone(phone);
            entity.setEmail(email);
            entity.setDescription(desc);
            entity.setSortNumber(sortNumber);
            entity.setStatus(1);
            Ret ret = srv.add(entity);
            renderJson(ret);
        }
    }
}
