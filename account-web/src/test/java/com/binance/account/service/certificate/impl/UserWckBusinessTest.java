
package com.binance.account.service.certificate.impl;

import com.binance.account.data.entity.certificate.UserWckAudit;
import com.binance.account.data.mapper.certificate.UserWckAuditLogMapper;
import com.binance.account.data.mapper.certificate.UserWckAuditMapper;
import com.binance.account.vo.certificate.request.WckAuditRequest;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After; 
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** 
* Created by Shining.Cai on 10/18/2018.
*/ 
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class UserWckBusinessTest extends Mockito{ 

    @InjectMocks
    private UserWckBusiness targetService;
    @Mock
    private UserWckAuditMapper wckAuditMapper;
    @Mock
    private UserWckAuditLogMapper wckAuditLogMapper;
    @Mock
    private UserKycBusiness kycBusiness;



    
    @Before
    public void before() throws Exception {
        when(wckAuditMapper.selectByPrimaryKey(anyLong())).thenReturn(mock(UserWckAudit.class));
    } 

    @After
    public void after() throws Exception { 
    } 

    /** 
    * 
    * Method: isSwitchOn() 
    * 
    */ 
    @Test
    public void testIsSwitchOn() throws Exception { 
    
    } 

    /** 
    * 
    * Method: getWckResultProfile(Long kycId) 
    * 
    */ 
    @Test
    public void testGetWckResultProfile() throws Exception { 
    
    } 

    /** 
    * 
    * Method: getWcAuditEvents(Long kycId) 
    * 
    */ 
    @Test
    public void testGetWcAuditEvents() throws Exception { 
    
    } 

    /** 
    * 
    * Method: applyWorldCheck(Jumio jumio, UserKyc kyc) 
    * 
    */ 
    @Test
    public void testApplyWorldCheck() throws Exception { 
    
    } 

    /** 
    * 
    * Method: applyOrResetWorldCheck(Jumio jumio, UserKyc kyc) 
    * 
    */ 
    @Test
    public void testApplyOrResetWorldCheck() throws Exception { 
    
    } 

    /** 
    * 
    * Method: listForAdmin(UserWckQuery query) 
    * 
    */ 
    @Test
    public void testListForAdmin() throws Exception { 
    
    } 

    /** 
    * 
    * Method: audit(WckAuditRequest request) 
    * 
    */ 
    @Test
    public void testAudit() throws Exception {
        WckAuditRequest request = spy(WckAuditRequest.class);
        request.setAuditorId(1L);
        request.setAuditorSeq(1);
        request.setIsValid(false);
        request.setKycId(1L);
        request.setForceFinal(true);
        targetService.audit(request);
    }

    /** 
    * 
    * Method: getDomainFlag() 
    * 
    */ 
    @Test
    public void testGetDomainFlag() throws Exception { 
    
    } 


} 
