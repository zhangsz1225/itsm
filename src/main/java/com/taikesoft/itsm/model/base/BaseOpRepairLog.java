package com.taikesoft.itsm.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseOpRepairLog<M extends BaseOpRepairLog<M>> extends Model<M> implements IBean {

	public void setId(java.lang.String id) {
		set("id", id);
	}
	
	public java.lang.String getId() {
		return getStr("id");
	}

	public void setPlanId(java.lang.String planId) {
		set("plan_id", planId);
	}
	
	public java.lang.String getPlanId() {
		return getStr("plan_id");
	}

	public void setContentId(java.lang.Integer contentId) {
		set("content_id", contentId);
	}
	
	public java.lang.Integer getContentId() {
		return getInt("content_id");
	}

	public void setContent(java.lang.String content) {
		set("content", content);
	}
	
	public java.lang.String getContent() {
		return getStr("content");
	}

	public void setExecuted(java.lang.String executed) {
		set("executed", executed);
	}
	
	public java.lang.String getExecuted() {
		return getStr("executed");
	}

	public void setProblemDesc(java.lang.String problemDesc) {
		set("problem_desc", problemDesc);
	}
	
	public java.lang.String getProblemDesc() {
		return getStr("problem_desc");
	}

	public void setResloveResult(java.lang.String resloveResult) {
		set("reslove_result", resloveResult);
	}
	
	public java.lang.String getResloveResult() {
		return getStr("reslove_result");
	}

	public void setSortNumber(java.lang.Integer sortNumber) {
		set("sort_number", sortNumber);
	}
	
	public java.lang.Integer getSortNumber() {
		return getInt("sort_number");
	}

	public void setCreateBy(java.lang.Integer createBy) {
		set("create_by", createBy);
	}
	
	public java.lang.Integer getCreateBy() {
		return getInt("create_by");
	}

	public void setCreateName(java.lang.String createName) {
		set("create_name", createName);
	}
	
	public java.lang.String getCreateName() {
		return getStr("create_name");
	}

	public void setCreateAt(java.util.Date createAt) {
		set("create_at", createAt);
	}
	
	public java.util.Date getCreateAt() {
		return get("create_at");
	}

}
