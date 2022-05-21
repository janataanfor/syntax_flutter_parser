/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;


/**
 *
 * @author ACER
 */
public class SyntaxParser {

    /**
     * @param args the command line arguments
     */
  public static void main(String args[])
    {
       
        LR1Parser obj3=new LR1Parser();//Create an instance of the desired parser-In this case LR1 type parser
//        obj3.read_grammar("C:\\Users\\ACER\\Documents\\NetBeansProjects\\syntax parser\\src\\syntax\\parser\\LR0.txt");
        obj3.read_grammar("/Users/oalshokri/StudioProjects/syntax_flutter_parser/android/app/src/main/java/parser/LR0.txt");
        //Read the grammar file and
        obj3.buildDFA(); //Build a dfa from the file       
        System.out.println(obj3.getParsingTable(false)?"Grammar is LR1 :)":"Grammar isn't LR1  :(");
        // if false was specify in the parameter (getParsingTable) it will not print if true then it will print the parsing table 
        System.out.println(obj3.parse("Noun Noun V",false)?"Successfully parsed":"Parse Failure");
        //System.out.println(obj3.states); //not needed 
        //obj3.print_transitions();
        obj3.getParsingTable(true);
        obj3.parse("particle Noun",true);
     }
    
}
