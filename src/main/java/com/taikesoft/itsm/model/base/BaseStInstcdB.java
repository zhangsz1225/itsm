package com.taikesoft.itsm.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseStInstcdB<M extends BaseStInstcdB<M>> extends Model<M> implements IBean {

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

	public void setInstcd(java.lang.String instcd) {
		set("instcd", instcd);
	}
	
	public java.lang.String getInstcd() {
		return getStr("instcd");
	}

	public void setInstnm(java.lang.String instnm) {
		set("instnm", instnm);
	}
	
	public java.lang.String getInstnm() {
		return getStr("instnm");
	}

	public void setTel(java.lang.String tel) {
		set("tel", tel);
	}
	
	public java.lang.String getTel() {
		return getStr("tel");
	}

	public void setLgtd(java.math.BigDecimal lgtd) {
		set("lgtd", lgtd);
	}
	
	public java.math.BigDecimal getLgtd() {
		return get("lgtd");
	}

	public void setLttd(java.math.BigDecimal lttd) {
		set("lttd", lttd);
	}
	
	public java.math.BigDecimal getLttd() {
		return get("lttd");
	}

	public void setComments(java.lang.String comments) {
		set("comments", comments);
	}
	
	public java.lang.String getComments() {
		return getStr("comments");
	}

}
