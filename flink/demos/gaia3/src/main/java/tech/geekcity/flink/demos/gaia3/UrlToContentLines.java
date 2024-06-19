package tech.geekcity.flink.demos.gaia3;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.Builder;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

@Getter
public class UrlToContentLines implements FlatMapFunction<String, String> {
  private final Supplier<OkHttpClient> okHttpClientSupplier;
  private final boolean sslVerify;
  private final boolean contentGzip;
  private transient OkHttpClient okHttpClient;

  @Builder
  public UrlToContentLines(
      Supplier<OkHttpClient> okHttpClientSupplier, boolean sslVerify, boolean contentGzip) {
    this.okHttpClientSupplier = okHttpClientSupplier;
    this.sslVerify = sslVerify;
    this.contentGzip = contentGzip;
  }

  @Override
  public void flatMap(String url, Collector<String> collector) throws Exception {
    Request request = new Request.Builder().url(url).build();
    Response response = okHttpClient().newCall(request).execute();
    ResponseBody responseBody = response.body();
    if (null == responseBody) {
      throw new RuntimeException("response body is null");
    }
    InputStream byteStream = responseBody.byteStream();
    try (BufferedReader bufferedReader =
        new BufferedReader(
            new InputStreamReader(contentGzip ? new GZIPInputStream(byteStream) : byteStream))) {
      bufferedReader.lines().forEach(collector::collect);
    }
  }

  private OkHttpClient okHttpClient() {
    if (null == okHttpClient) {
      okHttpClient =
          Optional.ofNullable(null == okHttpClientSupplier ? null : okHttpClientSupplier.get())
              .orElseGet(() -> defaultOkHttpClient(sslVerify));
    }
    return okHttpClient;
  }

  private OkHttpClient defaultOkHttpClient(boolean sslVerify) {
    if (sslVerify) {
      return new OkHttpClient();
    }
    TrustManager[] trustAllCerts =
        new TrustManager[] {
          new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                java.security.cert.X509Certificate[] chain, String authType) {}

            @Override
            public void checkServerTrusted(
                java.security.cert.X509Certificate[] chain, String authType) {}

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
              return new java.security.cert.X509Certificate[] {};
            }
          }
        };
    SSLContext sslContext;
    try {
      sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new RuntimeException(e);
    }
    OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
    okHttpClientBuilder.sslSocketFactory(
        sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0]);
    okHttpClientBuilder.hostnameVerifier((hostname, session) -> true);
    return okHttpClientBuilder.build();
  }
}
