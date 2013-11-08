/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package IBconnect;

import com.ib.client.Contract;
import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobDataMap;
/**
 *
 * @author Terence
 */
public class updatePSQL implements Job{
    private DateTime dt;
    private Contract contract;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
            
         JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        
         contract = new Contract();
         contract.m_symbol =  dataMap.getString("m_symbol");
         contract.m_expiry = dataMap.getString("m_expiry");
         contract.m_currency = "USD";
         contract.m_secType = "FUT";
         switch(contract.m_symbol){
            case "ZC": case "ZW": case "ZO": case "ZL":case "ZN": case "ZB":
                contract.m_exchange = "ECBOT";
                break;
            case "HG": case "GC": case "SI": case "PA": case "PL": case "CL":
                case "HO": case "NG": 
                contract.m_exchange = "NYMEX";
                break;
            case "ES":  case "LE": case "GF": case "HE":
                contract.m_exchange = "GLOBEX";
                break;
        }
            
            IBTradeGui.idsForReqs+=1;
            dt = new DateTime();
            
            
            //requesting data inserts it through the somewrapper methods
            IBTradeGui.contractListDownloadedToPSQL.put(IBTradeGui.idsForReqs, contract);
            IBTradeGui.connection.reqHistoricalData(IBTradeGui.idsForReqs, contract, dt.toString(IBTradeGui.formatterForHistoricalData), 
                       "1 W","1 day", "TRADES", 0, 1);
    }
    
}
