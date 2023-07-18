package com.binance.account.vo.tag.response;

import com.binance.master.commons.ToString;
import lombok.Data;

import java.util.List;

/**
 * @author lufei
 * @date 2019/2/21
 */
@Data
public class TagUserStatusResponse extends ToString {

    private static final long serialVersionUID = -5501622485256958749L;

    List<TagUserStatusVo> userStatusVoList;

}
