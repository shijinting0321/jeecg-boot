package org.jeecg.virtualgateway.service;

import org.jeecg.virtualgateway.entity.DeviceData;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author jtcl
 * @date 2022/6/7
 */
public interface IDeviceDataService extends IService<DeviceData> {
    /**
     * 查询设备数据
     *
     * @param limit 查询条数
     * @return 设备数据
     */
    List<DeviceData> listData(int limit);
}
