package com.wxb.plugin.core.gen;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 排序元素载体
 *
 * @author HCL
 * Create at 2019/5/27
 */
@ApiModel(description = "排序")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 需要进行排序的字段
     */
    @ApiModelProperty("字段")
    private String column;

    /**
     * 是否正序排列，默认 true
     */
    @ApiModelProperty("是否正序排列")
    private boolean asc = true;

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }
}
