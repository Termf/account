package com.binance.account.yubikey;

import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.WebAuthnAdminQuery;
import com.binance.account.data.entity.security.UserYubikey;
import com.binance.account.data.mapper.security.AdminYubikeyMapper;
import com.binance.account.vo.yubikey.UserYubikeyVo;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.StringUtils;
import com.yubico.webauthn.RelyingParty;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class WebAuthnAdminHandler extends AbstractWebAuth {

    /**
     * 需要与对应的域名段保持一致，多个域名间使用","分割,
     * 注意点：当这个值变更的时候，需要重启服务
     */
    @Value("${yubikey.relypart.admin.id:null}")
    private String relyPartIds;
    @Value("${yubikey.relypart.admin.name:Binance_Admin}")
    private String relyPartName;
    @Value("${yubikey.relypart.admin.origins:null}")
    private String origins;

    private RegistrationStorage registrationStorage;
    @Autowired
    private AdminYubikeyMapper yubikeyMapper;

    /**
     * 根据具体的域名信息配置对应的PR
     * key: app_id
     * value rp
     */
    private Map<String, RelyingParty> rpMap;

    @PostConstruct
    public void init() {
        rpMap = super.init(relyPartIds, origins, relyPartName);
        this.registrationStorage = new RegistrationStorage(yubikeyMapper, "admin");
    }

    @Override
    RelyingParty getRelyingParty(String origin, List<UserYubikey> userYubikeys) {
        return rpMap.get(origin);
    }

    @Override
    RegistrationStorage getStorage() {
        if (this.registrationStorage == null) {
            synchronized (this) {
                this.registrationStorage = new RegistrationStorage(yubikeyMapper, "admin");
            }
        }
        return this.registrationStorage;
    }

    @Override
    protected String origins() {
        return origins;
    }

    public List<UserYubikeyVo> getAllList() {
        List<UserYubikey> userYubikeys = yubikeyMapper.getAll();
        return convertToVo(userYubikeys);
    }

    private List<UserYubikeyVo> convertToVo(List<UserYubikey> yubikeys) {
        List<UserYubikeyVo> result = new ArrayList<>();
        if (yubikeys != null && !yubikeys.isEmpty()) {
            for (UserYubikey userYubikey : yubikeys) {
                UserYubikeyVo vo = new UserYubikeyVo();
                BeanUtils.copyProperties(userYubikey, vo);
                result.add(vo);
            }
        }
        return result;
    }

    public SearchResult<UserYubikeyVo> adminGetPageList(WebAuthnAdminQuery query) {
        long count = yubikeyMapper.adminGetPageListCount(query);
        if (count <= 0) {
            return new SearchResult<>(Collections.emptyList(), 0);
        }
        List<UserYubikey> yubikeys = yubikeyMapper.adminGetPageList(query);
        List<UserYubikeyVo> vos = convertToVo(yubikeys);
        return new SearchResult<>(vos, count);
    }

    /**
     * 强制解绑一个admin用户的yubikey
     * @param userId
     * @param origin
     * @return
     */
    public int deregisterForce(Long userId, String origin) {
        if (userId == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        List<UserYubikey> userYubikeys;
        if (StringUtils.isBlank(origin)) {
            userYubikeys = getStorage().getByUserId(userId);
        } else {
            userYubikeys = getStorage().getByOrigin(userId, origin);
        }
        log.info("force remove admin user webauthn key: userId:{} origin:{} size:{}", userId, origin, userYubikeys.size());
        int count = 0;
        for (UserYubikey yubikey : userYubikeys) {
            count += getStorage().deleteUserYubikey(userId, yubikey.getId());
        }
        return count;
    }

}
