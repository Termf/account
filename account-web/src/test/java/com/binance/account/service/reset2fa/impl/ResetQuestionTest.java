package com.binance.account.service.reset2fa.impl;

import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;

import com.binance.account.common.enums.UserRiskStatus;
import com.binance.account.data.entity.security.UserQuestionOptions;
import com.binance.account.data.entity.user.UserRiskFeature;
import com.binance.account.data.mapper.security.UserQuestionAnswerMapper;
import com.binance.account.data.mapper.security.UserQuestionOptionsMapper;
import com.binance.account.data.mapper.user.UserRiskFeatureMapper;
import com.binance.account.service.question.checker.QuestionModuleChecker;
import com.binance.master.utils.DateUtils;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = Application.class,webEnvironment=WebEnvironment.NONE)
public class ResetQuestionTest {

	
	@Resource
	UserQuestionOptionsMapper api;
	@Resource
	UserRiskFeatureMapper mapper;
	@Resource
	UserQuestionAnswerMapper qu;
	@Resource
	QuestionModuleChecker checker;
	
	@Test
	public void testPostProcessUserByRiskFeatrue() {
		checker.postProcessUserByRiskFeatrue(5, 2);
		assertTrue(true);
	}
	
	@Test
	public void testUserQuestionAnswersTest() {
//		UserQuestionAnswers tmp = qu.selectByPrimaryKey(342951856025157632L,350458720L);
//		tmp.setPass(true);
//		UserQuestionAnswers ss=new UserQuestionAnswers();
//		ss.setId(tmp.getId());
//		ss.setUserId(tmp.getUserId());
//		ss.setPass(true);
//		qu.updateSelective(tmp);
	}

	@Test
	public void testGetUserReset() {
		UserQuestionOptions opt=new UserQuestionOptions();
		opt.setId(1L);
		opt.setOptions("BTC");
		opt.setUserId(967L);
		opt.setRiskType("abc");
		opt.setCreateTime(DateUtils.getNewDate());
		api.insert(opt);
		
		UserQuestionOptions t = api.selectByPrimaryKey(967L, "abc");
		assertTrue(opt.getUserId().equals(t.getUserId()));
		
		api.deleteByPrimaryKey(t.getId());
	}
	
	@Test
	public void testUserRiskFeatureMapper() {
		UserRiskFeature featur = new UserRiskFeature();
		featur.setCreateTime(DateUtils.getNewUTCDate());
		featur.setFeatures("");
		featur.setFlowId("aboc");
		featur.setIp("127.0.0.1");
		featur.setRiskResult(Boolean.FALSE);
		featur.setStatus(UserRiskStatus.UNDO);
		featur.setUpdateTime(DateUtils.getNewUTCDate());
		featur.setUserId(12L);
		mapper.insert(featur);

		mapper.updateStatusFromTo(0, UserRiskStatus.UNDO.ordinal(), UserRiskStatus.DOING.ordinal(), 9527);
		
		UserRiskFeature newOne = new UserRiskFeature();
		newOne.setFeatures("abccss");
		newOne.setRiskResult(Boolean.TRUE);
		newOne.setId(featur.getId());
		mapper.updateSelectiveInDoing(newOne);

		List<UserRiskFeature> lst = mapper.getBeforeMinutes(0, UserRiskStatus.DOING.ordinal(), 9527);
		assertTrue(!CollectionUtils.isEmpty(lst));
		
		boolean has = false;
		for (UserRiskFeature userRiskFeature : lst) {
			mapper.delete(userRiskFeature.getId());
			if (userRiskFeature.getFeatures().equals("abccss")
					&& Boolean.TRUE.equals(userRiskFeature.getRiskResult())) {
				has = true;
			}
			System.out.println(userRiskFeature);
		}
		assertTrue(has);
	}
}
