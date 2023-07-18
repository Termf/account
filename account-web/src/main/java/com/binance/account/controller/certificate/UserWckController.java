package com.binance.account.controller.certificate;

import com.binance.account.api.UserWckApi;
import com.binance.account.common.constant.WckConst;
import com.binance.account.common.query.SearchResult;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingMapper;
import com.binance.account.service.certificate.CertificateHelper;
import com.binance.account.service.certificate.impl.NewUserWckBusiness;
import com.binance.account.service.certificate.impl.UserWckBusiness;
import com.binance.account.vo.certificate.UserChannelWckAuditVo;
import com.binance.account.vo.certificate.UserWckAuditVo;
import com.binance.account.vo.certificate.request.UserChannelWckQuery;
import com.binance.account.vo.certificate.request.UserWckQuery;
import com.binance.account.vo.certificate.request.WckAuditRequest;
import com.binance.account.vo.certificate.request.WckChannelAuditRequest;
import com.binance.inspector.vo.worldcheck.WckResultProfileVo;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Shining.Cai on 2018/09/10.
 **/
@RestController
public class UserWckController implements UserWckApi {

    @Autowired
    private UserWckBusiness userWckBusiness;
    @Autowired
    private NewUserWckBusiness newUserWckBusiness;
    @Autowired
    private CertificateHelper certificateHelper;

    @Override
    public APIResponse<SearchResult<UserWckAuditVo>> getList(@RequestBody APIRequest<UserWckQuery> request) {
        return APIResponse.getOKJsonResult(userWckBusiness.listForAdmin(request.getBody()));
    }

    @Override
    public APIResponse<SearchResult<UserChannelWckAuditVo>> getListByPages(@RequestBody APIRequest<UserChannelWckQuery> request) {
        return APIResponse.getOKJsonResult(newUserWckBusiness.listForAdminByPage(request.getBody()));
    }

    @Override
    public APIResponse<Void> audit(@RequestBody @Validated APIRequest<WckAuditRequest> request) {
        WckAuditRequest body = request.getBody();
        if (StringUtils.containsAny(body.getMemo(), WckConst.SEPARATOR_COL, WckConst.SEPARATOR_ROW)){
            return APIResponse.getErrorJsonResult("审核评价中不可包含'$'、'#'等特殊字符");
        }
        userWckBusiness.audit(body);
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<?> getWckResultProfile(@RequestParam("kycId") Long kycId) {
        List<WckResultProfileVo> list = userWckBusiness.getWckResultProfile(kycId);
        return APIResponse.getOKJsonResult(list);
    }

    @Override
    public APIResponse<Boolean> isWckSwitch() {
        boolean on = certificateHelper.isSwitchOn();
        if (on){
            return APIResponse.getOKJsonResult(true);
        }else {
            return APIResponse.getErrorJsonResult("World-Check未开启");
        }
    }

    @Override
    public APIResponse<Void> newAudit(@RequestBody @Validated APIRequest<WckChannelAuditRequest> request) {
        WckChannelAuditRequest body = request.getBody();
        if (StringUtils.containsAny(body.getMemo(), WckConst.SEPARATOR_COL, WckConst.SEPARATOR_ROW)){
            return APIResponse.getErrorJsonResult("审核评价中不可包含'$'、'#'等特殊字符");
        }
        newUserWckBusiness.newAudit(body);
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<?> getChannelWckResultProfile(@RequestParam("caseId") String caseId) {
        List<WckResultProfileVo> list = newUserWckBusiness.getChannelWckResultProfile(caseId);
        return APIResponse.getOKJsonResult(list);
    }

    @Override
    public APIResponse<Void> resetChannelWck(@RequestParam("caseId") String caseId) {
        if(StringUtils.isBlank(caseId)){
            return APIResponse.getErrorJsonResult("传入的caseId为空");
        }
        newUserWckBusiness.resetChannelWck(caseId);
        return APIResponse.getOKJsonResult();
    }
}
