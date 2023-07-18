package com.binance.account.job;

import javax.annotation.Resource;

import org.junit.Test;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.NONE)
public class SecurityResetNoChangeJobHandlerTest {

	@Resource
	private SecurityResetNoChangeJobHandler handler;
	
	@Test
	public void testExecuteString() {
		try {
			handler.execute("200");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
