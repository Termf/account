package com.binance.account.service.country.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.common.query.SearchResult;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.entity.country.CountryBlacklist;
import com.binance.account.data.entity.country.UserCountryWhitelist;
import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.country.CountryBlacklistMapper;
import com.binance.account.data.mapper.country.UserCountryWhitelistMapper;
import com.binance.account.data.mapper.security.UserSecurityLogMapper;
import com.binance.account.vo.country.CountryBlacklistVo;
import com.binance.account.vo.country.UserCountryWhitelistQuery;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.IP2LocationUtils;
import com.binance.master.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ip2location.IPResult;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.javasimon.aop.Monitored;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Shining.Cai on 2018/10/29.
 **/
@Log4j2
@Service
public class CountryBlacklistBusiness {

    @Autowired
    private CountryBlacklistMapper countryBlacklistMapper;
    @Autowired
    private UserSecurityLogMapper userSecurityLogMapper;
    @Autowired
    private UserKycApproveMapper userKycApproveMapper;
    @Autowired
    private UserCountryWhitelistMapper countryWhitelistMapper;

    @Value("${us.not.supported.states:}")
    private String usNotSupportedStates;

    @Monitored
    public boolean isBlack(String countryCode){
        CountryBlacklist blacklist = countryBlacklistMapper.selectByPrimaryKey(countryCode.toUpperCase());
        return blacklist != null && blacklist.getIsActive();
    }

    /**
     * 判断用户是否在黑名单
     * @param userId 用户id
     * @return boolean pair: left-是否黑名单 right-是否做过kyc
     */
    @Monitored
    public Pair<Boolean, Boolean> isBlack(Long userId){
        boolean isBlack = false;
        try {
            // 1.优先判断白名单
            if (this.isInWhiteList(userId)){
                return Pair.of(false, null);
            }
            // 2.其次判定KYC国家(kyc 通过)
            UserKycApprove kycApprove = userKycApproveMapper.selectByPrimaryKey(userId);
            if (kycApprove != null && kycApprove.getCertificateType() != null){
                String countryCode = null;
                if (KycCertificateResult.TYPE_USER == kycApprove.getCertificateType()) {
                    // 个人认证，去证件识别的国籍
                    countryCode = kycApprove.getCertificateCountry();
                }else {
                    // 企业认证国籍按用户输入的国籍算
                    countryCode = kycApprove.getBaseInfo() != null ? kycApprove.getBaseInfo().getCountry() : null;
                }
                if (StringUtils.isNotBlank(countryCode) && countryCode.length() == 2) {
                    return Pair.of(this.isBlack(countryCode), true);
                }else {
                    log.warn("get user-kyc country fail. {} {}", userId, countryCode);
                }
            }
            // 3.最后判定IP国家
            UserSecurityLog log = userSecurityLogMapper.getLastLoginLogByUserId(userId);
            if (log != null && StringUtils.isNotEmpty(log.getIp())){
                isBlack = this.isBlackIp(log.getIp());
            }
        } catch (Exception e) {
            log.error("CountryBlacklistBusiness.isBlack error, userId: {}", userId, e);
        }
        return Pair.of(isBlack, false);
    }

    @Monitored
    public boolean isBlackIp(String ip) {
        String code = IP2LocationUtils.getCountryShort(ip);
        log.info("isBlackIp result: {}->{}->{}", ip, code, (StringUtils.isNotBlank(code) && isBlack(code)));
        return StringUtils.isNotBlank(code) && isBlack(code);
    }

    public List<CountryBlacklistVo> listAll(){
        List<Map> rawList = countryBlacklistMapper.selectAll();
        if (rawList==null || rawList.isEmpty()){
            return Collections.emptyList();
        }else {
            return JSON.parseArray(JSON.toJSONString(rawList), CountryBlacklistVo.class);
        }
    }

    public void add(CountryBlacklist blacklist){
        if (countryBlacklistMapper.selectByPrimaryKey(blacklist.getCountryCode())!=null){
            throw new BusinessException("已在国家黑名单中");
        }
        countryBlacklistMapper.insertSelective(blacklist);
    }

    public void update(CountryBlacklist blacklist){
        if (countryBlacklistMapper.selectByPrimaryKey(blacklist.getCountryCode())==null){
            throw new BusinessException(GeneralCode.SYS_NOT_EXIST);
        }
        countryBlacklistMapper.updateByPrimaryKeySelective(blacklist);
    }

    public void delete(String countryCode){
        if (countryBlacklistMapper.selectByPrimaryKey(countryCode)==null){
            throw new BusinessException(GeneralCode.SYS_NOT_EXIST);
        }
        countryBlacklistMapper.deleteByPrimaryKey(countryCode);
    }

    /**
     * 用户是否在国家白名单
     * @param userId user.id
     * @return  true.在白名单
     */
    public boolean isInWhiteList(Long userId){
        return countryWhitelistMapper.isInWhiteList(userId);
    }

    public void addWhiteList(UserCountryWhitelist whitelist){
        countryWhitelistMapper.insertOrUpdate(whitelist);
    }

    public void deleteWhiteList(Long userId){
        countryWhitelistMapper.deleteByPrimaryKey(userId);
    }

    public SearchResult<UserCountryWhitelist> queryWhiteList(UserCountryWhitelistQuery query){
        PageHelper.startPage(query.getOffset(), query.getLimit());
        List<UserCountryWhitelist> list = countryWhitelistMapper.selectWhiteList(Collections.singletonMap("userId", query.getUserId()));
        PageInfo pageInfo = PageInfo.of(list);
        return new SearchResult<>(list, pageInfo.getTotal());
    }

    /**
     * us交易所，不支持的国家和城市判断
     * @param ip
     * @return
     */
    public boolean checkIsUsNotSupportedStatesOrCity(String ip){
        if(StringUtils.isEmpty(usNotSupportedStates)){
            return false;
        }
        log.info("checkIsUsNotSupportedStatesOrCity ip ={}",ip);
        JSONObject jsonObject = JSON.parseObject(usNotSupportedStates);
        log.info("checkIsUsNotSupportedStatesOrCity usNotSupportedStates= {}",usNotSupportedStates);
        Set<String> nationList = jsonObject.keySet();
        log.info("checkIsUsNotSupportedStatesOrCity nations={}", nationList);
        IPResult ipResult = IP2LocationUtils.getDetail(ip);
        if(ipResult == null){
            log.warn("checkIsUsNotSupportedStatesOrCity ip = {} can not get region info.");
            return false;
        }
        log.info("checkIsUsNotSupportedStatesOrCity country ={},city = {}",ipResult.getCountryShort(),ipResult.getRegion());
        if(!nationList.contains(ipResult.getCountryShort())){
            return false;
        }
        List<String> statesList = Arrays.asList(jsonObject.getString(ipResult.getCountryShort()).split(","));

        if(statesList.contains(ipResult.getRegion())) {
            return true;
        }

        return false;
    }
}
