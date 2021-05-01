package fr.naruse.ytaudiodownloader.page;

import fr.naruse.ytaudiodownloader.YoutubeAudioDownloader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PageReaderYtDownload extends AbstractPageReader{
    @Override
    public void read(YoutubeAudioDownloader downloader, String urlString) {
        System.out.println("[Youtube Audio] Reading page...");
        try {
            if(!urlString.contains("v=")){
                System.err.println("Not a youtube url!");
                return;
            }
            String videoId = urlString.split("v=")[1];

            String sourceCode = this.readPageSourceCode(new URL("https://www.yt-download.org/api/button/mp3/"+videoId));

            List<String> list = new ArrayList<>();
            for (String string : sourceCode.split("href")) {
                if(string.contains("https://www.yt-download.org/download")){
                    list.add(string);
                }
            }
            List<String> links = new ArrayList<>();
            for (String s : list) {
                links.add(s.split(" ")[0].replace("\"", "").replace("=", ""));
            }
            for (String link : links) {
                YoutubeAudioDownloader.Rate rate;
                if(link.contains("/320/")){
                    rate = YoutubeAudioDownloader.Rate.KBPS_320;
                }else if(link.contains("/256/")){
                    rate = YoutubeAudioDownloader.Rate.KBPS_256;
                }else if(link.contains("/192/")){
                    rate = YoutubeAudioDownloader.Rate.KBPS_192;
                }else{
                    rate = YoutubeAudioDownloader.Rate.KBPS_128;
                }
                downloader.putRatioURL(rate, link);
            }
            downloader.setPageReadDone(true);
            downloader.watchPageDone();
        } catch (Exception e) {
            System.err.println("[Youtube Audio] Can't find yt-download!");
            e.printStackTrace();
        }
    }
}
