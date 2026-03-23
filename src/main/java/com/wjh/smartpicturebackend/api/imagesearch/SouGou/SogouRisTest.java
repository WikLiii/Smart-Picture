package com.wjh.smartpicturebackend.api.imagesearch.SouGou;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 搜狗识图抓取测试类
 */
public class SogouRisTest {

    // 内部类，用于封装抓取结果
    public static class ImageResult {
        private String thumbUrl;  // 缩略图
        private String oriPicUrl; // 原图

        public String getThumbUrl() { return thumbUrl; }
        public void setThumbUrl(String thumbUrl) { this.thumbUrl = thumbUrl; }
        public String getOriPicUrl() { return oriPicUrl; }
        public void setOriPicUrl(String oriPicUrl) { this.oriPicUrl = oriPicUrl; }

        @Override
        public String toString() {
            return "【图片节点】\n原图: " + oriPicUrl + "\n缩略图: " + thumbUrl + "\n";
        }
    }

    public static void main(String[] args) {
        // 1. 你要搜索的原始图片 URL
        String targetImageUrl = "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png";

        try {
            System.out.println("--- 开始识图流程 ---");

            // 2. 构造搜狗识图 CDN 中转 URL
            String sogouCdnUrl = "https://img04.sogoucdn.com/v2/thumb/retype_exclude_gif/ext/auto?appid=122&url="
                    + URLEncoder.encode(targetImageUrl, StandardCharsets.UTF_8.name());

            // 3. 构造最终搜索 URL
            String finalSearchUrl = "https://ris.sogou.com/ris?query="
                    + URLEncoder.encode(sogouCdnUrl, StandardCharsets.UTF_8.name())
                    + "&flag=1&drag=1";

            // 4. 发起 Jsoup 请求 (关键：模拟真实浏览器)
            Connection.Response response = Jsoup.connect(finalSearchUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .header("Referer", "https://pic.sogou.com/") // 必须带 Referer
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .ignoreHttpErrors(true)
                    .execute();

            Document doc = response.parse();
            String html = doc.html();

            // 5. 正则提取 window.__INITIAL_STATE__
            Pattern pattern = Pattern.compile("window\\.__INITIAL_STATE__\\s*=\\s*(\\{.*?\\});", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(html);

            if (matcher.find()) {
                String initialState = matcher.group(1);
                System.out.println("数据状态提取成功，准备解析...");

                // 6. 解析相似图片列表
                List<ImageResult> results = parseSimImages(initialState);

                // 打印结果
                for (int i = 0; i < results.size(); i++) {
                    System.out.println("结果 [" + (i + 1) + "]:");
                    System.out.println(results.get(i));
                }

            } else {
                System.err.println("未能匹配到结果数据，请确认是否被搜狗防火墙拦截。");
            }

        } catch (Exception e) {
            System.err.println("程序异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 解析 JSON 字符串中的相似图片信息
     */
    private static List<ImageResult> parseSimImages(String json) {
        List<ImageResult> list = new ArrayList<>();

        // 使用正则匹配每一个 item 对象块
        // 匹配逻辑：找含有 thumbUrl 的对象块
        Pattern itemPattern = Pattern.compile("\\{[^{}]*\"thumbUrl\":\"[^{}]*\\}");
        Matcher matcher = itemPattern.matcher(json);

        while (matcher.find()) {
            String itemBlock = matcher.group();

            ImageResult imageResult = new ImageResult();
            // 提取缩略图
            imageResult.setThumbUrl(extractFieldValue(itemBlock, "thumbUrl"));
            // 提取原图 (优先取 ori_pic_url，取不到则取 pic_url)
            String oriPic = extractFieldValue(itemBlock, "ori_pic_url");
            if (oriPic == null || oriPic.isEmpty()) {
                oriPic = extractFieldValue(itemBlock, "pic_url");
            }
            imageResult.setOriPicUrl(oriPic);

            if (imageResult.getThumbUrl() != null) {
                list.add(imageResult);
            }

            if (list.size() >= 10) break; // 只取前10个
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