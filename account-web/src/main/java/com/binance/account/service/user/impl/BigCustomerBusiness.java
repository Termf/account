package com.binance.account.service.user.impl;

import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.tag.TagIndicatorMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.user.IBigCustomer;
import com.binance.account.vo.tag.response.EmailAndUserIdVo;
import com.binance.account.vo.tag.response.StatusAndUserIdVo;
import com.binance.account.vo.user.request.GetUserListRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class BigCustomerBusiness implements IBigCustomer {

    @Autowired
    private UserIndexMapper userIndexMapper;

    @Autowired
    private TagIndicatorMapper tagIndicatorMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<EmailAndUserIdVo> emailAndTag(GetUserListRequest request) {
        List<UserIndex> userIndexList = userIndexMapper.selectByUserIds(request.getUserIds());
        List<Map<String, Object>> tagNames = tagIndicatorMapper.selectTagNameByUserIds(request.getUserIds());
        List<EmailAndUserIdVo> vos = new ArrayList<>();
        for(Long userId : request.getUserIds()){
            EmailAndUserIdVo vo = new EmailAndUserIdVo();
            vo.setUserId(userId);
            for(UserIndex userIndex : userIndexList){
                if(userId.compareTo(userIndex.getUserId()) == 0){
                    vo.setEmail(userIndex.getEmail());
                    break;
                }
            }
            List<String> tagNameList = new ArrayList<>();
            for(Map<String, Object> map : tagNames){
                Long userIdTag = Long.parseLong(map.get("userId")+"");
                if(userIdTag.compareTo(userId) == 0){
                    tagNameList.add(map.get("tagName")+"");
                }
            }
            vo.setTagNames(tagNameList);
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public List<StatusAndUserIdVo> findUserStatus(GetUserListRequest request) {
        List<Long> userIds = request.getUserIds();
        if(CollectionUtils.isEmpty(userIds)){
            return Collections.emptyList();
        }
        List<User> users = userMapper.selectByUserIds(userIds);
        List<StatusAndUserIdVo> vos = new ArrayList<>(userIds.size());
        for(User user : users){
            StatusAndUserIdVo vo = new StatusAndUserIdVo();
            vo.setUserId(user.getUserId());
            vo.setStatus(user.getStatus());
            vos.add(vo);
        }
        return vos;
    }

}
