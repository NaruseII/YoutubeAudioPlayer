package fr.naruse.ytaudiodownloader.player;

import javazoom.jl.decoder.*;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MP3Player {

    private static final List<byte[]> QUEUE = new ArrayList<>();

    public static void addToQueue(byte[] response) {
        QUEUE.add(response);
    }

    public static void clearQueue() {
        QUEUE.clear();
    }

    private CustomPlayer player;

    private byte[] array;
    public MP3Player(byte[] array) {
        this.array = array;
    }

    public void setVolume(float volume) {
        if(player != null){
            player.setVolume(volume);
        }
    }

    public float getVolume() {
        if(player == null){
            return -1;
        }
        return player.volume;
    }

    public void close(){
        if(player != null){
            player.close();
        }
    }

    public void stop(){
        if(player != null){
            player.setPaused(true);
        }
    }

    public void play() {
        play(true);
    }

    public void play(boolean newThread) {
        if(player != null){
            player.setPaused(true);
        }
        try {
            BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(array));
            player = new CustomPlayer(bis);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if(newThread){
            new Thread(() -> truePlay()).start();
        }else{
            truePlay();
        }
    }

    private void truePlay(){
        try {
            player.play();
            byte[] bytes = checkQueueNext();
            if(bytes != null){
                array = bytes;
                System.out.println("[Youtube Audio] Starting next sound...");
                play(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] checkQueueNext(){
        if(QUEUE.size() == 0){
            return null;
        }
        byte[] b = QUEUE.get(0);
        QUEUE.remove(b);
        return b;
    }

    public void setVolumeFactor(float volume) {
        if(player != null){
            player.setVolumeFactor(volume);
        }
    }

    class CustomPlayer {

        private Bitstream bitstream;
        private Decoder decoder;
        private AudioDevice audio;
        private boolean closed;
        private boolean complete;
        private int lastPosition;

        private float volume = 1;
        private float volumeFactor = 1;
        private boolean isPaused = false;

        public CustomPlayer(InputStream stream) throws JavaLayerException {
            this.closed = false;
            this.complete = false;
            this.lastPosition = 0;
            this.bitstream = new Bitstream(stream);
            this.decoder = new Decoder();

            FactoryRegistry r = FactoryRegistry.systemRegistry();
            this.audio = r.createAudioDevice();

            this.audio.open(this.decoder);
        }

        public void play() throws JavaLayerException {
            this.play(2147483647);
        }

        public boolean play(int frames) throws JavaLayerException {
            boolean ret;
            for( ret = true; frames-- > 0 && ret; ) {
                if(isPaused){
                    frames++;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                ret = this.decodeFrame();
            }

            if (!ret) {
                AudioDevice out = this.audio;
                if (out != null) {
                    out.flush();
                    synchronized(this) {
                        this.complete = !this.closed;
                        this.close();
                    }
                }
            }

            return ret;
        }

        public synchronized void close() {
            AudioDevice out = this.audio;
            if (out != null) {
                this.closed = true;
                this.audio = null;
                out.close();
                this.lastPosition = out.getPosition();

                try {
                    this.bitstream.close();
                } catch (BitstreamException var3) {
                }
            }

        }

        public synchronized boolean isComplete() {
            return this.complete;
        }

        public int getPosition() {
            int position = this.lastPosition;
            AudioDevice out = this.audio;
            if (out != null) {
                position = out.getPosition();
            }

            return position;
        }

        protected boolean decodeFrame() throws JavaLayerException {
            try {
                AudioDevice out = this.audio;
                if (out == null) {
                    return false;
                } else {
                    Header h = this.bitstream.readFrame();
                    if (h == null) {
                        return false;
                    } else {
                        SampleBuffer output = (SampleBuffer)this.decoder.decodeFrame(h, this.bitstream);
                        synchronized(this) {
                            out = this.audio;
                            if (out != null) {
                                short[] samples = output.getBuffer();

                                for(int samp = 0; samp < samples.length; ++samp) {
                                    samples[samp] = (short)((int)((float)samples[samp] * this.volume * this.volumeFactor));
                                }

                                out.write(output.getBuffer(), 0, output.getBufferLength());
                            }
                        }

                        this.bitstream.closeFrame();
                        return true;
                    }
                }
            } catch (Exception var6) {
                throw new JavaLayerException("Exception decoding audio frame", var6);
            }
        }

        public void setVolume(float volume) {
            this.volume = volume;
        }

        public float getVolume() {
            return volume;
        }

        public void setPaused(boolean paused) {
            isPaused = paused;
        }

        public void setVolumeFactor(float volume) {
            this.volumeFactor = volume;
        }
    }
}
