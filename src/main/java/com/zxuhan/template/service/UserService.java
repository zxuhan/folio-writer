package com.zxuhan.template.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.zxuhan.template.model.dto.user.UserQueryRequest;
import com.zxuhan.template.model.entity.User;
import com.zxuhan.template.model.vo.LoginUserVO;
import com.zxuhan.template.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * User service layer.
 */
public interface UserService extends IService<User> {

    /**
     * User registration
     *
     * @param userAccount   user account
     * @param userPassword  user password
     * @param checkPassword confirm password
     * @return new user ID
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * Get desensitized info for the currently logged-in user
     *
     * @return LoginUserVO
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * User login
     *
     * @param userAccount  user account
     * @param userPassword user password
     * @param request      HTTP request
     * @return desensitized user info
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * Get the currently logged-in user
     *
     * @param request HTTP request
     * @return current user
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * Get desensitized user info
     *
     * @param user user entity
     * @return UserVO
     */
    UserVO getUserVO(User user);

    /**
     * Get desensitized user info list (paginated)
     *
     * @param userList list of users
     * @return list of UserVOs
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * User logout
     *
     * @param request HTTP request
     * @return whether logout succeeded
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * Build query wrapper from query request
     *
     * @param userQueryRequest query request
     * @return QueryWrapper
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * Encrypt password
     *
     * @param userPassword plain-text password
     * @return encrypted password
     */
    String getEncryptPassword(String userPassword);
}
