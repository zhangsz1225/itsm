package com.taikesoft.itsm.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseStWatergaugeB<M extends BaseStWatergaugeB<M>> extends Model<M> implements IBean {

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

	public void setUpwg(java.lang.String upwg) {
		set("upwg", upwg);
	}
	
	public java.lang.String getUpwg() {
		return getStr("upwg");
	}

	public void setUptm(java.util.Date uptm) {
		set("uptm", uptm);
	}
	
	public java.util.Date getUptm() {
		return get("uptm");
	}

	public void setDwwg(java.lang.String dwwg) {
		set("dwwg", dwwg);
	}
	
	public java.lang.String getDwwg() {
		return getStr("dwwg");
	}

	public void setDwtm(java.util.Date dwtm) {
		set("dwtm", dwtm);
	}
	
	public java.util.Date getDwtm() {
		return get("dwtm");
	}

}