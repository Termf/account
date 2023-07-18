package com.binance.account.service.subuser.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import com.binance.account.data.entity.user.User;
import com.binance.master.constant.Constant;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.BitUtils;

/**
 * Created by Fei.Huang on 2018/11/14.
 */
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class CheckSubUserBusinessTest extends SubUserUnitTest {

    @Test
    public void testCheckAndGetUserById() {
        assert null != checkSubUserBusiness.checkAndGetUserById(positiveUserId);

        try {
            checkSubUserBusiness.checkAndGetUserById(null);
        } catch (Exception e) {
            assert e.getMessage().contains(GeneralCode.SYS_NOT_SUPPORT.getMessage());
        }

        thrown.expectMessage(GeneralCode.USER_NOT_EXIST.getMessage());
        checkSubUserBusiness.checkAndGetUserById(negativeUserId);
    }

    @Test
    public void testAssertUser2FaAtLeastOneEnabled() {
        Long status = BitUtils.enable(0L, Constant.USER_MOBILE);
        checkSubUserBusiness.assertUser2FaAtLeastOneEnabled(status);

        thrown.expectMessage(GeneralCode.USER_GOOGLE_NOT_BIND.getMessage());
        status = BitUtils.disable(status, Constant.USER_MOBILE);
        status = BitUtils.disable(status, Constant.USER_GOOGLE);
        checkSubUserBusiness.assertUser2FaAtLeastOneEnabled(status);
    }

    @Test
    public void testAssertSubUserFunctionEnabled() {
        Long status = BitUtils.enable(0L, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED);
        checkSubUserBusiness.assertSubUserFunctionEnabled(status);

        thrown.expectMessage(GeneralCode.SUB_UER_FUNCTION_NOT_ENABLED.getMessage());
        status = BitUtils.disable(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED);
        checkSubUserBusiness.assertSubUserFunctionEnabled(status);
    }

    @Test
    public void testAssertSubUserFunctionDisabled() {
        Long status = BitUtils.disable(0L, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED);
        checkSubUserBusiness.assertSubUserFunctionDisabled(status);

        thrown.expectMessage(GeneralCode.SUB_UER_FUNCTION_ALREADY_ENABLED.getMessage());
        status = BitUtils.enable(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED);
        checkSubUserBusiness.assertSubUserFunctionDisabled(status);
    }

    @Test
    public void testIsSubUserFunctionEnabled() {
        Long status = BitUtils.disable(0L, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED);
        assert !checkSubUserBusiness.isSubUserFunctionEnabled(status);

        status = BitUtils.enable(status, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED);
        assert checkSubUserBusiness.isSubUserFunctionEnabled(status);
    }

    @Test
    public void testAssertIsSubUser() {
        Long status = BitUtils.disable(0L, Constant.USER_IS_SUBUSER);
        User user = new User();
        user.setStatus(status);
        try {
            checkSubUserBusiness.assertIsSubUser(user);
        } catch (Exception e) {
            assert e.getMessage().contains(GeneralCode.NOT_SUB_USER.getMessage());
        }

        user.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_IS_SUBUSER));
        user.setUserId(negativeSubUserId);
        try {
            checkSubUserBusiness.assertIsSubUser(user);
        } catch (Exception e) {
            assert e.getMessage().contains(GeneralCode.NOT_SUB_USER.getMessage());
        }

        user.setUserId(positiveSubUserId);
        assert null != checkSubUserBusiness.assertIsSubUser(user);
    }

    @Test
    public void testAssertIsEnabledSubUser() {
        User user = new User();
        Long status = 0L;
        status = BitUtils.enable(status, Constant.USER_IS_SUBUSER);
        status = BitUtils.enable(status, Constant.USER_IS_SUB_USER_ENABLED);
        user.setStatus(status);
        user.setUserId(positiveSubUserId);

        checkSubUserBusiness.assertIsEnabledSubUser(user);

        thrown.expectMessage(GeneralCode.SUB_USER_NOT_ENABLED.getMessage());
        user.setStatus(BitUtils.disable(user.getStatus(), Constant.USER_IS_SUB_USER_ENABLED));
        checkSubUserBusiness.assertIsEnabledSubUser(user);
    }

    @Test
    public void testIsEnabledSubUser() {
        User user = new User();
        Long status = 0L;
        status = BitUtils.enable(status, Constant.USER_IS_SUBUSER);
        status = BitUtils.enable(status, Constant.USER_IS_SUB_USER_ENABLED);
        user.setStatus(status);
        user.setUserId(positiveSubUserId);

        assert checkSubUserBusiness.isEnabledSubUser(user);

        user.setStatus(BitUtils.disable(user.getStatus(), Constant.USER_IS_SUB_USER_ENABLED));
        assert !checkSubUserBusiness.isEnabledSubUser(user);
    }

    @Test
    public void testAssertIsNotSubUser() {
        User user = new User();
        Long status = BitUtils.disable(0L, Constant.USER_IS_SUBUSER);
        user.setStatus(status);
        user.setUserId(negativeSubUserId);
        checkSubUserBusiness.assertIsNotSubUser(user);

        thrown.expectMessage(GeneralCode.SUB_USER_ALREADY_BOUND.getMessage());
        user.setStatus(BitUtils.enable(user.getStatus(), Constant.USER_IS_SUBUSER));
        user.setUserId(positiveSubUserId);
        checkSubUserBusiness.assertIsNotSubUser(user);
    }

    @Test
    public void testIsSubUser() {
        User user = new User();
        Long status = BitUtils.enable(0L, Constant.USER_IS_SUBUSER);
        user.setStatus(status);
        user.setUserId(positiveSubUserId);
        assert checkSubUserBusiness.isSubUser(user);

        user.setStatus(BitUtils.disable(user.getStatus(), Constant.USER_IS_SUBUSER));
        assert !checkSubUserBusiness.isSubUser(user);
    }

    @Test
    public void testAssertParentSubUserBound() {
        checkSubUserBusiness.assertParentSubUserBound(positiveParentUserId, positiveSubUserId);
    }

    @Test
    public void testAssertParentSubUserBoundNotCheckParent() {
        assert null != checkSubUserBusiness.assertParentSubUserBoundNotCheckParent(positiveParentUserId, positiveSubUserId);

        thrown.expectMessage(GeneralCode.TWO_USER_ID_NOT_BOUND.getMessage());
        checkSubUserBusiness.assertParentSubUserBoundNotCheckParent(negativeParentUserId, positiveSubUserId);
    }

    @Test
    public void testAssertParentSubUserUnbound() {
        thrown.expectMessage(GeneralCode.SUB_USER_ALREADY_BOUND.getMessage());
        checkSubUserBusiness.assertParentSubUserUnbound(positiveParentUserId, positiveSubUserId);
    }
}
