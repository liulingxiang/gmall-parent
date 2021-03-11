package com.atguigu.gmall.model.product;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "商品三级分类")
@TableName("base_category3")
public class BaseCategory3 extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableField(value = "name")
    @ApiModelProperty(value = "三级分类名称")
    String name;

    @TableField(value = "category2_id")
    @ApiModelProperty(value = "二级分类编号")
    Long category1Id;
}
