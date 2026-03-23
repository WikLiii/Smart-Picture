package com.wjh.smartpicturebackend.api.imagesearch;

import com.wjh.smartpicturebackend.api.imagesearch.model.ImageSearchResult;
import com.wjh.smartpicturebackend.api.imagesearch.sub.GetSogouImageListApi;
import com.wjh.smartpicturebackend.api.imagesearch.sub.GetSogouImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 搜狗以图搜图 Facade 门面
 */
@Slf4j
public class SogouImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param imageUrl 原始图片 URL
     * @return 相似图片结果列表
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        // Step 1: 构造请求 URL
        String imagePageUrl = GetSogouImagePageUrlApi.getImagePageUrl(imageUrl);

        // Step 2: 抓取页面并解析结果
        return GetSogouImageListApi.getImageList(imagePageUrl);
    }

    public static void main(String[] args) {
        // 使用你代码中指定的图片测试
        String imageUrl = "https://smart-picture-1394862407.cos.ap-beijing.myqcloud.com/space/2035256642648768514/2026-03-22_SbKhKq9HHjuulDVK.webp";
        List<ImageSearchResult> resultList = searchImage(imageUrl);

        System.out.println("--- 搜狗识图结果列表 ---");
        for (int i = 0; i < resultList.size(); i++) {
            System.out.println("结果 [" + (i + 1) + "]:");
            System.out.println("原图: " + resultList.get(i).getFromUrl());
            System.out.println("缩略图: " + resultList.get(i).getThumbUrl() + "\n");
        }
    }
}