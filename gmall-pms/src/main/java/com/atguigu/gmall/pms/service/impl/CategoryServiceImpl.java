package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<CategoryEntity> queryCategoriesWithSubByPid(Long pid) {
        return this.categoryMapper.queryCategoriesWithSubByPid(pid);
    }

    @Override
    public List<CategoryEntity> queryAllCategoriesByCid3(Long cid) {

        CategoryEntity lvl3Cat = this.getById(cid);
        if (lvl3Cat != null){
            CategoryEntity lvl2Cat = this.getById(lvl3Cat.getParentId());
            CategoryEntity lvl1Cat = this.getById(lvl2Cat.getParentId());

            return Arrays.asList(lvl1Cat,lvl2Cat,lvl3Cat);
        }
        return null;
    }


}