package com.taikesoft.itsm.wechat;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiService {
    public static final ApiService me = new ApiService();

    /**
     * 获取水位列表数据
     *
     * @param pageNum
     * @param pageSize
     * @param paraMap
     * @return
     */
    public Page<Record> getLatestWaters(int pageNum, int pageSize, Map<String, Object> paraMap) {

        String select = "SELECT *";
        StringBuffer sb = new StringBuffer();
        ArrayList arg = new ArrayList<>();
        sb.append(" FROM (SELECT a.*,b.stnm,b.instgd,b.stcd,c.uph,c.upl,c.upsh,c.upsl,c.dwh,c.dwl,c.dwsh,c.dwsl ");
        sb.append(" FROM  st_lastwater_s a INNER JOIN st_stbprp_b b ON a.stgd=b.rowguid LEFT JOIN st_stsz_b c ON a.stgd=c.stgd WHERE b.isvalid='1')t where 1=1 ");

        if (paraMap.containsKey("instgd")) {

            sb.append(" and t.instgd=? ");
            arg.add(paraMap.get("instgd"));
        }
        if (paraMap.containsKey("stgd")) {

            sb.append(" and t.stgd=? ");
            arg.add(paraMap.get("stgd"));
        }
        if (paraMap.containsKey("stnm")) {

            sb.append(" and t.stnm like ? ");
            arg.add("%"+paraMap.get("stnm")+"%");
        }

        sb.append(" order by t.instgd ASC ");
        return Db.paginate(pageNum, pageSize, select, sb.toString(), arg.toArray());
    }

    /**
     * 获取附件数据
     *
     * @param bid
     * @param userId
     * @return
     */
    public List<Record> getFileData(String bid, int userId) {

        return Db.find(" SELECT * FROM op_attachment WHERE isvalid='1' and bid=? and update_by=? ORDER BY create_at", bid, userId);
    }

    /**
     * 获取巡查检修数据
     *
     * @param type
     * @return
     */
    public List<Record> getContents(String type) {

        StringBuffer sb = new StringBuffer();
        ArrayList arg = new ArrayList();
        sb.append("SELECT * FROM op_content WHERE isvalid='1'");
        if (StrKit.notBlank(type)) {
            sb.append(" AND content_type=? ");
            arg.add(type);
        }
        sb.append(" ORDER BY sort_number ");
        return Db.find(sb.toString(), arg.toArray());
    }

    public Page<Record> getHistoryWaters(int pageNum, int pageSize, Map<String, Object> paraMap){

        String select = "SELECT *";
        StringBuffer sb = new StringBuffer();
        ArrayList arg = new ArrayList<>();
        sb.append(" FROM(SELECT a.*,c.uph,c.upl,c.upsh,c.upsl,c.dwh,c.dwl,c.dwsh,c.dwsl  ");
        sb.append(" FROM (SELECT * FROM  st_water_s WHERE 1=1 ");

        if (paraMap.containsKey("instgd")) {

            sb.append(" and instgd=? ");
            arg.add(paraMap.get("instgd"));
        }
        if (paraMap.containsKey("stgd")) {

            sb.append(" and stgd=? ");
            arg.add(paraMap.get("stgd"));
        }
        if (paraMap.containsKey("starttm")) {

            sb.append(" and tm>? ");
            arg.add(paraMap.get("starttm"));
        }
        if (paraMap.containsKey("endtm")) {

            sb.append(" and tm<? ");
            arg.add(paraMap.get("endtm"));
        }

        sb.append(" ) a LEFT JOIN st_stsz_b c ON a.stgd=c.stgd ORDER BY a.tm DESC )d");
        return Db.paginate(pageNum, pageSize, select, sb.toString(), arg.toArray());
    }

    /**
     * 运维人员获取
     * @return
     */
    public List<Record> getUserList() {
        String sql = "select *,(select count(*) from sys_account WHERE status=1) AS usersNum from sys_account where status=1 order by username ";
        return Db.find(sql);
    }
}
