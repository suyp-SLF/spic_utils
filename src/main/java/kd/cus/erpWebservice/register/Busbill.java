package kd.cus.erpWebservice.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import com.alibaba.fastjson.JSON;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.mservice.webapi.OperationWebApi;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.service.ServiceFactory;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.cus.erpWebservice.action.WebserviceMainTemplate;
import kd.cus.erpWebservice.action.entity.ResultEntity;

/**
 * 暂估应付单 2020.9.16
 * 
 * @author ZXR
 */
public class Busbill implements WebserviceMainTemplate {

	@Override
	public List<ResultEntity> disposeDate(List<Element> children, String p_source) throws Exception {

		List<ResultEntity> resultEnties = new ArrayList<ResultEntity>(); // disposeDate方法返回结果
		Map<String, String> measureunitsMap = new HashMap<String, String>(); // 计量单位map<name，number>返回结果
		List<String> iscodeidList = new ArrayList<>();// 存所有codeid编码
		List<String> isMeasureunitList = new ArrayList<>();// 存所有name计量单位
		Map<String, Object> maps = new HashMap<String, Object>();// 调用标准接口map
		List<Object> listData = new ArrayList<>();// "datas":[{}] 调用标准接口放maps
		List<String> codeExist = new ArrayList<>();// 查询已存在的codeid
		List<String> nocodeExist = new ArrayList<>();// 不存在的codeid

		// 遍历codeid、基本单位name存List里
		for (Element dataInfo : children) {
			// codeid
			if (!dataInfo.element("busbill").element("codeid").getText().isEmpty()) {
				iscodeidList.add(dataInfo.element("busbill").element("codeid").getText());
			} else {
				resultEnties.add(ResultEntity.PROCESS_ERROR("codeid不允许空").setInitDate("", "", ""));
			}
			// 计量单位
			if (!dataInfo.element("busbill").elements("payentry").isEmpty()) {
				List<Element> payentrys = dataInfo.element("busbill").elements("payentry");// 获得多条单据体
				for (Element payentry : payentrys) {
					if (!payentry.element("e_measureunit").getText().isEmpty()) {
						isMeasureunitList.add(payentry.element("e_measureunit").getText());// 计量单位
					}
				}
			} else {
				resultEnties.add(ResultEntity.PROCESS_ERROR("没找到<payentry>分录节点").setInitDate("", "", ""));
			}
		}
		// 校验codeid重复 和 计量单位 放map中
		validateMap(iscodeidList, isMeasureunitList, measureunitsMap, resultEnties, codeExist);

		// 处理数据
		for (Element dataInfo : children) {

			String codeid = dataInfo.element("busbill").element("codeid").getText(); // 转固单号
			if (codeExist.contains(codeid)) {// codeid重复，退出本次循环
				continue;
			} else {// 不存在的codeid
				nocodeExist.add(codeid);
			}

			// 字段为空校验，退出本次循环
			StringBuffer stringBuffer = doValidate(dataInfo);
			if (null != stringBuffer && stringBuffer.length() > 0) {
				resultEnties.add(ResultEntity.PROCESS_ERROR(stringBuffer.toString()).setInitDate(codeid, "", ""));
				continue;
			}
			doExecute(dataInfo, listData, p_source, measureunitsMap);// 封装数据
		}

		maps.put("datas", listData);
		maps.put("formid", "ap_busbill");
		 String string = JSON.toJSONString(maps);

		// 存在满足条件的codeid，将数据存入标准接口
		if (nocodeExist.size() > 0) {
			// 调用标准接口
			Map<String, Object> apiresultMap = ServiceFactory.getService(OperationWebApi.class)
					.executeOperation("ap_busbill", "save", maps);

			// 解析执行结果
			List<Map<String, Object>> datas = (List<Map<String, Object>>) apiresultMap.get("data");
			for (Map<String, Object> result : datas) {
				// 通过dindex获取codeid
				String gitListCodeid = nocodeExist.get((int) result.get("dindex"));

				if ((Boolean) result.get("success")) {// 成功
					resultEnties.add(ResultEntity.SUCCESS().setInitDate(gitListCodeid, "", ""));
				}

				if (!(Boolean) result.get("success")) {// 失败

					if (null != result.get("message")) {// message 错误提示
						resultEnties.add(ResultEntity.PROCESS_ERROR((String) result.get("message"))
								.setInitDate(gitListCodeid, "", ""));

					} else if (null != result.get("convertResult")) {// convertResult 错误提示
						Map<Integer, Object> convertResult = (Map<Integer, Object>) result.get("convertResult");
						resultEnties.add(ResultEntity.PROCESS_ERROR(convertResult.get(0).toString())
								.setInitDate(gitListCodeid, "", ""));

					} else {
						resultEnties.add(ResultEntity.PROCESS_ERROR("其他错误！").setInitDate("", "", ""));
					}
				}
			}
		}

		return resultEnties;
	}

	/* 功能:将XML数据封装到Map类型 */
	private void doExecute(Element dataInfo, List<Object> listData, String p_source, Map<String, String> measureunitMap)
			throws Exception {

		Map<String, Object> mapUnit = new HashMap<String, Object>();// 放基本单元的数据
		dataInfo = dataInfo.element("busbill");
		String codeid = dataInfo.element("codeid").getText();// codeid
		String org = dataInfo.element("org").getText();// 结算组织
		String billtype = dataInfo.element("billtype").getText(); // 单据类型
		String bizdate = dataInfo.element("bizdate").getText(); // 业务日期
		String asstacttype = dataInfo.element("asstacttype").getText(); // 往来类型
		String asstact = dataInfo.element("asstact").getText(); // 往来户
		String payorg = dataInfo.element("payorg").getText();// 付款组织
		String purmode = dataInfo.element("purmode").getText(); // 付款方式
		String department = dataInfo.element("department").getText();// 部门
		String currency = dataInfo.element("currency").getText(); // 币别

		// ------------------------单据头-----------------------------------
		// 结算组织 基础资料
		Map<String, Object> mapOrg = new HashMap<String, Object>();// 放基本单元的数据
		mapOrg.put("number", org);
		mapUnit.put("org", mapOrg);

		mapUnit.put("status", "A");// 状态 已审核
		//mapUnit.put("billno", codeid);// TO：暂放，测试环境用编码规则
		// 单据类型 基础资料
		Map<String, Object> mapBilltype = new HashMap<String, Object>();// 放基本单元的数据
		mapBilltype.put("number", billtype);
		mapUnit.put("billtype", mapBilltype);

		// 业务日期
		mapUnit.put("bizdate", bizdate);

		// 往来类型
		if("bd_supplier".equals(asstacttype)||"bos_user".equals(asstacttype)||"bd_customer".equals(asstacttype)) {
			mapUnit.put("asstacttype", asstacttype);
		}else {
			mapUnit.put("asstacttype", "bd_supplier");
		}
		

		// 往来户 基础资料
		Map<String, Object> mapAsstact = new HashMap<String, Object>();// 放基本单元的数据
		mapAsstact.put("number", asstact);
		mapUnit.put("asstact", mapAsstact);

		// 付款组织 基础资料
		Map<String, Object> mapPayorg = new HashMap<String, Object>();// 放基本单元的数据
		mapPayorg.put("number", payorg);
		mapUnit.put("payorg", mapPayorg);

		// 付款方式
		mapUnit.put("purmode", purmode);// 源编码

		// 付款组织 基础资料
		Map<String, Object> mapDepartment = new HashMap<String, Object>();// 放基本单元的数据
		mapDepartment.put("number", department);
		mapUnit.put("department", mapDepartment);

		// 币别 基础资料
		Map<String, Object> mapCurrency = new HashMap<String, Object>();// 放基本单元的数据
		mapCurrency.put("number", currency);
		mapUnit.put("currency", mapCurrency);

		 // 因标准接口BUG，币别1，汇率表必须传人民币，标准接口补丁发过来可以删除这个字段
		 Map<String, Object> mapExratetable = new HashMap<String, Object>();//
		 //放基本单元的数据
		 mapExratetable.put("number", "ERT-01");
		 mapUnit.put("exratetable", mapExratetable);
		
		mapUnit.put("exchangerate", "1");// 汇率

		// 因标准接口要求必须写款项性质，但是节点无法传递，在代码里设置 材料采购 死值。
		Map<String, Object> mapPayproperty = new HashMap<String, Object>();// 放基本单元的数据
		mapPayproperty.put("number", "2001");
		mapUnit.put("payproperty", mapPayproperty);

		mapUnit.put("spic_sourceid", codeid);// 源编码
		mapUnit.put("spic_source", p_source);// 来源单位

		// ---------------------单据体-明细分录---------------------

		List<Element> payentrys = dataInfo.elements("payentry");// 获得多条单据体
		List<Object> listGroup = new ArrayList<>();
		for (Element payentry : payentrys) {

			String e_material = payentry.element("e_material").getText();// 物料
			String e_expenseitem = payentry.element("e_expenseitem").getText();// 费用项目
			String e_measureunit = payentry.element("e_measureunit").getText();// 计量单位
			String e_quantity = payentry.element("e_quantity").getText();// 数量
			String e_ispresent = payentry.element("e_ispresent").getText();// 是否赠品
			String e_unitprice = payentry.element("e_unitprice").getText();// 单价
			String e_taxunitprice = payentry.element("e_taxunitprice").getText();// 含税单价
			String taxrateid = payentry.element("taxrateid").getText();// 税率
			String e_corebilltype = payentry.element("e_corebilltype").getText();// 核心单据类型
			String e_corebillno = payentry.element("e_corebillno").getText();// 核心单据号

			Map<String, Object> mapGroup = new HashMap<String, Object>();// 放分组的数据

			// 物料 基础资料
			Map<String, Object> mapE_material = new HashMap<String, Object>();// 放基本单元的数据
			mapE_material.put("number", e_material);
			mapGroup.put("e_material", mapE_material);

			mapGroup.put("e_expenseitem", e_expenseitem);

			// 基本单位 基础资料 传来的name
			Map<String, Object> mapBaseunit = new HashMap<String, Object>();// 放基本单元的数据

			// 计量单位 根据name获取number
			if (null != measureunitMap && null != measureunitMap.get(e_measureunit)) {
				mapBaseunit.put("number", (String) measureunitMap.get(e_measureunit));
			} else {
				mapBaseunit.put("number", e_measureunit);
			}

			mapGroup.put("e_measureunit", mapBaseunit);

			mapGroup.put("e_quantity", e_quantity);// 数量
			mapGroup.put("e_ispresent", Boolean.parseBoolean(e_ispresent));// 是否赠品
			mapGroup.put("e_unitprice", e_unitprice);// 单价
			mapGroup.put("e_taxunitprice", e_taxunitprice);// 含税单价
			mapGroup.put("taxrateid", taxrateid);// 税率
			mapGroup.put("e_corebilltype", e_corebilltype);// 核心单据类型
			mapGroup.put("e_corebillno", e_corebillno);// 核心单据号

			listGroup.add(mapGroup);

		}
		mapUnit.put("entry", listGroup);
		// --------------------------------------------------------------------
		listData.add(mapUnit);// 放 key data 的value中
	}

	/* 功能:校验空字段 返回结果：将封装到StringBuffer中 */
	private StringBuffer doValidate(Element dataInfo) throws Exception {

		Element elementPB = dataInfo.element("busbill");
		String codeid = elementPB.element("codeid").getText();
		StringBuffer stringBuffer = new StringBuffer();

		// -------------------单据头 校验-----------------------
		if (codeid.isEmpty()) {
			stringBuffer.append("&请填写codeid!");
		}
		if (elementPB.element("billtype").getText().isEmpty()) {
			stringBuffer.append("&请填写单据类型!");
		}
		if (elementPB.element("bizdate").getText().isEmpty()) {
			stringBuffer.append("&请填写业务日期!");
		}
		if (elementPB.element("asstacttype").getText().isEmpty()) {
			stringBuffer.append("&请填写往来类型!");
		}
		if (elementPB.element("asstact").getText().isEmpty()) {
			stringBuffer.append("&请填写往来户!");
		}
		if (elementPB.element("purmode").getText().isEmpty()) {
			stringBuffer.append("&请填写付款方式!");
		}
		if (elementPB.element("department").getText().isEmpty()) {
			stringBuffer.append("&请填写部门!");
		}

		// --------------------单据体--资产信息--分录---------------
		List<Element> payentry = elementPB.elements("payentry");

		for (Element paycode : payentry) {

			if (paycode.element("e_material").getText().isEmpty()) {
				stringBuffer.append("&请填写物料编码!");
			}
			if (paycode.element("e_measureunit").getText().isEmpty()) {
				stringBuffer.append("&请填写计量单位!");
			}
			if (paycode.element("e_quantity").getText().isEmpty()) {
				stringBuffer.append("&请填写数量!");
			}
			if (paycode.element("e_ispresent").getText().isEmpty()) {
				stringBuffer.append("&请填写是否赠品!");
			}
			if (paycode.element("e_unitprice").getText().isEmpty()) {
				stringBuffer.append("&请填写单价!");
			}
			if (paycode.element("e_taxunitprice").getText().isEmpty()) {
				stringBuffer.append("&请填写含税单价!");
			}
			if (paycode.element("taxrateid").getText().isEmpty()) {
				stringBuffer.append("&请填写税率!");
			}
			if (paycode.element("e_corebilltype").getText().isEmpty()) {
				stringBuffer.append("&请填写核心单据类型!");
			}
			if (paycode.element("e_corebillno").getText().isEmpty()) {
				stringBuffer.append("&请填写核心单据号!");
			}
		}

		return stringBuffer;
	}

	/*
	 * 功能：1.查询codeid编码是否存在，存在报错。2.将计量单位基础资料放measureunitsMap<"name","number">映射
	 * 入参：isAllcodeidList：所有codeid字段；isMaList：所有计量单位name字段；measureunitsMap：同上；
	 * resultEnties：返回结果；codeExist：存在的codeid编码
	 */
	private void validateMap(List<String> isAllcodeidList, List<String> isMaList, Map<String, String> measureunitsMap,
			List<ResultEntity> resultEnties, List<String> codeExist) {

		// 判断codeid是否存在
		DynamicObjectCollection dynamicObject3 = QueryServiceHelper.query("ap_busbill", "spic_sourceid",
				new QFilter[] { new QFilter("spic_sourceid", QCP.in, isAllcodeidList) });
		if (null != dynamicObject3) {
			for (DynamicObject kString : dynamicObject3) {
				resultEnties.add(ResultEntity.PROCESS_ERROR("暂估应付单号已存在").setInitDate(kString.getString("spic_sourceid"),
						"", ""));
				codeExist.add(kString.getString("spic_sourceid"));// 存在的编码
			}
		}

		// 计量单位 measureunitsMap<name,value>
		DynamicObjectCollection dynamicObjectma = QueryServiceHelper.query("bd_measureunits", "id,number,name",
				new QFilter[] { new QFilter("name", QCP.in, isMaList) });
		if (null != dynamicObjectma) {
			for (DynamicObject kString : dynamicObjectma) {
				// key不是空， valuse不是空
				if (!kString.getString("name").isEmpty() && !kString.getString("number").isEmpty()) {
					if (null != measureunitsMap) {
						if (!measureunitsMap.containsKey(kString.getString("name"))) {// 判断map key 中是否存在此name
							measureunitsMap.put(kString.getString("name"), kString.getString("number"));// name 和 number
						}
					} else {
						measureunitsMap.put(kString.getString("name"), kString.getString("number"));// name 和 number
					}

				}
			}
		}
	}

}
