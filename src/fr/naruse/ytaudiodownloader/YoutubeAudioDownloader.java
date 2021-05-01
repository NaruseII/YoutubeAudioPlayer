package fr.naruse.ytaudiodownloader;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import fr.naruse.ytaudiodownloader.page.AbstractPageReader;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.concurrent.*;

public abstract class YoutubeAudioDownloader {

    private HashMap<Rate, String> urlMap = new HashMap<>();
    private boolean pageReadDone = false;
    private boolean downloadDone = false;

    public YoutubeAudioDownloader(final String urlString, AbstractPageReader reader) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> reader.read(this, urlString));
        executor.shutdown();
    }

    public abstract void watchPageDone();

    public boolean isPageReadDone() {
        return pageReadDone;
    }

    public String getUrl(Rate rate){
        if(!pageReadDone){
            return null;
        }
        if(urlMap.containsKey(rate)){
            return urlMap.get(rate);
        }
        return null;
    }


    public String findFirstUrl(){
        String url = getUrl(Rate.KBPS_128);
        if(url != null){
            System.out.println("[Youtube Audio] Using ratio "+Rate.KBPS_128);
            return url;
        }
        url = getUrl(Rate.KBPS_192);
        if(url != null){
            System.out.println("[Youtube Audio] Using ratio "+Rate.KBPS_192);
            return url;
        }
        url = getUrl(Rate.KBPS_256);
        if(url != null){
            System.out.println("[Youtube Audio] Using ratio "+Rate.KBPS_256);
            return url;
        }
        url = getUrl(Rate.KBPS_320);
        if(url != null){
            System.out.println("[Youtube Audio] Using ratio "+Rate.KBPS_320);
            return url;
        }
        return null;
    }

    public boolean downloadToByteArray(final String stringUrl, final Callback<byte[]> callback){
        if(stringUrl == null || !pageReadDone){
            System.err.println("[Youtube Audio] Can't find download link!");
            return false;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future future = executor.submit(() -> {
            try {

                System.out.println("[Youtube Audio] Downloading...");
                byte[] bytes = downloadBytes(new URL(stringUrl));
                System.out.println("[Youtube Audio] Audio downloaded.");

                callback.onResponse(bytes);

                downloadDone = true;
            }catch (Exception e){
                System.err.println("[Youtube Audio] Can't download audio. ERROR");
                e.printStackTrace();
            }
        });
        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return true;
    }

    public InputStream getInputStream(URL url) throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
        connection.setSSLSocketFactory(sc.getSocketFactory());
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

        return connection.getInputStream();
    }

    private byte[] downloadBytes(URL host) {
        try (BufferedInputStream in = new BufferedInputStream(getInputStream(host));
             ByteOutputStream byteOutputStream = new ByteOutputStream()) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;

            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                byteOutputStream.write(dataBuffer, 0, bytesRead);
            }

            return byteOutputStream.getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isDownloadDone(){
        return downloadDone;
    }

    public void putRatioURL(Rate rate, String url){
        this.urlMap.put(rate, url);
    }

    public void setPageReadDone(boolean pageReadDone) {
        this.pageReadDone = pageReadDone;
    }

    public enum Rate {

        KBPS_320,
        KBPS_256,
        KBPS_192,
        KBPS_128

    }

    public interface Callback<T>{

        void onResponse(T response);

    }

    public static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }
    }
}
