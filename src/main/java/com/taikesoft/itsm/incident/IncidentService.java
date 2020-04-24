package com.taikesoft.itsm.incident;

import com.jfinal.kit.Ret;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.taikesoft.itsm.model.OpAttachment;
import com.taikesoft.itsm.model.OpIncident;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IncidentService {

    public static final IncidentService me = new IncidentService();

    private OpIncident incidentDao = new OpIncident().dao();

    public List<OpIncident> getIncidents(Map<String, Object> equalQo, Map<String, Object> likeQo) {
        StringBuilder sql = new StringBuilder();
        sql.append("select i.*,a.attachmentNames,a.attachmentIds," +
                "p.username,p.plan_type,p.plan_date,s.stnm,b.instnm " +
                "from op_incident i " +
                "left join (select bid,GROUP_CONCAT(name) as attachmentNames,GROUP_CONCAT(id) as attachmentIds from op_attachment " +
                "where bid is not null and table_name='op_incident' and isValid = '1' group by bid)" +
                "a on a.bid = i.rowguid " +
                "left join op_plan p on p.incidentid = i.rowguid " +
                "left join st_instcd_b b on b.rowguid = i.orgid " +
                "left join st_stbprp_b s on s.rowguid = i.stcd " +
                "where 1=1 ");
        List<Object> args = new ArrayList<>();

        if (equalQo != null) {
            if (equalQo.containsKey("status")) {
                sql.append(" and i.status=? ");
                args.add(equalQo.get("status"));
            }
            if (equalQo.containsKey("orgId")) {
                sql.append(" and i.orgid=? ");
                args.add(equalQo.get("orgId"));
            }
            if (equalQo.containsKey("stcd")) {
                sql.append(" and p.stcd=? ");
                args.add(equalQo.get("stcd"));
            }
        }
        if (likeQo != null) {
            if (likeQo.containsKey("title")) {
                sql.append(" and i.title like ? ");
                args.add("%" + likeQo.get("title") + "%");
            }
            if (likeQo.containsKey("memo")) {
                sql.append(" and p.memo like ? ");
                args.add("%" + likeQo.get("memo") + "%");
            }
        }
        sql.append("order by field(i.urgency, '非常高', '高', '中', '低' ), p.plan_date desc");
        return incidentDao.find(sql.toString(), args.toArray());
    }

    public Ret update(OpIncident incident) {
        incident.update();
        return Ret.ok("msg", "修改成功");
    }

    public Ret save(OpIncident incident) {
        incident.save();
        return Ret.ok("msg", "新增成功");
    }

    public Page<Record> getPages(Map<String, Object> equalQo, Map<String, Object> likeQo, Integer pageNumber, Integer pageSize) {
        String sqlPara = "select i.*,p.username,p.plan_type,o.instnm,t.stnm ";
        StringBuilder sqlExceptSelect = new StringBuilder();
        sqlExceptSelect.append(" from op_incident i " +
                "left join op_plan p on p.incidentid = i.rowguid " +
                "left join st_instcd_b o on i.orgid = o.rowguid " +
                "left join st_stbprp_b t on i.stcd = t.rowguid where 1=1 ");
        List<Object> args = new ArrayList<>();
        genQo(equalQo, likeQo, sqlExceptSelect, args);
        return Db.paginate(pageNumber, pageSize, sqlPara, sqlExceptSelect.toString(), args.toArray());
    }

    private void genQo(Map<String, Object> equalQo, Map<String, Object> likeQo, StringBuilder sb, List<Object> args) {
        if (equalQo != null) {
            // 人员
            if (equalQo.containsKey("userId")) {
                sb.append(" and i.createId=? ");
                args.add(equalQo.get("userId"));
            }
            // 创建人
            if (equalQo.containsKey("createName")) {
                sb.append(" and i.username=? ");
                args.add(equalQo.get("createName"));
            }
            // 未创建计划的事件
            if (equalQo.containsKey("type")) {
                sb.append(" and p.incidentid is null ");
            }
            // 事件状态
            if (equalQo.containsKey("status")) {
                sb.append(" and i.status=? ");
                args.add(equalQo.get("status"));
            }
            // 分配起始日期
            if (equalQo.containsKey("createDateFrom")) {
                sb.append(" and i.createTime >= ? ");
                args.add(equalQo.get("createDateFrom"));
            }
            // 分配结束日期
            if (equalQo.containsKey("createDateTo")) {
                sb.append(" and i.createTime <= ?  ");
                args.add(equalQo.get("createDateTo"));
            }
        }
        if (likeQo != null) {
            if (likeQo.containsKey("keywords")) {
                sb.append(" and (i.title like ? or p.memo like ? )");
                args.add("%" + likeQo.get("keywords") + "%");
                args.add("%" + likeQo.get("keywords") + "%");
            }
        }
        sb.append(" order by field(i.urgency,'非常高','高','中','低'),p.plan_date desc");
    }

    public OpIncident findById(String id) {
        return incidentDao.findById(id);
    }

    public Record getIncident(String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("select i.*,p.plan_date,p.plan_type,p.executed,p.execute_date,p.incidentId,p.memo,o.instnm,t.stnm ")
                .append(" from op_incident i ")
                .append(" left join op_plan p on p.incidentid=i.rowguid ")
                .append(" left join st_instcd_b o on p.org_id=o.rowguid left join st_stbprp_b t on p.stcd=t.rowguid ")
                .append(" where 1=1 and i.rowguid = ?");
        return Db.findFirst(sb.toString(), id);
    }
}
