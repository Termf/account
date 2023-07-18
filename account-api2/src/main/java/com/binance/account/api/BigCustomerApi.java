package com.binance.account.api;

import com.binance.account.vo.tag.response.EmailAndUserIdVo;
import com.binance.account.vo.tag.response.StatusAndUserIdVo;
import com.binance.account.vo.user.request.GetUserListRequest;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@RequestMapping(value = "/bigCustomer")
@Api(value = "大客户管理")
public interface BigCustomerApi {

    @ApiOperation("获取用户的邮箱和标签")
    @PostMapping("/emailAndTag")
    APIResponse<List<EmailAndUserIdVo>> emailAndTag(@RequestBody() @Validated() APIRequest<GetUserListRequest> request) throws Exception;

    @ApiOperation("批量获取用户状态")
    @PostMapping("/findUserStatus")
    APIResponse<List<StatusAndUserIdVo>> findUserStatus(@RequestBody() @Validated() APIRequest<GetUserListRequest> request) throws Exception;

}
