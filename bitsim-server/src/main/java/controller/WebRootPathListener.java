//package controller;
//
//import org.joda.time.DateTime;
//import org.springframework.web.context.ContextLoaderListener;
//
//import javax.servlet.ServletContextEvent;
//
///**
// * 在监听器中加入启动方法，获得webroot的物理路径
// */
//public class WebRootPathListener extends ContextLoaderListener {
//    public void contextDestroyed(ServletContextEvent sce) {
//    }
//
//    public void contextInitialized(ServletContextEvent sce) {
//        String webRootPath = sce.getServletContext().getRealPath("/");
//        System.setProperty("webRoot.path", webRootPath);
//        try {
//            System.out.println("MQTT线程启动......");
//            String time = new DateTime().toString("yyyy-MM-dd HH:mm");
//            MqttService.initMessage(time);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}