package com.taikesoft.itsm.common;

public class Constant {

    /** 默认分页 页面尺寸 */
    public static Integer DEFAULT_PAGE_SIZE;

    public static final String PLAN_TYPE_REPAIR = "检修";
    public static final String PLAN_TYPE_PATROL = "巡查";
    public static final String PLAN_TYPE_CONSTRUCT = "施工";
    public static final String PLAN_TYPE_OTHER = "其他";
    public static final String[] PLAN_TYPE_ALL = { PLAN_TYPE_REPAIR, PLAN_TYPE_PATROL, PLAN_TYPE_CONSTRUCT, PLAN_TYPE_OTHER };

    // 上传附件临时文件夹
    public static final String UPLOAD_FOLDER_TEMP = "/upload/temp/";//临时文件夹
    // 上传附件正式文件夹
    public static final String UPLOAD_FOLDER = "/upload/";//正式文件夹

    // 事件紧急程度
    public static final String URGENCY_HIGHER = "非常高";
    public static final String URGENCY_HIGH = "高";
    public static final String URGENCY_MEDIUM = "中";
    public static final String URGENCY_LOWER = "低";
    public static final String[] URGENCY_ALL = { URGENCY_HIGHER, URGENCY_HIGH, URGENCY_MEDIUM, URGENCY_LOWER };
}
