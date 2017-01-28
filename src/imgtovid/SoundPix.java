package imgtovid;

import java.awt.Color;
import java.awt.image.BufferedImage;


public class SoundPix {

int SAMPLING_RATE = 44100;              
int numOscs = 200;
int imgwidth, imgheight;
float out = 0;
double baseFreq = 50;
short[] sineTable = new short[4096]; //affects step resolution
Osc[] oscs;
int div;
static float[] hsb = new float[3];
boolean invert = false;

BufferedImage img;

SoundPix(BufferedImage inimg){
 img = inimg;   
 imgwidth = img.getWidth();
 imgheight = img.getHeight();
    
if(numOscs >= imgwidth){
  div = 1;  
}else{
div =  imgwidth/numOscs; 
}
oscs = new Osc[numOscs];
initTable(sineTable);
initOscs();    
}

void getLevels(int[] in, int atype){
        int a = 0;
    for (int i = 0; i < in.length; i+= div) {
        if(a < oscs.length){ 
         // oscs[a].amp = Math.pow(getBrightness(in[i])/(double)255, 1.5);
            oscs[a].amp = Math.pow(getHSB(in[i], atype), 1.5);
         //   System.out.println(getBrightness(in[i])/(double)255); 
          //  System.out.println(getHue(in[i]));
          
        }   a++;  
    }

}

void setInvert(boolean in){
    invert = in;
}

int getBrightness(int in){
  return ((in & 0xff) + (in >> 8 & 0xff) + (in >> 16 & 0xff));
}

double getHSB(int in, int type){
    Color.RGBtoHSB((in & 0xff), (in >> 8 & 0xff), (in >> 16 & 0xff), hsb);
    if(invert){
     return 1.0 - hsb[type];   
    }
    else return hsb[type];
}

Short getOutSig(){
    out = 0;
    for (int i = 0; i < numOscs; i++) {
        out += oscs[i].incOut()/numOscs;
    }
    return (short)out;
}

void initTable(short[] table){
for (int i = 0; i < table.length; i++) {
      table[i] = (short)(Math.sin(2*Math.PI * (i/(double)table.length) )*Short.MAX_VALUE/2);        
}
}

void initOscs(){ 
    double freq = baseFreq; 
    for(int i = 0; i < numOscs; i++){
        
    freq = baseFreq*Math.pow(2,i/(double)25);
    oscs[i] = new Osc(sineTable);
    oscs[i].setFreq(freq);
    }
}


class Osc{
int SAMPLING_RATE = 44100;
double amp = 0; 
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

void setAmp(double in){
   amp = in;
}

double incOut(){
theta +=step;
if(theta >= table.length){theta = 0;}
return table[theta]*amp;   
}

}
    
}
