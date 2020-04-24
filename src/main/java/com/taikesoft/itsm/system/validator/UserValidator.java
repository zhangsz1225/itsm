package com.taikesoft.itsm.system.validator;

import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class UserValidator extends Validator  {

    @Override
    protected void handleError(Controller c) {
        c.renderJson();

    }

    @Override
    protected void validate(Controller c) {

        setShortCircuit(true);
        validateRequired("sysAccount.username","username_msg","账号不能为空！");
        validateRequired("sysAccount.name","name_msg","姓名不能为空！");
        validateRequired("sysAccount.sort_number","sort_number_msg","排序号不能为空！");
        String sortNumber= c.getPara("sysAccount.sort_number");
        if(!sortNumber.matches("^[0-9]*$")) {
            addError("sort_number_msg", "排序号必须为数字！");
        }
    }
}