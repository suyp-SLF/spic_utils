package kd.cus.eip.service;

import java.util.Map;
public interface VoucherDataService {

	    void doVoucherString(String data) throws Exception;
	    
	    void doVoucher(Map<String,Object> data) throws Exception;
}
