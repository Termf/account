package com.binance.account.service.apimanage.impl;

import com.binance.account.data.entity.apimanage.OperateLogModel;
import com.binance.account.data.mapper.apimanage.OperateLogModelMapper;
import com.binance.account.service.apimanage.IOperateLogService;
import com.binance.master.utils.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class OperateLogServiceImpl extends BaseServiceImpl implements IOperateLogService {

    @Resource
    private OperateLogModelMapper operateLogModelMapper;

	@Override
	public void insert(String userId,String type,String result,String operation,String info) {
		try {
		   OperateLogModel model = new OperateLogModel();
           model.setId(UUID.randomUUID().toString().replace("-", ""));
           model.setUserId(userId);
           model.setOperateTime(new Date());
           model.setIpAddress(WebUtils.getRequestIp());
           model.setOperateType(operation);
           model.setOperateModel(type);
           model.setOperateResult(result);
           model.setResInfo(info);		
           this.operateLogModelMapper.insert(model);
        } catch (Exception e) {
            log.error("创建API后记录操作日志出现异常", e);
        }
	}

}
