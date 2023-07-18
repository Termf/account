package com.binance.account.service.withdraw.impl;

import com.binance.account.data.entity.withdraw.UserWithdrawLockLog;
import com.binance.account.data.entity.withdraw.UserWithdrawProperty;
import com.binance.account.data.mapper.withdraw.UserWithdrawLockLogMapper;
import com.binance.account.data.mapper.withdraw.UserWithdrawPropertyMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.withdraw.IUserWithdrawPropertyBusiness;
import com.binance.account.vo.withdraw.request.UserWithdrawLockLogRequest;
import com.binance.account.vo.withdraw.request.UserWithdrawLockRequest;
import com.binance.account.vo.withdraw.response.UserWithdrawLockAmountResponse;
import com.binance.account.vo.withdraw.response.UserWithdrawLockLogResponse;
import com.binance.account.vo.withdraw.response.UserWithdrawLockResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.CacheKeys;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.RedisCacheUtils;
import io.shardingsphere.api.HintManager;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.locks.Lock;

@Log4j2
@Service
public class UserWithdrawPropertyBusiness implements IUserWithdrawPropertyBusiness {
	
	@Autowired
	private UserWithdrawLockLogMapper userWithdrawLockLogMapper;
	@Autowired
	private UserWithdrawPropertyMapper userWithdrawPropertyMapper;

	@Override
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public UserWithdrawLockResponse lock(UserWithdrawLockRequest request) throws Exception {
		log.info("start to lock user withdraw:"+request);
		if (!"0".equals(request.getType())) {
			throw new BusinessException("parameter TYPE is incorrect");	
		}
		Long userId = request.getUserId();
		UserWithdrawLockLog lockLogModel = userWithdrawLockLogMapper.selectByUniqueKey(request.getTranId(), request.getType(), userId);
		if (lockLogModel != null) {
			return new UserWithdrawLockResponse();	
		}
		log.info("lock user withdraw get lock successfully,userId:"+userId);
		try{
			UserWithdrawLockLog log = new UserWithdrawLockLog(null, userId, request.getTranId(), request.getType(), request.getIsManual(), request.getAmount(), new Date(), request.getOperator());
			int num = userWithdrawLockLogMapper.insertSelective(log);
			if(num == 1){
				int line = updateOrInsertUserWithdrawProperty(request);
				if(line == 1){
					return new UserWithdrawLockResponse();
				}else {
					throw new BusinessException("lock user withdraw update or insert property failed,userId:"+userId);
				}
			}else {
				throw new BusinessException("lock user withdraw insert log failed,userId:"+userId);
			}
		}catch (DuplicateKeyException e){
			log.warn("lock DuplicateKeyException", e);
			return new UserWithdrawLockResponse();
		}

	}

	@Override
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public UserWithdrawLockResponse unlock(UserWithdrawLockRequest request) throws Exception {
		log.info("start to unlock user withdraw:"+request);
		if (!"1".equals(request.getType())) {
			throw new BusinessException("parameter TYPE is incorrect");
		}
		Long userId = request.getUserId();
		UserWithdrawLockLog lockLogModel = userWithdrawLockLogMapper.selectByUniqueKey(request.getTranId(), request.getType(), userId);
		if (lockLogModel != null) {
			return new UserWithdrawLockResponse();
		}
		HintManager hintManager = null;
		try {
			log.info("unlock user withdraw get lock successfully,userId:"+userId);
			Long tranId = request.getTranId();
			String type = "0";
			hintManager = HintManager.getInstance();
			hintManager.setMasterRouteOnly();
			UserWithdrawLockLog lockModel = userWithdrawLockLogMapper.selectByUniqueKey(tranId, type,userId);
			if(lockModel != null){
				request.setAmount(lockModel.getAmount().negate());
				//前置校验
				UserWithdrawProperty userWithdrawProperty=userWithdrawPropertyMapper.selectByPrimaryKey(userId);
				if(null!=userWithdrawProperty){
					if("1".equals(request.getIsManual())&& userWithdrawProperty.getWithdrawLockManual().compareTo(request.getAmount().abs())<0){
						log.warn("unlock amount insufficient,userId:"+userId);
						throw new BusinessException("unlock amount insufficient,userId:"+userId);
						//return new UserWithdrawLockResponse();
					}else if("0".equals(request.getIsManual())&& userWithdrawProperty.getWithdrawLock().compareTo(request.getAmount().abs())<0){
						log.warn("unlock amount insufficient,userId:"+userId);
						throw new BusinessException("unlock amount insufficient,userId:"+userId);
					}
				}
				//插入
				UserWithdrawLockLog lockLog = new UserWithdrawLockLog(null, userId, request.getTranId(), request.getType(), request.getIsManual(), request.getAmount(), new Date(), request.getOperator());
				int num = userWithdrawLockLogMapper.insertSelective(lockLog);
				if(num == 1){
					int line = updateOrInsertUserWithdrawProperty(request);
					if(line == 1){
						return new UserWithdrawLockResponse();
					}else {
						throw new BusinessException("unlock user withdraw update or insert property failed,userId:"+userId);
					}
				}else{
					log.info("unlock user withdraw insert log failed,userId:"+userId);
					throw new BusinessException("unlock user withdraw insert log failed,userId:"+userId);
					//return new UserWithdrawLockResponse();
				}

			}else{
				log.warn("UserWithdrawLockLog is null,userId:"+userId);
				throw new BusinessException(GeneralCode.SYS_NOT_EXIST);
				//return new UserWithdrawLockResponse();
			}
		}catch (DuplicateKeyException e){
			log.warn("unlock DuplicateKeyException", e);
			return new UserWithdrawLockResponse();
		}finally {
			if (null != hintManager) {
				hintManager.close();
			}
		}



	}
	
	private int updateOrInsertUserWithdrawProperty(UserWithdrawLockRequest request){
		
		UserWithdrawProperty model = new UserWithdrawProperty();
		model.setUserId(request.getUserId());
		if("0".equals(request.getIsManual())){//auto
			model.setWithdrawLock(request.getAmount());
		}else if("1".equals(request.getIsManual())){//manual
			model.setWithdrawLockManual(request.getAmount());
		}
		model.setUpdateTime(new Date());
		int num = userWithdrawPropertyMapper.updateWithdrawLock(model);
		if(num == 1){
			return num;
		}else{
			model.setInsertTime(new Date());
			num = userWithdrawPropertyMapper.insertSelective(model);
			return num;
		}
		
		
	}

	@Override
	public UserWithdrawLockAmountResponse getLockAmount(Long userId) {
		UserWithdrawProperty property = userWithdrawPropertyMapper.selectByPrimaryKey(userId);
		if(property == null){
			return new UserWithdrawLockAmountResponse(userId,BigDecimal.ZERO,BigDecimal.ZERO, BigDecimal.ZERO);
		}
		// 获取红包锁定金额
		BigDecimal packetAmt = userWithdrawLockLogMapper.sumLockWithOperator(userId, "promo");
		BigDecimal total = property.getWithdrawLock().add(property.getWithdrawLockManual());
		return new UserWithdrawLockAmountResponse(userId,total,property.getWithdrawLock(), property.getWithdrawLockManual(), packetAmt);
	}

    @Override
    public UserWithdrawLockLogResponse queryLockLog(UserWithdrawLockLogRequest request) {
		UserWithdrawLockLog lockLogModel = userWithdrawLockLogMapper.selectByUniqueKey(request.getTranId(), request.getType(), request.getUserId());
        if (lockLogModel != null) {
			UserWithdrawLockLogResponse logResponse = new UserWithdrawLockLogResponse();
			BeanUtils.copyProperties(lockLogModel, logResponse);
			return logResponse;
		}
		return null;
    }


}
