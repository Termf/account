package com.binance.account.service.tag;

import java.util.List;

import com.binance.account.vo.tag.TagImportVo;
import com.binance.account.vo.tag.response.TagResponse;
import com.binance.master.models.APIResponse;

/**
 * @author lufei
 * @date 2018/5/7
 */
public interface ITagImport {

    APIResponse<TagResponse> upload(String user, String xId, String type, List<TagImportVo> maps);

}
