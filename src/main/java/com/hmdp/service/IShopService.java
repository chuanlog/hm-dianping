package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IShopService extends IService<Shop> {
    /**
     * 根据id查询店铺信息
     * @param id 商铺id
     * @return
     */
    Result queryById(Long id);

    /**
     * 更新商铺信息
     * @param shop
     * @return
     */
    Result update(Shop shop);

    /**
     * 根据id查询商铺信息, 逻辑过期解决缓存击穿
     * @param id
     * @return
     */
    Result queryByIdWithLogicExpire(Long id);
}
