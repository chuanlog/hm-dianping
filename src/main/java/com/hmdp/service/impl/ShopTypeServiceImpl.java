package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private ShopTypeMapper shopTypeMapper;
    @Resource
    private StringRedisTemplate  stringRedisTemplate;
    /**
     * 查询所有店铺类型
     *
     * @return Result
     */
    @Override
    public Result queryTypeList() {
        //先查询缓存
        String key = RedisConstants.SHOP_TYPE_KEY;
        List<String> shopTypes = stringRedisTemplate.opsForList().range(key, 0, -1);
        if (shopTypes != null && !shopTypes.isEmpty()) {
            //存在，返回
            List<ShopType> shopTypes1 = new ArrayList<>();
            for (String shopType : shopTypes) {
                ShopType bean = JSONUtil.toBean(shopType, ShopType.class);
                shopTypes1.add(bean);
            }
            return Result.ok(shopTypes1);
        }
        //不存在，查询数据库
        List<ShopType> shopTypes2 = query().orderByAsc("sort").list();
        if (shopTypes2 == null || shopTypes2.isEmpty()) {
            //不存在，返回错误
            return Result.fail("店铺类型不存在");
        }
        //数据库存在，写入缓存
        shopTypes2.forEach(shopType -> {
            stringRedisTemplate.opsForList().rightPush(key, JSONUtil.toJsonStr(shopType));
        });
        return Result.ok(shopTypes2);
    }
}
