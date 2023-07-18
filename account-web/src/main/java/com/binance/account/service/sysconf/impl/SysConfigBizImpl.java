package com.binance.account.service.sysconf.impl;

import com.binance.master.old.ibusiness.sys.ISysConfig;
import com.binance.master.old.models.sys.SysConfig;
import com.binance.sysconf.service.SysConfigVarCacheService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * Description: 系统数据库配置表读取实现类<br/>
 * 此类只是为了兼容原有代码,否则建议直接使用SysConfigVarCacheService类
 * @author hongchaoMao - Date 2018/7/30
 */
@Primary
@Service
public class SysConfigBizImpl implements ISysConfig {

    @Resource
    private SysConfigVarCacheService sysConfigVarCacheService;

    @Override
    public SysConfig selectByDisplayName(String s) {
        SysConfig sysConfig = null;
        if (!StringUtils.isEmpty(s)){
            String code = sysConfigVarCacheService.getValue(s);
            if (code != null) {
                sysConfig = new SysConfig();
                sysConfig.setDisplayName(s);
                sysConfig.setCode(code);
            }
        }
        return sysConfig;
    }
}
