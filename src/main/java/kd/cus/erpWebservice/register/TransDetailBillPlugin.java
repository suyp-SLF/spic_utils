package kd.cus.erpWebservice.register;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import kd.bos.bill.IBillWebApiPlugin;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.api.ApiResult;
import kd.bos.entity.operate.result.IOperateInfo;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.service.business.datamodel.DynamicFormModelProxy;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.MetadataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.cus.api.SelectUtils;
import kd.cus.api.entity.FilterEntity;
import kd.bos.util.HttpClientUtils;;

/**
 * 交易明细查询
 * @author dhf
 */
public class TransDetailBillPlugin implements IBillWebApiPlugin {
	
	@Override
	public ApiResult doCustomService(Map<String, Object> params) {
		String strURL = "http://localhost:8081/test";
		String jsonStr = null;
		Map map =new HashMap();
		map.put("accountNo", "123456789");
		map.put("startDate", "2020-08-24");
		map.put("endDate", "2020-09-24");
		List<Map> list = new ArrayList<>();
		JSONObject jsonObject = doExcute(strURL, jsonStr, map, list);
		int rowCount = ((Integer)jsonObject.get("rowCount")).intValue();//数据总条数
		int pageSize = ((Integer)jsonObject.get("pageSize")).intValue();//分页大小
		int currentPage = ((Integer)jsonObject.get("currentPage")).intValue();//当前页
		int chu = rowCount / pageSize;	//一共几页
		int yu = rowCount % pageSize;//一共几页
		if (yu != 0) {//一页以内
			chu += 1;
		}
		map.put("pageSize", pageSize);
		for (int i = 0 ; i < chu - 1 ; i++) {
			currentPage += 1;
			map.put("currentPage", currentPage);
			doExcute(strURL, jsonStr, map, list);
		}
		excute(list);
		ApiResult apiResult = new ApiResult();
		apiResult.setSuccess(true);
		apiResult.setErrorCode("success");
		apiResult.setMessage("HelloWorld Success");
		apiResult.setData(null);
		return apiResult;
	}


	private JSONObject doExcute(String strURL, String jsonStr, Map map, List<Map> list) {
//		Map<String, String> header = new HashMap<>();
//		Map<String, Object> body = new HashMap<>();
				try {
			jsonStr = call(strURL, map);
		} catch (Exception e) {
			e.printStackTrace();
		} 
//		HttpClientUtils.post(strURL, header, body);
		JSONObject jsonObject = JSON.parseObject(jsonStr);
		List<Map> dataList = (List<Map>)jsonObject.get("dataList");
		list.addAll(dataList);
		return jsonObject;
	}
	
	private void excute(List<Map> list) {
		List<DynamicObject> dataEntities = new ArrayList<>();
		List billnoList = new ArrayList<>();
		List existBillnoList = new ArrayList<>();
		List<FilterEntity> filterEntities = new ArrayList<>();// 传入数据
		Map<String, DynamicObject> map1 = new HashMap<>();// 放基础资料返回结果
		list.forEach(data -> {
			Object billno = data.get("ticketNumber");
			billnoList.add(billno);
			Object companyNumber = data.get("cltNo");//单位编码
			filterEntities.add(new FilterEntity("bos_adminorg", "number", (String)companyNumber));// 资产组织
			Object currencyNo = data.get("currencyNo");
			int currencyNumber = 0;
			if (currencyNo != null) {
				currencyNumber = ((Integer)currencyNo).intValue();//币种
			}
			filterEntities.add(new FilterEntity("bd_currency", "number", currencyNumber + ""));// 币别
			Object accountNo = data.get("accountNo");
			int accountbankBankaccountnumber = 0;
			if (accountNo != null) {
				accountbankBankaccountnumber = ((Integer)accountNo).intValue();//币种
			}
			filterEntities.add(new FilterEntity("am_accountbank", "bankaccountnumber", accountbankBankaccountnumber + ""));// 银行账户
		});
		map1 = SelectUtils.loadAll(map1, filterEntities);
		//查询苍穹上已经已经存在的交易明细
		DynamicObjectCollection dynamicObjectCollection = QueryServiceHelper.query("bei_transdetail", "billno",
				new QFilter[] { new QFilter("billno", QCP.in, billnoList) });
		if (null != dynamicObjectCollection) {//有已经存在的交易明细
			for (DynamicObject dynamicObject : dynamicObjectCollection) {
				existBillnoList.add(dynamicObject.get("billno"));//把已经存在的交易明细的billno放入存在的billno列表中
			}
		}
		
		for (Map map : list) {
			Object billno = map.get("ticketNumber");//单据号
			if (existBillnoList.contains(billno)) {
				continue;
			} 
			Object companyNumber = map.get("cltNo");//单位编码
			Object recordId = map.get("recordId");//明细id
			Object bizdate = map.get("recordDate");//明细日期
			Object currencyNo = map.get("currencyNo");
			int currencyNumber = 0;
			if (currencyNo != null) {
				currencyNumber = ((Integer)currencyNo).intValue();//币种
			}
			Object accountNo = map.get("accountNo");
			int accountbankBankaccountnumber = 0;
			if (accountNo != null) {
				accountbankBankaccountnumber = ((Integer)accountNo).intValue();//币种
			}
			Object description = map.get("explain");//摘要
			String balanceDir = (String)map.get("balanceDir");//收入/支出
			Object amount = map.get("amount");//明细金额
			Object debitamount = null;
			Object creditamount = null;
			if ("1".equals(balanceDir)) {//支出
				debitamount = amount;//付款金额
				creditamount = 0;//收款金额
			} else if ("2".equals(balanceDir)) {//收入
				creditamount = amount;
				debitamount = 0;
			}
			Object transbalance = map.get("balance");//余额
			Object oppunit = map.get("opAccountName");//对方账户名
			Object oppbanknumber = map.get("opAccountNo");//对方账户
			Object oppbank = map.get("opBankName");//对方银行
			Object detailid = map.get("hostId");//银行流水号
			Map<Class<?>, Object> services = new HashMap<>();
			DynamicFormModelProxy model = new DynamicFormModelProxy("bei_transdetail", UUID.randomUUID().toString(),
					services);
			model.createNewData();
			DynamicObject transDetail = model.getDataEntity();//创建一个赋完默认值的对象

			DynamicObject company = map1.get("bos_adminorg" + "@_@" + "number" + "@_@" + companyNumber);
			transDetail.set("company", company);//资金组织
			transDetail.set("billno", billno);//票据号
			try {
				SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
				transDetail.set("bizdate", sdf1.parse((String)bizdate));//交易日期
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			DynamicObject currency = map1.get("bd_currency" + "@_@" + "number" + "@_@" + currencyNumber);
			transDetail.set("currency", currency);//币别
			
			DynamicObject accountbank = map1.get("am_accountbank" + "@_@" + "bankaccountnumber" + "@_@" + accountbankBankaccountnumber);
			transDetail.set("accountbank", accountbank);//银行账户
			transDetail.set("description", description);//摘要
			transDetail.set("debitamount", debitamount);//付款金额
			transDetail.set("creditamount", creditamount);//收款金额
			transDetail.set("transbalance", transbalance);//余额
			transDetail.set("oppunit", oppunit);//对方户名
			transDetail.set("oppbanknumber", oppbanknumber);//对方账号
			transDetail.set("oppbank", oppbank);//对方开户行
			transDetail.set("detailid", detailid);//交易流水号
			
			transDetail.set("spic_mxid", recordId);//明细ID
			dataEntities.add(transDetail);
		}
		OperationResult operationResult = OperationServiceHelper.executeOperate("save", "bei_transdetail",
				dataEntities.toArray(new DynamicObject[dataEntities.size()]), OperateOption.create());
		List<IOperateInfo> OperateInfos = operationResult.getAllErrorOrValidateInfo();
		
	}
	
	
	private String call(String strURL, Map map) throws Exception {
		URL url = new URL(strURL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(3000);
		conn.setReadTimeout(3000);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		OutputStream os = conn.getOutputStream();
		String jsonString = JSON.toJSONString(map);
		os.write(jsonString.getBytes());
		InputStream is = conn.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		StringBuffer sb = new StringBuffer();
		String line = null;
		while((line=br.readLine())!=null) {
			sb.append(line);
			sb.append("\r\n");
		}
		if (null != br) {
			br.close();
		}
		if (null != is) {
			is.close();
		}
		if (null != os) {
			os.close();
		}
		conn.disconnect();
		return sb.toString();
	}
}
