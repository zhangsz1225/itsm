package com.taikesoft.itsm.route;

import com.jfinal.config.Routes;
import com.taikesoft.itsm.itil.plan.PlanController;

/**
 * 运维模块路由
 */
public class OperationRoutes extends Routes {

    @Override
    public void config() {
        setBaseViewPath("/_view/itil");

        add("/plan", PlanController.class, "/");
    }
}
