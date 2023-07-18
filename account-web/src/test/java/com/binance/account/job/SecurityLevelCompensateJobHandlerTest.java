package com.binance.account.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import com.binance.account.Application;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.master.old.data.account.OldUserDataMapper;
import com.binance.master.old.models.account.OldUserData;
import com.xxl.job.core.biz.model.ReturnT;

/**
 * @author lufei
 * @date 2018/10/24
 */
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = Application.class)
public class SecurityLevelCompensateJobHandlerTest {

    @InjectMocks
    private SecurityLevelCompensateJobHandler jobHandler;

    @Mock
    private IMsgNotification iMsgNotification;

    @Mock
    private UserSecurityMapper userSecurityMapper;

    @Mock
    private OldUserDataMapper oldUserDataMapper;

    @Before
    public void before() {
        UserSecurity us1 = new UserSecurity();
        us1.setUserId(1L);
        us1.setSecurityLevel(1);

        UserSecurity us2 = new UserSecurity();
        us2.setUserId(2L);
        us2.setSecurityLevel(2);

        OldUserData pnk1 = new OldUserData();
        pnk1.setUserId("1");
        pnk1.setSecurityLevel(1);

        OldUserData pnk2 = new OldUserData();
        pnk2.setUserId("2");
        pnk2.setSecurityLevel(1);

        Map<String, Object> message1 = new HashMap<>();
        message1.put("userId", us1.getUserId());
        message1.put("level", us1.getSecurityLevel());

        Map<String, Object> message2 = new HashMap<>();
        message2.put("userId", us1.getUserId());
        message2.put("level", us1.getSecurityLevel());

        when(userSecurityMapper.selectRecentUpdateUserId(any()))
                .thenReturn(Stream.of(1L, 2L).collect(Collectors.toList()));

        when(userSecurityMapper.selectByPrimaryKey(1L)).thenReturn(us1);
        when(userSecurityMapper.selectByPrimaryKey(2L)).thenReturn(us2);

        when(oldUserDataMapper.selectByUserIds(Stream.of("1", "2").collect(Collectors.toList())))
                .thenReturn(Stream.of(pnk1, pnk2).collect(Collectors.toList()));
    }


    @Test
    public void testExecute() throws Exception {
        ReturnT<String> returnT = jobHandler.execute(null);
//        Mockito.verify(iMsgNotification, Mockito.times(1)).send(any());
        Assert.assertEquals(returnT.getCode(), returnT.SUCCESS_CODE);
    }

}
