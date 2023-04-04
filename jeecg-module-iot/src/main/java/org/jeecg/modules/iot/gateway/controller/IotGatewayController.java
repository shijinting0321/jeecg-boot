package org.jeecg.modules.iot.gateway.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.iot.gateway.entity.IotGateway;
import org.jeecg.modules.iot.gateway.service.IIotGatewayService;

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
 * @Description: 网关管理
 * @Author: jeecg-boot
 * @Date:   2023-04-04
 * @Version: V1.0
 */
@Api(tags="网关管理")
@RestController
@RequestMapping("/gateway/iotGateway")
@Slf4j
public class IotGatewayController extends JeecgController<IotGateway, IIotGatewayService> {
	@Autowired
	private IIotGatewayService iotGatewayService;
	
	/**
	 * 分页列表查询
	 *
	 * @param iotGateway
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "网关管理-分页列表查询")
	@ApiOperation(value="网关管理-分页列表查询", notes="网关管理-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<IotGateway>> queryPageList(IotGateway iotGateway,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<IotGateway> queryWrapper = QueryGenerator.initQueryWrapper(iotGateway, req.getParameterMap());
		Page<IotGateway> page = new Page<IotGateway>(pageNo, pageSize);
		IPage<IotGateway> pageList = iotGatewayService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param iotGateway
	 * @return
	 */
	@AutoLog(value = "网关管理-添加")
	@ApiOperation(value="网关管理-添加", notes="网关管理-添加")
	//@RequiresPermissions("gateway:iot_gateway:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody IotGateway iotGateway) {
		iotGatewayService.save(iotGateway);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param iotGateway
	 * @return
	 */
	@AutoLog(value = "网关管理-编辑")
	@ApiOperation(value="网关管理-编辑", notes="网关管理-编辑")
	//@RequiresPermissions("gateway:iot_gateway:edit")
	@PutMapping(value = "/edit")
	public Result<String> edit(@RequestBody IotGateway iotGateway) {
		boolean b = iotGatewayService.updateById(iotGateway);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "网关管理-通过id删除")
	@ApiOperation(value="网关管理-通过id删除", notes="网关管理-通过id删除")
	//@RequiresPermissions("gateway:iot_gateway:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		iotGatewayService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "网关管理-批量删除")
	@ApiOperation(value="网关管理-批量删除", notes="网关管理-批量删除")
	//@RequiresPermissions("gateway:iot_gateway:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.iotGatewayService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "网关管理-通过id查询")
	@ApiOperation(value="网关管理-通过id查询", notes="网关管理-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<IotGateway> queryById(@RequestParam(name="id",required=true) String id) {
		IotGateway iotGateway = iotGatewayService.getById(id);
		if(iotGateway==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(iotGateway);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param iotGateway
    */
    //@RequiresPermissions("gateway:iot_gateway:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, IotGateway iotGateway) {
        return super.exportXls(request, iotGateway, IotGateway.class, "网关管理");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("gateway:iot_gateway:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, IotGateway.class);
    }

}
