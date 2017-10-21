package imgtovid;


public class ImgToVid {

    public static void main(String[] args) {
        // auto mode unless 2nd arg passed
        Converter gen = new Converter(args[0]);
        Analyzer an = new Analyzer(gen.getImg(), gen.getFname());
        if(args.length > 1){
            gen.setConvType(args[1]);
        }else{  
            an.analyze();
            boolean inv = an.getInvert();
            boolean sat = an.getSat();
            if(sat){ 
                gen.setConvType("sat");
                System.out.println("sat");

            }else if(inv){
                gen.setInvert(true); 
                System.out.println("invert");
            }
        }
        gen.run();               
    }

}
