package com.taikesoft.itsm.incident;

import com.jfinal.aop.Before;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.taikesoft.itsm.common.Constant;
import com.taikesoft.itsm.itil.cmdb.InstcdService;
import com.taikesoft.itsm.itil.cmdb.StbprpService;
import com.taikesoft.itsm.itil.plan.PlanService;
import com.taikesoft.itsm.kit.TempFileRender;
import com.taikesoft.itsm.model.OpAttachment;
import com.taikesoft.itsm.model.OpIncident;
import com.taikesoft.itsm.model.OpPlan;
import com.taikesoft.itsm.model.SysAccount;
import com.taikesoft.itsm.system.controller.BaseController;
import com.taikesoft.itsm.system.service.AccountService;
import com.taikesoft.itsm.system.validator.IncidentValidator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class IncidentController extends BaseController {
    private IncidentService srv = IncidentService.me;

    /**
     * 系统首页
     */
    public void index() {
        // 水利站
        set("instcds", InstcdService.me.getInstcds());
        // 计划类型
        set("planTypes", Constant.PLAN_TYPE_ALL);
        render("incident_index.html");
    }

    /**
     * 获取所有事件
     */
    public void getIncidents() {
        Map<String, Object> equalQo = new HashMap<>();
        Map<String, Object> likeQo = new HashMap<>();
        //标题
        String title = getPara("title");
        if (StrKit.notBlank(title)) {
            likeQo.put("title", title);
        }
        //计划描述
        String memo = getPara("memo");
        if (StrKit.notBlank(memo)) {
            likeQo.put("memo", memo);
        }
        Integer origin = getParaToInt("origin");
        if (origin != null) {
            equalQo.put("origin", origin);
        }
        // 0:新建 1：已解决
        Integer status = getParaToInt("status");
        if (status != null) {
            equalQo.put("status", status);
        }
        //水站id
        String orgId = getPara("orgId");
        if (StrKit.notBlank(orgId)) {
            equalQo.put("orgId", orgId);
        }
        //测站id
        String stcd = getPara("stcd");
        if (StrKit.notBlank(stcd)) {
            equalQo.put("stcd", stcd);
        }
        List<OpIncident> incidents = srv.getIncidents(equalQo, likeQo);
        renderJson(incidents);
    }

    /**
     * 新增页
     */
    public void add() {
        set("planTypes", Constant.PLAN_TYPE_ALL);
        set("urgencyTypes", Constant.URGENCY_ALL);
        Map<String, Object> equalQo = new HashMap<>();
        equalQo.put("status", 1);
        //分配人员获取
        List<SysAccount> sysUsers = AccountService.me.getUsers(equalQo, null);
        Map<String, String> accountMap = new HashMap<>();
        for (SysAccount account : sysUsers) {
            accountMap.put("" + account.getId(), account.getName());
        }
        set("accountMap", accountMap);
        render("incident_add.html");
    }

    /**
     * 修改
     */
    public void edit() {
        String id = getPara("id");
        String instnm = getPara("instnm");
        String stnm = getPara("stnm");

        OpIncident incident = srv.findById(id);

        OpPlan plan = OpPlan.dao.findFirst("select * from op_plan where incident_id=?",incident.getRowguid());
        if (plan != null) {
            set("plan", plan);
            set("flag", true);
            set("createPlan", 1);
        } else {
            set("flag", false);
            set("createPlan", 0);
        }
        set("urgencyTypes", Constant.URGENCY_ALL);
        set("instnm", instnm);
        set("stnm", stnm);
        set("incident", incident);

        render("incident_edit.html");
    }

    /**
     * 修改保存
     */
    @Before(IncidentValidator.class)
    public void editSave() {
        OpIncident incident = getModel(OpIncident.class);
        OpIncident oldIncident = srv.findById(incident.getRowguid());
        oldIncident.setTitle(incident.getTitle());
        oldIncident.setSolution(incident.getSolution());
        oldIncident.setStatus(incident.getStatus());
        oldIncident.setUrgency(incident.getUrgency());
        oldIncident.setEndDate(incident.getEndDate());
        if (StrKit.isBlank(incident.getPlanId())) {
            if (oldIncident.getStatus() == 0 && incident.getStatus() == 1) {
                oldIncident.setSolveDate(new Date());
            }
        }
        oldIncident.update();
        renderJson(Ret.ok("msg", "修改成功"));
    }

    /**
     * 保存
     */
    @Before(IncidentValidator.class)
    public void save() {
        //标题，描述，紧急程度，status,planId,创建人信息，附件
        OpIncident incident = getModel(OpIncident.class);
        String id = StrKit.getRandomUUID();
        SysAccount account = getLoginAccount();
        List<String> attachmentIds = new ArrayList<>();
        String attachmentId1 = getPara("attachmentId1");
        String attachmentId2 = getPara("attachmentId2");
        String attachmentId3 = getPara("attachmentId3");
        String attachmentId4 = getPara("attachmentId4");
        if (StrKit.notBlank(attachmentId1)) {
            attachmentIds.add(attachmentId1);
        }
        if (StrKit.notBlank(attachmentId2)) {
            attachmentIds.add(attachmentId2);
        }
        if (StrKit.notBlank(attachmentId3)) {
            attachmentIds.add(attachmentId3);
        }
        if (StrKit.notBlank(attachmentId4)) {
            attachmentIds.add(attachmentId4);
        }
        incident.setRowguid(id);
        incident.setCreateId(account.getId());
        incident.setCreateName(account.getName());
        incident.setCreateTime(new Date());
        incident.setIsValid(1);
        for (String attachmentId : attachmentIds) {
            OpAttachment opAttachment = OpAttachment.dao.findById(attachmentId);
            opAttachment.setIsvalid("1");
            String path = opAttachment.getPath();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            //生产上windows?
            String orgName = getPara("orgName");
            String stcdName = getPara("stcdName");
            File destDirFile = new File((PathKit.getWebRootPath()).replace("\\", "/") + "/upload/" + orgName + "/" + stcdName + "/");
            if (!destDirFile.getParentFile().exists()) {
                destDirFile.getParentFile().mkdirs();
            }
            if (!destDirFile.exists()) {
                destDirFile.mkdirs();
            }

            String uploadPath = "/upload/" + orgName + "/" + stcdName + "/" + sdf.format(new Date()) + "/";
            String destFilePath = (PathKit.getWebRootPath()).replace("\\", "/") + uploadPath;
            File destFile = new File(destFilePath);
            if (!destFile.exists()) {
                destFile.mkdirs();
            }
            opAttachment.setPath(uploadPath + "/" + opAttachment.getId() + "_" + opAttachment.getName());
            opAttachment.setBid(incident.getRowguid());
            //手机端根据updateBy取的附件，所以这里挪的时候设置修改
            opAttachment.setUpdateAt(new Date());
            opAttachment.setUpdateBy(account.getId());
            opAttachment.setUpdateName(account.getName());
            opAttachment.update();
            //将临时文件移动至正式文件
            File startFile = new File((PathKit.getWebRootPath()).replace("\\", "/") + path);
            startFile.renameTo(new File((PathKit.getWebRootPath()).replace("\\", "/") + opAttachment.getPath()));
        }
        int createPlan = getParaToInt("createPlan");
        if (createPlan == 1) {
            //创建计划
            String orgId = getPara("orgId");
            String stcd = getPara("stcd");
            String userName = getPara("assignName");
            int userId = getParaToInt("assignId");
            String planType = getPara("planType");
            String planDate = getPara("planDate");
            String memo = getPara("memo");
            OpPlan plan = new OpPlan();
            plan.setId(incident.getPlanId());
            //事件id
            plan.setIncidentId(incident.getRowguid());
            //分配的运维人员
            plan.setUserId(userId);
            plan.setUsername(userName);
            //创建信息
            plan.setCreateBy(incident.getCreateId());
            plan.setCreateName(incident.getCreateName());
            plan.setCreateAt(new Date());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                plan.setPlanDate(sdf.parse(planDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            plan.setOrgId(orgId);
            plan.setStcd(stcd);
            plan.setPlanType(planType);
            plan.setMemo(memo);
            // 未执行
            plan.setExecuted("0");
            // 有效
            plan.setIsvalid("1");
            plan.save();
        }
        Ret ret = srv.save(incident);
        renderJson(ret);
    }

    /**
     * 附件上传
     */
    public void incidentUpload() {
        UploadFile file = getFile();
        if (file == null) {
            renderJson(Ret.fail("msg", "还未选择任何文件！"));
            return;
        }
        File tempFile = file.getFile();
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
        String id = StrKit.getRandomUUID();
        String uploadPath = Constant.TEMP_FOLDER + id + "_" + tempFile.getName();
        String destFilePath = (PathKit.getWebRootPath()).replace("\\", "/") + uploadPath;
        File destFile = new File(destFilePath);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        tempFile.renameTo(destFile);
        //数据库保存
        OpAttachment opAttachment = new OpAttachment();
        opAttachment.setId(id);
        opAttachment.setName(tempFile.getName());
        opAttachment.setPath(uploadPath);
        opAttachment.setTableName("op_incident");
        opAttachment.setType(fileType);
        opAttachment.setIsvalid("2");
        opAttachment.setCreateAt(new Date());
        opAttachment.setCreateBy(getLoginAccountId());
        opAttachment.setCreateName(getLoginAccount().getName());
        opAttachment.save();
        Map map = new HashMap();
        map.put("attachmentId", id);
        map.put("fileName", opAttachment.getName());
        renderJson(Ret.ok("map", map));
    }

    /**
     * 删除附件
     */
    public void deleteFile() {
        String id = getPara("attachmentId");
        OpAttachment attachment = OpAttachment.dao.findById(id);
        File file = new File((PathKit.getWebRootPath()).replace("\\", "/") + attachment.getPath());
        if (file.isFile()) {
            file.delete();
        }
        renderJson(Ret.ok());
    }

    public void downFile() {
        String attachmentIds = getPara("attachmentIds");
        String[] ids = attachmentIds.split(",");
        if (ids.length == 1) {
            OpAttachment attachment = OpAttachment.dao.findById(ids[0]);
            File file = new File(PathKit.getWebRootPath().replace("\\", "/") + attachment.getPath().replace("\\", "/"));
            renderFile(file);
        } else {
            byte[] buffer = new byte[1024];
            String zipPath = PathKit.getWebRootPath().replace("\\", "/") + "/upload/Temp/附件.zip";

            ZipOutputStream out = null;
            FileInputStream fis = null;
            try {
                out = new ZipOutputStream(new FileOutputStream(
                        zipPath));
                for (String id : ids) {
                    OpAttachment attachment = OpAttachment.dao.findById(id);
                    File file = new File(PathKit.getWebRootPath().replace("\\", "/") + attachment.getPath().replace("\\", "/"));
                    fis = new FileInputStream(file);
                    //为解决上传的附件的名字一样的问题:java.util.zip.ZipException: duplicate entry
                    // System.currentTimeMillis()
                    out.putNextEntry(new ZipEntry(System.currentTimeMillis() + "_" + attachment.getName()));
                    //设置压缩文件内的字符编码，不然会变成乱码
                    // out.setEncoding("GBK");
                    int len;
                    // 读入需要下载的文件的内容，打包到zip文件
                    while ((len = fis.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                    out.closeEntry();
                    fis.close();

                }
                out.close();
                File zipFile = new File(zipPath);
                //临时文件调用系统renderFile后如果还要删除临时文件需改写renderFile
                render(new TempFileRender(zipFile));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 查询水利站列表
     */
    public void getInsts() {
        renderJson(Ret.ok("rows", InstcdService.me.getInstcds()));
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
        // 有效性
        equalQo.put("isvalid", "1");
        List<Record> records = StbprpService.me.getBprps(equalQo, likeQo);
        renderJson(Ret.ok("rows", records));
    }
}
