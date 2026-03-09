package com.wjh.smartpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wjh.smartpicturebackend.model.dto.user.UserQueryRequest;
import com.wjh.smartpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wjh.smartpicturebackend.model.vo.LoginUserVO;
import com.wjh.smartpicturebackend.model.vo.UserVO;
import org.aspectj.weaver.ast.Var;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author lenovo
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2026-03-03 15:43:01
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */

    boolean userLogout(HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取加密后的密码
     *
     * @param userPasswprd
     * @return
     */
    String getEncryptPassword(String userPasswprd);

    /**
     * 获得脱敏后的登录用户信息
     *
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获得脱敏后的用户信息
     *
     * @param user
     * @return 脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获得脱敏后的用户信息列表
     *
     * @param userList
     * @return 脱敏后的用户信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);


}
