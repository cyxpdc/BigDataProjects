package com.pdc.fanssystem.driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.pdc.fanssystem.entity.Content;
import com.pdc.fanssystem.entity.NameSpace;
import com.pdc.fanssystem.entity.ReceiveContentEmail;
import com.pdc.fanssystem.entity.Relations;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;
/**
 * author PDC
 */
public class FansSystem {
    private Configuration conf = HBaseConfiguration.create();
    private Content content = new Content();
    private Relations relations = new Relations();
    private ReceiveContentEmail receiveContentEmail = new ReceiveContentEmail();

    public void initTable(){
        NameSpace.initNamespace();
        content.createTableContent();
        relations.createTableRelations();
        receiveContentEmail.createTableReceiveContentEmail();
        System.out.println("初始化表成功！");
    }
}
