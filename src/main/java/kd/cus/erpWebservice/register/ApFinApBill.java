package kd.cus.erpWebservice.register;

import com.alibaba.fastjson.JSON;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.operate.result.OperateErrorInfo;
import kd.bos.mservice.webapi.OperationWebApi;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.service.ServiceFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.cus.erpWebservice.action.WebserviceMainTemplate;
import kd.cus.erpWebservice.action.entity.ResultEntity;
import kd.fi.cas.helper.OperateServiceHelper;
import kd.fi.er.business.servicehelper.BaseCurrencyServiceHelper;

import org.dom4j.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ERP应付单集成程序（数据接收），不存在更新
 *
 * @author Wu Yanqi
 */
public class ApFinApBill implements WebserviceMainTemplate {

	@Override
	public List<ResultEntity> disposeDate(List<Element> children, String p_source) throws Exception {
		
		RequestContext.get().setClientUrl("tempURL");//为了避开插件空指针异常写的临时值

		List<ResultEntity> resultEntities = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();
		List<Map<String, Object>> paramsValue = new ArrayList<>();

		// 1. 校验数据已经存在
		Map<String, DynamicObject> codeids = new HashMap<>();
		children.forEach((e) -> {
			if (StringUtils.isNotBlank(e.elementText("codeid"))) {
				codeids.put(e.elementText("codeid"), null);
			}
		});
		doValidateIfExist(codeids);

		for (Element dataInfo : children) {
			// 校验必填项
			ResultEntity resultEntity = doValidateMustIn(dataInfo);
			if (resultEntity != null) {
				resultEntities.add(resultEntity);
				continue;
			}
			if (codeids.get(dataInfo.elementText("codeid")) == null) {
				// 上一步校验通过，则构造数据
				Map<String, Object> valueOne = constructValue(dataInfo);
				valueOne.put("spic_p_source", p_source);
				paramsValue.add(valueOne);
			} else {
				resultEntities.add(
						ResultEntity.PROCESS_ERROR("数据已存在！").setInitDate(dataInfo.elementText("codeid"), "", "财务应付单"));
			}
		}
		// // 组合币别，因为币别传入的是name
		// List<Object> currencyNameList = paramsValue.stream().map((item) ->
		// item.get("currency")).collect(Collectors.toList());
		// DynamicObjectCollection queryCurrencyResult =
		// QueryServiceHelper.query("bd_currency", "number,name", new QFilter[]{new
		// QFilter("name", QCP.in, currencyNameList)});
		// Map<String, String> nameNumber = new HashMap<>();
		// queryCurrencyResult.forEach(dynamicObject -> {
		// nameNumber.put(dynamicObject.getString("name"),
		// dynamicObject.getString("number"));
		// });
		List<String> billnos = new ArrayList<>();
		paramsValue.stream().forEach((item) -> {
			billnos.add((String) item.get("spic_erp_codeid"));
		});
		// paramsValue.stream().forEach((item)->{
		// Map<String, String> map = new HashMap<>();
		// map.put("number", nameNumber.get(item.get("currency")));
		// item.remove("currency");
		// item.put("currency", map);
		// billnos.add((String) item.get("billno"));
		// });
		doBizValidate(paramsValue, resultEntities);
		params.put("datas", paramsValue);
		params.put("formid", "ap_finapbill");
		// 合同号

		System.out.println("paramsValue = " + JSON.toJSONString(paramsValue));
		if (paramsValue.size() > 0) {
			Map<String, Object> executeSaveResult = ServiceFactory.getService(OperationWebApi.class)
					.executeOperation("ap_finapbill", "save", params);
			System.out.print(executeSaveResult);
			if (Boolean.valueOf(executeSaveResult.get("success").toString())) {
				for (Map<String, Object> data : paramsValue) {
					resultEntities
							.add(ResultEntity.SUCCESS().setInitDate((String) data.get("spic_erp_codeid"), "", "财务应付单"));
				}
			} else {
				// API执行失败的情况，对结果进行解析
				List<Map<String, Object>> resData = (List<Map<String, Object>>) executeSaveResult.get("data");
				if (resData != null && resData.size() > 0) {
					for (Map<String, Object> info : resData) {
						if (Boolean.valueOf(info.get("success").toString())) {
							resultEntities.add(ResultEntity.SUCCESS().setInitDate(
									billnos.get(Integer.valueOf(info.get("dindex").toString())), "", "财务应付单"));
							continue;
						}
						Map<Integer, Object> convertResult = (Map<Integer, Object>) info.get("convertResult");
						if (convertResult != null && convertResult.size() > 0) {
							resultEntities.add(ResultEntity
									.PROCESS_ERROR(convertResult.get(0).toString().replace("\"", "")).setInitDate(
											billnos.get(Integer.valueOf(info.get("dindex").toString())), "", "财务应付单"));
						} else {
							List<OperateErrorInfo> data = (List<OperateErrorInfo>) info.get("data");
							resultEntities.add(
									ResultEntity.PROCESS_ERROR(data.get(0).getMessage().replace("\"", "")).setInitDate(
											billnos.get(Integer.valueOf(info.get("dindex").toString())), "", "财务应付单"));
						}
					}
				}
			}
		}

		return resultEntities;
	}

	private List<ResultEntity> doValidateIfExist(Map<String, DynamicObject> codeids) {
		List<ResultEntity> resultEntities = new ArrayList<>();
		DynamicObject[] queryResult = BusinessDataServiceHelper.load("ap_finapbill", "spic_erp_codeid",
				new QFilter[] { new QFilter("spic_erp_codeid", QCP.in, codeids.keySet()) });
		for (DynamicObject dy : queryResult) {
			codeids.put(dy.getString("spic_erp_codeid"), dy);
		}
		return null;
	}

	/**
	 * 功能：获取基础资料放到list中,返回结果放到resultEntities
	 */
	private void doBizValidate(List<Map<String, Object>> paramsValue, List<ResultEntity> resultEntities) {
		if (paramsValue.size() <= 0) {
			return;
		}
		// 计量单位，传入的是名字，需要查询出编码
		Map<String, Map<String, Object>> maps = new HashMap<>(); // 字段名：<billno, value>
		// 收款银行，传递的是名字，空，替换为使用收款账号查出
		Map<String, Object> payeebanknumMaps = new HashMap<>();
		Map<String, Object> measureunitNameMaps = new HashMap<>();
		// // socicode
		// Map<String, Object> socicodeMaps = new HashMap<>();
		// // asstactname
		// Map<String, Object> asstactnameMaps = new HashMap<>();
		// 合同号
		Map<String, Object> purcontractMaps = new HashMap<>();

		for (Map<String, Object> data : paramsValue) {
			// 收款账号 payeebanknum
			payeebanknumMaps.put(data.get("spic_erp_codeid").toString(), data.get("payeebanknum"));
			// 分录中基础资料
			List<Map<String, Object>> detailentry = (List<Map<String, Object>>) data.get("detailentry");
			for (Map<String, Object> row : detailentry) {
				// 计量单位
				measureunitNameMaps.put(data.get("spic_erp_codeid").toString(),
						((Map<String, String>) row.get("measureunit")).get("name"));
			}
			// socicodeMaps.put(data.get("socicode").toString(),
			// data.get("spic_erp_codeid").toString());
			// asstactnameMaps.put(data.get("spic_erp_codeid").toString(),
			// data.get("asstactname").toString());
			// 合同号基础资料
			if (data.get("spic_purcontract") != null) {
				purcontractMaps.put(data.get("spic_erp_codeid").toString(), data.get("spic_purcontract"));
			}
		}
		maps.put("measureunit", measureunitNameMaps);
		maps.put("payeebanknum", payeebanknumMaps);

		maps.put("spic_purcontract", purcontractMaps);

		// -----------------------------------------获取供应商--------------
		Iterator<Map<String, Object>> iterator = paramsValue.iterator();
		while (iterator.hasNext()) {
			Map<String, Object> data = iterator.next();
			Object socicode = data.get("socicode");
			if (socicode != null && !"".equals(socicode)) {// 传过来供应商统一信用编码
				// 通过统一信用编码获取供应商对象
				DynamicObject item = QueryServiceHelper.queryOne("bd_supplier", "number",
						new QFilter[] { new QFilter("societycreditcode", QCP.equals, socicode.toString()) });
				HashMap<String, String> asstactNumber = new HashMap<>();
				if (item == null) {// 如果供应商没找到统一信用编码，根据传过来的供应商名字查询供应商对象
					if (data.get("asstactname") != null) {
						DynamicObject query = QueryServiceHelper.queryOne("bd_supplier", "societycreditcode,number",
								new QFilter[] { new QFilter("name", QCP.equals, data.get("asstactname").toString()) });
						if (query != null) {
							asstactNumber.put("number", query.getString("number"));
						}
					}
					if (asstactNumber.get("number") == null) {
						resultEntities.add(ResultEntity.PROCESS_ERROR("往来户在主数据系统中不存在")
								.setInitDate(data.get("spic_erp_codeid").toString(), "", "财务应付单"));
						iterator.remove();
						continue;
					}
				} else {
					asstactNumber.put("number", item.getString("number"));
					if (asstactNumber.get("number") == null) {
						resultEntities.add(ResultEntity.PROCESS_ERROR("往来户在主数据系统中不存在")
								.setInitDate(data.get("spic_erp_codeid").toString(), "", "财务应付单"));
						iterator.remove();
						continue;
					}

				}
				data.put("asstact", asstactNumber);
			} else {// 供应商没有统一信用编码，根据供应商名字查
				HashMap<String, String> asstactNumber = new HashMap<>();
				if (data.get("asstactname") != null) {
					DynamicObject query = QueryServiceHelper.queryOne("bd_supplier", "societycreditcode,number",
							new QFilter[] { new QFilter("name", QCP.equals, data.get("asstactname").toString()) });
					if (query != null) {
						asstactNumber.put("number", query.getString("number"));
					} else {
						resultEntities.add(ResultEntity.PROCESS_ERROR("往来户在主数据系统中不存在")
								.setInitDate(data.get("spic_erp_codeid").toString(), "", "财务应付单"));
						iterator.remove();
						continue;
					}

				}
				data.put("asstact", asstactNumber);
			}
		}
		// -----------------------------------------获取供应商--------------
		if (paramsValue.size() <= 0) {
			return;
		}
		// 供应商编码
		HashMap<String, Object> asstactNumMaps = new HashMap<>();
		paramsValue.forEach(map -> {
			asstactNumMaps.put(map.get("spic_erp_codeid").toString(),
					((Map<String, String>) (map.get("asstact"))).get("number"));
		});
		maps.put("asstact", asstactNumMaps);

		// 计量单位
		DynamicObjectCollection queryMeasureunitResult = QueryServiceHelper.query("bd_measureunits", "number,name",
				new QFilter[] { new QFilter("name", QCP.in, new HashSet<>(maps.get("measureunit").values())) });
		Map<String, String> nameIds = new HashMap<>(); // <name, id>
		queryMeasureunitResult.stream().forEach(dy -> {
			nameIds.put(dy.getString("name"), dy.getString("number"));
		});
		for (Map<String, Object> data : paramsValue) {
			List<Map<String, Object>> detailentry = (List<Map<String, Object>>) data.get("detailentry");
			for (Map<String, Object> row : detailentry) {
				String number = nameIds.get(((Map<String, String>) row.get("measureunit")).get("name"));
				Map<String, String> measureunit = new HashMap();
				measureunit.put("number", number);
				row.put("measureunit", measureunit);
			}
		}
		// 根据往来户编码获取对象
		DynamicObject[] entryBanks = BusinessDataServiceHelper.load("bd_supplier",
				"number,entry_bank,entry_bank.bankaccount,entry_bank.bank,entry_bank.bank.number",
				new QFilter[] { new QFilter("number", QCP.in, new HashSet<>(maps.get("asstact").values())) });

		Map<String, DynamicObjectCollection> asstactBanks = new HashMap<>(); // <name, id>

		Arrays.stream(entryBanks).forEach(item -> {
			asstactBanks.put(item.getString("number"), item.getDynamicObjectCollection("entry_bank"));
		});

		Iterator<Map<String, Object>> bankiterator = paramsValue.iterator();
		while (bankiterator.hasNext()) {
			Map<String, Object> data = bankiterator.next();
			String assactNum = ((Map<String, String>) data.get("asstact")).get("number");
			DynamicObjectCollection entry_banks = asstactBanks.get(assactNum);// 根据供应商编码获取供应商对象
			Map<String, List<DynamicObject>> payeeBankNumBanks = entry_banks.stream()
					.collect(Collectors.groupingBy(dy -> dy.getString("bankaccount")));
			// 收款银行
			String payeebanknum = (String) data.get("payeebanknum");
			if (!StringUtils.isBlank(payeebanknum)) {// 银行账号可能不传过来
				Map<String, String> bebank = new HashMap<>();
				if (payeeBankNumBanks == null || payeeBankNumBanks.get(payeebanknum) == null
						|| payeeBankNumBanks.get(payeebanknum).size() <= 0) {
					resultEntities.add(ResultEntity.PROCESS_ERROR("请维护供应商的银行信息")
							.setInitDate(data.get("spic_erp_codeid").toString(), "", "财务应付单"));
					bankiterator.remove();
					continue;
				}
				bebank.put("number",
						payeeBankNumBanks.get(payeebanknum).get(0).getDynamicObject("bank").getString("number"));
				data.put("bebank", bebank);
			}

		}

		// 合同编号
		DynamicObjectCollection spic_purcontractObjs = QueryServiceHelper.query("conm_purcontract", "id, billno",
				new QFilter[] { new QFilter("billno", QCP.in, new HashSet<>(maps.get("spic_purcontract").values())) });
		Map<String, String> purcontractBillNoIds = new HashMap<>();
		spic_purcontractObjs.forEach(dy -> {
			purcontractBillNoIds.put(dy.getString("billno"), dy.getString("id"));
		});
		paramsValue.forEach(item -> {
			Map<String, String> purcontract = new HashMap<>();
			purcontract.put("id", purcontractBillNoIds.get(item.get("spic_purcontract")));
			item.put("spic_purcontract", purcontract);
		});

	}

	/**
	 * 校验必填项
	 *
	 * @param dataInfo
	 * @return
	 */
	private ResultEntity doValidateMustIn(Element dataInfo) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		StringBuilder errorMessage = new StringBuilder();
		String codeid = dataInfo.elementText("codeid");

		if (StringUtils.isBlank(dataInfo.elementText("socicode"))
				&& StringUtils.isBlank(dataInfo.elementText("asstactname"))) {
			errorMessage.append("往来户的统一社会信用代码或者名称必须填写一个");
		}

		if (StringUtils.isBlank(codeid)) {
			errorMessage.append("codeid是必填项;");
		}
		String org = dataInfo.elementText("org");
		if (StringUtils.isBlank(org)) {
			errorMessage.append("结算组织是必填项;");
		}
		String billtype = dataInfo.elementText("billtype");
		if (StringUtils.isBlank(billtype)) {
			errorMessage.append("单据类型是必填项;");
		}
		String bizdate = dataInfo.elementText("bizdate");
		if (StringUtils.isBlank(bizdate)) {
			errorMessage.append("业务日期是必填项;");
		} else {
			try {
				dateFormat.parse(bizdate);
			} catch (ParseException e) {
				errorMessage.append("业务日期不符合日期格式yyyy-MM-dd;");
			}
		}
		// String asstact = dataInfo.elementText("asstact");
		// if (StringUtils.isBlank(asstact)) {
		// errorMessage.append("往来户是必填项;");
		// }
		String payorg = dataInfo.elementText("payorg");
		if (StringUtils.isBlank(payorg)) {
			errorMessage.append("付款组织是必填项;");
		}
		String purmode = dataInfo.elementText("purmode");
		if (StringUtils.isBlank(purmode)) {
			errorMessage.append("付款方式是必填项;");
		}
		String payeebanknum = dataInfo.elementText("payeebanknum");
		if (StringUtils.isBlank(payeebanknum)) {
			errorMessage.append("收款账号是必填项;");
		}
		// String bebank = dataInfo.elementText("bebank");
		// if (StringUtils.isBlank(bebank)) {
		// errorMessage.append("收款银行是必填项;");
		// }
		String settlementtype = dataInfo.elementText("settlementtype");
		if (StringUtils.isBlank(settlementtype)) {
			errorMessage.append("结算方式是必填项;");
		}
		String department = dataInfo.elementText("department");
		if (StringUtils.isBlank(department)) {
			errorMessage.append("部门是必填项;");
		}
		String currency = dataInfo.elementText("currency");
		if (StringUtils.isBlank(currency)) {
			errorMessage.append("币别是必填项;");
		}
		String creator = dataInfo.elementText("creator");
		if (StringUtils.isBlank(creator)) {
			errorMessage.append("创建人是必填项;");
		}
		List<Element> datailentries = dataInfo.elements("detailentry");
		List<String> entryErrorMessages = new ArrayList<>();
		int rowIndex = 0;
		for (Element detailentry : datailentries) {
			rowIndex++;
			StringBuilder errorBuild = new StringBuilder();
			// String expenseitem = detailentry.elementText("expenseitem");
			// if (StringUtils.isBlank(expenseitem)) {
			// errorBuild.append("费用项目是必填项");
			// }
			// String measureunit = detailentry.elementText("measureunit");
			// if (StringUtils.isBlank(measureunit)) {
			// errorBuild.append("计量单位是必填项");
			// }
			String quantity = detailentry.elementText("quantity");
			if (StringUtils.isBlank(quantity)) {
				errorBuild.append("数量是必填项");
			}
			String ispresent = detailentry.elementText("ispresent");
			if (StringUtils.isBlank(ispresent)) {
				errorBuild.append("赠品是必填项");
			}
			String price = detailentry.elementText("price");
			if (StringUtils.isBlank(price)) {
				errorBuild.append("单价是必填项");
			}
			String isincludetax = detailentry.elementText("isincludetax");
			if (StringUtils.isBlank(isincludetax)) {
				errorBuild.append("含税单价是必填项");
			}
			// String pricetaxtotal = detailentry.elementText("pricetaxtotal");
			// if (StringUtils.isBlank(pricetaxtotal)) {
			// errorBuild.append("应付金额是必填项");
			// }
			if (StringUtils.isNotBlank(errorBuild.toString())) {
				entryErrorMessages.add("行号：" + rowIndex + errorBuild.toString());
			}
		}

		DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("bos_org", "id",
				new QFilter[] { new QFilter("number", QCP.in, dataInfo.elementText("org")) });
		if (dynamicObject != null) {
			long pKLong = BaseCurrencyServiceHelper.getBaseCurrencyId(dynamicObject.getLong("id"));
			if (StringUtils.isBlank(pKLong)) {
				errorMessage.append("本位币没找到,可能组织没初始化！");
			}
		} else {
			errorMessage.append("当前组织不存在");
		}

		if (entryErrorMessages.size() > 0) {
			errorMessage.append(JSON.toJSONString(entryErrorMessages).replace("\"", ""));
			return ResultEntity.PROCESS_ERROR(errorMessage.toString().replace("\"", "")).setInitDate(codeid, "",
					"财务应付单");
		}
		if (StringUtils.isNotBlank(errorMessage.toString().replace("\"", ""))) {
			return ResultEntity.PROCESS_ERROR(errorMessage.toString().replace("\"", "")).setInitDate(codeid, "",
					"财务应付单");
		}
		return null;
	}

	/**
	 * 重组，构造参数
	 *
	 * @param dataInfo
	 * @return
	 */
	private Map<String, Object> constructValue(Element dataInfo) {

		Map<String, Object> retMap = new HashMap<>();
		String codeid = dataInfo.elementText("codeid");
		retMap.put("spic_erp_codeid", codeid);
		// asstactname
		String asstactname = dataInfo.elementText("asstactname");
		retMap.put("asstactname", asstactname);
		// socicode
		String socicode = dataInfo.elementText("socicode");
		retMap.put("socicode", socicode);

		String org = dataInfo.elementText("org");
		Map<String, String> orgMap = new HashMap<>();
		orgMap.put("number", org);
		retMap.put("org", orgMap);

		String billtype = dataInfo.elementText("billtype");
		Map<String, String> billtypeMap = new HashMap<>();
		billtypeMap.put("number", billtype);
		retMap.put("billtypeid", billtypeMap);

		// bizdate
		String bizdate = dataInfo.elementText("bizdate");
		retMap.put("bizdate", bizdate);

		// asstact
		// String asstact = dataInfo.elementText("asstact");
		// Map<String, String> asstactMap = new HashMap<>();
		// asstactMap.put("number", asstact);
		// retMap.put("asstact", asstactMap);

		// payorg
		String payorg = dataInfo.elementText("payorg");
		Map<String, String> payorgMap = new HashMap<>();
		payorgMap.put("number", payorg);
		retMap.put("payorg", payorgMap);

		// purmode
		String purmode = dataInfo.elementText("purmode");
		retMap.put("purmode", purmode);

		// payeebanknum
		String payeebanknum = dataInfo.elementText("payeebanknum");
		retMap.put("payeebanknum", payeebanknum);

		// bebank
		String bebank = dataInfo.elementText("bebank");
		// Map<String, String> bebankMap = new HashMap<>();
		// bebankMap.put("number", bebank);
		retMap.put("bebank", bebank);

		// settlementtype
		String settlementtype = dataInfo.elementText("settlementtype");
		Map<String, String> settlementtypeMap = new HashMap<>();
		settlementtypeMap.put("number", settlementtype);
		retMap.put("settlementtype", settlementtypeMap);

		// department
		String department = dataInfo.elementText("department");
		Map<String, String> departmentMap = new HashMap<>();
		departmentMap.put("number", department);
		retMap.put("spic_department", departmentMap);

		// premiumamt
		String premiumamt = dataInfo.elementText("premiumamt");
		retMap.put("premiumamt", premiumamt);

		// 质保金比例(%)
		// String premiumrate = dataInfo.elementText("premiumrate");
		// retMap.put("premiumrate", premiumrate);

		// currency，传的是名称
		String currency = dataInfo.elementText("currency");
		Map<String, String> currencyMap = new HashMap<>();
		currencyMap.put("number", currency);
		retMap.put("currency", currencyMap);

		// 本位币
		// Map<String, String> basecurrency = new HashMap<>();
		// basecurrency.put("number", "CNY");
		// retMap.put("basecurrency", basecurrency);
		// 往来类型
		retMap.put("asstacttype", "bd_supplier");
		// 汇率
		// retMap.put("exchangerate", 1);
		// 汇率表 ERT-01
		// Map<String, String> exratetable = new HashMap<>();
		// exratetable.put("number", "ERT-01");
		// retMap.put("exratetable", exratetable);

		// 合同号
		String spic_purcontract = dataInfo.elementText("spic_purcontract");
		retMap.put("spic_purcontract", spic_purcontract);

		// detailentry
		List<Element> datailentries = dataInfo.elements("detailentry");
		List<Map<String, Object>> detailentryList = new ArrayList<>();

		// 本位币
		DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("bos_org", "id",
				new QFilter[] { new QFilter("number", QCP.in, dataInfo.elementText("org")) });
		long pKLong = BaseCurrencyServiceHelper.getBaseCurrencyId(dynamicObject.getLong("id"));
		Map<String, Object> basecurrencyMaps = new HashMap<>();
		basecurrencyMaps.put("id", pKLong);
		retMap.put("basecurrency", basecurrencyMaps);

		// 人员创建人/审核人
		String creatorUser = dataInfo.elementText("creator");// 获得创建人编码
		Map<String, String> creatorUserMap = new HashMap<>();
		creatorUserMap.put("number", creatorUser);
		retMap.put("creator", creatorUserMap);// 创建人
		retMap.put("auditor", creatorUserMap);// 审核人
		retMap.put("modifier", creatorUserMap);// 修改人
		
		for (Element detailentry : datailentries) {
			Map<String, Object> row = new HashMap<>();
			// expenseitem
			String expenseitem = detailentry.elementText("expenseitem");
			Map<String, String> expenseitemMap = new HashMap<>();
			expenseitemMap.put("number", expenseitem);
			row.put("expenseitem", expenseitemMap);
			// measureunit 计量单位
			String measureunit = detailentry.elementText("measureunit");
			Map<String, String> measureunitMap = new HashMap<>();
			measureunitMap.put("name", measureunit);
			row.put("measureunit", measureunitMap);
			// quantity
			String quantity = detailentry.elementText("quantity");
			row.put("quantity", quantity);
			// ispresent
			String ispresent = detailentry.elementText("ispresent");
			row.put("ispresent", ispresent);
			// price
			String price = detailentry.elementText("price");
			row.put("price", price);
			// isincludetax
			String isincludetax = detailentry.elementText("isincludetax");
//			row.put("isincludetax", isincludetax);
			row.put("pricetax", isincludetax);
			// pricetaxtotal 分录应付金额
			String pricetaxtotal = detailentry.elementText("pricetaxtotal");
			row.put("e_pricetaxtotal", pricetaxtotal);
			// 删除掉“核心单据类型”，“核心单据号”
			// corebilltype
			// String corebilltype = detailentry.elementText("corebilltype");
			// row.put("corebilltype", corebilltype);
			// corebillno
			// String corebillno = detailentry.elementText("corebillno");
			// row.put("corebillno", corebillno);

			detailentryList.add(row);
		}
		retMap.put("detailentry", detailentryList);
		return retMap;
	}
}
