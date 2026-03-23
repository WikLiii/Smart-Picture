package com.wjh.smartpicturebackend.model.dto.file;

import lombok.Data;

/**
 * 上传图片的解析结果
 */
@Data
public class UploadPictureResult {


    private String url;
    /**
     * 缩略图 url
     */
    private String thumbnailUrl;


    private String picName;


    private Long picSize;


    private int picWidth;


    private int picHeight;


    private Double picScale;


    private String picFormat;
    /**
     * 主颜色
     */
    private String picColor;

}