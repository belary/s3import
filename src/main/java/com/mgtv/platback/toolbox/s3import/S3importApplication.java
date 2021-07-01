package com.mgtv.platback.toolbox.s3import;

import com.mgtv.platback.toolbox.s3import.service.CmdOptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class S3importApplication {

    private static final Logger logger =
            LoggerFactory.getLogger(S3importApplication.class);

    public static void main(String[] args) throws Exception {

        ConfigurableApplicationContext context =
                SpringApplication.run(S3importApplication.class, args);


        // 解析命令行
        CmdOptionHandler cmdRunner = context.getBean(CmdOptionHandler.class);

        // 根据命令行执行
        cmdRunner.handleArgumentOption();

        logger.info("task completed.");
    }

}
