package com.wxb.plugin.core.gen;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 数据基础响应DTO
 *
 */
@ApiModel(description = "数据基础响应DTO")
public class BaseDataResponse<T> extends BaseResponse {

    @ApiModelProperty("业务数据")
    private T data;

}