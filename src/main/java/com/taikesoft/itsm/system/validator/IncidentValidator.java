package com.taikesoft.itsm.system.validator;

import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.validate.Validator;

import java.util.Date;

public class IncidentValidator extends Validator {

    @Override
    protected void handleError(Controller c) {
        c.renderJson();
    }

    @Override
    protected void validate(Controller c) {

        setShortCircuit(true);
        validateRequired("opIncident.orgid", "org_msg", "请选择水利站！");
        validateRequired("opIncident.stcd", "stcd_msg", "请选择测站！");
        validateRequired("opIncident.title", "title_msg", "事件标题不能为空！");
        int status = c.getParaToInt("opIncident.status");
        if (status == 1) {
            validateRequired("opIncident.solution", "solution_msg", "解决方案描述不能为空！");
        }
        Date endDate = c.getParaToDate("opIncident.endDate");
        if (endDate == null) {
            validateRequired("opIncident.endDate", "endDate_msg", "客户填报的解决截止日期不能为空！");
        }
        int createPlan = c.getParaToInt("createPlan");
        if (createPlan == 1) {
            if(StrKit.isBlank(c.getPara("opIncident.rowguid"))){
                validateRequired("memo", "memo_msg", "计划描述不能为空！");
            }

        }
    }
}