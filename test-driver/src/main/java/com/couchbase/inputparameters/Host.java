package com.couchbase.inputparameters;

public class Host {

        public String ip;
        public String hostservices;
        public  Host(String ipAddress, String services)
        {
            ip=ipAddress;
            hostservices = services;
        }

        public void printConfig(){
            System.out.println("Host with IP: "+ip+" has services: "+ hostservices);
        }
}
