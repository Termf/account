package com.binance.account.service.tag.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.binance.account.common.query.SearchResult;
import com.binance.account.data.entity.tag.FullTagInfo;
import com.binance.account.data.entity.tag.TagCategory;
import com.binance.account.data.entity.tag.TagDetailDefine;
import com.binance.account.data.entity.tag.TagIndicator;
import com.binance.account.data.entity.tag.TagIndicatorDetail;
import com.binance.account.data.entity.tag.TagInfo;
import com.binance.account.data.entity.tag.TagPermission;
import com.binance.account.data.mapper.tag.TagCategoryMapper;
import com.binance.account.data.mapper.tag.TagDetailDefineMapper;
import com.binance.account.data.mapper.tag.TagIndicatorDetailMapper;
import com.binance.account.data.mapper.tag.TagIndicatorMapper;
import com.binance.account.data.mapper.tag.TagInfoMapper;
import com.binance.account.data.mapper.tag.TagPermissionMapper;
import com.binance.account.domain.bo.TagCategoryTreeNode;
import com.binance.account.domain.bo.TagTreeNode;
import com.binance.account.service.tag.ITagCategory;
import com.binance.account.service.tag.ITagIndicator;
import com.binance.account.vo.tag.TagDetailVo;
import com.binance.account.vo.tag.request.TagPageRequest;
import com.binance.account.vo.tag.response.TagCategoryResponse;
import com.binance.account.vo.tag.response.TagIndicatorDetailVo;
import com.binance.account.vo.tag.response.TagResponse;
import com.binance.account.vo.tag.response.TagTreeResponse;
import com.binance.master.models.APIResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

/**
 * @author lufei
 * @date 2018/6/21
 */
@Log4j2
@Service
public class TagCategoryBusiness implements ITagCategory {

    @Resource
    private TagCategoryMapper tagCategoryMapper;

    @Resource
    private TagInfoMapper tagInfoMapper;

    @Resource
    private TagIndicatorMapper tagIndicatorMapper;

    @Resource
    private TagIndicatorDetailMapper detailMapper;

    @Resource
    private TagDetailDefineMapper defineMapper;

    @Resource
    private ITagIndicator tagIndicator;

    @Resource
    private TagPermissionMapper permissionMapper;

    @Override
    public APIResponse<TagResponse> saveCategory(String pid, String name) {
        if (!NumberUtils.isParsable(pid)) {
            return APIResponse.getOKJsonResult(new TagResponse("参数错误"));
        }
        List<TagCategory> dbModels = this.tagCategoryMapper.getByPidAndName(pid, name);
        if (dbModels.size() > 0) {
            return APIResponse.getOKJsonResult(new TagResponse("该标签组下，已存在同名标签组"));
        }
        TagCategory model = new TagCategory();
        model.setPid(Long.parseLong(pid));
        model.setName(name);
        this.tagCategoryMapper.insert(model);
        return APIResponse.getOKJsonResult(new TagResponse(true, model.getId()));
    }

    @Override
    public APIResponse<TagResponse> modifyCategory(String id, String pid, String name) {
        if (!NumberUtils.isParsable(id) || !NumberUtils.isParsable(pid)) {
            return APIResponse.getOKJsonResult(new TagResponse("参数错误"));
        }
        TagCategory model = this.tagCategoryMapper.selectByPrimaryKey(id);
        if (model == null || model.getId() == null) {
            return APIResponse.getOKJsonResult(new TagResponse("标签无效"));
        }
        List<TagCategory> dbModels = this.tagCategoryMapper.getByPidAndName(pid, name);
        if (dbModels.size() > 1 || (dbModels.size() > 0 && !StringUtils.equals(dbModels.get(0).getId() + "", id))) {
            return APIResponse.getOKJsonResult(new TagResponse("该标签组下，已存在同名标签组"));
        }
        model.setPid(Long.parseLong(pid));
        model.setName(name);
        this.tagCategoryMapper.updateByPrimaryKey(model);
        return APIResponse.getOKJsonResult(new TagResponse(true, model.getId()));
    }

    @Override
    public APIResponse<TagResponse> removeCategory(String id) {
        if (StringUtils.equals(id, "0")) {
            return APIResponse.getOKJsonResult(new TagResponse("不能删除根标签组"));
        }
        this.deleteRecursive(id);
        return APIResponse.getOKJsonResult(new TagResponse(true, Long.parseLong(id)));
    }

    @Override
    public APIResponse<TagCategoryResponse> getTagCategoryById(String id) {
        if (!NumberUtils.isParsable(id)) {
            return APIResponse.getOKJsonResult(null);
        }
        TagCategory model = tagCategoryMapper.selectByPrimaryKey(id);
        if (model == null) {
            return APIResponse.getOKJsonResult(null);
        }
        TagCategoryResponse response = this.toTagCategoryResponse(model);
        if (model != null && NumberUtils.compare(model.getPid(), 0L) != 0) {
            TagCategory pModel = tagCategoryMapper.selectByPrimaryKey(model.getPid() + "");
            response.setPName(pModel.getName());
        }
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<Object> getTree(String tid, List<String> roleIds) {
        boolean isIgnorePermission = false;
        Set<Long> permission = Collections.emptySet();
        if (CollectionUtils.isEmpty(roleIds) || roleIds.contains("admin")) {
            isIgnorePermission = true;
        } else {
            List<TagPermission> permissions = permissionMapper.selectByRoleIds(roleIds);
            permission = permissions.stream().map(p -> p.getCategoryId()).collect(Collectors.toSet());
            permission.add(1L);
        }
        TagCategoryTreeNode tree = new TagCategoryTreeNode();
        tree.setId("0");
        tree.setText("全部");
        tree.setState("open");
        tree.setAttributes(new TreeAttribute("category", null));
        this.getTreeRecursive(tree, permission, isIgnorePermission);
        if (tree.getChildren() != null && tree.getChildren().size() > 0) {
            tree.getChildren().get(0).setState("open");
        }
        return APIResponse.getOKJsonResult(tree.getChildren());
    }

    @Override
    public APIResponse<Object> getExtraInfo(String userId, String tagId) {
        TagIndicator model = this.tagIndicatorMapper.selectByTagIdAndUserId(userId, Long.parseLong(tagId));
        Map<String, Object> map = new HashMap<>(4);
        List<List<TagIndicatorDetailVo>> details = null;
        if (model != null) {
            details = this.tagDetails(Long.parseLong(tagId), model.getId());
            map.put("categoryName", model.getCategoryName());
            map.put("remark", model.getRemark());
            map.put("value", model.getValue());
        } else {
            details = this.tagDetails(Long.parseLong(tagId), null);
            FullTagInfo info = this.tagInfoMapper.selectFullById(tagId);
            map.put("categoryName", info.getCategoryName());
        }
        map.put("detail", details);
        return APIResponse.getOKJsonResult(map);
    }

    @Override
    public APIResponse<SearchResult<TagCategoryResponse>> getTagCategoryParentList(TagPageRequest request) {
        try {
            String xId = request.getXId();
            if (StringUtils.equals(request.getTagType(), "tag")) {
                TagInfo model = this.tagInfoMapper.selectByPrimaryKey(xId);
                if (model != null && model.getId() != null) {
                    xId = model.getCategoryId() + "";
                }
            }
            List<TagCategoryResponse> models = this.getParentById(xId);
            SearchResult<TagCategoryResponse> searchResult = new SearchResult<>();
            searchResult.setTotal(models.size());
            searchResult.setRows(models);
            return APIResponse.getOKJsonResult(searchResult);
        } catch (Exception e) {
            log.error("查询标签组父类信息异常", e);
            return null;
        }
    }

    @Override
    public APIResponse<List<TagTreeResponse>> getTagCategoryList(String pid) {
        List<TagCategory> models = this.tagCategoryMapper.selectByPid(pid);
        List<TagTreeResponse> treeModels = new ArrayList<>();
        for (TagCategory model : models) {
            TagTreeResponse treeModel = new TagTreeResponse();
            treeModel.setId(model.getId() + "");
            treeModel.setText(model.getName());
            treeModel.setState("closed");
            treeModel.setAttributes("category");
            treeModels.add(treeModel);
        }
        List<TagInfo> infos = this.tagInfoMapper.selectSimpleByCategoryId(Long.parseLong(pid));
        for (TagInfo info : infos) {
            TagTreeResponse treeModel = new TagTreeResponse();
            treeModel.setId(info.getId() + "");
            treeModel.setText(info.getName());
            treeModel.setState("open");
            treeModel.setAttributes("tag");
            treeModels.add(treeModel);
        }
        return APIResponse.getOKJsonResult(treeModels);
    }

    private void deleteRecursive(String id) {
        TagCategory model = this.tagCategoryMapper.selectByPrimaryKey(id);
        if (model != null) {
            this.tagCategoryMapper.deleteByPrimaryKey(model.getId() + "");
            List<TagCategory> models = this.tagCategoryMapper.selectByPid(model.getId() + "");
            if (CollectionUtils.isEmpty(models)) {
                return;
            }
            for (TagCategory m : models) {
                this.deleteRecursive(m.getId() + "");
            }
        }
    }

    private TagCategoryResponse toTagCategoryResponse(TagCategory model) {
        TagCategoryResponse response = new TagCategoryResponse();
        response.setId(model.getId());
        response.setPid(model.getPid());
        response.setName(model.getName());
        response.setCreateTime(model.getCreateTime());
        response.setUpdateTime(model.getUpdateTime());
        return response;
    }

    private void getTreeRecursive(TagCategoryTreeNode tree, Set<Long> permission, boolean isIgnorePermission) {
        List<TagCategory> categories = this.tagCategoryMapper.selectByPid(tree.getId());
        List<TagInfo> tags = this.tagInfoMapper.selectSimpleByCategoryId(Long.parseLong(tree.getId()));
        if (!isIgnorePermission) {
            Iterator<TagCategory> categoryIterator = categories.iterator();
            while (categoryIterator.hasNext()) {
                TagCategory next = categoryIterator.next();
                if (!permission.contains(next.getId())) {
                    categoryIterator.remove();
                }
            }
            Iterator<TagInfo> tagInfoIterator = tags.iterator();
            while (tagInfoIterator.hasNext()) {
                TagInfo next = tagInfoIterator.next();
                if (!permission.contains(next.getCategoryId())) {
                    tagInfoIterator.remove();
                }
            }
        }
        List<TagCategoryTreeNode> categoryNodes = this.convertTagCategoryModelList(categories);
        List<TagTreeNode> infoNodes = this.convertTagInfoModels(tags);
        List<TagTreeNode> nodes = new ArrayList<>(categoryNodes.size() + infoNodes.size());
        for (TagTreeNode node : categoryNodes) {
            nodes.add(node);
        }
        nodes.addAll(infoNodes);
        tree.setChildren(nodes);
        if (CollectionUtils.isEmpty(categories)) {
            return;
        }
        for (TagCategoryTreeNode categoryNode : categoryNodes) {
            getTreeRecursive(categoryNode, permission, isIgnorePermission);
        }
    }

    private List<TagCategoryTreeNode> convertTagCategoryModelList(List<TagCategory> models) {
        if (CollectionUtils.isEmpty(models)) {
            return Collections.emptyList();
        }
        List<TagCategoryTreeNode> nodes = new ArrayList<>(models.size());
        for (TagCategory model : models) {
            nodes.add(parseTagCategory(model));
        }
        return nodes;
    }

    private TagCategoryTreeNode parseTagCategory(TagCategory model) {
        TagCategoryTreeNode tree = new TagCategoryTreeNode();
        tree.setId(model.getId() + "");
        tree.setText(model.getName());
        tree.setState("closed");
        tree.setAttributes(new TreeAttribute("category", null));
        return tree;
    }

    private List<TagTreeNode> convertTagInfoModels(List<TagInfo> models) {
        if (CollectionUtils.isEmpty(models)) {
            return Collections.emptyList();
        }
        List<TagTreeNode> nodes = new ArrayList<>(models.size());
        for (TagInfo model : models) {
            nodes.add(parseTagInfo(model));
        }
        return nodes;
    }

    private TagTreeNode parseTagInfo(TagInfo model) {
        TagTreeNode tree = new TagTreeNode();
        tree.setId(model.getId() + "");
        tree.setText(model.getName());
        tree.setState("open");
        String valueRange = model.getValue();
        if (StringUtils.isBlank(valueRange)) {
            valueRange = model.getMin() + " ~ " + model.getMax();
        }
        tree.setAttributes(new TreeAttribute("tag", valueRange));
        return tree;
    }

    private List<TagCategoryResponse> getParentById(String id) {
        List<TagCategoryResponse> models = new ArrayList<>();
        while (true) {
            TagCategoryResponse model = this.selectByPrimaryKeyAndParse(id);
            if (model == null || model.getId() == null) {
                break;
            }
            if (models.size() > 0) {
                TagCategoryResponse lastModel = models.get(models.size() - 1);
                lastModel.setPName(model.getName());
            }
            models.add(model);
            id = model.getPid() + "";
        }
        return models;
    }

    /**
     * 标签的详情的属性和值
     * 
     * @param tagId
     * @param indicatorId
     * @return
     */
    private List<List<TagIndicatorDetailVo>> tagDetails(Long tagId, Long indicatorId) {
        List<List<TagIndicatorDetailVo>> list = new ArrayList<>();
        TagDetailDefine define = defineMapper.selectByTagId(tagId);
        List<TagDetailVo> attrs = tagIndicator.tagDetailDefineToDetails(define);
        List<TagIndicatorDetailVo> columns = new ArrayList<>(attrs.size());
        for (int i = 0; i < attrs.size(); i++) {
            TagDetailVo vo = attrs.get(i);
            TagIndicatorDetailVo column = new TagIndicatorDetailVo();
            column.setField("f" + i);
            column.setValue(null);
            column.setAttr(vo.getAttr());
            column.setType(vo.getType());
            column.setRange(vo.getRange());
            column.setMust(vo.getMust());
            columns.add(column);
        }
        list.add(columns);

        if (indicatorId != null) {
            List<TagIndicatorDetail> details = detailMapper.selectByIndicatorId(indicatorId);
            for (TagIndicatorDetail detail : details) {
                List<TagIndicatorDetailVo> data = new ArrayList<>(attrs.size());
                for (int i = 0; i < attrs.size(); i++) {
                    TagDetailVo vo = attrs.get(i);
                    TagIndicatorDetailVo column = new TagIndicatorDetailVo();

                    String value = "";
                    Method method = BeanUtils.findMethod(TagIndicatorDetail.class, "getF" + i);
                    try {
                        Object obj = method.invoke(detail);
                        if (obj == null) {
                            value = "";
                        } else {
                            value = obj + "";
                        }
                    } catch (Exception e) {

                    }
                    column.setField("f" + i);
                    column.setValue(value);
                    column.setAttr(vo.getAttr());
                    column.setType(vo.getType());
                    column.setRange(vo.getRange());
                    column.setMust(vo.getMust());
                    data.add(column);
                }
                list.add(data);
            }
        }

        return list;
    }

    @Data
    @AllArgsConstructor
    private static class TreeAttribute {
        private String type;
        private String valueRange;
    }

    private TagCategoryResponse selectByPrimaryKeyAndParse(String id) {
        TagCategory model = this.tagCategoryMapper.selectByPrimaryKey(id);
        if (model == null) {
            return new TagCategoryResponse();
        }
        TagCategoryResponse response = new TagCategoryResponse();
        BeanUtils.copyProperties(model, response);
        return response;
    }

}
