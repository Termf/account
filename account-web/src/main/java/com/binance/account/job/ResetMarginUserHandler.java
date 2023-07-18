package com.binance.account.job;

import com.binance.account.constants.enums.MatchBoxAccountTypeEnum;
import com.binance.account.data.entity.user.MarginFixCheckInfo;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.user.MarginFixCheckInfoMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.helper.SkipSyncMarginHelper;
import com.binance.account.integration.mbxgateway.MatchboxApiClient;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.user.IUser;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.security.request.CreateMarginAccountRequest;
import com.binance.account.vo.user.response.CreateMarginUserResponse;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**

 *
 */
@Log4j2
@JobHandler(value = "resetMarginUserHandler")
@Component
public class ResetMarginUserHandler extends IJobHandler {
	
	@Resource
    private UserInfoMapper userInfoMapper;
	
	@Resource
    private UserMapper userMapper;
	
	@Resource
    private UserIndexMapper userIndexMapper;
	
	@Resource
    private IMsgNotification iMsgNotification;

	@Autowired
	private MatchboxApiClient matchboxApiClient;


	@Autowired
	private MarginFixCheckInfoMapper marginFixCheckInfoMapper;

	@Resource
	private IUser iUser;

	@Resource
	private UserCommonBusiness userCommonBusiness;


	/**
	 * userIds:userId集合，用逗号分隔
	 */
	@Override
	public ReturnT<String> execute(String param) throws Exception {
		TrackingUtils.saveTraceId();
		StopWatch sw = new StopWatch();
        log.info("param={}", param);
        log.info("start ResetMarginUserHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            if(StringUtils.isBlank(param)) {
            	return SUCCESS;
    		}
			resetMarginList(param);
            return SUCCESS;
        } catch (Exception e) {
            log.error("ResetMarginUserHandler error-->{}", e);
            return FAIL;
        }finally {
            sw.stop();
            log.info("end ResetMarginUserHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
            
        }
	}
	
	//注册回写pnk
	public void resetMarginList(String userIds) {
		try {
			//打印有问题的account列表

			//2 resetmargin账号
			if(StringUtils.isBlank(userIds)){
				log.info("userIds is blank");
				return;
			}
			String[] userIdArr = userIds.split(",");
			List<String> userIdList = Arrays.asList(userIdArr);
			for(String fixMarginUserId:userIdList){
				try{
					log.info("resetMargin  userId={}",fixMarginUserId);
					MarginFixCheckInfo marginFixCheckInfo=marginFixCheckInfoMapper.selectByOldMarginUserId(Long.valueOf(fixMarginUserId));
					//如果日志表里面没有找到代表这是不合法的marginuserid不应该去修复他
					// 还有一种可能是已经修复过的数据不要再重复修了
					if(null== marginFixCheckInfo || null!= marginFixCheckInfo.getNewMarginUserId()){
						log.info("marginFixCheckInfoMapper fixMarginUserId={}  need not fix",fixMarginUserId);
						continue;
					}

					UserInfo rootUserInfo=userInfoMapper.selectByPrimaryKey(marginFixCheckInfo.getRootUserId());
					// 还有一种可能是已经修复过的数据不要再重复修了
					if(null== rootUserInfo ){
						log.info("marginFixCheckInfoMapper fixMarginUserId={}  rootUserInfo isnull ",fixMarginUserId);
						continue;
					}


					// 重制老的rootuserinfo中的信息主要有2步
					// 1 状态位重制
					User rootUser=userCommonBusiness.checkAndGetUserById(rootUserInfo.getUserId());
					rootUser.setStatus(BitUtils.disable(rootUser.getStatus(), Constant.USER_IS_EXIST_MARGIN_ACCOUNT));
					userMapper.updateByEmailSelective(rootUser);


					// 2 marginuserid重制为null
					rootUserInfo.setMarginUserId(null);
					userInfoMapper.resetMarginUserIdByRootUserId(rootUserInfo);


					// 3 开始重新创建margin账号
					APIRequest<CreateMarginAccountRequest> originRequest = new APIRequest<CreateMarginAccountRequest>();
					originRequest.setLanguage(LanguageEnum.ZH_CN);
					originRequest.setTerminal(TerminalEnum.WEB);
					originRequest.setTrackingChain(TrackingUtils.getTrackingChain());
					CreateMarginAccountRequest createMarginAccountRequest=new CreateMarginAccountRequest();
					createMarginAccountRequest.setUserId(rootUserInfo.getUserId());
					//非常重要的一点,只有标志为true才不会向margin同步
					SkipSyncMarginHelper.set(true);
					APIResponse<CreateMarginUserResponse> apiResponse=iUser.createMarginAccount(APIRequest.instance(originRequest, createMarginAccountRequest));
					log.info("resetMargin  apiResponse={}",JsonUtils.toJsonNotNullKey(apiResponse));


                    //4 老的margin账号userstatus重制回去，反正一个没有从属关系的margin账号在这里会造成很多问题
					User oldMarginUser=userCommonBusiness.checkAndGetUserById(marginFixCheckInfo.getOldMarginUserId());
					oldMarginUser.setStatus(BitUtils.disable(oldMarginUser.getStatus(), Constant.USER_IS_MARGIN_USER));
					oldMarginUser.setStatus(BitUtils.enable(oldMarginUser.getStatus(), Constant.USER_DELETE));
					userMapper.updateByEmailSelective(oldMarginUser);

					//5 更新日志表
					marginFixCheckInfo.setNewMarginUserId(apiResponse.getData().getMarginUserId());
					marginFixCheckInfo.setNewMarginAccountId(apiResponse.getData().getMarginTradingAccount());
					marginFixCheckInfoMapper.updateByPrimaryKeySelective(marginFixCheckInfo);

				}catch (Exception e){
					log.info("fixMarginUserId single error-->{}", e);
				}

			}

		}catch (Exception e) {
	         log.info("fixMarginUserId error-->{}", e);
		}
	}

}
