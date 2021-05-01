package fr.naruse.ytaudiodownloader.page;

import fr.naruse.ytaudiodownloader.YoutubeAudioDownloader;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;

public abstract class AbstractPageReader {

    public abstract void read(YoutubeAudioDownloader downloader, String urlString);

    protected String readPageSourceCode(URL url) throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{new YoutubeAudioDownloader.TrustAnyTrustManager()}, new SecureRandom());

        connection.setSSLSocketFactory(sc.getSocketFactory());
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null)
        {
            stringBuilder.append(inputLine);
        }

        return stringBuilder.toString();
    }
}
