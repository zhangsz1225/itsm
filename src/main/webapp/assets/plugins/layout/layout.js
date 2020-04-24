﻿$(function() {
    $(".ltable tr:nth-child(odd)").addClass("odd_bg");
    $("#floatHead").smartFloat();
    $(".rule-single-checkbox").ruleSingleCheckbox();
    $(".rule-single-checkbox-onoff").ruleSingleCheckboxOnOff();
    $(".rule-multi-checkbox").ruleMultiCheckbox();
    $(".rule-multi-radio").ruleMultiRadio();
    $(".rule-single-select").ruleSingleSelect();
    $(".rule-multi-porp").ruleMultiPorp()
});
function checkAll(a) {
    if ($(a).text() == "全选") {
        $(a).children("span").text("取消");
        $(".checkall input:enabled").prop("checked", true)
    } else {
        $(a).children("span").text("全选");
        $(".checkall input:enabled").prop("checked", false)
    }
}
function tabs(a) {
    var b = $(a).parent().index("li");
    $(a).parent().parent().find("li a").removeClass("selected");
    $(a).addClass("selected");
    $(".tab-content").hide();
    $(".tab-content").eq(b).show()
}
function checkNumber(a) {
    if (isFirefox = navigator.userAgent.indexOf("Firefox") > 0) {
        if (! ((a.which >= 48 && a.which <= 57) || (a.which >= 96 && a.which <= 105) || (a.which == 8) || (a.which == 46))) {
            return false
        }
    } else {
        if (! ((event.keyCode >= 48 && event.keyCode <= 57) || (event.keyCode >= 96 && event.keyCode <= 105) || (event.keyCode == 8) || (event.keyCode == 46))) {
            event.returnValue = false
        }
    }
}
function checktxt(d, b) {
    var c = $(d).val().length;
    if (c < 1) {
        return false
    }
    var a = Math.ceil(c / 62);
    $("#" + b).html("您已输入<b>" + c + "</b>个字符，将以<b>" + a + "</b>条短信扣取费用。")
}
function ForDight(b, a) {
    b = Math.round(b * Math.pow(10, a)) / Math.pow(10, a);
    return b
}
function addCookie(e, a, d) {
    var f = e + "=" + escape(a);
    if (d > 0) {
        var c = new Date();
        var b = d * 3600 * 1000;
        c.setTime(c.getTime() + b);
        f += "; expires=" + c.toGMTString()
    }
    document.cookie = f
}
function getCookie(c) {
    var d = document.cookie.split("; ");
    for (var b = 0; b < d.length; b++) {
        var a = d[b].split("=");
        if (a[0] == c) {
            return unescape(a[1])
        }
    }
    return ""
}
function jsprint(c, b, a, e) {
    var d = "";
    switch (a) {
    case "Success":
        d = "32X32/succ.png";
        break;
    case "Error":
        d = "32X32/fail.png";
        break;
    default:
        d = "32X32/hits.png";
        break
    }
    var s = $.dialog.tips(c, 2, d);
    s.dialogClass = 'alert';
    if (b == "back") {
        frames["mainframe"].history.back(-1);
    } else {
        if (b != "") {
            frames["mainframe"].location.href = b;
        }
    }
    if (arguments.length == 4) {
        e()
    }
}
function jsdialog(e, a, c, b, h) {
    var g = "";
    var f = arguments.length;
    switch (b) {
    case "Success":
        g = "success.gif";
        break;
    case "Error":
        g = "error.gif";
        break;
    default:
        g = "alert.gif";
        break
    }
    var d = $.dialog({
        title: e,
        content: a,
        fixed: true,
        min: false,
        max: false,
        lock: true,
        icon: g,
        ok: true,
        close: function() {
            if (c == "back") {
                history.back(-1);
            } else {
                if (c != "") {
                    location.href = c
                }
            }
            if (f == 5) {
                h();
            }
        }
    })
}
function ShowMaxDialog(b, a) {
    $.dialog({
        title: b,
        content: "url:" + a,
        min: false,
        max: false,
        lock: false
    }).max()
}
function ExePostBack(a, b) {
    if ($(".checkall input:checked").size() < 1) {
        $.dialog.alert("对不起，请选中您要操作的记录！");
        return false
    }
    var c = "删除记录后不可恢复，您确定吗？";
    if (arguments.length == 2) {
        c = b
    }
    $.dialog.confirm(c,
    function() {
        __doPostBack(a, "")
    });
    return false
}
function CheckPostBack(a, b) {
    var c = "对不起，请选中您要操作的记录！";
    if (arguments.length == 2) {
        c = b
    }
    if ($(".checkall input:checked").size() < 1) {
        $.dialog.alert(c);
        return false
    }
    __doPostBack(a, "");
    return false
}
function ExeNoCheckPostBack(a, b) {
    var c = "删除记录后不可恢复，您确定吗？";
    if (arguments.length == 2) {
        c = b
    }
    $.dialog.confirm(c,
    function() {
        __doPostBack(a, "")
    });
    return false
}
$.fn.initValidform = function() {
    var a = function(b) {
        $(b).Validform({
            tiptype: function(h, g, f) {
                if (!g.obj.is("form")) {
                    if (g.obj.is(g.curform.find(".Validform_error:first"))) {
                        var e = g.obj.parents(".tab-content");
                        var d = $(".tab-content").index(e);
                        if (!$(".content-tab ul li").eq(d).children("a").hasClass("selected")) {
                            $(".content-tab ul li a").removeClass("selected");
                            $(".content-tab ul li").eq(d).children("a").addClass("selected");
                            $(".tab-content").hide();
                            e.show()
                        }
                    }
                    if (g.obj.parents("dd").find(".Validform_checktip").length == 0) {
                        g.obj.parents("dd").append("<span class='Validform_checktip' />");
                        g.obj.parents("dd").next().find(".Validform_checktip").remove()
                    }
                    var c = g.obj.parents("dd").find(".Validform_checktip");
                    f(c, g.type);
                    c.text(h)
                }
            },
            showAllError: true
        })
    };
    return $(this).each(function() {
        a($(this))
    })
};
$.fn.smartFloat = function() {
    var a = function(b) {
        var c = b.position().top;
        var d = b.css("position");
        $(window).scroll(function() {
            var e = $(this).scrollTop();
            if (e > c) {
                if (window.XMLHttpRequest) {
                    b.css({
                        position: "fixed",
                        top: 0
                    })
                } else {
                    b.css({
                        top: e
                    })
                }
            } else {
                b.css({
                    position: d,
                    top: c
                })
            }
        })
    };
    return $(this).each(function() {
        a($(this))
    })
};

$.fn.ruleSingleCheckboxOnOff = function() {
    var a = function(d) {
        var c = d.children("input:checkbox").eq(0);
        d.children().hide();
        var b = $('<a href="javascript:;">' + '<i class="off">关</i>' + '<i class="on">开</i>' + "</a>").prependTo(d);
        d.addClass("single-checkbox");
        if (c.prop("checked") == true) {
            b.addClass("selected")
        }
        if (c.prop("disabled") == true) {
            b.css("cursor", "default");
            return
        }
        $(b).click(function() {
            if ($(this).hasClass("selected")) {
                $(this).removeClass("selected")
            } else {
                $(this).addClass("selected")
            }
            c.trigger("click")
        })
    };
    return $(this).each(function() {
        a($(this))
    })
};
$.fn.ruleSingleCheckbox = function() {
    var a = function(d) {
        var c = d.children("input:checkbox").eq(0);
        d.children().hide();
        var b = $('<a href="javascript:;">' + '<i class="off">否</i>' + '<i class="on">是</i>' + "</a>").prependTo(d);
        d.addClass("single-checkbox");
        if (c.prop("checked") == true) {
            b.addClass("selected")
        }
        if (c.prop("disabled") == true) {
            b.css("cursor", "default");
            return
        }
        $(b).click(function() {
            if ($(this).hasClass("selected")) {
                $(this).removeClass("selected")
            } else {
                $(this).addClass("selected")
            }
            c.trigger("click")
        })
    };
    return $(this).each(function() {
        a($(this))
    })
};
$.fn.ruleMultiCheckbox = function() {
    var a = function(c) {
        c.addClass("multi-checkbox");
        c.children().hide();
        var b = $('<div class="boxwrap"></div>').prependTo(c);
        c.find(":checkbox").each(function() {
            var d = c.find(":checkbox").index(this);
            var e = $('<a href="javascript:;">' + c.find("label").eq(d).text() + "</a>").appendTo(b);
            if ($(this).prop("checked") == true) {
                e.addClass("selected")
            }
            if ($(this).prop("disabled") == true) {
                e.css("cursor", "default");
                return
            }
            $(e).click(function() {
                if ($(this).hasClass("selected")) {
                    $(this).removeClass("selected")
                } else {
                    $(this).addClass("selected")
                }
                c.find(":checkbox").eq(d).trigger("click")
            })
        })
    };
    return $(this).each(function() {
        a($(this))
    })
};
$.fn.ruleMultiPorp = function() {
    var a = function(c) {
        c.addClass("multi-porp");
        c.children().hide();
        var b = $("<ul></ul>").prependTo(c);
        c.find(":checkbox").each(function() {
            var d = c.find(":checkbox").index(this);
            var f = $("<li></li>").appendTo(b);
            var e = $('<a href="javascript:;">' + c.find("label").eq(d).text() + "</a><i></i>").appendTo(f);
            if ($(this).prop("checked") == true) {
                f.addClass("selected")
            }
            if ($(this).prop("disabled") == true) {
                e.css("cursor", "default");
                return
            }
            $(e).click(function() {
                if ($(this).parent().hasClass("selected")) {
                    $(this).parent().removeClass("selected")
                } else {
                    $(this).parent().addClass("selected")
                }
                c.find(":checkbox").eq(d).trigger("click")
            })
        })
    };
    return $(this).each(function() {
        a($(this))
    })
};
$.fn.ruleMultiRadio = function() {
    var a = function(c) {
        c.addClass("multi-radio");
        c.children().hide();
        var b = $('<div class="boxwrap"></div>').prependTo(c);
        c.find('input[type="radio"]').each(function() {
            var d = c.find('input[type="radio"]').index(this);
            var e = $('<a href="javascript:;">' + c.find("label").eq(d).text() + "</a>").appendTo(b);
            if ($(this).prop("checked") == true) {
                e.addClass("selected")
            }
            if ($(this).prop("disabled") == true) {
                e.css("cursor", "default");
                return
            }
            $(e).click(function() {
                $(this).siblings().removeClass("selected");
                $(this).addClass("selected");
                c.find('input[type="radio"]').prop("checked", false);
                c.find('input[type="radio"]').eq(d).prop("checked", true);
                c.find('input[type="radio"]').eq(d).trigger("click")
            })
        })
    };
    return $(this).each(function() {
        a($(this))
    })
};
$.fn.ruleSingleSelect = function() {
    var a = function(f) {
        f.addClass("single-select");
        f.children().hide();
        var d = $('<div class="boxwrap"></div>').prependTo(f);
        var e = $('<a class="select-tit" href="javascript:;"><span></span><i></i></a>').appendTo(d);
        var c = $('<div class="select-items"><ul></ul></div>').appendTo(d);
        var b = $('<i class="arrow"></i>').appendTo(d);
        var g = f.find("select").eq(0);
        g.find("option").each(function(j) {
            var h = g.find("option").index(this);
            var k = $("<li>" + $(this).text() + "</li>").appendTo(c.find("ul"));
            if ($(this).prop("selected") == true) {
                k.addClass("selected");
                e.find("span").text($(this).text())
            }
            if ($(this).prop("disabled") == true) {
                k.css("cursor", "default");
                return
            }
            k.click(function() {
                $(this).siblings().removeClass("selected");
                $(this).addClass("selected");
                g.find("option").prop("selected", false);
                g.find("option").eq(h).prop("selected", true);
                e.find("span").text($(this).text());
                b.hide();
                c.hide();
                g.trigger("change")
            })
        });
        if (g.prop("disabled") == true) {
            e.css("cursor", "default");
            return
        }
        e.click(function(h) {
            h.stopPropagation();
            if (c.is(":hidden")) {
                $(".single-select .select-items").hide();
                $(".single-select .arrow").hide();
                b.css("z-index", "1");
                c.css("z-index", "1");
                b.show();
                c.show()
            } else {
                b.css("z-index", "");
                c.css("z-index", "");
                b.hide();
                c.hide()
            }
        });
        $(document).click(function(h) {
            g.trigger("blur");
            b.hide();
            c.hide()
        })
    };
    return $(this).each(function() {
        a($(this))
    })
};