package com.binance.account.service.certificate;

import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.StringUtils;
import com.binance.sysconf.service.SysConfigVarCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component
public class CertificateHelper {

    private static final String SWITCH_WCK = "world_check_switch";

    @Resource
    private SysConfigVarCacheService sysConfigVarCacheService;
    @Resource
    private UserIndexMapper userIndexMapper;

    /**
     * world-check开关
     * @return true：开启  false：关闭  默认false
     */
    public boolean isSwitchOn(){
        String val = sysConfigVarCacheService.getValue(SWITCH_WCK);
        if (StringUtils.isNotBlank(val)){
            return Boolean.valueOf(val);
        }else {
            return false;
        }
    }

    /**
     * 获取交易所flag
     */
    public String getDomainFlag(){
        String domain = sysConfigVarCacheService.getValue("exch_domain");
        if (StringUtils.isBlank(domain)){
            log.warn("getDomainFlag failed: empty");
            return "";
        }else if (domain.contains("sg")){
            return "SG";
        }else if (domain.contains("co.ug")){
            return "UG";
        }else if (domain.contains("je")){
            return "JE";
        }else {
            return "";
        }
    }

    public Map<Long, String> getUserIdToEmailMap(List<Long> userIds) {
        Set<Long> userIdSet = new HashSet<Long>(userIds);
        List<UserIndex> userIndexs = userIndexMapper.selectByUserIds(userIdSet);
        Map<Long, String> result = new HashMap<>(userIndexs.size());
        userIndexs.forEach(item -> {
            result.put(item.getUserId(), item.getEmail());
        });
        return result;
    }

    public Long getUserByEmail(String email) {
        Long userId = userIndexMapper.selectIdByEmail(email);
        if (userId==null){
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        return userId;
    }

    public String getEmailByUserId(Long userId) {
        return userIndexMapper.selectEmailById(userId);
    }
}
