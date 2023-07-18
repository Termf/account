package com.binance.account.service.other.impl;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.other.IOther;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.other.SendDisableTokenEmailRequest;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.StringUtils;

@Service
public class OtherImpl implements IOther {

    @Autowired
    private UserCommonBusiness userCommonBusiness;
    
    @Resource
    private UserIndexMapper userIndexMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Override
    public APIResponse<String> sendDisableTokenEmail(APIRequest<SendDisableTokenEmailRequest> request) {
        final SendDisableTokenEmailRequest requestBody = request.getBody();
        final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
        if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
        }
        final User user = this.userMapper.queryByEmail(userIndex.getEmail());
        String disableToken = this.userCommonBusiness.sendDisableTokenEmail(requestBody.getTplCode(), user, requestBody.getData(), requestBody.getRemark(), requestBody.getCustomForbiddenLink());
        return APIResponse.getOKJsonResult(disableToken);
    }

}
