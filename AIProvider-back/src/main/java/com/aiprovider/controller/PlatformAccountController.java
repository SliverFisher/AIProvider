package com.aiprovider.controller;

import com.aiprovider.common.Result;
import com.aiprovider.model.dto.*;
import com.aiprovider.model.vo.*;
import com.aiprovider.service.PlatformAccountService;
import com.aiprovider.service.PlatformAccountLoginService;
import com.aiprovider.service.PlatformAccountValidationService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/platform-accounts")
public class PlatformAccountController {
    private final PlatformAccountService service;
    private final PlatformAccountLoginService loginService;
    private final PlatformAccountValidationService validationService;
    public PlatformAccountController(PlatformAccountService service,PlatformAccountLoginService loginService,PlatformAccountValidationService validationService){this.service=service;this.loginService=loginService;this.validationService=validationService;}
    @GetMapping public Result<PlatformAccountPageVO> page(@RequestParam(required=false)String query,@RequestParam(required=false)String platform,@RequestParam(required=false)String accountKind,@RequestParam(required=false)String status,@RequestParam(defaultValue="1")int page,@RequestParam(defaultValue="30")int pageSize){return Result.success(service.page(query,platform,accountKind,status,page,pageSize));}
    @GetMapping("/{id}") public Result<PlatformAccountVO> get(@PathVariable long id){return Result.success(service.get(id));}
    @PostMapping public Result<PlatformAccountVO> create(@RequestBody PlatformAccountCreateDTO dto){return Result.success(service.create(dto));}
    @PutMapping("/{id}") public Result<PlatformAccountVO> update(@PathVariable long id,@RequestBody PlatformAccountUpdateDTO dto){return Result.success(service.update(id,dto));}
    @PutMapping("/{id}/secrets/{type}") public Result<PlatformAccountVO> secret(@PathVariable long id,@PathVariable String type,@RequestBody PlatformSecretUpdateDTO dto){return Result.success(service.updateSecret(id,type,dto));}
    @GetMapping("/{id}/usages") public Result<List<PlatformAccountUsageVO>> usages(@PathVariable long id){return Result.success(service.usages(id));}
    @PostMapping("/{id}/login") public Result<PlatformLoginSessionVO> login(@PathVariable long id){return Result.success(loginService.start(id));}
    @GetMapping("/{id}/login/{sessionId}") public Result<PlatformLoginSessionVO> poll(@PathVariable long id,@PathVariable String sessionId){return Result.success(loginService.poll(id,sessionId));}
    @PostMapping("/{id}/validate") public Result<PlatformAccountVO> validate(@PathVariable long id){return Result.success(validationService.validate(id));}
    @DeleteMapping("/{id}") public Result<Void> archive(@PathVariable long id){service.archive(id);return Result.success(null);}
}
