package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    /**
     * @param voucherId
     * @return
     */
    @Override
    public Result sekillVoucher(Long voucherId) {
        //查数据库中的优惠券信息
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        //进行合法性检测
        if (seckillVoucher == null) {
            return Result.fail("优惠券不存在");
        }
        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("优惠券尚未开始");
        }
        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("优惠券已结束");
        }
        if (seckillVoucher.getStock() < 1) {
            return Result.fail("优惠券已售罄");
        }
        
        Long userId = UserHolder.getUser().getId();
        // 使用synchronized锁定用户ID
        synchronized (userId.toString().intern()) {
            // 获取当前类的代理对象，确保事务生效
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.creatVoutherOrder(voucherId);
        }
    }

    @Transactional
    public Result creatVoutherOrder(Long voucherId) {
        // 一人一单，查询订单，判断是否存在
        Long userId = UserHolder.getUser().getId();
        int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        if (count > 0) {
            return Result.fail("已购买该订单，无法再次购买");
        }
        //扣库存，建立订单
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();
        if (!success) {
            return Result.fail("购买失败");
        }
        VoucherOrder voucherOrder = new VoucherOrder();
        long id = redisIdWorker.nextId("order");
        voucherOrder.setId(id);
        voucherOrder.setUserId(UserHolder.getUser().getId());
        voucherOrder.setVoucherId(voucherId);
        save(voucherOrder);
        return Result.ok(id);
    }
}
