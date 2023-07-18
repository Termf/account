package com.binance.account.domain.bo;

import com.binance.master.old.models.account.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Fei.Huang on 2018/4/10.
 */
@Getter
@Setter
public class MigrationOldData {

    private Long minUserId;
    private boolean autoSynchron;

    // 原始数据
    private List<OldUser> oldUserList;
    private List<Long> oldUserIds;
    private List<OldUserData> oldUserDataList;
    private List<OldUserSecurity> oldUserSecurityList;
    private List<OldUserIpKey> oldUserIpKeyList;
    private List<OldUserIdPhoto> oldUserIdPhotoList;
    private List<OldCompanyAuthentication> oldCompanyAuthenticationList;
    private List<OldUserLog> oldUserLogList;

    // 把部分原始数据遍历成map<userId,data>格式后面方便使用
    private Map<String, OldUserData> oldUserDataMap = new HashMap<String, OldUserData>();
    private Map<String, OldUserSecurity> oldUserSecurityMap = new HashMap<String, OldUserSecurity>();
    private Map<String, java.util.List<OldUserIpKey>> oldUserIpKeysMap = new HashMap<String, java.util.List<OldUserIpKey>>();
    private Map<String, OldUserIdPhoto> oldUserIdPhotoMap = new HashMap<String, OldUserIdPhoto>();
    private Map<String, OldCompanyAuthentication> oldCompanyAuthenticationMap = new HashMap<String, OldCompanyAuthentication>();
    private Map<String, List<OldUserLog>> oldUserLogMap = new HashMap<>();

    public Map<String, OldUserData> getOldUserDataMap() {
        if (oldUserDataList != null) {
            oldUserDataMap = oldUserDataList.stream().collect(Collectors.toMap(OldUserData::getUserId, Function.identity()));
        }
        return oldUserDataMap;
    }

    public Map<String, OldUserSecurity> getOldUserSecurityMap() {
        if (oldUserSecurityList != null) {
            oldUserSecurityMap = oldUserSecurityList.stream().collect(Collectors.toMap(OldUserSecurity::getUserId, Function.identity()));
        }
        return oldUserSecurityMap;
    }

    public Map<String, List<OldUserIpKey>> getOldUserIpKeysMap() {
        if (oldUserIpKeyList != null) {
            oldUserIpKeyList.forEach(oldUserIpKey -> {
                List<OldUserIpKey> oldUserIpKeys = oldUserIpKeysMap.get(oldUserIpKey.getUserid());
                if (null == oldUserIpKeys) {
                    oldUserIpKeys = new ArrayList<>();
                }
                oldUserIpKeys.add(oldUserIpKey);
                oldUserIpKeysMap.put(oldUserIpKey.getUserid(), oldUserIpKeys);
            });
        }
        return oldUserIpKeysMap;
    }

    public Map<String, OldUserIdPhoto> getOldUserIdPhotoMap() {
        if (oldUserIdPhotoList != null) {
            oldUserIdPhotoList.forEach(oldUserIdPhoto -> {
                OldUserIdPhoto finalOldUserPhoto = oldUserIdPhotoMap.get(oldUserIdPhoto.getUserid());
                if (null == finalOldUserPhoto) {
                    finalOldUserPhoto = oldUserIdPhoto;
                } else {
                    // 若有更新值则取更新值
                    if (oldUserIdPhoto.getId() > finalOldUserPhoto.getId()) {
                        finalOldUserPhoto = oldUserIdPhoto;
                    }
                }
                oldUserIdPhotoMap.put(oldUserIdPhoto.getUserid(), finalOldUserPhoto);
            });
        }
        return oldUserIdPhotoMap;
    }

    public Map<String, OldCompanyAuthentication> getOldCompanyAuthenticationMap() {
        if (oldCompanyAuthenticationList != null) {
            oldCompanyAuthenticationList.forEach(oldCompanyAuthentication -> {
                OldCompanyAuthentication finalOldCompanyAuthentication =
                        oldCompanyAuthenticationMap.get(oldCompanyAuthentication.getUserId());
                if (finalOldCompanyAuthentication == null) {
                    finalOldCompanyAuthentication = oldCompanyAuthentication;
                } else {
                    // 若有更新值则取更新值
                    if (oldCompanyAuthentication.getId() > finalOldCompanyAuthentication.getId()) {
                        finalOldCompanyAuthentication = oldCompanyAuthentication;
                    }
                }
                oldCompanyAuthenticationMap.put(oldCompanyAuthentication.getUserId(), finalOldCompanyAuthentication);
            });
        }
        return oldCompanyAuthenticationMap;
    }

    public Map<String, List<OldUserLog>> getOldUserLogMap() {
        if (oldUserLogList != null) {
            oldUserLogList.forEach(oldUserLog -> {
                List<OldUserLog> oldUserLogs = oldUserLogMap.get(oldUserLog.getUserId());
                if (null == oldUserLogs) {
                    oldUserLogs = new ArrayList<>();
                }
                oldUserLogs.add(oldUserLog);
                oldUserLogMap.put(oldUserLog.getUserId(), oldUserLogs);
            });
        }
        return oldUserLogMap;
    }
}
