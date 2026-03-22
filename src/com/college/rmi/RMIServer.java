package com.college.rmi;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class RMIServer {
    public static void main(String[] args) throws Exception {
        CollegeService service = new CollegeServiceImpl();
        LocateRegistry.createRegistry(1099);
        Naming.rebind("rmi://localhost/CollegeService", service);
        System.out.println("RMI Server running on port 1099");
    }
}
