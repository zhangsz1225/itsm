package com.taikesoft.itsm.itil.cmdb;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.taikesoft.itsm.model.StInstcdB;

import java.util.List;

/**
 * 水利站服务
 */
public class InstcdService {

    public static final InstcdService me = new InstcdService();
    public static final StInstcdB dao = new StInstcdB();

    /**
     * 获取所有水利站
     * @return
     */
    public List<Record> getInstcds() {
        String sql = "select *,(select count(*) from st_stbprp_b WHERE isvalid='1' AND instgd=st_instcd_b.rowguid) AS stnum from st_instcd_b where ISVALID='1' order by INSTCD ";
        return Db.find(sql);
    }
}
