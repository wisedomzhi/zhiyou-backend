package com.wisewind.zhiyou.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 709894818199818039L;

    private String username;
    private String userAccount;
    private String userPassword;
    private String checkPassword;

}
