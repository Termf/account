package com.binance.account.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.common.enums.CertificateType;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.*;
import com.binance.account.data.mapper.certificate.*;
import com.binance.account.service.certificate.IUserKycDataMigration;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.account.service.country.impl.CountryBusiness;
import com.binance.account.vo.country.CountryVo;
import com.binance.inspector.api.FaceIdApi;
import com.binance.inspector.api.JumioApi;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author mikiya.chen
 * @date 2020/2/7 5:23 下午
 */

@Log4j2
@JobHandler(value = "KycCertificateInfoSupplyJobHandler")
@Component
public class KycCertificateInfoSupplyJobHandler extends IJobHandler {

    @Resource
    private UserKycApproveMapper userKycApproveMapper;

    @Resource
    private KycCertificateMapper kycCertificateMapper;

    @Resource
    private KycFillInfoMapper KycFillInfoMapper;

    @Resource
    private UserKycMapper userKycMapper;

    @Resource
    private CompanyCertificateMapper companyCertificateMapper;

    @Resource
    private JumioBusiness jumioBusiness;

    @Resource
    private CountryBusiness countryBusiness;

    @Resource
    private JumioApi jumioApi;

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private static final String BASE = "BASE";

    private static final String UNDEFINED = "undefined";

    private static final Integer DEFAULT_PAGE_SIZE = 200;
    
    @Autowired
	private ApolloCommonConfig config;
    
    @Resource
	private IUserKycDataMigration iUserKycDataMigration;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        XxlJobLogger.log("开始执行 KycCertificateInfoSupplyJobHandler 执行参数:" + s);
        log.info("START-KycCertificateInfoSupplyJobHandler");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try{
            handler(s);
            return ReturnT.SUCCESS;
        }catch (Exception e){
            XxlJobLogger.log("执行 KycCertificateInfoSupplyJobHandler 失败 param:{0} {1}", s, e);
            log.error("执行 KycCertificateInfoSupplyJobHandler 失败 param:{}", s, e);
            return FAIL;
        }finally {
            stopWatch.stop();
            XxlJobLogger.log("执行 KycCertificateInfoSupplyJobHandler 完成 use {0}s", stopWatch.getTotalTimeSeconds());
        }
    }

    private void handler(String params){
        if(StringUtils.isBlank(params)){
            Map<String, Object> param = new HashMap<>(4);
            param.put("start", 0);
            param.put("offset", DEFAULT_PAGE_SIZE);
            List<UserKycApprove> userKycApproves = userKycApproveMapper.selectUnFillCertificateInfoDataByPage(param);
            supplyCertificateInfo(userKycApproves);
        }else {
            JSONObject argsJson = JSON.parseObject(params);
            Long userId = argsJson.getLong("userId");
            Integer pageSize = argsJson.getInteger("pageSize");
            if (userId == null) {
                Map<String, Object> param = new HashMap<>(4);
                param.put("start", 0);
                param.put("offset", pageSize);
                List<UserKycApprove> userKycApproves = userKycApproveMapper.selectUnFillCertificateInfoDataByPage(param);
                supplyCertificateInfo(userKycApproves);
            } else {
                UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(userId);
                List<UserKycApprove> userKycApproves = new ArrayList<UserKycApprove>();
                userKycApproves.add(userKycApprove);
                supplyCertificateInfo(userKycApproves);
            }
        }

        if(config.isKycDataMigrationRun()) {
        	List<UserKycApprove> results = iUserKycDataMigration.selectPage(null, 0, config.getKycDataMigrationRunSize());
        	
        	if(results == null || results.isEmpty()) {
				return;
			}
        	for (UserKycApprove userKycApprove : results) {
        		try {
        			iUserKycDataMigration.moveToKycCertificate(userKycApprove);
        		}catch(Exception e) {
        			log.warn("迁移新流程异常 userId:{}", userKycApprove.getUserId(), e);
					iUserKycDataMigration.addExceptionTag(userKycApprove);
        		}
        	}
        }
        
    }

    private void supplyCertificateInfo(List<UserKycApprove> userKycApproves){
        for(UserKycApprove userKycApprove : userKycApproves) {
            try {
                if (userKycApprove.getBaseInfo() == null){
                    userKycApprove.setBaseInfo(new UserKycApprove.BaseInfo());
                }
                KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userKycApprove.getUserId());
                if (kycCertificate == null) {
                    CertificateType certificateType = CertificateType.getByCode(userKycApprove.getCertificateType());
                    if (Objects.equals(CertificateType.USER, certificateType)) {
                        //个人认证处理流程
                        UserKyc userKyc = userKycMapper.getLast(userKycApprove.getUserId());
                        //country信息补充
                        if (userKyc != null && StringUtils.isBlank(userKycApprove.getBaseInfo().getCountry())) {
                            userKycApprove.getBaseInfo().setCountry(userKyc.getBaseInfo().getCountry());
                        }
                        if (userKyc != null && StringUtils.isBlank(userKyc.getFaceOcrStatus()) && !StringUtils.isBlank(userKycApprove.getScanReference())) {
                            //填充jumioInfo
                            JumioInfoVo jumioInfoVo = jumioBusiness.getByUserAndScanRef(userKycApprove.getUserId(), userKycApprove.getScanReference(), JumioHandlerType.USER_KYC.getCode());
                            JumioInfoSupply(userKycApprove, jumioInfoVo);
                        } else {
                            //直接从approve表拿信息填充
                            InfoSupplyByApprove(userKycApprove);
                        }
                    } else if (Objects.equals(CertificateType.COMPANY, certificateType)) {
                        //企业认证处理流程
                        CompanyCertificate companyCertificate = companyCertificateMapper.getLast(userKycApprove.getUserId());
                        //country信息补充
                        if (companyCertificate != null && StringUtils.isBlank(userKycApprove.getBaseInfo().getCountry())) {
                            userKycApprove.getBaseInfo().setCountry(companyCertificate.getCompanyCountry());
                        }
                        if (!StringUtils.isBlank(userKycApprove.getScanReference())) {
                            //直接填充Jumio信息
                            JumioInfoVo jumioInfoVo = jumioBusiness.getByUserAndScanRef(userKycApprove.getUserId(), userKycApprove.getScanReference(), JumioHandlerType.USER_KYC.getCode());
                            JumioInfoSupply(userKycApprove, jumioInfoVo);
                        }else {
                            //直接从approve表拿信息填充
                            InfoSupplyByApprove(userKycApprove);
                        }
                    }
                } else {
                    //country信息补充
                    if (StringUtils.isBlank(userKycApprove.getBaseInfo().getCountry())) {
                        KycFillInfo kycFillInfo = KycFillInfoMapper.selectByUserIdFillType(userKycApprove.getUserId(), BASE);
                        userKycApprove.getBaseInfo().setCountry(kycFillInfo == null ? null : kycFillInfo.getCountry());
                    }
                    if (StringUtils.isBlank(kycCertificate.getJumioStatus())) {
                        //直接从approve表拿信息填充
                        InfoSupplyByApprove(userKycApprove);
                    } else {
                        //填充jumioInfo
                        APIResponse<JumioInfoVo> response = jumioApi.getLastJumio(APIRequest.instance(userKycApprove.getUserId()));
                        JumioInfoVo jumioInfoVo = response.getData();
                        JumioInfoSupply(userKycApprove, jumioInfoVo);
                    }
                }
                //如果此时userKycApprove certificate的四项信息仍为空,则进行信息的填充
                if(StringUtils.isBlank(userKycApprove.getCertificateFirstName()) && StringUtils.isBlank(userKycApprove.getCertificateLastName())
                && StringUtils.isBlank(userKycApprove.getCertificateDob()) && StringUtils.isBlank(userKycApprove.getCertificateCountry())) {
                    log.info("read the certificateInfo null, InfoSupplyForException, the userId is:{}", userKycApprove.getUserId());
                    InfoSupplyForException(userKycApprove);
                }
                userKycApproveMapper.updateCertificateInfo(userKycApprove);
                log.info("execute KycCertificateInfoSupply success, the userId is:{}", userKycApprove.getUserId());
            } catch (Exception e){
                log.warn("KycCertificateInfoSupplyJobHandler error, the userId is:{}, the exception is:{}", userKycApprove.getUserId(), e);
                InfoSupplyForException(userKycApprove);
                userKycApproveMapper.updateCertificateInfo(userKycApprove);
            }
        }
    }

    private void JumioInfoSupply(UserKycApprove userKycApprove, JumioInfoVo jumioInfoVo){
        if(jumioInfoVo == null){
            //查询jumioInfoVo出现异常，填充手写信息
            InfoSupplyForException(userKycApprove);
            return;
        }
        CountryVo countryVo = countryBusiness.getCountryByAlpha3(jumioInfoVo.getIssuingCountry());
        userKycApprove.setCertificateFirstName(jumioInfoVo.getFirstName());
        userKycApprove.setCertificateLastName(jumioInfoVo.getLastName());
        userKycApprove.setCertificateDob(jumioInfoVo.getDob());
        userKycApprove.setCertificateCountry(countryVo.getCode());
        return;
    }

    private void InfoSupplyByApprove(UserKycApprove userKycApprove){
        userKycApprove.setCertificateFirstName(userKycApprove.getBaseInfo().getFirstName());
        if(userKycApprove.getBaseInfo().getMiddleName() == null && userKycApprove.getBaseInfo().getLastName() == null ){
            userKycApprove.setCertificateLastName(null);
        }else if(userKycApprove.getBaseInfo().getMiddleName() == null){
            userKycApprove.setCertificateLastName(userKycApprove.getBaseInfo().getLastName());
        }else if(userKycApprove.getBaseInfo().getLastName() == null){
            userKycApprove.setCertificateLastName(userKycApprove.getBaseInfo().getMiddleName());
        }else {
            userKycApprove.setCertificateLastName(userKycApprove.getBaseInfo().getMiddleName() + userKycApprove.getBaseInfo().getLastName());
        }
        userKycApprove.setCertificateDob(userKycApprove.getBaseInfo().getDob() == null ? null : sdf.format(userKycApprove.getBaseInfo().getDob()));
        userKycApprove.setCertificateCountry(userKycApprove.getBaseInfo().getCountry());
    }

    private void InfoSupplyForException(UserKycApprove userKycApprove){
        KycFillInfo kycFillInfo = KycFillInfoMapper.selectByUserIdFillType(userKycApprove.getUserId(), BASE);
        if(kycFillInfo == null) {
        	userKycApprove.setCertificateFirstName(UNDEFINED);
            userKycApprove.setCertificateLastName(UNDEFINED);
            userKycApprove.setCertificateDob(UNDEFINED);
            userKycApprove.setCertificateCountry(UNDEFINED);
        }else {
        	userKycApprove.setCertificateFirstName(StringUtils.isBlank(kycFillInfo.getFirstName()) ? UNDEFINED : kycFillInfo.getFirstName());
        	userKycApprove.setCertificateLastName(StringUtils.isBlank(kycFillInfo.getLastName()) ? UNDEFINED : kycFillInfo.getLastName());
        	userKycApprove.setCertificateDob(StringUtils.isBlank(kycFillInfo.getBirthday()) ? UNDEFINED : kycFillInfo.getBirthday());
        	userKycApprove.setCertificateCountry(StringUtils.isBlank(kycFillInfo.getCountry()) ? UNDEFINED : kycFillInfo.getCountry());
        }
        
        
    }
}
