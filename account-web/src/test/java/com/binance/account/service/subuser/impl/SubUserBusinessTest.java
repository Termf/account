package com.binance.account.service.subuser.impl;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import com.binance.account.vo.subuser.SubUserEmailVo;
import com.binance.account.vo.subuser.request.BindingParentSubUserReq;
import com.binance.account.vo.subuser.request.ParentUserIdReq;
import com.binance.account.vo.subuser.request.UpdateSubUserRemarkReq;
import com.binance.account.vo.subuser.request.UserIdReq;
import com.binance.account.vo.subuser.response.SubUserTypeResponse;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

/**
 * Created by Fei.Huang on 2018/11/14.
 */
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class SubUserBusinessTest extends SubUserUnitTest {

    @Test
    public void testIsSubUserFunctionEnabled() throws Exception {
        APIRequest<ParentUserIdReq> request = new APIRequest<>();
        ParentUserIdReq parentUserIdReq = new ParentUserIdReq();
        parentUserIdReq.setParentUserId(positiveParentUserId);
        request.setBody(parentUserIdReq);
        APIResponse<Boolean> apiResponse = subUserBusiness.isSubUserFunctionEnabled(request);
        assert apiResponse.getData();

        parentUserIdReq.setParentUserId(positiveSubUserId);
        request.setBody(parentUserIdReq);
        APIResponse<Boolean> apiResponse2 = subUserBusiness.isSubUserFunctionEnabled(request);
        assert !apiResponse2.getData();

        thrown.expectMessage(GeneralCode.USER_NOT_EXIST.getMessage());
        parentUserIdReq.setParentUserId(negativeParentUserId);
        request.setBody(parentUserIdReq);
        subUserBusiness.isSubUserFunctionEnabled(request);
    }



    @Test
    public void testCheckRelationByUserId() throws Exception {
        APIRequest<UserIdReq> request = new APIRequest<>();
        UserIdReq userIdReq = new UserIdReq();
        userIdReq.setUserId(positiveParentUserId);
        request.setBody(userIdReq);
        APIResponse<SubUserTypeResponse> apiResponse = subUserBusiness.checkRelationByUserId(request);
        SubUserTypeResponse subUserTypeResponse = apiResponse.getData();
        List<Long> subUserIds = subUserTypeResponse.getSubUserIds();
        List<SubUserEmailVo> subUserEmailVos = subUserTypeResponse.getSubUserIdEmails();
        assert subUserTypeResponse.getUserType() == SubUserTypeResponse.UserType.PARENT;
        assert !CollectionUtils.isEmpty(subUserIds);
        assert !CollectionUtils.isEmpty(subUserEmailVos);

        userIdReq.setUserId(positiveSubUserId);
        APIResponse<SubUserTypeResponse> apiResponse2 = subUserBusiness.checkRelationByUserId(request);
        SubUserTypeResponse subUserTypeResponse2 = apiResponse2.getData();
        List<Long> subUserIds2 = subUserTypeResponse2.getSubUserIds();
        List<SubUserEmailVo> subUserEmailVos2 = subUserTypeResponse2.getSubUserIdEmails();
        assert subUserTypeResponse2.getUserType() == SubUserTypeResponse.UserType.SUB;
        assert CollectionUtils.isEmpty(subUserIds2);
        assert CollectionUtils.isEmpty(subUserEmailVos2);

        userIdReq.setUserId(positiveUserId);
        APIResponse<SubUserTypeResponse> apiResponse3 = subUserBusiness.checkRelationByUserId(request);
        SubUserTypeResponse subUserTypeResponse3 = apiResponse3.getData();
        List<Long> subUserIds3 = subUserTypeResponse3.getSubUserIds();
        List<SubUserEmailVo> subUserEmailVos3 = subUserTypeResponse3.getSubUserIdEmails();
        assert subUserTypeResponse3.getUserType() == SubUserTypeResponse.UserType.NORMAL;
        assert CollectionUtils.isEmpty(subUserIds3);
        assert CollectionUtils.isEmpty(subUserEmailVos3);
    }

    @Test
    public void testCheckRelationByParentSubUserIds() throws Exception {
        APIRequest<BindingParentSubUserReq> request = new APIRequest<>();
        BindingParentSubUserReq bindingParentSubUserReq = new BindingParentSubUserReq();
        bindingParentSubUserReq.setParentUserId(positiveParentUserId);
        bindingParentSubUserReq.setSubUserId(positiveSubUserId);
        request.setBody(bindingParentSubUserReq);
        APIResponse<Boolean> apiResponse = subUserBusiness.checkRelationByParentSubUserIds(request);
        assert apiResponse.getData();

        bindingParentSubUserReq.setParentUserId(negativeParentUserId);
        bindingParentSubUserReq.setSubUserId(positiveSubUserId);
        request.setBody(bindingParentSubUserReq);
        APIResponse<Boolean> apiResponse2 = subUserBusiness.checkRelationByParentSubUserIds(request);
        assert !apiResponse2.getData();
    }

    @Test
    public void testNotSubUserOrIsEnabledSubUser() throws Exception {
        APIRequest<UserIdReq> request = new APIRequest<>();
        UserIdReq userIdReq = new UserIdReq();
        userIdReq.setUserId(positiveUserId);
        request.setBody(userIdReq);
        APIResponse<Boolean> apiResponse = subUserBusiness.notSubUserOrIsEnabledSubUser(request);
        assert apiResponse.getData();

        userIdReq.setUserId(positiveSubUserId);
        APIResponse<Boolean> apiResponse2 = subUserBusiness.notSubUserOrIsEnabledSubUser(request);
        assert apiResponse2.getData();

        userIdReq.setUserId(disabledSubUserId);
        APIResponse<Boolean> apiResponse3 = subUserBusiness.notSubUserOrIsEnabledSubUser(request);
        assert !apiResponse3.getData();
    }

}
