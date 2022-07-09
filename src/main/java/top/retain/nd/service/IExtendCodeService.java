package top.retain.nd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.retain.nd.common.CommonResult;
import top.retain.nd.entity.ExtendCode;

public interface IExtendCodeService extends IService<ExtendCode> {
    CommonResult match(String account, String code);

    CommonResult add(ExtendCode code);
}
