package com.taikesoft.itsm.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseStWaterS<M extends BaseStWaterS<M>> extends Model<M> implements IBean {

	public void setRowguid(java.lang.String rowguid) {
		set("rowguid", rowguid);
	}
	
	public java.lang.String getRowguid() {
		return getStr("rowguid");
	}

	public void setIsvalid(java.lang.String isvalid) {
		set("isvalid", isvalid);
	}
	
	public java.lang.String getIsvalid() {
		return getStr("isvalid");
	}

	public void setStgd(java.lang.String stgd) {
		set("stgd", stgd);
	}
	
	public java.lang.String getStgd() {
		return getStr("stgd");
	}

	public void setTm(java.util.Date tm) {
		set("tm", tm);
	}
	
	public java.util.Date getTm() {
		return get("tm");
	}

	public void setUpz(java.math.BigDecimal upz) {
		set("upz", upz);
	}
	
	public java.math.BigDecimal getUpz() {
		return get("upz");
	}

	public void setDwz(java.math.BigDecimal dwz) {
		set("dwz", dwz);
	}
	
	public java.math.BigDecimal getDwz() {
		return get("dwz");
	}

}