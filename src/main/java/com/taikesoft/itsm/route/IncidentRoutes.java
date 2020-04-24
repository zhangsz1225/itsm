package com.taikesoft.itsm.route;

import com.jfinal.config.Routes;
import com.taikesoft.itsm.incident.IncidentController;

/**
 * 运维模块路由
 */
public class IncidentRoutes extends Routes {

    @Override
    public void config() {
        setBaseViewPath("/_view/incident");

        add("/incident", IncidentController.class, "/");
    }
}
