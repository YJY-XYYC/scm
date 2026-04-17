package com.scm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class DataSourceConfig implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    private final DataSource dataSource;

    @Autowired
    public DataSourceConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        try (Connection connection = dataSource.getConnection()) {
            log.info("数据库连接成功");
            log.info("数据库URL: {}", connection.getMetaData().getURL());
            log.info("数据库用户名: {}", connection.getMetaData().getUserName());
        } catch (Exception e) {
            log.error("数据库连接失败", e);
        }
    }
}