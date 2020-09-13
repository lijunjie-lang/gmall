package com.atguigu.gmall.ums.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户表
 *
 * @author lijunjie
 * @email 1610251381@qq.com
 * @date 2020-08-21 19:28:04
 */
public interface UserService extends IService<UserEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    Boolean checkData(String data, Integer type);

    UserEntity queryUser(String loginName, String password);

    void register(UserEntity userEntity, String code);

}

