package imgtovid;

import java.awt.Color;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class Analyzer {

BufferedImage img;
static float[] hsb = new float[3];
boolean invert = false;
boolean sat = false;
String fname;
FileWriter  writer;
PrintWriter printWriter;

final int streakThresh = 700;
final int totalThresh = 50000;
final float ratioThresh = 9;
final int histDifftThresh = 40;
final int lineDiffThresh = 2000;


Analyzer(BufferedImage img){
    this.img = img;
}
Analyzer(BufferedImage img, String fname){
    this.img = img;
    this.fname = fname;   
}

boolean invertHeuristic(){  
int s = streakCount(img, 0.8, 1, 20);
int t = totalInRange(img, 0.8, 2);  
float r = lightDarkRatio(20, 70); 
  
// invert =  t > totalThresh && ( (s > streakThresh && r > ratioThresh ) || s > 2000 ); //least strict
// invert =  t > totalThresh && s > 1300 && r > ratioThresh; // high strictness
 invert = (t > totalThresh && s > 1300 && r > ratioThresh) || (t > 70000 && (s > 2000 || r > 35)); // mid strictness
 return invert;  
}

boolean satHeuristic(){
    sat = lineSBDiff() > lineDiffThresh && histDiff(0.4) > histDifftThresh;
    return sat;
}

void analyze(){
    satHeuristic();
    invertHeuristic();  
}

boolean getInvert(){
  return invert;  
}

boolean getSat(){
    return sat;
}

//  sat vs bright hist width diff
int histDiff(){    
return countDiffLines(lineHist(img, 1, 0.001, 0)) - countDiffLines(lineHist(img, 2, 0.001, 0));
}
// less sensitive
int histDiff(double thresh){
 return countDiffLines(lineHist(img, 1, 0.001, thresh)) - countDiffLines(lineHist(img, 2, 0.001, thresh));
}
//sat vs bright line pixel diff
int lineSBDiff(){
    return (LineDiff(img, 1, 0.05)-LineDiff(img, 2, 0.05));
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
  //num lines with diff above threshhold
int LineDiff(BufferedImage img, int type, double thresh){
    int count = 0;
    for(int y = 1; y < img.getHeight(); y++ ){
        for(int x = 0; x < img.getWidth(); x++ ){
          
         if( Math.abs(getHSB(img.getRGB(x, y), type) - getHSB(img.getRGB(x, y-1), type)) > thresh ) {
             count++;
            }
          }   
        } 
        return count;
}


//count bright streaks
int streakCount(BufferedImage img, double low, double hi, int thresh){

  double current; int count = 0; int mainCount = 0;
  
    for(int y = 0; y < img.getHeight(); y++ ){
        for(int x = 0; x < img.getWidth(); x ++){
            
            current = getHSB(img.getRGB(x, y),2);

            if( current >= low && current <= hi){
                count++;
            }
            else{
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
 // light/dark  ratio
float lightDarkRatio(int dark, int light){
  double darkthresh = dark  /(double)100;
  double lightthresh = light/(double)100;  
  double darknum = 0; double lightnum = 0;
  double current;
    for(int i = 0; i < img.getWidth(); i++){
        for(int j = 0; j < img.getHeight(); j++){
            current =  getHSB(img.getRGB(i, j), 2);
            if(current <= darkthresh){
                darknum++;
            }
            else if(current >= lightthresh){
                lightnum++;
            }
            
        }
    }
    return (float)(lightnum/darknum);
    
}

double getHSB(int in, int type){
    Color.RGBtoHSB((in & 0xff), (in >> 8 & 0xff), (in >> 16 & 0xff), hsb);
    return hsb[type];
}

void startLog(String path){
     try {    
        writer = new FileWriter(path, true);
    } catch (IOException ex) {
         System.err.println( ex.getMessage() );
    }
    printWriter = new PrintWriter(writer, true);
    
}
void startLog(){
     try {    
        writer = new FileWriter("log.csv", true);
    } catch (IOException ex) {
         System.err.println( ex.getMessage() );
    }
    printWriter = new PrintWriter(writer, true);
    
}

String csvString(){
    analyze();
    return fname+", "+lineSBDiff()+", "+histDiff()+", "+histDiff(0.4)+", "+lightDarkRatio(20, 70)+", "+streakCount(img, 0.8, 1, 20)+", "+totalInRange(img, 0.8, 2)+", "+boolToInt(sat)+", "+boolToInt(invert)+", U, U";    
}

void log(){
   String s = csvString();
   System.out.println(s);
   printWriter.append(s+"\r\n"); 
   printWriter.close();
}

void print(){
  System.out.println(csvString());    
}

int boolToInt(boolean in){
    return in ? 1 : 0; 
}

}
