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
      parser.LR1Parser P=new parser.LR1Parser();//Create object of LR1Parser
       P.read_grammar("C:\\Users\\ACER\\Documents\\NetBeansProjects\\syntax parser\\src\\syntax\\parser\\LR0.txt");//Read the grammar file and 
        P.buildDFA();//Build a dfa from the file          
        P.getParsingTable();// do the parsing table
       
        
        // valid example
        int h=P.parse("V DT_N PREP DT_N");//the function will take as parameters input _toParse(string) and flag(array index)  and it return boolean 
        //after calling the function if h==-1 then the input is parsed successfully 
        System.out.println(h);
        
        // invalid example
        int y=P.parse("V V");
        //after calling the function if y>-1 then  y contains the error index of the word 
        System.out.println(y);
        
        // another invalid example
         y=P.parse("V N PREP");
        System.out.println(y);
        
}
}
