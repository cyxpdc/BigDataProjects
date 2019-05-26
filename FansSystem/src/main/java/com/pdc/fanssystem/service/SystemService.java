package com.pdc.fanssystem.service;

import com.pdc.fanssystem.util.Constant;
import com.pdc.fanssystem.util.HConnectionUtil;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;
import com.pdc.fanssystem.entity.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * author PDC
 */
public class SystemService {

    /**
     * 	发布信息
     * 	a、内容表中数据+1
     * 	b、获取所有粉丝，向粉丝的收件箱表中加入信息的Rowkey
     * @param userId 发布信息的用户id
     * @param content 内容
     */
    public void publishContent(String userId, String content){
        try {
            HConnection connection = HConnectionUtil.createConnection();
            //a、内容表中添加1条数据
            //a.1 首先获取内容表描述
            HTableInterface contentTBL = connection.getTable(TableName.valueOf(Constant.TABLE_CONTENT));
            //a.2 组装Rowkey
            long timestamp = System.currentTimeMillis();
            String contextRowKey = userId + "_" + timestamp;

            Put newContent = new Put(Bytes.toBytes(contextRowKey));
            newContent.add(Bytes.toBytes("info"), Bytes.toBytes("content"), timestamp, Bytes.toBytes(content));

            contentTBL.put(newContent);

            //b、向收件箱表中加入发布的Rowkey
            //b.1 查询用户关系表，得到当前用户有哪些粉丝
            HTableInterface relationsTBL = connection.getTable(TableName.valueOf(Constant.TABLE_RELATIONS));
            //b.2 取出粉丝
            Get fansFamily = new Get(Bytes.toBytes(userId));
            fansFamily.addFamily(Bytes.toBytes("fans"));

            Result result = relationsTBL.get(fansFamily);
            List<byte[]> fansList = new ArrayList<byte[]>();

            //遍历取出当前发布信息的用户的所有粉丝数据
            for(Cell cell : result.rawCells()){
                fansList.add(CellUtil.cloneQualifier(cell));
            }
            //如果该用户没有粉丝，则直接return
            if(fansList.size() <= 0) return;

            //开始操作收件箱表
            HTableInterface recTBL = connection.getTable(TableName.valueOf(Constant.TABLE_RECEIVE_CONTENT_EMAIL));
            List<Put> allFansEmail = new ArrayList<Put>();
            for(byte[] currentFan : fansList){
                Put currentFanEmail = new Put(currentFan);
                currentFanEmail.add(Bytes.toBytes("info"), Bytes.toBytes(userId), timestamp, Bytes.toBytes(contextRowKey));
                allFansEmail.add(currentFanEmail);
            }
            recTBL.put(allFansEmail);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            HConnectionUtil.close();
        }
    }

    /**
     *
     *  关注用户逻辑
     *   a、在微博用户关系表中，对当前主动操作的用户添加新的关注的好友
     *   b、在微博用户关系表中，对被关注的用户添加粉丝（当前操作的用户）
     *   c、当前操作用户的微博收件箱添加所关注的用户发布的微博rowkey
     *
     * @param userId 用户id
     * @param attendsId 被关注者id
     */
    public void addAttends(String userId, String... attendsId){
        if(attendsId == null || attendsId.length <= 0 || userId == null || userId.length() <= 0)
            return;

        try {
            HConnection connection = HConnectionUtil.createConnection();
            //用户关系表操作对象（连接到用户关系表）
            HTableInterface relationsTBL = connection.getTable(TableName.valueOf(Constant.TABLE_RELATIONS));
            List<Put> beAttendedPuts = new ArrayList<Put>();
            //a、在微博用户关系表中，添加新关注的好友
            Put attendPut = new Put(Bytes.toBytes(userId));
            for(String attendId : attendsId){
                //为当前用户添加关注的人
                attendPut.add(Bytes.toBytes("attends"), Bytes.toBytes(attendId), Bytes.toBytes(attendId));
                //b、为被关注的人，添加粉丝
                Put fansPut = new Put(Bytes.toBytes(attendId));
                fansPut.add(Bytes.toBytes("fans"), Bytes.toBytes(userId), Bytes.toBytes(userId));
                //将所有关注的人一个一个的添加到puts（List）集合中
                beAttendedPuts.add(fansPut);
            }
            beAttendedPuts.add(attendPut);
            relationsTBL.put(beAttendedPuts);

            //c.1、微博收件箱添加关注的用户发布的微博内容（content）的rowkey
            HTableInterface contentTBL = connection.getTable(TableName.valueOf(Constant.TABLE_CONTENT));
            Scan scan = new Scan();
            //用于存放取出来的新关注的人所发布的微博的rowkey
            List<byte[]> attentsContentRowkeys = new ArrayList<byte[]>();
            for(String attendId : attendsId){
                //过滤扫描rowkey，即：前置位匹配被关注的人的uid_
                RowFilter filter = new RowFilter(CompareFilter.CompareOp.EQUAL, new SubstringComparator(attendId + "_"));
                //为扫描对象指定过滤规则
                scan.setFilter(filter);
                //通过扫描对象得到scanner
                ResultScanner result = contentTBL.getScanner(scan);
                //迭代器遍历扫描出来的结果集
                Iterator<Result> iterator = result.iterator();
                while(iterator.hasNext()){
                    //取出每一个符合扫描结果的那一行数据
                    Result r = iterator.next();
                    for(Cell cell : r.rawCells()){
                        //将得到的rowkey放置于集合容器中
                        attentsContentRowkeys.add(CellUtil.cloneRow(cell));
                    }

                }
            }
            //c.2、将取出的微博rowkey放置于当前操作用户的收件箱中
            if(attentsContentRowkeys.size() <= 0) return;
            //得到微博收件箱表的操作对象
            HTableInterface recTBL = connection.getTable(TableName.valueOf(Constant.TABLE_RECEIVE_CONTENT_EMAIL));
            //用于存放多个关注的用户的发布的多条微博rowkey信息
            List<Put> recPuts = new ArrayList<Put>();
            for(byte[] contextRowKey : attentsContentRowkeys){
                Put userEmail = new Put(Bytes.toBytes(userId));
                //其格式为uid_timestamp
                String rowKey = Bytes.toString(contextRowKey);
                //分别截取uid和时间戳
                String attendUserId = rowKey.substring(0, rowKey.indexOf("_"));
                long timestamp = Long.parseLong(rowKey.substring(rowKey.indexOf("_") + 1));
                //将微博rowkey添加到指定单元格中
                userEmail.add(Bytes.toBytes("info"), Bytes.toBytes(attendUserId), timestamp, contextRowKey);
                recPuts.add(userEmail);
            }
            recTBL.put(recPuts);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            HConnectionUtil.close();
        }
    }

    /**
     * 取消关注（remove)
     * a、在微博用户关系表中，对当前主动操作的用户删除对应取关的好友
     * b、在微博用户关系表中，对被取消关注的人删除粉丝（当前操作人）
     * c、从收件箱中，删除取关的人的微博的rowkey
     *
     */
    public void removeAttends(String userId, String... attendsId){
        //过滤数据
        if(userId == null || userId.length() <= 0 || attendsId == null || attendsId.length <= 0) return;

        try {
            HConnection connection = HConnectionUtil.createConnection();
            //a、在微博用户关系表中，删除已关注的好友
            HTableInterface relationsTBL = connection.getTable(TableName.valueOf(Constant.TABLE_RELATIONS));

            //待删除的用户关系表中的所有数据
            List<Delete> deletes = new ArrayList<Delete>();
            //当前取关操作者的uid对应的Delete对象
            Delete attendsDelete = new Delete(Bytes.toBytes(userId));
            //遍历取关，同时每次取关都要将被取关的人的粉丝-1
            for(String attendId : attendsId){
                attendsDelete.deleteColumn(Bytes.toBytes("attends"), Bytes.toBytes(attendId));
                Delete fansDelete = new Delete(Bytes.toBytes(attendId));
                fansDelete.deleteColumn(Bytes.toBytes("fans"), Bytes.toBytes(userId));
                deletes.add(fansDelete);
            }

            deletes.add(attendsDelete);
            relationsTBL.delete(deletes);

            //c、从收件箱表中删除取关的人的内容rowkey
            //直接删除关注的人的id即可
            // 不像添加，需要使用过滤器得到所有新关注的人，再添加各自的内容
            HTableInterface recTBL = connection.getTable(TableName.valueOf(Constant.TABLE_RECEIVE_CONTENT_EMAIL));

            Delete contentDelete = new Delete(Bytes.toBytes(userId));
            for(String attendId : attendsId){
                contentDelete.deleteColumn(Bytes.toBytes("info"), Bytes.toBytes(attendId));
            }
            recTBL.delete(contentDelete);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HConnectionUtil.close();
        }
    }

    /**
     * 获取实际内容
     * a、从收件箱中获取所有关注的人的发布的内容的rowkey
     * b、根据得到的rowkey去内容表中得到数据
     * c、将得到的数据封装到Message对象中
     */
    public List<Message> getAttendsContent(String uid){
        try {
            HConnection connection = HConnectionUtil.createConnection();
            HTableInterface recTBL = connection.getTable(TableName.valueOf(Constant.TABLE_RECEIVE_CONTENT_EMAIL));
            //a、从收件箱中取得微博rowKey
            Get get = new Get(Bytes.toBytes(uid));
            //设置最大版本号
            get.setMaxVersions(5);
            List<byte[]> allContentRowKey = new ArrayList<byte[]>();
            Result result = recTBL.get(get);
            for(Cell cell : result.rawCells()){
                allContentRowKey.add(CellUtil.cloneValue(cell));
            }
            //b、根据取出的所有rowkey去微博内容表中检索数据
            HTableInterface contentTBL = connection.getTable(TableName.valueOf(Constant.TABLE_CONTENT));
            List<Get> contentsRowKeyGet = new ArrayList<Get>();
            //根据rowkey取出对应微博的具体内容
            for(byte[] contentRowKey : allContentRowKey){
                Get contentRowKeyGet = new Get(contentRowKey);
                contentsRowKeyGet.add(contentRowKeyGet);
            }
            //得到所有的微博内容的result对象
            Result[] results = contentTBL.get(contentsRowKeyGet);

            List<Message> messages = new ArrayList<Message>();
            for(Result res : results){
                for(Cell cell : res.rawCells()){
                    Message message = new Message();

                    String rowKey = Bytes.toString(CellUtil.cloneRow(cell));
                    String userid = rowKey.substring(0, rowKey.indexOf("_"));
                    String timestamp = rowKey.substring(rowKey.indexOf("_") + 1);
                    String content = Bytes.toString(CellUtil.cloneValue(cell));

                    message.setContent(content);
                    message.setTimestamp(timestamp);
                    message.setUid(userid);

                    messages.add(message);
                }
            }
            return messages;
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            HConnectionUtil.close();
        }
        return null;
    }
}
