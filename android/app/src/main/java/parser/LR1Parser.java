/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public class LR1Parser
{
  
    HashMap<String,ArrayList<ArrayList<String>>> rules;
    HashSet<String> terminals;
    HashSet<String> nonTerminals;
    ArrayList<String> allSymbols;
    ArrayList<String> table[][];
    String startSymbol;
    parser.AugmentedFirstAndFollow utils;
    ArrayList<DFA> states;
    DFA dfa;
    int minId;
    static class Pair
    {
        String rule;
        int dot;
        Pair(String c,int d)
        {
            rule=c;
            dot=d;
        }
        @Override
        public String toString()
        {
            int l=rule.indexOf("->"),i;
            String left=rule.substring(0,l).trim();
            String right=rule.substring(l+2).trim();
            StringBuilder sb=new StringBuilder();
            sb.append(left+" -> ");
            String tokens[]=right.split(" ");
            for(i=0;i<tokens.length;i++)
            {               
                if(tokens[i].equals(","))
                {
                    sb.append(": ");
                    continue;
                }
                if(i==dot)
                    sb.append(". ");
                sb.append(tokens[i]+" ");
            }
            if(i==dot)
                sb.append(". ");
            return sb.toString();
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + rule.hashCode();
            result = prime * result + dot;
            return result;
        }       
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Pair other = (Pair) obj;
            if (dot!= other.dot)
                return false;
            if (!rule.equals(other.rule))
                return false;
            return true;
        }
    }
    static class DFA
    {
        HashSet<Pair> rules;
        int id;
        HashMap<String, DFA> transitions;
        DFA()
        {
            rules=new HashSet<>();
            id=-1;
            transitions=new HashMap<>();
        }
        @Override
        public String toString()
        {
            return "Id: "+id+" , Rules: "+rules+"\n";//Mapping: "+transitions+"\n\n";
        }
    }
    public LR1Parser()
    {
        rules=new HashMap<>();
        startSymbol="";
        terminals=new HashSet<>();
        nonTerminals=new HashSet<>();
        allSymbols=new ArrayList<>();
        minId=-1;
        dfa=new DFA();
        utils=new parser.AugmentedFirstAndFollow();
        states=new ArrayList<>();
    }
    protected int getId()
    {
        return ++minId;
    }
    void modify(parser.AugmentedFirstAndFollow utils)
    {
        utils.ntCount.clear();
        utils.ntCount.addAll(nonTerminals);
        utils.tCount.clear();
        utils.tCount.addAll(terminals);
        utils.tCount.add("$");
        utils.ntCount.add(startSymbol+"'");
    }
    public void read_grammar(String filePath)
    {
        String str="";
        int line=0;
        try
        {
            BufferedReader br=new BufferedReader(new FileReader(filePath));
            str=br.readLine();
            String _terminals[]=str.split(" ");
            for(int i=0;i<_terminals.length;i++)
                terminals.add(_terminals[i]);
            str=br.readLine();
            String _nonTerminals[]=str.split(" ");
            for(int i=0;i<_nonTerminals.length;i++)
                nonTerminals.add(_nonTerminals[i]);
            allSymbols.addAll(terminals);
            allSymbols.addAll(nonTerminals);
            while((str=br.readLine())!=null)//Read the string, put in map
            {
                int l=str.indexOf("->"),i,j;
                String left=str.substring(0,l).trim();
                String right=str.substring(l+2).trim();
                l=right.length();
                String tokens[]=right.split("[|]");
                if(line==0)
                {
                    startSymbol=left;
                }
                line++;
                ArrayList<ArrayList<String>> al=new ArrayList<>();
                for(i=0;i<tokens.length;i++)
                {
                    String s[]=tokens[i].trim().split(" ");
                    ArrayList<String> temp=new ArrayList<>();
                    for(j=0;j<s.length;j++)
                        temp.add(s[j]);
                    al.add(temp);
                }
                rules.put(left,al);//Put all the rules in the map
                line++;
            }
            br.close();
            augment();
            modify(utils);
            unAugment();
            utils.module1(filePath,true,false);
        }catch(Exception e)
        {
            System.out.println("Couldn't read grammar file: "+e.getMessage());
            System.exit(0);
        }
    }
    public String join(ArrayList<String> v, String delim) 
    {
        if(v==null||v.size()==0)
            return "";
        StringBuilder ss=new StringBuilder();
        for(int i = 0; i < v.size(); ++i)
        {
            if(i != 0)
                ss.append(delim);
            ss.append(v.get(i));
        }
        return ss.toString();
    }
    public void getClosure(HashSet<Pair> closure)
    {
        boolean done=false;
        while(!done)
        {
            done=true;
            Iterator iterator=closure.iterator();
            HashSet<Pair> addAble=new HashSet<>();
            while(iterator.hasNext())
            {
                Pair pair=(Pair)iterator.next();
                int l=pair.rule.indexOf("->"),i;
                String left=pair.rule.substring(0,l).trim();
                String right=pair.rule.substring(l+2).trim();
                String tokens[]=right.split(" ");
                if(pair.dot>=tokens.length||pair.dot<0)
                    continue;
                else if(nonTerminals.contains(tokens[pair.dot]))
                {
                    ArrayList<ArrayList<String>> al=rules.get(tokens[pair.dot]);
                    for(i=0;i<al.size();i++)
                    {
                        String str=join(al.get(i)," ");
                        Pair p=new Pair(tokens[pair.dot]+" -> "+str,0);
                        if(!closure.contains(p))
                        {
                            done=false;
                            addAble.add(new Pair(tokens[pair.dot]+" -> "+str.trim(),0));
                        }
                    }
                }
            }
            closure.addAll(addAble);
        }
    }
    public HashSet<Pair> getGoto(HashSet<Pair> X, String I)
    {
        HashSet<Pair> goTo=new HashSet<>();
        HashSet<Pair> add=new HashSet<>();
        for(Pair p: X)
        {
            String str[]=p.rule.substring(p.rule.indexOf("->")+2).trim().split(" ");
            if(p.dot>=str.length||p.dot<0)
                continue;
            if(str[p.dot].equals(I))
                goTo.add(new Pair(p.rule,p.dot+1));
        }
        for(Pair p:goTo)
        {
            HashSet<Pair> temp=new HashSet<>();
            temp.add(p);
            getClosure(temp);
            add.addAll(temp);
        }
        goTo.addAll(add);
        return goTo;
    }
    public void augment()
    {
        ArrayList<ArrayList<String>> al=new ArrayList<>();
        ArrayList<String> al2=new ArrayList<>();
        al2.add(startSymbol);
        al.add(al2);
        rules.put(startSymbol+"'",al);
    }
    public void unAugment()
    {
        rules.remove(startSymbol+"'");
    }
    public int parse(String _toParse)
    {
        _toParse=_toParse+" $";
        Stack<String> stack=new Stack<>();
        ArrayList<ArrayList<String>> al=new ArrayList<>();
        ArrayList<String> a=new ArrayList<>();
        a.add("Step");
        a.add("Stack");
        a.add("Action");
        a.add("Input");
        al.add(a);
        stack.push("$");
        stack.push("0");
        int pointer=0,i,step=0;
        String toParse[]=_toParse.split(" ");
        while(!stack.empty()&&pointer<toParse.length)
        {
            a=new ArrayList<>();
            int row=Integer.parseInt(stack.peek());
            int col=allSymbols.indexOf(toParse[pointer]);
            a.add(step+"");
            a.add(stack+"");
            if(col<0)
            {
                a.add("Parse error");
                al.add(a);
                break;
            }
            if(table[row][col].size()==0)
            {
                a.add("Parse error");
                al.add(a);
                break;
            }
            String action=table[row][col].get(0);
            a.add(action);
            String str[]=action.split(" ");
            String left=str[0].trim();
            String right="";
            for(i=1;i<str.length;i++)
                right+=str[i]+" ";
            right=right.trim();
            if(left.equals("Shift"))
            {
                stack.push(toParse[pointer]);
                stack.push(right);
                pointer++;
            }
            else if(left.equals("Reduce"))
            {
                left=right.substring(0,right.indexOf("->")).trim();
                right=right.substring(right.indexOf("->")+2).trim();
                if(right.charAt(0)!='@')
                {
                    for(i=0;i<2*(str.length-3);i++)
                    {
                        if(stack.size()!=0)
                            stack.pop();
                        else
                        {
                            a.add("Parse error");
                            al.add(a);
                            return pointer;
                           
                        }
                    }
                }
                int top=Integer.parseInt(stack.peek());
                stack.push(left);                
                stack.push(table[top][allSymbols.indexOf(left)].get(0));
            }
            else if(left.equals("Accept")&&pointer==toParse.length-1)
            {
                String ppp="";
                for(i=pointer;i<toParse.length;i++)
                    ppp+=toParse[i]+" ";
                a.add(ppp);
                al.add(a);
                return -1;
            }
            String pp="";
            for(i=pointer;i<toParse.length;i++)
                pp+=toParse[i]+" ";
            a.add(pp);
            al.add(a);
            step++;
        }
        return pointer;
        
    }
  
    protected int getIndex(HashSet<Pair> Goto)
    {
        int i=0;
        for(DFA dfa: states)
        {
            if((dfa.rules.containsAll(Goto)&&Goto.containsAll(dfa.rules)))
                return i;
            i++;
        }
        return -1;
    }
    protected void print_transitions()
    {
        for(DFA dfa: states)
        {
            System.out.println("Map for state: "+dfa);
            System.out.println("Transitions: ");
            for(Map.Entry<String, DFA> mp:dfa.transitions.entrySet())
            {
                System.out.println(mp.getKey()+"->"+mp.getValue().rules+" ( S"+getIndex(mp.getValue().rules)+" )");
            }
            System.out.println();
        }
    }
    // parser
    public void buildDFA()
    {
        augment();
        dfa.id=getId();
        dfa.rules.add(new Pair(startSymbol+"' -> "+startSymbol+" , $",0));
        getLR1Closure(dfa.rules);
        states.add(dfa);
        int i=0;
        boolean done=false;
        while(!done)
        {
            done=true;
            for(i=0;i<states.size();i++)
            {
                DFA current=states.get(i);
                for(String str: allSymbols)//All symbol iteration
                {
                    HashSet<Pair> Goto=getLR1Goto(current.rules,str);
                    int index=getIndex(Goto);
                    if(index>=0)
                    {
                        current.transitions.put(str,states.get(index));
                    }
                    else
                    {
                        DFA new_DFA=new DFA();
                        new_DFA.id=getId();
                        new_DFA.rules=Goto;
                        states.add(new_DFA);
                        current.transitions.put(str,new_DFA);
                        done=false;
                    }
                }
            }
        }
        unAugment();
        minId=-1;
    }
    public boolean getParsingTable()
    {
        terminals.add("$");
        table=new ArrayList[states.size()][terminals.size()+nonTerminals.size()];
        int i,j;
        for(i=0;i<states.size();i++)
            for(j=0;j<terminals.size()+nonTerminals.size();j++)
                table[i][j]=new ArrayList<>();
        ArrayList<String> colMap=new ArrayList<>();
        colMap.addAll(terminals);
        colMap.addAll(nonTerminals);
        allSymbols.clear();
        allSymbols.addAll(colMap);
        int row=0;
        boolean isLR1=true;
        for(DFA dfa: states)
        {
            for(Pair p:dfa.rules)
            {
                int l=p.rule.indexOf("->");
                int l2=p.rule.indexOf(",");
                String left=p.rule.substring(0,l).trim();
                String mid=p.rule.substring(l+2,l2).trim();
                String right=p.rule.substring(l2+2).trim();
                String str[]=mid.split(" ");
                if(p.dot<0||p.dot>str.length)
                    continue;
                if(left.equals(startSymbol+"'")&&p.dot==str.length)
                {
                    table[row][colMap.indexOf("$")].add("Accept :)");
                }
                else if((p.dot==str.length)||(p.dot==0&&str[0].equals("@")))//Condition 2 for epsilon only
                {
                    String G[]=right.split("[|]");
                    for(String g:G)
                        table[row][colMap.indexOf(g.trim())].add("Reduce "+left+" -> "+mid);
                }
                else if(terminals.contains(str[p.dot]))
                {
                    HashSet<Pair> hs=getLR1Goto(dfa.rules,str[p.dot]);
                    int index=getIndex(hs);
                    if(index>=0)
                    {
                        boolean add=true;
                        for(String s: table[row][colMap.indexOf(str[p.dot].trim())])
                        {
                            if(s.equals("Shift "+index))
                               add=false;
                        }
                        if(add)
                            table[row][colMap.indexOf(str[p.dot].trim())].add("Shift "+index);
                    }
                }
            }
            for(String ss:nonTerminals)
            {
                HashSet<Pair> hs=getLR1Goto(dfa.rules,ss);
                int index=getIndex(hs);
                if(index>=0&&states.get(index).rules.size()!=0)
                {
                    table[row][colMap.indexOf(ss)].add(index+"");
                }
            }
            row++;
        }
        ArrayList<String> pretty[][]=new ArrayList[table.length+1][table[0].length+1];
        for(i=0;i<pretty.length;i++)
        {
            for(j=0;j<pretty[0].length;j++)
            {
                pretty[i][j]=new ArrayList<>();
            }
        }
        pretty[0][0].add("State");
        for(i=1;i<pretty[0].length;i++)
        {
            pretty[0][i].add(colMap.get(i-1)+"");
        }
        for(i=1;i<pretty.length;i++)
        {
            pretty[i][0].add((i-1)+"");
        }
        for(i=1;i<pretty.length;i++)
        {
            for(j=1;j<pretty[0].length;j++)
            {
                if(table[i-1][j-1].size()>1)
                    isLR1=false;
                pretty[i][j].addAll(table[i-1][j-1]);
            }
        }

        terminals.remove("$");
        
        return isLR1;
    }
    private void getLR1Closure(HashSet<Pair> closure)
    {
        boolean done=false;
        while(!done)
        {
            done=true;
            Iterator iterator=closure.iterator();
            HashSet<Pair> addAble=new HashSet<>();
            while(iterator.hasNext())
            {
                Pair pair=(Pair)iterator.next();
                int l=pair.rule.indexOf("->"),i;
                int l2=pair.rule.indexOf(",");
                String left=pair.rule.substring(0,l).trim();
                String mid=pair.rule.substring(l+2,l2).trim();
                String right=pair.rule.substring(l2+2).trim();
                String tokens[]=mid.split(" ");
                String newRight="";
                if(pair.dot>=tokens.length-1)
                {
                    newRight=right;
                }
                else
                {
                    if(terminals.contains(tokens[pair.dot+1]))
                        newRight=tokens[pair.dot+1];
                    else
                        newRight=join(utils._first.get(tokens[pair.dot+1])," | ");
                }
                if(pair.dot>=tokens.length||pair.dot<0)
                    continue;
                else if(nonTerminals.contains(tokens[pair.dot]))
                {
                    ArrayList<ArrayList<String>> al=rules.get(tokens[pair.dot]);
                    for(i=0;i<al.size();i++)
                    {
                        String str=join(al.get(i)," ");
                        Pair p=new Pair(tokens[pair.dot]+" -> "+str.trim()+" , "+newRight,0);
                        if(!closure.contains(p))
                        {
                            done=false;
                            addAble.add(p);
                        }
                    }
                }
            }
            closure.addAll(addAble);
        }
    }    
    private HashSet<Pair> getLR1Goto(HashSet<Pair> X, String I)
    {
        HashSet<Pair> goTo=new HashSet<>();
        HashSet<Pair> add=new HashSet<>();
        for(Pair p: X)
        {
            String str[]=p.rule.substring(p.rule.indexOf("->")+2).trim().split(" ");
            if(p.dot>=str.length||p.dot<0)
                continue;
            if(str[p.dot].equals(I))
                goTo.add(new Pair(p.rule,p.dot+1));
        }
        for(Pair p:goTo)
        {
            HashSet<Pair> temp=new HashSet<>();
            temp.add(p);
            getLR1Closure(temp);
            add.addAll(temp);
        }
        goTo.addAll(add);
        return goTo;
    }
}