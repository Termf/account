package com.binance.account.data.entity.apimanage;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class ApiDeletedModel extends ApiModel {

    private Long originalId;
    private Date deleteTime;
    private String email;

}
