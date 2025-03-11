//package kd.cus.erpWebservice.register;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import org.dom4j.Element;
//
//import kd.bd.master.MaterialSaveApiService;
//import kd.bd.master.vo.OperationApiVo;
//import kd.bos.dataentity.entity.DynamicObject;
//import kd.bos.entity.api.ApiResult;
//import kd.bos.orm.query.QCP;
//import kd.bos.orm.query.QFilter;
//import kd.bos.servicehelper.BusinessDataServiceHelper;
//import kd.cus.api.SelectUtils;
//import kd.cus.api.entity.FilterEntity;
//import kd.cus.erpWebservice.action.WebserviceMainTemplate;
//import kd.cus.erpWebservice.action.entity.ResultEntity;
//
///**
// * 物料 2020.9.9
// *
// * @author ZXR
// *
// */
//public class Material implements WebserviceMainTemplate {
//
//	@Override
//	public List<ResultEntity> disposeDate(List<Element> children, String p_source) throws Exception {
//
//		List<ResultEntity> resultEnties = new ArrayList<ResultEntity>();// 最终给方法的返回结果
//		Map<String, Object> maps = new HashMap<String, Object>();// 保存方法
//		List<FilterEntity> filterEntities = new ArrayList<>();// 传入数据
//		Map<String, DynamicObject> map = new HashMap<>();// 放基础资料返回结果
//		List<Object> listDataInsert = new ArrayList<>();// 新增数据集合
//		List<Object> listDataUpdata = new ArrayList<>();// 更新数据集合
//		Map<String, String> codeidAndNumber = new HashMap<>();// number和codeid一一对应，用于返回结果
//
//		for (Element dataInfo : children) {
//
//			// codeid
//			if (null != dataInfo.element("codeid").getText()) {
//				filterEntities
//						.add(new FilterEntity("bd_material", "spic_sourceid", dataInfo.element("codeid").getText()));// 物料
//			}
//			filterEntities.add(new FilterEntity("bos_org", "number", dataInfo.element("ORGCODE").getText()));// 创建组织
//			filterEntities.add(new FilterEntity("bd_measureunits", "name", dataInfo.element("DESC5").getText()));// 计量单位
//		}
//
//		map = SelectUtils.loadAll(map, filterEntities);
//
//		for (Element dataInfo : children) {
//
//			String CODE = dataInfo.element("CODE").getText(); // number编码
//			String CODEID = null;
//			if (!dataInfo.element("codeid").getText().isEmpty()) {
//				CODEID = dataInfo.element("codeid").getText(); // coedid
//			} else {
//				resultEnties.add(ResultEntity.PROCESS_ERROR("codeid不允许空").setInitDate("", "", ""));
//				continue;
//			}
//			doExecute(dataInfo, listDataInsert, listDataUpdata, p_source, map);
//			if (null == codeidAndNumber.get("CODE")) {
//				codeidAndNumber.put(CODE, CODEID);
//			}
//
//		}
//
//		// 新增
//		if (null != listDataInsert && listDataInsert.size() > 0) {
//			maps.put("data", listDataInsert);
//			// 返回结果集
//			saveResult(resultEnties, maps, codeidAndNumber);
//		}
//
//		// 更新
//		if (null != listDataUpdata && listDataUpdata.size() > 0) {
//			maps.put("data", listDataUpdata);
//			// 返回结果集
//			saveResult(resultEnties, maps, codeidAndNumber);
//		}
//		return resultEnties;
//	}
//
//	private void doExecute(Element dataInfo, List<Object> listDataInsert, List<Object> listDataUpdate, String p_source,
//			Map<String, DynamicObject> map) throws Exception {
//
//		Map<String, Object> mapUnit = new HashMap<String, Object>();// 放基本单元的数据
//
//		String ORGCODE = dataInfo.element("ORGCODE").getText();// 受用组织
//		String CODE = dataInfo.element("CODE").getText(); // number编码
//		String CODEID = dataInfo.element("codeid").getText(); // coedid
//		String DESC2 = dataInfo.element("DESC2").getText(); // 规格型号
//		String DESC5 = dataInfo.element("DESC5").getText(); // 基本单位
//		String CATEGORYCODE = dataInfo.element("CATEGORYCODE").getText();// 物料分类编码
//		String DESCLONG = dataInfo.element("DESCLONG").getText(); // 名称
//		StringBuffer stringBuffer = new StringBuffer();// 存放返回信息
//		// ------------------------单据头-----------------------------------
//
//		Map<String, Object> mapOrg = new HashMap<String, Object>();// 放基本单元的数据
//		mapOrg.put("number", ORGCODE);// 适用组织
//		mapUnit.put("createorg", mapOrg);
//
//		mapUnit.put("number", CODE);// 编码
//		mapUnit.put("status", "C");// 状态 已审核
//		mapUnit.put("name", DESCLONG);// 名称
//		mapUnit.put("modelnum", DESC2);// 规格型号
//		mapUnit.put("spic_sourceid", CODEID);// 源编码
//		mapUnit.put("spic_source", p_source);// 来源
//
//		// ---------------------单据体- 基本信息---------------------
//		// 基本单位 基础资料
//		Map<String, Object> mapBaseunit = new HashMap<String, Object>();// 放基本单元的数据
//
//		// 基本单位
//		String measureunits = "";
//		DynamicObject bd_measureunits = map.get("bd_measureunits" + "@_@" + "name" + "@_@" + DESC5);
//		// 没加载到数据
//		if (null != bd_measureunits) {
//			measureunits = bd_measureunits.getString("id");
//		} else {
//			stringBuffer.append("&没找到计量单位!");
//		}
//		mapBaseunit.put("id", measureunits);// 设置id值
//		mapUnit.put("baseunit", mapBaseunit);// 基本单位
//
//		// 控制策略 默认 设置管控范围内共享
//		mapUnit.put("ctrlstrategy", "6");
//
//		// ------------------单据体-公共信息---------------------
//		// 物料类型 下拉列表 默认常规
//		mapUnit.put("materialtype", "1");
//
//		// ---------------------单据体-分类标准-分录 entry_groupstandard--------
//		List<Object> listGroup = new ArrayList<>();
//		Map<String, Object> mapGroup = new HashMap<String, Object>();// 放分组的数据
//
//		// 物料标准分类
//		Map<String, Object> mapStandardid = new HashMap<String, Object>();// 放分组的数据
//		mapStandardid.put("number", "JBFLBZ");
//		mapGroup.put("standardid", mapStandardid);
//
//		// 物料分类
//		Map<String, Object> mapGroupid = new HashMap<String, Object>();// 放分组的数据
//		mapGroupid.put("number", CATEGORYCODE);
//
//		mapGroup.put("groupid", mapGroupid);
//
//		listGroup.add(mapGroup);
//
//		mapUnit.put("entry_groupstandard", listGroup);
//		// --------------------------------------------------------------------
//
//		// 存在 true 更新， 不存在false 新增
//		DynamicObject materialId = map.get("bd_material" + "@_@" + "spic_sourceid" + "@_@" + CODEID);
//		// 传入id就更新逻辑， 未传递id就走新增逻辑
//		if (null != materialId) {
//			// 更新
//			mapUnit.put("id", materialId.getString("id"));
//			listDataUpdate.add(mapUnit);
//		} else {
//			// 新增
//			listDataInsert.add(mapUnit);
//		}
//
//	}
//
//	private List<ResultEntity> saveResult(List<ResultEntity> resultEnties, Map<String, Object> maps,
//			Map<String, String> codeidAndNumber) {
//		// 返回结果集
//		MaterialSaveApiService materialSaveApiService = new MaterialSaveApiService();
//		ApiResult re = materialSaveApiService.doCustomService(maps);
//
//		Map<String, Object> reData = (Map<String, Object>) re.getData();
//
//		// 遍历data map
//		for (Entry<String, Object> re1 : reData.entrySet()) {
//			OperationApiVo Mapss = (OperationApiVo) re1.getValue();
//			String Stringnumber = Mapss.getNumber();
//			String Stringnumberme = Mapss.getMessage();
//			if (Mapss.getIsSuccesss().equals("false")) {// 失败
//				resultEnties.add(ResultEntity.PROCESS_ERROR(Stringnumberme)
//						.setInitDate(codeidAndNumber.get(Stringnumber), "", "物料"));
//			}
//			if (Mapss.getIsSuccesss().equals("true")) {// 成功
//				resultEnties.add(ResultEntity.SUCCESS().setInitDate(codeidAndNumber.get(Stringnumber), "", "物料"));
//			}
//
//		}
//		return resultEnties;
//	}
//}
