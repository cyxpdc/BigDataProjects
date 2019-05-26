package com.pdc.fanssystem.entity;

import com.pdc.fanssystem.util.Constant;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 * author PDC
 * 关系表
 */
public class Relations {

    /**
     * 用户关系表
     * Table Name:weibo:relations
     * RowKey:用户ID
     * ColumnFamily:attends,fans
     * ColumnLabel:关注用户ID，粉丝用户ID
     * ColumnValue:用户ID
     * Version：1个版本
     */
    public void createTableRelations(){
        HBaseAdmin admin = null;
        try {
            admin = new HBaseAdmin(Constant.conf);
            HTableDescriptor relations = new HTableDescriptor(TableName.valueOf(Constant.TABLE_RELATIONS));
            //关注的人列族
            HColumnDescriptor attends = new HColumnDescriptor(Bytes.toBytes("attends"));
            //设置块缓存
            attends.setBlockCacheEnabled(true);
            //设置块缓存大小
            attends.setBlocksize(2097152);
			/*设置压缩方式
			info.setCompressionType(Algorithm.SNAPPY);*/
            //设置版本确界
            attends.setMaxVersions(1);
            attends.setMinVersions(1);
            //粉丝列族
            HColumnDescriptor fans = new HColumnDescriptor(Bytes.toBytes("fans"));
            fans.setBlockCacheEnabled(true);
            fans.setBlocksize(2097152);
            fans.setMaxVersions(1);
            fans.setMinVersions(1);

            relations.addFamily(attends);
            relations.addFamily(fans);
            admin.createTable(relations);
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
