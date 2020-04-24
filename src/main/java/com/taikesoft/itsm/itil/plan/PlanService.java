package com.taikesoft.itsm.itil.plan;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.taikesoft.itsm.model.OpInspectLog;
import com.taikesoft.itsm.model.OpOtherLog;
import com.taikesoft.itsm.model.OpPlan;
import com.taikesoft.itsm.model.OpRepairLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 运维计划服务
 */
public class PlanService {

    public static final PlanService me = new PlanService();
    public static final OpPlan dao = new OpPlan().dao();

    public Page<Record> getPages(Map<String, Object> equalQo, Map<String, Object> likeQo, Integer pageNumber, Integer pageSize) {
        String sqlPara = "select p.*, o.instnm, t.stnm ";
        StringBuilder sqlExceptSelect = new StringBuilder();
        sqlExceptSelect.append(" from op_plan p left join st_instcd_b o on p.org_id=o.ROWGUID left join st_stbprp_b t on p.stcd=t.ROWGUID where 1=1 ");
        List<Object> args = new ArrayList<>();
        genQo(equalQo, likeQo, sqlExceptSelect, args);
        return Db.paginate(pageNumber, pageSize, sqlPara, sqlExceptSelect.toString(), args.toArray());
    }

//    public int getCount(Map<String, Object> equalQo, Map<String, Object> likeQo) {
//        StringBuilder sb = new StringBuilder();
//        List<Object> args = new ArrayList<>();
//        sb.append("select count(p.id) from op_plan p ");
//        genQo(equalQo, likeQo, sb, args);
//        return
//    }

    public Record getPlanExtra(Map<String, Object> equalQo, Map<String, Object> likeQo) {
        StringBuilder sb = new StringBuilder();
        sb.append("select p.*,i.title,i.urgency, o.instnm, t.stnm ")
                .append(" from op_plan p ")
                .append(" left join op_incident i on p.incidentId=i.ROWGUID ")
                .append(" left join st_instcd_b o on p.org_id=o.ROWGUID left join st_stbprp_b t on p.stcd=t.ROWGUID ")
                .append(" where 1=1 ");
        List<Object> args = new ArrayList<>();
        genQo(equalQo, likeQo, sb, args);
        return Db.findFirst(sb.toString(), args.toArray());
    }

    private void genQo(Map<String, Object> equalQo, Map<String, Object> likeQo, StringBuilder sb, List<Object> args) {
        if (equalQo != null) {
            // id
            if (equalQo.containsKey("id")) {
                sb.append(" and p.id=? ");
                args.add(equalQo.get("id"));
            }
            // 水利站
            if (equalQo.containsKey("instgd")) {
                sb.append(" and p.org_id=? ");
                args.add(equalQo.get("instgd"));
            }
            // 人员
            if (equalQo.containsKey("userId")) {
                sb.append(" and p.user_id=? ");
                args.add(equalQo.get("userId"));
            }
            // 计划时间
            if (equalQo.containsKey("planDateFrom")) {
                sb.append(" and p.plan_date >= ? ");
                args.add(equalQo.get("planDateFrom"));
            }
            if (equalQo.containsKey("planDateTo")) {
                sb.append(" and p.plan_date <= ? ");
                args.add(equalQo.get("planDateTo"));
            }
            // 执行状态
            if (equalQo.containsKey("executed")) {
                sb.append(" and p.executed=? ");
                args.add(equalQo.get("executed"));
            }
            // 计划类型
            if (equalQo.containsKey("planType")) {
                sb.append(" and p.plan_type=? ");
                args.add(equalQo.get("planType"));
            }
            // 数据有效性
            if (equalQo.containsKey("isvalid")) {
                sb.append(" and p.isvalid=? ");
                args.add(equalQo.get("isvalid"));
            }
        }
        if (likeQo != null) {
            if (likeQo.containsKey("username")) {
                sb.append(" and p.username like ? ");
                args.add("%" + likeQo.get("username") + "%");
            }
            // 排涝站
            if (likeQo.containsKey("stcd")) {
                sb.append(" and p.stcd like ? ");
                args.add("%" + likeQo.get("stcd") + "%");
            }
            if (likeQo.containsKey("keywords")) {
                sb.append(" and (p.username like ? or o.INSTNM like ? or t.STNM like ? or p.plan_type like ? ) ");
                args.add("%" + likeQo.get("keywords") + "%");
                args.add("%" + likeQo.get("keywords") + "%");
                args.add("%" + likeQo.get("keywords") + "%");
                args.add("%" + likeQo.get("keywords") + "%");
            }
        }
        sb.append(" order by p.plan_date desc, p.org_id, p.stcd, p.plan_type, p.username ");
    }

    public OpRepairLog getRepairLog(String planId) {
        return new OpRepairLog().findFirst("select * from op_repair_log where plan_id=?", planId);
    }

    public OpOtherLog getOtherLog(String planId) {
        return new OpOtherLog().findFirst("select * from op_other_log where plan_id=?", planId);
    }

    public OpInspectLog getInspectLog(String planId) {
        return new OpInspectLog().findFirst("select * from op_inspect_log where plan_id=?", planId);
    }

}
