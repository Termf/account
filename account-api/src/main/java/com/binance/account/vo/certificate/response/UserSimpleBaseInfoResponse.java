package com.binance.account.vo.certificate.response;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@ApiModel("乌干达用户KYC简易信息")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSimpleBaseInfoResponse {

    private String firstName;
    private String lastName;
    private String middleName;
    private Date dob;
    private String nationality;

}
