package imgtovid;


public class ImgToVid {
static boolean convert = false;
    
    public static void main(String[] args) {
        Batch b = new Batch();
        String[] files = b.getList();
       // String inpath = args[0];   
        if(args.length > 0 && args[0].equals( "convert" )){convert = true;}
        for( String inpath : files){
        Converter gen = new Converter(inpath);
        Analyzer an = new Analyzer(gen.getImg(), gen.getFname());
        an.startLog();
        an.log();
        if(convert){
        gen.setConvType("bright");
        gen.run();
        gen = new Converter(inpath);
        gen.setConvType("bright");
        gen.setInvert(true);
        gen.run();
        gen = new Converter(inpath);
        gen.setConvType("sat");
        gen.run();
        }
        }
                 
    }

}
