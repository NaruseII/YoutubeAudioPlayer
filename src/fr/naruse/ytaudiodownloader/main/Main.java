package fr.naruse.ytaudiodownloader.main;

import fr.naruse.ytaudiodownloader.YoutubeAudioDownloader;
import fr.naruse.ytaudiodownloader.page.PageReaderYtDownload;
import fr.naruse.ytaudiodownloader.player.MP3Player;

import java.util.Scanner;

public class Main {

    private static MP3Player player = null;

    public static void main(String[] args) {
        System.out.println("WARNING >> This class is only for tests");
        System.out.println("Waiting youtube link...");

        Scanner scanner = new Scanner(System.in);
        String url = scanner.next();

        System.out.println("Starting task...");

        new YoutubeAudioDownloader(url, new PageReaderYtDownload()) {
            @Override
            public void watchPageDone() {
                String downloadUrl = this.findFirstUrl();

                //downloadUrl = this.getUrl(Rate.KBPS_320); // You can choose mp3 ratio

                this.downloadToByteArray(downloadUrl, response -> {
                    System.out.println("Playing...");
                    player = new MP3Player(response);
                    player.play();
                    player.setVolume(0.2f); // Volume 0.2 = 20%
                });
            }
        };

        while (true){
            String s = scanner.next();
            if(s.equals("stop")){
                if(player != null){
                    player.stop();
                }
                System.exit(0);
                return;
            }

            try{
                Float f = Float.parseFloat(s);
                player.setVolume(f);
            }catch (Exception e){
                System.err.println("Type 'stop' or a float to change volume");
            }
        }
    }

    //https://www.youtube.com/watch?v=UceSOk9Jq1Y
}
