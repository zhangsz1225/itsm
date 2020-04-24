package com.taikesoft.itsm.wechat;

import com.jfinal.kit.HashKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.taikesoft.itsm.model.SysAccount;

import java.util.Date;

public class LoginService {

    public static final LoginService me = new LoginService();

    private SysAccount accountDao = new SysAccount().dao();

    public Ret login(String username, String password, String wxid, String loginIp) {
        if(StrKit.isBlank(wxid)){
            return Ret.fail("msg","微信账号不能为空");
        }else{
            //验证微信账号
            SysAccount wxAccount = accountDao.findFirst("select * from sys_account where wxid=? limit 1", wxid);
            if(wxAccount == null){
                //未注册绑定
                if (StrKit.isBlank(username) || StrKit.isBlank(password)) {
                    return Ret.fail("msg", "账号或密码不能为空");
                }
                SysAccount loginAccount = accountDao.findFirst("select * from sys_account where userName=? limit 1", username.trim());
                if (loginAccount == null) {
                    return Ret.fail("msg", "账号或密码不正确");
                }
                if (loginAccount.isStatusLockId()) {
                    return Ret.fail("msg", "账号已被锁定");
                }
                String hashedPass = HashKit.md5(password.trim());
                // 未通过密码验证
                if (!loginAccount.getPassword().equals(hashedPass)) {
                    return Ret.fail("msg", "账号或密码不正确");
                }

                Db.tx(() -> {
                    //更新登录ip，登录时间，绑定微信账号

                    Record wxRecord = Db.findById("sys_account",loginAccount.getId()).set("wxid",wxid).set("login_ip", loginIp).set("login_at", new Date());
                    Db.update("sys_account",wxRecord);
                    //登录日志
                    Record loginLog = new Record().set("account_id", loginAccount.getId()).set("ip", loginIp).set("login_at", new Date()).set("login_type", "wechat");
                    Db.save("sys_login_log", loginLog);
                    return true;
                });
                return Ret.ok("msg","登录成功").set("data",JsonKit.toJson(loginAccount.removeSensitiveInfo()));
            }else{
                //已注册绑定
                if (wxAccount.isStatusLockId()) {
                    return Ret.fail("msg", "账号已被锁定");
                }

                Db.tx(() -> {
                    //更新登录ip，登录时间
                    Record wxRecord = Db.findById("sys_account",wxAccount.getId()).set("login_ip", loginIp).set("login_at", new Date());
                    Db.update("sys_account",wxRecord);
                    //登录日志
                    Record loginLog = new Record().set("account_id", wxAccount.getId()).set("ip", loginIp).set("login_at", new Date()).set("login_type", "wechat");
                    Db.save("sys_login_log", loginLog);
                    return true;
                });
                return Ret.ok("msg","登录成功").set("data",JsonKit.toJson(wxAccount.removeSensitiveInfo()));
            }
        }

    }

    /**
     * 验证微信id是否有效
     * @param wxid 微信id
     * @return
     */
    public boolean validateWxid(String wxid) {
        if (StrKit.isBlank(wxid)) {
            return false;
        }

        SysAccount wxAccount = accountDao.findFirst("select * from sys_account where wxid=? limit 1", wxid);
        return wxAccount != null;
    }

    /**
     * 根据wxid查询对应的账号
     * @param wxid
     * @return
     */
    public SysAccount getAccountByWxid(String wxid) {
        return accountDao.findFirst("select * from sys_account where wxid=? limit 1", wxid);
    }
}
