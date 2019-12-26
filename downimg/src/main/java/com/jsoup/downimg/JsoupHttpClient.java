package com.jsoup.downimg;

/**
 * @Author: zly
 * @Date: 2019/12/24 14:39
 */

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

@RestController
@Slf4j
public class JsoupHttpClient {
    //自动下载首页图片合集
    @GetMapping("/getPicture")
    public String downloadPicture() throws IOException {
        // 创建httpclient实例
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建httpget实例
        HttpGet httpget = new HttpGet("https://boodo.qq.com/pages/tag.html?name=COSPLAY");

        // 执行get请求
        CloseableHttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        // 获取返回实体
        String content = EntityUtils.toString(entity, "utf-8");

        // 解析网页 得到文档对象
        Document doc = Jsoup.parse(content);
        Elements elements = doc.select("script");
        Element element = elements.get(16);
        ArrayList<Integer> list = new ArrayList<>();
        if (element != null) {
            String dataStr = element.data();
            String[] split = dataStr.split("window.__INITIAL_STATE__=");
            JSONObject jsonObject = JSON.parseObject(split[1]);
            Object commentTargetId = jsonObject.get("page.ugc.tag");
            JSONObject jsonObject1 = JSON.parseObject(commentTargetId.toString());
            Object tabLists = jsonObject1.get("feedLists");
            JSONArray objects = JSONArray.parseArray(tabLists.toString());
            for (Object object : objects) {
                JSONObject jsonObject2 = JSON.parseObject(object.toString());
                Integer id = jsonObject2.getInteger("id");
                list.add(id);
            }

        }

        if (list.size() != 0) {
            for (Integer integer : list) {
                String imgUrl = "https://boodo.qq.com/pages/ugc/detail.html?id=" + integer;
                HttpGet httpget2 = new HttpGet(imgUrl);
                // 执行get请求
                CloseableHttpResponse response2 = httpclient.execute(httpget2);
                HttpEntity entity2 = response2.getEntity();
                // 获取返回实体
                String content2 = EntityUtils.toString(entity2, "utf-8");

                // 解析网页 得到文档对象
                Document doc2 = Jsoup.parse(content2);
                // 获取指定的 <img />
                Elements elements2 = doc2.select(".imgauto");

                if (elements2 != null || elements2.size() != 0) {
                    for (int i = 0; i < elements2.size(); i++) {
                        Element element2 = elements2.get(i);
                        // 获取 <img /> 的 src
                        String url = element2.attr("src");
                        if (url.contains("http")) {
                            System.out.println("请求的图片链接:" + url);
                            // 再发请求最简单了，并由于该链接是没有 https:开头的，得人工补全 ✔
                            HttpGet PicturehttpGet = new HttpGet(url);
                            CloseableHttpResponse pictureResponse = null;
                            try {
                                pictureResponse = httpclient.execute(PicturehttpGet);
                                HttpEntity pictureEntity = pictureResponse.getEntity();
                                InputStream inputStream = pictureEntity.getContent();
                                long timeMillis = System.currentTimeMillis();
                                // 使用 common-io 下载图片到本地，注意图片名不能重复 ✔
                                FileUtils.copyToFile(inputStream, new File("C://LLLLLLLLLLLLLLLLLLL//imagesTest//" + timeMillis + ".jpg"));
                            } catch (Exception e) {
                                continue;
                            } finally {
                                pictureResponse.close(); // pictureResponse关闭
                            }
                        }
                    }
                }
            }
        }

        log.info("图片下载完毕");
        response.close(); // response关闭
        httpclient.close(); // httpClient关闭
        return "图片下载已完成!!!";

    }

    //手动下载输入的ID或者链接
    @GetMapping(value = "/getPicture2")
    public String downloadPicture2(String id) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response2 = null;
        if (id.contains(",")) {
            String[] split = id.split(",");
            for (int i = 0; i < split.length; i++) {
                HttpGet httpget2 = new HttpGet(split[i]);
                // 执行get请求
                response2 = httpclient.execute(httpget2);
                HttpEntity entity2 = response2.getEntity();
                // 获取返回实体
                String content2 = EntityUtils.toString(entity2, "utf-8");

                // 解析网页 得到文档对象
                Document doc2 = Jsoup.parse(content2);
                // 获取指定的 <img />
                Elements elements2 = doc2.select(".imgauto");
                downPicture(httpclient, elements2);
            }
        } else {
            Pattern pattern = Pattern.compile("^-?[0-9]+");
            boolean isNums = pattern.matcher(id).matches();
            if (isNums) {
                String imgUrl = "https://boodo.qq.com/pages/ugc/detail.html?id=" + id;
                HttpGet httpget2 = new HttpGet(imgUrl);
                // 执行get请求
                response2 = httpclient.execute(httpget2);
                HttpEntity entity2 = response2.getEntity();
                // 获取返回实体
                String content2 = EntityUtils.toString(entity2, "utf-8");

                // 解析网页 得到文档对象
                Document doc2 = Jsoup.parse(content2);
                // 获取指定的 <img />
                Elements elements2 = doc2.select(".imgauto");
                downPicture(httpclient, elements2);
            } else {
                HttpGet httpget2 = new HttpGet(id);
                // 执行get请求
                response2 = httpclient.execute(httpget2);
                HttpEntity entity2 = response2.getEntity();
                // 获取返回实体
                String content2 = EntityUtils.toString(entity2, "utf-8");

                // 解析网页 得到文档对象
                Document doc2 = Jsoup.parse(content2);
                // 获取指定的 <img />
                Elements elements2 = doc2.select(".imgauto");
                downPicture(httpclient, elements2);
            }

        }
        response2.close();
        httpclient.close(); // httpClient关闭
        log.info("图片下载完毕");
        return "200";
    }

    //统一封装的图片下载方法
    private void downPicture(CloseableHttpClient httpclient, Elements elements2) throws IOException {
        if (elements2 != null || elements2.size() != 0) {
            for (int i = 0; i < elements2.size(); i++) {
                Element element2 = elements2.get(i);
                // 获取 <img /> 的 src
                String url = element2.attr("src");
                if (url.contains("http")) {
                    System.out.println("请求的图片链接:" + url);
                    // 再发请求最简单了，并由于该链接是没有 https:开头的，得人工补全 ✔
                    HttpGet PicturehttpGet = new HttpGet(url);
                    CloseableHttpResponse pictureResponse = null;
                    try {
                        pictureResponse = httpclient.execute(PicturehttpGet);
                        HttpEntity pictureEntity = pictureResponse.getEntity();
                        InputStream inputStream = pictureEntity.getContent();
                        // 使用 common-io 下载图片到本地，注意图片名不能重复 ✔
                        long time = new Date().getTime();
                        String fileName = time + ".jpg";
                        FileUtils.copyToFile(inputStream, new File("C://down_images//images//" + fileName));
                    } catch (Exception e) {
                        continue;
                    } finally {
                        pictureResponse.close(); // pictureResponse关闭
                    }
                }
            }
        }
    }

    //public  void downFile(HttpServletRequest request, HttpServletResponse response,InputStream fis,String fileName){
    //    BufferedInputStream bis = null;
    //    BufferedOutputStream bos = null;
    //    OutputStream fos = null;
    //    try {
    //        bis = new BufferedInputStream(fis);
    //        fos = response.getOutputStream();
    //        bos = new BufferedOutputStream(fos);
    //        setFileDownloadHeader(request,response, fileName);
    //        int byteRead = 0;
    //        byte[] buffer = new byte[8192];
    //        while((byteRead=bis.read(buffer,0,8192))!=-1){
    //            bos.write(buffer,0,byteRead);
    //        }
    //        bos.flush();
    //        fis.close();
    //        bis.close();
    //        fos.close();
    //        bos.close();
    //    } catch (Exception e) {
    //    }
    //}
    //
    //public static void setFileDownloadHeader(HttpServletRequest request,HttpServletResponse response, String fileName) {
    //    try {
    //        //中文文件名支持
    //        String encodedfileName = null;
    //        String agent = request.getHeader("USER-AGENT");
    //
    //        if(null != agent && -1 != agent.indexOf("MSIE")){//IE
    //            encodedfileName = java.net.URLEncoder.encode(fileName,"UTF-8");
    //        }else if(null != agent && -1 != agent.indexOf("Mozilla")){
    //            encodedfileName = new String (fileName.getBytes("UTF-8"),"iso-8859-1");
    //        }else{
    //            encodedfileName = java.net.URLEncoder.encode(fileName,"UTF-8");
    //        }
    //        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedfileName + "\"");
    //    } catch (UnsupportedEncodingException e) {
    //        e.printStackTrace();
    //    }
    //}


}
