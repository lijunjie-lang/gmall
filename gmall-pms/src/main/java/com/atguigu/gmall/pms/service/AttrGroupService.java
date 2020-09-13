package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 属性分组
 *
 * @author lijunjie
 * @email 1610251381@qq.com
 * @date 2020-08-21 19:13:10
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<AttrGroupEntity> queryGroupWithAttrsByCid(Long cid);

    List<GroupVo> queryGroupVoByCidAndSpuIdAndSkuId(Long cid, Long spuId, Long skuId);
}

