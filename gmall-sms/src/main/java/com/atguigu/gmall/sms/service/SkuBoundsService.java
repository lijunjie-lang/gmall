package com.atguigu.gmall.sms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 商品spu积分设置
 *
 * @author lijunjie
 * @email 1610251381@qq.com
 * @date 2020-08-21 19:25:10
 */
public interface SkuBoundsService extends IService<SkuBoundsEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    void saveSkuSakes(SkuSaleVo skuSaleVo);

    List<ItemSaleVo> querySaleVoBySkuId(Long skuId);
}

