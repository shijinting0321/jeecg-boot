package org.jeecg.modules.iot.gateway.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.jeecg.modules.iot.gateway.entity.IotGateway;
import org.jeecg.modules.iot.gateway.mapper.IotGatewayMapper;
import org.jeecg.modules.iot.gateway.service.IIotGatewayService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 网关管理
 * @Author: jeecg-boot
 * @Date:   2023-04-04
 * @Version: V1.0
 */
@Service
public class IotGatewayServiceImpl extends ServiceImpl<IotGatewayMapper, IotGateway> implements IIotGatewayService {

    @Override
    public boolean save(IotGateway entity) {
        //校验编码唯一性
        List<IotGateway> list = super.list(new LambdaQueryWrapper<IotGateway>().eq(IotGateway::getCode, entity.getCode()));
        if (CollectionUtils.isNotEmpty(list)){
          return false;
        }
        return super.save(entity);
    }
}
