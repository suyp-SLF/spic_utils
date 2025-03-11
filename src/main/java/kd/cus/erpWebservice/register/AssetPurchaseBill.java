package kd.cus.erpWebservice.register;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.dom4j.Element;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.operate.result.IOperateInfo;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.service.business.datamodel.DynamicFormModelProxy;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.cus.api.SelectUtils;
import kd.cus.api.entity.FilterEntity;
import kd.cus.erpWebservice.action.WebserviceMainTemplate;
import kd.cus.erpWebservice.action.entity.ResultEntity;

/**
 * 采购转固单 /固定资产 2020.9.11
 * 
 * @author ZXR
 *
 */
public class AssetPurchaseBill implements WebserviceMainTemplate {

	@Override
	public List<ResultEntity> disposeDate(List<Element> children, String p_source) throws Exception {
		List<ResultEntity> resultEnties = new ArrayList<ResultEntity>();
		List<DynamicObject> assets = new ArrayList<DynamicObject>();

		List<String> iscodeidList = new ArrayList<>();// 存所有codeid编码
		List<String> codeExist = new ArrayList<>();// 查询已存在的codeid
		List<String> nocodeExist = new ArrayList<>();// 不存在的codeid

		List<FilterEntity> filterEntities = new ArrayList<>();// 传入数据
		Map<String, DynamicObject> map = new HashMap<>();// 放基础资料返回结果

		// codeid放iscodeidList集合，基础资料获取出来放map中
		for (Element dataInfo : children) {
			// codeid
			if (!dataInfo.element("purchasebill").element("codeid").getText().isEmpty()) {
				iscodeidList.add(dataInfo.element("purchasebill").element("codeid").getText());
			} else {
				resultEnties.add(ResultEntity.PROCESS_ERROR("codeid不允许空").setInitDate("", "", ""));
			}

			Element elementPB = dataInfo.element("purchasebill");

			filterEntities.add(new FilterEntity("bos_org", "number", elementPB.element("assetunit").getText()));// 资产组织
			filterEntities.add(new FilterEntity("bos_org", "number", elementPB.element("checkOrg").getText()));// 核算范围
			filterEntities.add(new FilterEntity("bos_user", "number", elementPB.element("handler").getText()));// 经办人
			filterEntities.add(new FilterEntity("bos_adminorg", "number", elementPB.element("handleorg").getText()));// 经办部门
			// 单据体中的基础资料
			List<Element> assetsentrys = elementPB.elements("assetsentry");
			for (Element asscode : assetsentrys) {
				filterEntities
						.add(new FilterEntity("fa_assetcategory", "number", asscode.element("assetcat").getText()));// 资产类别
				filterEntities.add(new FilterEntity("bd_measureunits", "name", asscode.element("unit").getText()));// 计量单位
				filterEntities
						.add(new FilterEntity("fa_storeplace", "number", asscode.element("storeplace").getText()));// 存放地点
				filterEntities.add(new FilterEntity("bd_supplier", "number", asscode.element("supplier").getText())); // 供应商
			}
		
		}
		map = SelectUtils.loadAll(map, filterEntities);
		
		// 校验codeid重复 和 计量单位 放map中
		validateCodeidMap(iscodeidList, resultEnties, codeExist);

		for (Element dataInfo : children) {

			String codeid = dataInfo.element("purchasebill").element("codeid").getText(); // 转固单号

			// codeid重复，退出本次循环
			if (codeExist.contains(codeid)) {
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

			// 新增
			Map<Class<?>, Object> services = new HashMap<>();
			DynamicFormModelProxy model = new DynamicFormModelProxy("fa_purchasebill", UUID.randomUUID().toString(),
					services);
			model.createNewData();
			DynamicObject assetsFromDB = model.getDataEntity();
			assetsFromDB.set("id", codeid); // FID

			// 来源
			if (!p_source.isEmpty()) {
				assetsFromDB.set("spic_source", p_source);
			}

			// 封装数据，没找到的基础资料摘出来结束循环
			StringBuffer sBuffer = doExecute(dataInfo, assetsFromDB, map);
			if (null != sBuffer && sBuffer.length() > 0) {
				resultEnties.add(ResultEntity.PROCESS_ERROR(sBuffer.toString()).setInitDate(codeid, "", ""));
				continue;
			} else {// 通过校验，进入保存方法中
				assets.add(assetsFromDB);
			}
		}
		if (nocodeExist.size() > 0) {
			resultEnties = saveResult(assets, resultEnties);
		}
		return resultEnties;
	}

	// 存数据操作、基础资料字段校验
	private StringBuffer doExecute(Element dataInfo, DynamicObject assetObj, Map<String, DynamicObject> map)
			throws Exception {

		ResultEntity resultEnties = null;
		StringBuffer stringBuffer = new StringBuffer();// 存放返回信息

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Element elementPB = dataInfo.element("purchasebill");
		String codeid = elementPB.element("codeid").getText();
		String assetunit = elementPB.element("assetunit").getText(); // 资产组织
		String checkOrg = elementPB.element("checkOrg").getText(); // 核算范围
		String purchasedate = elementPB.element("purchasedate").getText();// 业务日期
		String buildway = elementPB.element("buildway").getText(); // 建卡方式
		String handler = elementPB.element("handler").getText();// 经办人
		String handleorg = elementPB.element("handleorg").getText(); // 经办部门

		// -------------------单据头-----------------------
		// 资产组织
		DynamicObject aassetOrgDy = map.get("bos_org" + "@_@" + "number" + "@_@" + assetunit);
		// map没值、职能没值、不是资产组织
		if (null == aassetOrgDy ) {
			stringBuffer.append("&没找到资产组织!");
		}
		assetObj.set("assetunit", aassetOrgDy);

		assetObj.set("spic_sourceid", codeid); // 来源编码

		// 核算组织 业务单元
		DynamicObject checkOrgDy = map.get("bos_org" + "@_@" + "number" + "@_@" + checkOrg);
		// map没值、职能没值、不是核算组织
		if (null == checkOrgDy) {
			stringBuffer.append("&没找到核算组织!");
		}
		assetObj.set("org", checkOrgDy); // 核算组织 组织 业务单元

		assetObj.set("purchasedate", dateFormat.parse(purchasedate));// 业务日期

		assetObj.set("buildway", buildway); // 建卡方式 下拉列表

		// 经办人/人员
		DynamicObject handlerDy = map.get("bos_user" + "@_@" + "number" + "@_@" + handler);
		if (null == map.get("bos_user" + "@_@" + "number" + "@_@" + handler)) {
			stringBuffer.append("&没找到经办人!");
		}
		assetObj.set("handler", handlerDy);

		// 经办部门 行政组织
		DynamicObject handleOrgDy = map.get("bos_adminorg" + "@_@" + "number" + "@_@" + handleorg);
		if (null == handleOrgDy) {
			stringBuffer.append("&没找到经办部门!");
		}
		assetObj.set("handleorg", handleOrgDy);

		// --------------------单据体--资产信息--分录---------------

		List<Element> assetsentrys = elementPB.elements("assetsentry");

		DynamicObjectCollection assets_entry = assetObj.getDynamicObjectCollection("assetsentry");

		assets_entry.clear();// 无论新增还是更新都清空，因为创建数据的时候自动添加一行空的数据
		int count =0;//行号，便于提示
		for (Element asscode : assetsentrys) {
			
			++count;
			
			//StringBuffer stringBufferEntry = new StringBuffer();// 分录错误信息
			
			DynamicObject entry_bankRow = assets_entry.addNew();// 添加一行
			String assetcat = asscode.element("assetcat").getText();// 资产类别
			String unit = asscode.element("unit").getText();// 计量单位 必录
			String storeplace = asscode.element("storeplace").getText();// 存储地点 基础资料 必录
			String realaccountdate = asscode.element("realaccountdate").getText();// 启用日期 必录
			String supplier = asscode.element("supplier").getText();// 供应商 基础资料

			// 资产类别 基础资料
			DynamicObject assetcatDy = map.get("fa_assetcategory" + "@_@" + "number" + "@_@" + assetcat);
			if (null == assetcatDy) {
				stringBuffer.append("&第"+count+"行分录_没找到资产类别!");
			}
			entry_bankRow.set("assetcat", assetcatDy);

			// 资产名称
			entry_bankRow.set("assetname", asscode.element("assetname").getText());

			// 规格型号
			entry_bankRow.set("model", asscode.element("model").getText());

			// 计量单位 计量单位类型
			DynamicObject unitDy = map.get("bd_measureunits" + "@_@" + "name" + "@_@" + unit);

			if (null == unitDy) {
				stringBuffer.append("&第"+count+"行分录_没找到计量单位!");
			}
			entry_bankRow.set("unit", unitDy);

			entry_bankRow.set("assetqty", asscode.element("assetqty").getText());// 数量

			// 存放地点 基础资料
			DynamicObject storeplaceDy = map.get("fa_storeplace" + "@_@" + "number" + "@_@" + storeplace);
			if (null == storeplaceDy) {
				stringBuffer.append("&第"+count+"行分录_没找到存放地点!");
			}
			entry_bankRow.set("storeplace", storeplaceDy);// 存放地点

			// 启用日期
			entry_bankRow.set("realaccountdate", dateFormat.parse(realaccountdate));

			// 供应商 基础资料
			if (!supplier.isEmpty()) {
				DynamicObject supplierDy = map.get("bd_supplier" + "@_@" + "number" + "@_@" + supplier);
				if (null == supplierDy) {
					stringBuffer.append("&第"+count+"行分录_没找到供应商!");
				}
				entry_bankRow.set("supplier", supplierDy);
			}

			entry_bankRow.set("unitprice", asscode.element("unitprice").getText());// 单价

			entry_bankRow.set("notaxamount", asscode.element("notaxamount").getText());// 无税金额

			entry_bankRow.set("taxamount", asscode.element("taxamount").getText());// 税额

			entry_bankRow.set("totalamount", asscode.element("totalamount").getText());// 价税合计

		}

		return stringBuffer;
	}

	// 保存操作返回结果集
	private List<ResultEntity> saveResult(List<DynamicObject> assets, List<ResultEntity> resultEnties)
			throws Exception {

		OperationResult operationResult = OperationServiceHelper.executeOperate("save", "fa_purchasebill",
				assets.toArray(new DynamicObject[assets.size()]), OperateOption.create());
		// 错误
		List<IOperateInfo> OperateInfos = operationResult.getAllErrorOrValidateInfo();

		Map<Object, String> resultErrMap = new HashMap<>();
		for (IOperateInfo operateInfo : OperateInfos) {
			if (null == resultErrMap.get(operateInfo.getPkValue())) {
				resultErrMap.put(operateInfo.getPkValue(), operateInfo.getMessage());
			} else {
				resultErrMap.put(operateInfo.getPkValue(),
						resultErrMap.get(operateInfo.getPkValue()) + operateInfo.getMessage());
			}
		}
		resultErrMap.entrySet().forEach(res -> {
			resultEnties
					.add(ResultEntity.PROCESS_ERROR(res.getValue()).setInitDate(res.getKey().toString(), "", "主数据"));
		});

		// 成功
		List<Object> successPkIdsList = operationResult.getSuccessPkIds();
		Set<Object> successPkIds = new HashSet<Object>(successPkIdsList);
		for (Object object : successPkIds) {
			resultEnties.add(ResultEntity.SUCCESS().setInitDate(object.toString(), "", "主数据"));
		}
		return resultEnties;
	}

	// 校验空
	private StringBuffer doValidate(Element dataInfo) throws Exception {

		ResultEntity resultEntie = null;
		Element elementPB = dataInfo.element("purchasebill");
		String codeid = elementPB.element("codeid").getText();
		StringBuffer stringBuffer = new StringBuffer();
		// -------------------单据头 校验-----------------------

		if (elementPB.element("assetunit").getText().isEmpty()) {
			stringBuffer.append("&请填写资产组织编码!");
		}
		if (elementPB.element("checkOrg").getText().isEmpty()) {
			stringBuffer.append("&请填写核算组织编码!");
		}
		if (elementPB.element("purchasedate").getText().isEmpty()) {
			stringBuffer.append("&请填写业务日期!");
		}
		if (elementPB.element("buildway").getText().isEmpty()) {
			stringBuffer.append("&请填写建卡方式!");
		}
		if (elementPB.element("handler").getText().isEmpty()) {
			stringBuffer.append("&请填写经办人编码!");
		}
		if (elementPB.element("handleorg").getText().isEmpty()) {
			stringBuffer.append("&请填写经办部门编码!");
		}

		// --------------------单据体--资产信息--分录 校验---------------

		List<Element> assetsentrys = elementPB.elements("assetsentry");

		for (Element asscode : assetsentrys) {
			// 资产名称
			if (asscode.element("assetname").getText().isEmpty()) {
				stringBuffer.append("&请填写资产名称!");
			}
			// 计量单位
			if (asscode.element("unit").getText().isEmpty()) {
				stringBuffer.append("&请填写计算单位!");
			}
			// 存储地点
			if (asscode.element("storeplace").getText().isEmpty()) {
				stringBuffer.append("&请填写存储地点!");
			}
			// 启用日期
			if (asscode.element("realaccountdate").getText().isEmpty()) {
				stringBuffer.append("&请填写启用日期!");
			}
			// 单价
			if (asscode.element("unitprice").getText().isEmpty()) {
				stringBuffer.append("&请填写单价!");
			}
			// 数量
			if (asscode.element("assetqty").getText().isEmpty()) {
				stringBuffer.append("&请填写数量!");
			}
			// 价税合计
			if (asscode.element("totalamount").getText().isEmpty()) {
				stringBuffer.append("&请填写价税合计!");
			}

		}

		return stringBuffer;
	}

	// codeid校验
	private void validateCodeidMap(List<String> isAllcodeidList, List<ResultEntity> resultEnties,
			List<String> codeExist) {
		// 判断codeid是否存在
		DynamicObjectCollection dynamicObject3 = QueryServiceHelper.query("fa_purchasebill", "spic_sourceid",
				new QFilter[] { new QFilter("spic_sourceid", QCP.in, isAllcodeidList) });
		if (null != dynamicObject3) {
			for (DynamicObject kString : dynamicObject3) {
				resultEnties.add(ResultEntity.PROCESS_ERROR("暂估应付单号已存在").setInitDate(kString.getString("spic_sourceid"),
						"", ""));
				codeExist.add(kString.getString("spic_sourceid"));// 存在的编码
			}
		}
	}

}
