package kd.cus.eip;

import java.util.HashMap;
import java.util.Map;

import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
import kd.cus.eip.service.VoucherDataService;


public class VoucherDataServiceImp implements VoucherDataService{
	
	private final Map<String,String> voucherMidTableName = new HashMap<String,String>(){{
		put("sw","spic_voucher_head0909");
	}};
	private final Map<String,String> voucherMidTableDBRoute = new HashMap<String,String>(){{
		put("sw","uniontest1");
	}};

	@Override
	public void doVoucherString(String data) throws Exception {
		// TODO Auto-generated method stub
		

		String sql = "UPDATE spic_voucher_head_i SET status =  'failed'  ";
		try (TXHandle h = TX.requiresNew()) {
			int a = DB.update(DBRoute.of("sys"), sql);
			System.out.println( a + " + " + sql);
            }
		System.out.println(data);
	}
	
	@Override
	//启动方案事件处理调用
	public void doVoucher(Map<String, Object> data) throws Exception {
		// TODO Auto-generated method stub
		//候选键
		Map source_selector = (Map) data.get("source_selector");
		//来源系统编码
		String sourcesys = (String) source_selector.get("sourcesys");
		//来源系统对应的中间表名
		String tableName = voucherMidTableName.get(sourcesys);
		//源数据主键
		String source_id = data.get("source_id").toString();
		//方案执行状态
		String status = data.get("action").toString().trim();
		
		String sql = " UPDATE " + tableName + " SET status =  '" + status + "' WHERE fid = '" + source_id + "'";
		try (TXHandle h = TX.requiresNew()) {
			int a = DB.update(DBRoute.of(voucherMidTableDBRoute.get(sourcesys)), sql);
			System.out.println( a + " + " + sql);
            }
		System.out.println(data);
	}

}
