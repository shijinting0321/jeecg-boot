package org.jeecg.modules.demo.deviceparam.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.deviceparam.entity.IotDeviceParam;
import org.jeecg.modules.demo.deviceparam.service.IIotDeviceParamService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;

 /**
 * @Description: 设备实例
 * @Author: jeecg-boot
 * @Date:   2023-04-04
 * @Version: V1.0
 */
@Api(tags="设备实例")
@RestController
@RequestMapping("/deviceparam/iotDeviceParam")
@Slf4j
public class IotDeviceParamController extends JeecgController<IotDeviceParam, IIotDeviceParamService> {
	@Autowired
	private IIotDeviceParamService iotDeviceParamService;
	
	/**
	 * 分页列表查询
	 *
	 * @param iotDeviceParam
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "设备实例-分页列表查询")
	@ApiOperation(value="设备实例-分页列表查询", notes="设备实例-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<IotDeviceParam>> queryPageList(IotDeviceParam iotDeviceParam,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<IotDeviceParam> queryWrapper = QueryGenerator.initQueryWrapper(iotDeviceParam, req.getParameterMap());
		Page<IotDeviceParam> page = new Page<IotDeviceParam>(pageNo, pageSize);
		IPage<IotDeviceParam> pageList = iotDeviceParamService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param iotDeviceParam
	 * @return
	 */
	@AutoLog(value = "设备实例-添加")
	@ApiOperation(value="设备实例-添加", notes="设备实例-添加")
	@RequiresPermissions("deviceparam:iot_device_param:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody IotDeviceParam iotDeviceParam) {
		iotDeviceParamService.save(iotDeviceParam);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param iotDeviceParam
	 * @return
	 */
	@AutoLog(value = "设备实例-编辑")
	@ApiOperation(value="设备实例-编辑", notes="设备实例-编辑")
	@RequiresPermissions("deviceparam:iot_device_param:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody IotDeviceParam iotDeviceParam) {
		iotDeviceParamService.updateById(iotDeviceParam);
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
	@RequiresPermissions("deviceparam:iot_device_param:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		iotDeviceParamService.removeById(id);
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
	@RequiresPermissions("deviceparam:iot_device_param:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.iotDeviceParamService.removeByIds(Arrays.asList(ids.split(",")));
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
	public Result<IotDeviceParam> queryById(@RequestParam(name="id",required=true) String id) {
		IotDeviceParam iotDeviceParam = iotDeviceParamService.getById(id);
		if(iotDeviceParam==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(iotDeviceParam);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param iotDeviceParam
    */
    @RequiresPermissions("deviceparam:iot_device_param:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, IotDeviceParam iotDeviceParam) {
        return super.exportXls(request, iotDeviceParam, IotDeviceParam.class, "设备实例");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("deviceparam:iot_device_param:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, IotDeviceParam.class);
    }

}
