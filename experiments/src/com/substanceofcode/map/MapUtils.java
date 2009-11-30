package com.substanceofcode.map;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;



public class MapUtils {
    
   static ByteArrayOutputStream    baos = new ByteArrayOutputStream(8196);
    
    /**
     * Make an input stream into a byte array
     * @param is
     * @return byte array
     */
    public static byte[] parseInputStream(InputStream is) {


        byte[] buffer = new byte[4096];
               
        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);

        int bytesRead = 0;
        int totalBytesRead = 0;
            
        try {
            while (true) {
                bytesRead = is.read(buffer, 0, 4096);
                if (bytesRead == -1) {
                    break;
                } else {
                    totalBytesRead += bytesRead;
                    baos.write(buffer, 0, bytesRead);                   
                }
            }
        } catch (IOException e) {
            e.printStackTrace();          
        }catch(OutOfMemoryError e){
            e.printStackTrace();
          
        }
        

        if (is != null)
            try {
                is.close();               
            } catch (IOException e) {

            }

        is = null;
        if(totalBytesRead>0) {            
            return baos.toByteArray();
        } else {
            return null;
        }
    }
}