package parserthing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import s2a.inference.api.AbstractQuantifierFactory;
import s2a.inference.api.AbstractTheoryFactory;
import s2a.inference.api.InferenceRule;
import s2a.inference.api.Logician;
import s2a.inference.api.Theory;
import s2a.inference.mgp.LogicianFactory;
import s2a.predicates.api.AbstractPredicateFactory;
import s2a.predicates.api.Predicate;
import s2a.predicates.api.PredicateCreateException;
import s2a.predicates.api.PredicateObject;
import s2a.predicates.api.PredicateType;


public  class Parser {
    
    private static int uniqueCode = 1;
    
    private final String filename;
    
    private  final LogicianFactory logFactory = 
            LogicianFactory.instance;

    private  final AbstractPredicateFactory predFactory = 
            AbstractPredicateFactory.getInstance();

    private  final AbstractTheoryFactory thFactory = 
            AbstractTheoryFactory.getInstance();
    
    static private final AbstractQuantifierFactory quantifierFactory = 
            AbstractQuantifierFactory.getInstance();
    
    private   final Theory theory = 
            thFactory.createTheory();
    
    private final Logician logician = 
            logFactory.createLogician();
    
    private  Predicate target = 
            null;
        
    private final HashMap<String, PredicateObject> args = new HashMap<>();
    
    private boolean isTarget = true;

    public Parser(String filename) {
        
        this.filename = filename;
    }

    public Parser() {
        this.filename = null;
    }
       
    
    public boolean prove () throws IOException, PredicateParseException, PredicateCreateException {
        addAllRules();
        parseFile();
        if (isTarget)
            return(logician.proveTrue(theory, target));
        else
            return(logician.proveFalse(theory, target));
    }
    
    public boolean prove_win(String text) throws IOException, PredicateParseException, PredicateCreateException {
        
        parseText(text);
        if (isTarget)
            return(logician.proveTrue(theory, target));
        else
            return(logician.proveFalse(theory, target));
    }
    
    /**
     * Просмотр файла по строкам и в соответствии с видом каждой строки 
     * принимается решение о принадлежности данных к фактам, правилам или 
     * предикату-цели (в начале строки цели стоит ?)
     * @param filename - имя просматриваемого файла
     * @throws IOException 
     * @throws parserthing.PredicateParseException 
     * @throws s2a.predicates.api.PredicateCreateException 
     */

    public  void parseFile () throws IOException, PredicateParseException, PredicateCreateException {
        boolean targetFound = false;
         
        String regexArg = "((_{0,1}[a-z]+)|(\\-{0,1}[0-9]+)|([A-Z]+))";
        String regexFact = "[A-Z]+(_[A-Z]+)*\\(" + regexArg + "(,"+regexArg +")*\\)";       //^ - the beginning and $ - the end
        String regexTargetTrue = "\\?" + regexFact;
        String regexTargetFalse = "\\?!" + regexFact;
        

        List<String> list = Files.readAllLines(new File(filename).toPath(), Charset.defaultCharset() );
        
            for (String line:list) {
                System.out.println("Parsing line: " + line);
                line = line.replaceAll(" ", "");
                if (line.matches(regexFact))
                    theory.addPredicate(parseFact(line));
                else if (line.matches(regexTargetTrue)) 
                {
                    target = parseTarget(line);
                    targetFound = true;
                    break;
                }
                else if (line.matches(regexTargetFalse)) 
                {
                    isTarget = false;
                    target = parseTarget(line);
                    targetFound = true;
                    break;
                }
                else if (line.equals("\n"))
                    ;
                else    
                {
                    System.out.println("something's wrong w/line "
                            + line);
                }
            }
                
        if (!targetFound)
            throw new PredicateParseException("Target-predicate not found");
        args.clear();
    }
    
    public void parseText (String text) throws PredicateParseException, PredicateCreateException {
        boolean targetFound = false;
        String regexArg = "((_{0,1}[a-z]+)|(\\-{0,1}[0-9]+)|([A-Z]+))";
        String regexFact = "[A-Z]+(_[A-Z]+)*\\(" + regexArg + "(,"+regexArg +")*\\)";       //^ - the beginning and $ - the end
        String regexRule = regexFact + ":-" + regexFact + "(," + regexFact + ")*";
        String regexTargetTrue = "\\?" + regexFact;
        String regexTargetFalse = "\\?!" + regexFact;
        
        String list[] = text.split("\n");
        
            for (String line:list) {
                System.out.println("Parsing line: " + line);
                line = line.replaceAll(" ", "");
                if (line.matches(regexFact))
                    theory.addPredicate(parseFact(line));
                else if (line.matches(regexTargetTrue)) 
                {
                    target = parseTarget(line);
                    targetFound = true;
                    break;
                }
                else if (line.matches(regexTargetFalse)) 
                {
                    isTarget = false;
                    target = parseTarget(line);
                    targetFound = true;
                    break;
                }
                else if (line.equals("\n"))
                    ;
                else    
                {
                    System.out.println("something's wrong w/line "
                            + line);
                    throw new PredicateParseException("Syntax error in line: " + line);
                }
            }
        
        if (!targetFound)
            throw new PredicateParseException("Target-predicate not found!");
        args.clear();        
    }
    /**
     * 
     * @param name - Comparison, Pointer, ZeroNonzero, Arithmetic expected 
     * @throws java.io.IOException 
     */
    
    /**
     *
     * @param fname
     * @throws java.io.IOException
     */
    public void addRules(String fname) throws IOException {
        
        System.out.println("!!!ADDING RULES " + fname);
        String regexArg = "((_{0,1}[a-z]+)|(\\-{0,1}[0-9]+)|([A-Z]+))";
        String regexFact = "[A-Z]+(_[A-Z]+)*\\(" + regexArg + "(,"+regexArg +")*\\)";   //^ - the beginning and $ - the end
        String regexRule = regexFact + ":-" + regexFact + "(," + regexFact + ")*";
        
        List<String> list;
        list = Files.readAllLines(new File(fname).toPath(), Charset.defaultCharset() );
        
        try {
            for (String line:list) {
                line = line.replaceAll(" ", "");
                if (line.matches(regexRule))
                    logician.addRule(parseRule(line));
                else if (line.equals(""))
                    break;
                else    
                {
                    System.out.println("The line doesn't match the regex: "
                            + line);
                    throw new IOException("Wrong line in file " + fname);
                }
            }
        }
        catch (PredicateCreateException | PredicateParseException pce) 
        {
            System.out.println(pce.getLocalizedMessage());
        }        
    }
    
    public void addAllRules () throws IOException {
        this.addRules("rules/_Arithmetic.txt");
        this.addRules("rules/_Comparison.txt");        
        this.addRules("rules/_ZeroNonzero.txt");
//        this.addRules("rules/_Pointer.txt");
    }
     
    /**
     * Парсинг строки вида NAME (arg1,arg2,...)
     * @param input входная строка
     * @return Predicate
     * @throws PredicateParseException - если предикат неизвестного типа или с неверным кол-вом аргументов
     * @throws PredicateCreateException - если невозможно создать предикат
     */
    private Predicate parseFact(String input) throws PredicateParseException, PredicateCreateException {
        
        List<PredicateObject> argsLoc = new ArrayList<>();
        String[] parts = input.split("\\(|\\)|,");
        String intConstantRegex ="-{0,1}\\d+";
        String varRegex = "[a-z]+";
        
        try {
            PredicateType.valueOf(parts[0]);
        }
        catch (IllegalArgumentException iae) {
            
            throw new PredicateParseException("Неверное имя предикатa "
                    + input);

        }
        if ((parts.length - 1)  != PredicateType.valueOf(parts[0]).getArgsNumber()) {
            throw new PredicateParseException("Неверное количество аргументов");
        }
        
        for (int i = 1; i<parts.length; i++) {
            PredicateObject po;
            if (parts[i].matches(varRegex))
                po = predFactory.createVariableObject(4, parts[i]); 
            else if (parts[i].matches(intConstantRegex))
                po = predFactory.createIntegerConstantObject(Long.decode(parts[i]), 1);
            else
                throw new PredicateParseException("Неправильное имя у переменной "
                        + parts[i]);
            if (args.containsKey(po.getUniqueName()))
                argsLoc.add(args.get(po.getUniqueName()));
            else {
                argsLoc.add(po);
                args.put(po.getUniqueName(), po);
            }
            
        }
        Predicate p = predFactory.createPredicate(PredicateType.valueOf(parts[0]), (argsLoc));
        System.out.println("GOT "
                + p.getType() + "  " + p.getArguments());
        return predFactory.createPredicate(PredicateType.valueOf(parts[0]), (argsLoc));
    }
        
    private Predicate parseItemQ (String input) throws PredicateParseException, PredicateCreateException {
        
        System.out.println("Parsing item " + input);
        ArrayList<PredicateObject> argsLoc = new ArrayList<>();
        input = input.replaceAll(" ", "");
        String[] parts = input.split("\\(|\\)|,"); 
        String intConstantRegex ="-{0,1}\\d+";
        String simConstantRegex = "[A-Z]+";
        String valueRegex = "[a-z]+";
        String nonconstRegex = "_[a-z]+";
        
        try {
            PredicateType.valueOf(parts[0]);
            System.out.println(parts[0]);
        }
        catch (IllegalArgumentException iae) {
            
            throw new PredicateParseException("Неверное имя предикатa "
                    + input);

        }
        if ((parts.length - 1)  != PredicateType.valueOf(parts[0]).getArgsNumber()) {
            throw new PredicateParseException("Неверное количество аргументов");
        }
        
        PredicateObject qo ;
        for (int i = 1; i<parts.length; i++) {
            int uNr = uniqueCode++;
            if (parts[i].matches(simConstantRegex))
                qo = quantifierFactory.createQuantifierSimpleConstant(uNr, parts[i]);
            else if (parts[i].matches(valueRegex))
                qo = quantifierFactory.createQuantifierValue(uNr, parts[i]);    
            else if (parts[i].matches(intConstantRegex))
                qo = predFactory.createIntegerConstantObject(Long.decode(parts[i]), 1);
            else if (parts[i].matches(nonconstRegex))
                qo = quantifierFactory.createQuantifierNonconstValue(uNr, input);
            else
                throw new PredicateParseException("Неправильное имя у переменной "
                        + parts[i]);
            
            if (args.containsKey(qo.getUniqueName()))
                argsLoc.add(args.get(qo.getUniqueName()));
            else if (args.containsValue(qo))
                System.out.println("бедабеда!");
                    
            else {
                argsLoc.add(qo);
                args.put(qo.getUniqueName(), qo);
            }
        }
        System.out.println(argsLoc);
        
        return predFactory.createPredicate(PredicateType.valueOf(parts[0]), (argsLoc));
    }
               
    /**
     * Парсинг строки вида NAME (arg1,arg2,...) :- NAME2(arg1,...), NAME3(arg1,...), ...
     * @param input
     * @return Inference Rule
     * @throws PredicateParseException - если предикат неизвестного типа или с неверным кол-вом аргументов
     * @throws PredicateCreateException - если невозможно создать предикат
     */
    private InferenceRule parseRule(String input) throws PredicateParseException, PredicateCreateException {
        args.clear();
        
        System.out.println("=====PARSING LINE " + input);
        
        String regexFact = "[A-Z]+(_[A-Z]+)*\\(((_{0,1}[a-z]+)|(\\-{0,1}[0-9]+)|([A-Z]+))(,((_{0,1}[a-z]+)|(\\-{0,1}[0-9]+)|([A-Z]+)))*\\)";       //^ - the beginning and $ - the end
        String regexRule = regexFact + ":-" + regexFact + "(," + regexFact + ")*";
        String regexTarget = "\\?" + regexFact;
        
        Predicate right ;
        List<Predicate> left = new ArrayList<>();
        String sFacts, sTarget, parts[];
                
        
        int delimiter = input.indexOf(":-");
        
        try {
            sTarget = input.substring(0, delimiter);
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            throw new PredicateParseException("Неправильный формат ввода: "
                    + input);
        }
        
        right = parseItemQ(sTarget);
        
        sFacts = input.substring(delimiter+2);
        
        parts = sFacts.split("\\)");
        
        for (String it : parts)
        {
            if (it.startsWith(","))
                    it = it.substring(1);
            try {
                left.add(parseItemQ(it));
            }
            catch (PredicateParseException ppe)
            {
               throw new PredicateParseException(ppe.getMessage());
            }
        }        
        args.clear();
        
        return logFactory.createPrologRule(left, right);
    }

    /**
     * Парсинг строки вида ?NAME (arg1,arg2,...)
     * @param input
     * @return Predicate
     * @throws PredicateParseException - если предикат неизвестного типа или с неверным кол-вом аргументов
     * @throws PredicateCreateException - если невозможно создать предикат
     */
    private  Predicate parseTarget(String input) throws PredicateParseException, PredicateCreateException {
        input = input.replaceAll("\\?", "");
        input = input.replaceAll("\\!", "");
        return parseFact(input);
    }
    
}