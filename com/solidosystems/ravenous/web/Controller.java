package com.solidosystems.ravenous.web;

import com.solidosystems.ravenous.host.HostController;
import java.sql.Connection;

public class Controller extends HostController{
    /**
     * This method is called right after the controller is instantiated.
     * Override it to have code executed before all method calls.
     */
    public void pre(){
        
    }
    
    /**
     * This method is called after the URL referenced method on the controller is invoked.
     * Override it to have code executed after all method calls.
     * Be aware that if exceptions are raised, this method will sometimes be called more than once.
     */
    public void post(){
        
    }
}