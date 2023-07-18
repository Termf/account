package com.binance.account.service.tag.impl;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.binance.account.data.entity.tag.TagIndicatorDetail;
import com.binance.account.data.mapper.tag.TagIndicatorDetailMapper;
import com.binance.account.vo.tag.request.SaveBatchAddUserTag;
import com.binance.account.vo.tag.request.YubikeyMarketingRequest;
import com.binance.account.vo.tag.response.TagUserStatusVo;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.account.vo.user.request.GetUserListRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.javasimon.aop.Monitored;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.binance.account.common.query.SearchResult;
import com.binance.account.data.entity.tag.FullTagInfo;
import com.binance.account.data.entity.tag.TagCategory;
import com.binance.account.data.entity.tag.TagDetailDefine;
import com.binance.account.data.entity.tag.TagIndicator;
import com.binance.account.data.entity.tag.TagInfo;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.tag.TagCategoryMapper;
import com.binance.account.data.mapper.tag.TagDetailDefineMapper;
import com.binance.account.data.mapper.tag.TagIndicatorMapper;
import com.binance.account.data.mapper.tag.TagInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.domain.bo.ComboTree;
import com.binance.account.service.tag.ITagIndicator;
import com.binance.account.vo.tag.TagDetailValueVo;
import com.binance.account.vo.tag.TagDetailVo;
import com.binance.account.vo.tag.request.TagIndicatorConditionPageRequest;
import com.binance.account.vo.tag.request.TagIndicatorQueryRequest;
import com.binance.account.vo.tag.response.TagIndicatorResponse;
import com.binance.account.vo.tag.response.TagInfoIndicatorResponse;
import com.binance.account.vo.tag.response.TagInfoResponse;
import com.binance.account.vo.tag.response.TagResponse;
import com.binance.master.models.APIResponse;

import lombok.extern.log4j.Log4j2;

/**
 * @author lufei
 * @date 2018/5/8
 */
@Log4j2
@Service
public class TagIndicatorBusiness implements ITagIndicator {

    @Resource
    private TagIndicatorMapper tagIndicatorMapper;
    @Resource
    private TagInfoMapper tagInfoMapper;
    @Resource
    private TagCategoryMapper tagCategoryMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private TagDetailDefineMapper defineMapper;
    @Resource
    private TagIndicatorDetailMapper detailMapper;

    @Value("${tag.yubikey:25}")
    private Long yubikey;

    private String yubikeyValue = "Yubikey";

    @Value("${tag.yubikey.YubikeyMarketing:88}")
    private Long yubikeyMarketing;

    private String yubikeyMarketingValue = "Yubikey Marketing";

    @Override
    public APIResponse<TagResponse> saveIndicator(String user, String userId, String tagId, String remark, String value,
            List<TagDetailValueVo> detail) {
        TagIndicator model = this.tagIndicatorMapper.selectByTagIdAndUserId(userId, Long.parseLong(tagId));
        if (model != null) {
            return APIResponse.getOKJsonResult(new TagResponse("标签关系已存在"));
        }
        FullTagInfo info = this.tagInfoMapper.selectFullById(tagId);
        List<TagIndicator> models = new ArrayList<>(1);
        model = new TagIndicator();
        model.setUserId(userId);
        model.setTagId(info.getId());
        model.setValue(value.trim());
        model.setCategoryName(info.getCategoryName());
        model.setTagName(info.getName());
        model.setRemark(remark);
        model.setLastUpdatedBy(user);
        models.add(model);
        this.tagIndicatorMapper.insertbatch(models);

        this.saveOrUpdate(model.getId(), detail);
        return APIResponse.getOKJsonResult(new TagResponse(true, model.getId()));
    }

    @Override
    public APIResponse<TagResponse> modifyIndicator(String user, String id, String tagId, String remark, String value,
            List<TagDetailValueVo> detail) {
        TagIndicator model = this.tagIndicatorMapper.selectOneById(Long.parseLong(id));
        if (model == null) {
            return APIResponse.getOKJsonResult(new TagResponse("数据不存在"));
        }
        TagInfo tagInfo = this.tagInfoMapper.selectFullById(tagId);
        if (tagInfo == null) {
            return APIResponse.getOKJsonResult(new TagResponse("数据不存在"));
        }
        model.setCategoryName(((FullTagInfo) tagInfo).getCategoryName());
        model.setTagId(Long.parseLong(tagId));
        model.setTagName(tagInfo.getName());
        model.setValue(value);
        model.setRemark(remark);
        model.setLastUpdatedBy(user);
        this.tagIndicatorMapper.updateByPrimaryKey(model);

        this.saveOrUpdate(model.getId(), detail);
        return APIResponse.getOKJsonResult(new TagResponse(true, model.getId()));
    }

    @Override
    public APIResponse<TagResponse> removeIndicator(String id) {
        this.tagIndicatorMapper.deleteByPrimaryKey(id);
        return APIResponse.getOKJsonResult(new TagResponse(true, Long.parseLong(id)));
    }

    @Override
    public APIResponse<TagInfoResponse> getTagInfoById(String id) {
        FullTagInfo model = tagInfoMapper.selectFullById(id);
        if (model == null) {
            return APIResponse.getErrorJsonResult("查询不到数据");
        }
        if (model.getPCategoryId() != null) {
            TagCategory categoryModel = this.tagCategoryMapper.selectByPrimaryKey(model.getPCategoryId() + "");
            model.setPCategoryName(categoryModel.getName());
        }
        TagInfoResponse response = new TagInfoResponse();
        BeanUtils.copyProperties(model, response);
        TagDetailDefine detailDefine = defineMapper.selectByTagId(Long.parseLong(id));
        if (detailDefine == null) {
            response.setTagDetail(Collections.emptyList());
        } else {
            List<TagDetailVo> detailVos = tagDetailDefineToDetails(detailDefine);
            response.setTagDetail(detailVos);
        }
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<SearchResult<TagIndicatorResponse>> getIndicatorList(TagIndicatorQueryRequest request) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", request.getUserId());
        param.put("tagName", request.getTagName());
        param.put("categoryName", request.getCategoryName());
        param.put("tagValue", request.getTagValue());
        param.put("tagMinValue", request.getTagMinValue());
        param.put("tagMaxValue", request.getTagMaxValue());
        param.put("tagRemark", request.getTagRemark());
        param.put("position", request.getPosition());
        param.put("size", request.getSize());
        SearchResult<TagIndicatorResponse> searchResult = new SearchResult<>();
        if (StringUtils.equals("1", request.getTagEachChild()) && StringUtils.isNotBlank(request.getTagName())) {
            Set<Long> tagIdSet = new HashSet<>(100);
            List<TagInfo> tagInfos = tagInfoMapper.selectByName(request.getTagName());
            for (TagInfo tagInfo : tagInfos) {
                List<Long> tagIds = this.findChildTagInfoId(tagInfo.getId()).getData();
                for (Long id : tagIds) {
                    tagIdSet.add(id);
                }
            }
            param.put("tagIds", tagIdSet.stream().collect(Collectors.toList()));
            param.put("tagName", null);
        }
        searchResult.setRows(this.selectByCondition(param).getData());
        searchResult.setTotal(this.countByCondition(param));
        return APIResponse.getOKJsonResult(searchResult);
    }

    @Override
    public APIResponse<SearchResult<TagInfoIndicatorResponse>> getTagInfoByUserid(String tagUserid, Integer position,
            Integer size) {
        Map<String, Object> param = new HashMap<>(3);
        param.put("userid", tagUserid);
        param.put("position", position);
        param.put("size", size);
        SearchResult<TagInfoIndicatorResponse> searchResult = new SearchResult<>();
        List<TagInfoIndicatorResponse> rows = this.getTagInfoByUserid(param);
        searchResult.setRows(rows);
        searchResult.setTotal(this.getCountTagInfoByUserid(param));
        return APIResponse.getOKJsonResult(searchResult);
    }

    @Override
    public APIResponse<List<TagInfoResponse>> selectByName(String tagName) {
        List<TagInfo> infos = tagInfoMapper.selectByName(tagName);
        if (CollectionUtils.isEmpty(infos)) {
            return APIResponse.getErrorJsonResult("查询不到数据");
        }
        List<TagInfoResponse> responses = new ArrayList<>(infos.size());
        for (TagInfo info : infos) {
            TagInfoResponse response = new TagInfoResponse();
            BeanUtils.copyProperties(info, response);
            responses.add(response);
        }
        return APIResponse.getOKJsonResult(responses);
    }

    @Override
    public List<TagDetailVo> tagDetailDefineToDetails(TagDetailDefine detailDefine) {
        if (detailDefine == null) {
            return Collections.emptyList();
        }
        List<TagDetailVo> detailVos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String methodPrefix = "getF" + i;
            Method attrMethod = BeanUtils.findMethod(TagDetailDefine.class, methodPrefix + "Name");
            Method typeMethod = BeanUtils.findMethod(TagDetailDefine.class, methodPrefix + "Type");
            Method rangeMethod = BeanUtils.findMethod(TagDetailDefine.class, methodPrefix + "Range");
            Method mustMethod = BeanUtils.findMethod(TagDetailDefine.class, methodPrefix + "Must");
            try {
                Object attr = attrMethod.invoke(detailDefine);
                Object type = typeMethod.invoke(detailDefine);
                Object range = rangeMethod.invoke(detailDefine);
                Object must = mustMethod.invoke(detailDefine);
                if (attr == null || type == null || must == null) {
                    break;
                }
                String attrStr = attr + "";
                String typeStr = type + "";
                String rangeStr = "";
                String mustStr = must + "";
                if (range != null) {
                    rangeStr = range + "";
                }
                detailVos.add(new TagDetailVo(attrStr, typeStr, rangeStr, mustStr));
            } catch (Exception e) {
                log.error("tag detail define set value exception: ", e);
            }
        }
        return detailVos;
    }

    @Override
    @Monitored
    public List<TagUserStatusVo> selectTagAndUserStatus(GetUserListRequest request) {
        List<Long> userIds = request.getUserIds();
        if(CollectionUtils.isEmpty(userIds)){
            return Collections.emptyList();
        }
        List<Map<String, Object>> tags = tagIndicatorMapper.selectTagNameByUserIds(userIds);
        Map<Long, String> userIdTagNameMap = new HashMap<>(userIds.size());
        for(Map<String, Object> map : tags){
            if(map.get("tagName") == null){
                continue;
            }
            Long userId = Long.parseLong(map.get("userId")+"");
            String tagName = userIdTagNameMap.get(userId);
            if(tagName == null){
                userIdTagNameMap.put(userId, map.get("tagName") + "");
            }else{
                userIdTagNameMap.put(userId, tagName + "," +map.get("tagName") + "");
            }
        }
        List<User> users = this.userMapper.selectByUserIds(userIds);
        List<TagUserStatusVo> vos = new ArrayList<>(userIds.size());
        for(Long userId : userIds){
            for(User user : users){
                if(NumberUtils.compare(userId, user.getUserId()) == 0){
                    TagUserStatusVo vo = new TagUserStatusVo();
                    vo.setUserId(userId);
                    vo.setTagName(userIdTagNameMap.get(userId));
                    vo.setUserStatusEx(new UserStatusEx(user.getStatus()));
                    vos.add(vo);
                    break;
                }
            }
        }
        return vos;
    }

    @Override
    public boolean addUsersUnderYubikey(List<Long> userIds) {
        if(CollectionUtils.isEmpty(userIds)){
            return true;
        }
        List<Map<String, Long>> list = new ArrayList<>(userIds.size());
        for(Long userId : userIds){
            Map<String, Long> map = new HashMap<>();
            map.put("userId", userId);
            map.put("tagId", yubikeyMarketing);
            list.add(map);
        }
        List<Long> existUserIds = tagIndicatorMapper.selectByUserIdsAndTagIds(list);
        userIds.removeAll(existUserIds);
        List<TagIndicator> models = new ArrayList<>(userIds.size());
        for(Long userId : userIds){
            TagIndicator model = new TagIndicator();
            model.setUserId(userId + "");
            model.setTagId(yubikeyMarketing);
            model.setValue(yubikeyMarketingValue);
            model.setCategoryName(yubikeyValue);
            model.setTagName(yubikeyMarketingValue);
            model.setRemark("yubikey marketing活动");
            model.setLastUpdatedBy("admin");
            models.add(model);
        }
        if(CollectionUtils.isNotEmpty(models)){
            this.tagIndicatorMapper.insertbatch(models);
        }
        return true;
    }

    @Override
    public boolean isUserIdMatchTag(YubikeyMarketingRequest request) {
        log.info("yubikey marketing match tag:{}", request);
        List<TagInfo> tagInfos = tagInfoMapper.selectSimpleByCategoryId(yubikey);
        String tagInfoName = request.getTagName().trim();
        Long matchTagId = null;
        for(TagInfo tagInfo : tagInfos){
            if(StringUtils.equalsIgnoreCase(tagInfoName, tagInfo.getName())){
                matchTagId = tagInfo.getId();
                break;
            }
        }
        if(matchTagId == null){
            log.warn("yubikey marketing has no tag, userId:{}", request.getUserId());
            return false;
        }
        TagIndicator tagIndicator = tagIndicatorMapper.selectByTagIdAndUserId(request.getUserId() + "", matchTagId);
        return tagIndicator != null;
    }

    @Override
    public List<SaveBatchAddUserTag> batchSaveIndicator(List<SaveBatchAddUserTag> tags) {
        List<SaveBatchAddUserTag> corrResults = new ArrayList<>();

        for(SaveBatchAddUserTag tag:tags){
            TagIndicator model = new TagIndicator();
            BeanUtils.copyProperties(tag,model);
            int result = this.tagIndicatorMapper.insert(model);
            log.info("batch create tagdaliy result. {} ",result);
            if(result == 1){
                corrResults.add(tag);
            }
        }
        return corrResults;
    }

    @Override
    public List<String> batchDeleteIndicator(Map<String,Long> uIdAndTags) {
        for(Map.Entry<String,Long> uIdAndTag : uIdAndTags.entrySet()){
            String uid = uIdAndTag.getKey();
            Long tagId = uIdAndTag.getValue();
            this.tagIndicatorMapper.deleteByUIdAndTagId(uid,tagId);
        }
        return null;
    }

    private List<TagInfoIndicatorResponse> getTagInfoByUserid(Map<String, Object> param) {
        if (StringUtils.isBlank(param.get("userid") + "") || StringUtils.equals("null", param.get("userid") + "")) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = this.tagIndicatorMapper.selectTagInfoByUserid(param);
        return this.toTagInfoIndicatorResponse(list);
    }

    private Long getCountTagInfoByUserid(Map<String, Object> param) {
        if (StringUtils.isBlank(param.get("userid") + "")) {
            return 0L;
        }
        return this.tagIndicatorMapper.countTagInfoByUserid(param);
    }

    private List<TagInfoIndicatorResponse> toTagInfoIndicatorResponse(List<Map<String, Object>> list) {
        List<TagInfoIndicatorResponse> responses = new ArrayList<>(list.size());
        for (Map<String, Object> map : list) {
            TagInfoIndicatorResponse response = new TagInfoIndicatorResponse();
            Object indicatorId = map.get("indicatorId");
            if (indicatorId != null) {
                response.setIndicatorId(new Long(indicatorId + ""));
            }
            Object categoryId = map.get("categoryId");
            if (categoryId != null) {
                response.setCategoryId(new Long(categoryId + ""));
            }
            Object categoryName = map.get("categoryName");
            if (categoryName != null) {
                response.setCategoryName((String) categoryName);
            }
            Object tagId = map.get("tagId");
            if (tagId != null) {
                response.setTagId(new Long(tagId + ""));
            }
            Object tagName = map.get("tagName");
            if (tagName != null) {
                response.setTagName((String) tagName);
            }
            Object minValue = map.get("minValue");
            if (minValue != null) {
                response.setMinValue((BigDecimal) minValue);
            }
            Object maxValue = map.get("maxValue");
            if (maxValue != null) {
                response.setMaxValue((BigDecimal) maxValue);
            }
            Object value = map.get("value");
            if (value != null) {
                response.setValue((String) value);
            }
            Object indicatorValue = map.get("indicatorValue");
            if (indicatorValue != null) {
                response.setIndicatorValue(indicatorValue + "");
            }
            if (value == null || StringUtils.isBlank(value + "")) {
                response.setValueRange(response.getMinValue() + " ~ " + response.getMaxValue());
            } else {
                response.setValueRange(response.getValue());
            }
            Object remark = map.get("remark");
            if (remark != null) {
                response.setRemark((String) remark);
            }
            Object lastUpdatedBy = map.get("lastUpdatedBy");
            if (lastUpdatedBy != null) {
                response.setLastUpdatedBy((String) lastUpdatedBy);
            }
            responses.add(response);
        }
        return responses;
    }

    @Override
    public APIResponse<List<Long>> findChildTagInfoId(Long id) {
        if (id == null) {
            return APIResponse.getOKJsonResult(Collections.emptyList());
        }
        List<ComboTree> children = findTreeInfoTreeRecursive(id, null);
        Set<Long> childrenIds = new HashSet<>(100);
        for (ComboTree tree : children) {
            childrenIds.addAll(getTagInfoChildIdFromComboTree(new HashSet<>(), tree));
        }
        childrenIds.add(id);
        return APIResponse.getOKJsonResult(childrenIds.stream().collect(Collectors.toList()));
    }

    @Override
    public APIResponse<List<TagIndicatorResponse>> selectByCondition(TagIndicatorConditionPageRequest param) {
        return this.selectByCondition(this.toTagIndicatorConditionPageMap(param));
    }

    @Override
    public APIResponse<Long> countByCondition(TagIndicatorConditionPageRequest param) {
        Long total = this.countByCondition(this.toTagIndicatorConditionPageMap(param));
        return APIResponse.getOKJsonResult(total);
    }

    @Override
    public APIResponse<TagIndicatorResponse> selectByTagIdAndUserId(String userId, Long tagId) {
        TagIndicator dbModel = this.tagIndicatorMapper.selectByTagIdAndUserId(userId, tagId);
        if (dbModel == null) {
            return APIResponse.getErrorJsonResult("查询不到数据");
        }
        TagIndicatorResponse response = new TagIndicatorResponse();
        BeanUtils.copyProperties(dbModel, response);
        return APIResponse.getOKJsonResult(response);
    }

    private List<ComboTree> findTreeInfoTreeRecursive(Long pid, List<ComboTree> trees) {
        List<TagInfo> roots = this.tagInfoMapper.selectByPid(pid);
        if (CollectionUtils.isEmpty(trees)) {
            trees = new ArrayList<>(roots.size());
        }
        if (roots.size() == 0) {
            return trees;
        }
        for (TagInfo root : roots) {
            ComboTree tree = new ComboTree(root.getId(), root.getName(), "closed");
            List<ComboTree> children = this.findTreeInfoTreeRecursive(tree.getId(), null);
            if (children.size() > 0) {
                tree.setChildren(children);
            } else {
                tree.setState("open");
            }
            trees.add(tree);
        }
        return trees;
    }

    private Set<Long> getTagInfoChildIdFromComboTree(Set<Long> childrenTagInfoIds, ComboTree tree) {
        childrenTagInfoIds.add(tree.getId());
        List<ComboTree> trees = tree.getChildren();
        if (CollectionUtils.isNotEmpty(trees)) {
            for (ComboTree t : trees) {
                this.getTagInfoChildIdFromComboTree(childrenTagInfoIds, t);
            }
        }
        return childrenTagInfoIds;
    }

    private Map<String, Object> toTagIndicatorConditionPageMap(TagIndicatorConditionPageRequest request) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", request.getUserId());
        param.put("tagName", request.getTagName());
        param.put("categoryName", request.getCategoryName());
        param.put("tagValue", request.getTagValue());
        param.put("tagMinValue", request.getTagMinValue());
        param.put("tagMaxValue", request.getTagMaxValue());
        param.put("tagRemark", request.getTagRemark());
        param.put("position", request.getPosition());
        param.put("size", request.getSize());
        return param;
    }

    private APIResponse<List<TagIndicatorResponse>> selectByCondition(Map<String, Object> param) {
        List<TagIndicator> models = this.tagIndicatorMapper.selectByCondition(param);
        List<Long> userIds = new ArrayList<>(models.size());
        List<TagIndicatorResponse> responses = new ArrayList<>(models.size());
        for (TagIndicator model : models) {
            userIds.add(new Long(model.getUserId()));
        }
        if (CollectionUtils.isNotEmpty(userIds)) {
            List<User> users = this.userMapper.selectByUserIds(userIds);
            for (TagIndicator model : models) {
                for (User u : users) {
                    if (StringUtils.equals(model.getUserId(), u.getUserId() + "")) {
                        TagIndicatorResponse response = new TagIndicatorResponse();
                        response.setId(model.getId());
                        response.setUserId(model.getUserId());
                        response.setTagId(model.getTagId());
                        response.setCategoryName(model.getCategoryName());
                        response.setTagName(model.getTagName());
                        response.setValue(model.getValue());
                        response.setRemark(model.getRemark());
                        response.setLastUpdatedBy(model.getLastUpdatedBy());
                        response.setUpdateTime(model.getUpdateTime());
                        response.setEmail(u.getEmail());
                        responses.add(response);
                        continue;
                    }
                }
            }
        }
        return APIResponse.getOKJsonResult(responses);
    }

    private Long countByCondition(Map<String, Object> param) {
        return this.tagIndicatorMapper.countByCondition(param);
    }

    private void saveOrUpdate(Long indicatorId, List<TagDetailValueVo> detail) {
        this.detailMapper.deleteByIndicatorId(indicatorId);
        if(detail == null){
            return;
        }
        for(TagDetailValueVo vo : detail){
            TagIndicatorDetail model = new TagIndicatorDetail();
            model.setIndicatorId(indicatorId);
            model.setF0(vo.getF0());
            model.setF1(vo.getF1());
            model.setF2(vo.getF2());
            model.setF3(vo.getF3());
            model.setF4(vo.getF4());
            model.setF5(vo.getF5());
            model.setF6(vo.getF6());
            model.setF7(vo.getF7());
            model.setF8(vo.getF8());
            model.setF9(vo.getF9());
            this.detailMapper.insert(model);
        }
    }

}
