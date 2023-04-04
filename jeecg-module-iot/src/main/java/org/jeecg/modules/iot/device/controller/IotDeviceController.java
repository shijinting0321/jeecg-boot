package org.jeecg.modules.iot.device.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.iot.device.entity.IotDevice;
import org.jeecg.modules.iot.device.service.IIotDeviceService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

/**
 * @Description: 设备实例
 * @Author: jeecg-boot
 * @Date:   2023-04-04
 * @Version: V1.0
 */
@Api(tags="设备实例")
@RestController
@RequestMapping("/device/iotDevice")
@Slf4j
public class IotDeviceController extends JeecgController<IotDevice, IIotDeviceService> {
	@Autowired
	private IIotDeviceService iotDeviceService;
	
	/**
	 * 分页列表查询
	 *
	 * @param iotDevice
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "设备实例-分页列表查询")
	@ApiOperation(value="设备实例-分页列表查询", notes="设备实例-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<IotDevice>> queryPageList(IotDevice iotDevice,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<IotDevice> queryWrapper = QueryGenerator.initQueryWrapper(iotDevice, req.getParameterMap());
		Page<IotDevice> page = new Page<IotDevice>(pageNo, pageSize);
		IPage<IotDevice> pageList = iotDeviceService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param iotDevice
	 * @return
	 */
	@AutoLog(value = "设备实例-添加")
	@ApiOperation(value="设备实例-添加", notes="设备实例-添加")
	//@RequiresPermissions("device:iot_device:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody IotDevice iotDevice) {
		iotDeviceService.save(iotDevice);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param iotDevice
	 * @return
	 */
	@AutoLog(value = "设备实例-编辑")
	@ApiOperation(value="设备实例-编辑", notes="设备实例-编辑")
	//@RequiresPermissions("device:iot_device:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody IotDevice iotDevice) {
		iotDeviceService.updateById(iotDevice);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "设备实例-通过id删除")
	@ApiOperation(value="设备实例-通过id删除", notes="设备实例-通过id删除")
	//@RequiresPermissions("device:iot_device:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		iotDeviceService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "设备实例-批量删除")
	@ApiOperation(value="设备实例-批量删除", notes="设备实例-批量删除")
	//@RequiresPermissions("device:iot_device:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.iotDeviceService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "设备实例-通过id查询")
	@ApiOperation(value="设备实例-通过id查询", notes="设备实例-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<IotDevice> queryById(@RequestParam(name="id",required=true) String id) {
		IotDevice iotDevice = iotDeviceService.getById(id);
		if(iotDevice==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(iotDevice);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param iotDevice
    */
    //@RequiresPermissions("device:iot_device:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, IotDevice iotDevice) {
        return super.exportXls(request, iotDevice, IotDevice.class, "设备实例");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("device:iot_device:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, IotDevice.class);
    }

}
