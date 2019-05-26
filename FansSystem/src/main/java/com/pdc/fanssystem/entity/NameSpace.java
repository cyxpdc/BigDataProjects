package com.pdc.fanssystem.entity;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;

import java.io.IOException;

/**
 * author PDC
 */
public class NameSpace {
    /**
     * 获取配置conf
     */
    private static Configuration conf = HBaseConfiguration.create();

    /**
     * 初始化命名空间
     */
    public static void initNamespace(){
        HBaseAdmin admin = null;
        try {
            admin = new HBaseAdmin(conf);
            //命名空间类似于关系型数据库中的schema，可以想象成文件夹
            NamespaceDescriptor weibo = NamespaceDescriptor
                    .create("weibo")
                    .addConfiguration("creator", "pdc")
                    .addConfiguration("create_time", System.currentTimeMillis() + "")
                    .build();
            admin.createNamespace(weibo);
            System.out.println("初始化命名空间成功！");
        } catch (MasterNotRunningException e) {
            e.printStackTrace();
        } catch (ZooKeeperConnectionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(null != admin){
                try {
                    admin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
