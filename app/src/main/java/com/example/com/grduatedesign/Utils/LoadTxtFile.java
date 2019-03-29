package com.example.com.grduatedesign.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LoadTxtFile {
    public static List<String> txtList(File txtfile) {
        List<String>txtList=new ArrayList<>();
        try {
            InputStream instream = new FileInputStream(txtfile);
            if (instream != null)
            {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                //分行读取
                while (( line = buffreader.readLine()) != null) {
                    txtList.add(line+"\n");
                }
                instream.close();
            }
        }
        catch (java.io.FileNotFoundException e)
        {
            L.d( "The File doesn't not exist.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }


        return txtList;
    }
}
