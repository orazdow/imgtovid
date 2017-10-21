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

public class Converter {

    // video parameters
    final int videoStreamIndex = 0;
    final int videoStreamId = 0;
    final long frameRate = DEFAULT_TIME_UNIT.convert(25, MILLISECONDS);
    int width = 420;
    int height = 320;
    
    // audio parameters
    final int audioStreamIndex = 1;
    final int audioStreamId = 0;
    final int channelCount = 1;
    final int sampleRate = 44100; 
    final int sampleCount = 1000;
    
    final long duration = DEFAULT_TIME_UNIT.convert(height*32, MILLISECONDS); //hack, approximates right num
    int line = 0;    
    BufferedImage img;   
    String inpath, outpath;
    int convType = 2;
    boolean invert = false;
    IMediaWriter writer; 
    SoundPix soundgen;
    File infile;
    String fname;
    
    
Converter(String in){

    try {
        inpath = in;  
        infile = new File(inpath);
        img = ImageIO.read(infile);
        fname = infile.getName();
        outpath = inpath.substring(0, inpath.lastIndexOf('.')) + ".mp4";
        } catch (IOException e) {
        System.err.println(e.getMessage());
        }
    
       img = resize(img, 400);
       width = img.getWidth();
       height = img.getHeight(); 
       soundgen = new SoundPix(img);
}

void setImg(BufferedImage in){
    img = in;
    img = resize(img, 400);
    width = img.getWidth();
    height = img.getHeight(); 
    soundgen = new SoundPix(img);
}

BufferedImage getImg(){
    return img;
}

String getFname(){
    return fname;
}

void setConvType(int in){
    convType = in;
}

void setConvType(String in){ 
   if(in.equalsIgnoreCase( "bright") || in.equalsIgnoreCase( "brightness")){
       convType = 2;
   }
   else if(in.equalsIgnoreCase("sat")|| in.equalsIgnoreCase("saturation")){
       convType = 1;
   }
   else{
       System.err.println("wrong convType syntax");
   }
}

void run(){
       
      int[] buff = new int[width];       
      int[] linepix = new int[img.getWidth()+1];
      for (int i = 0; i < linepix.length; i++) 
      {
          linepix[i] = 0xffffff; 
      }

        writer = ToolFactory.makeWriter(outpath);
           
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
      while (clock >= nextFrameTime && line < height)
      {
        BufferedImage frame = img; 
        paintLine(frame, line, buff, linepix);
        
        writer.encodeVideo(videoStreamIndex, frame, nextFrameTime, DEFAULT_TIME_UNIT);
          
        
        restoreLine(frame, line, buff);
        soundgen.getLevels(buff, convType);
        
        nextFrameTime += frameRate; 
        line++;
      }

       for (int i = 0; i < mSamples.length; ++i)
       mSamples[i] = soundgen.getOutSig();
        
       writer.encodeAudio(audioStreamIndex, mSamples, clock, DEFAULT_TIME_UNIT);       
       totalSampleCount += sampleCount;
    }
    
    writer.close();    
    
    
}

void paintLine(BufferedImage img, int lineindex, int[] buff, int[] arr){  
// put line in buffer to resotre from later    
img.getRGB(0, lineindex, img.getWidth(), 1, buff, 0, 1);
// paint line
img.setRGB(0, lineindex, img.getWidth(), 1, arr, 0, 1);

}

void restoreLine(BufferedImage img, int lineindex, int[] buff){
img.setRGB(0, lineindex, img.getWidth(), 1, buff, 0, 1);
}

BufferedImage resize(BufferedImage inimage, int targetheight){
    int inw = inimage.getWidth();
    int inh = inimage.getHeight();
    float ratio = targetheight/(float)inh;
    int newW = (int)((float)inw*ratio);
    int newH = (int)((float)inh*ratio);
    
    if(newH % 2 != 0){newH--;}
    if(newW % 2 != 0){newW--;}
    
    Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
    BufferedImage dimg = new BufferedImage(newW, newH, inimage.getType());
    
    Graphics2D g2d = dimg.createGraphics();
    g2d.drawImage(tmp, 0, 0, null);
    g2d.dispose();
    
    return dimg;
}

void setInvert(boolean in){
    invert = in;
    soundgen.setInvert(in);
}

void invertImage(){
    
 for(int i = 0; i < img.getWidth(); i++){
     for(int j = 0; j < img.getHeight(); j++){
         
         int c = img.getRGB(i, j);
         
         int r = 255 - (c & 0xff);
         int g = 255 - (c >> 8 & 0xff);
         int b = 255 - (c >> 16 & 0xff);
         
        int out =  b << 16 | g << 8 | r;
        
        img.setRGB(i, j, out);
         
         
     }
 }   

    
}

}

