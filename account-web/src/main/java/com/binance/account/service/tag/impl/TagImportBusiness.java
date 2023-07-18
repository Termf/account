package com.binance.account.service.tag.impl;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.binance.account.data.entity.tag.TagDetailDefine;
import com.binance.account.data.entity.tag.TagIndicator;
import com.binance.account.data.entity.tag.TagIndicatorDetail;
import com.binance.account.data.entity.tag.TagInfo;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.tag.TagCategoryMapper;
import com.binance.account.data.mapper.tag.TagDetailDefineMapper;
import com.binance.account.data.mapper.tag.TagIndicatorDetailMapper;
import com.binance.account.data.mapper.tag.TagIndicatorMapper;
import com.binance.account.data.mapper.tag.TagInfoMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.service.tag.ITagImport;
import com.binance.account.service.tag.ITagIndicator;
import com.binance.account.vo.tag.TagDetailVo;
import com.binance.account.vo.tag.TagImportVo;
import com.binance.account.vo.tag.response.TagResponse;
import com.binance.master.models.APIResponse;

import lombok.extern.log4j.Log4j2;

/**
 * @author lufei
 * @date 2018/5/7
 */
@Log4j2
@Service
public class TagImportBusiness implements ITagImport {

    @Resource
    private TagCategoryMapper tagCategoryMapper;
    @Resource
    private TagInfoMapper tagInfoMapper;
    @Resource
    private TagIndicatorMapper tagIndicatorMapper;
    @Resource
    private UserIndexMapper userIndexMapper;
    @Resource
    private TagDetailDefineMapper defineMapper;
    @Resource
    private ITagIndicator tagIndicator;
    @Resource
    private TagIndicatorDetailMapper detailMapper;

    @Override
    public APIResponse<TagResponse> upload(String user, String xId, String type, List<TagImportVo> list) {
        String categoryName = null;
        List<TagInfo> infos = new ArrayList<>();
        if (StringUtils.equals(type, "tag")) {
            Set<String> set = list.stream().map(v -> v.getTagName()).collect(Collectors.toSet());
            if (set.size() > 1) {
                return APIResponse.getOKJsonResult(new TagResponse("一次只能导入一种标签"));
            }
            TagInfo info = this.tagInfoMapper.selectByPrimaryKey(xId);
            categoryName = this.tagCategoryMapper.selectByPrimaryKey(info.getCategoryId() + "").getName();
            infos.add(info);
        } else if (StringUtils.equals(type, "category")) {
            categoryName = this.tagCategoryMapper.selectByPrimaryKey(xId).getName();
            infos = this.tagInfoMapper.selectSimpleByCategoryId(Long.parseLong(xId));
        } else {
            return APIResponse.getOKJsonResult(new TagResponse(0));
        }
        list = this.removeInvalidTag(list, infos);
        list = this.removeInvalidUserId(list);
        if (StringUtils.equals(type, "tag")) {
            list = this.removeInvalidTagDetail(list, infos.get(0));
        }
        List<TagIndicator> indicators = this.mergeSameTag(list, categoryName, user);
        for (TagIndicator indicator : indicators) {
            TagIndicator model =
                    this.tagIndicatorMapper.selectByTagIdAndUserId(indicator.getUserId(), indicator.getTagId());
            Long indicatorId = null;
            if (model != null) {
                log.info("标签导入，用户{}已关联{}标签", indicator.getUserId(), indicator.getTagId());
                model.setValue(indicator.getValue());
                model.setRemark(indicator.getRemark());
                model.setLastUpdatedBy(indicator.getLastUpdatedBy());
                this.tagIndicatorMapper.updateByPrimaryKey(model);
                indicatorId = model.getId();
            } else {
                this.tagIndicatorMapper.insert(indicator);
                indicatorId = indicator.getId();
            }
            if (!StringUtils.equals(type, "tag")) {
                continue;
            }
            List<TagIndicatorDetail> details = indicator.getDetails();
            if (CollectionUtils.isEmpty(details)) {
                continue;
            }
            for (TagIndicatorDetail detail : details) {
                detail.setIndicatorId(indicatorId);
                detailMapper.insert(detail);
            }
        }
        return APIResponse.getOKJsonResult(new TagResponse(indicators.size()));
    }

    /**
     * 去除标签组和标签不匹配的 去除标签值和数据库对应不上的 加入 tid, 更新 tagName
     */
    private List<TagImportVo> removeInvalidTag(List<TagImportVo> vos, List<TagInfo> infos) {
        Iterator<TagImportVo> iterator = vos.iterator();
        while (iterator.hasNext()) {
            TagImportVo next = iterator.next();
            try {
                String userId = new BigDecimal(next.getUserId().trim()).longValue() + "";
                next.setUserId(userId);
            } catch (Exception e) {
                iterator.remove();
            }
            String tagName = null;
            if (StringUtils.isNotBlank(next.getTagName())) {
                tagName = next.getTagName().trim();
            }
            String value = null;
            if (StringUtils.isNotBlank(next.getValue())) {
                value = next.getValue().trim();
            }
            if (StringUtils.isBlank(value)) {
                value = next.getTagName();
            }
            List<TagInfo> suits = lookSuitTagInfo(infos, tagName, value);
            if (suits.size() != 1) {
                log.warn("标签导入，用户[{}]没有合适的标签", next.getUserId());
                iterator.remove();
                continue;
            }
            next.setTid(suits.get(0).getId() + "");
            next.setTagName(suits.get(0).getName() + "");
        }
        return vos;
    }

    private List<TagImportVo> removeInvalidTagDetail(List<TagImportVo> list, TagInfo tagInfo) {
        TagDetailDefine define = defineMapper.selectByTagId(tagInfo.getId());
        Map<String, Object>[] maps = this.defineToMap(define);
        Iterator<TagImportVo> iterator = list.iterator();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        boolean isLegal;
        while (iterator.hasNext()) {
            TagImportVo next = iterator.next();
            isLegal = this.isLegalTagDetail(next, maps, sdf);
            if (!isLegal) {
                iterator.remove();
            }
        }
        return list;
    }

    private List<TagImportVo> removeInvalidUserId(List<TagImportVo> list) {
        int i = 0, itemSize = 500, maxSize = list.size();
        List<Long> invalidUserIds = new ArrayList<>(itemSize);
        while (true) {
            if (i + itemSize < maxSize) {
                invalidUserIds.addAll(this.invalidUserId(list.subList(i, i + itemSize)));
            } else {
                invalidUserIds.addAll(this.invalidUserId(list.subList(i, maxSize)));
                break;
            }
            i = i + itemSize;
        }
        Iterator<TagImportVo> iterator = list.iterator();
        while (iterator.hasNext()) {
            TagImportVo next = iterator.next();
            if (invalidUserIds.contains(next.getUserId())) {
                iterator.remove();
            }
        }
        return list;
    }

    /**
     * 合并相同的标签和用户关系，一个关系可以有多个详情
     * 
     * @param list
     * @param categoryName
     * @param user
     * @return
     */
    private List<TagIndicator> mergeSameTag(List<TagImportVo> list, String categoryName, String user) {
        Map<String, TagIndicator> map = new HashMap<>(list.size());
        for (TagImportVo vo : list) {
            String key = vo.getUserId() + "." + vo.getTid();
            TagIndicator indicator = map.get(key);
            if (indicator == null) {
                indicator = new TagIndicator();
                indicator.setUserId(vo.getUserId());
                indicator.setTagId(Long.parseLong(vo.getTid()));
                indicator.setValue(vo.getValue());
                indicator.setCategoryName(categoryName);
                indicator.setTagName(vo.getTagName());
                indicator.setRemark(vo.getRemark());
                indicator.setLastUpdatedBy(user);
                map.put(key, indicator);
            }
            TagIndicatorDetail detail = new TagIndicatorDetail();
            detail.setF0(vo.getF0());
            detail.setF1(vo.getF1());
            detail.setF2(vo.getF2());
            detail.setF3(vo.getF3());
            detail.setF4(vo.getF4());
            detail.setF5(vo.getF5());
            detail.setF6(vo.getF6());
            detail.setF7(vo.getF7());
            detail.setF8(vo.getF8());
            detail.setF9(vo.getF9());
            indicator.getDetails().add(detail);
        }
        return map.values().stream().collect(Collectors.toList());
    }

    private List<Long> invalidUserId(List<TagImportVo> list) {
        List<Long> userIds = null;
        List<UserIndex> userIndices = null;
        if(CollectionUtils.isEmpty(list)){
            userIds = Collections.emptyList();
            userIndices = Collections.emptyList();
        }else{
            userIds = list.stream().map(k -> Long.parseLong(k.getUserId())).collect(Collectors.toList());
            userIndices = this.userIndexMapper.selectByUserIds(userIds);
        }
        List<Long> remote = userIndices.stream().map(k -> k.getUserId()).collect(Collectors.toList());
        List<Long> retain = new ArrayList<>(userIds);
        retain.retainAll(remote);
        userIds.removeAll(retain);
        return userIds;
    }

    /**
     * 合法的标签名和值，只会找到一个合适的标签
     */
    private List<TagInfo> lookSuitTagInfo(List<TagInfo> infos, String tagName, String value) {
        List<TagInfo> suits = new ArrayList<>();
        for (TagInfo info : infos) {
            if (!StringUtils.equalsIgnoreCase(info.getName(), tagName)) {
                continue;
            }
            boolean isValueSuit = this.isValueSuit(info, value);
            if (!isValueSuit) {
                continue;
            }
            suits.add(info);
            break;
        }
        return suits;
    }

    private boolean isValueSuit(TagInfo info, String value) {
        if (StringUtils.isBlank(info.getValue())) {
            if (!NumberUtils.isParsable(value)) {
                return false;
            }
            BigDecimal val = new BigDecimal(value);
            if (val.compareTo(info.getMin()) < 0 || val.compareTo(info.getMax()) > 0) {
                return false;
            } else {
                return true;
            }
        } else {
            if (StringUtils.equals(info.getValue(), value)) {
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean isLegalTagDetail(TagImportVo vo, Map<String, Object>[] maps, SimpleDateFormat sdf) {
        try {
            for (int i = 0; i < maps.length; i++) {
                String name = (String) maps[i].get("f" + i + "Name");
                String type = (String) maps[i].get("f" + i + "Type");
                Set<String> range = (Set) maps[i].get("f" + i + "Range");
                String must = (String) maps[i].get("f" + i + "Must");
                if (StringUtils.isBlank(name)) {
                    break;
                }
                Method method = BeanUtils.findMethod(TagImportVo.class, "getF" + i);
                Object obj = method.invoke(vo);
                String value = null;
                if (obj != null) {
                    value = (String) obj;
                }
                if (StringUtils.equalsIgnoreCase("time", type) && StringUtils.isNotBlank(value)) {
                    sdf.parse(value);
                    continue;
                }
                if (StringUtils.equalsIgnoreCase("number", type) && StringUtils.isNotBlank(value)) {
                    if (!NumberUtils.isParsable(value)) {
                        log.warn("标签导入[t:{}-u:{}]:交易标签详情-数字:", vo.getTid(), vo.getUserId());
                        return false;
                    }
                    continue;
                }
                if (StringUtils.equalsIgnoreCase("range", type) && StringUtils.isNotBlank(value)) {
                    if (!range.contains(value)) {
                        log.warn("标签导入[t:{}-u:{}]:交易标签详情-范围:", vo.getTid(), vo.getUserId());
                        return false;
                    }
                    continue;
                }
                if (StringUtils.equalsIgnoreCase("y", must) && StringUtils.isBlank(value)) {
                    log.warn("标签导入[t:{}-u:{}]:交易标签详情-必填:", vo.getTid(), vo.getUserId());
                    return false;
                }
            }
            return true;
        } catch (ParseException e) {
            log.warn("标签导入[t:{}-u:{}]:交易标签详情-时间:", vo.getTid(), vo.getUserId(), e);
            return false;
        } catch (Exception e) {
            log.warn("标签导入[t:{}-u:{}]:交易标签详情-校验异常:", vo.getTid(), vo.getUserId(), e);
            return false;
        }
    }

    private Map<String, Object>[] defineToMap(TagDetailDefine define) {
        List<TagDetailVo> vos = tagIndicator.tagDetailDefineToDetails(define);
        Map<String, Object>[] maps = new HashMap[vos.size()];
        int i = 0;
        for (TagDetailVo vo : vos) {
            String name = vo.getAttr();
            String type = vo.getType();
            String range = vo.getRange();
            String must = vo.getMust();
            Map<String, Object> map = new HashMap<>();
            map.put("f" + i + "Name", name);
            map.put("f" + i + "Type", type);
            if (StringUtils.isNotBlank(range)) {
                map.put("f" + i + "Range", Stream.of(range.split(",")).collect(Collectors.toSet()));
            } else {
                map.put("f" + i + "Range", Collections.emptySet());
            }
            map.put("f" + i + "Must", must);
            maps[i++] = map;
        }
        return maps;
    }

}
