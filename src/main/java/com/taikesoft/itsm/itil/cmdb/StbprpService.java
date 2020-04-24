package com.taikesoft.itsm.itil.cmdb;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.taikesoft.itsm.model.StStbprpB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 排涝站服务
 */
public class StbprpService {

    public static final StbprpService me = new StbprpService();
    public static final StStbprpB dao = new StStbprpB();

    /**
     * 根据水利站id获取所有排涝站
     * @param instgd 水利站id
     * @return
     */
    public List<StStbprpB> getByInstcd(String instgd) {
        String sql = "select * from st_stbprp_b where ISVALID='1' and INSTGD=?";
        return dao.find(sql, instgd);
    }


    public List<Record> getBprps(Map<String, Object> equalQo, Map<String, Object> likeQo) {
        StringBuilder sb = new StringBuilder();
        sb.append("select s.*, b.instnm, b.instcd ")
                .append(" from st_stbprp_b s ")
                .append(" left join st_instcd_b b on s.instgd=b.rowguid ")
                .append(" where 1=1 ");
        List<Object> args = new ArrayList<>();
        genQo(equalQo, likeQo, sb, args);
        return Db.find(sb.toString(), args.toArray());
    }

    public Page<Record> getPages(Map<String, Object> equalQo, Map<String, Object> likeQo, Integer pageNumber, Integer pageSize) {
        String sqlPara = "select s.*, b.instnm, b.instcd ";
        StringBuilder sqlExceptSelect = new StringBuilder();
        sqlExceptSelect.append(" from st_stbprp_b s left join st_instcd_b b on s.instgd=b.rowguid where 1=1 ");
        List<Object> args = new ArrayList<>();
        genQo(equalQo, likeQo, sqlExceptSelect, args);
        return Db.paginate(pageNumber, pageSize, sqlPara, sqlExceptSelect.toString(), args.toArray());
    }

    private void genQo(Map<String, Object> equalQo, Map<String, Object> likeQo, StringBuilder sb, List<Object> args) {

        if (equalQo != null) {
            if (equalQo.containsKey("instgd")) {
                sb.append(" and s.instgd=? ");
                args.add(equalQo.get("instgd"));
            }
            if (equalQo.containsKey("stcd")) {
                sb.append(" and s.rowguid=? ");
                args.add(equalQo.get("stcd"));
            }
            if (equalQo.containsKey("isvalid")) {
                sb.append(" and s.isvalid=? ");
                args.add(equalQo.get("isvalid"));
            }
        }

        if (likeQo != null) {
            if (likeQo.containsKey("keywords")) {
                sb.append(" and (s.stnm like ? or s.stcd like ? )");
                args.add("%" + likeQo.get("keywords") + "%");
                args.add("%" + likeQo.get("keywords") + "%");
            }
        }

        sb.append(" order by b.instcd, s.stnm");
    }
}
