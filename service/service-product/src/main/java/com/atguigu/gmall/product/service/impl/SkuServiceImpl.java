package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.atguigu.gmall.product.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.product.mapper.SkuImageMapper;
import com.atguigu.gmall.product.mapper.SkuMapper;
import com.atguigu.gmall.product.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.product.service.SkuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        // 保存sku，生成主键id
        skuMapper.insert(skuInfo);
        Long skuInfoId = skuInfo.getId();
        // 保存图片表中间表
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuInfoId);
            skuImageMapper.insert(skuImage);
        }
        // 保存平台属性中间表
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuInfoId);
            skuAttrValueMapper.insert(skuAttrValue);
        }
        // 保存销售属性中间表
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuInfoId);
            skuSaleAttrValue.setSpuId(skuInfo.getSpuId());

            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        }
    }

    @Override
    public Page<SkuInfo> getSkuList(Long page, Long limit) {
        Page<SkuInfo> skuInfoPage = new Page<>();
        skuInfoPage.setCurrent(page);
        skuInfoPage.setSize(limit);
        skuMapper.selectPage(skuInfoPage,null);
        return skuInfoPage;
    }

    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setIsSale(1);
        skuInfo.setId(skuId);
        skuMapper.updateById(skuInfo);
    }

    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuMapper.updateById(skuInfo);
    }

    @GmallCache
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        SkuInfo skuInfo = null;
        skuInfo = skuMapper.selectById(skuId);// mysql
        return skuInfo;
    }

    public SkuInfo getSkuInfoCache(Long skuId) {
        SkuInfo skuInfo = null;
        String key = "sku:" + skuId + ":info";
        //查询缓存
        skuInfo = (SkuInfo) redisTemplate.opsForValue().get(key);
        if (skuInfo==null){
            //在使用数据库之前，先获得分布式锁
            String lockKey = "sku:" + skuId + ":lock";
            String localValue = UUID.randomUUID().toString();
            Boolean ok = redisTemplate.opsForValue().setIfAbsent(lockKey,localValue,3, TimeUnit.SECONDS);//3秒后锁过期
            if (ok){
                //查询数据库
                skuInfo = skuMapper.selectById(skuId);
                if (skuInfo!=null){
                    //同步缓存
                    redisTemplate.opsForValue().set(key,skuInfo);
                }else {
                    redisTemplate.opsForValue().set(key,new SkuInfo(),10, TimeUnit.SECONDS);
                }
                //判断释放当前锁,在设置分布式锁的时候根据锁的value值删除锁，防止其他线程乱删
//                String localValueDel = (String) redisTemplate.opsForValue().get(lockKey);
//                if (StringUtils.isNotEmpty(localValueDel)&&localValueDel.equals(null)){
//                    //释放锁
//                    redisTemplate.delete(lockKey);
//                }

                // 在查询lockValue和对比lockValue时，或者在del命令执行的间隙，锁过期，导致乱删
                // 所以使用lua脚本对锁进行删除
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";// 脚本字符串
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();// 封装redis的lua脚本对象
                redisScript.setResultType(Long.class);// 脚本的返回值类型
                redisScript.setScriptText(script);
                redisTemplate.execute(redisScript, Arrays.asList(lockKey), localValue);
            }else {
                //自旋
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuInfo(skuId);
            }
        }
        return skuInfo;
    }

    @Override
    public List<SkuImage> getSkuInfoImageList(Long skuId) {
        QueryWrapper<SkuImage> skuImageQueryWrapper = new QueryWrapper<>();
        skuImageQueryWrapper.eq("sku_id",skuId);
        List<SkuImage> imageList = skuImageMapper.selectList(skuImageQueryWrapper);
        return imageList;
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuMapper.selectById(skuId);
        return skuInfo.getPrice();
    }

    @Override
    public List<Map<String, Object>> getSaleAttrValuesBySpu(Long spuId) {
        List<Map<String,Object>> mapList = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);
        return mapList;
    }
}
