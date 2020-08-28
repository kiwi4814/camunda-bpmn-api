

package me.corningrey.camunda.api.util;

import com.alibaba.fastjson.JSONObject;
import me.corningrey.camunda.api.model.UnitedLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 类HttpClientUtil.java
 */
public class HttpClientUtil {

    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static SSLConnectionSocketFactory sslsf = null;
    private static PoolingHttpClientConnectionManager cm = null;
    private static SSLContextBuilder builder = null;

    static {
        try {
            builder = new SSLContextBuilder();
            // Trust all certificaties
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;
                }
            });
            sslsf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register(HTTP, new PlainConnectionSocketFactory())
                    .register(HTTPS, sslsf)
                    .build();
            cm = new PoolingHttpClientConnectionManager(registry);
            cm.setMaxTotal(500);//max connection
        } catch (Exception e) {
            UnitedLogger.error(e);
        }
    }

    public static CloseableHttpClient getHttpClient() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setConnectionManager(cm)
                .setConnectionManagerShared(true)
                .build();
        return httpClient;
    }

    /**
     * httpClient post请求 返回String数据
     *
     * @param url
     * @param params
     * @return
     */
    public static String methodPost(String url, Map<String, String> params, Map<String, String> headers) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        CloseableHttpClient httpclient = getHttpClient();
        HttpResponse response = null;
        HttpPost httpPost = null;
        String result = null;
        try {
            URL url2 = new URL(url);
            URI uri = new URI(url2.getProtocol(), url2.getAuthority(), url2.getPath(), url2.getQuery(), null);
//            httpclient = HttpClientBuilder.create().build();
            httpPost = new HttpPost(uri);
            // 参数处理
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            if (params != null && !params.isEmpty()) {
                Iterator<Entry<String, String>> itor = params.entrySet().iterator();
                while (itor.hasNext()) {
                    Entry<String, String> en = itor.next();
                    nvps.add(new BasicNameValuePair(en.getKey(), en.getValue()));
                }
            }
            // 设置header
            if (headers != null && !headers.isEmpty()) {
                for (Entry<String, String> entry : headers.entrySet()) {
                    httpPost.addHeader(entry.getKey(), entry.getValue());
                }
            }
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));
            } catch (Exception e) {
                UnitedLogger.error(e);
                return null;
            }
            // 请求数据
            response = httpclient.execute(httpPost);
            if (response == null) {
                return null;
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } catch (ParseException e) {
            UnitedLogger.error(e);
            return null;
        } catch (IOException e) {
            UnitedLogger.error(e);
            return null;
        } catch (URISyntaxException e1) {
            UnitedLogger.error(e1);
            return null;
        } finally {
            // 关闭连接,释放资源
            if (httpclient != null) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    UnitedLogger.error(e);
                }
            }
        }
        return result;
    }

    /**
     * httpClient　get请求、设置header 返回String数据
     *
     * @param url
     * @return
     */
    public static String methodGet(String url, Map<String, String> params, Map<String, String> headers) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        String result = null;
        CloseableHttpClient httpclient = getHttpClient();
        try {
            // 设置请求参数
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> pairs = new ArrayList<NameValuePair>(params.size());
                for (Entry<String, String> entry : params.entrySet()) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }

                if (url.indexOf("?") != -1) {
                    url += "&" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, "UTF-8"));
                } else {
                    url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, "UTF-8"));
                }
            }
            URL url2 = new URL(url);
            URI uri = new URI(url2.getProtocol(), url2.getAuthority(), url2.getPath(), url2.getQuery(), null);

            // 创建httpget
            HttpGet httpget = new HttpGet(uri);
            if (headers != null && !headers.isEmpty()) {
                for (Entry<String, String> entry : headers.entrySet()) {
                    httpget.addHeader(entry.getKey(), entry.getValue());
                }
            }
            // 执行get请求.
            HttpResponse response = httpclient.execute(httpget);
            if (response == null) {
                return null;
            }
            // 获取响应实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            UnitedLogger.error(e);
            return null;
        } finally {
            // 关闭连接,释放资源
            if (httpclient != null) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    UnitedLogger.error(e);
                }
            }
        }
        return result;
    }

    /**
     * httpClient　get请求 返回String数据
     *
     * @param url
     * @return
     */
    public static String methodGet(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        String result = null;
        CloseableHttpClient httpclient = getHttpClient();
        try {
            URL url2 = new URL(url);
            URI uri = new URI(url2.getProtocol(), url2.getAuthority(), url2.getPath(), url2.getQuery(), null);
            // 创建httpget
            HttpGet httpget = new HttpGet(uri);
            // 执行get请求.
            HttpResponse response = httpclient.execute(httpget);
            if (response == null) {
                return null;
            }
            // 获取响应实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            UnitedLogger.error(e);
        } finally {
            // 关闭连接,释放资源
            if (httpclient != null) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    UnitedLogger.error(e);
                }
            }
        }
        return result;
    }

    /**
     * httpClient　get请求、设置header 返回String数据
     *
     * @param url
     * @return
     */
    public static String methodGet(String url, Map<String, String> headers) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        String result = null;
        CloseableHttpClient httpclient = getHttpClient();
        try {
            URL url2 = new URL(url);
            URI uri = new URI(url2.getProtocol(), url2.getAuthority(), url2.getPath(), url2.getQuery(), null);
            // 创建httpget
            HttpGet httpget = new HttpGet(uri);
            if (headers != null && !headers.isEmpty()) {
                for (Entry<String, String> entry : headers.entrySet()) {
                    httpget.addHeader(entry.getKey(), entry.getValue());
                }
            }
            // 执行get请求.
            HttpResponse response = httpclient.execute(httpget);
            if (response == null) {
                return null;
            }
            // 获取响应实体
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            UnitedLogger.error(e);
            return null;
        } finally {
            // 关闭连接,释放资源
            if (httpclient != null) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    UnitedLogger.error(e);
                }
            }
        }
        return result;
    }

    /**
     * http get 无参请求，返回json数据
     *
     * @param url
     * @return
     */
    public static JSONObject methodGetJson(String url) {
        String result = methodGet(url);
        return stringToJson(result);
    }

    /**
     * String 转 Json
     *
     * @param str
     * @return
     */
    public static JSONObject stringToJson(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(str);
        } catch (Exception e) {
            UnitedLogger.error(e);
            return null;
        }
        return jsonObject;
    }

}
