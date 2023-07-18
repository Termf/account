package com.binance.account.service.subuser.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.binance.account.data.entity.subuser.SubUserBinding;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.subuser.SubUserBindingMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.master.constant.Constant;
import com.binance.master.utils.BitUtils;
import com.google.common.collect.Lists;

/**
 * Created by Fei.Huang on 2018/11/14.
 */
public class SubUserUnitTest extends Mockito {

    // User相关正向、反向测试数据
    protected final static Long negativeUserId = 35000001L;
    protected final static String negativeUserEmail = "35000001@qq.com";
    protected final static Long positiveUserId = 35000002L;
    protected final static String positiveUserEmail = "35000002@qq.com";

    // ParentUser相关正向、反向测试数据
    protected final static Long negativeParentUserId = 35000003L;
    protected final static String negativeParentUserEmail = "35000003@qq.com";
    protected final static Long positiveParentUserId = 35000004L;
    protected final static String positiveParentUserEmail = "35000004@qq.com";

    // SubUser相关正向、反向测试数据
    protected final static Long negativeSubUserId = 35000005L;
    protected final static String negativeSubUserEmail = "35000005@qq.com";
    protected final static Long positiveSubUserId = 35000006L;
    protected final static String positiveSubUserEmail = "35000006@qq.com";
    protected final static Long disabledSubUserId = 35000007L;
    protected final static String disabledSubUserEmail = "35000007@qq.com";

    @InjectMocks
    protected CheckSubUserBusiness checkSubUserBusiness;
    @InjectMocks
    protected SubUserBusiness subUserBusiness;
    @InjectMocks
    protected SubUserAdminBusiness subUserAdminBusiness;

    @Mock
    protected UserMapper userMapper;
    @Mock
    protected UserIndexMapper userIndexMapper;
    @Mock
    protected UserInfoMapper userInfoMapper;
    @Mock
    protected SubUserBindingMapper subUserBindingMapper;
    @Mock
    protected IMsgNotification iMsgNotification;

    @Before
    public void before() throws Exception {
        mocknorMalUserRelated();
        mockParentUserRelated();
        mockSubUserRelated();
//        iMsgNotification.send(any());
    }

    /**
     * NormalUser相关
     */
    private void mocknorMalUserRelated() {

        // 正向mock
        UserIndex normalUserIndex = new UserIndex();
        normalUserIndex.setUserId(positiveUserId);
        normalUserIndex.setEmail(positiveUserEmail);
        lenient().when(userIndexMapper.selectByPrimaryKey(positiveUserId)).thenReturn(normalUserIndex);

        User normalUser = new User();
        normalUser.setUserId(positiveUserId);
        normalUser.setEmail(positiveUserEmail);
        normalUser.setStatus(BitUtils.enable(0L, Constant.USER_MOBILE));
        lenient().when(userMapper.queryByEmail(positiveUserEmail)).thenReturn(normalUser);

        lenient().when(userMapper.updateUserStatusByEmail(any())).thenReturn(1);
        lenient().when(userInfoMapper.updateByPrimaryKeySelective(any())).thenReturn(1);

        // 反向mock
        lenient().when(userIndexMapper.selectByPrimaryKey(negativeUserId)).thenReturn(null);
        lenient().when(userMapper.queryByEmail(negativeUserEmail)).thenReturn(null);
    }

    /**
     * ParentUser相关
     */
    private void mockParentUserRelated() {

        // 正向mock
        UserIndex parentUserIndex = new UserIndex();
        parentUserIndex.setUserId(positiveParentUserId);
        parentUserIndex.setEmail(positiveParentUserEmail);
        lenient().when(userIndexMapper.selectByPrimaryKey(positiveParentUserId)).thenReturn(parentUserIndex);

        User parentUser = new User();
        parentUser.setUserId(positiveParentUserId);
        parentUser.setEmail(positiveParentUserEmail);
        parentUser.setStatus(BitUtils.enable(0L, Constant.USER_IS_SUBUSER_FUNCTION_ENABLED));
        parentUser.setStatus(BitUtils.enable(parentUser.getStatus(), Constant.USER_MOBILE));
        lenient().when(userMapper.queryByEmail(positiveParentUserEmail)).thenReturn(parentUser);


        UserInfo parentUserInfo = new UserInfo();
        parentUserInfo.setUserId(positiveParentUserId);
        lenient().when(userInfoMapper.selectByPrimaryKey(positiveParentUserId)).thenReturn(parentUserInfo);

        // 反向mock
        lenient().when(userIndexMapper.selectByPrimaryKey(negativeParentUserId)).thenReturn(null);
        lenient().when(userMapper.queryByEmail(negativeParentUserEmail)).thenReturn(null);
    }

    /**
     * SubUser相关
     */
    private void mockSubUserRelated() {

        // 正向mock
        UserIndex subUserIndex = new UserIndex();
        subUserIndex.setUserId(positiveSubUserId);
        subUserIndex.setEmail(positiveSubUserEmail);
        lenient().when(userIndexMapper.selectByPrimaryKey(positiveSubUserId)).thenReturn(subUserIndex);

        User subUser = new User();
        subUser.setUserId(positiveSubUserId);
        subUser.setEmail(positiveSubUserEmail);
        subUser.setStatus(BitUtils.enable(0L, Constant.USER_IS_SUBUSER));
        subUser.setStatus(BitUtils.enable(subUser.getStatus(), Constant.USER_IS_SUB_USER_ENABLED));
        lenient().when(userMapper.queryByEmail(positiveSubUserEmail)).thenReturn(subUser);

        SubUserBinding subUserBinding = new SubUserBinding();
        subUserBinding.setParentUserId(positiveParentUserId);
        subUserBinding.setSubUserId(positiveSubUserId);
        lenient().when(subUserBindingMapper.selectBySubUserId(positiveSubUserId)).thenReturn(subUserBinding);

        SubUserBinding updateSubUserBinding = new SubUserBinding();
        updateSubUserBinding.setSubUserId(positiveSubUserId);
        lenient().when(subUserBindingMapper.updateBySubUserIdSelective(updateSubUserBinding)).thenReturn(1);

        List<SubUserBinding> subUserBindings = new ArrayList<>();
        subUserBindings.add(subUserBinding);
        lenient().when(subUserBindingMapper.getSubUserBindingsByParentUserId(positiveParentUserId))
                .thenReturn(subUserBindings);

        List<UserIndex> subUserIndexList = new ArrayList<>();
        subUserIndexList.add(subUserIndex);
        lenient().when(userIndexMapper.selectByUserIds(Lists.newArrayList(positiveSubUserId)))
                .thenReturn(subUserIndexList);

        lenient().when(subUserBindingMapper.insert(any())).thenReturn(1);

        // DisabledSubUser
        UserIndex disabledSubUserIndex = new UserIndex();
        disabledSubUserIndex.setUserId(disabledSubUserId);
        disabledSubUserIndex.setEmail(disabledSubUserEmail);
        lenient().when(userIndexMapper.selectByPrimaryKey(disabledSubUserId)).thenReturn(disabledSubUserIndex);

        User disabledSubUser = new User();
        disabledSubUser.setUserId(disabledSubUserId);
        disabledSubUser.setEmail(disabledSubUserEmail);
        disabledSubUser.setStatus(BitUtils.enable(0L, Constant.USER_IS_SUBUSER));
        disabledSubUser.setStatus(BitUtils.disable(disabledSubUser.getStatus(), Constant.USER_IS_SUB_USER_ENABLED));
        lenient().when(userMapper.queryByEmail(disabledSubUserEmail)).thenReturn(disabledSubUser);

        // 反向mock
        lenient().when(userIndexMapper.selectByPrimaryKey(negativeSubUserId)).thenReturn(null);
        lenient().when(userMapper.queryByEmail(negativeSubUserEmail)).thenReturn(null);
        lenient().when(subUserBindingMapper.selectBySubUserId(negativeSubUserId)).thenReturn(null);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();
}
