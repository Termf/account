package com.binance.account.service.security.service;

import com.alibaba.fastjson.JSON;
import com.binance.account.service.security.model.MultiFactorSceneCheckResult;
import com.binance.account.service.security.model.TwoVerifyBizScene;
import com.binance.account.service.security.model.UserTwoVerifyInfo;
import com.binance.account.service.security.service.strategy.CryptoWithdrawBizSceneBackupTwoVerifyStrategy;
import com.binance.account.service.security.utils.EnumUtils;
import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: mingming.sheng
 * @Date: 2020/5/19 2:00 下午
 */
@Log4j2
@Service
public class MultiFactorVerifyService {
    @Value("#{'${account.multifactors.bizscene.disablelist:}'.split(',')}")
    private List<String> disableBizSceneList;

    // 开启提现备用策略
    @Value("${account.multifactors.cryptowithdraw.backup.switch:false}")
    private boolean changeCryptoWithdrawToBackupStrategy;


    /**
     * 获取用户对应场景下的2fa策略
     */
    public MultiFactorSceneCheckResult get2FaVerifyList(UserTwoVerifyInfo verifyInfo) {
        if (CollectionUtils.isNotEmpty(disableBizSceneList) && disableBizSceneList.contains(verifyInfo.getBizScene().getCode())) {
            log.error("MultiFactorVerifyService.get2FaVerifyList,当前场景2fa禁用,verifyInfo={}", JSON.toJSONString(verifyInfo));
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
        }

        //通用校验
        if (!verifyInfo.getIsBindMobile() && !verifyInfo.getIsBindEmail()) {
            log.error("MultiFactorVerifyService.get2FaVerifyList,用户既不是手机注册用户也不是邮箱注册用户,verifyInfo={}",
                    JSON.toJSONString(verifyInfo));
            throw new BusinessException(GeneralCode.SYS_VALID);
        }


        TwoVerifyBizScene twoVerifyBizScene = EnumUtils.getByCode(TwoVerifyBizScene.class, verifyInfo.getBizScene().getCode());
        if (null == twoVerifyBizScene) {
            log.error("MultiFactorVerifyService.get2FaVerifyList,当前场景未配置对应2fa策略,verifyInfo={}", JSON.toJSONString(verifyInfo));
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }

        // 提现场景备用策略开关
        if (verifyInfo.getBizScene() == BizSceneEnum.CRYPTO_WITHDRAW && changeCryptoWithdrawToBackupStrategy) {
            return CryptoWithdrawBizSceneBackupTwoVerifyStrategy.get2FaVerifyList(verifyInfo);
        }

        MultiFactorSceneCheckResult verifyResult = twoVerifyBizScene.get2FaVerifyList(verifyInfo);
        log.info("新2fa策略请求,verifyInfo={},verifyResult={}", JSON.toJSONString(verifyInfo), JSON.toJSONString(verifyResult));
        return verifyResult;
    }
}
