package com.pdc.fanssystem.entity;

import com.pdc.fanssystem.util.Constant;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * author PDC
 * 内容表
 */
public class Content {

    /**
     * 创建内容表
     * Table Name:weibo:content
     * RowKey:用户ID_时间戳
     * ColumnFamily:info
     * ColumnLabel:标题	内容		图片URL
     * Version:1个版本
     */
    public void createTableContent(){
        HBaseAdmin admin = null;
        try {
            admin = new HBaseAdmin(Constant.conf);
            //表信息
            HTableDescriptor content = new HTableDescriptor(TableName.valueOf(Constant.TABLE_CONTENT));
            //列族信息
            HColumnDescriptor info = new HColumnDescriptor(Bytes.toBytes("info"));
            //设置块缓存
            info.setBlockCacheEnabled(true);
            //设置块缓存大小
            info.setBlocksize(2097152);
			/*设置压缩方式
            info.setCompressionType(Algorithm.SNAPPY);*/
            //设置版本确界
            info.setMaxVersions(1);
            info.setMinVersions(1);
            //添加列族到表中
            content.addFamily(info);
            //创建表
            admin.createTable(content);

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
