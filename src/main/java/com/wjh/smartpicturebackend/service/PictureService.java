package com.wjh.smartpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wjh.smartpicturebackend.exception.BusinessException;
import com.wjh.smartpicturebackend.exception.ErrorCode;
import com.wjh.smartpicturebackend.model.dto.picture.*;
import com.wjh.smartpicturebackend.model.dto.user.UserQueryRequest;
import com.wjh.smartpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wjh.smartpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;
import com.wjh.smartpicturebackend.model.entity.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author lenovo
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2026-03-09 23:00:05
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     * @param inputSource
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);



    /**
     * 获取查询条件
     *
     * @param pictureQueryRequest
     * @return
     */

    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片包装类（单条）
     * @param picture
     * @param request
     * @return
     */

    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 分页获取图片封装
     * @param picturePage
     * @param request
     * @return
     */

    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 图片信息校验
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 设置审核参数（管理员自动审核通过，用户需设置为待审核）
     *
     * @param picture
     * @param loginUser
     */

    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取图片
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return
     */
    Integer uploadPictureByBatch(
            PictureUploadByBatchRequest pictureUploadByBatchRequest,
            User loginUser
    );

    /**
     * 数据清理
     * @param oldPicture
     */
    void clearPictureFile(Picture oldPicture);


    /**
     * 校验空间图片的权限
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser, Picture picture) ;

    /**
     * 删除图片
     * @param pictureId
     * @param loginUser
     */

    void deletePicture(long pictureId, User loginUser);

    /**
     * 编辑图片
     * @param pictureEditRequest
     * @param loginUser
     */

    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 以颜色搜图
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */

    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);
}
