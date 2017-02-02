package imgtovid;


public class ImgToVid {
static boolean convert = false;
    // no args : batch analyze, log all
    // arg = "convert" : batch analyze, convert, log all
    // arg = file.jpg :  analyze, convert, log file
    public static void main(String[] args) {
        Batch b = new Batch();
        String[] files = b.getList();
       // String inpath = args[0];   
        if(args.length > 0 && args[0].equals( "convert" )){convert = true;}
        else if(args.length > 0 && args[0].substring(args[0].lastIndexOf(".")).equals(".jpg")){
        convert = true;
        files = new String[1]; files[0] = args[0];
        }        
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
