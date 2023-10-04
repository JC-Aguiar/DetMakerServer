//package br.com.ppw.dma.util;
//
//import org.springframework.web.WebApplicationInitializer;
//import org.springframework.web.context.support.GenericWebApplicationContext;
//import org.springframework.web.servlet.DispatcherServlet;
//
//public class MainWebAppInitializer implements WebApplicationInitializer {
//
//    private final static String TMP_FOLDER = "/tmp";
//    private final static int MAX_UPLOAD_SIZE = 5 * 1024 * 1024;
//
//    @Override
//    public void onStartup(ServletContext servletContext) throws ServletException {
//        ServletRegistration.Dynamic appServlet = servletContext.addServlet(
//            "mvc", new DispatcherServlet(new GenericWebApplicationContext()));
//        appServlet.setLoadOnStartup(1);
//        MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
//            TMP_FOLDER,
//            MAX_UPLOAD_SIZE,
//            MAX_UPLOAD_SIZE * 2,
//            MAX_UPLOAD_SIZE / 2);
//        appServlet.setMultipartConfig(multipartConfigElement);
//    }
//}
