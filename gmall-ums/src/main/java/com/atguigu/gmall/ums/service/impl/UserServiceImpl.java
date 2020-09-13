package com.atguigu.gmall.ums.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        switch (type){
            case 1: wrapper.eq("username", data);break;
            case 2: wrapper.eq("phone", data);break;
            case 3: wrapper.eq("email", data);break;
            default: return null;
        }
        return this.count(wrapper) == 0;
    }

    @Override
    public UserEntity queryUser(String loginName, String password) {
        List<UserEntity> userEntities = this.list(new QueryWrapper<UserEntity>()
        .eq("username", loginName)
        .or()
        .eq("email", loginName)
        .or()
        .eq("phone", loginName));
        if (CollectionUtils.isEmpty(userEntities)){
            return null;
        }
        String pwd = null;
        for (UserEntity userEntity : userEntities){
            pwd = DigestUtils.md5Hex(password + userEntity.getSalt());
            if (StringUtils.equals(pwd,userEntity.getPassword())){
                return userEntity;
            }
        }
        return null;
    }

    @Override
    public void register(UserEntity userEntity, String code) {
        // 1.校验短信验证码 TODO：

        // 2.生成盐
        String salt = StringUtils.substring(UUID.randomUUID().toString(), 0, 6);
        userEntity.setSalt(salt);

        // 3.对密码加盐加密
        userEntity.setPassword(DigestUtils.md5Hex(userEntity.getPassword() + salt));

        // 4.新增用户
        userEntity.setLevelId(1l);
        userEntity.setSourceType(1);
        userEntity.setIntegration(1000);
        userEntity.setGrowth(1000);
        userEntity.setStatus(1);
        userEntity.setCreateTime(new Date());
        this.save(userEntity);


    }

}