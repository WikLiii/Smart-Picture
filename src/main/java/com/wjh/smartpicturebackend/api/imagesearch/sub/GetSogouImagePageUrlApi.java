package com.wjh.smartpicturebackend.api.imagesearch.sub;

import com.wjh.smartpicturebackend.exception.BusinessException;
import com.wjh.smartpicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 获取搜狗以图搜图页面地址（Step 1）
 */
@Slf4j
public class GetSogouImagePageUrlApi {

    public static String getImagePageUrl(String imageUrl) {
        try {
            // 1. 构造搜狗识图 CDN 中转 URL
            String sogouCdnUrl = "https://img04.sogoucdn.com/v2/thumb/retype_exclude_gif/ext/auto?appid=122&url="
                    + URLEncoder.encode(imageUrl, StandardCharsets.UTF_8.name());

            // 2. 构造最终搜索 URL
            return "https://ris.sogou.com/ris?query="
                    + URLEncoder.encode(sogouCdnUrl, StandardCharsets.UTF_8.name())
                    + "&flag=1&drag=1";
        } catch (Exception e) {
            log.error("构造搜狗识图页面地址失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "构造搜狗识图页面地址失败");
        }
    }
}