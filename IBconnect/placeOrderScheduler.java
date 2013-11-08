/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package IBconnect;

import com.ib.client.Contract;
import com.ib.client.Order;
import java.awt.Frame;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Terence
 */

public class placeOrderScheduler implements Job {
    // for connecting to db
    
    String url;
    Connection con;
    Statement st;
    ResultSet openDownBarRS;
    double openDownBar;
    ResultSet openUpBarRS;
    double openUpBar;
    Statement lastTenStatement;
    ResultSet lastTenRS;
    double absTempBarSize;
    DateTime dt;
    
    //for placing order
    Double m_auxPrice;
    Integer m_orderId;
    String m_action;
    Long m_totalQuantity;
    String m_orderType;
    Boolean m_outsideRth;
    
    
            
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
         
         //get the instance of IBTradeGui using the getFrames app. the instance will be used to log trades.
         Frame myframes[] = java.awt.Frame.getFrames();
         IBTradeGui IBTInstance = (IBTradeGui) myframes[0];
         DateTime newdt = new DateTime();
        
         Logger logger = LoggerFactory.getLogger(placeOrderScheduler.class);
         
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
         IBTInstance.setTradeListText(newdt.toString("HH:mm:ss") + "Contract "+ contract.m_symbol + contract.m_expiry + "traded on " +contract.m_exchange);
         // initialize an order
         final Order order;
         order = new Order();
         
         //get the most recent opens of large up and down bars
        try {
            
            //determine aux price
            url = "jdbc:postgresql://localhost/IBDB";
            con = DriverManager.getConnection(url, "developer", "password");
            st = con.createStatement();
            openUpBarRS = st.executeQuery("WITH lastThreeHundred AS (SELECT open, barsize, _date FROM " + contract.m_symbol + contract.m_expiry + 
                    " ORDER BY _date DESC LIMIT 300) "
                    + "SELECT open FROM lastThreeHundred WHERE barsize > "
                    + "(SELECT abs(barsize) FROM " + contract.m_symbol + contract.m_expiry + " ORDER BY _date DESC LIMIT 1) ORDER BY _date DESC LIMIT 1"); 
            
            if (openUpBarRS.next()){
                openUpBar = openUpBarRS.getDouble("open");
            }
            else {
                    lastTenRS = st.executeQuery("WITH barsizes AS (SELECT barsize FROM " + 
                            contract.m_symbol + contract.m_expiry +" ORDER BY _date DESC LIMIT 10 OFFSET 1) SELECT open FROM " + contract.m_symbol + contract.m_expiry + 
                            " WHERE barsize = (SELECT max(barsize) FROM barsizes) ORDER BY _date DESC LIMIT 1");
                    if (lastTenRS.next()){
                        openUpBar = lastTenRS.getDouble("open");
                    }
            }

            openDownBarRS = st.executeQuery("WITH lastThreeHundred AS (SELECT open, barsize, _date FROM " + contract.m_symbol + contract.m_expiry + 
                    " ORDER BY _date DESC LIMIT 300) "
                    + "SELECT open FROM lastThreeHundred WHERE barsize < "
                    + "(SELECT abs(barsize)*-1 FROM " + contract.m_symbol + contract.m_expiry + " ORDER BY _date DESC LIMIT 1) ORDER BY _date DESC LIMIT 1"); 
            
            if (openDownBarRS.next()){
                openDownBar = openDownBarRS.getDouble("open");
            }
            else {
                    lastTenRS = st.executeQuery("WITH barsizes AS (SELECT barsize FROM " + 
                            contract.m_symbol + contract.m_expiry +" ORDER BY _date DESC LIMIT 10 OFFSET 1) SELECT open FROM " + contract.m_symbol + contract.m_expiry + 
                            " WHERE barsize = (SELECT min(barsize) FROM barsizes) ORDER BY _date DESC LIMIT 1");
                    if (lastTenRS.next()){
                        openDownBar = lastTenRS.getDouble("open");
                    }
            }
                        
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(placeOrderScheduler.class.getName()).log(Level.SEVERE, null, ex);
        } 
        finally {
                try {
                    if (openUpBarRS != null) {
                        openUpBarRS.close();
                    }
                    if (openDownBarRS != null) {
                        openDownBarRS.close();
                    }
                    if (lastTenRS != null) {
                        lastTenRS.close();
                    }
                    if (st != null) {
                        st.close();
                    }
                    if (con != null) {
                        con.close();
                    }
                    } catch (SQLException ex) {
                    java.util.logging.Logger.getLogger(placeOrderScheduler.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
         
         logger.info("Open of Up/Down Bar is " + openUpBar +" / " + openDownBar + "for " + contract.m_symbol + contract.m_expiry);
         IBTInstance.setTradeListText(newdt.toString("HH:mm:ss") + "Open of Up/Down Bar is " + openUpBar +" / " + openDownBar + "for " + contract.m_symbol + contract.m_expiry);
         //set order details
         order.m_outsideRth = true;
         if (someWrapper.currentPositions.containsKey(contract.m_symbol+contract.m_expiry)){
                if (someWrapper.currentPositions.get(contract.m_symbol+contract.m_expiry)>0){
                    order.m_action = "SELL";
                    order.m_orderType = "STP";
                    order.m_totalQuantity = 2;
                    //if long, special situation when open up bar > open downbar
                    if (openUpBar > openDownBar){
                        order.m_auxPrice = openDownBar;
                        logger.info("For " + contract.m_symbol + contract.m_expiry +": Current position is" + someWrapper.currentPositions.get(contract.m_symbol+contract.m_expiry)+ ", Position was LONG so sold on stop at " + order.m_auxPrice + " because of special siuation");
                        IBTInstance.setTradeListText(newdt.toString("HH:mm:ss") + "For " + contract.m_symbol + contract.m_expiry +": Position was LONG so sold on stop at " + order.m_auxPrice + " because of special siuation");
                    }
                    else {
                        order.m_auxPrice = openUpBar;
                        logger.info("For " + contract.m_symbol + contract.m_expiry +": Current position is" + someWrapper.currentPositions.get(contract.m_symbol+contract.m_expiry)+ ",Position was LONG so sold on stop at " + order.m_auxPrice);
                        IBTInstance.setTradeListText(newdt.toString("HH:mm:ss") + "For " + contract.m_symbol + contract.m_expiry +": Position was LONG so sold on stop at " + order.m_auxPrice);
                    }
                }
                else if (someWrapper.currentPositions.get(contract.m_symbol+contract.m_expiry)<0){
                    order.m_action = "BUY";
                    order.m_orderType = "STP";
                    order.m_totalQuantity = 2;
                    //if short, special situation when open up bar > open downbar
                    if (openUpBar > openDownBar){
                        order.m_auxPrice = openUpBar;
                        logger.info("For " + contract.m_symbol + contract.m_expiry +": Current position is" + someWrapper.currentPositions.get(contract.m_symbol+contract.m_expiry)+ ", Position was SHORT so bought on stop at " + order.m_auxPrice + " because of special siuation");
                        IBTInstance.setTradeListText(newdt.toString("HH:mm:ss") + "For " + contract.m_symbol + contract.m_expiry +": Position was SHORT so bought on stop at " + order.m_auxPrice + " because of special siuation");
                    }
                    else {
                        order.m_auxPrice = openDownBar;
                        logger.info("For " + contract.m_symbol + contract.m_expiry +": Position was SHORT so bought on stop at " + order.m_auxPrice);
                        IBTInstance.setTradeListText(newdt.toString("HH:mm:ss") + "For " + contract.m_symbol + contract.m_expiry +": Current position is" + someWrapper.currentPositions.get(contract.m_symbol+contract.m_expiry)+ ", Position was SHORT so bought on stop at " + order.m_auxPrice);
                    }
                }
                else {
                        // no current position thus go arbitrarily long.
                        order.m_action = "BUY";
                        order.m_orderType = "MKT";
                        order.m_totalQuantity = 1;
                        logger.info("For " + contract.m_symbol + contract.m_expiry +": Current position is" + someWrapper.currentPositions.get(contract.m_symbol+contract.m_expiry)+ ", No Position yet so buy at market");
                        IBTInstance.setTradeListText(newdt.toString("HH:mm:ss") + "For " + contract.m_symbol + contract.m_expiry +": No Position yet so buy at market");
                }
         }
         else {
                    order.m_action = "BUY";
                    order.m_orderType = "MKT";
                    order.m_totalQuantity = 1;
                    logger.info("For " + contract.m_symbol + contract.m_expiry +": No Position yet so buy at market");
                    IBTInstance.setTradeListText(newdt.toString("HH:mm:ss") + "For " + contract.m_symbol + contract.m_expiry +": No Position yet so buy at market");
         }
          
        IBTradeGui.idsForReqs+=1;
        IBTradeGui.connection.placeOrder(IBTradeGui.idsForReqs, contract, order);
    }
    
    
}