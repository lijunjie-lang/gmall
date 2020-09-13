package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {

    @PostMapping("pms/spu/json")
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);

    @GetMapping("pms/sku/spu/{spuId}")
    @ApiOperation("根据spuId查询sku列表")
    public ResponseVo<List<SkuEntity>> querySkuBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("pms/brand/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    @GetMapping("pms/category/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    @ApiOperation("根据spuId查询检索属性及值")
    @GetMapping("pms/spuattrvalue/spu/{spuId}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValueBySpuId(
            @PathVariable("spuId")Long spuId
    );

    @ApiOperation("根据spuId查询检索属性及值")
    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValueBySkuId(
            @PathVariable("skuId")Long skuId
    );

    @GetMapping("pms/spu/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);


    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategoriesByPid(@PathVariable("parentId")Long parentId);

    @GetMapping("pms/category/subs/{pid}")
    public ResponseVo<List<CategoryEntity>> queryCategoriesWithSubByPid(@PathVariable("pid")Long pid);

    @GetMapping("pms/sku/{id}")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    @GetMapping("pms/category/all/{cid}")
    public ResponseVo<List<CategoryEntity>> queryAllCategoriesByCid3(@PathVariable("cid")Long cid);

    @GetMapping("pms/skuimages/sku/{skuId}")
    public ResponseVo<List<SkuImagesEntity>> queryImagesBySkuId(@PathVariable("skuId")Long skuId);

    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySaleAttrValueVoBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("pms/skuattrvalue/spu/sku/{spuId}")
    public ResponseVo<String> querySaleAttrValuesMappingSkuIdBySpuId(@PathVariable("spuId")Long spuId);

    @GetMapping("pms/spudesc/{spuId}")
    public ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

    @GetMapping("pms/attrgroup/cid/{cid}")
    public ResponseVo<List<GroupVo>> queryGroupVoByCidAndSpuIdAndSkuId(
            @PathVariable("cid")Long cid,
            @RequestParam("spuId")Long spuId,
            @RequestParam("skuId")Long skuId
    );

    @GetMapping("pms/skuattrvalue/sku/sale/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySaleAttrValuesBySkuId(@PathVariable("skuId")Long skuId);


}
