package com.binance.account.controller;

import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.binance.master.utils.IPUtils;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseTestMock {
    
    static {
        System.setProperty("local_ip", IPUtils.getIp());
    }
    
    protected MockMvc mvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Before
    public void setUp() throws JsonProcessingException {
        String path = webApplicationContext.getServletContext().getContextPath();
        System.out.println("ContextPath   -------------> " + path);
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    
    
    protected String performAndAssertResponse(MockHttpServletRequestBuilder requestBuilder) {
        String returnVal = null;
        try {
            MvcResult result = mvc.perform(requestBuilder).andDo(MockMvcResultHandlers.print()).andReturn();
            Assert.assertTrue("Status > 300",result.getResponse().getStatus() < 300);
            returnVal = result.getResponse().getContentAsString();
        } catch (Exception e) {
            Assert.assertTrue(e.toString(), false);
        }
        log.info("response:{}", returnVal);
        return returnVal;
    }
}