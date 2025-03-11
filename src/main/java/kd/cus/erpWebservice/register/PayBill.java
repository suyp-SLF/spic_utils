package kd.cus.erpWebservice.register;

import org.dom4j.Element;

import kd.cus.erpWebservice.action.WebserviceMainTemplate;
import kd.cus.erpWebservice.action.entity.ResultEntity;
import com.alibaba.fastjson.JSON;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ERP付款单集成接口 必录codeid bizdate业务日期 expectdate期望付款日期 paymenttype付款类型
 * payeetype收款人类型 可空 settletype结算方式 settletnumber结算号 必录paymentchannel支付渠道 可空
 * description摘要 必录org付款人 openorg核算组织 可空payerbanknum付款账号 payerbankname付款银行
 * 必录receivelabel收款人 payeebanknum收款账号 payeebank收款银行 currency付款币别 明细
 * e_expenseitem费用项目（可空） 必录 e_actamt实付金额 contractnumber合同/订单号
 * 
 * @author hdp
 * @author ZXR(修改)
 *
 */
public class PayBill implements WebserviceMainTemplate {

	@Override
	public List<ResultEntity> disposeDate(List<Element> children, String p_source) throws Exception {
		List<ResultEntity> resultEntities = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();
		List<Map<String, Object>> paramsValue = new ArrayList<>();

		// 1. 校验数据已经存在
		// 遍历所有的codeid放codeids Map中
		Map<String, DynamicObject> codeids = new HashMap<>();
		children.forEach((e) -> {
			if (StringUtils.isNotBlank(e.element("paybill").element("codeid").getText())) {
				codeids.put(e.element("paybill").element("codeid").getText(), null);
			}
		});
		// 校验codeid是否存在，将存在的数据放codeids中
		doValidateIfExist(codeids);

		for (Element dataInfo : children) {
			// 校验必填项
			ResultEntity resultEntity = doValidateMustIn(dataInfo.element("paybill"));
			if (resultEntity != null) {
				resultEntities.add(resultEntity);
				continue;
			}
			// 校验唯一codeid编码
			if (codeids.get(dataInfo.element("paybill").elementText("codeid")) == null) {
				// 上一步校验通过，则构造数据
				Map<String, Object> valueOne = constructValue(dataInfo.element("paybill"));
				valueOne.put("spic_p_source", p_source);
				paramsValue.add(valueOne);
			} else {
				resultEntities.add(ResultEntity.PROCESS_ERROR("数据已存在！")
						.setInitDate(dataInfo.element("paybill").elementText("codeid"), "", "司库付款单"));
			}
		}

		List<String> billnos = new ArrayList<>();// 不存在编码集合(符合插入条件的)
		paramsValue.stream().forEach((item) -> {
			billnos.add((String) item.get("spic_erp_codeid"));
		});

		// doBizValidate(paramsValue);
		params.put("datas", paramsValue);
		params.put("formid", "cas_paybill");
		System.out.println("paramsValue = " + JSON.toJSONString(paramsValue));
		if (paramsValue.size() > 0) {
			Map<String, Object> executeSaveResult = ServiceFactory.getService(OperationWebApi.class)
					.executeOperation("cas_paybill", "save", params);
			if (Boolean.valueOf(executeSaveResult.get("success").toString())) {
				for (Map<String, Object> data : paramsValue) {
					resultEntities
							.add(ResultEntity.SUCCESS().setInitDate((String) data.get("spic_erp_codeid"), "", "司库付款单"));
				}
			} else {
				
				// API执行失败的情况，对结果进行解析
				List<Map<String, Object>> resData = (List<Map<String, Object>>) executeSaveResult.get("data");
				if (resData != null && resData.size() > 0) {
					for (Map<String, Object> info : resData) {
						
						List<OperateErrorInfo> objects = (List<OperateErrorInfo>) info.get("data");
			
						if (Boolean.valueOf(info.get("success").toString())) {
							resultEntities.add(ResultEntity.SUCCESS().setInitDate(
									billnos.get(Integer.valueOf(info.get("dindex").toString())), "", "司库付款单"));
							continue;
						}
						Map<Integer, Object> convertResult = (Map<Integer, Object>) info.get("convertResult");

						if (convertResult != null && convertResult.size() > 0) {
							resultEntities.add(ResultEntity.PROCESS_ERROR(convertResult.get(0).toString()).setInitDate(
									billnos.get(Integer.valueOf(info.get("dindex").toString())), "", "司库付款单"));
						} else {
							resultEntities.add(ResultEntity.PROCESS_ERROR(objects.get(0).getMessage()).setInitDate(
									billnos.get(Integer.valueOf(info.get("dindex").toString())), "", "司库付款单"));
						}

					}
				}
			}
		}

		return resultEntities;
	}

	/**
	 * 功能:查询编码是否存在
	 * 
	 * @params codeids传来的所有唯一编码
	 */
	private List<ResultEntity> doValidateIfExist(Map<String, DynamicObject> codeids) throws Exception {
		// List<ResultEntity> resultEntities = new ArrayList<>();
		DynamicObject[] queryResult = BusinessDataServiceHelper.load("cas_paybill", "spic_erp_codeid",
				new QFilter[] { new QFilter("spic_erp_codeid", QCP.in, codeids.keySet()) });
		for (DynamicObject dy : queryResult) {
			codeids.put(dy.getString("spic_erp_codeid"), dy);// 查询到的数据放codeids中，<codeid编码,Dynameic对象>
		}
		return null;
	}

	private void doBizValidate(List<Map<String, Object>> paramsValue) {
		// 计量单位，传入的是名字，需要查询出编码
		/*
		 * Map<String, Map<String, Object>> maps = new HashMap<>(); // 字段名：<billno,
		 * value>
		 * 
		 * Map<String, Object> measureunitNameMaps = new HashMap<>(); for (Map<String,
		 * Object> data : paramsValue) { List<Map<String, Object>> detailentry =
		 * (List<Map<String, Object>>) data.get("detailentry"); for (Map<String, Object>
		 * row : detailentry) {
		 * measureunitNameMaps.put(data.get("spic_erp_codeid").toString(), ((Map<String,
		 * String>) row.get("measureunit")).get("name")); } } maps.put("measureunit",
		 * measureunitNameMaps);
		 * 
		 * DynamicObjectCollection queryMeasureunitResult =
		 * QueryServiceHelper.query("bd_measureunits", "number,name", new QFilter[] {
		 * new QFilter("name", QCP.in, new HashSet<>(maps.get("measureunit").values()))
		 * }); Map<String, String> nameIds = new HashMap<>(); // <name, id>
		 * queryMeasureunitResult.stream().forEach(dy -> {
		 * nameIds.put(dy.getString("name"), dy.getString("number")); }); for
		 * (Map<String, Object> data : paramsValue) { List<Map<String, Object>>
		 * detailentry = (List<Map<String, Object>>) data.get("detailentry"); for
		 * (Map<String, Object> row : detailentry) { String number =
		 * nameIds.get(((Map<String, String>) row.get("measureunit")).get("name"));
		 * Map<String, String> measureunit = new HashMap(); measureunit.put("number",
		 * number); row.put("measureunit", measureunit); } }
		 */
	}

	/**
	 * 校验必填项
	 * 
	 * @param dataInfo
	 *            paybill节点中的数据
	 * @return ResultEntity 错误信息/null
	 */
	private ResultEntity doValidateMustIn(Element dataInfo) throws Exception {
		// dataInfo = dataInfo.element("paybill");
		// Element paybillInfo = dataInfo.element("paybill");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		StringBuilder errorMessage = new StringBuilder();

		// codeid
		String codeid = dataInfo.elementText("codeid");
		if (StringUtils.isBlank(codeid)) {
			errorMessage.append("codeid是必填项;");
		}

		// 业务日期
		String bizdate = dataInfo.elementText("bizdate");
		if (StringUtils.isBlank(bizdate)) {
			errorMessage.append("业务日期是必填项;");
		} else {
			try {
				dateFormat.parse(bizdate);
			} catch (Exception e) {
				errorMessage.append("业务日期不符合日期格式yyyy-MM-dd;");
			}

		}

		// 期望付款日期
		String expectdate = dataInfo.elementText("expectdate");
		if (StringUtils.isBlank(expectdate)) {
			errorMessage.append("期望付款日期是必填项;");
		} else {
			try {
				dateFormat.parse(expectdate);
			} catch (Exception e) {
				errorMessage.append("期望付款日期不符合日期格式yyyy-MM-dd;");
			}

		}

		// 付款类型
		String paymenttype = dataInfo.elementText("paymenttype");
		if (StringUtils.isBlank(paymenttype)) {
			errorMessage.append("付款类型是必填项;");
		}

		// 收款人类型 默认值供应商bd_supplier
		String payeetype = dataInfo.elementText("payeetype");
		if (StringUtils.isBlank(payeetype)) {
			errorMessage.append("收款人类型是必填项;");
		}

		// settletype结算方式 settletnumber结算号 可空

		// paymentchannel支付渠道必录
		String paymentchannel = dataInfo.elementText("paymentchannel");
		if (StringUtils.isBlank(paymentchannel)) {
			errorMessage.append("支付渠道是必填项;");
		}

		// description摘要 可空

		// 付款人org
		String org = dataInfo.elementText("org");
		if (StringUtils.isBlank(org)) {
			errorMessage.append("付款人是必填项;");
		}

		// 付款人openorg
		String openorg = dataInfo.elementText("openorg");
		if (StringUtils.isBlank(openorg)) {
			errorMessage.append("核算组织是必填项;");
		}

		// 收款人
		String receivelabel = dataInfo.element("receivelabel").getText();
		if (StringUtils.isBlank(receivelabel)) {
			errorMessage.append("收款人是必填项;");
		}

		// 收款账号
		String payeebanknum = dataInfo.elementText("payeebanknum");
		if (StringUtils.isBlank(payeebanknum)) {
			errorMessage.append("收款账号是必填项;");
		}

		// 收款银行 ？？？？
		String payeebank = dataInfo.elementText("payeebank");
		if (StringUtils.isBlank(payeebank)) {
			errorMessage.append("收款银行是必填项;");
		}
		// 币别
		String currency = dataInfo.elementText("currency");
		if (StringUtils.isBlank(currency)) {
			errorMessage.append("币别是必填项;");
		}
		// String settlementtype = dataInfo.elementText("settlementtype");
		// if (StringUtils.isBlank(settlementtype)) {
		// errorMessage.append("结算方式是必填项;");
		// }
		// String department = dataInfo.elementText("department");
		// if (StringUtils.isBlank(department)) {
		// errorMessage.append("部门是必填项;");
		// }

		List<Element> datailentries = dataInfo.elements("advconap");
		// List<String> entryErrorMessages = new ArrayList<>();
		int rowIndex = 0;
		for (Element detailentry : datailentries) {
			rowIndex++;
			// StringBuilder errorBuild = new StringBuilder();
			// // 核算组织openorg 必录
			// String settleorg = detailentry.elementText("openorg");
			// if (StringUtils.isBlank(settleorg)) {
			// errorMessage.append("第["+rowIndex+"]行:"+"核算组织是必填项;");
			// }
			// 费用项目 可空
			// String e_expenseitem = detailentry.elementText("expenseitem");
			// if (StringUtils.isBlank(e_expenseitem)) {
			// errorBuild.append("费用项目是必填项");
			// }
			// 实付金额 必录
			String e_actamt = detailentry.elementText("e_actamt");
			if (StringUtils.isBlank(e_actamt)) {
				errorMessage.append("第[" + rowIndex + "]行:" + "实付金额是必填项");
			}
			// 合同号/订单号 必录
			String contractnumber = detailentry.elementText("contractnumber");
			if (StringUtils.isBlank(contractnumber)) {
				errorMessage.append("第[" + rowIndex + "]行:" + "合同/订单号是必填项");
			}
		}
		// entryErrorMessages.add(errorMessage.toString());
		if (null != errorMessage && errorMessage.toString().length() > 0) {
			// errorMessage.append(JSON.toJSONString(entryErrorMessages));
			return ResultEntity.PROCESS_ERROR(errorMessage.toString()).setInitDate(codeid, "", "ERP付款单");
		}
		return null;
	}

	/**
	 * 功能:xml节点转成map类型
	 * 
	 * @param dataInfo
	 *            paybill节点中的数据
	 */
	private Map<String, Object> constructValue(Element dataInfo) throws Exception {
		// codeid 必录
		Map<String, Object> retMap = new HashMap<>();
		String codeid = dataInfo.elementText("codeid");
		retMap.put("spic_erp_codeid", codeid);

		// String billtype = dataInfo.elementText("billtype");
		// Map<String, String> billtypeMap = new HashMap<>();
		// billtypeMap.put("number", billtype);

		// 单据状态 苍穹必录
		retMap.put("billstatus", "A");

		// 单据类型
		Map<String, Object> billtypeData = new HashMap<>();
		billtypeData.put("number", "cas_paybill_other_BT_S");
		retMap.put("billtype", billtypeData);

		// bizdate 业务日期 必录
		String bizdate = dataInfo.elementText("bizdate");
		retMap.put("bizdate", bizdate);

		// expectdate 期望付款日期 必录
		String expectdate = dataInfo.elementText("expectdate");
		retMap.put("expectdate", expectdate);

		// paymenttype 付款类型 必录
		String paymenttype = dataInfo.elementText("paymenttype");
		Map<String, String> paymenttypeMap = new HashMap<>();
		paymenttypeMap.put("number", paymenttype);
		retMap.put("paymenttype", paymenttypeMap);

		// payeetype 收款人类型 必录
		String payeetype = dataInfo.elementText("payeetype");
		retMap.put("payeetype", payeetype);

		// settletype 可空 结算方式 基础资料
		String settletype = dataInfo.elementText("settletype");
		Map<String, String> settletypeMap = new HashMap<>();
		settletypeMap.put("number", settletype);
		retMap.put("settletype", settletypeMap);

		// settletnumber 结算号 可空 文本
		String settletnumber = dataInfo.elementText("settletnumber");
		retMap.put("settletnumber", settletnumber);

		// paymentchannel 支付渠道 必录
		String paymentchannel = dataInfo.elementText("paymentchannel");
		retMap.put("paymentchannel", paymentchannel);

		// description摘要 可空
		String description = dataInfo.elementText("description");
		retMap.put("description", description);

		// org付款人 必录
		String org = dataInfo.elementText("org");
		Map<String, String> orgMap = new HashMap<>();
		orgMap.put("number", org);
		retMap.put("org", orgMap);

		// 核算组织 必录
		String openorg = dataInfo.elementText("openorg");
		Map<String, String> openorgMap = new HashMap<>();
		openorgMap.put("number", openorg);
		retMap.put("org", openorgMap);

		// payerbanknum 付款账号 必录
		String payeracctbank = dataInfo.elementText("payerbanknum");
		Map payeracctbankData = new HashMap();
		payeracctbankData.put("number", payeracctbank);
		retMap.put("payeracctbank", payeracctbankData);

		// 根据payerbanknum 取 payerbank 付款银行 ，必录 payerbank
		DynamicObjectCollection payerbankInfo = QueryServiceHelper.query("bd_accountbanks", "id,name,number,bank",
				new QFilter[] { new QFilter("number", QCP.equals, payeracctbank) });
		DynamicObjectCollection bankInfo = QueryServiceHelper.query("bd_finorginfo", "id,name,number",
				new QFilter[] { new QFilter("id", QCP.equals, payerbankInfo.get(0).get("bank")) });

		Map payerbanknameData = new HashMap();
		payerbanknameData.put("number", bankInfo.get(0).get("number"));
		retMap.put("payerbank", payerbanknameData);

		// 根据收款人 receivelabel 查询 收款人id和 收款人姓名payeename, 收款人 必录 传编码查询收款id
		DynamicObjectCollection payeeInfo = new DynamicObjectCollection();
		if (dataInfo.elementText("receivelabel") != null) {
			String payeeStr = dataInfo.elementText("receivelabel");
			payeeInfo = QueryServiceHelper.query("bd_supplier", "id,number,name",
					new QFilter[] { new QFilter("number", QCP.equals, payeeStr) });// 供应商
			if (payeeInfo != null && "bd_supplier".equals(dataInfo.elementText("payeetype"))) {
				retMap.put("payee", payeeInfo.get(0).get("id"));
				retMap.put("payeename", payeeInfo.get(0).get("name"));
			} else {
				payeeInfo = QueryServiceHelper.query("bos_org", "id,number,name",
						new QFilter[] { new QFilter("number", QCP.equals, payeeStr) });// 公司
				if (payeeInfo != null && "bos_org".equals(dataInfo.elementText("payeetype"))) {
					retMap.put("payee", payeeInfo.get(0).get("id"));
					retMap.put("payeename", payeeInfo.get(0).get("name"));
				} else {
					payeeInfo = QueryServiceHelper.query("bd_customer", "id,number,name",
							new QFilter[] { new QFilter("number", QCP.equals, payeeStr) });// 客户
					if (payeeInfo != null && "bd_customer".equals(dataInfo.elementText("payeetype"))) {
						retMap.put("payee", payeeInfo.get(0).get("id"));
						retMap.put("payeename", payeeInfo.get(0).get("name"));
					} else {
						payeeInfo = QueryServiceHelper.query("bos_user", "id,number,name",
								new QFilter[] { new QFilter("number", QCP.equals, payeeStr) });// 职员
						if (payeeInfo != null && "bos_user".equals(dataInfo.elementText("payeetype"))) {
							retMap.put("payee", payeeInfo.get(0).get("id"));
							retMap.put("payeename", payeeInfo.get(0).get("name"));
						} else {
							// 其他
						}
					}
				}
			}
		} else {

		}
		// payeebanknum 收款账号 必录
		String payeebanknum = dataInfo.elementText("payeebanknum");
		retMap.put("payeebanknum", payeebanknum);

		// payeebank 收款银行 必录 payeebankname
		String payeebankname = dataInfo.elementText("payeebank");
		DynamicObjectCollection payeebankInfo = QueryServiceHelper.query("bd_accountbanks", "id,name,number,bank",
				new QFilter[] { new QFilter("number", QCP.equals, payeebanknum) });
		DynamicObjectCollection payeebank = QueryServiceHelper.query("bd_finorginfo", "id,name,number",
				new QFilter[] { new QFilter("id", QCP.equals, payeebankInfo.get(0).get("bank")) });
		// Map payeeData = new HashMap();
		// payeeData.put("number",payeebank.get(0).get("number"));
		retMap.put("payeebankname", payeebank.get(0).get("name"));

		// currency， 必录
		String currency = dataInfo.elementText("currency");
		Map<String, String> currencyMap = new HashMap<>();
		currencyMap.put("number", currency);
		retMap.put("currency", currencyMap);

		// 付款明细 advconap
		List<Element> advconaps = dataInfo.elements("advconap");
		List<Map<String, Object>> detailentryList = new ArrayList<>();
		for (Element detailentry : advconaps) {
			Map<String, Object> row = new HashMap<>();
			// e_expenseitem 费用项目 可空
			if (detailentry.elementText("e_expenseitem") != null) {
				String e_expenseitem = detailentry.elementText("e_expenseitem");
				Map<String, String> e_expenseitemMap = new HashMap<>();
				e_expenseitemMap.put("number", e_expenseitem);
				row.put("e_expenseitem", e_expenseitemMap);
			}
			// openorg 核算组织 必录
			// String settleorg = detailentry.elementText("openorg");
			// Map<String, String> settleorgMap = new HashMap<>();
			// settleorgMap.put("number", settleorg);
			// row.put("settleorg", settleorgMap);

			// contractnumber 合同/订单号 必录
			String contractnumber = detailentry.elementText("contractnumber");
			row.put("contractnumber", contractnumber);

			// e_actamt 实付金额 必录
			String e_actamt = detailentry.elementText("e_actamt");
			row.put("e_actamt", e_actamt);

			row.put("e_payableamt", e_actamt);
			// Object actpayamt = detailentry.elementText("e_actamt");
			retMap.put("actpayamt", e_actamt);

			detailentryList.add(row);
		}
		retMap.put("entry", detailentryList);
		return retMap;
	}
}
