package com.binance.account.vo.certificate.response;

import com.binance.account.util.IdNumberMaskUtil;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author liliang1
 * @date 2019-04-25 19:37
 */
@ApiModel("用户KYC的国籍")
@Data
public class UserKycCountryResponse extends ToString {

    private static final long serialVersionUID = 1323998382370245703L;

    private static final String ID_NUMBER_MARK = "************";

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("认证类型: 1:个人认证, 2:企业认证 -1:未认证 ")
    private Integer certificateType;

    @ApiModelProperty("如果认证过KYC，返回两位的国家码")
    private String countryCode;

    @ApiModelProperty("kyc认证通过后的名")
    private String firstName = "";

    @ApiModelProperty("kyc认证通过后的姓")
    private String lastName = "";

    @ApiModelProperty("kyc认证通过的地址")
    private String address = "";

    @ApiModelProperty("kyc认证通过的城市")
    private String city;

    @ApiModelProperty("kyc认证通过的证件号")
    private String idNumber = "";

    @ApiModelProperty("认证的生日")
    private String birthday;

    @ApiModelProperty("kyc认证通过的证件类型")
    private String documentType = "";

    @ApiModelProperty("是否能做C2C交易")
    private boolean canDoC2C = false;

    @ApiModelProperty("用户basic输入的firstName")
    private String fillFirstName;
    @ApiModelProperty("用户basic输入的lastName")
    private String fillLastName;
    @ApiModelProperty("用户basic输入的middleName")
    private String fillMiddleName;
    @ApiModelProperty("用户basic输入的生日")
    private String fillBirthday;

    public void setFirstName(String firstName) {
        this.firstName = StringUtils.isBlank(firstName) ? "" : firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = StringUtils.isBlank(lastName) ? "" : lastName;
    }

    public void setAddress(String address) {
        this.address = StringUtils.isBlank(address) ? "" : address;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = IdNumberMaskUtil.getIdNumberMark(idNumber);
    }

    public void setDocumentType(String documentType) {
        this.documentType = StringUtils.isBlank(documentType) ? "" : documentType;
    }

    public void setFillFirstName(String fillFirstName) {
        this.fillFirstName = StringUtils.isBlank(fillFirstName) ? "" : fillFirstName;
    }

    public void setFillLastName(String fillLastName) {
        this.fillLastName = StringUtils.isBlank(fillLastName) ? "" : fillLastName;
    }

    public void setFillMiddleName(String fillMiddleName) {
        this.fillMiddleName = StringUtils.isBlank(fillMiddleName) ? "" : fillMiddleName;
    }
}
