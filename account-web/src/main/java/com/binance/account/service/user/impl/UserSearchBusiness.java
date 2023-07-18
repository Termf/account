package com.binance.account.service.user.impl;

import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.entity.user.UserMobileIndex;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.tag.TagIndicatorMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.data.mapper.user.UserMobileIndexMapper;
import com.binance.account.data.mapper.user.UserSearchMapper;
import com.binance.account.service.country.ICountry;
import com.binance.account.service.user.IUserSearch;
import com.binance.account.vo.user.ex.SearchUserListEx;
import com.binance.account.vo.user.request.SearchUserListRequest;
import com.binance.account.vo.user.response.SearchUserListResponse;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author lufei
 * @date 2018/8/3
 */
@Log4j2
@Service
public class UserSearchBusiness implements IUserSearch {

    @Resource
    private ICountry iCountry;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserSecurityMapper usMapper;
    @Resource
    protected UserInfoMapper uiMapper;
    @Resource
    private UserIndexMapper userIndexMapper;
    @Resource
    private UserMobileIndexMapper userMobileIndexMapper;
    @Resource
    private TagIndicatorMapper tagIndicatorMapper;
    @Resource
    private UserSearchMapper userSearchMapper;

    private final static String[] TABLE_SUFFIX_ARRAY = {"_0", "_1", "_2", "_3", "_4", "_5", "_6", "_7", "_8", "_9",
            "_10", "_11", "_12", "_13", "_14", "_15", "_16", "_17", "_18", "_19",};

    private boolean isForkJoin = true;

    @Override
    public APIResponse<SearchUserListResponse> searchUserList(SearchUserListRequest request) {
        if (request.getUserId() != null) {
            return queryByUserId(request);
        }
        if (StringUtils.isNotBlank(request.getMobile())) {
            return queryByMobile(request);
        }
        return this.query(request);
    }

    private APIResponse<SearchUserListResponse> queryByMobile(SearchUserListRequest request) {
        String mobile = request.getMobile();
        String mobileCode = request.getMobileCode();
        UserMobileIndex userMobileIndex =null;
        if(org.apache.commons.lang3.StringUtils.isNotBlank(mobileCode)){
            userMobileIndex= userMobileIndexMapper.selectByPrimaryKey(mobile,mobileCode);
        }else{
            userMobileIndex= userMobileIndexMapper.selectByMobile(mobile);
        }
        if (userMobileIndex == null) {
            return APIResponse.getOKJsonResult(new SearchUserListResponse(Collections.emptyList(), 0L));
        }
        if (request.getUserId() == null || request.getUserId().compareTo(userMobileIndex.getUserId()) == 0) {
            request.setUserId(userMobileIndex.getUserId());
            return this.queryByUserId(request);
        } else {
            return APIResponse.getOKJsonResult(new SearchUserListResponse(Collections.emptyList(), 0L));
        }
    }

    private APIResponse<SearchUserListResponse> queryByUserId(SearchUserListRequest request) {
        Long userId = request.getUserId();
        List<Long> tagIds = request.getTagIds();
        List<Long> userIdInCommon = new ArrayList<>();
        userIdInCommon.add(userId);
        if (!CollectionUtils.isEmpty(tagIds)) {
            this.selectUserIdFromTag(userIdInCommon, tagIds, true);
        }
        if (CollectionUtils.isEmpty(userIdInCommon)) {
            return APIResponse.getOKJsonResult(new SearchUserListResponse(Collections.emptyList(), 0L));
        }
        UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
        if (userIndex == null) {
            return APIResponse.getOKJsonResult(new SearchUserListResponse(Collections.emptyList(), 0L));
        }
        User user = this.userMapper.queryByEmail(userIndex.getEmail());
        if (user == null) {
            return APIResponse.getOKJsonResult(new SearchUserListResponse(Collections.emptyList(), 0L));
        }
        UserInfo ui = this.uiMapper.selectByPrimaryKey(userId);
        if (ui == null) {
            return APIResponse.getOKJsonResult(new SearchUserListResponse(Collections.emptyList(), 0L));
        }
        UserSecurity us = this.usMapper.selectByPrimaryKey(userId);
        boolean isPass = this.checkParam(user, us, ui, request);
        if (isPass) {
            SearchUserListEx ex = this.compose(user, us, ui);
            return APIResponse.getOKJsonResult(this.createOneResponse(ex));
        } else {
            return APIResponse.getOKJsonResult(new SearchUserListResponse(Collections.emptyList(), 0L));
        }
    }

    private APIResponse<SearchUserListResponse> query(SearchUserListRequest request) {
        final List<Long> userIdInCommon = new ArrayList<>(10_000_000);
        String email = StringUtils.trimToEmpty(request.getEmail()).toLowerCase();
        List<String> emailList = request.getEmails();
        Long status = request.getStatus();
        Long mask = request.getMask();
        Date startInsertTime = request.getStartInsertTime();
        Date endInsertTime = request.getEndInsertTime();
        Boolean canEmailLike = false;
        if(request.getCanEmailLike() != null && request.getCanEmailLike()){
            canEmailLike = true;
        }
        boolean isAssign = false;
        if (StringUtils.isNotBlank(email) || NumberUtils.compare(status, 0L) != 0 || NumberUtils.compare(mask, 0L) != 0
                || startInsertTime != null || endInsertTime != null || org.apache.commons.collections4.CollectionUtils.isNotEmpty(emailList)) {
            this.selectUserIdFromUser(userIdInCommon, email, status, mask, startInsertTime, endInsertTime, emailList, canEmailLike);
            isAssign = true;
        }
        String mobileCode = request.getMobileCode();
        if (StringUtils.isNotBlank(mobileCode)) {
            this.selectUserIdFromUserSecurity(userIdInCommon, mobileCode, isAssign);
        }
        String remark = request.getRemark();
        if (StringUtils.isNotBlank(remark)) {
            this.selectUserIdFromUserInfo(userIdInCommon, remark, isAssign);
        }
        List<Long> tagIds = request.getTagIds();
        if (!CollectionUtils.isEmpty(tagIds)) {
            this.selectUserIdFromTag(userIdInCommon, tagIds, isAssign);
        }
        return this.createMultiResponse(userIdInCommon, request.getOffset(), request.getLimit());
    }

    private void selectUserIdFromUser(final List<Long> userIdInCommon, String email, Long status, Long mask,
            Date startInsertTime, Date endInsertTime, List<String> emailList, boolean canEmailLike) {
        if (isForkJoin) {
            Map<String, Object> param = new HashMap<>(5);
            param.put("email", email);
            param.put("canEmailLike", canEmailLike);
            param.put("emails", emailList);
            param.put("status", status);
            param.put("mask", mask);
            param.put("startInsertTime", startInsertTime);
            param.put("endInsertTime", endInsertTime);
            List<Long> userIds = this.forkQuery("user", param);
            userIdInCommon.addAll(userIds);
        } else {
            List<Long> userIds = this.userMapper.queryUserId(mask, status, email, startInsertTime, endInsertTime, emailList, canEmailLike);
            userIdInCommon.addAll(userIds);
        }
    }

    private void selectUserIdFromUserSecurity(final List<Long> userIdInCommon, String mobileCode, boolean isAssign) {
        List<Long> userIds;
        if (isForkJoin) {
            Map<String, Object> param = new HashMap<>(1);
            param.put("mobileCode", mobileCode);
            userIds = this.forkQuery("user_security", param);
        } else {
            userIds = this.usMapper.selectUserIdByMobileCode(mobileCode);
        }
        if (isAssign) {
            userIdInCommon.retainAll(userIds);
        } else {
            userIdInCommon.addAll(userIds);
        }
    }

    private void selectUserIdFromUserInfo(final List<Long> userIdInCommon, String remark, boolean isAssign) {
        List<Long> userIds;
        if (isForkJoin) {
            Map<String, Object> param = new HashMap<>(1);
            param.put("remark", remark);
            userIds = this.forkQuery("user_info", param);
        } else {
            userIds = this.uiMapper.fuzzyQueryByRemark(remark);
        }
        if (isAssign) {
            userIdInCommon.retainAll(userIds);
        } else {
            userIdInCommon.addAll(userIds);
        }
    }

    private void selectUserIdFromTag(final List<Long> userIdInCommon, List<Long> tagIds, boolean isAssign) {
        List<Long> userIds = this.tagIndicatorMapper.selectUserIdsByTagIds(tagIds);
        userIds = userIds.parallelStream().distinct().collect(Collectors.toList());
        if (isAssign) {
            userIdInCommon.retainAll(userIds);
        } else {
            userIdInCommon.addAll(userIds);
        }
    }

    private SearchUserListResponse createOneResponse(SearchUserListEx ex) {
        this.fillBindTagCount(Stream.of(ex).collect(Collectors.toList()));
        SearchUserListResponse response = new SearchUserListResponse();
        List<SearchUserListEx> list = new ArrayList<>(1);
        list.add(ex);
        response.setCount(1L);
        response.setSearchUserList(list);
        return response;
    }

    private APIResponse<SearchUserListResponse> createMultiResponse(List<Long> userIds, Integer offset, Integer limit) {
        if (CollectionUtils.isEmpty(userIds)) {
            return APIResponse.getOKJsonResult(new SearchUserListResponse(Collections.emptyList(), 0L));
        }
        Collections.sort(userIds);
        int total = userIds.size();
        if (offset < 0) {
            offset = 0;
        }
        if (offset > userIds.size()) {
            return APIResponse.getOKJsonResult(new SearchUserListResponse(Collections.emptyList(), (long) total));
        }
        if (limit < 0) {
            limit = 20;
        }
        if (total - offset - limit < limit) {
            userIds = userIds.subList(offset, total);
        } else {
            userIds = userIds.subList(offset, offset + limit);
        }
        List<SearchUserListEx> searchUserList = new ArrayList<>(limit);
        for (Long userId : userIds) {
            UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
            if(userIndex == null){
                log.warn("user_index 没有:{}", userId);
                continue;
            }
            User user = this.userMapper.queryByEmail(userIndex.getEmail());
            if(user == null){
                log.warn("user 没有:{}", userId);
                continue;
            }
            UserInfo ui = this.uiMapper.selectByPrimaryKey(userId);
            if(ui == null){
                log.warn("user_info 没有:{}", userId);
                continue;
            }
            UserSecurity us = this.usMapper.selectByPrimaryKey(userId);
            SearchUserListEx ex = this.compose(user, us, ui);
            searchUserList.add(ex);
        }
        this.fillBindTagCount(searchUserList);
        return APIResponse.getOKJsonResult(new SearchUserListResponse(searchUserList, (long) total));
    }

    private SearchUserListEx compose(User user, UserSecurity us, UserInfo ui) {
        SearchUserListEx ex = new SearchUserListEx();
        ex.setUserId(user.getUserId());
        ex.setEmail(user.getEmail());
        Country country = null;
        if (null != us && null != us.getMobileCode()) {
            country = this.iCountry.getCountryByCode(us.getMobileCode());
        }
        ex.setMobileCountry(null != country && null != country.getCn() ? country.getCn() : "");
        ex.setMobileCode(null != country && null != country.getMobileCode() ? country.getMobileCode() : "");
        ex.setMobile(null != us && null != us.getMobile() ? us.getMobile() : "");
        ex.setLoginFailedNum(null != us && null != us.getLoginFailedNum() ? us.getLoginFailedNum() : null);
        ex.setLoginFailedTime(null != us && null != us.getLoginFailedTime() ? us.getLoginFailedTime() : null);
        ex.setStatus(user.getStatus());
        ex.setInsertTime(user.getInsertTime());
        ex.setRemark(ui.getRemark());
        ex.setTradingAccount(ui.getTradingAccount());
        if (us != null) {
            ex.setWithdrawFaceStatus(us.getWithdrawSecurityFaceStatus());
        }
        return ex;
    }

    private boolean checkParam(User user, UserSecurity us, UserInfo ui, SearchUserListRequest request) {
        // 检查User表
        if (user == null) {
            return false;
        }
        if (request.getUserId() != null) {
            if (request.getUserId().compareTo(user.getUserId()) != 0) {
                return false;
            }
        }
        if (StringUtils.isNotBlank(request.getEmail())) {
            if (!user.getEmail().toLowerCase().contains(request.getEmail().toLowerCase())) {
                return false;
            }
        }
        //emails不为空且查到的用户email不在emails中，则返回false
        List<String> emails = request.getEmails();
        int num = 0;
        if (!CollectionUtils.isEmpty(emails)){
            for (String e : emails){
                if (user.getEmail().toLowerCase().contains(e.toLowerCase())){
                    num+=1;
                }
            }
            if (num == 0){
                return false;
            }
        }
        if (request.getStartInsertTime() != null) {
            if (request.getStartInsertTime().compareTo(user.getInsertTime()) > 0) {
                return false;
            }
        }
        if (request.getEndInsertTime() != null) {
            if (request.getEndInsertTime().compareTo(user.getInsertTime()) < 0) {
                return false;
            }
        }
        if ((user.getStatus() & request.getMask()) != request.getStatus()) {
            return false;
        }
        // 检查UserSecurity表
        if (us == null) {
            if (StringUtils.isNotBlank(request.getMobile()) || StringUtils.isNotBlank(request.getMobileCode())) {
                return false;
            }
        } else {
            if (StringUtils.isNotBlank(request.getMobile())) {
                if (!StringUtils.equalsIgnoreCase(request.getMobile(), us.getMobile())) {
                    return false;
                }
            }
            if (StringUtils.isNotBlank(request.getMobileCode())) {
                if (!StringUtils.equalsIgnoreCase(request.getMobileCode(), us.getMobileCode())) {
                    return false;
                }
            }
        }
        // 检查UserInfo表
        if (ui == null) {
            return false;
        }
        if (StringUtils.isNotBlank(request.getRemark())) {
            if (!ui.getRemark().toLowerCase().contains(request.getRemark().toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    private void fillBindTagCount(List<SearchUserListEx> exes) {
        List<String> userIds = new ArrayList<>();
        for(SearchUserListEx ex : exes){
            userIds.add(ex.getUserId()+"");
        }
        List<Map<String, Long>> countList = this.tagIndicatorMapper.countBindTagByUserId(userIds);
        Map<String, Long> countMap = new HashMap<>(countList.size());
        for(Map<String, Long> map : countList){
            countMap.put(map.get("userId")+"", Long.parseLong(map.get("total")+""));
        }
        for (SearchUserListEx ex : exes) {
            Long bindTagCount = countMap.get(ex.getUserId()+"");
            if(bindTagCount == null){
                ex.setBindTagCount(0L);
            }else{
                ex.setBindTagCount(bindTagCount);
            }
        }
    }

    private class QueryTask extends RecursiveTask<List<Long>> {

        private Map<String, Object> param;

        private List<String> tables = new ArrayList<>(20);

        QueryTask(String tableName, Map<String, Object> param) {
            for (String table : TABLE_SUFFIX_ARRAY) {
                tables.add(tableName + table);
            }
            this.param = param;
        }

        private QueryTask(List<String> tables, Map<String, Object> param) {
            this.tables = tables;
            this.param = param;
        }

        @Override
        protected List<Long> compute() {
            if (tables.size() <= 4) {
                List<Long> userIds = new ArrayList<>(1_000_000);
                for (String table : tables) {
                    if (table.startsWith("user_security")) {
                        userIds.addAll(userSearchMapper.queryUserSecurity(table, param));
                    } else if (table.startsWith("user_info")) {
                        userIds.addAll(userSearchMapper.queryUserInfo(table, param));
                    } else if (table.startsWith("user")) {
                        userIds.addAll(userSearchMapper.queryUser(table, param));
                    }
                }
                return userIds;
            }
            List<Long> userIds = new ArrayList<>(10_000_000);
            QueryTask task1 = new QueryTask(tables.subList(0, 4), param);
            QueryTask task2 = new QueryTask(tables.subList(4, 8), param);
            QueryTask task3 = new QueryTask(tables.subList(8, 12), param);
            QueryTask task4 = new QueryTask(tables.subList(12, 16), param);
            QueryTask task5 = new QueryTask(tables.subList(16, 20), param);
            invokeAll(task1, task2, task3, task4, task5);
            userIds.addAll(task1.join());
            userIds.addAll(task2.join());
            userIds.addAll(task3.join());
            userIds.addAll(task4.join());
            userIds.addAll(task5.join());
            return userIds;
        }
    }

    private List<Long> forkQuery(String table, Map<String, Object> param) {
        ForkJoinPool fjp = new ForkJoinPool(5);
        QueryTask task = new QueryTask(table, param);
        List<Long> userIds = fjp.invoke(task);
        return userIds;
    }

}
