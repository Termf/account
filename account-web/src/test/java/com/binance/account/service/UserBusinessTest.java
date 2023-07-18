package com.binance.account.service;

import com.alibaba.fastjson.JSON;
import com.binance.account.Application;
import com.binance.account.controller.user.UserInfoController;
import com.binance.account.data.entity.user.UserIp;
import com.binance.account.data.mapper.user.UserIpMapper;
import com.binance.account.service.user.impl.UserBusiness;
import com.binance.account.vo.user.UserIpLikeVo;
import com.binance.account.vo.user.request.SetUserConfigRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;


/**
 * @author liliang1
 * @date 2018-10-18 9:52
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class UserBusinessTest {

    @InjectMocks
    private UserBusiness userBusiness;
    @Mock
    private UserIpMapper userIpMapper;

    @Resource
    UserInfoController userInfoController;

    @Test
    public void testIpLikeCheck() {
        UserIp userIp = new UserIp(100001L, "192.168.10.136");
        UserIp userIp1 = new UserIp(100001L, "192.163.10.11");
        UserIp userIp2 = new UserIp(100001L, "192.168.11.17");
        List<UserIp> userIps = Arrays.asList(userIp, userIp1, userIp2);
        when(userIpMapper.getIpByUser(anyLong())).thenReturn(userIps);

        UserIpLikeVo vo = new UserIpLikeVo();
        //没有Userid 的情况
        APIResponse<UserIpLikeVo> result1 = userBusiness.ipLikeCheck(APIRequest.instance(vo));
        System.out.println(JSON.toJSONString(result1));

        UserIpLikeVo.IpLikeVo ipLikeVo = new UserIpLikeVo.IpLikeVo();
        ipLikeVo.setIp("192.168.10.253");
        UserIpLikeVo.IpLikeVo ipLikeVo1 = new UserIpLikeVo.IpLikeVo();
        ipLikeVo1.setIp("192.168.12.253");
        vo.setUserId(100001L);
        vo.setIpList(Arrays.asList(ipLikeVo, ipLikeVo1));

        APIResponse<UserIpLikeVo> result2 = userBusiness.ipLikeCheck(APIRequest.instance(vo));
        System.out.println(JSON.toJSONString(result2));
        List<UserIpLikeVo.IpLikeVo> ipLikeVos = result2.getData().getIpList();
        for (UserIpLikeVo.IpLikeVo temp : ipLikeVos) {
            if (StringUtils.equalsIgnoreCase("192.168.10.253", temp.getIp())) {
                Assert.isTrue(temp.getExist() > 0, "验证错误");
            }
            if (StringUtils.equalsIgnoreCase("192.168.12.253", temp.getIp())) {
                Assert.isTrue(temp.getExist() <= 0, "验证错误");
            }
        }
    }

    @Test
    public void testController() {
        SetUserConfigRequest  request = new SetUserConfigRequest();
        request.setUserId(350608596L);
        //request.setConfigType(UserPreferType.PREFER_LANG.getCode());
        request.setConfigName("en");
        try {
            APIResponse<Integer> result = userInfoController.saveUserConfig(APIRequest.instance(request));
            System.out.println(JSON.toJSONString(result));
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
