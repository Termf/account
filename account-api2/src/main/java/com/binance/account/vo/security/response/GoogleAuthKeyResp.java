package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import lombok.*;

/**
 * Created by Fei.Huang on 2018/12/19.
 */
@ApiModel("获取谷歌authkey Response")
@Getter
@Setter
@NoArgsConstructor
public class GoogleAuthKeyResp extends ToString {
    private String authKey;
    private String qrCode;
}
