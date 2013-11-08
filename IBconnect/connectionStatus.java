/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package IBconnect;

/**
 *
 * @author Terence
 */
public class connectionStatus implements Runnable{
    
    private IBTradeGui anIBTInstance;
    
    public connectionStatus(IBTradeGui anIBTInstance){
        this.anIBTInstance = anIBTInstance;
    }
    
    @Override
    public void run() {
        while (true){
            if(!IBTradeGui.connection.isConnected()){
                anIBTInstance.setConnectionText(IBTradeGui.connection.isConnected(), anIBTInstance);
                IBTradeGui.idsForReqs +=1;
                IBTradeGui.connection.eConnect("127.0.0.1",7496,IBTradeGui.idsForReqs); //7496 through TWS and 4001 through IB Gateway
                IBTradeGui.clientId = IBTradeGui.idsForReqs;
            }
            else {
                anIBTInstance.setConnectionText(IBTradeGui.connection.isConnected(), anIBTInstance);
            }
        }
    }
}
    

