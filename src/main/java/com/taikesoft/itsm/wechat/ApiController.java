package com.taikesoft.itsm.wechat;

import com.alibaba.fastjson.JSONArray;
import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.ext.kit.DateKit;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import com.taikesoft.itsm.common.Constant;
import com.taikesoft.itsm.incident.IncidentService;
import com.taikesoft.itsm.interceptor.WeiXinInterceptor;
import com.taikesoft.itsm.itil.cmdb.InstcdService;
import com.taikesoft.itsm.itil.cmdb.StbprpService;
import com.taikesoft.itsm.itil.plan.PlanService;
import com.taikesoft.itsm.kit.IpKit;
import com.taikesoft.itsm.model.*;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 小程序服务接口
 */
@Before(WeiXinInterceptor.class)
public class ApiController extends Controller {

    private static final Log log = Log.getLog(ApiController.class);

    /**
     * 登录接口
     */
    @Clear(WeiXinInterceptor.class)
    public void login() {
        if (getRequest().getMethod().equalsIgnoreCase("GET")) {
            renderJson(Ret.fail("msg", "必须使用POST请求"));
            return;
        }
        String username = getPara("username");
        String password = getPara("password");
        String wxid = getPara("wxid");
        String loginIp = IpKit.getRealIp(getRequest());
        renderJson(LoginService.me.login(username, password, wxid, loginIp));
    }

    /**
     * 微信id查询绑定用户
     */
    public void getAccount() {
        String wxid = getPara("wxid");
        SysAccount account = LoginService.me.getAccountByWxid(wxid);
        if (account == null) {
            renderJson(Ret.fail("msg", "微信id无效"));
            return;
        }
        renderJson(Ret.ok("data", account.removeSensitiveInfo()));
    }

    /**
     * 查询计划接口
     */
    public void getPlans() {
        Map<String, Object> equalQo = new HashMap<>();
        Map<String, Object> likeQo = new HashMap<>();

        // 水利站
        String orgId = getPara("orgId");
        if (StrKit.notBlank(orgId)) {
            equalQo.put("instgd", orgId);
        }
        // 排涝站
        String stcd = getPara("stcd");
        if (StrKit.notBlank(stcd)) {
            equalQo.put("stcd", stcd);
        }
        // 人员id
        String userId = getPara("userId");
        if (StrKit.notBlank(userId)) {
            equalQo.put("userId", userId);
        }
        String username = getPara("username");
        if (StrKit.notBlank(username)) {
            likeQo.put("username", username);
        }
        // 计划时间
        try {
            Date planDateFrom = getParaToDate("planDateFrom");
            if (planDateFrom != null) {
                equalQo.put("planDateFrom", planDateFrom);
            }
            Date planDateTo = getParaToDate("planDateTo");
            if (planDateTo != null) {
                equalQo.put("planDateTo", planDateTo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("接收日期参数转换发生异常", e);
        }
        // 执行状态
        Integer executed = getParaToInt("executed");
        if (executed != null) {
            equalQo.put("executed", executed);
        }
        // 计划类型
        String planType = getPara("planType");
        if (StrKit.notBlank(planType)) {
            equalQo.put("planType", planType);
        }
        // 数据有效性
        equalQo.put("isvalid", "1");

        // 分页
        Integer pageNumber = getParaToInt("pageNum");
        if (pageNumber == null) {
            pageNumber = 1;
        }

        Integer pageSize = getParaToInt("pageSize");
        if (pageSize == null) {
            pageSize = Constant.DEFAULT_PAGE_SIZE;
        }

        // 关键字
        String keywords = getPara("keywords");
        if (StrKit.notBlank(keywords)) {
            likeQo.put("keywords", keywords);
        }

        Page<Record> records = PlanService.me.getPages(equalQo, likeQo, pageNumber, pageSize);
        renderJson(Ret.ok("total", records.getTotalRow()).set("rows", records.getList()));
    }

    /**
     * 根据计划id获取计划及执行情况
     */
    public void getPlan() {

        // 计划id
        String pid = getPara("id");
        if (StrKit.isBlank(pid)) {
            renderJson(Ret.fail("msg", "请求参数不正确"));
            return;
        }

        // 计划
        Map<String, Object> equalQo = new HashMap<>();
        equalQo.put("id", pid);
        Record plan = PlanService.me.getPlanExtra(equalQo, null);
        if (plan == null) {
            renderJson(Ret.fail("msg", "未找到计划"));
            return;
        }

        // 执行情况
        String planType = plan.getStr("plan_type");
        if (Constant.PLAN_TYPE_CONSTRUCT.equals(planType) || Constant.PLAN_TYPE_REPAIR.equals(planType)) {
            OpRepairLog repairLog = new OpRepairLog().findFirst("select * from op_repair_log where plan_id=?", pid);
            renderJson(Ret.ok("plan", plan).set("log", repairLog));
        } else if (planType.equals(Constant.PLAN_TYPE_PATROL)) {
            List<OpInspectLog> inspectLog = new OpInspectLog().find("select * from op_inspect_log where plan_id=?", pid);
            renderJson(Ret.ok("plan", plan).set("log", inspectLog));
        } else if (Constant.PLAN_TYPE_OTHER.equals(planType)) {
            OpOtherLog otherLog = new OpOtherLog().findFirst("select * from op_other_log where plan_id=?", pid);
            renderJson(Ret.ok("plan", plan).set("log", otherLog));
        }
    }

    /**
     * 查询水利站列表
     */
    public void getInsts() {

        renderJson(Ret.ok("rows", InstcdService.me.getInstcds()));
    }

    /**
     * 查询运维人员列表
     */
    public void getUserList() {

        renderJson(Ret.ok("rows", ApiService.me.getUserList()));
    }

    /**
     * 查询排涝站列表
     */
    public void getStations() {

        Map<String, Object> equalQo = new HashMap<>();
        Map<String, Object> likeQo = new HashMap<>();

        // 水利站id
        String instgd = getPara("instgd");
        if (StrKit.notBlank(instgd)) {
            equalQo.put("instgd", instgd);
        }
        // 测站id--计划新增时事件带出来的唯一的测站
        String stcd = getPara("stcd");
        if (StrKit.notBlank(stcd)) {
            equalQo.put("stcd", stcd);
        }
        // 关键字
        String keywords = getPara("keywords");
        if (StrKit.notBlank(keywords)) {
            likeQo.put("keywords", keywords);
        }
        // 有效性
        equalQo.put("isvalid", "1");
        // 分页
        Integer pageNumber = getParaToInt("pageNum");
        Integer pageSize = getParaToInt("pageSize");

        if (pageNumber != null && pageSize != null) {
            Page<Record> records = StbprpService.me.getPages(equalQo, likeQo, pageNumber, pageSize);
            renderJson(Ret.ok("total", records.getTotalRow()).set("rows", records.getList()));
        } else {
            List<Record> records = StbprpService.me.getBprps(equalQo, likeQo);
            renderJson(Ret.ok("rows", records));
        }
    }

    /**
     * 新增计划
     */
    @Before(Tx.class)
    public void addPlan() {
        if (getRequest().getMethod().equalsIgnoreCase("GET")) {
            renderJson(Ret.fail("msg", "必须使用POST请求"));
            return;
        }
        String wxid = getPara("wxid");
        SysAccount account = LoginService.me.getAccountByWxid(wxid);
        OpPlan plan = getModel(OpPlan.class);
        if (plan == null) {
            renderJson(Ret.fail("msg", "提交数据出错"));
            return;
        }

        plan = new OpPlan();
        plan.setId(StrKit.getRandomUUID());
        plan.setPlanDate(getParaToDate("planDate"));
        plan.setPlanType(getPara("planType"));
        plan.setStcd(getPara("stcd"));
        plan.setOrgId(getPara("orgId"));
        plan.setMemo(getPara("memo"));
        plan.setUserId(getParaToInt("userId"));
        plan.setUsername(getPara("username"));
        String incidentId = getPara("incidentId");
        plan.setIncidentId(incidentId);
        plan.setExecuted("0");                      // 未执行
        plan.setIsvalid("1");                       // 有效
        plan.setCreateBy(account.getId());          // 创建人id
        plan.setCreateName(account.getName());      // 创建人姓名
        plan.setCreateAt(new Date());               // 创建日期
        plan.save();

        renderJson(Ret.ok());
    }

    /**
     * 获取巡查检修内容列表
     */

    public void getContents() {

        String type = getPara("contentType");//内容类型
        List<Record> list = ApiService.me.getContents(type);
        renderJson(Ret.ok("rows", list));
    }

    /**
     * 新增计划内容
     */
    @Before(Tx.class)
    public void addPlanContent() {

        if (getRequest().getMethod().equalsIgnoreCase("GET")) {
            renderJson(Ret.fail("msg", "必须使用POST请求"));
            return;
        }

        //修改附件
        String attachmentIds = getPara("attachmentIds");
        updateByAttachments(attachmentIds, getPara("instnm"), getPara("stnm"), getPara("userName"), getParaToInt("userId"));

        //计划更新
        String planId = getPara("planId");
        OpPlan oldOpPlan = OpPlan.dao.findById(planId);
        oldOpPlan.setExecuteDate(getParaToDate("executeDate"));
        //将对应的事件修改为 已解决
        if("1".equals(getPara("executed"))){
            String incidentId = oldOpPlan.getIncidentId();
            if(StrKit.notBlank(incidentId)){
                if("0".equals(oldOpPlan.getExecuted())){
                    OpIncident incident = OpIncident.dao.findById(incidentId);
                    incident.setSolveDate(new Date());
                }
            }
        }
        oldOpPlan.setExecuted(getPara("executed"));
        oldOpPlan.setUpdateName(getPara("username"));
        oldOpPlan.setUpdateBy(getParaToInt("userId"));
        oldOpPlan.setUpdateAt(new Date());
        oldOpPlan.update();
        //巡查，施工 检修, 其它
        String type = getPara("planType");
        if (Constant.PLAN_TYPE_PATROL.equals(type)) {
            List<OpInspectLog> opInspectLogs = JSONArray.parseArray(getPara("planContentList"), OpInspectLog.class);
            for (OpInspectLog opInspectLog : opInspectLogs) {
                opInspectLog.setId(StrKit.getRandomUUID());
                opInspectLog.setPlanId(planId);
                opInspectLog.setCreateAt(new Date());
                opInspectLog.setCreateBy(getParaToInt("userId"));
                opInspectLog.setCreateName(getPara("username"));
                opInspectLog.save();
            }

        } else if (Constant.PLAN_TYPE_CONSTRUCT.equals(type) || Constant.PLAN_TYPE_REPAIR.equals(type)) {
            OpRepairLog opRepairLog = new OpRepairLog();
            opRepairLog.setId(StrKit.getRandomUUID());
            opRepairLog.setPlanId(planId);
            opRepairLog.setContentId(getParaToInt("contentId"));
            opRepairLog.setContent(getPara("content"));
            opRepairLog.setProblemDesc(getPara("problemDesc"));
            opRepairLog.setResloveResult(getPara("resloveResult"));
            opRepairLog.setSortNumber(getParaToInt("sortNumber"));
            opRepairLog.setCreateAt(new Date());
            opRepairLog.setCreateBy(getParaToInt("userId"));
            opRepairLog.setCreateName(getPara("username"));
            opRepairLog.save();

        } else if (Constant.PLAN_TYPE_OTHER.equals(type)) {

            OpOtherLog opOtherLog = new OpOtherLog();
            opOtherLog.setId(StrKit.getRandomUUID());
            opOtherLog.setPlanId(planId);
            opOtherLog.setExecuteDesc(getPara("execute_desc"));
            opOtherLog.setCreateAt(new Date());
            opOtherLog.setCreateBy(getParaToInt("userId"));
            opOtherLog.setCreateName(getPara("username"));
            opOtherLog.save();
        }
        renderJson(Ret.ok());
    }

    private void updateByAttachments(String attachmentIds, String instnm, String stnm, String username, int userId) {
        if (StrKit.notBlank(attachmentIds)) {
            JSONArray ids = (JSONArray) JSONArray.toJSON(getPara("attachmentIds"));
            if (ids != null) {

                for (int i = 0; i < ids.size(); i++) {
                    updateFile(ids.getString(i), instnm, stnm, username, userId);
                }
            }
        }
    }

    /**
     * 附件预上传
     */
    @Before(Tx.class)
    public void fileUpload() {

        UploadFile file;
        file = getFile();
        File tempFile = file.getFile();
        if (file.getFile() == null) {
            renderJson(Ret.fail("msg", "文件上传失败"));
            return;
        }
        String fileName = System.currentTimeMillis() + "";
        String fileType = tempFile.getName().substring(tempFile.getName().lastIndexOf("."));
        String fileTypeReg = ".(jpg|jpeg|png|gif|bmp|mp4|avi|3gp|wmv)";
        boolean isFileType = Pattern.matches(fileTypeReg, fileType);
        if (!isFileType) {
            renderJson(Ret.fail("msg", "文件" + fileType + "格式不支持上传！"));
            return;
        }
        //附件大小不能超过1G
        if (tempFile.length() > (long) (1024 * 1024 * 1024)) {
            renderJson(Ret.fail("msg", "文件太大不能上传！"));
            return;
        }
        String uploadPath = Constant.UPLOAD_FOLDER_TEMP + fileName + fileType;
        String destFilePath = (PathKit.getWebRootPath()).replace("\\", "/") + uploadPath;
        File destFile = new File(destFilePath);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        tempFile.renameTo(destFile);
        //数据库保存
        String tableName = getPara("tableName");
        String bid = getPara("bid");
        int userId = getParaToInt("userId");
        String username = getPara("username");
        String id = StrKit.getRandomUUID();
        OpAttachment opAttachment = new OpAttachment();
        opAttachment.setId(id);
        opAttachment.setBid(bid);
        opAttachment.setName(fileName);
        opAttachment.setPath(uploadPath);
        opAttachment.setTableName(tableName);
        opAttachment.setType(fileType);
        opAttachment.setIsvalid("2");
        opAttachment.setCreateAt(new Date());
        opAttachment.setCreateBy(userId);
        opAttachment.setCreateName(username);
        opAttachment.save();
        Map<String, String> map = new LinkedHashMap();
        map.put("id", id);
        renderJson(Ret.ok("data", map));
    }

    /**
     * 保存附件
     *
     * @param id
     * @param instnm
     * @param stnm
     * @param username
     * @param userId
     */
    @Before(Tx.class)
    public void updateFile(String id, String instnm, String stnm, String username, Integer userId) {

        OpAttachment oldData = OpAttachment.dao.findById(id);
        if (oldData == null) {

            log.error("附件id=" + id + "的信息在数据库中不存在！");
        } else {
            String oldFileName = oldData.getPath();
            File oldFile = new File((PathKit.getWebRootPath()).replace("\\", "/") + oldFileName);
            String fileName = Constant.UPLOAD_FOLDER + instnm + "/" + stnm + "/" + DateKit.toStr(new Date(), "yyyyMMdd") + "/" + oldData.getName() + oldData.getType();
            File file = new File((PathKit.getWebRootPath()).replace("\\", "/") + fileName);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            oldFile.renameTo(file);
            oldData.setPath(fileName);
            oldData.setIsvalid("1");
            oldData.setUpdateAt(new Date());
            oldData.setUpdateBy(userId);
            oldData.setUpdateName(username);
            oldData.update();
        }
    }

    /**
     * 附件删除
     */
    @Before(Tx.class)
    public void deleteFile() {
        if (getRequest().getMethod().equalsIgnoreCase("GET")) {
            renderJson(Ret.fail("msg", "必须使用POST请求"));
            return;
        }
        SysAccount account = LoginService.me.getAccountByWxid(getPara("wxid"));

        OpAttachment opAttachment = OpAttachment.dao.findById(getPara("id"));

        if (opAttachment != null && "1".equals(opAttachment.getIsvalid())) {
            File file = new File((PathKit.getWebRootPath()).replace("\\", "/") + opAttachment.getPath());

            if (file.isFile()) {
                file.delete();
            }
        }
        opAttachment.setIsvalid("0");
        opAttachment.setUpdateName(account.getName());
        opAttachment.setUpdateBy(account.getId());
        opAttachment.setUpdateAt(new Date());
        opAttachment.update();
        renderJson(Ret.ok("msg", "删除成功！"));
    }

    /**
     * 附件获取
     */

    public void getAttachments() {

        String bid = getPara("bid");
        String wxid = getPara("wxid");
        SysAccount account = LoginService.me.getAccountByWxid(wxid);
        List<Record> list = ApiService.me.getFileData(bid, account.getId());
        renderJson(Ret.ok("rows", list));
    }


    /**
     * 获取最新水位列表数据
     */
    public void getLatestWater() {
        String instgd = getPara("instgd");//水利站标识
        String stgd = getPara("stgd");//排涝站标识
        String stnm = getPara("stnm");//排涝站名称
        int pageNum = 1;
        int pageSize = 20;
        if (StrKit.notBlank(getPara("pageNum"))) {
            pageNum = getParaToInt("pageNum");
        }
        if (StrKit.notBlank(getPara("pageSize"))) {
            pageSize = getParaToInt("pageSize");
        }

        Map<String, Object> paraMap = new LinkedHashMap<>();

        if (StrKit.notBlank(instgd)) {
            paraMap.put("instgd", instgd);
        }
        if (StrKit.notBlank(stgd)) {
            paraMap.put("stgd", stgd);
        }
        if (StrKit.notBlank(stnm)) {
            paraMap.put("stnm", stnm);
        }
        Page<Record> list = ApiService.me.getLatestWaters(pageNum, pageSize, paraMap);
        renderJson(Ret.ok("rows", list.getList()).set("total", list.getTotalRow()));
    }

    /**
     * 获取测站历史水位列表数据
     */
    public void getHistoryWater() {
        String instgd = getPara("instgd");//水利站标识
        String stgd = getPara("stgd");//排涝站标识
        String startrm = getPara("startrm") + " " + getPara("startTime");//起始时间
        String endtm = getPara("endtm") + " " + getPara("endTime");//结束时间
        int pageNum = 1;
        int pageSize = 20;
        if (StrKit.notBlank(getPara("pageNum"))) {
            pageNum = getParaToInt("pageNum");
        }
        if (StrKit.notBlank(getPara("pageSize"))) {
            pageSize = getParaToInt("pageSize");
        }

        Map<String, Object> paraMap = new LinkedHashMap<>();

        if (StrKit.notBlank(instgd)) {
            paraMap.put("instgd", instgd);
        }
        if (StrKit.notBlank(stgd)) {
            paraMap.put("stgd", stgd);
        }
        if (StrKit.notBlank(startrm)) {
            paraMap.put("startrm", startrm);
        }
        if (StrKit.notBlank(endtm)) {
            paraMap.put("endtm", endtm);
        }
        Page<Record> list = ApiService.me.getHistoryWaters(pageNum, pageSize, paraMap);
        renderJson(Ret.ok("rows", list.getList()).set("total", list.getTotalRow()));
    }

    /**
     * 查询登录人创建的事件接口
     */
    public void getIncidents() {
        Map<String, Object> equalQo = new HashMap<>();
        Map<String, Object> likeQo = new HashMap<>();

        String keywords = getPara("keywords");
        if (StrKit.notBlank(keywords)) {
            likeQo.put("keywords", keywords);
        }
        if(StrKit.notBlank(getPara("userId"))){
            int userId = getParaToInt("userId");
            equalQo.put("userId", userId);
        }
        if(StrKit.notBlank(getPara("type"))){
            equalQo.put("type", "type");
        }
        // 计划时间
        try {
            Date createDateFrom = getParaToDate("createDateFrom");
            if (createDateFrom != null) {
                equalQo.put("createDateFrom", createDateFrom);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("接收日期参数转换发生异常", e);
        }

        if (StrKit.notBlank(getPara("createDateTo"))) {
            String createDateToStr = getPara("createDateTo") + " 24:00:00";
//            Date createDateTo = null;
//            try {
//                createDateTo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(createDateToStr);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
            Date createDateTo = DateKit.toDate(createDateToStr);
            equalQo.put("createDateTo", createDateTo);
        }

        // 0:新建1：已解决
        Integer status = getParaToInt("status");
        if (status != null) {
            equalQo.put("status", status);
        }
        // 分页
        Integer pageNumber = getParaToInt("pageNum");
        if (pageNumber == null) {
            pageNumber = 1;
        }

        Integer pageSize = getParaToInt("pageSize");
        if (pageSize == null) {
            pageSize = Constant.DEFAULT_PAGE_SIZE;
        }
        Page<Record> records = IncidentService.me.getPages(equalQo, likeQo, pageNumber, pageSize);
        renderJson(Ret.ok("total", records.getTotalRow()).set("rows", records.getList()));

    }

    /**
     * 根据事件id获取事件基本信息
     */
    public void getIncident() {
        // 事件id
        String id = getPara("id");
        if (StrKit.isBlank(id)) {
            renderJson(Ret.fail("msg", "请求参数不正确"));
            return;
        }
        // 事件
        Record incident = IncidentService.me.getIncident(id);
        if (incident == null) {
            renderJson(Ret.fail("msg", "未找到事件"));
        } else {
            renderJson(Ret.ok("incident", incident));
        }
    }

    /**
     * 事件保存
     */
    public void addIncident() {
        if (getRequest().getMethod().equalsIgnoreCase("GET")) {
            renderJson(Ret.fail("msg", "必须使用POST请求"));
            return;
        }
        String wwxid = getPara("wxid");
        //因为新增时有附件上传需要穿bid，所以前端生成uuid
        String rowguid = StrKit.getRandomUUID();
        String orgId = getPara("orgId");
        String stcd = getPara("stcd");
        String instnm = getPara("instnm");
        String stnm = getPara("stnm");
        String title = getPara("title");
        String urgency = getPara("urgency");
        String origin = getPara("origin");
        int status = getParaToInt("status");
        Date endDate = getParaToDate("endDate");
        String solution = getPara("solution");
        int userId = getParaToInt("userId");
        String userName = getPara("username");
        OpIncident incident = new OpIncident();
        incident.setRowguid(rowguid);
        incident.setOrgid(orgId);
        incident.setStcd(stcd);
        incident.setTitle(title);
        incident.setUrgency(urgency);
        incident.setOrigin(origin);
        incident.setStatus(status);
        incident.setEndDate(endDate);
        if (status == 1) {
            incident.setSolveDate(new Date());
        }
        incident.setSolution(solution);
        incident.setCreateId(userId);
        incident.setCreateName(userName);
        incident.setCreateTime(new Date());
        incident.setIsValid(1);
        //创建事件时是否创建计划
        int createPlan = getParaToInt("createPlan");
        if (createPlan == 1) {
            String planType = getPara("planType");
            Date planDate = getParaToDate("planDate");
            String memo = getPara("memo");
            int assignId = getParaToInt("assignId");
            String assignName = getPara("assignName");
            OpPlan plan = new OpPlan();
            plan.setId(StrKit.getRandomUUID());
            plan.setOrgId(orgId);
            plan.setStcd(stcd);
            plan.setIncidentId(incident.getRowguid());
            plan.setPlanType(planType);
            plan.setPlanDate(planDate);
            plan.setMemo(memo);
            plan.setUserId(assignId);
            plan.setUsername(assignName);
            plan.setExecuted("0");
            plan.setIsvalid("1");
            plan.setCreateBy(userId);
            plan.setCreateName(userName);
            plan.setCreateAt(new Date());
            plan.save();
        }
        incident.save();
        String attachmentIds = getPara("attachmentIds");
        //附件保存
        updateByAttachments(attachmentIds, instnm, stnm, userName, userId);
        renderJson(Ret.ok());
    }

    /**
     * 修改事件基本信息
     */
    public void addIncidentContent() {
        if (getRequest().getMethod().equalsIgnoreCase("GET")) {
            renderJson(Ret.fail("msg", "必须使用POST请求"));
            return;
        }
        //事件更新
        String incidentId = getPara("incidentId");
        OpIncident oldOpIncident = OpIncident.dao.findById(incidentId);
        //标题
        oldOpIncident.setTitle(getPara("title"));
        //紧急程度
        oldOpIncident.setUrgency(getPara("urgency"));
        //事件状态
        oldOpIncident.setStatus(getParaToInt("status"));
        //解决方案描述
        oldOpIncident.setSolution(getPara("solution"));
        //解决截止日期
        oldOpIncident.setEndDate(getParaToDate("endDate"));
        //解决日期
        oldOpIncident.setSolveDate(getParaToDate("solveDate"));
        oldOpIncident.update();
        renderJson(Ret.ok());
    }
}
