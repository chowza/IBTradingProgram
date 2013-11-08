/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package IBconnect;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author Terence
 */
public class chooseMostActiveContract implements Job{

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        try {
            IBTradeGui.chooseContract("ZC");
            IBTradeGui.chooseContract("ZW");
            IBTradeGui.chooseContract("ZL");
            IBTradeGui.chooseContract("ZO");
            IBTradeGui.chooseContract("ES");
            IBTradeGui.chooseContract("LE");
            Logger.getLogger(chooseMostActiveContract.class.getName()).info("First 6 contracts done, pausing 10 minutes");
            Thread.sleep(600000);
            IBTradeGui.chooseContract("HE");
            IBTradeGui.chooseContract("GF");
            IBTradeGui.chooseContract("ZN");
            IBTradeGui.chooseContract("ZB");
            IBTradeGui.chooseContract("CL");
            IBTradeGui.chooseContract("NG");
            Logger.getLogger(chooseMostActiveContract.class.getName()).info("Second 6 contracts done, pausing 10 minutes");
            Thread.sleep(600000);
            IBTradeGui.chooseContract("HO");
            IBTradeGui.chooseContract("GC");
            IBTradeGui.chooseContract("SI");
            IBTradeGui.chooseContract("PL");
            IBTradeGui.chooseContract("PA");
            IBTradeGui.chooseContract("HG");
            Logger.getLogger(chooseMostActiveContract.class.getName()).info("Final 6 contracts done, pausing 10 minutes");
            Thread.sleep(600000);
            Logger.getLogger(chooseMostActiveContract.class.getName()).info("Finding most active contracts");
            IBTradeGui.findMostActiveContracts();
        } catch (InterruptedException ex) {
            Logger.getLogger(chooseMostActiveContract.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
