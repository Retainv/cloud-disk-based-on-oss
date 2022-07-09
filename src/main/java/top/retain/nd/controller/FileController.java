package top.retain.nd.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import top.retain.nd.common.CommonResult;
import top.retain.nd.common.CommonResultTool;
import top.retain.nd.entity.UserFile;
import top.retain.nd.service.IUserService;
import top.retain.nd.service.impl.FileService;
import top.retain.nd.util.SpringSecurityUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.nio.file.FileAlreadyExistsException;
import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Retain
 * @date 2021/9/29 21:38
 */
@RestController
@RequestMapping("/file")
@Api("文件类接口")
@Slf4j
public class FileController {


    @Value("${OSS.accessKeyId}")
    private String accessKeyId;
    @Value("${OSS.accessKeySecret}")
    private String accessKeySecret;
    @Resource
    private FileService fileService;

    @Resource
    private IUserService userService;


    @GetMapping("/oss")
    @Deprecated()
    public CommonResult getOssSecrect(@ApiIgnore HttpSession session) {
        Map<String, String> map = new HashMap<>();
        map.put("accessKeyId", accessKeyId);
        map.put("accessKeySecret", accessKeySecret);
        String account = SpringSecurityUtil.currentUser(session);
        String bucketName = userService.getUserBucket(account);
        map.put("bucketName", bucketName);
        map.put("region", "oss-cn-chengdu");
        return CommonResultTool.success(map);
    }

    @RequestMapping(value = "/username", method = RequestMethod.GET)
    @ResponseBody
    public String currentUserName(Principal principal) {
        return principal.getName();
    }

//    @PostMapping("/upload")
//    public CommonResult upload(HttpServletRequest request,
//                               @PathVariable("account") String account,
//                               @RequestParam(value = "file", required = false)  MultipartFile file,
//                               @RequestParam(value = "md5", required = false) String md5) throws IOException {
//
//    }

    @PostMapping("/download")
    public CommonResult download(@ApiIgnore HttpSession session
                                ,@RequestParam() String objectPath
                                 ) {
        String account = SpringSecurityUtil.currentUser(session);
//        fileService.streamDownload(account,objectPath, response);
        return CommonResultTool.success(fileService.browserDownload(account, objectPath));
    }

    /**
     * 获取文件夹所有文件，返回前端进行下载打包
     * @param session
     * @param objectPath
     * @return
     */
    @PostMapping("/downloadDir")
    public CommonResult downloadDir(@ApiIgnore HttpSession session,
                                     String shareId
            ,@RequestParam() String objectPath
    ) {
        String account = SpringSecurityUtil.currentUser(session);
        if (shareId == null) {
            account = SpringSecurityUtil.currentUser(session);
        }
        return CommonResultTool.success(fileService.downloadDir(account, shareId, objectPath));
    }


    @PostMapping("/list")
    @ApiOperation(value = "分页查询")
    @Deprecated
    public CommonResult listFilesPaged(@RequestParam(defaultValue = "1") Integer currentPage
                                       ,@RequestParam(defaultValue = "20") Integer size
                                        ,@ApiIgnore HttpSession session)  {
        String account = SpringSecurityUtil.currentUser(session);
        return CommonResultTool.success(fileService.listFilesPaged(account,currentPage, size));
    }

    @GetMapping("/listByPrefix")
    @ApiOperation("分页查询前缀路径的文件")
    @Deprecated
    public CommonResult listFileByPrefixPaged(@RequestParam(defaultValue = "1") Integer currentPage
                                                ,@RequestParam(defaultValue = "20") Integer size
                                              ,@RequestParam(defaultValue = "") String prefix
                                                ,@ApiIgnore HttpSession session) {
        String account = SpringSecurityUtil.currentUser(session);
        return CommonResultTool.success(fileService.listFilesByPrefixPaged(account,currentPage, size, prefix));
    }
    @PostMapping("/delete")
    @ApiOperation(value = "删除文件")
    public CommonResult deleteFile(@ApiIgnore HttpSession session
            ,@RequestParam(defaultValue = "") String filePath)  {
        String account = SpringSecurityUtil.currentUser(session);
        return CommonResultTool.success(fileService.deleteSingleFile(account, filePath));
    }

    @PostMapping("/deleteDir")
    @ApiOperation(value = "删除文件夹")
    public CommonResult deleteDir(@ApiIgnore HttpSession session
            ,@RequestParam(defaultValue = "") String filePath)  {

        String account = SpringSecurityUtil.currentUser(session);
        return CommonResultTool.success(fileService.deleteDir(account, filePath));
    }


    @PostMapping("/uploadFile")
    @ApiOperation(value = "非上传文件,保存文件信息, prefix以 / 结尾, 无prefix为空")
    public CommonResult uploadFile(@RequestParam Long userId,
            @RequestBody UserFile file)  {
        log.info(String.valueOf(file));
        fileService.saveUploadFile(userId,file);
        return CommonResultTool.success();
    }
    @PostMapping("/createDir")
    @ApiOperation(value = "创建新文件夹")
    public CommonResult createDir(@ApiIgnore HttpSession session,
                                   @RequestParam String prefix,
                                  @RequestParam String dirName) throws FileAlreadyExistsException {
        String account = SpringSecurityUtil.currentUser(session);
        return CommonResultTool.success(fileService.createDir(account,prefix, dirName));
    }
    @GetMapping("/list")
    public CommonResult list(@RequestParam(defaultValue = "") String prefix
            ,@ApiIgnore HttpSession session) {
        String account = SpringSecurityUtil.currentUser(session);
        List<UserFile> userFiles = fileService.listFilesByPrefix(account, prefix);
        return CommonResultTool.success(userFiles);
    }

    @GetMapping("/url")
    public CommonResult generateUrl(@RequestParam("ossPath") String ossPath,
                                    @RequestParam("expireTime") Long expireTime
            ,@ApiIgnore HttpSession session) {
        String account = SpringSecurityUtil.currentUser(session);
        Date date = new Date(expireTime);
        return CommonResultTool.success(fileService.generateUrl(account, ossPath, date));
    }

    @GetMapping("/share/qr")
    public CommonResult shareQr(@RequestParam("shareUrl") String shareUrl
            ,@ApiIgnore HttpSession session) {
        return CommonResultTool.success(fileService.createQr(shareUrl));
    }
    @PostMapping("/rename")
    public CommonResult rename(@RequestParam("newName") String newName,
                               @RequestParam("ossPath") String ossPath
            ,@ApiIgnore HttpSession session) {
        String account = SpringSecurityUtil.currentUser(session);
        return CommonResultTool.success(fileService.rename(account, ossPath, newName));
    }

    @PostMapping("/bin")
    public CommonResult moveToBin(@RequestParam("ossPath") String ossPath
            ,@ApiIgnore HttpSession session) {
        String account = SpringSecurityUtil.currentUser(session);
        return CommonResultTool.success(fileService.moveToBin(account, ossPath));
    }

    @GetMapping("/detail")
    public CommonResult detail(@RequestParam("ossPath") String ossPath
            ,@ApiIgnore HttpSession session) {
        String account = SpringSecurityUtil.currentUser(session);
        return CommonResultTool.success(fileService.detail(account,ossPath));
    }

    @GetMapping("/list/bin")
    @ApiOperation("查询回收站文件")
    public CommonResult listBinFiles(@ApiIgnore HttpSession session) {
        String account = SpringSecurityUtil.currentUser(session);
        return CommonResultTool.success(fileService.listBinFiles(account));
    }

    @PostMapping("/clear")
    @ApiOperation("清空回收站文件")
    public CommonResult clearBinFiles(@ApiIgnore HttpSession session) {
        String account = SpringSecurityUtil.currentUser(session);
        return CommonResultTool.success(fileService.clearBinFiles(account));
    }

    @PostMapping("/withdraw")
    @ApiOperation("移出回收站")
    public CommonResult withdrawBinFile(@ApiIgnore HttpSession session,
                                        @RequestParam String ossPath) {
        String account = SpringSecurityUtil.currentUser(session);
        return CommonResultTool.success(fileService.withdrawBinFile(account, ossPath));
    }


    @GetMapping("/direct-url")
    @ApiOperation("获取预览地址，和分享地址不同")
    public CommonResult getPreviewUrl(@RequestParam("ossPath") String ossPath,
                                    @RequestParam("expireTime") Long expireTime
            ,@ApiIgnore HttpSession session) {
        String account = SpringSecurityUtil.currentUser(session);
        Date date = new Date(expireTime);
        return CommonResultTool.success(fileService.generatePreviewUrl(account, ossPath, date));
    }

    @GetMapping("/exist")
    @ApiOperation("文件是否存在")
    public CommonResult doesFileExists(@RequestParam("filePath") String filePath
            ,@ApiIgnore HttpSession session) {
        String account = SpringSecurityUtil.currentUser(session);
        return CommonResultTool.success(fileService.doesFileExists(account, filePath));
    }

    @GetMapping("/space")
    @ApiOperation("获取用户空间容量")
    public CommonResult getUserFileSpace(@ApiIgnore HttpSession session) {
        String account = SpringSecurityUtil.currentUser(session);
        return CommonResultTool.success(fileService.getUserFileSpace(account));
    }
}
