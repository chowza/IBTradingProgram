/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package IBconnect;
import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.UnderComp;
import com.ib.client.EWrapperMsgGenerator;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Terence
 */
        
public class someWrapper implements EWrapper {
    
    //declare pointers
    private IBTradeGui anIBTInstance;
    public static HashMap <String,Integer> currentPositions;
    
    public someWrapper() {
    }
    
    
    //set the pointers
    public void referenceIBTradeGui(IBTradeGui anIBTInstance){
        this.anIBTInstance = anIBTInstance;
        currentPositions = new HashMap();
    }
    
    public void connectionClosed(){
    };
    
    public void error( Exception e){
        Logger.getLogger(someWrapper.class.getName()).log(Level.SEVERE, null, e);
    };
    public void error( String str){
        Logger.getLogger(someWrapper.class.getName()).log(Level.SEVERE, null, str);
    };
    public void error(int id, int errorCode, String errorMsg){
        System.out.println(EWrapperMsgGenerator.error(id, errorCode, errorMsg));
    };
    
    public void tickPrice( int tickerId, int field, double price, int canAutoExecute){};
    public void tickSize( int tickerId, int field, int size){};
    public void tickOptionComputation( int tickerId, int field, double impliedVol,
    		double delta, double optPrice, double pvDividend,
    		double gamma, double vega, double theta, double undPrice){};
	public void tickGeneric(int tickerId, int tickType, double value){};
	public void tickString(int tickerId, int tickType, String value){};
	public void tickEFP(int tickerId, int tickType, double basisPoints,
			String formattedBasisPoints, double impliedFuture, int holdDays,
			String futureExpiry, double dividendImpact, double dividendsToExpiry){};
    public void orderStatus( int orderId, String status, int filled, int remaining,
            double avgFillPrice, int permId, int parentId, double lastFillPrice,
            int clientId, String whyHeld){
    };
    public void openOrder( int orderId, Contract contract, Order order, OrderState orderState){
    };
    public void openOrderEnd(){};
    public void updateAccountValue(String key, String value, String currency, String accountName){};
    public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue,
            double averageCost, double unrealizedPNL, double realizedPNL, String accountName){
        String contractexpiry;
        if (contract.m_symbol.equalsIgnoreCase("HO") || contract.m_symbol.equalsIgnoreCase("NG")) {
            if (!contract.m_expiry.substring(4,contract.m_expiry.length()-2).equals("12")){
                int exp = Integer.parseInt(contract.m_expiry.substring(4,contract.m_expiry.length()-2)) + 1;
                if (exp <9){
                    contractexpiry = contract.m_symbol + contract.m_expiry.substring(0,contract.m_expiry.length()-4)+"0"+exp;
                    currentPositions.put(contractexpiry, position);
                }
                else{
                    contractexpiry = contract.m_symbol + contract.m_expiry.substring(0,contract.m_expiry.length()-4)+exp;
                    currentPositions.put(contractexpiry, position);
                }
            }
            else {
                int expyear = Integer.parseInt(contract.m_expiry.substring(0,contract.m_expiry.length()-4)) + 1;
                contractexpiry = contract.m_symbol + expyear + "01";
                currentPositions.put(contractexpiry, position);
            }
        }
        else {
            contractexpiry = contract.m_symbol + contract.m_expiry.substring(0,contract.m_expiry.length()-2);
            currentPositions.put(contractexpiry, position);
        }
        System.out.println("update Portfolio activated and currentpositions has these details:" + contractexpiry);
    };
    public void updateAccountTime(String timeStamp){};
    public void accountDownloadEnd(String accountName){};
    public void nextValidId( int orderId){};
    public void contractDetails(int reqId, ContractDetails contractDetails){    };
    public void bondContractDetails(int reqId, ContractDetails contractDetails){};
    public void contractDetailsEnd(int reqId){};
    public void execDetails( int reqId, Contract contract, Execution execution){};
    public void execDetailsEnd( int reqId){};
    public void updateMktDepth( int tickerId, int position, int operation, int side, double price, int size){};
    public void updateMktDepthL2( int tickerId, int position, String marketMaker, int operation,
    		int side, double price, int size){};
    public void updateNewsBulletin( int msgId, int msgType, String message, String origExchange){};
    public void managedAccounts( String accountsList){};
    public void receiveFA(int faDataType, String xml){};
    public void historicalData(int reqId, String date, double open, double high, double low,
                      double close, int volume, int count, double WAP, boolean hasGaps){
        
        anIBTInstance.getHistoricalDataFields(reqId, date, open, high,low, close, volume, count, WAP, hasGaps);
    };
    public void scannerParameters(String xml){};
    public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance,
    		String benchmark, String projection, String legsStr){};
    public void scannerDataEnd(int reqId){};
    public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count){
        
    }
    
    public void currentTime(long time){};
    public void fundamentalData(int reqId, String data){};
    public void deltaNeutralValidation(int reqId, UnderComp underComp){};
    public void tickSnapshotEnd(int reqId){};
    public void marketDataType(int reqId, int marketDataType){};
    public void commissionReport(CommissionReport commissionReport){};
}
