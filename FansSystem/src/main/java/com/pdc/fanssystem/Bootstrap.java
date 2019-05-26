package com.pdc.fanssystem;

import com.pdc.fanssystem.driver.FansSystem;
import com.pdc.fanssystem.entity.Message;
import com.pdc.fanssystem.service.SystemService;

import java.util.List;

/**
 * author PDC
 */
public class Bootstrap {

    public static void main(String[] args) {
        start();
    }

    private static void start() {
        init();
        test();
    }

    private static void init() {
        FansSystem fansSystem = new FansSystem();
        fansSystem.initTable();
    }

    private static void test() {
        Bootstrap bootstrap = new Bootstrap();
        SystemService service = new SystemService();

        bootstrap.testPublishContent(service);
        bootstrap.testAddAttend(service);
        bootstrap.testShowMessage(service);
        bootstrap.testRemoveAttend(service);
        bootstrap.testShowMessage(service);
    }

    /*------------------------------------TestMethod----------------------------------*/

    public void testPublishContent(SystemService service){
        service.publishContent("0001", "今天买了一包薯片");
        service.publishContent("0001", "今天天气不错。");
    }

    public void testAddAttend(SystemService service){
        service.publishContent("0008", "准备1！");
        service.publishContent("0009", "准备2！");
        service.addAttends("0001", "0008", "0009");
    }

    public void testRemoveAttend(SystemService service) {
        service.removeAttends("0001", "0008");
    }

    public void testShowMessage(SystemService service){
        List<Message> messages = service.getAttendsContent("0001");
        for(Message message : messages){
            System.out.println(message);
        }
    }

}
