package com.binance.account.integration.assetservice;

import com.binance.assetservice.api.IProductApi;
import com.binance.assetservice.vo.request.product.PriceConvertRequest;
import com.binance.assetservice.vo.response.product.PriceConvertResponse;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Log4j2
@Service
public class ProductApiClient {
    @Resource
    private IProductApi iProductApi;


    public BigDecimal mgmtPriceConvert(String coin, BigDecimal amount,String baseAsset) throws Exception{
        BigDecimal totalAmount = amount;
        if (!coin.equals(baseAsset)) {
            PriceConvertRequest priceConvertRequest = new PriceConvertRequest();
            priceConvertRequest.setAmount(totalAmount);
            priceConvertRequest.setFrom(coin);
            priceConvertRequest.setTo(baseAsset);
            APIResponse<PriceConvertResponse> priceConvertResponse =
                    this.iProductApi.mgmtPriceConvert(APIRequest.instance(priceConvertRequest));
            if (APIResponse.Status.ERROR == priceConvertResponse.getStatus()) {
                log.error("ProductApiClient.mgmtPriceConvert error" + priceConvertResponse.getErrorData());
                throw new BusinessException("mgmtPriceConvert failed");
            }
            totalAmount = totalAmount.multiply(priceConvertResponse.getData().getPrice());
        }
        return totalAmount;
    }


    public BigDecimal getEqualBtcAmount(String coin, BigDecimal amount) throws Exception{
        return mgmtPriceConvert(coin,amount,"BTC");
    }



}
