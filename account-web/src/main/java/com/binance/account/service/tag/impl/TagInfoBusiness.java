package com.binance.account.service.tag.impl;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.binance.account.data.mapper.tag.TagDetailDefineMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.binance.account.common.query.SearchResult;
import com.binance.account.data.entity.tag.TagDetailDefine;
import com.binance.account.data.entity.tag.TagInfo;
import com.binance.account.data.mapper.tag.TagInfoMapper;
import com.binance.account.service.tag.ITagInfo;
import com.binance.account.vo.tag.TagDetailVo;
import com.binance.account.vo.tag.request.TagInfoEntityRequest;
import com.binance.account.vo.tag.request.TagPageRequest;
import com.binance.account.vo.tag.response.TagInfoResponse;
import com.binance.account.vo.tag.response.TagResponse;
import com.binance.master.models.APIResponse;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class TagInfoBusiness implements ITagInfo {

    @Resource
    private TagInfoMapper tagInfoMapper;
    @Resource
    private TagDetailDefineMapper defineMapper;

    @Override
    public APIResponse<TagResponse> saveTag(TagInfoEntityRequest request) {
        TagInfo model = new TagInfo();
        return this.saveOrUpdateTag(request, model);
    }

    @Override
    public APIResponse<TagResponse> modifyTag(TagInfoEntityRequest request) {
        TagInfo model = this.tagInfoMapper.selectByPrimaryKey(request.getTagId());
        return this.saveOrUpdateTag(request, model);
    }

    @Override
    public APIResponse<TagResponse> removeTag(String id) {
        tagInfoMapper.deleteByPrimaryKey(id);
        defineMapper.deleteByTagId(Long.parseLong(id));
        return APIResponse.getOKJsonResult(new TagResponse(true, Long.parseLong(id)));
    }

    @Override
    public APIResponse<SearchResult<TagInfoResponse>> getTagList(TagPageRequest request) throws Exception {
        SearchResult<TagInfoResponse> searchResult = new SearchResult<>();
        String xId = request.getXId();
        if (StringUtils.equals(request.getTagType(), "tag")) {
            TagInfo model = tagInfoMapper.selectByPrimaryKey(xId);
            if (model != null && model.getId() != null) {
                xId = model.getCategoryId() + "";
            }
        }
        if (StringUtils.isNotBlank(xId)) {
            List<TagInfo> infos = this.tagInfoMapper.selectByCategoryId(xId, request.getPosition(), request.getSize());
            List<TagInfoResponse> responses = new ArrayList<>(infos.size());
            for (TagInfo info : infos) {
                TagInfoResponse response = new TagInfoResponse();
                BeanUtils.copyProperties(info, response);
                responses.add(response);
            }
            searchResult.setRows(responses);
            searchResult.setTotal(this.tagInfoMapper.countByCategoryId(xId));
        }
        return APIResponse.getOKJsonResult(searchResult);
    }

    @Override
    public APIResponse<List<TagInfoResponse>> selectByCategoryNameAndPTagName(String categoryName, String pTagName) {
        List<TagInfo> infos = this.tagInfoMapper.selectByCategoryNameAndPTagName(categoryName, pTagName);
        List<TagInfoResponse> responses = new ArrayList<>(infos.size());
        for (TagInfo info : infos) {
            TagInfoResponse response = new TagInfoResponse();
            BeanUtils.copyProperties(info, response);
            responses.add(response);
        }
        return APIResponse.getOKJsonResult(responses);
    }

    private APIResponse<TagResponse> saveOrUpdateTag(TagInfoEntityRequest request, TagInfo model){
        List<TagDetailVo> detailVos = request.getTagDetail();
        if(CollectionUtils.isNotEmpty(detailVos)){
            for(TagDetailVo vo : detailVos){
                if(!StringUtils.equalsAnyIgnoreCase(vo.getMust(), "y", "n")
                        ||!StringUtils.equalsAnyIgnoreCase(vo.getType(), "time", "text", "number", "range")){
                    return APIResponse.getOKJsonResult(new TagResponse("输入不合法"));
                }
                if(StringUtils.isBlank(vo.getAttr())){
                    return APIResponse.getOKJsonResult(new TagResponse("属性不能为空"));
                }
            }
        }
        Object result = this.saveOrUpdateModel(request, model);
        if (result instanceof String) {
            return APIResponse.getOKJsonResult(new TagResponse((String) result));
        }
        if(model.getId() == null){
            this.tagInfoMapper.insert(model);
        }else{
            this.tagInfoMapper.updateByPrimaryKey(model);
        }
        this.saveOrUpdateTagDetailDefine(request.getTagDetail(), model.getId());
        return APIResponse.getOKJsonResult(new TagResponse(true, model.getId()));
    }

    private Object saveOrUpdateModel(TagInfoEntityRequest vo, TagInfo model) {
        Map<String, String> map = this.lookTagParent(vo.getTagParentTagName(), vo.getTagParentCategoryName());
        if (StringUtils.isNotBlank(map.get("error"))) {
            return map.get("error");
        }
        String result = this.checkRepeatName(vo.getTagCategoryId(), model.getId(), vo.getTagName());
        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        if (StringUtils.isBlank(map.get("pid"))) {
            model.setPid(null);
        } else {
            model.setPid(Long.parseLong(map.get("pid")));
        }
        model.setCategoryId(Long.parseLong(vo.getTagCategoryId()));
        model.setName(vo.getTagName());
        if (StringUtils.isNotBlank(vo.getTagMin())) {
            model.setMin(new BigDecimal(vo.getTagMin()));
        }
        if (StringUtils.isNotBlank(vo.getTagMax())) {
            model.setMax(new BigDecimal(vo.getTagMax()));
        }
        model.setValue(vo.getTagValue());
        if (model.getMin() == null && model.getMax() == null && StringUtils.isBlank(model.getValue())) {
            model.setValue(vo.getTagName());
        }
        return model;
    }

    private void saveOrUpdateTagDetailDefine(List<TagDetailVo> detailVos, Long tagId) {
        TagDetailDefine existDefine = defineMapper.selectByTagId(tagId);
        if (CollectionUtils.isEmpty(detailVos)) {
            if(existDefine != null){
                defineMapper.deleteByTagId(tagId);
            }
            return;
        }
        TagDetailDefine detailDefine = new TagDetailDefine();
        detailDefine.setTagId(tagId);
        for (int i = 0; i < detailVos.size(); i++) {
            String methodPrefix = "setF" + i;
            Method attrMethod = BeanUtils.findMethod(TagDetailDefine.class, methodPrefix + "Name", String.class);
            Method typeMethod = BeanUtils.findMethod(TagDetailDefine.class, methodPrefix + "Type", String.class);
            Method rangeMethod = BeanUtils.findMethod(TagDetailDefine.class, methodPrefix + "Range", String.class);
            Method mustMethod = BeanUtils.findMethod(TagDetailDefine.class, methodPrefix + "Must", String.class);
            try {
                attrMethod.invoke(detailDefine, detailVos.get(i).getAttr());
                typeMethod.invoke(detailDefine, detailVos.get(i).getType());
                rangeMethod.invoke(detailDefine, detailVos.get(i).getRange());
                mustMethod.invoke(detailDefine, detailVos.get(i).getMust());
            } catch (Exception e) {
                log.error("tag detail define set value exception: ", e);
            }
        }
        if(existDefine != null){
            defineMapper.deleteByTagId(tagId);
        }
        defineMapper.insert(detailDefine);
    }

    /**
     * @param parentTagName 父标签名称
     * @param parentCategoryName 父标签组名称
     * @return categoryId 标签组ID, parentTagId 父标签ID, parentCategoryId 父标签组ID, error 错误信息
     */
    private Map<String, String> lookTagParent(String parentTagName, String parentCategoryName) {
        Map<String, String> map = new HashMap<>(2);
        if (StringUtils.isNotBlank(parentTagName) && StringUtils.isNotBlank(parentCategoryName)) {
            List<TagInfo> parentInfos =
                    this.tagInfoMapper.selectByCategoryNameAndPTagName(parentCategoryName, parentTagName);
            if (CollectionUtils.isEmpty(parentInfos)) {
                map.put("error", "标签组和父标签组不匹配");
            }
            map.put("pid", parentInfos.get(0).getId() + "");
        }
        return map;
    }

    /**
     * 同一个标签组下标签不能重名，同一个父标签下标签不能重名（没有需求，未实现）
     *
     * @param categoryId 标签组ID
     * @param name 标签名称
     * @return
     */
    private String checkRepeatName(String categoryId, Long id, String name) {
        List<TagInfo> models = this.tagInfoMapper.selectSimpleByCategoryId(Long.parseLong(categoryId));
        for (TagInfo m : models) {
            if (StringUtils.equals(name, m.getName())) {
                if (id == null || NumberUtils.compare(id, m.getId()) != 0) {
                    return "标签组下已存在同名标签";
                }
            }
        }
        return null;
    }
}
