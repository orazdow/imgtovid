package imgtovid;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Analyzer {

BufferedImage img;
static float[] hsb = new float[3];
boolean invert = false;
String fname;

final int streakThresh = 500;
final int totalThresh = 50000;

Analyzer(BufferedImage img){
    this.img = img;
}
Analyzer(BufferedImage img, String fname){
    this.img = img;
    this.fname = fname;   
}

boolean invertHeuristic(){
    
  int s = streakCount(img, 0.8, 1, 20);
  int r = totalInRange(img, 0.8, 2);   
  
  return s > streakThresh && r > totalThresh;
  
}

double getHSB(int in, int type){
    Color.RGBtoHSB((in & 0xff), (in >> 8 & 0xff), (in >> 16 & 0xff), hsb);
    if(invert){
     return 1.0 - hsb[type];   
    }
    else return hsb[type];
}

//sat vs bright channel estimation
int histDiff(){    
return countDiffLines(lineHist(img, 1, 0.001, 0)) - countDiffLines(lineHist(img, 2, 0.001, 0));
}
int histDiff(double thresh){
 return countDiffLines(lineHist(img, 1, 0.001, thresh)) - countDiffLines(lineHist(img, 2, 0.001, thresh));
}

int countDiffLines(Integer[] a){
    Integer last = a[0];
    int count = 0;
    for(int i = 1; i < a.length; i++){
        if( a[i] != last ){
            count++;
        }
        last = a[i];
        
    }    return count;
}
 //estimate total variation
Integer[] lineHist(BufferedImage img, int type, double range, double thresh){
  ArrayList<Integer> lines = new ArrayList<Integer>();    
  ArrayList<Double> levs = new ArrayList<Double>(); 
  double current;
    for(int y = 0; y < img.getHeight(); y++ ){
        for(int x = 0; x < img.getWidth(); x++ ){
            
            current = getHSB(img.getRGB(x, y),type);
            boolean contains = false;

            for(int i = 0; i < levs.size(); i++){
             if (levs.get(i) > current - range && levs.get(i) < current + range ){
                 if(current > thresh)
                 contains = true; break;
             }            
            }
            if(!contains){
                levs.add(current);
            }         
        } 
       // System.out.println(levs.size());
        lines.add(levs.size());
        levs.clear();
    }
    
      return lines.toArray(new Integer[lines.size()]);
}
   
//count bright streaks
int streakCount(BufferedImage img, double low, double hi, int thresh){

  double current; int count = 0; int mainCount = 0;
  
    for(int y = 0; y < img.getHeight(); y++ ){
        for(int x = 0; x < img.getWidth(); x ++){
            
            current = getHSB(img.getRGB(x, y),2);

            if( current >= low && current <= hi){
                count++;
            }else{
                if(count >= thresh){
                    mainCount++;
                    count = 0;
                }
            }
            
        }
       }   

      return mainCount;
}
 //  total brightness
int totalInRange(BufferedImage img, double thresh, int type){
double current; int count = 0;

    for(int y = 0; y < img.getHeight(); y++ ){
        for(int x = 0; x < img.getWidth(); x ++){
          
        current = getHSB(img.getRGB(x, y), type); 
        
         if(current > thresh){
             count++;
         }
            
        } 
    }
    
    return count;
}
 // light dark streak ratio
double streakRatio(BufferedImage img, double darklev, double lightlev, double range, int thresh){

    double current; int darkCount = 0; int lightCount = 0; int dStreak = 0; int lStreak = 0;

    for(int y = 0; y < img.getHeight(); y++ ){
        for(int x = 0; x < img.getWidth(); x ++){
         
            current = getHSB(img.getRGB(x, y),2);
            
            if(Math.abs(current - darklev) <= range){
                darkCount++;
            }else if(darkCount >= thresh){
                dStreak++; darkCount = 0;
            }
            
            if(Math.abs(current - lightlev) <= range){
                lightCount++;
            }else if(lightCount >= thresh){
                lStreak++; lightCount = 0;
            }
            
          }
        }    
    
    return lStreak / (double)dStreak;
}
}
