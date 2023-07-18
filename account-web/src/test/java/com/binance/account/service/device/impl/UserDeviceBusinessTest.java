
package com.binance.account.service.device.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.data.entity.device.UserDevice;
import com.binance.account.data.entity.device.UserDeviceProperty;
import com.binance.account.data.mapper.device.UserDeviceMapper;
import com.binance.account.data.mapper.device.UserDevicePropertyMapper;
import com.binance.account.data.mapper.device.UserDeviceRelationMapper;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.device.response.AddUserDeviceResponse;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
* Created by Shining.Cai on 11/05/2018.
*/ 
@SuppressWarnings("unchecked")
@RunWith(PowerMockRunner.class)
@PrepareForTest(RedisCacheUtils.class)
@SpringBootTest
@PowerMockIgnore({"javax.script.*", "javax.management.*"})
public class UserDeviceBusinessTest extends Mockito{

    @InjectMocks
    private UserDeviceBusiness targetService;
    @Mock
    private UserDeviceSearchService searchService;
    @Mock
    private UserDeviceRelationMapper relationMapper;
    @Mock
    private UserCommonBusiness userCommonBusiness;
    @Mock
    private UserDeviceMapper userDeviceMapper;
    @Mock
    private UserDevicePropertyMapper userDevicePropertyMapper;

    Map content = JSON.parseObject("{\"content_lang\":\"am\",\"device_id\":\"1540892676906fGVciWOt67KQ70KPYJ9\",\"system_lang\":\"zu_ZA\",\"timezone\":\"America/Panama\",\"content_encoding\":\"gzip, deflate, br\",\"screen_resolution\":\"1920,1080\",\"list_plugin\":\"Chrome PDF Plugin::Portable Document Format::application/x-google-chrome-pdf~pdf,Chrome PDF Viewer::::application/pdf~pdf,Native Client::::application/x-nacl~,application/x-pnacl~,Widevine Content Decryption Module::Enables Widevine licenses for playback of HTML audio/video content. (version: 1.4.9.1076)::application/x-ppapi-widevine-cdm~\",\"login_ip\":\"192.168.95.1\",\"canvas_code\":\"b11ad5f6\",\"accept\":\"*/*\",\"webgl_vendor\":\"Google Inc.\",\"device_name\":\"Mozilla/5.0 (compatible; MSIE 6.0; Windows NT 5.01; Trident/4.1)\",\"system_version\":\"Macintosh; U; Intel Mac OS X 10_9_1\",\"webgl_renderer\":\"ANGLE (Intel(R) HD Graphics 630 Direct3D11 vs_5_0 ps_5_0)\",\"brand_model\":\"unknown\",\"user_agent\":\"Mozilla/5.0 (Windows NT 5.2) AppleWebKit/5361 (KHTML, like Gecko) Chrome/46.0.844.0 Safari/5361\"}");
    Map<String, Integer> weight = new HashMap<>();

    
    @Before
    public void before() throws Exception {
        UserDeviceBusiness mocked = mock(UserDeviceBusiness.class);
        List<UserDevice> list = new ArrayList<>();
        List<UserDeviceProperty> properties = new ArrayList<>();
        UserDevice device = new UserDevice();
        device.setUserId(999L);
        device.setId(1L);
        list.add(device);
        when(searchService.searchDeviceByDeviceId(anyString())).thenReturn(list);
        when(relationMapper.insertIgnoreSelective(any())).thenReturn(0);
        when(userCommonBusiness.getEmailById(anyLong())).thenReturn("qwer@123.c");
        Arrays.asList("property_key","accept","brand_model","canvas_code","content_encoding","content_lang","device_id","device_name","fingerprint","http_hearders","list_plugin","login_ip","screen_resolution","system_lang","system_version","timezone","user_agent","webgl_renderer","webgl_vendor")
                .forEach(e -> {
                    UserDeviceProperty property = new UserDeviceProperty();
                    property.setPropertyWeight(1);
                    property.setAgentType("web");
                    property.setPropertyKey(e);
                    property.setStatus((byte) 1);
                    properties.add(property);
                });
        when(mocked.getDevicePropertyConfig("web", UserDeviceProperty.STATUS.OPEN.getCode())).thenReturn(properties);
        when(userDeviceMapper.selectByUserIdAndAgentType(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(new ArrayList<>());
        when(userDevicePropertyMapper.selectByTypeAndStatus(any(), anyByte())).thenReturn(properties);
        PowerMockito.mockStatic(RedisCacheUtils.class);
        BDDMockito.when(RedisCacheUtils.get("CACHE_ACCOUNT_DEVICE_PROPERTY", List.class)).thenReturn(properties);
    }

    @After
    public void after() throws Exception { 
    }


    /** 
    * 
    * Method: updateRelatedDevice(Long userId, Long devicePk, String relatedDeviceIds) 
    * 
    */ 
    @Test
    public void testUpdateRelatedDevice() throws Exception {
        targetService.updateRelatedDevice(2L, 123L, "123,1528891613391RXsd24OMcKqLPX20IQ7");
    }

    @Test
    public void testAddDeviceForWithdraw() throws Exception {

        AddUserDeviceResponse response = targetService.addDeviceHistoryForWithdraw(1L, "web", content);
        assert StringUtils.length(response.getDeviceId())==32;
    }

} 
