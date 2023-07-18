package com.binance.account.service.certificate.impl;

import com.binance.account.common.enums.KycStatus;
import com.binance.account.common.enums.WckStatus;
import com.binance.account.common.query.SearchResult;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.Jumio;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.certificate.UserWckAudit;
import com.binance.account.data.entity.certificate.UserWckAuditLog;
import com.binance.account.data.mapper.certificate.JumioMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.certificate.UserWckAuditLogMapper;
import com.binance.account.data.mapper.certificate.UserWckAuditMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.domain.bo.WckAuditDataHolder;
import com.binance.account.service.certificate.CertificateHelper;
import com.binance.account.vo.certificate.UserWckAuditVo;
import com.binance.account.vo.certificate.request.UserWckQuery;
import com.binance.account.vo.certificate.request.WckAuditRequest;
import com.binance.account.vo.user.request.KycAuditRequest;
import com.binance.inspector.api.WorldCheckApi;
import com.binance.inspector.vo.worldcheck.WckResultProfileVo;
import com.binance.inspector.vo.worldcheck.request.WckInspectApplyRequest;
import com.binance.inspector.vo.worldcheck.response.WckInspectApplyResponse;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: caixinning
 * @date: 2018/08/03 20:10
 **/
@Log4j2
@Service
public class UserWckBusiness {

    @Autowired
    private WorldCheckApi worldCheckApi;
    @Autowired
    private UserWckAuditMapper wckAuditMapper;
    @Autowired
    private UserWckAuditLogMapper wckAuditLogMapper;
    @Autowired
    private UserIndexMapper userIndexMapper;
    @Autowired
    private UserKycMapper userKycMapper;
    @Autowired
    private UserKycBusiness userKycBusiness;
    @Autowired
    private JumioMapper jumioMapper;
    @Autowired
    private ApolloCommonConfig apolloCommonConfig;
    @Autowired
    private CertificateHelper certificateHelper;

    /**
     * 查询 world check审核结果
     * @param kycId 即UserKyc表的id
     */
    public List<WckResultProfileVo> getWckResultProfile(Long kycId){
        String caseId = certificateHelper.getDomainFlag() + kycId;
        APIResponse<List<WckResultProfileVo>> rs = worldCheckApi.getWckResultProfile(caseId);
        if (rs.getStatus()==APIResponse.Status.OK){
            return rs.getData();
        }else {
            log.warn("getWckResultProfile failed, caseId: {}, msg:{}", caseId, rs.getErrorData());
            throw new BusinessException("getWckResultProfile failed:"+rs.getErrorData());
        }
    }

    /**
     * world check审核进度
     */
    public List getWcAuditEvents(Long kycId){
        String caseId = certificateHelper.getDomainFlag() + kycId;
        APIResponse<List> rs = worldCheckApi.getWcAuditEvents(caseId);
        if (rs.getStatus()==APIResponse.Status.OK){
            return rs.getData();
        }else {
            log.warn("getWcAuditEvents failed, caseId: {}, msg:{}", caseId, rs.getErrorData());
            throw new BusinessException("getWcAuditEvents failed:"+rs.getErrorData());
        }
    }

    /**
     * 发起world check审核
     */
    public void applyWorldCheck(Jumio jumio, UserKyc kyc){
        String caseId = certificateHelper.getDomainFlag() + kyc.getId();
        WckInspectApplyRequest request = new WckInspectApplyRequest();
        request.setCaseId(caseId);
        String checkName = kyc.getCheckName(jumio);
        if (StringUtils.isBlank(checkName)){
            log.warn("applyWorldCheck, Jumio name is empty, use fill name: {} - {}", kyc.getUserId(), kyc.getId());
            checkName = kyc.getFillName();
        }
        request.setName(checkName);
        request.setBirthDate(kyc.tryGetDob(jumio));
        request.setNationality(jumio.getIssuingCountry());
        UserWckAudit userWckAudit = wckAuditMapper.selectByPrimaryKey(kyc.getId());
        if(userWckAudit != null) {
            log.warn("applyWorldCheck user_wck_audit has the record kycId={}",kyc.getId());
            return;
        }
        APIResponse<WckInspectApplyResponse> response = worldCheckApi.applyWorldCheck(APIRequest.instance(request));
        if (response.getStatus() == APIResponse.Status.OK){
            WckInspectApplyResponse body = response.getData();
            UserWckAudit wckAudit = new UserWckAudit();
            wckAudit.setKycId(kyc.getId());
            wckAudit.setUserId(kyc.getUserId());
            wckAudit.setStatus(WckStatus.INITIAL);
            wckAudit.setCaseSystemId(body.getCaseSystemId());
            wckAudit.setIssuingCountry(jumio.getIssuingCountry());
            wckAuditMapper.insertSelective(wckAudit);
        }else {
            log.error("applyWorldCheck failed, request:{}, response:{}", request, response);
        }
    }

    /**
     * 发起world check审核（若已有审核记录，则先删除）
     */
    public void applyOrResetWorldCheck(Jumio jumio, UserKyc kyc){
        UserWckAudit audit = wckAuditMapper.selectByPrimaryKey(kyc.getId());
        if (audit != null){
            log.warn("Reset UserWckAudit, kycId:{}, data:{}", audit.getKycId(), audit.toString());
            wckAuditMapper.deleteByKycId(audit.getKycId());
            wckAuditLogMapper.deleteByKycId(audit.getKycId());
        }
        this.applyWorldCheck(jumio, kyc);
    }

    /**
     * 查询审核列表
     */
    public SearchResult<UserWckAuditVo> listForAdmin(UserWckQuery query){
        if (query.getUserId()==null && StringUtils.isNotBlank(query.getEmail())){
            Long userId = userIndexMapper.selectIdByEmail(query.getEmail());
            if (userId==null){
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            query.setUserId(userId);
        }

        // 准备翻页
        SearchResult<UserWckAuditVo> result = null;

        PageHelper.startPage(query.getPage(), query.getRows());
        log.info("listForAdmin query.country={}",query.getCountry());
        List<Map<String, Object>> rawList = wckAuditMapper.selectWckAuditInfo(query.getUserId(), query.getStatus(), query.getAuditorId(), query.getAuditorSeq(),query.getCountry());

        if (CollectionUtils.isNotEmpty(rawList)){
            // 处理原始数据
            List<UserWckAuditVo> auditVos = rawList.stream().map(rawData ->{
                UserWckAuditVo vo = new UserWckAuditVo(rawData);
                UserKyc kyc = userKycMapper.getById(vo.getUserId(), vo.getKycId());
                if (kyc!=null){
                    Jumio jumio = jumioMapper.selectByPrimaryKey(kyc.getUserId(),kyc.getJumioId());
                    if(jumio!=null){
                        vo.setUserName(kyc.getFillName());
                        vo.setUserNameFromJumio(jumio.getName());
                        vo.setBirthDate(kyc.tryGetDob(jumio));
                        vo.setCountryLocation(StringUtils.join(jumio.getCity()," ",jumio.getAddress()).trim());
                        vo.setCardType(jumio.getDocumentType());
                        vo.setCardNumber(jumio.getNumber());
                        vo.setEmail(userIndexMapper.selectEmailById(vo.getUserId()));
                    }
                }
                return vo;
            }).collect(Collectors.toList());
            log.info("auditVos size is={},rawList size={}",auditVos.size(),rawList.size());
            PageInfo pageInfo = PageInfo.of(rawList);
            result = new SearchResult<>(auditVos, pageInfo.getTotal());
        }
        //结束翻页
        PageHelper.clearPage();
        return result;
    }
    /**
     * 查询审核列表
     */
    public SearchResult<UserWckAuditVo> getWckList(UserWckQuery query){
        if (query.getUserId()==null && StringUtils.isNotBlank(query.getEmail())){
            Long userId = userIndexMapper.selectIdByEmail(query.getEmail());
            if (userId==null){
                throw new BusinessException(GeneralCode.USER_NOT_EXIST);
            }
            query.setUserId(userId);
        }
        // 准备翻页
        SearchResult<UserWckAuditVo> result = null;
        PageHelper.startPage(query.getPage(), query.getRows());
        List<Map<String, Object>> rawList = wckAuditMapper.selectByAdmin(query.getUserId(), query.getStatus(), query.getAuditorId(), query.getAuditorSeq());
        List<Long> kycIds = rawList.stream().map(rawData ->{
            Long kycId = (Long) rawData.get("kyc_id");
            return kycId;
            }).collect(Collectors.toList());
        List<UserKyc> userKycList = userKycMapper.getByPrimaryKeys(kycIds);

        if (CollectionUtils.isNotEmpty(rawList)){
            // 处理原始数据
            List<UserWckAuditVo> auditVos = rawList.stream().map(rawData ->{
                UserWckAuditVo vo = new UserWckAuditVo(rawData);
                UserKyc kyc = userKycMapper.getById(vo.getUserId(), vo.getKycId());
                if (kyc!=null){
                    Jumio jumio = jumioMapper.selectByPrimaryKey(kyc.getUserId(),kyc.getJumioId());
                    if(jumio!=null){
                        vo.setUserName(kyc.getFillName());
                        vo.setUserNameFromJumio(jumio.getName());
                        vo.setBirthDate(kyc.tryGetDob(jumio));
                        vo.setNationality(jumio.getIssuingCountry());
                        vo.setCountryLocation(StringUtils.join(jumio.getCity()," ",jumio.getAddress()).trim());
                        vo.setCardType(jumio.getDocumentType());
                        vo.setCardNumber(jumio.getNumber());
                        vo.setEmail(userIndexMapper.selectEmailById(vo.getUserId()));
                    }
                }
                return vo;
            }).collect(Collectors.toList());
            PageInfo pageInfo = PageInfo.of(rawList);
            result = new SearchResult<>(auditVos, pageInfo.getTotal());
        }
        //结束翻页
        PageHelper.clearPage();
        return result;
    }

    /**
     * 审核步骤：
     * 1.准备数据
     * 2.基础数据验证
     * 3.执行审核逻辑
     * 4.结果落库
     * 5.视情况更新KYC数据
     */
    public void audit(WckAuditRequest request){

        UserWckAudit wckAudit = wckAuditMapper.selectByPrimaryKey(request.getKycId());
        List<UserWckAuditLog> auditLogs = wckAuditLogMapper.selectByKycIds(Lists.newArrayList(request.getKycId()));

        WckAuditDataHolder holder = WckAuditDataHolder.build(request);

        holder.prepareData(wckAudit, auditLogs);
        holder.validate();
        holder.doAudit();

        wckAuditMapper.updateByPrimaryKeySelective(holder.getResult());
        wckAuditLogMapper.insertSelective(holder.getAuditLog());
        if (request.getForceFinal()){
            wckAuditLogMapper.insertSelective(holder.getForceFinalAuditLog());
        }

        if (holder.isFinished()){
            // 更新kyc数据
            KycAuditRequest kycAuditRequest = new KycAuditRequest();
            kycAuditRequest.setId(wckAudit.getKycId());
            kycAuditRequest.setUserId(wckAudit.getUserId());
            if (holder.isPassed()){
                kycAuditRequest.setStatus(KycStatus.wckPassed);
            }else {
                kycAuditRequest.setStatus(KycStatus.wckRefused);
                kycAuditRequest.setMemo("World-Check One refused: "+ request.getMemo());
            }
            log.info("userwckbusiness apolloCommonConfig isMainsiteWckBackendSwitch = {}",apolloCommonConfig.isMainsiteWckBackendSwitch());
            if(!apolloCommonConfig.isMainsiteWckBackendSwitch()) {
                APIResponse<?> response = userKycBusiness.audit(APIRequest.instance(kycAuditRequest));
                log.info("Update kyc status after wck audit: {}", response.toString());
                if (response.getStatus() != APIResponse.Status.OK ){
                    throw new BusinessException(String.valueOf(response.getErrorData()));
                }
            }

        }
    }

    /**
     * 根据KYC ID 集 获取对应记录集
     * @param kycIds
     * @return
     */
    public Map<Long, UserWckAudit> getByKycIds(Collection<Long> kycIds) {
        if (kycIds == null || kycIds.isEmpty()) {
            return Maps.newHashMap();
        }
        List<UserWckAudit> list = wckAuditMapper.selectByKycIds(kycIds);
        if (list == null || list.isEmpty()) {
            return Maps.newHashMap();
        }else {
            return list.stream().collect(Collectors.toMap(UserWckAudit::getKycId, item -> item));
        }
    }

}
