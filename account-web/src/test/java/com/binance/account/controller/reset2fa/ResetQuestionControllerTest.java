package com.binance.account.controller.reset2fa;

import java.util.List;

import org.junit.Test;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.binance.account.controller.BaseTestMock;
import com.binance.account.vo.reset.request.ResetQuestionConfigArg;
import com.binance.account.vo.reset.response.ResetQuestionConfigBody;
import com.binance.master.models.APIRequest;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = Application.class)
//@WebAppConfiguration
public class ResetQuestionControllerTest extends BaseTestMock {

	@Test
	public void testManageQuestionConfig() {
		//ResetQuestionConfigArg arg;
		//String json = insert();
		//List<ResetQuestionConfigBody> obj = parse(json);
		//ResetQuestionConfigBody tmp = obj.get(0);
		//select();
		//delete(tmp.id);
	}

	private void delete(long id) {
		ResetQuestionConfigArg arg;
		arg = new ResetQuestionConfigArg();
		arg.setOpType("update");
		arg.setId(id);
		execute(arg);
	}

	private void select() {
		ResetQuestionConfigArg arg;
		arg = new ResetQuestionConfigArg();
		arg.setOpType("select");
		execute(arg);
	}

	private String insert() {
		ResetQuestionConfigArg arg = new ResetQuestionConfigArg();
		arg.setOpType("update");
		String json = execute(arg);
		return json;
	}

	public static List<ResetQuestionConfigBody> parse(String json) {
		JSONObject obj = JSON.parseObject(json);
		return obj.getObject("data", new TypeReference<List<ResetQuestionConfigBody>>() {
		});
	}

	private String execute(ResetQuestionConfigArg arg) {
		String uri = "/userReset2fa/manageQuestionConfig";
		String jsonString = JSON.toJSONString(APIRequest.instance(arg));
		System.out.println("parameter:" + jsonString);
		MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(uri)
				.header("content-type", "application/json; charset=utf-8").accept("application/json; charset=utf-8")
				.content(jsonString);
		return performAndAssertResponse(requestBuilder);
	}
}
