package imgtovid;


public class ImgToVid {

    public static void main(String[] args) {
        // auto mode
        Converter gen = new Converter(args[0]);
        if(args.length > 1){ gen.setOutPath(args[1]); }
        Analyzer an = new Analyzer(gen.getImg(), gen.getFname());
        an.analyze();
        boolean inv = an.getInvert();
        boolean sat = an.getSat();
        if(inv){ 
            gen.setInvert(true); 
            System.out.println("invert");
        }else if(sat){
            gen.setConvType("sat");
            System.out.println("sat");
        }
        gen.run();               
    }

}
