
package imgtovid;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;
import com.xuggle.xuggler.IAudioSamples;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import javax.imageio.ImageIO;


public class ImgToVid {

    // video parameters

    static final int videoStreamIndex = 0;
    static final int videoStreamId = 0;
    static final long frameRate = DEFAULT_TIME_UNIT.convert(25, MILLISECONDS);
    static int width = 420;
    static int height = 320;
    
    // audio parameters

    static final int audioStreamIndex = 1;
    static final int audioStreamId = 0;
    static final int channelCount = 1;
    static final int sampleRate = 44100; // Hz
    static final int sampleCount = 1000;
    
    static final long duration = DEFAULT_TIME_UNIT.convert(height*32, MILLISECONDS); //hack, approximates right num
    static int a = 0;
    
    static BufferedImage img;
    
    public static void main(String[] args) {
     
    try {
    img = ImageIO.read(new File(args[0]));
    } catch (IOException e) {
    System.err.println(e.getMessage());
     }
    
       img = resize(img, 400);
       width = img.getWidth();
       height = img.getHeight();
       int[] buff = new int[width];
       
     int[] linepix = new int[img.getWidth()+1];
     for (int i = 0; i < linepix.length; i++) {
        linepix[i] = 0xffffff;
      }
        
           final IMediaWriter writer = ToolFactory.makeWriter("outvid.mp4");
           
              long nextFrameTime = 0;
              long totalSampleCount = 0;
              short[] mSamples = new short[sampleCount];

           writer.addVideoStream(videoStreamIndex, videoStreamId,
             width, height);

   
           writer.addAudioStream(audioStreamIndex, audioStreamId,
            channelCount, sampleRate); 
     
        for (long clock = 0; clock < duration; clock = IAudioSamples
           .samplesToDefaultPts(totalSampleCount, sampleRate))
    {
      // while the clock time exceeds the time of the next video frame,
      // get and encode the next video frame

      while (clock >= nextFrameTime && a < height)
      {
        BufferedImage frame = img; 
        paintLine(frame, a, buff, linepix);
        writer.encodeVideo(videoStreamIndex, frame, nextFrameTime, 
          DEFAULT_TIME_UNIT);
        restoreLine(frame, a, buff);
        nextFrameTime += frameRate; a++;
        //  System.out.println(a);
      }

      // compute and encode the audio for the balls
       for (int i = 0; i < mSamples.length; ++i)
        mSamples[i] = (short)(Math.random()*Short.MAX_VALUE);
       
   //   short[] samples = mSamples;
      writer.encodeAudio(audioStreamIndex, mSamples, clock, 
        DEFAULT_TIME_UNIT);
      totalSampleCount += sampleCount;
    }

    // manually close the writer
    
    writer.close();
           
        
    }
    
static void paintLine(BufferedImage img, int lineindex, int[] buff, int[] arr){  
// put line in buffer to resotre from later    
img.getRGB(0, lineindex, img.getWidth(), 1, buff, 0, 1);
// paint line
img.setRGB(0, lineindex, img.getWidth(), 1, arr, 0, 1);

}

static void restoreLine(BufferedImage img, int lineindex, int[] buff){
img.setRGB(0, lineindex, img.getWidth(), 1, buff, 0, 1);
}

static BufferedImage resize(BufferedImage inimage, int targetheight){
    int inw = inimage.getWidth();
    int inh = inimage.getHeight();
    float ratio = targetheight/(float)inh;
    int newW = (int)((float)inw*ratio);
    int newH = (int)((float)inh*ratio);
    
    Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
    BufferedImage dimg = new BufferedImage(newW, newH, inimage.getType());
    
    Graphics2D g2d = dimg.createGraphics();
    g2d.drawImage(tmp, 0, 0, null);
    g2d.dispose();
    
    return dimg;
}

static class soundPix{
    
}

}
