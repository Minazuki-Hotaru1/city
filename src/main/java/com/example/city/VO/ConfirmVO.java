package com.example.city.VO;

import lombok.Data;

@Data
public class ConfirmVO {
    private String id;
    private String username;
    private String password;

    private String typeID;
    private String address;
    private String roles;
    private String roleName;

    private String haveSeeIt;
}
