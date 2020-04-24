package com.taikesoft.itsm.itil.plan;

import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.taikesoft.itsm.common.Constant;
import com.taikesoft.itsm.itil.cmdb.InstcdService;
import com.taikesoft.itsm.model.OpPlan;
import com.taikesoft.itsm.system.controller.BaseController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 运维计划控制器
 */
public class PlanController extends BaseController {

    public static final Log log = Log.getLog(PlanController.class);

    /**
     * 运维计划列表
     */
    public void list() {
        // 水利站
        setAttr("instcds", InstcdService.me.getInstcds());
        // 计划类型
        setAttr("planTypes", Constant.PLAN_TYPE_ALL);

        StringBuilder params = new StringBuilder();
        Map<String, Object> equalQo = new HashMap<>();
        Map<String, Object> likeQo = new HashMap<>();
        // 水利站
        String orgId = getPara("instgd");
        if (StrKit.notBlank(orgId)) {
            equalQo.put("instgd", orgId);
            params.append("&instgd=").append(orgId);
        }
        // 排涝站
        String stcd = getPara("stcd");
        if (StrKit.notBlank(stcd)) {
            likeQo.put("stcd", stcd);
            params.append("&stcd=").append(stcd);
        }
        // 人员id
        String userId = getPara("userId");
        if (StrKit.notBlank(userId)) {
            equalQo.put("userId", userId);
            params.append("&userId=").append(userId);
        }
        String username = getPara("username");
        if (StrKit.notBlank(username)) {
            likeQo.put("username", username);
            params.append("&username=").append(username);
        }
        // 计划时间
        try {
            Date planDateFrom = getParaToDate("planDateFrom");
            if (planDateFrom != null) {
                equalQo.put("planDateFrom", planDateFrom);
                params.append("&planDateFrom=").append(getPara("planDateFrom"));
            }
            Date planDateTo = getParaToDate("planDateTo");
            if (planDateTo != null) {
                equalQo.put("planDateTo", planDateTo);
                params.append("&planDateTo=").append(getPara("planDateTo"));
            }
        } catch (Exception e) {
            log.error("接收日期参数转换发生异常", e);
            e.printStackTrace();
        }
        // 执行状态
        Integer executed = getParaToInt("executed");
        if (executed != null) {
            equalQo.put("executed", executed.intValue());
            params.append("&executed=").append(executed.intValue());
        }
        // 计划类型
        String planType = getPara("planType");
        if (StrKit.notBlank(planType)) {
            equalQo.put("planType", planType);
            params.append("&planType=").append(planType);
        }
        // 数据有效性
        equalQo.put("isvalid", "1");

        // 分页
        Integer pageNumber = getParaToInt("pn");
        if (pageNumber == null) {
            pageNumber = 1;
        }

        Integer pageSize = getParaToInt("ps");
        if (pageSize == null) {
            pageSize = Constant.DEFAULT_PAGE_SIZE;
        }

        Page<Record> records = PlanService.me.getPages(equalQo, likeQo, pageNumber, pageSize);

        setAttr("controller", "plan");

        setAttr("records", records.getList());
        setAttr("pageNumber", pageNumber);
        setAttr("pageSize", pageSize);
        setAttr("totalPage", records.getTotalPage());
        setAttr("totalRow", records.getTotalRow());
        setAttr("searchParams", params.toString());
        setAttr("first", pageSize * pageNumber - pageSize + 1);
        setAttr("last", pageSize * pageNumber > records.getTotalRow() ? records.getTotalRow() : pageSize * pageNumber);

        keepPara();

        render("plan_index.html");
    }

    /**
     * 查看执行详情
     */
    public void detail() {
        String pid = getPara();

        OpPlan plan = PlanService.dao.findById(pid);
        if (plan == null) {
            render("");
            return;
        }
        setAttr("plan", plan);

        if (Constant.PLAN_TYPE_CONSTRUCT.equals(plan.getPlanType())) {
            setAttr("log", PlanService.me.getRepairLog(pid));
            render("plan_execute_construct.html");
        } else if (Constant.PLAN_TYPE_REPAIR.equals(plan.getPlanType())) {
            setAttr("log", PlanService.me.getRepairLog(pid));
            render("plan_execute_repair.html");
        } else if (Constant.PLAN_TYPE_PATROL.equals(plan.getPlanType())) {
            setAttr("log", PlanService.me.getInspectLog(pid));
            render("plan_execute_inspect.html");
        } else {
            setAttr("log", PlanService.me.getOtherLog(pid));
            render("plan_execute_other.html");
        }
    }
}
