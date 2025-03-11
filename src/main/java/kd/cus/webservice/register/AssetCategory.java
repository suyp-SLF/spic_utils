package kd.cus.webservice.register;

import java.util.*;
import java.util.stream.Collectors;

import akka.stream.impl.io.FilePublisher;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.operate.result.IOperateInfo;
import kd.bos.entity.operate.result.OperateErrorInfo;
import kd.bos.entity.tree.TreeNode;
import kd.bos.entity.validate.ValidateResult;
import kd.bos.servicehelper.MetadataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import org.dom4j.Element;


import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.validate.ValidateResultCollection;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.service.business.datamodel.DynamicFormModelProxy;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.cus.webservice.action.WebserviceMainTemplate;
import kd.cus.webservice.action.entity.ResultEntity;

/**
 * 资产类别
 * 
 * @author suyp
 *
 */
public class AssetCategory implements WebserviceMainTemplate {

	private final static String ASSETCATEGORY_LOGO = "fa_assetcategory";
	private final static String SOURCE_VALUE = "主数据";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * kd.cus.webservice.action.WebserviceMainTemplate#disposeDate(java.lang.String)
	 */
	@Override
	public List<ResultEntity> disposeDate(List<Element> children) throws Exception {
		// TODO Auto-generated method stub
		List<ResultEntity> resultEntitys = new ArrayList<>();
		return save(resultEntitys, children);
	}

	/**
	 * 
	 * @param resultEntitys
	 * @param children
	 * @return
	 */
	private List<ResultEntity> save(List<ResultEntity> resultEntitys, List<Element> children) throws Exception {
		//Log logger = LogFactory.getLog(AssetCategory.class);
		List<DynamicObject> dataEntities = new ArrayList<>();
		Map<String, String> unique = new HashMap<String, String>();
		for(Element child:children){
			String codeid = null, version = null, code = null, name = null, parentCode = null;
			codeid = child.element("CODEID").getText();
			version = child.element("VERSION").getText();
			code = child.element("CODE").getText(); // 资产类别编码
			name = child.element("DESC1").getText();// 资产类别名称
            parentCode = child.element("PARENTCODE").getText();
			// child.element("DESC2").getText();//板块编码
			unique.put(codeid, version);
			QFilter[] qFilters = { new QFilter("id", QCP.equals, codeid) };
			DynamicObject dy_value = null;
			String properites = StringUtils.join(MetadataServiceHelper.getDataEntityType(ASSETCATEGORY_LOGO).getAllFields().entrySet().stream().map(Map.Entry::getKey).toArray(), ",");
			dy_value = BusinessDataServiceHelper.loadSingle(ASSETCATEGORY_LOGO,properites,qFilters);
			if (null == dy_value){
				Map<Class<?>, Object> services = new HashMap<>();
				DynamicFormModelProxy model = new DynamicFormModelProxy(ASSETCATEGORY_LOGO, UUID.randomUUID().toString(), services);
				model.createNewData();
				dy_value = model.getDataEntity();
			}
			DynamicObject parent_dy_value = null;
			if (StringUtils.isNotBlank(parentCode)){
				QFilter[] parentCodeFilters =  { new QFilter("number", QCP.equals, parentCode) };
				parent_dy_value = BusinessDataServiceHelper.loadSingle(ASSETCATEGORY_LOGO,properites,parentCodeFilters);
				if (parent_dy_value == null){
					QFilter[] undistributedCodeFilters =  { new QFilter("number", QCP.equals, "undistributed") };
					parent_dy_value = BusinessDataServiceHelper.loadSingle(ASSETCATEGORY_LOGO,properites,undistributedCodeFilters);
				}
			}
			parent_dy_value.set("isleaf",true);
			long id = OrgUnitServiceHelper.getRootOrgId();
			DynamicObject rootOrg = BusinessDataServiceHelper.loadSingle(id, "bos_adminorg");
			TreeNode treeNode = new TreeNode();
			treeNode.setLeaf(true);
			treeNode.setData(dy_value);
			dy_value.set("id", codeid);
//			dy_value.set("isleaf",true);
			dy_value.set("longnumber",null);
			dy_value.set("number", code);
			dy_value.set("name", name);
			dy_value.set("createorg",rootOrg);
			dy_value.set("org",rootOrg);
			dy_value.set("parent", parent_dy_value);
			dy_value.set("spic_parent_prestore",parentCode);
			dy_value.set("enable", "1");
			dy_value.set("status", "C");
			dataEntities.add(dy_value);
		}
		// 保存/更新到数据库
		OperationResult operationResult = OperationServiceHelper.executeOperate("save", ASSETCATEGORY_LOGO, dataEntities.toArray(new DynamicObject[dataEntities.size()]), OperateOption.create());
//		List<IOperateInfo> validataResults = operationResult.getAllErrorOrValidateInfo();
//		 = StringUtils.join(validataResults.stream().map(IOperateInfo::getMessage).toArray(), '|');，OperationResult op = SaveServiceHelper.saveOperate("save", ASSETCATEGORY_LOGO, dataEntities.toArray(new DynamicObject[dataEntities.size()]), OperateOption.create());
		Map<String,String> errorMsgs = new HashMap<>();
		operationResult.getAllErrorOrValidateInfo().forEach(iOperateInfo->{
			if (errorMsgs.size() == 0){
				errorMsgs.put(iOperateInfo.getPkValue().toString(),iOperateInfo.getMessage());
			}else{
				errorMsgs.put(iOperateInfo.getPkValue().toString(),errorMsgs.get(iOperateInfo.getPkValue().toString()) + iOperateInfo.getMessage());
			}

		});
		errorMsgs.entrySet().forEach(errorMsg->{
			resultEntitys.add(ResultEntity.PROCESS_ERROR(errorMsg.getValue()).setInitDate(errorMsg.getKey(),unique.get(errorMsg.getKey().toString()),null));
		});
		operationResult.getSuccessPkIds().forEach(successMsg->{
			resultEntitys.add(ResultEntity.SUCCESS().setInitDate(successMsg.toString(),unique.get(successMsg),null));
		});

		return resultEntitys;
	}

	/**判断一个数据是否存在于列表中
     *
     *
     */
    public static boolean isExist(List list, String str) {
        boolean exist = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(str)) {
                return true;
            }
        }
        return exist;
    }
}
