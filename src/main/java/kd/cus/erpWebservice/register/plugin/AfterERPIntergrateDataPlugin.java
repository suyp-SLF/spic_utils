package kd.cus.erpWebservice.register.plugin;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.entity.datamodel.events.ImportDataEventArgs;
import kd.bos.mvc.bill.BillModel;

/**
 * @author Wu Yanqi
 */
public class AfterERPIntergrateDataPlugin extends AbstractBillPlugIn {
    @Override
    public void afterImportData(ImportDataEventArgs e) {

        Object model_init_by_webapi = ((BillModel) (e.getSource())).getContextVariable("MODEL_INIT_BY_WEBAPI");
        if (model_init_by_webapi != null && (Boolean)model_init_by_webapi) {
            Object premiumamt = e.getSourceData().get("premiumamt");
            if (premiumamt != null) {
                ((BillModel)(e.getSource())).setValue("premiumamt", premiumamt);
            }
        }
    }
}
