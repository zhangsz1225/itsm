var urlBack = new Array();

/**
 *
 * @param url
 * @param params
 * @param callback
 * @returns {*}
 */
// (function ($) {


function loadPage(url) {
    loadPage(url, '#mainDiv');
}

function loadPage(url, container) {
    if (container != '#myAuditList' && container != '#tab-content-list' && container != '#tab-content-edit') {
        urlBack.push(url);
    }
    jQuery(container).load(url, function (response, status, xhr) {
        if (status == "success") {
            if (response) {
                try {
                    var result = jQuery.parseJSON(response);
                    if (result.code == 100) {
                        jQuery(container).html("");
                        alert(result.data);
                    }
                } catch (e) {
                    return response;
                }
            }
        }
    });
}

function homeUrlBack(id) {
    if (urlBack.length > 1) {
        var backUl = urlBack[urlBack.length - 2];
        urlBack.splice(urlBack.length - 2, 2);
        loadPage(backUl, id);
    }
}

/**
 * Load a url into a page 增加beforeSend以便拦截器在将该请求识别为非ajax请求
 */
var _old_load = jQuery.fn.load;
jQuery.fn.load = function (url, params, callback) {

    if (typeof url !== "string" && _old_load) {
        return _old_load.apply(this, arguments);
    }

    var selector, type, response, self = this, off = url.indexOf(" ");
    if (off > -1) {
        selector = jQuery.trim(url.slice(off));
        url = url.slice(0, off);
    }
    if (jQuery.isFunction(params)) {
        callback = params;
        params = undefined;
    } else if (params && typeof params === "object") {
        type = "POST";
    }
    if (self.length > 0) {
        jQuery.ajaxSetup({
            cache: true
        });
        jQuery.ajax({
            url: url,
            beforeSend: function (xhr) {
                xhr.setRequestHeader('X-Requested-With', {
                    toString: function () {
                        return '';
                    }
                });
            },
            type: type || "GET",
            dataType: "html",
            data: params
        }).done(
            function (responseText) {
                // console.log(responseText);
                response = arguments;
                // 页面超时跳转到首页
                if (responseText.startWith("<!--login_page_identity-->")) {
                    window.location.href = basePath + "/";
                } else {
                    self.html(selector ? jQuery("<div>").append(
                        jQuery.parseHTML(responseText)).find(selector)
                        : responseText);
                }
            }).always(
            callback
            && function (jqXHR, status) {
                self.each(function () {
                    callback.apply(this,
                        response
                        || [jqXHR.responseText,
                            status, jqXHR]);
                });
            });
    }

    return this;
};

// 递归删除空属性防止把null变成空值
function deleteEmptyProp(obj) {
    for (var a in obj) {
        if (typeof (obj[a]) == "object" && obj[a] != null) {
            deleteEmptyProp(obj[a]);
        } else {
            if (!obj[a]) {
                delete obj[a];
            }
        }
    }
    return obj;
}

function ajaxPost(url, params, callback) {
    var result = null;
    if (params && typeof params == "object") {
        params = deleteEmptyProp(params);
    }
    jQuery.ajax({
        type: 'post',
        async: false,
        url: url,
        data: params,
        dataType: 'json',
        // 请求成功
        success: function (data, status) {
            result = data;
            if (data && data.code && data.code == '101') {
                modals.error("操作失败，请刷新重试，具体错误：" + data.message);
                return false;
            }
            if (callback) {
                callback.call(this, data, status);
            }
        },
        // 请求失败后
        error: function (jqXHR, textStatus, errorThrown) {
            if (jqXHR && jqXHR.readyState && jqXHR.readyState == '4') {
                var sessionstatus = jqXHR.getResponseHeader("session-status");
                if (sessionstatus == "timeout") {
                    // 如果超时就处理 ，指定要跳转的页面
                    window.location.href = basePath + "/";
                } else if (textStatus == "parsererror") {// csrf异常

                } else {
                    modals.error({
                        text: JSON.stringify(jqXHR) + '<br/>err1:'
                        + JSON.stringify(textStatus) + '<br/>err2:'
                        + JSON.stringify(errorThrown),
                        large: true
                    });
                    return;
                }
            }

            modals.error({
                text: JSON.stringify(jqXHR) + '<br/>err1:'
                + JSON.stringify(textStatus) + '<br/>err2:'
                + JSON.stringify(errorThrown),
                large: true
            });
        }
    });
    return result;
}

function getServerTime(base_path, format) {
    var result = null;
    var sdate = new Date(ajaxPost(base_path + '/base/getServerTime'));
    if (sdate != 'Invalid Date') {
        result = formatDate(sdate, format || 'yyyy/mm/dd');
    }
    return result;
}

/**
 * 格式化日期
 */
function formatDate(date, format) {
    if (!date)
        return date;
    date = (typeof date == "number") ? new Date(date) : date;
    return date.Format(format);
}

Date.prototype.Format = function (fmt) {
    var o = {
        "m+": this.getMonth() + 1, // 月份
        "d+": this.getDate(), // 日
        "h+": this.getHours(), // 小时
        "i+": this.getMinutes(), // 分
        "s+": this.getSeconds(), // 秒
        "q+": Math.floor((this.getMonth() + 3) / 3), // 季度
        "S": this.getMilliseconds()
        // 毫秒
    };
    if (/(y+)/.test(fmt))
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "")
            .substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt))
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k])
                : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}


/**
 * 比较两个时间的大小 d1>d2 返回大于0
 *
 * @param d1
 * @param d2
 * @returns {number}
 * @constructor
 */
function DateDiff(d1, d2) {
    var result = Date.parse(d1.replace(/-/g, "/"))
        - Date.parse(d2.replace(/-/g, "/"));
    return result;
}

/**
 * 字符串转日期
 *
 * @returns {number}
 */
String.prototype.strToDate = function () {
    if (this && this != "") {
        return Date.parse(this.replace(/-/g, "/"));
    } else
        return "";
}

/**
 * 将map类型[name,value]的数据转化为对象类型
 */
function getObjectFromMap(aData) {
    var map = {};
    for (var i = 0; i < aData.length; i++) {
        var item = aData[i];
        if (!map[item.name]) {
            map[item.name] = item.value;
        }
    }
    return map;
}

/**
 * 获取下一个编码 000001，000001000006，6 得到结果 000001000007
 */
function getNextCode(prefix, maxCode, length) {
    if (maxCode == null) {
        var str = "";
        for (var i = 0; i < length - 1; i++) {
            str += "0";
        }
        return prefix + str + 1;
    } else {
        var str = "";
        var sno = parseInt(maxCode.substring(prefix.length)) + 1;
        for (var i = 0; i < length - sno.toString().length; i++) {
            str += "0";
        }
        return prefix + str + sno;
    }

}


String.prototype.startWith = function (s) {
    if (s == null || s == "" || this.length == 0 || s.length > this.length)
        return false;
    if (this.substr(0, s.length) == s)
        return true;
    else
        return false;
    return true;
}

String.prototype.replaceAll = function (s1, s2) {
    return this.replace(new RegExp(s1, "gm"), s2);
}

String.prototype.format = function () {
    if (arguments.length == 0)
        return this;
    for (var s = this, i = 0; i < arguments.length; i++)
        s = s.replace(new RegExp("\\{" + i + "\\}", "g"), arguments[i]);
    return s;
};

function dateFormatUtil(longTypeDate) {
    var dateTypeDate = "";
    var date = new Date();
    if (longTypeDate == '' || longTypeDate == null || longTypeDate == undefined) {
        return '-';
    } else {
        date.setTime(longTypeDate);
        dateTypeDate += date.getFullYear(); // 年
        dateTypeDate += "-" + getMonth(date); // 月
        dateTypeDate += "-" + getDay(date); // 日
        return dateTypeDate;
    }

}

// 返回 01-12 的月份值
function getMonth(date) {
    var month = "";
    month = date.getMonth() + 1; // getMonth()得到的月份是0-11
    if (month < 10) {
        month = "0" + month;
    }
    return month;
}

// 返回01-30的日期
function getDay(date) {
    var day = "";
    day = date.getDate();
    if (day < 10) {
        day = "0" + day;
    }
    return day;
}

function getdatetimepicker(id, formatValue) {
    $(id).datetimepicker({
        format: formatValue,
        minView: "month",
        autoclose: true,
        language: "zh-CN"
    });
}

function getCheck(id) {
    $(id).iCheck({
        checkboxClass: 'icheckbox_square-green',
        radioClass: 'iradio_square-green',
        increaseArea: '20%'
    });
}

/**
 * 文件上传
 *
 * @param fileId：上传input id
 * @param url包括：方法名 ,
 *            业务Id：id,
 *            附件分类：type(例如：招投标、合同、施工过程、变更、审计、付款),备注:memo , 绑定业务表名 tableNmae 文件夹
 *            分层 主业务名：projectName 子业务名：SubprojectName(不传为日期作为文件夹名)
 * @param 是否显示拖住框Boolean类型：Drag
 */

function initFile(fileId, url, Drag, data, deleteStrue, uploadBox) {
    var preview = [];
    var previewConfig = [];

    if (data) {
        var attchJson = $.parseJSON(data);
        for (var i = 0; i < attchJson.length; i++) {
            preview.push(attchJson[i].url);
            var o = {};
            switch (attchJson[i].fileType) {
                case "jpg":
                    o.type = "image";
                    break;
                case "png":
                    o.type = "image";
                    break;
                case "gif":
                    o.type = "image";
                    break;
                case "bmp":
                    o.type = "image";
                    break;
                case "pdf":
                    o.type = "pdf";
                    break;
                case "PDF":
                    o.type = "pdf";
                    break;
                case "ini":
                    o.type = "text";
                    break;
                case "txt":
                    o.type = "text";
                    break;

                default:
                    o.type = "image";
                    break;
            }
            o.caption = attchJson[i].caption;
            o.size = attchJson[i].size;
            o.key = attchJson[i].key;
            o.url = "/file/fileDelete?id=" + attchJson[i].key;
            previewConfig.push(o);
        }
    }

    $(fileId).fileinput({
        language: 'zh',
        theme: 'fa',
        uploadUrl: url,
        initialPreviewAsData: true,
        preferIconicPreview: true,
        previewFileIconSettings: {
            'doc': '<i class="fa fa-file-word-o text-primary"></i>',
            'docx': '<i class="fa fa-file-word-o text-primary"></i>',
            'xls': '<i class="fa fa-file-excel-o text-success"></i>',
            'ppt': '<i class="fa fa-file-powerpoint-o text-danger"></i>',
            'zip': '<i class="fa fa-file-archive-o text-muted"></i>',
            'htm': '<i class="fa fa-file-code-o text-info"></i>',
            'gif': '<i class="fa fa-file-photo-o text-muted"></i>',
            //'pdf' : '<i class="fa fa-file-pdf-o text-danger" ></i>',
            'txt': '<i class="fa fa-file-text-o text-info"></i>',
        },
        initialPreview: preview,
        initialPreviewConfig: previewConfig,
        previewFileExtSettings: {
            'doc': function (ext) {
                return ext.match(/(doc|docx)$/i);
            },
            'xls': function (ext) {
                return ext.match(/(xls|xlsx)$/i);
            },
            'ppt': function (ext) {
                return ext.match(/(ppt|pptx)$/i);
            },
            'jpg': function (ext) {
                return ext.match(/(jpg|png|gif|bmp)$/i);
            },
            'zip': function (ext) {
                return ext.match(/(zip|rar|tar|gzip|gz|7z)$/i);
            },
            'htm': function (ext) {
                return ext.match(/(php|js|css|htm|html)$/i);
            },
            'txt': function (ext) {
                return ext.match(/(txt|ini|md)$/i);
            },
            /*'pdf' : function(ext) {
                return ext.match(/(pdf)$/i);
            },*/
        },
        otherActionButtons: '<button type="button" class="kv-file-down btn btn-xs btn-default" {dataKey} title="下载附件"><i class="fa fa-cloud-download"></i></button>',
        overwriteInitial: false, // 不覆盖已存在的图片
        uploadAsync: true, // 异步上传
        showUpload: false, // 是否显示上传按钮
        showRemove: false, // 显示移除按钮
        showPreview: true, // 是否显示预览
        showCaption: true,// 是否显示标题
        showCancel: false,// 是否显示取消按钮
        dropZoneEnabled: Drag,// 是否显示拖拽区域		
        maxFileSize: 100000000000,// 上传文件最大值
        maxFilesNum: 10,// 上传文件最大个数

        allowedPreviewTypes: ['image', 'html', 'text', 'video', 'audio', 'flash', 'pdf', 'object'],
        layoutTemplates: deleteStrue,
        showBrowse: uploadBox,
        showCaption: uploadBox,
        enctype: 'multipart/form-data',
        slugCallback: function (filename) {
            return filename.replace('(', '_').replace(']', '_');
        }

    }).on("filebatchselected", function (event, data, previewId, index) {
        $(this).fileinput("upload");
    });


    $(fileId).on("fileuploaded", function (event, data, previewId, index) {
        console.log(data);
        var status = data.response;
        if (status.success == "true") {
            $("#" + previewId).attr({'id': status.id});
            var id = status.id;
        } else {
            alert('文件上传失败');
        }
    });

    $(fileId).on('filesuccessremove', function (event, id) {
        $.ajax({
            type: 'POST',
            url: "/file/fileDelete?id=" + id,
            dataType: "json",
            success: function (data, status) {
                if (data.success == "false") {
                    alert("文件删除失败");
                }
            }
        });
    });
}

//文件下载
function downloadFile() {
    $('.kv-file-down').on('click', function () {
        $.ajax({
            type: 'POST',
            url: "/file/downloadFile?id=" + $(this).data("key"),
            dataType: "json",
            success: function (data, status) {
                if (data.success == "true") {
                    window.open(data.path);
                } else {
                    alert("文件下载失败");
                }
            }
        });
    });
}

/**
 * 文件下载老方法
 * @param fileId：下载input id
 */
function initDownloadFile(fileId, initialPreview, initialPreviewConfig, deleteStrue) {
    $(fileId).fileinput({
        language: 'zh',
        theme: 'fa',
        initialPreviewAsData: true,
        previewFileIcon: '<i class="fa fa-file"></i>',
        preferIconicPreview: true,
        previewFileIconSettings: {
            'doc': '<i class="fa fa-file-word-o text-primary" ></i>',
            'xls': '<i class="fa fa-file-excel-o text-success" ></i>',
            'ppt': '<i class="fa fa-file-powerpoint-o text-danger"></i>',
            /*'pdf' : '<i class="fa fa-file-pdf-o text-danger" ></i>',*/
            'zip': '<i class="fa fa-file-archive-o text-muted" ></i>',
            'gif': '<i class="fa fa-file-photo-o text-muted" ></i>',
            'htm': '<i class="fa fa-file-code-o text-info" ></i>',
            'txt': '<i class="fa fa-file-text-o text-info" ></i>',
        },
        previewFileExtSettings: {
            'doc': function (ext) {
                return ext.match(/(doc|docx)$/i);
            },
            'xls': function (ext) {
                return ext.match(/(xls|xlsx)$/i);
            },
            'ppt': function (ext) {
                return ext.match(/(ppt|pptx)$/i);
            },
            'zip': function (ext) {
                return ext.match(/(zip|rar|tar|gzip|gz|7z)$/i);
            },
            'htm': function (ext) {
                return ext.match(/(htm|html)$/i);
            },
            'txt': function (ext) {
                return ext.match(/(txt|ini|csv|java|php|js|css)$/i);
            },
            'pdf': function (ext) {
                return ext.match(/(pdf)$/i);
            },
        },
        showBrowse: false,// 显示选择文件
        showUpload: false,// 显示下载
        showCaption: false,// 是否显示被选文件的简介
        showRemove: false, // 显示移除按钮
        showPreview: true, // 是否显示预览
        showCaption: false,// 是否显示标题 data
        showCancel: false,// 是否显示取消按钮
        browseClass: "btn btn-primary btn-lg",
        previewFileIcon: "<i class='glyphicon glyphicon-king'></i>",
        otherActionButtons: '<button type="button" class="kv-file-down btn btn-xs btn-default" {datakey} title="下载附件"><i class="fa fa-cloud-download"></i></button>',
        overwriteInitial: false,
        initialPreviewAsData: true,
        initialPreview: initialPreview,
        initialPreviewConfig: initialPreviewConfig,
        layoutTemplates: deleteStrue
    });
    var test = function (initialPreview) {

    }

    $('.kv-file-down').on('click', function () {
        $.ajax({
            type: 'POST',
            url: "/file/downloadFile?id=" + $(this).data("key"),
            dataType: "json",
            success: function (data, status) {
                if (data.success == "true") {
                    window.open(data.path);
                    //window.location.href = data.path;
                } else {
                    alert("文件下载失败");
                }
            }
        });
    })
}

function formatterDate(value, row, index) {
    if (value === '' || value === undefined || !value)
        return '-';
    return new Date(value).Format("yyyy-mm-dd");
}

function formatDateTime(time, format) {
    var t = new Date(time);
    var tf = function (i) {
        return (i < 10 ? '0' : '') + i
    };
    return format.replace(/yyyy|MM|dd|HH|mm|ss/g, function (a) {
        switch (a) {
            case 'yyyy':
                return tf(t.getFullYear());
                break;
            case 'MM':
                return tf(t.getMonth() + 1);
                break;
            case 'mm':
                return tf(t.getMinutes());
                break;
            case 'dd':
                return tf(t.getDate());
                break;
            case 'HH':
                return tf(t.getHours());
                break;
            case 'ss':
                return tf(t.getSeconds());
                break;
        }
    })
}

//返回几秒前，几分钟前，几小时前，几天前
function getDateDiff(dateStr) {
    var publishTime = dateStr / 1000,
        d_seconds,
        d_minutes,
        d_hours,
        d_days,
        timeNow = parseInt(new Date().getTime() / 1000),
        d,

        date = new Date(publishTime * 1000),
        Y = date.getFullYear(),
        M = date.getMonth() + 1,
        D = date.getDate(),
        H = date.getHours(),
        m = date.getMinutes(),
        s = date.getSeconds();
    //小于10的在前面补0
    if (M < 10) {
        M = '0' + M;
    }
    if (D < 10) {
        D = '0' + D;
    }
    if (H < 10) {
        H = '0' + H;
    }
    if (m < 10) {
        m = '0' + m;
    }
    if (s < 10) {
        s = '0' + s;
    }

    d = timeNow - publishTime;
    d_days = parseInt(d / 86400);
    d_hours = parseInt(d / 3600);
    d_minutes = parseInt(d / 60);
    d_seconds = parseInt(d);

    if (d_days > 0 && d_days < 3) {
        return d_days + '天前';
    } else if (d_days <= 0 && d_hours > 0) {
        return d_hours + '小时前';
    } else if (d_hours <= 0 && d_minutes > 0) {
        return d_minutes + '分钟前';
    } else if (d_seconds < 60) {
        if (d_seconds <= 0) {
            return '刚刚发表';
        } else {
            return d_seconds + '秒前';
        }
    } else if (d_days >= 3 && d_days < 30) {
        return M + '-' + D + '&nbsp;' + H + ':' + m;
    } else if (d_days >= 30) {
        return Y + '-' + M + '-' + D + '&nbsp;' + H + ':' + m;
    }
}

//判断是否非空
function isEmpty(obj) {
    if (typeof obj == "undefined" || obj == null || obj == "") {
        return true;
    } else {
        return false;
    }
}

//选中table行样式
$(".table").on("click-row.bs.table", function (e, row, ele) {
    $(".danger").removeClass("danger");
    $(ele).addClass("danger");
});

// })(jQuery)


function loadPageStatus(formId, name) {
    var form = $(formId).form();
    var storage = window.sessionStorage;
    var obj = new Object();
    if (storage.length > 0) {
        var inPlantableData = JSON.parse(storage.getItem(name));
        if (inPlantableData != null) {
            pageNum = parseInt(inPlantableData.pageNum);
            obj.pageNum = pageNum;
            pageSize = parseInt(inPlantableData.pageSize);
            obj.pageSize = pageSize;
            form.initFormData(inPlantableData);
        }
        return obj;
    } else {
        return null;
    }
}
