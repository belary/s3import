package com.mgtv.platback.toolbox.s3import.service;


import com.mgtv.platback.toolbox.s3import.config.CmdlineConstants;
import com.mgtv.platback.toolbox.s3import.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CmdOptionHandler {


    private static final Logger log =
            LoggerFactory.getLogger(CmdOptionHandler.class);

    @Resource
    private ApplicationArguments arguments;

    @Resource
    private S3DownloadMgr s3DownloadMgr;

    @Value("${aws.s3.prefix}")
    private String logPrefix;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public void handleArgumentOption() throws Exception {

        if (CollectionUtils.isEmpty(arguments.getOptionValues(CmdlineConstants.LOG_DATE))
                || CollectionUtils.isEmpty(arguments.getOptionValues(CmdlineConstants.LOCAL_PATH))
                || CollectionUtils.isEmpty(arguments.getOptionValues(CmdlineConstants.OUTPUT_PATH))
        ) {
            log.info("invalid params");
            printUsage();
            return;
        }

        if (arguments.containsOption(CmdlineConstants.CLI_HELP)) {
            printUsage();
            return;
        }

        handleDownloadS3(); 
    }

    protected void handleDownloadS3() {
        String strLogDate, strS3KeyPrefix,  strLocalPath, strOutputPath;
        strLogDate = getFirstElement(arguments.getOptionValues(CmdlineConstants.LOG_DATE));
        strS3KeyPrefix = buildS3Prefix(strLogDate) + "/" + "20";
        strLocalPath = getFirstElement(arguments.getOptionValues(CmdlineConstants.LOCAL_PATH));
        s3DownloadMgr.downloadDir(bucket, strS3KeyPrefix, strLocalPath, false);

    }

    private String buildS3Prefix(String logDate) {
        return  logPrefix + "/"  + logDate;
    }
    
    private <T> T getFirstElement(List<T> lstElements) {
        if (lstElements == null || lstElements.size() == 0) {
            return null;
        }
        return lstElements.get(0);
    }


    public static void printUsage() {
        final String USAGE = "\n" +
                "Usage:\n" +
                "    s3download [--recursive] [--pause] <s3_path> <local_paths>\n\n" +
                "Where:\n" +
                "    --log_date    - Specify the log date to retrieve.\n" +
                "                    Copies the contents of the directory recursively.\n\n" +

                "    --merge       - Attempt to merge the logs into one piece default value is true\n\n" +

                "    --local_path  - The local path to use to download the object(s)\n" +
                "                     cleared each time when begin to download.\n\n" +

                "    --output_path - The local path to store the unzipped or merged log file\n\n" +
               
                "Examples:\n" +
                "    --log_date=20210625 --local_path=\\mnt\\data\\download --output_path=\\mnt\\data\\logs";

        System.out.println(USAGE);
        System.out.println("    --logDate=${logDate} --merge=${merge} --local_path=${downloadDir} --outputDir=${outputDir}");
    }

}
