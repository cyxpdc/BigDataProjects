package com.pdc.fanssystem.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * author PDC
 */
public class Constant {
    /**
     * 获取配置conf
     */
    public static final  Configuration conf = HBaseConfiguration.create();
    /**
     * 微博内容表的表名
     */
    public static final byte[] TABLE_CONTENT = Bytes.toBytes("fanssystem:content");
    /**
     * 用户关系表的表名
     */
    public static final byte[] TABLE_RELATIONS = Bytes. toBytes("fanssystem:relations");
    /**
     * 微博收件箱表的表名
     */
    public static final byte[] TABLE_RECEIVE_CONTENT_EMAIL = Bytes.toBytes("fanssystem:receive_content_email");
}
