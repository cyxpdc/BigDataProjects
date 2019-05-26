# 项目1：粉丝系统小demo

- [x] <span  style="color: #5bdaed; ">目的：用来练习HBase的业务操作所用，无前端，为后端自测</span>

## 需求分析

- 用户编写内容的浏览，数据库表设计

- 可以将索引等信息存在mysql，数据存在Hbase

- 用户社交体现：关注用户，取关用户

- 拉取关注的人的内容

## HBase表

**内容表**

![1558876360996](F:/markdownPicture/assets/1558876360996.png)

**用户关系表**

![1558876392750](F:/markdownPicture/assets/1558876392750.png)

**用户内容接收邮件表**

![1558876454863](F:/markdownPicture/assets/1558876454863.png)

## 业务流程解析

**发布内容**

> 1.内容表中添加1条数据
>
> 2.收件箱表对所有粉丝用户添加数据

**关注用户**

> 1.在用户关系表中，对当前主动操作的用户添加新关注的好友
>
> 2.在用户关系表中，对被关注的用户添加新的粉丝
>
> 3.收件箱表中添加所关注的用户发布的内容

**取关用户**

> 1.在用户关系表中，对当前主动操作的用户移除取关的好友(attends)
>
> 2.在用户关系表中，对被取关的用户移除粉丝
>
> 3.收件箱中删除取关的用户发布的内容

**获取关注的人的内容**

> 1.从收件箱中获取所关注的用户的内容RowKey 
>
> 2.根据获取的RowKey，得到内容

## 类简介

Bootstrap为启动类，内包含一些测试用例；

Constant定义常量；

HConnectionUtil定义HConnection的操作；

FansSystem为驱动类，初始化命名空间及表；

SystemService为四个业务的代码；

Context为内容表；

Relations为用户关系表；

ReceiveContentEmail为用户内容接收邮件表；

Message为内容详情表；

NameSpace为命名空间，类似数据库的schema。