package com.pdc.fanssystem.entity;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * author PDC
 * 站内信
 */
public class ReceiveContentEmail {
    /**
     * 获取配置conf
     */
    private Configuration conf = HBaseConfiguration.create();
    /**
     * 收件箱表的表名
     */
    private static final byte[] TABLE_RECEIVE_CONTENT_EMAIL = Bytes.toBytes("weibo:receive_content_email");

    /**
     * 创建收件箱表
     * Table Name: weibo:receive_content_email
     * RowKey:用户ID
     * ColumnFamily:info
     * ColumnLabel:用户ID-发布微博的人的用户ID
     * ColumnValue:关注的人的微博的RowKey
     * Version:1000
     */
    public void createTableReceiveContentEmail(){
        HBaseAdmin admin = null;
        try {
            admin = new HBaseAdmin(conf);

            HTableDescriptor receive_content_email = new HTableDescriptor(TableName.valueOf(TABLE_RECEIVE_CONTENT_EMAIL));
            HColumnDescriptor info = new HColumnDescriptor(Bytes.toBytes("info"));

            info.setBlockCacheEnabled(true);
            info.setBlocksize(2097152);
            info.setMaxVersions(1000);
            info.setMinVersions(1000);

            receive_content_email.addFamily(info);;
            admin.createTable(receive_content_email);
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
