package com.wjh.smartpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wjh.smartpicturebackend.model.dto.picture.PictureQueryRequest;
import com.wjh.smartpicturebackend.model.dto.picture.PictureUploadRequest;
import com.wjh.smartpicturebackend.model.dto.user.UserQueryRequest;
import com.wjh.smartpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wjh.smartpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;
import com.wjh.smartpicturebackend.model.entity.User;

import javax.servlet.http.HttpServletRequest;

/**
 * @author lenovo
 * @description 针对表【picture(图片)】的数据库操作Service
 * @createDate 2026-03-09 23:00:05
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
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
}
