package com.taikesoft.itsm.common;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.jfinal.config.*;
import com.jfinal.core.JFinal;
import com.jfinal.json.MixedJsonFactory;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.cron4j.Cron4jPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.template.Engine;
import com.taikesoft.itsm.interceptor.LoginSessionInterceptor;
import com.taikesoft.itsm.model._MappingKit;
import com.taikesoft.itsm.route.IncidentRoutes;
import com.taikesoft.itsm.route.OperationRoutes;
import com.taikesoft.itsm.route.SystemRoutes;
import com.taikesoft.itsm.route.WechatRoutes;

import java.sql.Connection;

public class ItsmConfig extends JFinalConfig {

    private static Prop p;

    private WallFilter wallFilter;

    public static void main(String[] args) {
        JFinal.start("src/main/webapp", 80, "/", 5);
    }

    // 先加载开发环境配置，再追加生产环境的少量配置覆盖掉开发环境配置
    static void loadConfig() {
        if (p == null) {
            p = PropKit.use("config-dev.txt").appendIfExists("config-pro.txt");
        }
    }

    @Override
    public void configConstant(Constants me) {
        loadConfig();

        me.setDevMode(p.getBoolean("devMode", false));
        me.setJsonFactory(MixedJsonFactory.me());

        Constant.DEFAULT_PAGE_SIZE = p.getInt("pageSize", 10);

        // 支持 Controller、Interceptor 之中使用 @Inject 注入业务层，并且自动实现 AOP
        me.setInjectDependency(true);
    }

    /**
     * 路由拆分到 FrontRutes 与 AdminRoutes 之中配置的好处：
     * 1：可分别配置不同的 baseViewPath 与 Interceptor
     * 2：避免多人协同开发时，频繁修改此文件带来的版本冲突
     * 3：避免本文件中内容过多，拆分后可读性增强
     * 4：便于分模块管理路由
     */
    @Override
    public void configRoute(Routes me) {
        me.add(new SystemRoutes());
        me.add(new OperationRoutes());
        me.add(new IncidentRoutes());
        me.add(new WechatRoutes());
    }

    /**
     * 配置模板引擎，通常情况只需配置共享的模板函数
     */
    @Override
    public void configEngine(Engine me) {
        me.setDevMode(p.getBoolean("engineDevMode", false));

//        // 添加角色、权限指令
//        me.addDirective("role", RoleDirective.class);
//        me.addDirective("permission", PermissionDirective.class);
//        me.addDirective("perm", PermissionDirective.class);		// 配置一个别名指令
//
//        // 添加角色、权限 shared method
//        me.addSharedMethod(AdminAuthKit.class);

//        me.addSharedFunction("/_view/common/__layout.html");
        me.addSharedFunction("/_view/common/_paginate.html");
//
//        me.addSharedFunction("/_view/_admin/common/__admin_layout.html");
//        me.addSharedFunction("/_view/_admin/common/_admin_paginate.html");
    }

    /**
     * 抽取成独立的方法，便于 _Generator 中重用该方法，减少代码冗余
     */
    public static DruidPlugin getDruidPlugin() {
        loadConfig();
        DruidPlugin plugin = new DruidPlugin(p.get("jdbcUrl"), p.get("user"), p.get("password").trim());
        String dataSourceName = p.get("dataSourceName");
        if (StrKit.notBlank(dataSourceName)) {
            plugin.setName(dataSourceName);
        }
        return plugin;
    }

    @Override
    public void configPlugin(Plugins me) {
        DruidPlugin druidPlugin = getDruidPlugin();
        wallFilter = new WallFilter();			// 加强数据库安全
        wallFilter.setDbType("mysql");
        druidPlugin.addFilter(wallFilter);
        druidPlugin.addFilter(new StatFilter());	// 添加 StatFilter 才会有统计数据
        me.add(druidPlugin);

        ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
        arp.setTransactionLevel(Connection.TRANSACTION_READ_COMMITTED);
        _MappingKit.mapping(arp);
        // 强制指定复合主键的次序，避免不同的开发环境生成在 _MappingKit 中的复合主键次序不相同
//        arp.setPrimaryKey("document", "mainMenu,subMenu");
        me.add(arp);
        arp.setShowSql(p.getBoolean("devMode", false));

        arp.getEngine().setToClassPathSourceFactory();
//        arp.addSqlTemplate("/sql/all_sqls.sql");

        me.add(new EhCachePlugin());

        me.add(new Cron4jPlugin(p));
    }

    @Override
    public void configInterceptor(Interceptors me) {
        me.add(new LoginSessionInterceptor());
    }

    @Override
    public void configHandler(Handlers me) {
//        me.add(DruidKit.getDruidStatViewHandler()); // druid 统计页面功能
//        me.add(new UrlSeoHandler());			 	// index、detail 两类 action 的 url seo
    }

    /**
     * 本方法会在 jfinal 启动过程完成之后被回调，详见 jfinal 手册
     */
    public void onStart() {
        // 调用不带参的 renderJson() 时，排除对 loginAccount、remind 的 json 转换
//        JsonRender.addExcludedAttrs(
//                LoginService.loginAccountCacheName,
//                LoginSessionInterceptor.remindKey,
//                FriendInterceptor.followNum, FriendInterceptor.fansNum, FriendInterceptor.friendRelation
//        );

        // 让 druid 允许在 sql 中使用 union
        // https://github.com/alibaba/druid/wiki/%E9%85%8D%E7%BD%AE-wallfilter
        wallFilter.getConfig().setSelectUnionCheck(false);
    }
}
