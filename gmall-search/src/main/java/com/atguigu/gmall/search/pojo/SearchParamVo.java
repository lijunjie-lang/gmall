package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParamVo {

    private String keyword; // 搜索关键字

    private List<Long> brandId; // 品牌过滤

    private List<Long> categoryId;  // 分类过滤

    private List<String> props;  // 规格参数过滤

    private Integer sort; // 排序字段：0-得分排序 1-价格升序 2-价格降序排列 3-新品降序 4-销量降序

    private Boolean store; // 是否有货筛选

    // 价格区间过滤
    private Double priceFrom;
    private Double priceTo;

    // 分页
    private Integer pageNum = 1;
    private final Integer pageSize = 20;


}
