package com.taikesoft.itsm.system.service;

import com.jfinal.kit.HashKit;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import com.taikesoft.itsm.kit.EmailKit;
import com.taikesoft.itsm.model.SysAccount;
import com.taikesoft.itsm.model.SysAuthCode;
import com.taikesoft.itsm.model.SysSession;

import java.util.Date;

public class LoginService {

    public static final LoginService me = new LoginService();

    private SysAccount accountDao = new SysAccount().dao();

    // 存放登录用户的 cacheName
    public static final String loginAccountCacheName = "loginAccount";

    // "platformId" 仅用于 cookie 名称，其它地方如 cache 中全部用的 "sessionId" 来做 key
    public static final String sessionIdName = "platformId";

    /**
     * 登录成功返回 sessionId 与 loginAccount，否则返回一个 msg
     */
    public Ret login(String userName, String password, boolean keepLogin, String loginIp) {
        userName = userName.toLowerCase().trim();
        password = password.trim();
        SysAccount loginAccount = accountDao.findFirst("select * from sys_account where userName=? limit 1", userName);
        if (loginAccount == null) {
            return Ret.fail("msg", "用户名或密码不正确");
        }
        if (loginAccount.isStatusLockId()) {
            return Ret.fail("msg", "账号已被锁定");
        }

        String hashedPass = HashKit.md5(password);
        // 未通过密码验证
        if (!loginAccount.getPassword().equals(hashedPass)) {
            return Ret.fail("msg", "用户名或密码不正确");
        }

        // 如果用户勾选保持登录，暂定过期时间为 3 年，否则为 120 分钟，单位为秒
        long liveSeconds =  keepLogin ? 3 * 365 * 24 * 60 * 60 : 120 * 60;
        // 传递给控制层的 cookie
        int maxAgeInSeconds = (int)(keepLogin ? liveSeconds : -1);
        // expireAt 用于设置 session 的过期时间点，需要转换成毫秒
        long expireAt = System.currentTimeMillis() + (liveSeconds * 1000);
        // 保存登录 session 到数据库
        SysSession session = new SysSession();
        String sessionId = StrKit.getRandomUUID();
        session.setId(sessionId);
        session.setAccountId(loginAccount.getId());
        session.setExpireAt(expireAt);
        if ( ! session.save()) {
            return Ret.fail("msg", "保存 session 到数据库失败，请联系管理员");
        }

        loginAccount.removeSensitiveInfo();                                 // 移除 password 与 salt 属性值
        loginAccount.put("sessionId", sessionId);                          // 保存一份 sessionId 到 loginAccount 备用
        CacheKit.put(loginAccountCacheName, sessionId, loginAccount);

        createLoginLog(loginAccount.getId(), loginIp);

        return Ret.ok(sessionIdName, sessionId)
                .set(loginAccountCacheName, loginAccount)
                .set("maxAgeInSeconds", maxAgeInSeconds);   // 用于设置 cookie 的最大存活时间
    }

    public SysAccount getLoginAccountWithSessionId(String sessionId) {
        return CacheKit.get(loginAccountCacheName, sessionId);
    }

    /**
     * 通过 sessionId 获取登录用户信息
     * sessoin表结构：session(id, accountId, expireAt)
     *
     * 1：先从缓存里面取，如果取到则返回该值，如果没取到则从数据库里面取
     * 2：在数据库里面取，如果取到了，则检测是否已过期，如果过期则清除记录，
     *     如果没过期则先放缓存一份，然后再返回
     */
    public SysAccount loginWithSessionId(String sessionId, String loginIp) {
        SysSession session = SysSession.dao.findById(sessionId);
        if (session == null) {      // session 不存在
            return null;
        }
        if (session.isExpired()) {  // session 已过期
            session.delete();		// 被动式删除过期数据，此外还需要定时线程来主动清除过期数据
            return null;
        }

        SysAccount loginAccount = accountDao.findById(session.getAccountId());
        // 找到 loginAccount 并且 是正常状态 才允许登录
        if (loginAccount != null && loginAccount.isStatusOk()) {
            loginAccount.removeSensitiveInfo();                                 // 移除 password 与 salt 属性值
            loginAccount.put("sessionId", sessionId);                          // 保存一份 sessionId 到 loginAccount 备用
            CacheKit.put(loginAccountCacheName, sessionId, loginAccount);

            createLoginLog(loginAccount.getId(), loginIp);
            return loginAccount;
        }
        return null;
    }

    /**
     * 创建登录日志
     */
    private void createLoginLog(Integer accountId, String loginIp) {
        Record loginLog = new Record().set("account_id", accountId).set("ip", loginIp).set("login_at", new Date()).set("login_type", "web");
        Db.save("sys_login_log", loginLog);
    }

    /**
     * 发送密码找回授权邮件
     */
    public Ret sendRetrievePasswordAuthEmail(String userName) {
        if (StrKit.isBlank(userName) || userName.indexOf('@') == -1) {
            return Ret.fail("msg", "email 格式不正确，请重新输入");
        }
        userName = userName.toLowerCase().trim();   // email 转成小写
        SysAccount account = accountDao.findFirst("select * from sys_account where userName=? limit 1", userName);
        if (account == null) {
            return Ret.fail("msg", "您输入的 email 还没注册，无法找回密码");
        }

        String authCode = AuthCodeService.me.createRetrievePasswordAuthCode(account.getId());

        String title = "JFinal 密码找回邮件";
        String content = "在浏览器地址栏里输入并访问下面链接即可重置密码：\n\n"
                + " http://www.jfinal.com/login/retrievePassword?authCode="
                + authCode;

        String emailServer = PropKit.get("emailServer");
        String fromEmail = PropKit.get("fromEmail");
        String emailPass = PropKit.get("emailPass");
        String toEmail = account.getStr("userName");
        try {
            EmailKit.sendEmail(emailServer, fromEmail, emailPass, toEmail, title, content);
            return Ret.ok("msg", "密码找回邮件已发送至邮箱，请收取邮件并重置密码");
        } catch (Exception e) {
            return Ret.fail("msg", "密码找回邮件发送失败，可能是邮件服务器出现故障，请去JFinal官方QQ群留言给群主");
        }
    }

    /**
     * 找回密码
     */
    public Ret retrievePassword(String authCodeId, String newPassword) {
        if (StrKit.isBlank(newPassword)) {
            return Ret.fail("msg", "密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Ret.fail("msg", "密码长度不能小于6");
        }

        SysAuthCode authCode = AuthCodeService.me.getAuthCode(authCodeId);
        if (authCode != null && authCode.isValidRetrievePasswordAuthCode()) {
            String salt = HashKit.generateSaltForSha256();
            newPassword = HashKit.sha256(salt + newPassword);
            int accountId = authCode.getAccountId();
            int result = Db.update("update account set password=?, salt=? where id=? limit 1", newPassword, salt, accountId);
            if (result > 0) {
                return Ret.ok("msg", "密码更新成功，现在即可登录");
            } else {
                return Ret.fail("msg", "未找到账号，请联系管理员");
            }
        } else {
            return Ret.fail("msg", "authCode 不存在或已经失效，可以尝试重新发送找回密码邮件");
        }
    }

    /**
     * 退出登录
     */
    public void logout(String sessionId) {
        if (sessionId != null) {
            CacheKit.remove(loginAccountCacheName, sessionId);
            SysSession.dao.deleteById(sessionId);
        }
    }

    /**
     * 从数据库重新加载登录账户信息
     */
    public void reloadLoginAccount(SysAccount loginAccountOld) {
        String sessionId = loginAccountOld.get("sessionId");
        SysAccount loginAccount = accountDao.findFirst("select * from sys_account where id=? limit 1", loginAccountOld.getId());
        loginAccount.removeSensitiveInfo();               // 移除 password 与 salt 属性值
        loginAccount.put("sessionId", sessionId);        // 保存一份 sessionId 到 loginAccount 备用

        // 集群方式下，要做一通知其它节点的机制，让其它节点使用缓存更新后的数据，
        // 将来可能把 account 用 id : obj 的形式放缓存，更新缓存只需要 CacheKit.remove("account", id) 就可以了，
        // 其它节点发现数据不存在会自动去数据库读取，所以未来可能就是在 AccountService.getById(int id)的方法引入缓存就好
        // 所有用到 account 对象的地方都从这里去取
        CacheKit.put(loginAccountCacheName, sessionId, loginAccount);
    }
}
