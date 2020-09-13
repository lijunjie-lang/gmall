package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.UserInfo;
import com.atguigu.gmall.common.exception.CartException;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CartAsyncService asyncService;

    private static final String KEY_PREFIX = "cart:info:";
    private static final String PRICE_PREFIX = "cart:price";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public List<Cart> queryCheckedCartsByUserId(Long userId) {
        // 获取该用户的所有购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        List<Object> values = hashOps.values();
        if (CollectionUtils.isEmpty(values)){
            return null;
        }

        // 过滤出选中的购物车信息
        return values.stream().map(json -> {
            try {
                return MAPPER.readValue(json.toString(), Cart.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Cart::getCheck).collect(Collectors.toList());
    }

    public void addCart(Cart cart) {
        String userId = getUserId();
        String key = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        String skuIdString = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();
        try {
            if (hashOps.hasKey(skuIdString)){
                String json = hashOps.get(skuIdString).toString();
                cart = MAPPER.readValue(json, Cart.class);
                cart.setCount(cart.getCount().add(count));
                this.asyncService.updateByUserIdAndSkuId(userId, cart,  skuIdString);
//                this.cartMapper.update(cart, new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuIdString));
//                hashOps.put(skuIdString, MAPPER.writeValueAsString(cart));
            }else {
                cart.setUserId(userId);
                ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
                SkuEntity skuentity = skuEntityResponseVo.getData();
                if (skuentity == null){
                    throw new CartException("您加入购物车的商品不存在。。。");
                }
                cart.setDefaultImage(skuentity.getDefaultImage());
                cart.setTitle(skuentity.getTitle());
                cart.setPrice(skuentity.getPrice());
                ResponseVo<List<SkuAttrValueEntity>> listResponseVo = this.pmsClient.querySaleAttrValuesBySkuId(cart.getSkuId());
                List<SkuAttrValueEntity> skuAttrValueEntities = listResponseVo.getData();
                cart.setSaleAttrs(MAPPER.writeValueAsString(skuAttrValueEntities));

                ResponseVo<List<ItemSaleVo>> responseVo = this.smsClient.querySaleVoBySkuId(cart.getSkuId());
                List<ItemSaleVo> itemSaleVos = responseVo.getData();
                cart.setSales(MAPPER.writeValueAsString(itemSaleVos));

                ResponseVo<List<WareSkuEntity>> wareSkuBySkuId = this.wmsClient.queryWareSkuBySkuId(cart.getSkuId());
                List<WareSkuEntity> wareSkuEntities = wareSkuBySkuId.getData();
                if (!CollectionUtils.isEmpty(wareSkuEntities)){
                    cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                }
                cart.setCheck(true);
                this.asyncService.addCart(userId,cart);
//                this.cartMapper.insert(cart);
                this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuIdString,skuentity.getPrice().toString());
            }
            hashOps.put(skuIdString, MAPPER.writeValueAsString(cart));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


    }

    public Cart queryCartBySkuId(Long skuId) throws JsonProcessingException {
        String userId = this.getUserId();
        String key = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())){
            String json = hashOps.get(skuId.toString()).toString();
            if (StringUtils.isNotBlank(json)){
                return MAPPER.readValue(json, Cart.class);
            }
        }
        return null;
    }

    private String getUserId(){
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userId = null;
        if (userInfo.getUserId() == null){
            userId = userInfo.getUserKey();
        }else {
            userId = userInfo.getUserId().toString();
        }
        return userId;
    }


    public List<Cart> queryCartsByUserId() {
        // 1.获取userKey，查询未登录的购物车
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();
        String unloginKey = KEY_PREFIX + userKey;
        // 获取未登录的购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(unloginKey);
        List<Object> values = hashOps.values(); // json集合
        List<Cart> unLoginCarts = null;
        if (!CollectionUtils.isEmpty(values)){
            unLoginCarts = values.stream().map(cartJson -> {
                try {
                    Cart cart = MAPPER.readValue(cartJson.toString(), Cart.class);
                    cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId().toString())));
                    return cart;
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
        }

        // 2.获取UserId，判断userId是否为空
        Long userId = userInfo.getUserId();
        if (userId == null){
            return unLoginCarts;
        }

        // 3.合并购物车
        String loginKey = KEY_PREFIX + userId;
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(loginKey);
        if (!CollectionUtils.isEmpty(unLoginCarts)){
            unLoginCarts.forEach(cart -> {
                try {
                    if (loginHashOps.hasKey(cart.getSkuId().toString())){
                        String cartJson = loginHashOps.get(cart.getSkuId().toString()).toString();
                        BigDecimal count = cart.getCount();
                        cart = MAPPER.readValue(cartJson, Cart.class);
                        cart.setCount(cart.getCount().add(count));
                        this.asyncService.updateByUserIdAndSkuId(userId.toString(), cart, cart.getSkuId().toString());
                    } else {
                        cart.setUserId(userId.toString());
                        this.asyncService.addCart(userId.toString(), cart);
                    }
                    loginHashOps.put(cart.getSkuId().toString(), MAPPER.writeValueAsString(cart));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
        }

        // 4.删除未登录的购物车
        this.redisTemplate.delete(unloginKey);
        this.asyncService.deleteCartByUserId(userKey);

        // 5.查询登录状态的购物车
        List<Object> cartJsons = loginHashOps.values();
        if (!CollectionUtils.isEmpty(cartJsons)){
            return cartJsons.stream().map(cartJson -> {
                try {
                    Cart cart = MAPPER.readValue(cartJson.toString(), Cart.class);
                    cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId().toString())));
                    return cart;
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
        }

        return null;
    }

    public void updateNum(Cart cart) {
        String userId = this.getUserId();
        String key = KEY_PREFIX + userId;

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        if (hashOps.hasKey(cart.getSkuId().toString())){
            try {
                BigDecimal count = cart.getCount();
                String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
                cart = MAPPER.readValue(cartJson, Cart.class);
                cart.setCount(count);
                hashOps.put(cart.getSkuId().toString(), MAPPER.writeValueAsString(cart));
                this.asyncService.updateByUserIdAndSkuId(userId, cart, cart.getSkuId().toString());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

    }

    public void deleteCart(Long skuId) {
        String userId = this.getUserId();
        String key = KEY_PREFIX + userId;

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())){
            hashOps.delete(skuId.toString());
            this.asyncService.deleteCartByUserIdAndSkuId(userId, skuId);
        }
    }
}
