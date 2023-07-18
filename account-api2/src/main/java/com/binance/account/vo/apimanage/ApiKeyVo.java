package com.binance.account.vo.apimanage;

import com.binance.master.commons.ToString;
import lombok.Data;

/**
 * ApiKey
 */
@Data
public class ApiKeyVo extends ToString {
    private Long keyId;
    private String apiKey;
    private String secretKey;
    private String type;
}

//{"apiKey":"3oABnKonsD9BfPy8XR3GnqozmMrbVifTaPmj46wqf8AAE2cHhOmauQti4Tc0smHo","secretKey":"zZE5avZP0s5hk4amGc7XWRijb6Vye43MtbF6nzRFVgVDnVF3dByA4CevVcYjF4cw","keyId":35}