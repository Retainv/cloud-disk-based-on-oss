package top.retain.nd.util;

import cn.hutool.core.util.RandomUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import top.retain.nd.dto.STSResp;
import top.retain.nd.exception.DownloadException;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * @author Retain
 * @date 2021/9/29 13:13
 */
@Component
@Slf4j
public class OSSUtils {

    @Value("${OSS.endpoint}")
    private String endpoint;
    @Value("${OSS.ram.accessKeyId}")
    private String accessKeyId;
    @Value("${OSS.ram.accessKeySecret}")
    private String accessKeySecret;
    @Value("${OSS.ram.roleArn}")
    private String roleArn;
    @Value("${OSS.region}")
    private String region;

    public String createBucket(Long userId) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        // 创建CreateBucketRequest对象。
        String bucketName = userId + "-" + RandomUtil.randomString(10);
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
//        createBucketRequest.setHnsStatus(HnsStatus.Enabled);
//createBucketRequest.setCannedACL(CannedAccessControlList.PublicRead);
        ossClient.createBucket(createBucketRequest);
        setCors(bucketName);
        ossClient.shutdown();
        return bucketName;
    }

    public void setCors(String bucketName) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        SetBucketCORSRequest request = new SetBucketCORSRequest(bucketName);
        ArrayList<SetBucketCORSRequest.CORSRule> putCorsRules = new ArrayList<>();

        SetBucketCORSRequest.CORSRule corRule = new SetBucketCORSRequest.CORSRule();

        ArrayList<String> allowedOrigin = new ArrayList<String>();
// 指定允许跨域请求的来源。
        allowedOrigin.add( "*");

        ArrayList<String> allowedMethod = new ArrayList<String>();
// 指定允许的跨域请求方法(GET/PUT/DELETE/POST/HEAD)。
        allowedMethod.add("GET");
        allowedMethod.add("PUT");
        allowedMethod.add("DELETE");
        allowedMethod.add("POST");

        ArrayList<String> allowedHeader = new ArrayList<String>();
// 是否允许预取指令（OPTIONS）中Access-Control-Request-Headers头中指定的Header。
        allowedHeader.add("*");

        ArrayList<String> exposedHeader = new ArrayList<String>();
// 指定允许用户从应用程序中访问的响应头。
        exposedHeader.add("ETag");
        exposedHeader.add("x-oss-request-id");

        corRule.setAllowedMethods(allowedMethod);
        corRule.setAllowedOrigins(allowedOrigin);
// AllowedHeaders和ExposeHeaders不支持通配符。
        corRule.setAllowedHeaders(allowedHeader);
        corRule.setExposeHeaders(exposedHeader);
// 指定浏览器对特定资源的预取（OPTIONS）请求返回结果的缓存时间，单位为秒。
        corRule.setMaxAgeSeconds(100);

        putCorsRules.add(corRule);
        request.setCorsRules(putCorsRules);
        ossClient.setBucketCORS(request);
// 关闭OSSClient。
        ossClient.shutdown();

    }

    public STSResp generateSTS() {
        //构建一个阿里云客户端，用于发起请求。
        //构建阿里云客户端时需要设置AccessKey ID和AccessKey Secret。
        DefaultProfile profile = DefaultProfile.getProfile(region, accessKeyId, accessKeySecret);
        IAcsClient client = new DefaultAcsClient(profile);

        //构造请求，设置参数。关于参数含义和设置方法，请参见《API参考》。
        AssumeRoleRequest request = new AssumeRoleRequest();
        request.setRegionId(region);
        request.setRoleArn(roleArn);
        request.setRoleSessionName("tmpRam");
        // 一天过期
        request.setDurationSeconds(43200L);

        //发起请求，并得到响应。
        try {
            STSResp sts = new STSResp();
            AssumeRoleResponse response = client.getAcsResponse(request);
            sts.setCredentials(response.getCredentials());
            log.info("获取STS：" + response.getCredentials().toString());
            return sts;
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            log.error("ErrCode:" + e.getErrCode());
            log.error("ErrMsg:" + e.getErrMsg());
            log.error("RequestId:" + e.getRequestId());
        }
        return null;
    }
    public boolean isBucketExist(String bucketName) {
        if (StringUtils.isEmpty(bucketName)) {
            return false;
        }
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        boolean exists = ossClient.doesBucketExist(bucketName);
        ossClient.shutdown();
        return exists;
    }


    // ********************上传文件****************


    public boolean createDir(String bucketName, String prefix, String dirName) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        String targetFilePath = prefix + dirName + "/";
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("application/x-directory");
        objectMetadata.setContentLength(0);

        byte[] buffer = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, targetFilePath, in);

        try {
            PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);
            System.out.println(putObjectResult);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        ossClient.shutdown();
        return true;
    }

    public String uploadImg(File file) {
        String bucketName = "avatorandqr";
        if (!file.exists()) {
            return "";
        }
        String targetFilePath = "qr/" + file.getName();
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, targetFilePath, file);

        try {
             ossClient.putObject(putObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return generateUrl(bucketName, targetFilePath, new Date(System.currentTimeMillis() + 3000L * 1000 * 24 * 10000));
    }

    public String uploadAvator(MultipartFile file, Long userId) throws IOException {
        String bucketName = "focus-avator-1";
        String targetFilePath = userId + "/" + file.getOriginalFilename();
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, targetFilePath, file.getInputStream());

        try {
            ossClient.putObject(putObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return generateUrl(bucketName, targetFilePath, new Date(System.currentTimeMillis() + 3000L * 1000 * 24 * 10000));
    }
    /**
     * 普通文件上传
     *
     * @param bucketName
     * @param targetFilePath
     * @param localFilePath
     * @return
     */
    public boolean normalUpload(String bucketName, String targetFilePath, String localFilePath) {
        File file = new File(localFilePath);
        if (!file.exists()) {
            return false;
        }
        targetFilePath = targetFilePath + "/" + localFilePath.substring(localFilePath.lastIndexOf(File.separator) + 1);
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, targetFilePath, file);

        try {
            ossClient.putObject(putObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        ossClient.shutdown();
        return true;
    }


    /**
     * 断点续传上传
     *
     * @return
     */
    public boolean resumeUpload(String bucketName, String localFilePath, String prefix) throws Throwable {
        File file = new File(localFilePath);


        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        // 不存在文件夹，新创建
        if (!file.exists() && !localFilePath.contains(".")) {
                localFilePath = prefix + localFilePath + "/";
            CompleteMultipartUploadResult uploadResult = doUploadRequest(localFilePath, bucketName, localFilePath, ossClient);
            log.info("上传" + localFilePath + "e-tag:" + uploadResult.getETag());
            return true;
        }
        if (file.isDirectory()) {
            // 存在本地文件夹，上传
            String dir =localFilePath.substring(localFilePath.lastIndexOf(File.separator) + 1);
            List<String> files = FileUtils.getDirFiles(file);
            assert files != null;
            for (String f : files) {
                String key = prefix + f.substring(f.lastIndexOf(dir));
                // windows分隔符转换
                key = key.replaceAll("\\\\", "/");
                // 文件夹
                if (!key.contains(".")) {
                    key += "/";
                }
                CompleteMultipartUploadResult uploadResult = doUploadRequest(f, bucketName, key, ossClient);
                log.info("上传" + localFilePath + "e-tag:" + uploadResult.getETag());

            }
        }
        ossClient.shutdown();
        return true;
    }

    private CompleteMultipartUploadResult doUploadRequest(String filePath, String bucketName, String key, OSS ossClient) throws Throwable {
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType("text/plain");
        UploadFileRequest uploadFileRequest = new UploadFileRequest(bucketName, key);

        uploadFileRequest.setUploadFile(filePath);
        // 指定上传并发线程数，默认值为1。
        uploadFileRequest.setTaskNum(5);
        // 指定上传的分片大小。
        uploadFileRequest.setPartSize(1024 * 1024);
        // 开启断点续传，默认关闭。
        uploadFileRequest.setEnableCheckpoint(true);
        // 记录本地分片上传结果的文件。上传过程中的进度信息会保存在该文件中。
//        uploadFileRequest.setCheckpointFile(localFil);
        // 文件的元数据。
        uploadFileRequest.setObjectMetadata(meta);
        UploadFileResult uploadResult = ossClient.uploadFile(uploadFileRequest);
//        uploadFileRequest.setCallback();
        // 断点续传上传。
        CompleteMultipartUploadResult multipartUploadResult =
                uploadResult.getMultipartUploadResult();
        return multipartUploadResult;
    }



    // ****************下载文件************************

    /**
     * 普通下载
     *
     * @param localFilePath 下载到的本地位置
     * @param objectPath    oss文件路径
     */
    public boolean normalDownload(String bucketName, String localFilePath, String objectPath) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.getObject(new GetObjectRequest(bucketName, objectPath)
                    , new File(localFilePath));
        } catch (Exception e) {
            throw new DownloadException("下载失败！请稍后再试");
        }
        ossClient.shutdown();
        return true;
    }

    /**
     * 断点续传下载
     *
     * @return
     */
    public boolean resumeDownload(String bucketName, String localFilePath, String objectPath) throws Throwable {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
// 下载请求，10个任务并发下载，启动断点续传。
        DownloadFileRequest downloadFileRequest = new DownloadFileRequest(bucketName, objectPath);
        downloadFileRequest.setDownloadFile(localFilePath);
        downloadFileRequest.setPartSize(1024 * 1024);
        downloadFileRequest.setTaskNum(10);
        downloadFileRequest.setEnableCheckpoint(true);
        downloadFileRequest.setCheckpointFile(localFilePath + ".dcp");

// 下载文件。
        DownloadFileResult downloadRes = null;
            downloadRes = ossClient.downloadFile(downloadFileRequest);

// 下载成功时，会返回文件元信息。
        ObjectMetadata objectMetadata = downloadRes.getObjectMetadata();
        System.out.println(objectMetadata.getETag());
        System.out.println(objectMetadata.getLastModified());
        System.out.println(objectMetadata.getUserMetadata().get("meta"));

        ossClient.shutdown();
        return true;
    }

    /**
     * 浏览器流式下载
     */
    public void streamDownload(String bucketName, String objectPath, HttpServletResponse response) {
        InputStream in = null;
        OutputStream out = null;
        try {
            response.reset();
            String fileName = objectPath.substring(objectPath.lastIndexOf('/') + 1);
            response.setContentType("application/x-download");
            response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(fileName,"UTF-8"));
            out = response.getOutputStream();
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            OSSObject object = ossClient.getObject(bucketName, objectPath);
            log.info("正在下载"+ object);
            in = object.getObjectContent();
            byte[] data = new byte[1024];
            int len = 0;
            while ((len = in.read(data)) != -1) {
                out.write(data, 0, len);
            }
            out.flush();
            object.close();
            ossClient.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // *************管理文件************

    /**
     *  分页列举所有文件
     * @param bucketName
     * @param maxKeys 每页最大个数
     * @return
     */
    public List<OSSObjectSummary> listFilesPaged(String bucketName,int currentPage, int maxKeys) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        String nextContinuationToken = null;
        ListObjectsV2Result result = null;
        List<OSSObjectSummary> sums = null;
        // 分页列举，每次传入上次返回结果中的nextContinuationToken。
        do {
            ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request(bucketName).withMaxKeys(maxKeys);
            listObjectsV2Request.setContinuationToken(nextContinuationToken);
            result = ossClient.listObjectsV2(listObjectsV2Request);

            sums = result.getObjectSummaries();

            nextContinuationToken = result.getNextContinuationToken();
            currentPage--;
        } while (result.isTruncated() && currentPage > 0);

// 关闭OSSClient。
        ossClient.shutdown();
        return sums;
    }

    public List<OSSObjectSummary> listFilesByPrefixPaged(String bucketName,int currentPage, int size, String prefix) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        String nextContinuationToken = null;
        ListObjectsV2Result result = null;
        List<OSSObjectSummary> sums = null;
        // 分页列举指定前缀的文件。
        do {
            ListObjectsV2Request listObjectsV2Request = new ListObjectsV2Request(bucketName).withMaxKeys(size);
            listObjectsV2Request.setPrefix(prefix);
            listObjectsV2Request.setContinuationToken(nextContinuationToken);
            result = ossClient.listObjectsV2(listObjectsV2Request);

            sums = result.getObjectSummaries();

            nextContinuationToken = result.getNextContinuationToken();
            currentPage--;
        } while (currentPage > 0 && result.isTruncated());

// 关闭OSSClient。
        ossClient.shutdown();
        return sums;
    }

    /**
     * 删除文件
     */
    public Boolean deleteFile(String bucketName,String filePath) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
// 删除文件或目录。如果要删除目录，目录必须为空。
        try {
            ossClient.deleteObject(bucketName, filePath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("文件删除失败!"+ e.getMessage());
        }
// 关闭OSSClient。
        ossClient.shutdown();
        return true;
    }

    /**
     * 删除文件夹及其下所有文件
     * @return
     */
    public Boolean deleteDir(String bucketName,String filePath) {
        // 填写不包含Bucket名称在内的目录完整路径。例如Bucket下testdir目录的完整路径为testdir/。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        // 删除目录及目录下的所有文件。
        String nextMarker = null;
        ObjectListing objectListing = null;
        do {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName)
                    .withPrefix(filePath)
                    .withMarker(nextMarker);

            objectListing = ossClient.listObjects(listObjectsRequest);
            if (objectListing.getObjectSummaries().size() > 0) {
                List<String> keys = new ArrayList<String>();
                for (OSSObjectSummary s : objectListing.getObjectSummaries()) {
                    System.out.println("key name: " + s.getKey());
                    keys.add(s.getKey());
                }
                DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName).withKeys(keys);
                DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(deleteObjectsRequest);
                List<String> deletedObjects = deleteObjectsResult.getDeletedObjects();

            }

            nextMarker = objectListing.getNextMarker();
        } while (objectListing.isTruncated());

        ossClient.shutdown();
        return true;
    }


    public String generateUrl(String bucketName,String filePath, Date date) {
        OSS client = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        URL url = client.generatePresignedUrl(bucketName, filePath, date);
        String httpsUrl = url.toString().replace("http", "https");
        log.info(httpsUrl);
        return httpsUrl;
    }

    public String copyObject(String bucketName,String sourcePath, String destPath) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        CopyObjectResult result = ossClient.copyObject(bucketName, sourcePath, bucketName, destPath);
        System.out.println("ETag: " + result.getETag() + " LastModified: " + result.getLastModified());

// 关闭OSSClient。
        ossClient.shutdown();
        return result.getETag();
    }

    public SimplifiedObjectMeta getDetail(String bucketName, String ossPath) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        return ossClient.getSimplifiedObjectMeta(bucketName, ossPath);
    }

    public boolean getFileExist(String bucketName, String ossPath) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        boolean found = ossClient.doesObjectExist(bucketName, ossPath);
        ossClient.shutdown();
        return found;
    }

    public boolean copySameRegionBucketFile(String srcBucket, String srcPath, String destBucket, String destPath) {
        if (!getFileExist(srcBucket, srcPath)) {
            throw new RuntimeException("文件不存在");
        }
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        CopyObjectResult result = ossClient.copyObject(srcBucket, srcPath, destBucket, destPath);
        System.out.println("ETag: " + result.getETag() + " LastModified: " + result.getLastModified());
        ossClient.shutdown();
        return true;
    }


    /**
     * 获取根目录下所有文件大小
     * @param bucketName
     * @return
     */
    public Long getUserUsedSpace(String bucketName){
        try {
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            long sizeTotal = 0L;
            ObjectListing objectListing = null;
            do {
                // 每次列举1000个文件或目录。
                ListObjectsRequest request = new ListObjectsRequest(bucketName).withDelimiter("/").withMaxKeys(1000);
                if (objectListing != null) {
                    request.setMarker(objectListing.getNextMarker());
                }

                objectListing = ossClient.listObjects(request);
                //获取当前文件夹下所有子目录大小
                List<String> folders = objectListing.getCommonPrefixes();
                for (String folder : folders) {
                    sizeTotal = calculateFolderLength(ossClient, bucketName, folder)+sizeTotal;
                }

                //获取当前文件夹下所有文件大小
                List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
                if (sums.size()==0){
                    continue;
                }
                for (OSSObjectSummary s : sums) {
                    sizeTotal = sizeTotal+s.getSize();
                }

            } while (objectListing.isTruncated());
            //转换为TB
//            sizeTotal = sizeTotal/1024/1024/1024/1024;
            log.info(" 文件夹下所有子目录大小 : [{}] bytes",sizeTotal);
            ossClient.shutdown();
            return sizeTotal;
        }catch (Exception e){
            e.printStackTrace();
            return 0L;
        }

    }

    private static long calculateFolderLength(OSS ossClient, String bucketName, String folder) {
        long size = 0L;
        ObjectListing objectListing = null;
        do {
            // MaxKey默认值为100，最大值为1000。
            ListObjectsRequest request = new ListObjectsRequest(bucketName).withPrefix(folder).withMaxKeys(1000);
            if (objectListing != null) {
                request.setMarker(objectListing.getNextMarker());
            }
            objectListing = ossClient.listObjects(request);
            List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
            for (OSSObjectSummary s : sums) {
                size += s.getSize();
            }
        } while (objectListing.isTruncated());
        return size;
    }
}
