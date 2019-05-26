package com.pdc.fanssystem.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;

import java.io.IOException;

/**
 * author PDC
 */
public class HConnectionUtil {

    private static HConnection connection = null;

    public static HConnection createConnection(){
        try {
            connection = HConnectionManager.createConnection(Constant.conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void close() {
        if(null != connection){
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
