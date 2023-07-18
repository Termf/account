package com.binance.account.job;

import com.binance.account.constants.enums.MatchBoxAccountTypeEnum;
import com.binance.account.data.entity.user.MarginFixCheckInfo;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.user.MarginFixCheckInfoMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.integration.mbxgateway.MatchboxApiClient;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.user.IUser;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.user.ex.UserStatusEx;
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
import java.util.List;

/**

 *
 */
@Log4j2
@JobHandler(value = "storeMarginUserFixLogHandler")
@Component
public class StoreMarginUserFixLogHandler extends IJobHandler {
	
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
        log.info("start storeMarginUserFixLogHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            if(StringUtils.isBlank(param)) {
            	return SUCCESS;
    		}
			resetMarginList(param);
            return SUCCESS;
        } catch (Exception e) {
            log.error("storeMarginUserFixLogHandler error-->{}", e);
            return FAIL;
        }finally {
            sw.stop();
            log.info("end storeMarginUserFixLogHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
            
        }
	}
	
	//注册回写pnk
	public void resetMarginList(String userIds) {
		try {
			//打印有问题的account列表

			List<User> userList=userMapper.getNeedFixMarginUser();

			//1 落历史记录
			for(User user:userList){

				try{
					UserStatusEx userStatusEx=new UserStatusEx(user.getStatus());
					if(userStatusEx.getIsMarginUser()){
						String accountType=matchboxApiClient.getAccountTypeByUserId(user.getUserId());
						if(MatchBoxAccountTypeEnum.SPOT.getAccountType().equalsIgnoreCase(accountType)){
							//查询主账号并且把相关数据全部捞出来

							UserInfo rootUserInfo=userInfoMapper.selectRootUserInfoByMarginUserId(user.getUserId());
							UserInfo marginUserInfo=userInfoMapper.selectByPrimaryKey(user.getUserId());
							if(null ==rootUserInfo || null ==marginUserInfo || null ==rootUserInfo.getTradingAccount() || null ==marginUserInfo.getTradingAccount() ){
								log.info("userId={}  is null",user.getUserId());
								continue;
							}
							MarginFixCheckInfo marginFixCheckInfo=new MarginFixCheckInfo();
							marginFixCheckInfo.setRootUserId(rootUserInfo.getUserId());
							marginFixCheckInfo.setRootAccountId(rootUserInfo.getTradingAccount());
							marginFixCheckInfo.setOldMarginUserId(marginUserInfo.getUserId());
							marginFixCheckInfo.setOldMarginAccountId(marginUserInfo.getTradingAccount());
							MarginFixCheckInfo oldMarginFixCheckInfo=marginFixCheckInfoMapper.selectByPrimaryKey(rootUserInfo.getUserId());
							if(null!=oldMarginFixCheckInfo){
								log.info("storeMarginUserFixLogHandler already exist userId={}",rootUserInfo.getUserId());
								continue;
							}
							log.info("storeMarginUserFixLogHandler insert start  marginFixCheckInfo={}",JsonUtils.toJsonNotNullKey(marginFixCheckInfo));
							int result =marginFixCheckInfoMapper.insertSelective(marginFixCheckInfo);
							log.info("storeMarginUserFixLogHandler insert end  result={}",result);

						}
					}
				}catch (Exception e){
					log.error("storeMarginUserFixLogHandler single error-->{}", e);

				}

			}


		}catch (Exception e) {
	         log.info("storeMarginUserFixLogHandler error-->{}", e);
		}
	}

}
