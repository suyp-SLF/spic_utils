package kd.cus.erpWebservice.register;

import com.alibaba.fastjson.JSONObject;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.ksql.util.StringUtil;
import kd.bos.login.utils.DateUtils;
import kd.bos.util.HttpClientUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.dom4j.Element;
import com.alibaba.fastjson.JSONObject;

import json.JSON;
import kd.fi.arapcommon.dev.BeanFactory;
import kd.fi.arapcommon.dev.beanfactory.BeanRepository;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.api.ApiResult;
import kd.cus.api.LogUtils;
import kd.cus.api.SpicCusConfig;
import kd.cus.erpWebservice.action.WebserviceMainTemplate;
import kd.cus.erpWebservice.action.entity.ResultEntity;
import kd.fi.ar.business.invoice.InvoiceFacade;
import kd.fi.ar.business.invoice.IssueInvoiceApiPlugin;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.mservice.form.OperationWebApiImpl;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.service.ServiceFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.cus.erpWebservice.action.WebserviceMainTemplate;
import kd.cus.erpWebservice.action.entity.ResultEntity;
import org.dom4j.Element;
import kd.bos.util.HttpClientUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

/**
 * 已开票信息接收接口(接收远光税务)
 *
 * @author hdp
 *
 */
public class InvoicedInfoRec implements WebserviceMainTemplate {

	private static int default_connectionTimeout = 10000;
	private static int default_readTimeout = 10000;

	@Override
	public List<ResultEntity> disposeDate(List<Element> children, String p_source) throws Exception {
		List<ResultEntity> resultEntities = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();
		List<Map<String, Object>> valueOne = new ArrayList<>();
		List<Map<String, Object>> paramsValue = new ArrayList<>();
		for (Element dataInfo : children) {
			// 校验必填项
			ResultEntity resultEntity = doValidateMustIn(dataInfo);
			if (resultEntity != null) {
				resultEntities.add(resultEntity);
				continue;
			}

			valueOne = constructValue(dataInfo);

		}

		List<String> billnos = new ArrayList<>();
		paramsValue.stream().forEach((item) -> {
			billnos.add((String) item.get("FP_DM"));
		});

		params.put("datas", valueOne);
		// params.put("formid", "ar_invoice");
		System.out.println("paramsValue = " + JSON.toJSONString(paramsValue));

		// InvoiceFacade invoiceFacade = (InvoiceFacade)
		// BeanFactory.getBean(InvoiceFacade.class, new Object[0]);
		// invoiceFacade.issueCallBack(paramsValue);

		String tenantId = "spic";
		String accountId = RequestContext.get().getAccountId();// 998449887089525760dev 967074656869679104 test0901
																// 872529855609044992 967074656869679104
		String app_token = "";
		String access_token = "";
		// 获取app_token
		JSONObject paramApp = new JSONObject();// 系统地址+ "/api/getAppToken.do";
		// String appTokenUrl = "http://localhost:8080/ierp/api/getAppToken.do";
		// 如果MC中没取到值抛出异常
		String strURL = SpicCusConfig.getSpicCusConfig().getTestUrl();// 访问地址
		String appTokenUrl = "http://"+strURL+":8000/ierp/api/getAppToken.do";
		paramApp.put("appId", "portal"); // 向管理员索取
		paramApp.put("appSecuret", "1234567"); // 向管理员索取
		paramApp.put("tenantid", tenantId); // 向管理员索取（租户ID）
		paramApp.put("accountId", accountId); // 向管理员索取（数据中心ID）
		paramApp.put("language", "zh_CN");
		String rsAppToken = null; // post提交
		try {
			rsAppToken = HttpClientUtils.postAppJson(appTokenUrl, null, paramApp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JSONObject resJsonApp = SerializationUtils.fromJsonString(rsAppToken, JSONObject.class);
		JSONObject dataJsonApp = new JSONObject();
		if (StringUtil.equals(resJsonApp.get("state").toString(), "success")) {
			dataJsonApp = resJsonApp.getObject("data", JSONObject.class); // 获取AppToken
			System.out.println(dataJsonApp.get("app_token"));
		} else {
			System.out.println("获取失败");
		}

		// 获取access_token
		JSONObject paramAccess = new JSONObject();
		// String accTokenUrl = "http://localhost:8080/ierp/api/login.do";// 系统地址+
		// "/api/login.do";//ierptest.spic.com.cn
		String accTokenUrl = "http://"+strURL+":8000/ierp/api/login.do";// 系统地址+
																			// "/api/login.do";//ierptest.spic.com.cn
		paramAccess.put("tenantid", tenantId); // 向管理员索取（租户ID）
		paramAccess.put("accountId", accountId); // 向管理员索取（数据中心ID）
		paramAccess.put("apptoken", dataJsonApp.get("app_token")); // 5.1 获取的AppToken
		paramAccess.put("user", "18611646231"); // 用户名 （约定一个通用用户）
		paramAccess.put("usertype", "Mobile"); // 1:用户名 2:手机 3：邮箱
		String rsAccess = null; // post提交
		try {
			rsAccess = HttpClientUtils.postAppJson(accTokenUrl, null, paramAccess);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JSONObject resJsonAccess = SerializationUtils.fromJsonString(rsAccess, JSONObject.class);
		JSONObject dataJsonAccess = new JSONObject();
		if (StringUtil.equals(resJsonAccess.get("state").toString(), "success")) {
			dataJsonAccess = resJsonAccess.getObject("data", JSONObject.class); // 获取AppToken
			System.out.println(dataJsonAccess.get("access_token"));
		} else {
			System.out.println("获取失败");
		}

		// 访问标准方法
		JSONObject jsonObject = new JSONObject();
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("datas", valueOne);

		// String url =
		// "http://localhost:8080/ierp/kapi/app/ar/invoice_issue?access_token="
		String url = "http://"+strURL+":8000/ierp/kapi/app/ar/invoice_issue?access_token="
				+ dataJsonAccess.get("access_token");

		// param.put("userid", "ID-000001"); //1:用户名 2:手机 3、邮箱
		Map<String, String> header = new HashMap<>();
		header.put("api", "true");
		header.put("Content-Type", "application/json;charset=utf-8");
		header.put("Authorzation", "Basic YWRtaW46");

		String rs = null; // post提交
		try {
			rs = HttpClientUtils.postAppJson(url, header, param, default_connectionTimeout, default_readTimeout);
		} catch (IOException e) {
			e.printStackTrace();
		}
		JSONObject resJson = SerializationUtils.fromJsonString(rs, JSONObject.class);
		if (resJson.getBoolean("success")) {
			for (Map<String, Object> data : valueOne) {
				resultEntities.add(ResultEntity.SUCCESS().setInitDate((String) data.get("billNo"),
						resJson.get("message").toString(), "远光回推开票单"));
			}
		} else {
			for (Map<String, Object> data : valueOne) {
				resultEntities.add(ResultEntity.PROCESS_ERROR("").setInitDate((String) data.get("billNo"),
						resJson.get("message").toString(), "远光回推开票单"));
			}
		}
		return resultEntities;
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
		// 发票代码 FP_DM
		String FP_DM = dataInfo.elementText("FP_DM");
		if (StringUtils.isBlank(FP_DM)) {
			errorMessage.append("发票代码FP_DM是必填项;");
		}
		// 发票号码 FP_HM
		String FP_HM = dataInfo.elementText("FP_HM");
		if (StringUtils.isBlank(FP_HM)) {
			errorMessage.append("发票号码FP_HM是必填项;");
		}
		// 发票校验码JYM 是否需要校验码？？？？？
		// String billtype = dataInfo.elementText("billtype");
		// if (StringUtils.isBlank(billtype)) {
		// errorMessage.append("单据类型是必填项;");
		// }
		// 开票日期KPRQ
		String KPRQ = dataInfo.elementText("KPRQ");
		if (StringUtils.isBlank(KPRQ)) {
			errorMessage.append("开票日期是必填项;");
		} else {
			try {
				dateFormat.parse(KPRQ);
			} catch (ParseException e) {
				errorMessage.append("开票日期不符合日期格式yyyy-MM-dd;");
			}
		}
		// 发票种类INVKIND
		String INVKIND = dataInfo.elementText("INVKIND");
		if (StringUtils.isBlank(INVKIND)) {
			errorMessage.append("发票种类是必填项;");
		}
		// 作废状态STATUS
		String STATUS = dataInfo.elementText("STATUS");
		if (StringUtils.isBlank(STATUS)) {
			errorMessage.append("作废状态是必填项;");
		}
		// 销售方纳税人识别号XSF_NSRSBH
		String XSF_NSRSBH = dataInfo.elementText("XSF_NSRSBH");
		if (StringUtils.isBlank(XSF_NSRSBH)) {
			errorMessage.append("销售方纳税人识别号是必填项;");
		}
		// 销售方名称XSF_MC
		String XSF_MC = dataInfo.elementText("XSF_MC");
		if (StringUtils.isBlank(XSF_MC)) {
			errorMessage.append("销售方名称是必填项;");
		}
		// 销售方地址XSF_DZ
		String XSF_DZDH = dataInfo.elementText("XSF_DZDH");
		if (StringUtils.isBlank(XSF_DZDH)) {
			errorMessage.append("销售方地址是必填项;");
		}
		// // 销售方电话XSF_DH
		// String XSF_DH = dataInfo.elementText("XSF_DH");
		// if (StringUtils.isBlank(XSF_DH)) {
		// errorMessage.append("销售方电话是必填项;");
		// }
		// 销售方银行名称
		String XSF_YHMC = dataInfo.elementText("XSF_YHMC");
		if (StringUtils.isBlank(XSF_YHMC)) {
			errorMessage.append("销售方银行名称是必填项;");
		}
		// 销售方银行账号
		String XSF_YHZH = dataInfo.elementText("XSF_YHZH");
		if (StringUtils.isBlank(XSF_YHZH)) {
			errorMessage.append("销售方银行账号是必填项;");
		}
		// 购方公司名称GMF_MC 购方-单位名称
		String GMF_MC = dataInfo.elementText("GMF_MC");
		if (StringUtils.isBlank(GMF_MC)) {
			errorMessage.append("购方公司名称(购方-单位名称)是必填项;");
		}
		// 购买方纳税人识别号 GMF_NSRSBH 购方单位-纳税人识别号
		String GMF_NSRSBH = dataInfo.elementText("GMF_NSRSBH");
		if (StringUtils.isBlank(GMF_NSRSBH)) {
			errorMessage.append("购买方纳税人识别号(购方单位-纳税人识别号)是必填项;");
		}
		// 购方公司地址 GMF_DH 购方单位-地址 ???????????? 电话 ？？？？？？？？？？？？？？？？
		String GMF_DZDH = dataInfo.elementText("GMF_DZDH");
		if (StringUtils.isBlank(GMF_DZDH)) {
			errorMessage.append("购方公司地址(购方单位-地址)是必填项;");
		}
		// String GMF_DH = dataInfo.elementText("GMF_DH");
		// if (StringUtils.isBlank(GMF_DH)) {
		// errorMessage.append("购方公司电话(购方电话)是必填项;");
		// }
		// 购方公司电话 GMF_YHMC 购方公司电话 ???????????? 银行名称 ？？？？？？？？？？？？？
		String GMF_YHMC = dataInfo.elementText("GMF_YHMC");
		if (StringUtils.isBlank(GMF_YHMC)) {
			errorMessage.append("销售方银行账号是必填项;");
		}
		// 购方开户行 GMF_KHH 购方单位-开户行
		String GMF_KHH = dataInfo.elementText("GMF_KHH");
		if (StringUtils.isBlank(GMF_KHH)) {
			errorMessage.append("购方开户行(购方单位-开户行)是必填项;");
		}
		// 购方开户行账号 GMF_YHZH 购方单位-账号
		String GMF_YHZH = dataInfo.elementText("GMF_YHZH");
		if (StringUtils.isBlank(GMF_YHZH)) {
			errorMessage.append("购方开户行账号(购方单位-账号)是必填项;");
		}
		// 价税合计
		String JSHJ = dataInfo.elementText("JSHJ");
		if (StringUtils.isBlank(JSHJ)) {
			errorMessage.append("价税合计是必填项;");
		}
		// 开票人 KPR
		String KPR = dataInfo.elementText("KPR");
		if (StringUtils.isBlank(KPR)) {
			errorMessage.append("开票人是必填项;");
		}
		// 收款人 SKR 收票人
		String SKR = dataInfo.elementText("SKR");
		if (StringUtils.isBlank(SKR)) {
			errorMessage.append("收款人(收票人)是必填项;");
		}
		// 复核人 FHR
		String FHR = dataInfo.elementText("FHR");
		if (StringUtils.isBlank(FHR)) {
			errorMessage.append("复核人是必填项;");
		}
		// 合计金额 HJJE 金额
		String HJJE = dataInfo.elementText("HJJE");
		if (StringUtils.isBlank(HJJE)) {
			errorMessage.append("合计金额(金额)是必填项;");
		}
		// 合计税额 HJSE 税额
		String HJSE = dataInfo.elementText("HJSE");
		if (StringUtils.isBlank(HJSE)) {
			errorMessage.append("合计税额(税额)是必填项;");
		}
		// ID 唯一标识 税务必录 单据编码+组织

		List<Element> datailentries = dataInfo.element("INVDETINFO").element("CHILDLIST").elements("CHILD");
		List<String> entryErrorMessages = new ArrayList<>();
		int rowIndex = 0;
		for (Element detailentry : datailentries) {
			rowIndex++;
			StringBuilder errorBuild = new StringBuilder();
			// 商品服务编码 SPBM 税收分类编码
			String SPBM = detailentry.elementText("SPBM");
			if (StringUtils.isBlank(SPBM)) {
				errorBuild.append("商品服务编码(税收分类编码)是必填项");
			}
			// 商品服务名称 XMMC 开票名称
			String measureunit = detailentry.elementText("measureunit");
			if (StringUtils.isBlank(measureunit)) {
				errorBuild.append("商品服务名称(开票名称)是必填项");
			}
			// 商品金额 XMJE 金额
			String XMJE = detailentry.elementText("XMJE");
			if (StringUtils.isBlank(XMJE)) {
				errorBuild.append("商品金额(金额)是必填项");
			}
			// 税率SL
			String SL = detailentry.elementText("SL");
			if (StringUtils.isBlank(SL)) {
				errorBuild.append("税率是必填项");
			}
			// 税额 SE
			String SE = detailentry.elementText("SE");
			if (StringUtils.isBlank(SE)) {
				errorBuild.append("税额是必填项");
			}
			// String isincludetax = detailentry.elementText("isincludetax");
			// if (StringUtils.isBlank(isincludetax)) {
			// errorBuild.append("含税单价是必填项");
			// }
			// String pricetaxtotal = detailentry.elementText("pricetaxtotal");
			// if (StringUtils.isBlank(pricetaxtotal)) {
			// errorBuild.append("应付金额是必填项");
			// }
			// if (StringUtils.isNotBlank(errorBuild.toString())) {
			// entryErrorMessages.add("行号：" + rowIndex + errorBuild.toString());
			// }
		}
		if (entryErrorMessages.size() > 0) {
			errorMessage.append(JSON.toJSONString(entryErrorMessages));
			return ResultEntity.PROCESS_ERROR(errorMessage.toString()).setInitDate("", "", "开票单");
		}
		return null;
	}

	/**
	 * 重组，构造参数
	 *
	 * @param dataInfo
	 * @return
	 */
	private List<Map<String, Object>> constructValue(Element dataInfo) {

		List returnList = new ArrayList();

		String billNOs = dataInfo.element("YWIDS").elementText("ID");
		if (billNOs.contains(",")) {
			String[] billnos = billNOs.split(",");
			for (int i = 0; i < billnos.length; i++) {
				getReMapMore(dataInfo, returnList, billnos, i);
			}
		} else {
			getReMapOne(dataInfo, returnList, billNOs);
		}

		return returnList;
	}

	private void getReMapOne(Element dataInfo, List returnList, String billNOs) {
		Map<String, Object> retMap = new HashMap<>();
		// 单据编号
		// ID 唯一标识吗 单据编号---
		retMap.put("billNo", billNOs);
		// invoiceCode 发票代码 FP_DM
		String FP_DM = dataInfo.elementText("FP_DM");
		retMap.put("invoiceCode", FP_DM);

		// invoiceNo 发票号码 FP_HM
		String FP_HM = dataInfo.elementText("FP_HM");
		retMap.put("invoiceNo", FP_HM);

		// invoiceDate 开票日期 KPRQ
		String KPRQ = dataInfo.elementText("KPRQ");
		// new SimpleDateFormat().parse().getTime();
		try {
			retMap.put("invoiceDate", new SimpleDateFormat("yyyy-mm-dd").parse(KPRQ).getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// retMap.put("invoiceDate", Date.parse(KPRQ));
		// retMap.put("invoiceDate", KPRQ);

		// 发票类型 INVKIND 发票种类 "invoiceType”:”1”,
		// 发票种类：1、普通电子发票；2、电子发票专票；3、普通纸质发票；4、专用纸质发票；5、普通纸质卷票
		String INVKIND = dataInfo.elementText("INVKIND");
		retMap.put("invoicetype", Integer.parseInt(INVKIND));

		// 开票状态 STATUS "invoiceStatus”:”1”, //发票状态 0是正常，1是作废。
		// 发票状态0:正常、1：失控、2：作废、3：红冲、4：异常
		String STATUS = dataInfo.elementText("STATUS");
		if ("0".equals(STATUS)) {
			retMap.put("invoiceStatus", Integer.parseInt("0"));
		} else if ("1".equals(STATUS)) {
			retMap.put("invoiceStatus", Integer.parseInt("2"));
		}

		// // 是否作废重开SFZFCK
		// String SFZFCK = dataInfo.elementText("SFZFCK");
		// retMap.put("invoiceStatus", SFZFCK);

		// "type": 0, ????????????????????? //0蓝票，1红票
		String YFPLX = dataInfo.elementText("YFPLX");
		if ("".equals(YFPLX)) {
			retMap.put("type", Integer.parseInt("0"));
		} else {
			retMap.put("type", Integer.parseInt("1"));
			retMap.put("originalInvoiceCode", dataInfo.elementText("YFP_DM")); // 原发票代码
			retMap.put("originalInvoiceNo", dataInfo.elementText("YFP_HM"));// 原发票号码
		}

		// XXBBH 信息表编号 红字信息表编码
		// YFP_DM 原发票代码 蓝票发票代码
		// YFP_HM 原发票号码 蓝票发票号码

		// "salerTaxNo”销货方方识别号 XSF_NSRSBH 销售方纳税人识别号
		String XSF_NSRSBH = dataInfo.elementText("XSF_NSRSBH");
		retMap.put("salerTaxNo", XSF_NSRSBH);

		// "salerName”销货方名称 XSF_MC 销售方名称
		String XSF_MC = dataInfo.elementText("XSF_MC");
		retMap.put("salerName", XSF_MC);

		// "salerAddressPhone”:”深圳金蝶软件软B栋13652398890”,销货方地址、固定电话
		// XSF_DZDH 销售方地址电话
		String XSF_DZDH = dataInfo.elementText("XSF_DZDH");
		// String XSF_DH = dataInfo.elementText("XSF_DH");
		retMap.put("salerAddressPhone", XSF_DZDH);

		// XSF_YHMC 销售方银行名称 销售方银行名称
		// String XSF_YHMC = dataInfo.elementText("XSF_YHMC");
		// retMap.put("salerAccount", XSF_YHZH);

		// XSF_YHZH 销售方银行账号 salerAccount招商银行987654321”销货方银行账号
		String XSF_YHZH = dataInfo.elementText("XSF_YHZH");
		retMap.put("salerAccount", XSF_YHZH);

		// GMF_MC购方公司名称 购方-单位名称 "buyerName”:”河南派客”,购货方名称
		String GMF_MC = dataInfo.elementText("GMF_MC");
		retMap.put("buyerName", GMF_MC);

		// GMF_NSRSBH 购买方纳税人识别号 "buyerTaxNo”购货方方识别号
		String GMF_NSRSBH = dataInfo.elementText("GMF_NSRSBH");
		retMap.put("buyerTaxNo", GMF_NSRSBH);

		// "buyerAddressPhone”购货方地址、固定电话buyeraddr buyertel
		String GMF_DZDH = dataInfo.elementText("GMF_DZDH");
		// String GMF_DH = dataInfo.elementText("GMF_DH");
		retMap.put("buyerAddressPhone", GMF_DZDH);

		// 购买方开户行 GMF_KHH 购方开户行

		// "buyerAccount”购货方银行账号 GMF_YHZH 购方开户行账号
		String GMF_YHZH = dataInfo.elementText("GMF_YHZH");
		retMap.put("buyerAccount", GMF_YHZH);

		// 价税合计 JSHJ 价税合计"totalAmount”:234.00价税合计金额
		String JSHJ = dataInfo.elementText("JSHJ");
		retMap.put("totalAmount", Double.parseDouble(JSHJ));

		// "invoiceAmount”:200.00,//不含税金额 HJJE 合计金额
		String HJJE = dataInfo.elementText("HJJE");
		retMap.put("invoiceAmount", Double.parseDouble(HJJE));

		// "totalTaxAmount”:34.00,//税额 HJSE 合计税额
		String HJSE = dataInfo.elementText("HJSE");
		retMap.put("totalTaxAmount", Double.parseDouble(HJSE));

		// "drawer”:”测试企业”, //开票员 KPR 开票人
		String KPR = dataInfo.elementText("KPR");
		retMap.put("drawer", KPR);

		// "payee”:””, //收款员 SKR 收款人
		String SKR = dataInfo.elementText("SKR");
		retMap.put("payee", SKR);

		// "reviewer”:””, //复核人 FHR 复核人
		String FHR = dataInfo.elementText("FHR");
		retMap.put("reviewer", FHR);

		// BZ 备注 "remark”:””,//备注
		String BZ = dataInfo.elementText("BZ");
		retMap.put("remark", BZ);

		// detailentry Element INVDETINFO =
		// dataInfo.element("INVDETINFO").element("CHILDLIST");
		List<Element> datailentries = dataInfo.element("INVDETINFO").element("CHILDLIST").elements("CHILD");
		List<Map<String, Object>> detailentryList = new ArrayList<>();
		for (Element detailentry : datailentries) {
			Map<String, Object> row = new HashMap<>();

			String MXID = detailentry.elementText("MXID");
			row.put("itemId", MXID);

			// DW 计量单位
			String DW = detailentry.elementText("DW");
			row.put("unit", DW);

			// XMMC 商品服务名称 "goodsName”:”食品”, //项目名称 ---- 开票名称
			String XMMC = detailentry.elementText("XMMC");
			row.put("goodsName", XMMC);

			// XMJE 商品金额 "detailAmount”:200.00,//项目金额
			String XMJE = detailentry.elementText("XMJE");
			row.put("detailAmount", Double.parseDouble(XMJE));

			// SL 税率 "taxRate”:0.170000 ,//税率
			String SL = detailentry.elementText("SL");
			row.put("taxRate", Double.parseDouble(SL));

			// SE 税额 "taxAmount”:34.00,//税额
			String SE = detailentry.elementText("SE");
			row.put("taxAmount", Double.parseDouble(SE));

			// ZKJE 折扣金额（含税）
			// "taxFlag": "0", ????????????????? //含税标记
			// "preferentialPolicy": 0, //优惠政策
			// "zeroTaxRateFlag": "", //零税率标识
			// "unit": "毫升", //项目单位 you
			// "discountType": "0", //折扣行
			// "goodsCode": "1010303010000000000", //商品编码

			// GGXH 规格型号 "specModel”:””,//规格型号
			String GGXH = detailentry.elementText("GGXH");
			row.put("specModel", GGXH);

			// XMSL 项目数量 "num”:1.000000, //项目数量
			String XMSL = detailentry.elementText("XMSL");
			row.put("num", Double.parseDouble(XMSL));

			// XMDJ 项目单价 "unitPrice”:200.000000, //项目单价
			String XMDJ = detailentry.elementText("XMDJ");
			row.put("unitPrice", Double.parseDouble(XMDJ));

			// "zeroTaxRateFlag”:”1” , 零税率标识 LSLBS
			String LSLBS = detailentry.elementText("LSLBS");
			row.put("zeroTaxRateFlag", LSLBS);
			// "discountType”:””, 折扣行

			// "preferentialPolicy”:””, 优惠政策 YHZCBS
			String YHZCBS = detailentry.elementText("YHZCBS");
			row.put("vatException", YHZCBS);

			// "vatException”:”” 增值税特殊管理 ZZSTSGL 增值税特殊管理
			String ZZSTSGL = detailentry.elementText("ZZSTSGL");
			row.put("vatException", ZZSTSGL);
			// "taxFlag”:0, 含税标记

			detailentryList.add(row);
		}
		retMap.put("items", detailentryList);
		// retMap.put("recentry", detailentryList);
		// return retMap;
		returnList.add(retMap);
	}

	private void getReMapMore(Element dataInfo, List returnList, String[] billnos, int i) {
		Map<String, Object> retMap = new HashMap<>();
		// 单据编号
		// ID 唯一标识吗 单据编号---
		retMap.put("billNo", billnos[i]);

		// invoiceCode 发票代码 FP_DM
		String FP_DM = dataInfo.elementText("FP_DM");
		retMap.put("invoiceCode", FP_DM);

		// invoiceNo 发票号码 FP_HM
		String FP_HM = dataInfo.elementText("FP_HM");
		retMap.put("invoiceNo", FP_HM);

		// invoiceDate 开票日期 KPRQ
		// invoiceDate 开票日期 KPRQ
		String KPRQ = dataInfo.elementText("KPRQ");
		// new SimpleDateFormat().parse().getTime();
		try {
			retMap.put("invoiceDate", new SimpleDateFormat("yyyy-mm-dd").parse(KPRQ).getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 发票类型 INVKIND 发票种类 "invoiceType”:”1”,
		// 发票种类：1、普通电子发票；2、电子发票专票；3、普通纸质发票；4、专用纸质发票；5、普通纸质卷票
		String INVKIND = dataInfo.elementText("INVKIND");
		retMap.put("invoicetype", Integer.parseInt(INVKIND));

		// 开票状态 STATUS "invoiceStatus”:”1”, //发票状态 0是正常，1是作废。
		// 发票状态0:正常、1：失控、2：作废、3：红冲、4：异常
		String STATUS = dataInfo.elementText("STATUS");
		if ("0".equals(STATUS)) {
			retMap.put("invoiceStatus", Integer.parseInt("0"));
		} else if ("1".equals(STATUS)) {
			retMap.put("invoiceStatus", Integer.parseInt("2"));
		}

		// // 是否作废重开SFZFCK
		// String SFZFCK = dataInfo.elementText("SFZFCK");
		// retMap.put("invoiceStatus", SFZFCK);

		// XXBBH 信息表编号 红字信息表编码
		// YFP_DM 原发票代码 蓝票发票代码
		// YFP_HM 原发票号码 蓝票发票号码

		// "salerTaxNo”销货方方识别号 XSF_NSRSBH 销售方纳税人识别号
		String XSF_NSRSBH = dataInfo.elementText("XSF_NSRSBH");
		retMap.put("salerTaxNo", XSF_NSRSBH);

		// "salerName”销货方名称 XSF_MC 销售方名称
		String XSF_MC = dataInfo.elementText("XSF_MC");
		retMap.put("salerName", XSF_MC);

		// "salerAddressPhone”:”深圳金蝶软件软B栋13652398890”,销货方地址、固定电话
		// XSF_DZDH 销售方地址电话
		String XSF_DZDH = dataInfo.elementText("XSF_DZDH");
		// String XSF_DZ = dataInfo.elementText("XSF_DZ");
		retMap.put("salerAddressPhone", XSF_DZDH);

		// "type": 0, ????????????????????? //0蓝票，1红票
		String YFPLX = dataInfo.elementText("YFPLX");
		if ("".equals(YFPLX)) {
			retMap.put("type", Integer.parseInt("0"));
		} else {
			retMap.put("type", Integer.parseInt("1"));
			retMap.put("originalInvoiceCode", dataInfo.elementText("YFP_DM")); // 原发票代码
			retMap.put("originalInvoiceNo", dataInfo.elementText("YFP_HM"));// 原发票号码
		}
		// XSF_YHMC 销售方银行名称 销售方银行名称
		// String XSF_YHMC = dataInfo.elementText("XSF_YHMC");
		// retMap.put("salerAccount", XSF_YHZH);

		// XSF_YHZH 销售方银行账号 salerAccount招商银行987654321”销货方银行账号
		String XSF_YHZH = dataInfo.elementText("XSF_YHZH");
		retMap.put("salerAccount", XSF_YHZH);

		// GMF_MC购方公司名称 购方-单位名称 "buyerName”:”河南派客”,购货方名称
		String GMF_MC = dataInfo.elementText("GMF_MC");
		retMap.put("buyerName", GMF_MC);

		// GMF_NSRSBH 购买方纳税人识别号 "buyerTaxNo”购货方方识别号
		String GMF_NSRSBH = dataInfo.elementText("GMF_NSRSBH");
		retMap.put("buyerTaxNo", GMF_NSRSBH);

		// "buyerAddressPhone”购货方地址、固定电话buyeraddr buyertel
		String GMF_DZDH = dataInfo.elementText("GMF_DZDH");
		// String GMF_DH = dataInfo.elementText("GMF_DH");
		retMap.put("buyerAddressPhone", GMF_DZDH);

		// 购买方开户行 GMF_KHH 购方开户行

		// "buyerAccount”购货方银行账号 GMF_YHZH 购方开户行账号
		String GMF_YHZH = dataInfo.elementText("GMF_YHZH");
		retMap.put("buyerAccount", GMF_YHZH);

		// 价税合计 JSHJ 价税合计"totalAmount”:234.00价税合计金额
		String JSHJ = dataInfo.elementText("JSHJ");
		retMap.put("totalAmount", Double.parseDouble(JSHJ));

		// "invoiceAmount”:200.00,//不含税金额 HJJE 合计金额
		String HJJE = dataInfo.elementText("HJJE");
		retMap.put("invoiceAmount", Double.parseDouble(HJJE));

		// "totalTaxAmount”:34.00,//税额 HJSE 合计税额
		String HJSE = dataInfo.elementText("HJSE");
		retMap.put("totalTaxAmount", Double.parseDouble(HJSE));

		// "drawer”:”测试企业”, //开票员 KPR 开票人
		String KPR = dataInfo.elementText("KPR");
		retMap.put("drawer", KPR);

		// "payee”:””, //收款员 SKR 收款人
		String SKR = dataInfo.elementText("SKR");
		retMap.put("payee", SKR);

		// "reviewer”:””, //复核人 FHR 复核人
		String FHR = dataInfo.elementText("FHR");
		retMap.put("reviewer", FHR);

		// BZ 备注 "remark”:””,//备注
		String BZ = dataInfo.elementText("BZ");
		retMap.put("remark", BZ);

		// detailentry Element INVDETINFO =
		// dataInfo.element("INVDETINFO").element("CHILDLIST");
		List<Element> datailentries = dataInfo.element("INVDETINFO").element("CHILDLIST").elements("CHILD");
		List<Map<String, Object>> detailentryList = new ArrayList<>();
		for (Element detailentry : datailentries) {
			Map<String, Object> row = new HashMap<>();

			// SPBM 商品服务编码 "goodsCode”:”3040203000000000000”,//商品编码 --- 税收分类编码
			String SPBM = detailentry.elementText("SPBM");
			row.put("goodsCode", SPBM);

			// 行id
			String MXID = detailentry.elementText("MXID");
			row.put("itemId", MXID);

			// XMMC 商品服务名称 "goodsName”:”食品”, //项目名称 ---- 开票名称
			String XMMC = detailentry.elementText("XMMC");
			row.put("goodsName", XMMC);

			// XMJE 商品金额 "detailAmount”:200.00,//项目金额
			String XMJE = detailentry.elementText("XMJE");
			row.put("detailAmount", Double.parseDouble(XMJE));

			// SL 税率 "taxRate”:0.170000 ,//税率
			String SL = detailentry.elementText("SL");
			row.put("taxRate", Double.parseDouble(SL));

			// SE 税额 "taxAmount”:34.00,//税额
			String SE = detailentry.elementText("SE");
			row.put("taxAmount", Double.parseDouble(SE));

			// ZKJE 折扣金额（含税）
			// DW 计量单位
			String DW = detailentry.elementText("DW");
			row.put("unit", DW);

			// GGXH 规格型号 "specModel”:””,//规格型号
			String GGXH = detailentry.elementText("GGXH");
			row.put("specModel", GGXH);

			// XMSL 项目数量 "num”:1.000000, //项目数量
			String XMSL = detailentry.elementText("XMSL");
			row.put("num", Double.parseDouble(XMSL));

			// XMDJ 项目单价 "unitPrice”:200.000000, //项目单价
			String XMDJ = detailentry.elementText("XMDJ");
			row.put("unitPrice", Double.parseDouble(XMDJ));

			// "zeroTaxRateFlag”:”1” , 零税率标识 LSLBS
			// "taxFlag”:0, 含税标记
			String LSLBS = detailentry.elementText("LSLBS");
			if ("".equals(LSLBS)) {
				row.put("zeroTaxRateFlag", LSLBS);
				row.put("taxFlag", "1");
			} else {
				row.put("zeroTaxRateFlag", LSLBS);
				row.put("taxFlag", "0");
			}

			// "discountType”:””, 折扣行 FPHXZ 0正常行、1折扣行、2被折扣行
			String FPHXZ = detailentry.elementText("FPHXZ");
			row.put("discountType", FPHXZ);

			// "preferentialPolicy”:””, 优惠政策 YHZCBS
			String YHZCBS = detailentry.elementText("YHZCBS");
			row.put("vatException", YHZCBS);

			// "vatException”:”” 增值税特殊管理 ZZSTSGL 增值税特殊管理
			String ZZSTSGL = detailentry.elementText("ZZSTSGL");
			row.put("vatException", ZZSTSGL);

			detailentryList.add(row);
		}
		retMap.put("items", detailentryList);
		// retMap.put("recentry", detailentryList);
		// return retMap;
		returnList.add(retMap);
	}
}