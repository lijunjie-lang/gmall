package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * spu信息
 *
 * @author lijunjie
 * @email 1610251381@qq.com
 * @date 2020-08-21 19:13:10
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo querySpuInfo(PageParamVo pageParamVo, Long categoryId);

    void bigSave(SpuVo spu);

    void saveSku(SpuVo spuVo, Long spuId);

    void saveBaseAttr(SpuVo spuVo, Long spuId);



    Long saveSpu(SpuVo spuVo);


}

