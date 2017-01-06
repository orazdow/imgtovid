
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
       SoundPix soundgen = new SoundPix(width);
       
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
        soundgen.getLevels(buff);
        nextFrameTime += frameRate; a++;
        //  System.out.println(a);
      }

      // compute and encode the audio for the balls
          // short out = 0;
       for (int i = 0; i < mSamples.length; ++i){
          //  mSamples[i] = (short)(soundgen.oscs[50].incOut() + (short)(soundgen.oscs[40].incOut())   )    ;
            mSamples[i] = soundgen.getOutSig();
       }
       //   mSamples[i] =  soundgen.getOutSig(); //(short)(Math.random()*Short.MAX_VALUE);
      //   mSamples[i] =  (short)(Math.random()*Short.MAX_VALUE);
      
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

static class SoundPix{

final int SAMPLING_RATE = 44100;      
final int SAMPLE_SIZE = 2; 
int SamplesThisPass;
        
int numOscs = 160;
float out = 0;
double baseFreq = 50;
short[] sineTable = new short[4096]; //affects step resolution
Osc[] oscs;
int div;
float coefs[];


SoundPix(int imgwidth){
if(numOscs >= imgwidth){
  div = 1;  
}else{
div =  imgwidth/numOscs; 
}
oscs = new Osc[numOscs];
initTable(sineTable);
initOscs();    
}

//void setCoeffs(float[] in){
//    for(int i = 0; i < in.length; i++){
//        if(i < oscs.length){
//            oscs[i].amp = in[i];
//        }else{ System.out.println("ERROR: pix array: "+in.length+" larger than osc array: "+oscs.length); break; }
//    }
//}

void getLevels(int[] in){
        int a = 0;
    for (int i = 0; i < in.length; i+= div) {
       // System.out.println(a);
        if(a < oscs.length){ 
          oscs[a].amp = (float)getBrightness(in[i])/255;
        }   a++;  
    }
 
//for(int i = 0; i < oscs.length; i++){
//    oscs[i].amp = 1;
//}
    
}

static int getBrightness(int in){
  return ((in & 0xff) + (in >> 8 & 0xff) + (in >> 16 & 0xff));
}

Short getOutSig(){
    out = 0;
    for (int i = 0; i < numOscs; i++) {
        out += oscs[i].incOut()/numOscs;
    }
    return (short)out;
    
    
    //(short)(soundgen.oscs[50].incOut() + (short)(soundgen.oscs[40].incOut())   )
}

void initTable(short[] table){
for (int i = 0; i < table.length; i++) {
      table[i] = (short)(Math.sin(2*Math.PI * (i/(double)table.length) )*Short.MAX_VALUE/2);        
}
}

void initOscs(){ 
    double freq = baseFreq; //int a = 0;
    for(int i = 0; i < numOscs; i++){
          freq = baseFreq*Math.pow(2,i/(double)25);
        oscs[i] = new Osc(sineTable);
        oscs[i].setFreq(freq);
      //  System.out.println(oscs[i].freq+" "+oscs[i].step);;
    }
}


 class Osc{
int SAMPLING_RATE = 44100;
float amp = 0; //1;
int theta;
double freq;
int step;
short[] table;

Osc(short[] table){
  this.table = table;  
}

void setFreq(double freq){
this.freq = freq; 
step = (int)(freq/SAMPLING_RATE*table.length);   
  
}

void setAmp(float in){
    amp = in;
}

float incOut(){
theta +=step;
if(theta >= table.length){theta = 0;}
return table[theta]*amp;   
}

}

}



}
