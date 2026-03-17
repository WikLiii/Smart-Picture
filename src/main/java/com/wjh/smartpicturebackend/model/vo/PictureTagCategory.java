package com.wjh.smartpicturebackend.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 图片标签分页列表视图
 */
@Data
public class PictureTagCategory {
    /**
     * 标签列表
     */
    private List<String> tagList;
    /**
     * 分类列表
     */
    private List<String> categoryList;

}
