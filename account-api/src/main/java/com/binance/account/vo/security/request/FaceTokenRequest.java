package com.binance.account.vo.security.request;

import com.binance.master.enums.LanguageEnum;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author liliang1
 * @date 2018-09-13 17:58
 */
@Setter
@Getter
public class FaceTokenRequest implements Serializable {
    private static final long serialVersionUID = 2386095614465611477L;

    /** 语言 未输入默认为英文 */
    private LanguageEnum language;

    /** 用户ID */
    @NotNull
    private Long userId;

    /** 附加信息, 会在结果中原样返回 */
    private String extraData;

    /** 照片名称 */
    @NotNull
    private String imageName;

    /** 照片 */
    @NotNull
    private byte[] image;

    /** 图片是否进行放缩 */
    private boolean imageScale;

    /** 图片放缩倍数, eg: 0.25 放缩到原图的0.25倍 如果未输入或者小于等于0，使用系统默认值 */
    private Double imageScaleMultiple;




}
