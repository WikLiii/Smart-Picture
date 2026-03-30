package com.wjh.smartpicturebackend.controller;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wjh.smartpicturebackend.annotation.AuthCheck;
import com.wjh.smartpicturebackend.common.BaseResponse;
import com.wjh.smartpicturebackend.common.DeleteRequest;
import com.wjh.smartpicturebackend.common.ResultUtils;
import com.wjh.smartpicturebackend.constant.UserConstant;
import com.wjh.smartpicturebackend.exception.BusinessException;
import com.wjh.smartpicturebackend.exception.ErrorCode;
import com.wjh.smartpicturebackend.exception.ThrowUtils;
import com.wjh.smartpicturebackend.manager.auth.SpaceUserAuthManager;
import com.wjh.smartpicturebackend.model.dto.space.*;
import com.wjh.smartpicturebackend.model.entity.Space;
import com.wjh.smartpicturebackend.model.entity.User;
import com.wjh.smartpicturebackend.model.enums.SpaceLevelEnum;
import com.wjh.smartpicturebackend.model.vo.SpaceVO;
import com.wjh.smartpicturebackend.service.SpaceService;
import com.wjh.smartpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/space")
@Slf4j
public class SpaceController {
    @Resource
    private UserService userService;
    @Resource
    private SpaceService spaceService;
    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;


    /**
     * 创建空间
     * @param spaceAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest,HttpServletRequest request){
        ThrowUtils.throwIf(spaceAddRequest == null ,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long newId = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(newId);
    }


    /**
     * 根据id删除空间
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request){
        if(deleteRequest == null || deleteRequest.getId() <= 0){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Long id = deleteRequest.getId();
        Space oldspace = spaceService.getById(id);
        ThrowUtils.throwIf(oldspace == null,ErrorCode.NOT_FOUND_ERROR);
        //权限校验（仅本人和管理员可以删除）
        if (!oldspace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = spaceService.removeById(id);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);

        return ResultUtils.success(true);
    }

    /**
     * 更新空间（仅管理员可用）
     * @param spaceUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        if(spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest,space);
        //自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        spaceService.validSpace(space,false);
        Long id = spaceUpdateRequest.getId();
        Space oldspace = spaceService.getById(id);
        ThrowUtils.throwIf(oldspace == null,ErrorCode.NOT_FOUND_ERROR);
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result ,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据id获取空间（仅管理员可用）
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Space> getSpaceById(long id , HttpServletRequest request){

        ThrowUtils.throwIf(id <= 0 ,ErrorCode.PARAMS_ERROR);
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null,ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(space);
    }

    /**
     * 根据id获取空间（封装类）
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request){

        ThrowUtils.throwIf(id <= 0 ,ErrorCode.PARAMS_ERROR);

        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null,ErrorCode.NOT_FOUND_ERROR);
        SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
        User loginUser = userService.getLoginUser(request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        spaceVO.setPermissionList(permissionList);
        return ResultUtils.success(spaceVO);
    }

    /**
     * 分页获取空间列表（仅管理员可用）
     * @param spaceQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest){
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);


    }

    /**
     * 分页获取空间列表（封装类）
     * @param spaceQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
                                                             HttpServletRequest request) {
        long current = spaceQueryRequest.getCurrent();
        long size = spaceQueryRequest.getPageSize();
        //限制爬虫

        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        //查询数据库
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));

        return ResultUtils.success(spaceService.getSpaceVOPage(spacePage, request));
    }
    /**
     * 编辑空间
     * @param spaceEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest
            ,HttpServletRequest request){

        if(spaceEditRequest == null || spaceEditRequest.getId() <= 0){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest,space);

        //自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        space.setEditTime(new Date());
        spaceService.validSpace(space,false);
        User loginUser = userService.getLoginUser(request);
        long id = spaceEditRequest.getId();
        Space oldspace = spaceService.getById(id);
        ThrowUtils.throwIf(oldspace == null,ErrorCode.NOT_FOUND_ERROR);

        //权限校验（仅本人和管理员可以删除）
        if (!oldspace.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }


        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result ,ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()))
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }

}


