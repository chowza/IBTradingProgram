/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package IBconnect;

import com.ib.client.Contract;
import com.ib.client.Order;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author Terence
 */
public class closeOldOpenMoreActive implements Job{

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
         
         Logger logger = LoggerFactory.getLogger(closeOldOpenMoreActive.class);
         
         JobDataMap dataMap = context.getJobDetail().getJobDataMap();
         
         //set contract details
         final Contract contract;
         contract = new Contract();
         contract.m_symbol = dataMap.getString("m_symbol");
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
         logger.info("Contract "+ contract.m_symbol + contract.m_expiry + "traded on " +contract.m_exchange);
         
         // initialize an order
         final Order order;
         order = new Order();
         
         //iterate through the current positions to see if any of the current positions 
         // is trading on the same symbol but a different expiry. If that is the case we would want
         // to close out that contract and open one on the mostActiveContract
                    
        Iterator<Map.Entry<String,Integer>> it3;
            it3 = someWrapper.currentPositions.entrySet().iterator();
            while (it3.hasNext()){
                Map.Entry<String,Integer> entry3 = it3.next();

                // check if expiry is different
                if (contract.m_symbol.equals(entry3.getKey().substring(0,2)) ){
                        if(contract.m_expiry.equals(entry3.getKey().substring(2))){
                            //contracts have same symbol and same expiry, no need to close anything out.
                            logger.info("Contract.m_expiry is: "+ contract.m_expiry + "and entry3.getKey().substring(2) is: " + entry3.getKey().substring(2)
                                    +". The current position is the same as the most active contract. No Need to close anything out.");
                        }
                        else{
                            // there is a current position on the same product with a different expiry
                            logger.info("Contract.m_expiry is: "+ contract.m_expiry + "and entry3.getKey().substring(2) is: " + entry3.getKey().substring(2)
                                    +". The current position is different from the most active contract. Need to close something out if position !=0.");
                            if (entry3.getValue()>0){
                                //close out the current position
                                //open one of the moreActiveContract
                                contract.m_expiry = entry3.getKey().substring(2);
                                order.m_action = "SELL";
                                order.m_orderType = "MKT";
                                order.m_totalQuantity = 1;
                                logger.info("For " + contract.m_symbol + contract.m_expiry +": Current Position is " + entry3.getValue() + " Currently Long, Selling at MKT to go Long (at Mkt) on the more Active contract with expiry: " + dataMap.getString("m_expiry"));
                                IBTradeGui.idsForReqs+=1;
                                IBTradeGui.connection.placeOrder(IBTradeGui.idsForReqs, contract, order);
                                //set the contract expiry back
                                contract.m_expiry = dataMap.getString("m_expiry");
                                order.m_action = "BUY";
                                IBTradeGui.idsForReqs+=1;
                                IBTradeGui.connection.placeOrder(IBTradeGui.idsForReqs, contract, order);

                            }
                            else if (entry3.getValue()<0){
                                //close out the current position
                                //open one of the moreActiveContract
                                contract.m_expiry = entry3.getKey().substring(2);
                                order.m_action = "BUY";
                                order.m_orderType = "MKT";
                                order.m_totalQuantity = 1;
                                logger.info("For " + contract.m_symbol + contract.m_expiry +": Current Position is " + entry3.getValue() + " Currently Short, Buying at MKT to go Short (at MKT) on the more Active contract with expiry: " + dataMap.getString("m_expiry"));
                                IBTradeGui.idsForReqs+=1;
                                IBTradeGui.connection.placeOrder(IBTradeGui.idsForReqs, contract, order);
                                //set the contract expiry back
                                contract.m_expiry = dataMap.getString("m_expiry");
                                order.m_action = "SELL";
                                IBTradeGui.idsForReqs+=1;
                                IBTradeGui.connection.placeOrder(IBTradeGui.idsForReqs, contract, order);

                            }
                            else {
                                logger.info("For " + contract.m_symbol + contract.m_expiry +": Current Position is " + entry3.getValue() + " Currently 0 position, no need to do anything" );
                                // = 0 no position, no need to do anything
                            }
                    }
                }
            }
        
    }
    
}
