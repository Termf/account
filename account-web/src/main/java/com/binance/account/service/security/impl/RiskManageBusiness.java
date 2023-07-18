package com.binance.account.service.security.impl;

import static com.binance.platform.amazon.s3.util.S3ObjectUtils.escapeObjectKey;

import com.alibaba.fastjson.JSON;
import com.amazonaws.services.rekognition.model.IndexFacesResult;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.service.file.impl.FileStorageBusiness;
import com.binance.account.service.security.IRiskManage;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIRequestHeader;
import com.binance.master.models.APIResponse;
import com.binance.master.old.ibusiness.sys.ISysConfig;
import com.binance.master.old.models.sys.SysConfig;
import com.binance.platform.amazon.s3.service.RekongnitionService;
import com.binance.platform.amazon.s3.util.S3ObjectUtils;
import com.binance.risk.api.RiskFaceIdIndexApi;
import com.binance.risk.api.RiskIdNumberApi;
import com.binance.risk.vo.OperationType;
import com.binance.risk.vo.RiskIdNumberBlackListVo;
import com.binance.risk.vo.cases.request.RiskFaceIdIndexRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;


/**
 * @author liliang1
 * @date 2018-09-20 16:06
 */
@Log4j2
@Service
public class RiskManageBusiness implements IRiskManage {

    @Resource
    private UserIndexMapper userIndexMapper;

    @Resource
    private RiskIdNumberApi riskIdNumberApi;

    @Resource
    private RekongnitionService rekongnitionService;

    @Resource
    private RiskFaceIdIndexApi riskFaceIdIndexApi;

    @Resource
    private ISysConfig iSysConfig;

    @Resource
    private FileStorageBusiness fileStorageBusiness;

    //iSysConfig.selectByDisplayName("user_activate")

    @Override
    public boolean checkIdNumberBackList(Long userId, String idType, String idNumber, String country, OperationType operationType, String jumioFacePath) {
        UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null) {
            log.info("检查用户证件信息是否在黑名单中获取用户信息失败, userId:{}", userId);
            throw new BusinessException("用户信息错误");
        }
        String email = userIndex.getEmail();
        APIRequestHeader header = new APIRequestHeader();
        header.setTerminal(TerminalEnum.PC);
        header.setLanguage(LanguageEnum.ZH_CN);
        RiskIdNumberBlackListVo vo = new RiskIdNumberBlackListVo();
        vo.setUserId(String.valueOf(userId));
        vo.setEmail(email);
        vo.setIdType(idType);
        vo.setIdNumber(idNumber);
        vo.setCountry(country);
        vo.setOperationType(operationType);
        vo.setFacePath(jumioFacePath);
        try {
            APIResponse<Boolean> result = riskIdNumberApi.checkIdNumberBlackList(APIRequest.instance(header, vo));
            log.info("检查用户证件信息黑名单结果. userId:{} result:{}", userId, JSON.toJSONString(result));
            return result.getData() == null ? false : result.getData();
        }catch (Exception e) {
            log.error("调用检查用户证件信息黑名单接口异常. userId:{} ", userId, e);
            throw new BusinessException("调用检查用户证件信息黑名单接口异常");
        }
    }


    @Override
    public void indexFaceIfNeeded(String s3Key) {
        log.info("index face：{}", s3Key);
        if (StringUtils.isBlank(s3Key)) {
            return;
        }

        SysConfig needIndex = iSysConfig.selectByDisplayName("need_index_user_face");
        if (needIndex == null || BooleanUtils.toBoolean(needIndex.getCode()) == false) {
            log.info("no need to index user face, return...");
            return;
        }
        try {
            log.info("downloading image: {}", s3Key);
            byte[] image = fileStorageBusiness.load(s3Key);
            if (ArrayUtils.isEmpty(image)) {
                log.info("failed to download image");
                return;
            }
            log.info("download completed: {} kb", (image.length / 1000));
            IndexFacesResult result = rekongnitionService.indexFaces(
                    S3ObjectUtils.objectKeyToExternalImageId(escapeObjectKey(s3Key)), 1, image);
            if (CollectionUtils.isEmpty(result.getFaceRecords())) {
                log.warn("result.getFaceRecords() of {} is empty.", s3Key);
            } else {
                String faceId = result.getFaceRecords().get(0).getFace().getFaceId();
                APIRequestHeader header = new APIRequestHeader();
                header.setTerminal(TerminalEnum.PC);
                header.setLanguage(LanguageEnum.ZH_CN);
                RiskFaceIdIndexRequest reqVo = new RiskFaceIdIndexRequest();
                reqVo.setPath(escapeObjectKey(s3Key));
                reqVo.setFaceId(faceId);
                APIResponse<Boolean> response = riskFaceIdIndexApi.insertFaceIdIndexByPathAndFaceId(APIRequest.instance(header, reqVo));
                log.info("insert faceid response: {}",  JSON.toJSONString(response));
            }
        } catch (Exception e) {
            log.error("index [" + s3Key + "] error.", e);
        }
    }
}
