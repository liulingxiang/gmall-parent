package com.atguigu.gmall.model.product;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName(value = "base_category1")
@ApiModel(description = "商品一级分类")
public class BaseCategory1 extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableField(value = "name")
    @ApiModelProperty(value = "一级分类名称")
    String name;
}
