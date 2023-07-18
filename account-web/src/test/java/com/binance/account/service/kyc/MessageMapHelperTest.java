package com.binance.account.service.kyc;

import com.binance.account.data.entity.certificate.MessageMap;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.utils.MessageUtils;
import com.binance.account.vo.other.MessageMapVo;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

public class MessageMapHelperTest extends BaseTest {

	@Resource
	private MessageUtils messageUtils;

	@Test
	public void testSaveMessageMap() {
		List<MessageMapVo> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			MessageMap mapcn = new MessageMap("JUNIT_TEST_"+i, "cn", "单元测试");
			MessageMap mapen = new MessageMap("JUNIT_TEST_"+i, "en", "JUNIT TEST!!!!!");
			
			MessageMapVo v1 = new MessageMapVo();
			MessageMapVo v2 = new MessageMapVo();
			BeanUtils.copyProperties(mapcn, v1);
			BeanUtils.copyProperties(mapen, v2);
			list.add(v1);
			list.add(v2);
		}
		MessageMapHelper.saveMessageMap(list);
		list = new ArrayList<MessageMapVo>();
		for (int i = 0; i < 10; i++) {
			MessageMap mapcn;
			MessageMap mapen;
			if(i%2 ==0) {
				mapcn = new MessageMap("JUNIT_TEST_"+(i+100), "cn", "单元测试");
				mapen = new MessageMap("JUNIT_TEST_"+(i+100), "en", "JUNIT TEST!!!!!");
			}else {
				mapcn = new MessageMap("JUNIT_TEST_"+i, "cn", "单元测试 update");
				mapen = new MessageMap("JUNIT_TEST_"+i, "en", "JUNIT TEST update!!!!!");
			}
			
			MessageMapVo v1 = new MessageMapVo();
			MessageMapVo v2 = new MessageMapVo();
			BeanUtils.copyProperties(mapcn, v1);
			BeanUtils.copyProperties(mapen, v2);
			list.add(v1);
			list.add(v2);
		}

		MessageMapHelper.saveMessageMap(list);
	}
	
	@Test
	public void testFuzzyGetByCode() {
		List<MessageMapVo> list = MessageMapHelper.fuzzyGetByCode("JUNIT");
		System.out.println(list.size());
	}

	@Test
	public void testMessage() {
		messageUtils.getMessage(AccountErrorCode.WITHDRAW_FACE_NEED_KYC, null);
	}

}
