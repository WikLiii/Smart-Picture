package com.wjh.smartpicturebackend.api.imagesearch.sub;

import com.wjh.smartpicturebackend.api.imagesearch.model.ImageSearchResult;
import com.wjh.smartpicturebackend.exception.BusinessException;
import com.wjh.smartpicturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取搜狗图片列表接口（Step 2）
 */
@Slf4j
public class GetSogouImageListApi {

    public static List<ImageSearchResult> getImageList(String searchUrl) {
        try {
            // 发起 Jsoup 请求 (模拟真实浏览器)
            Connection.Response response = Jsoup.connect(searchUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .header("Referer", "https://pic.sogou.com/") // 必须带 Referer
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .ignoreHttpErrors(true)
                    .execute();

            Document doc = response.parse();
            String html = doc.html();

            // 正则提取 window.__INITIAL_STATE__
            Pattern pattern = Pattern.compile("window\\.__INITIAL_STATE__\\s*=\\s*(\\{.*?\\});", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(html);

            if (matcher.find()) {
                String initialState = matcher.group(1);
                // 解析相似图片列表
                return parseSimImages(initialState);
            } else {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未能匹配到结果数据，请确认是否被搜狗防火墙拦截。");
            }
        } catch (Exception e) {
            log.error("获取搜狗图片列表失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片列表失败");
        }
    }

    /**
     * 解析 JSON 字符串中的相似图片信息
     */
    private static List<ImageSearchResult> parseSimImages(String json) {
        List<ImageSearchResult> list = new ArrayList<>();

        // 使用正则匹配每一个 item 对象块：找含有 thumbUrl 的对象块
        Pattern itemPattern = Pattern.compile("\\{[^{}]*\"thumbUrl\":\"[^{}]*\\}");
        Matcher matcher = itemPattern.matcher(json);

        while (matcher.find()) {
            String itemBlock = matcher.group();

            ImageSearchResult imageResult = new ImageSearchResult();
            // 提取缩略图
            imageResult.setThumbUrl(extractFieldValue(itemBlock, "thumbUrl"));

            // 提取原图 (优先取 ori_pic_url，取不到则取 pic_url)
            String oriPic = extractFieldValue(itemBlock, "ori_pic_url");
            if (oriPic == null || oriPic.isEmpty()) {
                oriPic = extractFieldValue(itemBlock, "pic_url");
            }
            imageResult.setFromUrl(oriPic);

            if (imageResult.getThumbUrl() != null) {
                list.add(imageResult);
            }

            // 只取前10个
            if (list.size() >= 24) break;
        }
        return list;
    }

    /**
     * 从 JSON 块中提取特定字段的值，并处理转义
     */
    private static String extractFieldValue(String block, String fieldName) {
        String regex = "\"" + fieldName + "\":\"(.*?)\"";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(block);
        if (m.find()) {
            String value = m.group(1);
            // 处理 Unicode 转义字符 \u002F 以及普通的反斜杠转义
            return decodeUrl(value);
        }
        return null;
    }

    /**
     * URL 解码：处理搜狗 JSON 中常见的转义符
     */
    private static String decodeUrl(String url) {
        if (url == null) return null;
        return url.replace("\\u002F", "/")
                .replace("\\/", "/");
    }
}