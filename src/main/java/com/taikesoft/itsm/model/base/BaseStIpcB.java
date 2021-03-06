package com.taikesoft.itsm.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseStIpcB<M extends BaseStIpcB<M>> extends Model<M> implements IBean {

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

	public void setInstgd(java.lang.String instgd) {
		set("instgd", instgd);
	}
	
	public java.lang.String getInstgd() {
		return getStr("instgd");
	}

	public void setStgd(java.lang.String stgd) {
		set("stgd", stgd);
	}
	
	public java.lang.String getStgd() {
		return getStr("stgd");
	}

	public void setCmnm(java.lang.String cmnm) {
		set("cmnm", cmnm);
	}
	
	public java.lang.String getCmnm() {
		return getStr("cmnm");
	}

	public void setLid(java.lang.String lid) {
		set("lid", lid);
	}
	
	public java.lang.String getLid() {
		return getStr("lid");
	}

	public void setLpwd(java.lang.String lpwd) {
		set("lpwd", lpwd);
	}
	
	public java.lang.String getLpwd() {
		return getStr("lpwd");
	}

	public void setInnerip(java.lang.String innerip) {
		set("innerip", innerip);
	}
	
	public java.lang.String getInnerip() {
		return getStr("innerip");
	}

	public void setInnerport(java.lang.String innerport) {
		set("innerport", innerport);
	}
	
	public java.lang.String getInnerport() {
		return getStr("innerport");
	}

	public void setInnersp(java.lang.String innersp) {
		set("innersp", innersp);
	}
	
	public java.lang.String getInnersp() {
		return getStr("innersp");
	}

	public void setOuterip(java.lang.String outerip) {
		set("outerip", outerip);
	}
	
	public java.lang.String getOuterip() {
		return getStr("outerip");
	}

	public void setOutserport(java.lang.String outserport) {
		set("outserport", outserport);
	}
	
	public java.lang.String getOutserport() {
		return getStr("outserport");
	}

	public void setOutsersp(java.lang.String outsersp) {
		set("outsersp", outsersp);
	}
	
	public java.lang.String getOutsersp() {
		return getStr("outsersp");
	}

	public void setVcrgd(java.lang.String vcrgd) {
		set("vcrgd", vcrgd);
	}
	
	public java.lang.String getVcrgd() {
		return getStr("vcrgd");
	}

	public void setVcrtp(java.lang.String vcrtp) {
		set("vcrtp", vcrtp);
	}
	
	public java.lang.String getVcrtp() {
		return getStr("vcrtp");
	}

	public void setVcrnum(java.lang.String vcrnum) {
		set("vcrnum", vcrnum);
	}
	
	public java.lang.String getVcrnum() {
		return getStr("vcrnum");
	}

	public void setLivecd(java.lang.String livecd) {
		set("livecd", livecd);
	}
	
	public java.lang.String getLivecd() {
		return getStr("livecd");
	}

	public void setComments(java.lang.String comments) {
		set("comments", comments);
	}
	
	public java.lang.String getComments() {
		return getStr("comments");
	}

}
