package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    private SpuDescService spuDescService;

    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Autowired
    private SpuDescMapper spuDescMapper;
    @Autowired
    private SpuAttrValueService spuAttrValueService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuInfo(PageParamVo pageParamVo, Long categoryId) {

        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();

        if (categoryId != 0){
            wrapper.eq("category_id", categoryId);
        }

        String key = pageParamVo.getKey();
        if (StringUtils.isNotBlank(key)){
            wrapper.and(t -> t.like("name", key).or().like("id", key));
        }
        return new PageResultVo(this.page(pageParamVo.getPage(),wrapper));
    }

    @Override
    @GlobalTransactional
    public void bigSave(SpuVo spu) {
        // 1.保存spu相关
        // 1.1. 保存spu基本信息pms_spu
        Long spuId = saveSpu(spu);
        // 1.2. 保存spu的描述信息pms_spu_desc
        this.spuDescService.saveSpuDesc(spu, spuId);
        // 1.3. 保存spu的规格参数信息pms_spu_attr_value
        saveBaseAttr(spu, spuId);

        // 2. 保存sku相关信息
        saveSku(spu, spuId);

    }

    public void saveSku(SpuVo spu, Long spuId) {
        List<SkuVo> skus = spu.getSkus();
        if (CollectionUtils.isEmpty(skus)){
            return;
        }
        //遍历保存sku的相关信息
        skus.forEach(skuVo -> {

            // 2.1. 保存sku基本信息pms_sku
            skuVo.setId(null);
            skuVo.setSpuId(spuId);
            skuVo.setBrandId(spu.getBrandId());
            skuVo.setCatagoryId(spu.getCategoryId());
            List<String> images = skuVo.getImages();
            if (!CollectionUtils.isEmpty(images)){
                skuVo.setDefaultImage(skuVo.getDefaultImage() ==
                        null ? images.get(0) : skuVo.getDefaultImage());
            }
            this.skuMapper.insert(skuVo);
            Long skuId = skuVo.getId();
            // 2.2. 保存sku图片信息pms_skuImages
            if (!CollectionUtils.isEmpty(images)){
                List<SkuImagesEntity> imagesEntities = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setId(null);
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setUrl(image);
                    skuImagesEntity.setSort(1);
                    skuImagesEntity.setDefaultStatus(0);
                    if (StringUtils.equals(skuVo.getDefaultImage(), image)){
                        skuImagesEntity.setDefaultStatus(1);
                    }
                    return skuImagesEntity;

                }).collect(Collectors.toList());
                this.skuImagesService.saveBatch(imagesEntities);
            }

            // 2.3. 保存sku的规格参数（销售属性）pms_sku_attr_value
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();

            if (!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(attr -> {
                    attr.setSkuId(skuId);
                    attr.setSort(0);
                    attr.setId(null);
                });
                this.skuAttrValueService.saveBatch(saleAttrs);
            }

            // 3. 保存营销相关信息，需要远程调用gmall-sms
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuVo, skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            this.gmallSmsClient.saveSkuSakes(skuSaleVo);
        });
    }

    public void saveBaseAttr(SpuVo spu, Long spuId) {
        List<SpuAttrValueVo> baseAttrs = spu.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)){
            List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrs.stream().map(spuAttrValueVo -> {
                SpuAttrValueEntity baseEntity = new SpuAttrValueEntity();
                BeanUtils.copyProperties(spuAttrValueVo, baseEntity);
                baseEntity.setSpuId(spuId);
                baseEntity.setSort(1);
                baseEntity.setId(null);
                return baseEntity;
            }).collect(Collectors.toList());
            this.spuAttrValueService.saveBatch(spuAttrValueEntities);
        }
    }

    public Long saveSpu(SpuVo spu) {
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        spu.setId(null);
        this.save(spu);
        return spu.getId();
    }

}