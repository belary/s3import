package com.mgtv.platback.toolbox.s3import.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.mgtv.platback.toolbox.s3import.utils.XferMgrProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;


@Service
public class S3DownloadMgr {

    private static final Logger log = LoggerFactory.getLogger(S3DownloadMgr.class);

    private int deletedFilesCnt;

    private int unzipCnt;


    /**
     *  remove local dir and output dir
     */
    public void cleanupDir(String outputDir, String downloadDir) {

        int delUnzipCnt = deleteDir(new File(outputDir));
        log.info("清理解压文件共"+delUnzipCnt+"个");
        deletedFilesCnt = 0;
        int delDownloadCnt = deleteDir(new File(downloadDir));
        log.info("清理下载文件共"+delDownloadCnt+"个");
    }

    /**
     * delete file recursively
     */
    public int deleteDir(File root) {
        File[] files = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    deleteDir(f);
                } else {
                    if (f.exists()) { // 判断是否存在
                        deleteDir(f);
                        try {
                            if (f.delete()) {
                                deletedFilesCnt++;
                            }
                        } catch (Exception e) {
                            log.error("删除文件失败", e);
                        }
                    }
                }
            }
        }
        return deletedFilesCnt;
    }


    /**
     * unzip to specify folder
     */
    public int unzipDir(File zipFile, String outputDir ) throws Exception {

        File outputFile = new File(outputDir);
        if ( !outputFile.exists() && !outputFile.mkdirs() ) {
            throw new RuntimeException("Couldn't create output directories for " + outputFile.getAbsolutePath());
        }

        if (zipFile.isDirectory()) {
            for (File child : zipFile.listFiles()) {
                unzipDir(child, outputDir);
            }
        } else if (zipFile.getAbsolutePath().endsWith(".gz")) {
            unzipCnt ++;
            try (GZIPInputStream gis = new GZIPInputStream(
                    new FileInputStream(zipFile)
            )) {
                Files.copy(gis, Paths.get(outputDir + zipFile.getName().replace(".gz", "")), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return unzipCnt;
    }

    /**
     * download a folder
     */
    public void downloadDir(String bucket_name, String key_prefix,
                            String dir_path, boolean pause) {
       log.info("downloading to directory: " + dir_path +
                (pause ? " (pause)" : ""));

        TransferManager xfer_mgr = TransferManagerBuilder.standard().build();

        try {
            MultipleFileDownload xfer = xfer_mgr.downloadDirectory(
                    bucket_name, key_prefix, new File(dir_path));
            // loop with Transfer.isDone()
            XferMgrProgress.showTransferProgress(xfer);
            // or block with Transfer.waitForCompletion()
            XferMgrProgress.waitForCompletion(xfer);
        } catch (AmazonServiceException e) {
            log.error("aws xfer folder error!", e);
        }
        xfer_mgr.shutdownNow();
    }


    /**
     * download single file
     */
    public void downloadFile(String bucket_name, String key_name,
                             String file_path, boolean pause) {
        log.info("Downloading to file: " + file_path +
                (pause ? " (pause)" : ""));

        File f = new File(file_path);
        TransferManager xfer_mgr = TransferManagerBuilder.standard().build();
        try {
            Download xfer = xfer_mgr.download(bucket_name, key_name, f);
            // loop with Transfer.isDone()
            XferMgrProgress.showTransferProgress(xfer);
            // or block with Transfer.waitForCompletion()
            XferMgrProgress.waitForCompletion(xfer);
        } catch (AmazonServiceException e) {
            log.error("aws xferMgr error!", e);
        }
        xfer_mgr.shutdownNow();
    }

}
