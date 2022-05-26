package com.example.syntax_flutter_parser;

import io.flutter.embedding.android.FlutterActivity;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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
                                final String textToParse = call.argument("text");

                                    parseResult = parseMyText(textToParse);


                                if (parseResult != 0) {
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

    private int parseMyText(String textToParse){
//        String txt = "ARTV V1 V P PRO CONJ ARTN RP ADJ Noun\n" +
//                "VS NS VP NP N S\n" +
//                "S -> VS | NS\n" +
//                "VS -> VP NP | VP NS\n" +
//                "NS -> NP NP | NP VP | NP VS | NP NS | NP\n" +
//                "VP -> ARTV V1 | V | V1 | V P | V1 P\n" +
//                "NP -> N | PRO | ARTN N | ARTN PRO | N CONJ N | PRO CONJ PRO | N CONJ PRO | PRO CONJ N | N ARTN N\n" +
//                "N -> N ADJ | Noun | PRO | RP | ADJ";
//        String txt = "هذا مثال على العربي\n"+
//                "سطر ثاني";

        String txt = "ARTV V P PRO CONJ ADJ PREP DT_N N\nS VS NS VP NP Noun\nS -> VS | NS\nVS -> VP NP | VP NS\nNS -> NP NP | NP VP | NP VS | NP NS | NP\nVP -> ARTV V | V | V P\nNP -> Noun | PRO | PREP Noun | PREP PRO | Noun CONJ Noun | PRO CONJ PRO | Noun CONJ PRO | PRO CONJ Noun | Noun PREP Noun\nNoun -> Noun ADJ | N | PRO | ADJ | DT_N";

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

        System.out.println("befour instance");
        LR1Parser P=new LR1Parser();//Create an instance of the desired parser-In this case LR1 type parser
        System.out.println("befour path");
        P.read_grammar(getFilesDir()+"/testme.txt");

//        parser.LR1Parser P=new parser.LR1Parser();//Create object of LR1Parser
//        P.read_grammar("C:\\Users\\ACER\\Documents\\NetBeansProjects\\syntax parser\\src\\syntax\\parser\\LR0.txt");//Read the grammar file and
        P.buildDFA();//Build a dfa from the file
        P.getParsingTable();// do the parsing table


        // valid example
        int h= 0;
        h=P.parse(textToParse);//the function will take as parameters input _toParse(string) and flag(array index)  and it return boolean
        //after calling the function if h==-1 then the input is parsed successfully
        System.out.println(h);
//
//        // invalid example
//        int y=P.parse("V V");
//        //after calling the function if y>-1 then  y contains the error index of the word
//        System.out.println(y);
//
//        // another invalid example
//        y=P.parse("V N PREP");
//        System.out.println(y);

        return h;
    }

}
