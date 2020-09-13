package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Test
    void contextLoads() {

        this.restTemplate.createIndex(Goods.class);
        this.restTemplate.putMapping(Goods.class);

        Integer pageNum = 1;
        Integer pageSize = 100;
        do {
            //分批查询spu
            PageParamVo paramVo = new PageParamVo(pageNum, pageSize, null);
            ResponseVo<List<SpuEntity>> responseVo = this.pmsClient.querySpuByPageJson(paramVo);
            List<SpuEntity> spuEntities = responseVo.getData();
            if (CollectionUtils.isEmpty(spuEntities)){
                return;
            }

            //遍历spu，查询spu下sku的信息
            spuEntities.forEach(spuEntity -> {
                ResponseVo<List<SkuEntity>> skuResponseVo = this.pmsClient.querySkuBySpuId(spuEntity.getId());
                List<SkuEntity> skuEntities = skuResponseVo.getData();
                if (!CollectionUtils.isEmpty(skuEntities)){
                    List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                        Goods goods = new Goods();
                        //sku相关信息
                        goods.setSkuId(skuEntity.getId());
                        goods.setTitle(skuEntity.getTitle());
                        goods.setSubTitle(skuEntity.getSubtitle());
                        goods.setPrice(skuEntity.getPrice().doubleValue());
                        goods.setDefaultImage(skuEntity.getDefaultImage());
                        //spu创建时间
                        goods.setCreateTime(spuEntity.getCreateTime());

                        ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(skuEntity.getBrandId());
                        BrandEntity brandEntity = brandEntityResponseVo.getData();
                        if (brandEntity != null){
                            goods.setBrandId(brandEntity.getId());
                            goods.setBrandName(brandEntity.getName());
                            goods.setLogo(brandEntity.getLogo());
                        }
                        ResponseVo<CategoryEntity> categoryEntityResponseVo = this.pmsClient.queryCategoryById(skuEntity.getCatagoryId());
                        CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                        if (categoryEntity != null){
                            goods.setCategoryId(categoryEntity.getId());
                            goods.setCategoryName(categoryEntity.getName());
                        }

                        ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = this.wmsClient.queryWareSkuBySkuId(skuEntity.getId());
                        List<WareSkuEntity> wareSkuEntities = wareSkuResponseVo.getData();
                        if (!CollectionUtils.isEmpty(wareSkuEntities)){
                            goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                            goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a,b) -> a + b).get());
                        }

                        // 用于收集所有的搜索属性
                        ArrayList<SearchAttrValueVo> attrValueVos = new ArrayList<>();
                        ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo = this.pmsClient.querySearchAttrValueBySkuId(skuEntity.getId());
                        List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
                        if (!CollectionUtils.isEmpty(skuAttrValueEntities)){
                            attrValueVos.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                                BeanUtils.copyProperties(skuAttrValueEntities, searchAttrValueVo);
                                return searchAttrValueVo;
                            }).collect(Collectors.toList()));
                        }
                        ResponseVo<List<SpuAttrValueEntity>> spuAttrValueResponseVo = this.pmsClient.querySearchAttrValueBySpuId(spuEntity.getId());
                        List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueResponseVo.getData();
                        if (!CollectionUtils.isEmpty(spuAttrValueEntities)){
                            attrValueVos.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                                BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValueVo);
                                return searchAttrValueVo;
                            }).collect(Collectors.toList()));
                        }
                        goods.setSearchAttrs(attrValueVos);


                        return goods;
                    }).collect(Collectors.toList());
                this.goodsRepository.saveAll(goodsList);






                }




            });

            // 如果是最后一页，这里pageSize!=100
            pageSize = spuEntities.size();
            pageNum++;
        }while (pageSize == 100);





    }

}
