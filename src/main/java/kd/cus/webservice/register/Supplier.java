package kd.cus.webservice.register;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import kd.bos.context.RequestContextCreator;
import kd.bos.service.operation.OperationServiceImpl;
import org.dom4j.Element;

import kd.bd.master.util.GroupStandardUtils;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.operate.result.IOperateInfo;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.property.MuliLangTextProp;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.service.business.datamodel.DynamicFormModelProxy;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.cus.api.ParseResultMessageUtil;
import kd.cus.webservice.action.WebserviceMainTemplate;
import kd.cus.webservice.action.entity.ResultEntity;
import kd.isc.iscb.platform.core.util.setter.MuliLangTextPropSetter;

/*
 示例数据：
 <DATAINFO>
        <CODE REMARK="供应商编码">10040147</CODE>
        <DESC1 REMARK="注册名称">上海合凯电力保护设备有限公司</DESC1>
        <DESC2 REMARK="注册国家">中国</DESC2>
        <DESC3 REMARK="注册省/市">上海市</DESC3>
        <DESC4 REMARK="注册城市">市辖区</DESC4>
        <DESC5 REMARK="注册地址">上海市松江区民益路201号1幢302室</DESC5>
        <DESC6 REMARK="公司类型">有限责任公司</DESC6>
        <DESC7 REMARK="注册资本">1100万元</DESC7>
        <DESC8 REMARK="注册币种">CNY</DESC8>
        <DESC9 REMARK="注册经营范围">三相组合式过电压保护器，消弧及过电压保护装置，自脱离大容量过电压保护装置，大容量高速开关保护装置，高压限流熔断组合保护装置等产品的设计、生产、销售。电网配套设备，电站成套设备，电气设备批发零售。电力保护设备领域四技服务。</DESC9>
        <DESC10 REMARK="营业执照注册号">9131011763159495XP</DESC10>
        <DESC11 REMARK="法定代表人名称">郭耀华</DESC11>
        <DESC12 REMARK="营业执照颁发机构">工商局</DESC12>
        <DESC13 REMARK="营业执照有效期">2020-01-04</DESC13>
        <DESC14 REMARK="纳税登记证号">9131011763159495XP</DESC14>
        <DESC15 REMARK="单位成立时间">2000-01-05</DESC15>
        <DESC16 REMARK="组织机构代码">9131011763159495XP</DESC16>
        <DESC17 REMARK="企业行业类别">其它</DESC17>
        <DESC18 REMARK="行政区域代码">310117</DESC18>
        <DESC19 REMARK="国家电话代码"></DESC19>
        <DESC20 REMARK="公司曾用名称"></DESC20>
        <DESC21 REMARK="总公司"></DESC21>
        <DESC22 REMARK="母公司"></DESC22>
        <DESC23 REMARK="英文名称"></DESC23>
        <DESC24 REMARK="简称"></DESC24>
        <DESC25 REMARK="邮政编码">230088</DESC25>
        <DESC26 REMARK="通讯地址">合肥市双凤开发区双凤路28号</DESC26>
        <DESC27 REMARK="电话">0551-65684709</DESC27>
        <DESC28 REMARK="传真">0551-65684711</DESC28>
        <DESC29 REMARK="网址"></DESC29>
        <DESC30 REMARK="邮箱"></DESC30>
        <DESCSHORT REMARK="短描述"></DESCSHORT>
        <DESC32 REMARK="状态">正常</DESC32>
        <DESC31 REMARK="是否内部供应商">否</DESC31>
        <DESC33 REMARK="统一社会信用代码"></DESC33>
        <REMARK REMARK="备注"></REMARK>
        <UUID REMARK="MDM唯一标识"></UUID>
        <CODEID REMARK="MDM主键">14668679</CODEID>
        <FREEZEFLAG REMARK="数据状态">0</FREEZEFLAG>
        <MNEMONICCODE REMARK="助记码">SHHKDLBHSBYXGS</MNEMONICCODE>
        <RECORDERCODE REMARK="制单人编码">admin</RECORDERCODE>
        <RECORDERDESC REMARK="制单人名称">超级管理员</RECORDERDESC>
        <RECORDTIME REMARK="制单时间">2017-12-27 15:06:33</RECORDTIME>
        <RECORDERDCORP REMARK="制单人单位编码">10001</RECORDERDCORP>
        <SUBMITCORP REMARK="提报单位编码">10001</SUBMITCORP>
        <AUDITORCODE REMARK="审核人编码"></AUDITORCODE>
        <AUDITORDESC REMARK="审核人名称"></AUDITORDESC>
        <AUDITTIME REMARK="审核时间"></AUDITTIME>
        <VERSION REMARK="主数据版本">0</VERSION>
        <SYSCODEVERSION REMARK="主数据模型版本">17</SYSCODEVERSION>
        <SPECIALITYCODES></SPECIALITYCODES>
    </DATAINFO>
 */

/**
 * @author Wu Yanqi
 */
public class Supplier implements WebserviceMainTemplate {

	@Override
	public List<ResultEntity> disposeDate(List<Element> children) throws Exception {
		List<ResultEntity> resultEnties = new ArrayList<ResultEntity>();
		List<DynamicObject> suppliers = new ArrayList<DynamicObject>();
		Map<String, String> uniqueSup = new HashMap<String, String>();
		for (Element dataInfo : children) {
			// 1. 从element中读取各属性的值
			String CODE = dataInfo.element("CODE").getText(); // 供应商编码
			String CODEID = dataInfo.element("CODEID").getText(); // MDM主键
			String VERSION = dataInfo.element("VERSION").getText(); // 主数据版本
			// 2. 以CODEID作为供应商在苍穹系统中的主键
			boolean exists = QueryServiceHelper.exists("bd_supplier", CODEID);
			if (!exists) {
				// 新增
				Map<Class<?>, Object> services = new HashMap<>();
				DynamicFormModelProxy model = new DynamicFormModelProxy("bd_supplier", UUID.randomUUID().toString(),
						services);
				model.createNewData();
				// 新建供应商对象
				DynamicObject supplieObj = model.getDataEntity();
				// 设置主键
				supplieObj.set("id", CODEID);
				supplieObj.set("number", CODE); // 编码，如果有自己的编码规则，则不需要传值

				doExecute(dataInfo, supplieObj, false);

				suppliers.add(supplieObj);
				uniqueSup.put(CODEID, VERSION);
			} else {
				// 更新
				DynamicObject supplierFromDB = BusinessDataServiceHelper.loadSingle(CODEID, "bd_supplier");
				doExecute(dataInfo, supplierFromDB, true);
				suppliers.add(supplierFromDB);
				uniqueSup.put(CODEID, VERSION);
			}
		}
		// 保存/更新到数据库
		OperationResult operationResult = OperationServiceHelper.executeOperate("save", "bd_supplier",
				suppliers.toArray(new DynamicObject[suppliers.size()]), OperateOption.create());
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
			resultEnties.add(ResultEntity.PROCESS_ERROR(res.getValue()).setInitDate(res.getKey().toString(),
					uniqueSup.get(res.getKey().toString()), "主数据"));
		});
//		RequestContextCreator.createForTripSI("spic", "872529855609044992", "934736282129537024");
		// 提交/审核
		List<Object> successPkIdsList = operationResult.getSuccessPkIds();
		if (successPkIdsList.size() <= 0) {
			return resultEnties;
		}
		Set<Object> successPkIds = new HashSet<Object>(successPkIdsList);
		// 提交
		DynamicObject[] successDynamicObjs = BusinessDataServiceHelper.load(successPkIds.toArray(),
				suppliers.get(0).getDynamicObjectType());
		OperationResult executeOperate1 = OperationServiceHelper.executeOperate("submit", "bd_supplier",
				successDynamicObjs, null);
		Set<Object> submitSuccess = new HashSet<Object>(executeOperate1.getSuccessPkIds());
		// 审核
		DynamicObject[] successSubmitDynamicObjs = BusinessDataServiceHelper.load(submitSuccess.toArray(),
				suppliers.get(0).getDynamicObjectType());
		OperationResult executeOperate2 = OperationServiceHelper.executeOperate("audit", "bd_supplier",
				successSubmitDynamicObjs, null);
		Set<Object> auditSuccess = new HashSet<Object>(executeOperate2.getSuccessPkIds());
		for (Object object : auditSuccess) {
			resultEnties.add(
					ResultEntity.SUCCESS().setInitDate(object.toString(), uniqueSup.get(object.toString()), "主数据"));
		}
		return resultEnties;
	}

	private void doExecute(Element dataInfo, DynamicObject supplieObj, boolean isUpdate) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String DESC1 = dataInfo.element("DESC1").getText(); // 注册名称
		String DESC2 = dataInfo.element("DESC2").getText(); // 注册国家
		String DESC3 = dataInfo.element("DESC3").getText(); // 注册省/市
		String DESC4 = dataInfo.element("DESC4").getText(); // 注册城市
		String DESC5 = dataInfo.element("DESC5").getText(); // 注册地址
		String DESC6 = dataInfo.element("DESC6").getText(); // 公司类型
		String DESC7 = dataInfo.element("DESC7").getText(); // 注册资本
		String DESC8 = dataInfo.element("DESC8").getText(); // 注册币种
		String DESC9 = dataInfo.element("DESC9").getText(); // 注册经营范围
		String DESC10 = dataInfo.element("DESC10").getText(); // 营业执照注册号
		String DESC11 = dataInfo.element("DESC11").getText(); // 法定代表人名称
		String DESC12 = dataInfo.element("DESC12").getText(); // 营业执照颁发机构
		String DESC13 = dataInfo.element("DESC13").getText(); // 营业执照有效期
		String DESC14 = dataInfo.element("DESC14").getText(); // 纳税登记证号
		String DESC15 = dataInfo.element("DESC15").getText(); // 单位成立时间
		String DESC16 = dataInfo.element("DESC16").getText(); // 组织机构代码
		String DESC17 = dataInfo.element("DESC17").getText(); // 企业行业类别
		String DESC18 = dataInfo.element("DESC18").getText(); // 行政区域代码
		String DESC19 = dataInfo.element("DESC19").getText(); // 国家电话代码
		String DESC20 = dataInfo.element("DESC20").getText(); // 公司曾用名称
		String DESC21 = dataInfo.element("DESC21").getText(); // 总公司
		String DESC22 = dataInfo.element("DESC22").getText(); // 母公司
		String DESC23 = dataInfo.element("DESC23").getText(); // 英文名称
		String DESC24 = dataInfo.element("DESC24").getText(); // 简称
		String DESC25 = dataInfo.element("DESC25").getText(); // 邮政编码
		String DESC26 = dataInfo.element("DESC26").getText(); // 通讯地址
		String DESC27 = dataInfo.element("DESC27").getText(); // 电话
		String DESC28 = dataInfo.element("DESC28").getText(); // 传真
		String DESC29 = dataInfo.element("DESC29").getText(); // 网址
		String DESC30 = dataInfo.element("DESC30").getText(); // 邮箱
		String DESCSHORT = dataInfo.element("DESCSHORT").getText(); // 短描述
		String DESC31 = dataInfo.element("DESC31").getText(); // 是否内部供应商
		String DESC32 = dataInfo.element("DESC32").getText(); // 状态
		String DESC33 = dataInfo.element("DESC33").getText(); // 统一社会信用代码
		String REMARK = dataInfo.element("REMARK").getText(); // 备注
		String SUBMITCORP = dataInfo.element("SUBMITCORP").getText(); // 提报单位编码
		Element SPECIALITYCODES = dataInfo.element("SPECIALITYCODES");

		supplieObj.set("name", DESC1); // 注册名称
		// 根据行政组织代码获取行政组织对象
		// 国家电力投资集团有限公司(100000)
		// 北方：(A002)
		DynamicObject adminorg = BusinessDataServiceHelper.loadSingle("bos_adminorg", "",
				new QFilter[] { new QFilter("number", QCP.equals, "100000") });
		// 创建组织
		supplieObj.set("createorg", adminorg);

		// ---------------------工商信息---------------------
		// 国家/地区
		DynamicObject country = BusinessDataServiceHelper.loadSingle("bd_country", "",
				new QFilter[] { new QFilter("name", QCP.equals, DESC2) });
		supplieObj.set("country", country);
		// 统一社会信用代码
		supplieObj.set("societycreditcode", DESC33);
		// 工商登记号 biz_register_num
		supplieObj.set("biz_register_num", DESC10);
		// 纳税人识别号
		supplieObj.set("tx_register_no", DESC14);
		// 法人代表
		MuliLangTextProp artificialperson = new MuliLangTextProp();
		MuliLangTextPropSetter artificialpersonSetter = new MuliLangTextPropSetter(artificialperson);
		artificialpersonSetter.setObjValue(supplieObj, "artificialperson", DESC11);
		// 注册资本（万)
		MuliLangTextProp regcapital = new MuliLangTextProp();
		MuliLangTextPropSetter regcapitalSetter = new MuliLangTextPropSetter(regcapital);
		regcapitalSetter.setObjValue(supplieObj, "regcapital", DESC7);
		// 营业期限
		MuliLangTextProp businessterm = new MuliLangTextProp();
		MuliLangTextPropSetter businesstermSetter = new MuliLangTextPropSetter(businessterm);
		businesstermSetter.setObjValue(supplieObj, "businessterm", DESC13);
		// 经营范围
		MuliLangTextProp businessscope = new MuliLangTextProp();
		MuliLangTextPropSetter businessscopeSetter = new MuliLangTextPropSetter(businessscope);
		businessscopeSetter.setObjValue(supplieObj, "businessscope", DESC9);
		// 成立日期
		if (StringUtils.isNotBlank(DESC15)) {
			supplieObj.set("establishdate", dateFormat.parse(DESC15)); // 日期格式：yyyy-MM-dd
		}
		// 简称
		MuliLangTextProp simplename = new MuliLangTextProp();
		MuliLangTextPropSetter simplenameSetter = new MuliLangTextPropSetter(simplename);
		simplenameSetter.setObjValue(supplieObj, "simplename", DESC24);
		// 联系电话
		supplieObj.set("bizpartner_phone", DESC27);
		// 传真
		supplieObj.set("bizpartner_fax", DESC28);
		// 公司网址
		supplieObj.set("url", DESC29);
		// 电子邮箱
		supplieObj.set("postal_code", DESC30);
		// 行政区划
		// 详细地址 bizpartner_address
		MuliLangTextProp bizpartner_address = new MuliLangTextProp();
		MuliLangTextPropSetter bizpartner_addressSetter = new MuliLangTextPropSetter(bizpartner_address);
		bizpartner_addressSetter.setObjValue(supplieObj, "bizpartner_address", DESC5);
		// 身份证号
		// 组织机构代码
		supplieObj.set("orgcode", DESC16);
		/*--------------拓展字段--------------*/
		// 短描述
		supplieObj.set("spic_short_desc", DESCSHORT);
		// 是否内部供应商 bos_org，内部业务单元
		if (StringUtils.equals("是", DESC31)) {
			DynamicObject bosOrg = BusinessDataServiceHelper.loadSingle("bos_org", "",
					new QFilter[] { new QFilter("name", QCP.equals, DESC1) });
			supplieObj.set("internal_company", bosOrg);
		}
		// 公司类型 spic_type
		supplieObj.set("spic_type", DESC6);
		// 提报单位编码
		supplieObj.set("spic_reportunitcode", SUBMITCORP);
		// 备注
		supplieObj.set("spic_remark", REMARK);
		// 企业行业类别
		supplieObj.set("spic_industrytype", DESC17);
		// 行政区域代码
		supplieObj.set("spic_regioncode", DESC18);
		// 公司曾用名称
		supplieObj.set("spic_originname", DESC20);
		// 总公司 DESC21
		supplieObj.set("spic_firstoffice", DESC21);
		// 母公司 DESC22
		supplieObj.set("spic_parentoffice", DESC22);
		// 英文名称 DESC23
		supplieObj.set("spic_englishname", DESC23);
		// 通讯地址
		supplieObj.set("spic_mailaddr", DESC26);
		// 邮政编码
		supplieObj.set("spic_postcode", DESC25);
		// 营业执照颁发机构 spic_buslicauthority
		supplieObj.set("spic_buslicauthority", DESC12);

		// ---------------------采购信息---------------------
		// 交易币别
		DynamicObject settlementcyObj = BusinessDataServiceHelper.loadSingle("bd_currency", "",
				new QFilter[] { new QFilter("number", QCP.equals, DESC8) });
		supplieObj.set("settlementcyid", settlementcyObj);

		List<Element> specialityItems = SPECIALITYCODES.elements("SPECIALITYCODE");
		for (Element specialitycode : specialityItems) {
			String specialitycodeAttr = specialitycode.attribute("SPECIALITYCODE").getText();
			if (StringUtils.equals(specialitycodeAttr, "ZZXX")) {
				// ---------------------资质信息---------------------
				DynamicObjectCollection entry_zzxx = supplieObj.getDynamicObjectCollection("spic_entryzzxx");
//				if (isUpdate) {
					entry_zzxx.clear();
//				}
				List<Element> valuelist = specialitycode.elements("VALUELIST");
				for (Element value : valuelist) {
					DynamicObject entry_zzxxRow = entry_zzxx.addNew();
					List<Element> elements = value.elements("PROPERTYCODE");
					for (Element element : elements) {
						String attr = element.attribute("PROPERTYCODE").getText();
						String val = element.getText();
						// spic_zzname
						if (StringUtils.equals("ZZMC", attr)) {
							entry_zzxxRow.set("spic_zzname", val);
							continue;
						}
						// spic_zzexplain
						if (StringUtils.equals("ZZSM", attr)) {
							entry_zzxxRow.set("spic_zzexplain", val);
							continue;
						}
						// spic_zzexpiredate
						if (StringUtils.equals("ZZYXQ", attr) && StringUtils.isNotBlank(val)) {
							entry_zzxxRow.set("spic_zzexpiredate", dateFormat.parse(val));
							continue;
						}
					}
				}
			} else if (StringUtils.equals(specialitycodeAttr, "LXRXX")) {
				// ---------------------联系人---------------------
				DynamicObjectCollection entry_linkman = supplieObj.getDynamicObjectCollection("entry_linkman");
//				if (isUpdate) {
					entry_linkman.clear();
//				}
				List<Element> valuelist = specialitycode.elements("VALUELIST");
				boolean isDefault = true;
				for (Element value : valuelist) {
					DynamicObject entry_linkmanRow = entry_linkman.addNew();
					// 需要设置一个默认联系人，取第一条记录设置为默认
					if (isDefault) {
						entry_linkmanRow.set("isdefault_linkman", true);
						isDefault = false;
					}
					List<Element> elements = value.elements("PROPERTYCODE");
					for (Element element : elements) {
						String attr = element.attribute("PROPERTYCODE").getText();
						String val = element.getText();
						if (StringUtils.equals(attr, "LXR")) {
							// 联系人 contactperson
							MuliLangTextProp contactperson = new MuliLangTextProp();
							MuliLangTextPropSetter contactpersonSetter = new MuliLangTextPropSetter(contactperson);
							contactpersonSetter.setObjValue(entry_linkmanRow, "contactperson", val);
							continue;
						}
						if (StringUtils.equals(attr, "LXRDH")) {
							// 联系电话 phone
							entry_linkmanRow.set("phone", val);
							continue;
						}
						if (StringUtils.equals(attr, "LXREMAIL")) {
							// 联系邮箱 email
							entry_linkmanRow.set("email", val);
							continue;
						}
					}
				}
			} else if (StringUtils.equals(specialitycodeAttr, "YHXX")) {
				// ---------------------银行信息---------------------
				DynamicObjectCollection entry_bankEntry = supplieObj.getDynamicObjectCollection("entry_bank");
				if (isUpdate) {
				entry_bankEntry.clear();
				}
				boolean isDefault = true;
				List<Element> valuelist = specialitycode.elements("VALUELIST");
				for (Element value : valuelist) {
					DynamicObject entry_bankRow = entry_bankEntry.addNew();
					// 需要设置一个默认银行信息，取第一条记录设置为默认
					if (isDefault) {
						entry_bankRow.set("isdefault_bank", true);
						isDefault = false;
					}
					List<Element> elements = value.elements("PROPERTYCODE");
					for (Element element : elements) {
						String attr = element.attribute("PROPERTYCODE").getText();
						String val = element.getText();
						// 账户名称
						if (StringUtils.equals("ZHMC", attr)) {
							MuliLangTextProp accountname = new MuliLangTextProp();
							MuliLangTextPropSetter accountnameSetter = new MuliLangTextPropSetter(accountname);
							accountnameSetter.setObjValue(entry_bankRow, "accountname", val);
							continue;
						}
						// 银行账户 bankaccount
						if (StringUtils.equals("YHZH", attr)) {
							entry_bankRow.set("bankaccount", val);
							continue;
						}
						// 银行名称 spic_bankname
						if (StringUtils.equals("YHMC", attr)) {
							entry_bankRow.set("spic_bankname", val);
							continue;
						}
						// 省、市/县 spic_pccregion
						if (StringUtils.equals("SSX", attr)) {
							entry_bankRow.set("spic_pccregion", val);
							continue;
						}
						// 开户银行名称 bank
						if (StringUtils.equals("KHYX", attr)) {
							DynamicObject bank = BusinessDataServiceHelper.loadSingle("bd_bebank", "",
									new QFilter[] { new QFilter("name", QCP.equals, val) });
							entry_bankRow.set("bank", bank);
							continue;
						}
						// 大额支付行号
						if (StringUtils.equals("DEZFHH", attr)) {
							entry_bankRow.set("spic_bigbankaccount", val);
							continue;
						}
						// 银行所属国家 spic_bankcountry
						if (StringUtils.equals("YHSSGJ", attr)) {
							entry_bankRow.set("spic_bankcountry", val);
							continue;
						}
						// 银行账户终止日期(格式：YYYY-MM-DD) spic_bankaccexpiredate
						if (StringUtils.equals("YHZHYXQ", attr) && StringUtils.isNotBlank(val)) {
							entry_bankRow.set("spic_bankaccexpiredate", dateFormat.parse(val));
							continue;
						}
					}
				}
			}
		}
		// ---------------------分类标准---------------------
		// 设置分录
		DynamicObjectCollection groupstandardEntry = supplieObj.getDynamicObjectCollection("entry_groupstandard");
//		if (isUpdate) {
			groupstandardEntry.clear();
//		}
		// 设置分类标准
		Set<Long> groupStandard = GroupStandardUtils.getGroupStandard("bd_supplier",
				Long.valueOf(adminorg.getPkValue().toString()), Boolean.TRUE, (QFilter) null);
		for (Long standardId : groupStandard) {
			DynamicObject entryRow = groupstandardEntry.addNew();
			DynamicObject standardObj = BusinessDataServiceHelper.loadSingle(standardId, "bd_suppliergroupstandard");
			entryRow.set("standardid", standardObj);
			if (StringUtils.equals(standardObj.get("number").toString(), "JBFLBZ")) {
				if (StringUtils.equals("是", DESC31)) {
					DynamicObject isInternal = BusinessDataServiceHelper.loadSingle("bd_suppliergroup", "", new QFilter[]{new QFilter("number", QCP.equals, "001"), new QFilter("standard.id", QCP.equals, standardId)});
					entryRow.set("groupid", isInternal);
				} else {
					DynamicObject isNotInternal = BusinessDataServiceHelper.loadSingle("bd_suppliergroup", "", new QFilter[]{new QFilter("number", QCP.equals, "002"), new QFilter("standard.id", QCP.equals, standardId)});
					entryRow.set("groupid", isNotInternal);
				}
			}
		}

		// ---------------------其他信息---------------------
		// 控制策略
		supplieObj.set("ctrlstrategy", "5"); // 1. 逐级分配；2. 自由分配；5. 全局共享；6. 管控范围内共享；7. 私有
		// 使用状态
		if (StringUtils.equals("正常", DESC32)) {
			supplieObj.set("enable", "1"); // 0. 禁用；1. 可用
		} else if (StringUtils.equals("停用", DESC32)) {
			supplieObj.set("enable", "0"); // 0. 禁用；1. 可用
		}
		// 数据状态
		supplieObj.set("status", "A"); // A. 暂存；B. 已提交；C. 已审核
	}
}
