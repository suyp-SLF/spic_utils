//package kd.cus.erpWebservice.register;
//
//import java.math.BigDecimal;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.stream.Collectors;
//import java.util.Map.Entry;
//
//import org.dom4j.Element;
//
//import kd.bd.master.MaterialSaveApiService;
//import kd.bos.dataentity.entity.DynamicObject;
//import kd.bos.dataentity.entity.DynamicObjectCollection;
//import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
//import kd.bos.dataentity.utils.StringUtils;
//import kd.bos.entity.EntityMetadataCache;
//import kd.bos.entity.api.ApiResult;
//import kd.bos.entity.operate.result.OperationResult;
//import kd.bos.entity.property.EntryProp;
//import kd.bos.orm.query.QCP;
//import kd.bos.orm.query.QFilter;
//import kd.bos.service.business.datamodel.DynamicFormModelProxy;
//import kd.bos.servicehelper.BusinessDataServiceHelper;
//import kd.bos.servicehelper.basedata.BaseDataServiceHelper;
//import kd.cus.erpWebservice.action.WebserviceMainTemplate;
//import kd.cus.erpWebservice.action.entity.ResultEntity;
//import kd.fi.cas.helper.OperateServiceHelper;
//import kd.fi.fr.utils.AccountBookUtil;
//
///***
// * 通用转账申请单，承接远光税务系统推送的凭证
// * @author 宋瑞平
// *
// */
//public class Fr_ManualtallyBill implements WebserviceMainTemplate {
//
//	@Override
//	public List<ResultEntity> disposeDate(List<Element> children, String p_source) throws Exception {
//		// TODO Auto-generated method stub
//
//		List<ResultEntity> resultEnties = new ArrayList<ResultEntity>();// 最终给方法的返回结果
//
//		List<String> listOr = new ArrayList<>();// 没有找到创建组织
//
//		for (Element dataInfo : children) {
//
//			String otsysbillno = dataInfo.element("SPIC_OTSYSBILLNO").getText();
//			List<Object> listData = new ArrayList<>();
//
//			Map<Class<?>, Object> services = new HashMap<>();
//     		DynamicFormModelProxy model = new DynamicFormModelProxy("fr_manualtallybill", UUID.randomUUID().toString(), services);
//     		Object one = model.createNewData();
//     		DynamicObject newBill = model.getDataEntity();
//
//			convert(dataInfo, newBill);
//
//			OperationResult result = OperateServiceHelper.executeOperate("save", "fr_manualtallybill", new DynamicObject[] {newBill}, null);
//
//
//			if (result.isSuccess()) {
//					resultEnties.add(ResultEntity.SUCCESS().setInitDate(otsysbillno, "", "税务凭证"));
//			} else {
//			// 返回错误结果
//				resultEnties.add(ResultEntity.PROCESS_ERROR(result.getMessage()).setInitDate(otsysbillno, "", "税务凭证"));
//			}
//		}
//		return resultEnties;
//	}
//
//	private void convert(Element dataInfo, DynamicObject dataentity) throws Exception {
//		// TODO Auto-generated method stub
//		//组织
//		String orgnumber = dataInfo.element("ORG").getText();
//		DynamicObject org = null;
//		if(StringUtils.isNotEmpty(orgnumber)) {
//			org = BusinessDataServiceHelper.loadSingle("bos_org", "", new QFilter[] {new QFilter("number", QCP.equals, orgnumber)});
//			dataentity.set("tallycompany", org);
//		}
//		//账簿类型
//		String accountBookNum = dataInfo.element("ACCOUNTBOOK").getText();
//		if(StringUtils.isNotEmpty(accountBookNum)) {
//			DynamicObject accountBook = BusinessDataServiceHelper.loadSingle("bd_accountbookstype", "", new QFilter[] {new QFilter("number", QCP.equals, accountBookNum)});
//			dataentity.set("accountbook", accountBook);
//		}else {
//			DynamicObject defaultAccountBook = BusinessDataServiceHelper.loadSingle("bd_accountbookstype", "", new QFilter[] {new QFilter("number", QCP.equals, "100001")});
//			dataentity.set("accountbook", defaultAccountBook);
//		}
//
//		//凭证类型vouchertype
//		if(null != org) {
//			Element vouchertypeEle = dataInfo.element("VOUCHERTYPE");
//			String vouchertypeNo =  null != vouchertypeEle && StringUtils.isNotEmpty(vouchertypeEle.getText()) ? vouchertypeEle.getText() : "01";
//			DynamicObject vouchertype = BusinessDataServiceHelper.loadSingle("gl_vouchertype", "", new QFilter[] {BaseDataServiceHelper.getBaseDataFilter("gl_vouchertype", org.getLong("id")),
//					   																						new QFilter("number", QCP.equals, vouchertypeNo)});
//			dataentity.set("vouchertype", vouchertype);
//		}
//
//		//本位币
//		String localcurNum = dataInfo.element("LOCALCUR").getText();
//		DynamicObject localcur = null;
//		if(StringUtils.isNotEmpty(localcurNum)) {
//			localcur = BusinessDataServiceHelper.loadSingle("bd_currency", "", new QFilter[] {new QFilter("number", QCP.equals, localcurNum)});
//			dataentity.set("currency", localcur);
//		}
//		//是否多币别
//		dataentity.set("ismulticurrency", dataInfo.element("ISMULTICURRENCY").getText());
//		//事由
//		dataentity.set("description", dataInfo.element("DESCRIPTION").getText());
//		//摘要
//		dataentity.set("abstract", dataInfo.element("DESCRIPTION").getText());
//		//集成系统标识
//		dataentity.set("spic_othersys", dataInfo.element("SPIC_OTHERSYS").getText());
//		//集成系统凭证号
//		dataentity.set("spic_otsysbillno", dataInfo.element("SPIC_OTSYSBILLNO").getText());
//		//时间
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		String bizdateString = dataInfo.element("BIZDATE").getText();
//		dataentity.set("bizdate", sdf.parse(bizdateString));
//		String bookdateString = dataInfo.element("BOOKDATE").getText();
//		dataentity.set("bookdate", sdf.parse(bookdateString));
////		dataentity.set("", new );
//		List<Element> entryInfoList = dataInfo.element("PAYENTRYS").elements("PAYENTRY");
//		//会计期间
//		DynamicObject tallyCompany = dataentity.getDynamicObject("tallycompany");
//		DynamicObject accountBook = dataentity.getDynamicObject("accountbook");
//		Map<String, Long> priodMap = AccountBookUtil.getPriodWithBook(Long.valueOf(tallyCompany.getLong("id")), Long.valueOf(accountBook.getLong("id")));
//		Date dateValue = dataentity.getDate("bookdate");
//		if ((dateValue != null) && (!priodMap.isEmpty()))      {
//		  DynamicObject period = null;
//		  period = this.getPeriodByDate(dateValue, "id", ((Long)priodMap.get("periodType")).longValue());
//		  dataentity.set("period", period);
//		}
//
//		//记账明细
//		EntryProp prop = (EntryProp) EntityMetadataCache.getDataEntityType(dataentity.getDataEntityType().getName()).findProperty("tallyentryentity");
//		DynamicObjectType dt = prop.getDynamicCollectionItemPropertyType();
//		//核算维度分录
//		EntryProp asstactentryProp = (EntryProp) EntityMetadataCache.getDataEntityType(dataentity.getDataEntityType().getName()).findProperty("asstactentry");
//		DynamicObjectType asstDt = asstactentryProp.getDynamicCollectionItemPropertyType();
//
//		DynamicObjectCollection entryCol = new DynamicObjectCollection();
//
//		Map<String,String> assgrpInfoMap = new HashMap<String,String>();
//		//集成字段标识和核算维度映射
//		assgrpInfoMap.put("业务板块","YWBK");
//
//		for (Element entryInfo : entryInfoList) {
//
//			DynamicObject entryrow = new DynamicObject(dt);
//			//科目
//			String accountNum = entryInfo.element("ACCOUNT").getText();
//			DynamicObject account = null;
//			if(StringUtils.isNotEmpty(accountNum) && org != null) {
//				account = BusinessDataServiceHelper.loadSingle("bd_accountview", "checkitementry,checkitementry.asstactitem,checkitementry.asstactitem,checkitementry.isrequire",
//								new QFilter[] {BaseDataServiceHelper.getBaseDataFilter("bd_accountview", org.getLong("id")),
//											   new QFilter("number", QCP.equals, accountNum)});
////				DynamicObjectCollection accountCol = BaseDataServiceHelper.queryBaseData("bd_accountview", org.getLong("id"), new QFilter("number", QCP.equals, accountNum), "checkitementry,checkitementry.asstactitem,checkitementry.asstactitem,checkitementry.isrequire");
////				account = accountCol.size()>0 ? accountCol.get(0) : null;
//				entryrow.set("account", account);
//			}
//
//			//核算维度
//			if(account != null) {
//
//				//核算维度子分录
//				DynamicObjectCollection asstactentryCol = new DynamicObjectCollection();
//				//科目对应核算维度分录
//				DynamicObjectCollection checkitementry = account.getDynamicObjectCollection("checkitementry");
//
//
//				for (DynamicObject checkitementryRow : checkitementry) {
//					DynamicObject asstEntryrow = new DynamicObject(asstDt);
//					asstEntryrow.set("fieldname", checkitementryRow.get("asstactitem"));
//					asstEntryrow.set("isrequire", checkitementryRow.get("isrequire"));
//					//核算维度集成字段标识
//					String asstField = assgrpInfoMap.get(checkitementryRow.getDynamicObject("asstactitem").getString("name"));
//					//核算维度值编码
//					String asstValuenumber = (StringUtils.isEmpty(asstField) || entryInfo.element(asstField) == null) ? null : entryInfo.element(asstField).getText();
//					//获取核算维度值
//					DynamicObject asstacttype = BusinessDataServiceHelper.loadSingleFromCache(checkitementryRow.getDynamicObject("asstactitem").getPkValue(),"bd_asstacttype");
//					DynamicObject asstacttypeValue = getAsstacttypeValue(org.getLong("id"), asstValuenumber,  asstacttype);
//					asstEntryrow.set("value_id", asstacttypeValue == null ? null : (Long)asstacttypeValue.getPkValue());
//					asstactentryCol.add(asstEntryrow);
//				}
//				entryrow.set("asstactentry", asstactentryCol);
//				entryrow.set("assgrpdesc",this.getDesc(asstactentryCol));
//			}
//
//			//币别
//			String currencyNum = entryInfo.element("CURRENCY").getText();
//			if(StringUtils.isNotEmpty(currencyNum)) {
//
//				DynamicObject currency = StringUtils.equals(localcurNum, currencyNum) ? localcur:BusinessDataServiceHelper.loadSingle("bd_currency", "", new QFilter[] {new QFilter("number", QCP.equals, currencyNum)});
//				entryrow.set("cuscurrency", currency);
//			}
//			//汇率
//			String exrate = entryInfo.element("EXRATE").getText();
//			entryrow.set("exrate", new BigDecimal(exrate));
//
//			//借方金额
//			String tallyamount = entryInfo.element("TALLYAMOUNT").getText();
//			entryrow.set("tallyamount", new BigDecimal(tallyamount));
//
//			//借方金额(本位币)
//			String standardamount = entryInfo.element("STANDARDAMOUNT").getText();
//			entryrow.set("standardamount", new BigDecimal(standardamount));
//
//			//贷方金额
//			String loanamount = entryInfo.element("LOANAMOUNT").getText();
//			entryrow.set("loanamount", new BigDecimal(loanamount));
//
//			//贷方金额(本位币)
//			String loanstanamount = entryInfo.element("LOANSTANAMOUNT").getText();
//			entryrow.set("loanstanamount", new BigDecimal(loanstanamount));
//
//			//备注
//			entryrow.set("remark", entryInfo.element("REMARK").getText());
//
//			entryCol.add(entryrow);
//		}
//
//		dataentity.set("tallyentryentity", entryCol);
//
//
//
//	}
//
//	private DynamicObject getAsstacttypeValue(Long orgId, String number,DynamicObject asstacttype) {
//		if(asstacttype != null && !StringUtils.isEmpty(number)) {
//
//			String valuetype = asstacttype.getString("valuetype");
//			if(StringUtils.equals("2", valuetype)) {
//				//辅助资料分类
//				DynamicObject assistanttype = asstacttype.getDynamicObject("assistanttype");
//				if(assistanttype != null) {
//
//					return BusinessDataServiceHelper.loadSingleFromCache("bos_assistantdata_detail", new QFilter[] {
//							new QFilter("group.id", QCP.equals, assistanttype.getPkValue()),
//							new QFilter("number", QCP.equals, number)
//						});
//				}
//			}else if(StringUtils.equals("1", valuetype)) {
//				//基础资料对象
//				DynamicObject valuesource = asstacttype.getDynamicObject("valuesource");
//				if(valuesource != null) {
//					DynamicObjectCollection dcol = BaseDataServiceHelper.queryBaseData(valuesource.getString("name"), orgId, new QFilter("number", QCP.equals, number), "id");
//					if(dcol.size()>0) {
//						return dcol.get(0);
//					}
//				}
//
//			}
//
//		}
//		return null;
//	}
//
//	public static String getDesc(DynamicObjectCollection entries) {
//       StringBuilder sb = new StringBuilder();
//       Iterator var2 = entries.iterator();
//
//       while(var2.hasNext()) {
//          DynamicObject row = (DynamicObject)var2.next();
//          if (sb.length() > 50) {
//             break;
//          }
//
//          String name = row.getString("fieldname.name");
//          sb.append(name);
//          sb.append('：');
//          String value = "";
//          String type = row.getString("fieldname.valuetype");
//          String entityId;
//          if ("3".equals(type)) {
//             entityId = row.getString("txtval");
//             if (entityId != null) {
//                entityId = entityId.replaceAll(";", "，");
//                value = entityId;
//             }
//          } else {
//             entityId = null;
//             if ("1".equals(type)) {
//                entityId = row.getString("fieldname.valuesource.id");
//             } else if ("2".equals(type)) {
//                entityId = "bos_assistantdata_detail";
//             }
//
//             if (StringUtils.isNotBlank(entityId)) {
//                List<Long> ids = new ArrayList(2);
//                ids.add(row.getLong("value_id"));
//                if (!ids.isEmpty()) {
//                   Map<Object, DynamicObject> valsFormDb = BusinessDataServiceHelper.loadFromCache(entityId, "name", (new QFilter("id", "in", ids)).toArray());
//                   List<String> vals = (List)valsFormDb.values().stream().map((x) -> {
//                      return x.getString("name");
//                   }).collect(Collectors.toList());
//                   if (!vals.isEmpty()) {
//                      value = String.join("，", vals);
//                   }
//                }
//             }
//          }
//
//          sb.append(value);
//          sb.append('；');
//       }
//
//       String substring = sb.substring(0, sb.length() > 50 ? 50 : sb.length());
//       if (substring.endsWith("；")) {
//          substring = substring.substring(0, substring.length() - 1);
//       }
//
//       return substring;
//    }
//
//	private DynamicObject getPeriodByDate(Date date, String selectField, long periodType)
//	{
//	  QFilter f = new QFilter("beginDate", "<=", date);
//	  QFilter g = new QFilter("enddate", ">=", date);
//	  QFilter t = new QFilter("periodtype", "=", Long.valueOf(periodType));
//	  DynamicObject period = BusinessDataServiceHelper.loadSingleFromCache("bd_period", selectField, new QFilter[] { f, g, t });
//	  return period;
//	}
//
//
//
//}
