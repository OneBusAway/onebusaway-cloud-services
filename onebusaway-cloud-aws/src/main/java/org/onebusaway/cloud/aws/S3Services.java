/**
 * Copyright (C) 2018 Cambridge Systematics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.cloud.aws;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// adapted from mta-otp-deployer
public class S3Services {
    private final Logger _log = LoggerFactory.getLogger(S3Services.class);

    // this expects config files present in ~/.aws
    final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();

    final TransferManager tm = TransferManagerBuilder.defaultTransferManager();

    private AmazonS3 getS3Provider(CredentialContainer cc) {
        String profile = cc.getProfile();
        if (profile == null || "default".equalsIgnoreCase(profile)) {
            return s3;
        }
        AmazonS3 provider = AmazonS3ClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider(profile)).build();
        return provider;
    }

    // input stream will need be closed
    public S3ObjectInputStream fetch(String url, CredentialContainer profile) {
        _log.info("fetching {} to {} with profile {}", url, profile);
        AmazonS3URI uri = new AmazonS3URI(url);
        S3Object o = getS3Provider(profile).getObject(uri.getBucket(), uri.getKey());
        return o.getObjectContent();
    }

    public String fetch(String url, String toFileName, String destinationPath, CredentialContainer profile) {
        _log.info("fetching {} to {} with profile {}", url, destinationPath);
        S3ObjectInputStream s3is = fetch(url, profile);
        File mkdir = new File(destinationPath);
        if (!mkdir.exists() || !mkdir.isDirectory()) {
            mkdir.mkdirs();
        }
        String destinationFileName = destinationPath + File.separator + toFileName;
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(new File(destinationFileName));
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
            s3is.close();
            fos.close();
        } catch (IOException ioe) {
            _log.error("unable to write {} to {}", url, destinationFileName);
            destinationFileName = null;
        }
        return destinationFileName;
    }

    public boolean put(String url, String fileName) {
        _log.info("uploading {} to {}", fileName, url);
        AmazonS3URI uri = new AmazonS3URI(url);
        s3.putObject(uri.getBucket(), uri.getKey(), new File(fileName));
        return true;
    }

    public boolean putRecursively(String url, String directoryName) {
        _log.info("uploading (recursively) {} to {}", directoryName, url);
        try {
            AmazonS3URI uri = new AmazonS3URI(url);
            MultipleFileUpload upload = tm.uploadDirectory(uri.getBucket(), uri.getKey(), new File(directoryName), true);
            upload.waitForCompletion();
            _log.info("upload complete!");
        } catch (Exception any) {
            _log.error("upload failed: ", any);
            return false;
        }
        return true;
    }
}
