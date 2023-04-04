package org.jeecg.modules.iot.gateway.service;

import org.jeecg.modules.iot.gateway.entity.IotGateway;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 网关管理
 * @Author: jeecg-boot
 * @Date:   2023-04-04
 * @Version: V1.0
 */
public interface IIotGatewayService extends IService<IotGateway> {

    boolean save(IotGateway entity);

}
