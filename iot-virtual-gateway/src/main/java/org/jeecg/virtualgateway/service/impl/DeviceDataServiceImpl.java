package org.jeecg.virtualgateway.service.impl;

import org.jeecg.virtualgateway.entity.DeviceData;
import org.jeecg.virtualgateway.mapper.DeviceDataMapper;
import org.jeecg.virtualgateway.service.IDeviceDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author jtcl
 * @date 2022/6/17
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DeviceDataServiceImpl
        extends ServiceImpl<DeviceDataMapper, DeviceData> implements IDeviceDataService {
    @Override
    @Transactional(readOnly = true)
    public List<DeviceData> listData(int limit) {
        return list(new LambdaQueryWrapper<DeviceData>()
                .orderByAsc(DeviceData::getTimestamp)
                .last("limit " + limit));
    }
}
