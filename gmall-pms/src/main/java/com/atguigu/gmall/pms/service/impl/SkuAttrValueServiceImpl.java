package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySearchAttrValueBySkuId(Long skuId) {

        return this.skuAttrValueMapper.querySearchAttrValueBySkuId(skuId);
    }

    @Override
    public List<SaleAttrValueVo> querySaleAttrValueVoBySpuId(Long spuId) {

        List<SkuEntity> skuEntities = this.skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if (CollectionUtils.isEmpty(skuEntities)){
            return null;
        }
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());


        List<SkuAttrValueEntity> skuAttrValueEntities = this.skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().in("sku_id", skuIds));

        ArrayList<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();
        Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream().collect(Collectors.groupingBy(t -> t.getAttrId()));
        map.forEach((attrId,skuattrvalues) -> {
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            saleAttrValueVo.setAttrId(attrId);
            saleAttrValueVo.setAttrName(skuattrvalues.get(0).getAttrName());
            Set<String> attrValues = skuattrvalues.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet());
            saleAttrValueVo.setAttrValues(attrValues);
        });
        return saleAttrValueVos;
    }

    @Override
    public String querySaleAttrValuesMappingSkuIdBySpuId(Long spuId) {

        List<Map<String,Object>> maps = this.skuAttrValueMapper.querySaleAttrValuesMappingSkuIdBySpuId(spuId);
        if (CollectionUtils.isEmpty(maps)){
            return null;
        }
        Map<String, Long> saleAttrValuesMappingSkuIdMap  = maps.stream().collect(Collectors.toMap(map -> map.get("attrvalues").toString(), map -> (Long) map.get("sku_id")));
        return JSON.toJSONString(saleAttrValuesMappingSkuIdMap);

    }

}