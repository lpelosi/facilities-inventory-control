/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ordertracker;

import javax.print.DocPrintJob;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;

/**
 *
 * @author lpelosi
 */
class PrintJobWatcher {
    boolean done = false;
    
    PrintJobWatcher(DocPrintJob job){
        job.addPrintJobListener(new PrintJobAdapter() {
            
            public void printJobCanceled(PrintJobEvent pje){
                allDone();
            }
            
            public void printJobCompleted(PrintJobEvent pje){
                allDone();
            }
            
            public void printJobFailed(PrintJobEvent pje){
                allDone();
            }
            
            public void printJobNoMoreEvents(PrintJobEvent pje){
                allDone();
            }
            
            void allDone(){
                synchronized (PrintJobWatcher.this){
                    done = true;
                    PrintJobWatcher.this.notify();
                }
            }
        });
    }
    
    public synchronized void waitForDone(){
        try{
            while(!done){
                wait();
            }
        } catch(InterruptedException e){
            
        }
    }
}
