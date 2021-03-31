package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartApiService;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@Service
public class CartApiServiceImpl implements CartApiService {

    @Autowired
    CartInfoMapper cartInfoMapper;
    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public void addCart(CartInfo cartInfo) {

        SkuInfo skuInfo = productFeignClient.getSkuInfo(cartInfo.getSkuId());

        cartInfo.setIsChecked(1);
        cartInfo.setSkuPrice(skuInfo.getPrice());
        cartInfo.setSkuName(skuInfo.getSkuName());
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setCartPrice(skuInfo.getPrice().multiply(new BigDecimal(cartInfo.getSkuNum())));

        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", cartInfo.getSkuId());
        queryWrapper.eq("sku_id", cartInfo.getSkuId());
        try {
            CartInfo cartInfoCache = cartInfoMapper.selectOne(queryWrapper);
            if (cartInfoCache == null) {
                cartInfoMapper.insert(cartInfo);
                //同步缓存
                redisTemplate.opsForHash().put("user:" + cartInfo.getUserId() + ":cart", cartInfo.getSkuId() + "", cartInfo);
            }else {
                cartInfoCache.setSkuNum(cartInfo.getSkuNum()+cartInfoCache.getSkuNum());
                cartInfoCache.setSkuPrice(skuInfo.getPrice());
                cartInfoCache.setCartPrice(skuInfo.getPrice().multiply(new BigDecimal(cartInfoCache.getSkuNum())));
                cartInfoMapper.updateById(cartInfoCache);
                //同步缓存
                redisTemplate.opsForHash().put("user:" + cartInfo.getUserId() + ":cart", cartInfo.getSkuId() + "", cartInfoCache);
            }
        } catch (Exception e) {
            System.out.println("数据库异常");
        }
    }

    @Override
    public List<CartInfo> getCartList(String userId) {

        List<CartInfo> cartInfos = null;
        cartInfos = redisTemplate.opsForHash().values("user:" + userId + ":cart");

        if (cartInfos == null || cartInfos.size() == 0) {
            QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            cartInfos = cartInfoMapper.selectList(queryWrapper);
            if (cartInfos != null && cartInfos.size() > 0) {
                HashMap<String, Object> map = new HashMap<>();
                //因为price字段不做冗余，所以购物车中的价格每次需要单独查询
                for (CartInfo cartInfo : cartInfos) {
                    BigDecimal skuPrice = productFeignClient.getSkuPrice(cartInfo.getSkuId());
                    cartInfo.setSkuPrice(skuPrice);
                    map.put(cartInfo.getSkuId() + "", cartInfo);
                }
                redisTemplate.opsForHash().putAll("user:" + userId + ":cart", map);
            }
        }
        return cartInfos;
    }

    @Override
    public void checkCart(CartInfo cartInfo) {
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", cartInfo.getUserId());
        queryWrapper.eq("sku_id", cartInfo.getSkuId());
        cartInfoMapper.update(cartInfo, queryWrapper);

        //List<CartInfo> values = (List<CartInfo>)redisTemplate.opsForHash().values("user:" + cartInfo.getUserId() + ":cart");获取所有值
        CartInfo cacheCart = (CartInfo) redisTemplate.opsForHash().get("user:" + cartInfo.getUserId() + ":cart", cartInfo.getSkuId() + "");
        cacheCart.setIsChecked(cartInfo.getIsChecked());
        redisTemplate.opsForHash().put("user:" + cartInfo.getUserId() + ":cart", cartInfo.getSkuId() + "", cacheCart);
    }
}
