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


@Service
public class S3DownloadMgr {

    private static final Logger log = LoggerFactory.getLogger(S3DownloadMgr.class);


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
