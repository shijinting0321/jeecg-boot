package org.jeecg.modules.iot.plugins.controller;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.iot.plugins.entity.IotProtocolPlugins;
import org.jeecg.modules.iot.plugins.service.IIotProtocolPluginsService;

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
 * @Description: 协议插件管理
 * @Author: jeecg-boot
 * @Date:   2023-04-04
 * @Version: V1.0
 */
@Api(tags="协议插件管理")
@RestController
@RequestMapping("/plugins/iotProtocolPlugins")
@Slf4j
public class IotProtocolPluginsController extends JeecgController<IotProtocolPlugins, IIotProtocolPluginsService> {
	@Autowired
	private IIotProtocolPluginsService iotProtocolPluginsService;
	
	/**
	 * 分页列表查询
	 *
	 * @param iotProtocolPlugins
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "协议插件管理-分页列表查询")
	@ApiOperation(value="协议插件管理-分页列表查询", notes="协议插件管理-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<IotProtocolPlugins>> queryPageList(IotProtocolPlugins iotProtocolPlugins,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<IotProtocolPlugins> queryWrapper = QueryGenerator.initQueryWrapper(iotProtocolPlugins, req.getParameterMap());
		Page<IotProtocolPlugins> page = new Page<IotProtocolPlugins>(pageNo, pageSize);
		IPage<IotProtocolPlugins> pageList = iotProtocolPluginsService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param iotProtocolPlugins
	 * @return
	 */
	@AutoLog(value = "协议插件管理-添加")
	@ApiOperation(value="协议插件管理-添加", notes="协议插件管理-添加")
	//@RequiresPermissions("plugins:iot_protocol_plugins:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody IotProtocolPlugins iotProtocolPlugins) {
		iotProtocolPluginsService.save(iotProtocolPlugins);
		return Result.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param iotProtocolPlugins
	 * @return
	 */
	@AutoLog(value = "协议插件管理-编辑")
	@ApiOperation(value="协议插件管理-编辑", notes="协议插件管理-编辑")
	//@RequiresPermissions("plugins:iot_protocol_plugins:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody IotProtocolPlugins iotProtocolPlugins) {
		iotProtocolPluginsService.updateById(iotProtocolPlugins);
		return Result.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "协议插件管理-通过id删除")
	@ApiOperation(value="协议插件管理-通过id删除", notes="协议插件管理-通过id删除")
	//@RequiresPermissions("plugins:iot_protocol_plugins:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		iotProtocolPluginsService.removeById(id);
		return Result.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "协议插件管理-批量删除")
	@ApiOperation(value="协议插件管理-批量删除", notes="协议插件管理-批量删除")
	//@RequiresPermissions("plugins:iot_protocol_plugins:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.iotProtocolPluginsService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "协议插件管理-通过id查询")
	@ApiOperation(value="协议插件管理-通过id查询", notes="协议插件管理-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<IotProtocolPlugins> queryById(@RequestParam(name="id",required=true) String id) {
		IotProtocolPlugins iotProtocolPlugins = iotProtocolPluginsService.getById(id);
		if(iotProtocolPlugins==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(iotProtocolPlugins);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param iotProtocolPlugins
    */
    //@RequiresPermissions("plugins:iot_protocol_plugins:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, IotProtocolPlugins iotProtocolPlugins) {
        return super.exportXls(request, iotProtocolPlugins, IotProtocolPlugins.class, "协议插件管理");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    //@RequiresPermissions("plugins:iot_protocol_plugins:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, IotProtocolPlugins.class);
    }

}
