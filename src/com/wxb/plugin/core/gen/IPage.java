package com.wxb.plugin.core.gen;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * @author liyuanyuan
 */
@ApiModel(description = "分页信息")
public class IPage<T> {

    @ApiModelProperty("数据")
    private List<T> records;

    @ApiModelProperty("总条数")
    private long total;

    @ApiModelProperty("分页大小")
    private long size;

    @ApiModelProperty("当前页")
    private long current;

    @ApiModelProperty("排序")
    private List<OrderItem> orders;

    @ApiModelProperty("是否优化统计sql")
    private boolean optimizeCountSql;

    @ApiModelProperty("是否搜索统计")
    private boolean isSearchCount;

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public List<OrderItem> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderItem> orders) {
        this.orders = orders;
    }

    public boolean isOptimizeCountSql() {
        return optimizeCountSql;
    }

    public void setOptimizeCountSql(boolean optimizeCountSql) {
        this.optimizeCountSql = optimizeCountSql;
    }

    public boolean isSearchCount() {
        return isSearchCount;
    }

    public void setSearchCount(boolean searchCount) {
        isSearchCount = searchCount;
    }
}

