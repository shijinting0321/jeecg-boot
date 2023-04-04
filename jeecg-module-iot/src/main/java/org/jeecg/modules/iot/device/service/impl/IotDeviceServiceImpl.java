package org.jeecg.modules.iot.device.service.impl;

import org.jeecg.modules.iot.device.entity.IotDevice;
import org.jeecg.modules.iot.device.mapper.IotDeviceMapper;
import org.jeecg.modules.iot.device.service.IIotDeviceService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 设备实例
 * @Author: jeecg-boot
 * @Date:   2023-04-04
 * @Version: V1.0
 */
@Service
public class IotDeviceServiceImpl extends ServiceImpl<IotDeviceMapper, IotDevice> implements IIotDeviceService {

}
