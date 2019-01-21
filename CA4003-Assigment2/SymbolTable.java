import java.util.*;

public class SymbolTable extends Object {
	private Hashtable<String, LinkedList<String>> symbolTable;
    private Hashtable<String, String> vals;
    private Hashtable<String, String> types;
    
    SymbolTable() {
        symbolTable = new Hashtable<>();
        vals = new Hashtable<>();
        types = new Hashtable<>();
        symbolTable.put("global", new LinkedList<>());
    }

    public void insert(String id, String value, String type, String scope) {
        LinkedList<String> tmp = symbolTable.get(scope);
        if (tmp == null) {
            tmp = new LinkedList<>();
            tmp.add(id);
            symbolTable.put(scope, tmp);
        } else {
            tmp.addFirst(id);
        }
        vals.put(id + scope, value);
        types.put(id + scope, type);
    }

    public void printSymbolTable() {
        String scope;
        Enumeration e = symbolTable.keys();
        while (e.hasMoreElements()) {
            scope = (String) e.nextElement();
            System.out.println("\nScope: " + scope + "\n-------\n");
            LinkedList<String> list = symbolTable.get(scope);
            for (String id : list) {
                String value = vals.get(id + scope);
                String type = types.get(id + scope);
                System.out.print(id + ": " + value + "(" + type + ")" + "\n");
            }
        }
    }
    

    public String getType(String id, String scope) {
        LinkedList<String> list = symbolTable.get(scope);
        LinkedList<String> globalList = symbolTable.get("global");
        if(list!= null){
            for (String matchingId : list) {
                if(matchingId.equals(id)) {
                    return types.get(id + scope);    
                }   
            }
        }
        if(globalList!= null){
            for (String matchingId : globalList) {
                if(matchingId.equals(id)) {
                    return types.get(id + "global");    
                }
            }
        }
        return "";
    }

    public ArrayList<String> getFunctionIds() {
        LinkedList<String> tmp = symbolTable.get("global");
        ArrayList<String> functionIds = new ArrayList<String>();
        for (String id : tmp){
            if(vals.get(id + "global")!= null){
                String functionId = vals.get(id + "global");
                if(functionId.equals("function")){
                    functionIds.add(id);
                }
            }
        }
        return functionIds;
    }
    
    public ArrayList<String> getFunctionParameters(String scope) {
        ArrayList<String> parameters = new ArrayList<String>();
        LinkedList<String> list = symbolTable.get(scope);
        if(list != null){
            for (String id : list) {
                String value = vals.get(id + scope);
                if(value.equals("parameter")) {
                    parameters.add(types.get(id + scope));
                }
            }
        }
        return parameters;   
    }

    public boolean isConstant(String id , String scope){
        LinkedList<String> list = symbolTable.get(scope);
        LinkedList<String> globalList = symbolTable.get("global");
        boolean check = false;
        if(list!=null && list.contains(id)){
            if(vals.get(id + scope).equals("constant")){
                check = true;
            }
        } 
        else if(globalList!=null && globalList.contains(id)){
            if(vals.get(id + "global").equals("constant")){
                check = true;
            }
        }
        return check;
    }

    public void dupsInScopes(){
        Set<String>keys = symbolTable.keySet();
        Hashtable<String, LinkedList<String>> scopeDuplicates = new Hashtable<String, LinkedList<String>>();
        for(String key : keys) {
            LinkedList<String> tmpList = symbolTable.get(key);
            while(0 < tmpList.size() -1){
                if (tmpList.size() > 0) {
                    String checker = tmpList.pop();
                    if(tmpList.contains(checker)){
                        System.out.println("\tIdentifier '" + checker + "' declared more that once in scope of " + key);
                    }
                }
            }
        }
    }


    public Hashtable <String, Hashtable <String, ArrayList<Integer>>> getDeclations() {
        Hashtable <String, Hashtable <String, ArrayList<Integer>>> scopeDeclarations = new Hashtable <String, Hashtable< String, ArrayList<Integer>>>();
        String scope;
        Enumeration e = symbolTable.keys();
        while (e.hasMoreElements()) {
            scope = (String) e.nextElement();
            LinkedList<String> scopeTable = symbolTable.get(scope);
            Hashtable< String, ArrayList<Integer>> declarations = new Hashtable< String, ArrayList<Integer>>();
            for (String id : scopeTable) {
                String value = vals.get(id + scope);
                if(value.equals("variable")) {
                    ArrayList<Integer> booleanList = new ArrayList<>(Arrays.asList(0,0));
                    declarations.put(id, booleanList);
                }
                else if(value.equals("constant")) {
                    ArrayList<Integer> booleanList = new ArrayList<>(Arrays.asList(1,0));
                    declarations.put(id, booleanList);

                }
                
            }
            scopeDeclarations.put(scope, declarations);
        }
        return scopeDeclarations;
    }
    
    public boolean isFunction(String id) {
        boolean isFunction = false;
        if(vals.get(id + "global")!=null) {
            if(vals.get(id + "global").equals("function")){
            isFunction = true;
            }
        }
        return isFunction;
    }
}