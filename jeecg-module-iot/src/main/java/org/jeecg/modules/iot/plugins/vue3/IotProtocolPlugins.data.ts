import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '租户号',
    align:"center",
    dataIndex: 'tenantId'
   },
   {
    title: '乐观锁',
    align:"center",
    dataIndex: 'version'
   },
   {
    title: '创建部门',
    align:"center",
    dataIndex: 'createDept'
   },
   {
    title: '更新IP',
    align:"center",
    dataIndex: 'updateIp'
   },
   {
    title: '逻辑删除标记;0-未删除 1-已删除',
    align:"center",
    dataIndex: 'delFlag'
   },
   {
    title: '排序',
    align:"center",
    dataIndex: 'sort'
   },
   {
    title: '备注',
    align:"center",
    dataIndex: 'remark'
   },
   {
    title: '协议插件编码',
    align:"center",
    dataIndex: 'pluginCode'
   },
   {
    title: '名称',
    align:"center",
    dataIndex: 'name'
   },
   {
    title: '描述',
    align:"center",
    dataIndex: 'description'
   },
   {
    title: '协议文件',
    align:"center",
    dataIndex: 'file'
   },
   {
    title: '状态',
    align:"center",
    dataIndex: 'status'
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '租户号',
    field: 'tenantId',
    component: 'Input',
  },
  {
    label: '乐观锁',
    field: 'version',
    component: 'Input',
  },
  {
    label: '创建部门',
    field: 'createDept',
    component: 'Input',
  },
  {
    label: '更新IP',
    field: 'updateIp',
    component: 'Input',
  },
  {
    label: '逻辑删除标记;0-未删除 1-已删除',
    field: 'delFlag',
    component: 'InputNumber',
  },
  {
    label: '排序',
    field: 'sort',
    component: 'InputNumber',
  },
  {
    label: '备注',
    field: 'remark',
    component: 'Input',
  },
  {
    label: '协议插件编码',
    field: 'pluginCode',
    component: 'Input',
  },
  {
    label: '名称',
    field: 'name',
    component: 'Input',
  },
  {
    label: '描述',
    field: 'description',
    component: 'Input',
  },
  {
    label: '协议文件',
    field: 'file',
    component: 'Input',
  },
  {
    label: '状态',
    field: 'status',
    component: 'Input',
  },
	// TODO 主键隐藏字段，目前写死为ID
	{
	  label: '',
	  field: 'id',
	  component: 'Input',
	  show: false
	},
];



/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}