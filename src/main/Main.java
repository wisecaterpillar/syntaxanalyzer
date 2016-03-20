
package main;

import java.io.IOException;
import java.util.Scanner;
import parserthing.Parser;
import parserthing.PredicateParseException;
import s2a.predicates.api.PredicateCreateException;

public class Main {

   
    public static void main(String[] args) throws PredicateParseException, PredicateCreateException  {
        
        Scanner in = new Scanner(System.in);
        Parser parser = null;
        boolean run = true;
        String cmd = null;
        
        while (run) {
            System.out.println(">>");
            cmd = in.nextLine();
            if (cmd.equals("q"))
                run = false;
            else {
                try {
                    parser = new Parser(cmd);
                    System.out.println("The result is : " + parser.prove());
                }
                catch (IOException ioe) {
                    System.out.println("Wrong file name!");
                }
            }
        }
    }
}
