package com.wjh.smartpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wjh.smartpicturebackend.exception.BusinessException;
import com.wjh.smartpicturebackend.exception.ErrorCode;
import com.wjh.smartpicturebackend.exception.ThrowUtils;
import com.wjh.smartpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.wjh.smartpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.wjh.smartpicturebackend.model.entity.Space;
import com.wjh.smartpicturebackend.model.entity.SpaceUser;
import com.wjh.smartpicturebackend.model.entity.User;
import com.wjh.smartpicturebackend.model.enums.SpaceRoleEnum;
import com.wjh.smartpicturebackend.model.vo.SpaceUserVO;
import com.wjh.smartpicturebackend.model.vo.SpaceVO;
import com.wjh.smartpicturebackend.model.vo.UserVO;
import com.wjh.smartpicturebackend.service.SpaceService;
import com.wjh.smartpicturebackend.service.SpaceUserService;
import com.wjh.smartpicturebackend.mapper.SpaceUserMapper;
import com.wjh.smartpicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author lenovo
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
* @createDate 2026-03-29 20:53:43
*/
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService {
    
    
    @Resource
    private UserService userService;
    @Resource
    @Lazy
    private SpaceService spaceService;
    /**
     * 创建空间成员
     *
     * @param spaceUserAddRequest
     * @return
     */
    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        //参数校验
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest,spaceUser);
        validSpaceUser(spaceUser,true);
        //数据库操作
        boolean result = this.save(spaceUser);
        ThrowUtils.throwIf(!result,ErrorCode.OPERATION_ERROR);
        return spaceUser.getUserId();

    }
    /**
     * 校验空间成员
     *
     * @param spaceUser
     * @param add       是否为创建时检验
     */
    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        ThrowUtils.throwIf(spaceUser == null,ErrorCode.PARAMS_ERROR);
        //创建时，空间id和用户id必填

        Long userId = spaceUser.getUserId();
        Long spaceId = spaceUser.getSpaceId();
        if(add){
            ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId,userId),ErrorCode.PARAMS_ERROR);
            User user = userService.getById(userId);
            ThrowUtils.throwIf(user == null,ErrorCode.NOT_FOUND_ERROR,"用户不存在");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null,ErrorCode.NOT_FOUND_ERROR,"空间不存在");
        }
        //校验空间角色
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        if(spaceRole != null && spaceRoleEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"空间角色不存在");
        }

    }
    /**
     * 获取空间成员包装类（单条）
     *
     * @param spaceUser
     * @param request
     * @return
     */

    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
        //对象转封装类
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        //关联查询用户信息
        Long userId = spaceUser.getUserId();
        if(userId != null && userId > 0){
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceUserVO.setUser(userVO);
        }
        //关联查询空间信息
        Long spaceId = spaceUser.getSpaceId();
        if(spaceId != null && spaceId > 0){
            Space space = spaceService.getById(spaceId);
            SpaceVO spaceVO = spaceService.getSpaceVO(space,request);
            spaceUserVO.setSpace(spaceVO);
        }

        return spaceUserVO;
    }
    /**
     * 获取空间成员包装类（列表）
     *
     * @param spaceUserList
     * @return
     */

    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        //判断输入列表是否为空
        if (CollUtil.isEmpty(spaceUserList)){
            return Collections.emptyList();
        }

        //对象列表->封装对象列表
        List<SpaceUserVO> spaceUserVOList = spaceUserList.stream().map(SpaceUserVO::objToVo).collect(Collectors.toList());
        //1.收集需要关联的用户id和空间id
        Set<Long> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        Set<Long> spaceIdSet = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());
        //2.批量查询用户和空间
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        Map<Long, List<Space>> spaceIdSpaceListMap = spaceService.listByIds(spaceIdSet).stream()
                .collect(Collectors.groupingBy(Space::getId));
        //3.填充SpaceUserVO的用户和空间信息
        spaceUserVOList.forEach(spaceUserVO -> {
            Long userId = spaceUserVO.getUserId();
            Long spaceId = spaceUserVO.getSpaceId();
            //填充用户信息
            User user = null;
            if(userIdUserListMap.containsKey(userId)){
                 user = userIdUserListMap.get(userId).get(0);
            }
            spaceUserVO.setUser(userService.getUserVO(user));
            //填充空间信息
            Space space = null;
            if(spaceIdSpaceListMap.containsKey(spaceId)){
                space = spaceIdSpaceListMap.get(spaceId).get(0);
            }
            spaceUserVO.setSpace(SpaceVO.objToVo(space));
        });
        return spaceUserVOList;
    }
    /**
     * 获取查询对象
     *
     * @param spaceUserQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        if (spaceUserQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceRole), "spaceRole", spaceRole);
        return queryWrapper;
    }

}




