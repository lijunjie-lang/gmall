package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author lijunjie
 * @email 1610251381@qq.com
 * @date 2020-08-21 19:18:21
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
	
}
