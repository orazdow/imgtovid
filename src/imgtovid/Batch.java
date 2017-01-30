
package imgtovid;

import java.io.File;
import java.util.ArrayList;

public class Batch {
    String[] list;

Batch(){
 list = getFiles();
}

String[] getList(){
return list;
}

private String[] getFiles(){
    File file =  new File("./");
    File[] files = file.listFiles();
    ArrayList<String> list = new ArrayList<>();
    
    for(File el : files){
        String s = el.getName();
        if(s.lastIndexOf(".") > 0) 
        if( s.substring(s.lastIndexOf(".")).equals(".jpg") || s.substring(s.lastIndexOf(".")).equals(".jpeg") )
        list.add(s);          
    }
    
    String[] rtn = list.toArray(new String[0]);
    return rtn;
}

}
