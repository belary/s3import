package com.mgtv.platback.toolbox.s3import.service;


import com.mgtv.platback.toolbox.s3import.config.CmdlineConstants;
import com.mgtv.platback.toolbox.s3import.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        handleS3LogByDate();
    }

    protected void handleS3LogByDate() throws Exception {

        String strLogDate, strLocalPath, strOutputPath;
        List<String> lstS3KeyPrefix;
        List<String> lstLogHours = arguments.getOptionValues(CmdlineConstants.LOG_HOURS);

        strLogDate = getFirstElement(arguments.getOptionValues(CmdlineConstants.LOG_DATE));
        lstS3KeyPrefix = buildS3Prefix(strLogDate, lstLogHours);
        lstS3KeyPrefix.forEach(prefix -> log.info("s3 key is {}", prefix));
        strLocalPath = getFirstElement(arguments.getOptionValues(CmdlineConstants.LOCAL_PATH));
        strOutputPath = getFirstElement(arguments.getOptionValues(CmdlineConstants.OUTPUT_PATH)) ;



        s3DownloadMgr.cleanupDir(strOutputPath, strLocalPath);

        // download files
        s3DownloadMgr.downloadDir(bucket,  lstS3KeyPrefix, strLocalPath, false);

        log.info("download complete.");

        // download postprocess

        int cnt = s3DownloadMgr.unzipDir(new File(strLocalPath), strOutputPath);

        log.info("unzipped completed {} files", cnt);
    }


    private List<String> buildS3Prefix(String logDate, List<String> logHours) {
        List<String> lstPrefix = new ArrayList<>();
        if (logHours != null && logHours.size() > 0) {
            lstPrefix =
                    logHours.stream()
                            .map(hour -> logPrefix + "/" + logDate + "/" + hour)
                            .collect(Collectors.toList());
        } else {
            lstPrefix.add(logPrefix + "/"  + logDate);
        }

        return lstPrefix;
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

                "    --log_hours   - Specify the hour to download multiple hours supported\n" +
                "                    no provided with this parameter will download all the " +
                "                    data in the log_date \n" +

                "    --local_path  - The local path to use to download the object(s)\n" +
                "                     cleared each time when begin to download.\n\n" +

                "    --output_path - The local path to store the unzipped or merged log file,should end with \\(windows) or / \n\n" +
               
                "Examples:\n" +
                "    --log_date=20210625 --local_path=\\mnt\\data\\download --output_path=\\mnt\\data\\logs";

        System.out.println(USAGE);
        System.out.println("    --logDate=${logDate} --merge=${merge} --local_path=${downloadDir} --outputDir=${outputDir} --log_date=2021/07/06 --log_hours=20 --log_hours=21 ");
    }

}
