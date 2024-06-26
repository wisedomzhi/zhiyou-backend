package com.wisewind.zhiyou.model.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 6277352785631800023L;

    private String userAccount;
    private String userPassword;
}
