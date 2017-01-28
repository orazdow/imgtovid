package imgtovid;

public class ImgToVid {

    
    public static void main(String[] args) {
        String inpath = args[0];   
        Converter gen = new Converter(inpath);
        Analyzer an = new Analyzer(gen.getImg());
        System.out.println(an.invertHeuristic());
        gen.setConvType("bright");
        gen.run();
        gen = new Converter(inpath);
        gen.setConvType("sat");
        gen.run();
        gen = new Converter(inpath);
        gen.setConvType("bright");
        gen.setInvert(true);
        gen.run();
                 
    }

}
