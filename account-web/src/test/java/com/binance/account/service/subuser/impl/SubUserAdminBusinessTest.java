package com.binance.account.service.subuser.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import com.binance.account.vo.subuser.request.BindingParentSubUserReq;
import com.binance.account.vo.subuser.request.ParentUserIdReq;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

/**
 * Created by Fei.Huang on 2018/11/14.
 */
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class SubUserAdminBusinessTest extends SubUserUnitTest {

    @Test
    public void testEnableSubUserFunction() throws Exception {
        APIRequest<ParentUserIdReq> request = new APIRequest<>();
        ParentUserIdReq parentUserIdReq = new ParentUserIdReq();
        parentUserIdReq.setParentUserId(positiveUserId);
        request.setBody(parentUserIdReq);
        APIResponse<Boolean> apiResponse = subUserAdminBusiness.enableSubUserFunction(request);
        assert apiResponse.getData();

        thrown.expectMessage(GeneralCode.SUB_UER_FUNCTION_ALREADY_ENABLED.getMessage());
        parentUserIdReq.setParentUserId(positiveParentUserId);
        subUserAdminBusiness.enableSubUserFunction(request);
    }

    @Test
    public void testDisableSubUserFunction() throws Exception {
        APIRequest<ParentUserIdReq> request = new APIRequest<>();
        ParentUserIdReq parentUserIdReq = new ParentUserIdReq();
        parentUserIdReq.setParentUserId(positiveParentUserId);
        request.setBody(parentUserIdReq);
        APIResponse<Boolean> apiResponse = subUserAdminBusiness.disableSubUserFunction(request);
        assert apiResponse.getData();

        thrown.expectMessage(GeneralCode.SUB_UER_FUNCTION_NOT_ENABLED.getMessage());
        parentUserIdReq.setParentUserId(positiveUserId);
        subUserAdminBusiness.disableSubUserFunction(request);
    }

    @Test
    public void testBindParentSubUser() throws Exception {
        APIRequest<BindingParentSubUserReq> request = new APIRequest<>();
        BindingParentSubUserReq bindingParentSubUserReq = new BindingParentSubUserReq();
        bindingParentSubUserReq.setParentUserId(positiveParentUserId);
        bindingParentSubUserReq.setSubUserId(positiveUserId);
        request.setBody(bindingParentSubUserReq);
        APIResponse<Boolean> apiResponse = subUserAdminBusiness.bindParentSubUser(request);
        assert apiResponse.getData();

        thrown.expectMessage(GeneralCode.SYS_NOT_SUPPORT.getMessage());
        bindingParentSubUserReq.setParentUserId(negativeUserId);
        bindingParentSubUserReq.setSubUserId(negativeUserId);
        subUserAdminBusiness.bindParentSubUser(request);
    }
}
