//package br.com.ppw.dma.config;
//
//import lombok.Getter;
//import lombok.Setter;
//import lombok.ToString;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.jdbc.datasource.DriverManagerDataSource;
//
//import javax.sql.DataSource;
//
//@Getter
//@Setter
//@Slf4j
//@ToString
//@Configuration
//@PropertySource("classpath:db-config.properties")
//public class DatabaseConfig {
//
//    @Value("${db.sistema}")
//    private String dbSistema;
//
//    @Value("${db.ambiente}")
//    private String dbAmbiente;
//
//    @Value("${db.url}")
//    private String dbUrl;
//
//    @Value("${db.username}")
//    private String dbUsername;
//
//    @Value("${db.password}")
//    private String dbPassword;
//
//    @Value("${db.schema}")
//    private String dbSchema;
//
//    public static BancoAmbiente ambienteInfo;
//
//    public record BancoAmbiente(String sistema, String name) { }
//
//    @Bean
//    public BancoAmbiente getAmbiente() {
//        log.info("Sistema: {}. Ambiente: {}.", dbSistema, dbAmbiente);
//        ambienteInfo = new BancoAmbiente(dbSistema, dbAmbiente);
//        return ambienteInfo;
//    }
//
//    @Bean
//    public DataSource dataSource() {
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
//        dataSource.setUrl(dbUrl);
//        dataSource.setUsername(dbUsername);
//        dataSource.setPassword(dbPassword);
//        dataSource.setSchema(dbSchema);
//        return dataSource;
//    }
//}
