package com.example.syntax_flutter_parser;

import android.os.Build;

import io.flutter.embedding.android.FlutterActivity;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
import parser.LR1Parser;

public class MainActivity extends FlutterActivity {

    private static final String CHANNEL = "samples.flutter.dev/parse";

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            // Note: this method is invoked on the main thread.
                            if (call.method.equals("parseMyText")) {
                                int parseResult = 0;

                                    parseResult = parseMyText();


                                if (parseResult != -1) {
                                    result.success(parseResult);
                                } else {
                                    result.error("UNAVAILABLE", "parsing not completed.", null);
                                }
                            } else {
                                result.notImplemented();
                            }
                        }
                );
    }

    private int parseMyText(){
//        String txt = "ARTV V1 V P PRO CONJ ARTN RP ADJ Noun\n" +
//                "VS NS VP NP N S\n" +
//                "S -> VS | NS\n" +
//                "VS -> VP NP | VP NS\n" +
//                "NS -> NP NP | NP VP | NP VS | NP NS | NP\n" +
//                "VP -> ARTV V1 | V | V1 | V P | V1 P\n" +
//                "NP -> N | PRO | ARTN N | ARTN PRO | N CONJ N | PRO CONJ PRO | N CONJ PRO | PRO CONJ N | N ARTN N\n" +
//                "N -> N ADJ | Noun | PRO | RP | ADJ";
        String txt = "هذا مثال على العربي\n"+
                "سطر ثاني";

        FileOutputStream fos=null;
        try {
            fos = openFileOutput("testme.txt",MODE_PRIVATE);
            fos.write(txt.getBytes());
            System.out.println(getFilesDir()+"/testme.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileInputStream fis=null;

        try {
            fis=openFileInput("testme.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br=new BufferedReader(isr);

            System.out.println(br.readLine());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int batteryLevel = 1;
        System.out.println("befour instance");
        LR1Parser obj3=new LR1Parser();//Create an instance of the desired parser-In this case LR1 type parser
        System.out.println("befour path");
        obj3.read_grammar(getFilesDir()+"/testme.txt");
        //Read the grammar file and
        obj3.buildDFA(); //Build a dfa from the file
        System.out.println(obj3.getParsingTable(false)?"Grammar is LR1 :)":"Grammar isn't LR1  :(");
        // if false was specify in the parameter (getParsingTable) it will not print if true then it will print the parsing table
        System.out.println(obj3.parse("Noun Noun V",false)?"Successfully parsed":"Parse Failure");
        //System.out.println(obj3.states); //not needed
        //obj3.print_transitions();
        obj3.getParsingTable(true);
        obj3.parse("particle Noun",true);

        return batteryLevel;
    }

}
