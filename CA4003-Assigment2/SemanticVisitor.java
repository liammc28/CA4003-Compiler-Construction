import java.util.*;

public class SemanticVisitor implements MyParserVisitor {

    private static String scope;
    private static SymbolTable st;
    private static ArrayList<String> functionIds;
    private static Hashtable <String, Hashtable <String, ArrayList<Integer>>>  scopeWrittenAndRead;
    private static ArrayList<String> parentTypes = new ArrayList<String>(Arrays.asList("Arg_List" , "FunctionReturn", "Arith_Operator", "Negative", "comp_Op"));
    
    public static ErrorTable errorTable = new ErrorTable();

    @Override
    public Object visit(SimpleNode node, Object data){
        throw new RuntimeException("Visit SimpleNode");
    }

    @Override
    public Object visit(Program node, Object data){
        scope = "global";
        st = (SymbolTable) data;
        functionIds = st.getFunctionIds();

        scopeWrittenAndRead = st.getDeclations();

        node.childrenAccept(this, data);

        Enumeration e = scopeWrittenAndRead.keys();
        String scopeKey;
        while(e.hasMoreElements()) {
              scopeKey = (String) e.nextElement();
              Enumeration e1 = scopeWrittenAndRead.get(scopeKey).keys();
              while(e1.hasMoreElements()) {
                String id = (String) e1.nextElement();
                ArrayList<Integer> idList = scopeWrittenAndRead.get(scopeKey).get(id);
                if(idList.get(0) == 0){
                    errorTable.insert(scopeKey,id,"Identifier has not been written to");
                }
                if(idList.get(1) == 0) {
                    errorTable.insert(scopeKey,id,"Identifier has not been read from");
                }
              }
        }

        if(functionIds.size() > 0){
            for(String functionName : functionIds){
                errorTable.insert("global","functions not called",functionName);
            }   
        }

        errorTable.printErrors();

        st.dupsInScopes();

        return data;
    }

    @Override
    public Object visit(Function_List node, Object data){
        node.childrenAccept(this, data);
        return data;
    }

    @Override
    public Object visit(Function node, Object data){
        SimpleNode functionTypeString = (SimpleNode) node.jjtGetChild(0);
        SimpleNode functionIDString = (SimpleNode) node.jjtGetChild(1);
        SimpleNode functionReturnString = (SimpleNode) node.jjtGetChild(5);
        DataType functionType = (DataType) node.jjtGetChild(0).jjtAccept(this, data);
        FunctionReturn returnType = (FunctionReturn) node.jjtGetChild(5);

        node.childrenAccept(this, data);
        if(returnType.jjtGetNumChildren() == 0) {
            if(functionType != DataType.type_unknown){
                errorTable.insert(scope,"return statement","Function must return a " + functionTypeString.value);
            }
        }
        else{
            if(functionType == DataType.type_unknown) {
                errorTable.insert(scope,"return statement","Void function should not return anything");
            }
            else {
                DataType returnTypeData = (DataType) returnType.jjtGetChild(0).jjtAccept(this, data);
                if(functionType  != returnTypeData) {
                    errorTable.insert(scope,"return statement","Function must return a " + functionTypeString.value);
                }
            }
        }
        scope = "global";
        return DataType.function;
    }

    @Override
    public Object visit(FunctionReturn node, Object data){
        DataType id = DataType.type_unknown;
        if(node.jjtGetNumChildren() !=0){
            id = (DataType) node.jjtGetChild(0).jjtAccept(this,data);
        }
        else{
            node.childrenAccept(this,data);
        }       
        return id;
    }

    @Override
    public Object visit(Declaration_List node, Object data){
        node.childrenAccept(this, data);
        return data;
    }

    @Override
    public Object visit(Variable_Declaration node, Object data) {
        node.childrenAccept(this, data);
        return DataType.var_decl;
    }

    @Override
    public Object visit(Constant_Declaration node, Object data){
        SimpleNode id = (SimpleNode) node.jjtGetChild(0);
        DataType child2 = (DataType) node.jjtGetChild(1).jjtAccept(this, data);
        DataType child3 = (DataType) node.jjtGetChild(2).jjtAccept(this, data);
        if(child2 != child3){
            errorTable.insert(scope,(String)id.value,"Constant declaration of type " + child2 + " can not be assigned to type " + child3);
        }
        return DataType.const_decl;
    }



    @Override
    public Object visit(Identifier node, Object data){
        SimpleNode parent = (SimpleNode) node.jjtGetParent();

        String value = (String) node.jjtGetValue();
       
        String nodeValue = (String) node.value;
        if(parentTypes.contains(parent.toString())){
          ArrayList<Integer> b = new ArrayList<Integer>();

          if(nodeValue != null && scopeWrittenAndRead.get(scope) != null){
            if(scopeWrittenAndRead.get(scope).get(nodeValue) != null){
                System.out.println();
                b = scopeWrittenAndRead.get(scope).get(nodeValue);
                b.set(1,1);
                scopeWrittenAndRead.get(scope).put(nodeValue, b);
            }
            else if(scopeWrittenAndRead.get("global").get(nodeValue) != null){
                b = scopeWrittenAndRead.get("global").get(nodeValue); 
                b.set(1,1);
                scopeWrittenAndRead.get("global").put(nodeValue, b);
            }
          }
            
        }

        if(parent.toString() == "Variable_Declaration"){
            return DataType.var_decl;
        }
        else if(parent.toString() == "Constant_Declaration"){
            return DataType.const_decl;
        }
        else if(parent.toString() == "Function"){
            String functionId = (String)node.jjtGetValue();
            scope = functionId;
            return DataType.function;
        }
        else if(st.getType(value, scope).equals("")){
            errorTable.insert(scope,value,"Identifier is not declared within scope");
        }
        else if(st.getType(value, scope).equals("integer")) {
          return DataType.Num;
        }
        else if(st.getType(value, scope).equals("boolean")) {
          return DataType.bool;
        }
        
        return DataType.type_unknown;   
    }

    @Override
    public Object visit(Number node, Object data){
        return DataType.Num;
    }



    @Override
    public Object visit(Type node, Object data){
        String s = (String)node.jjtGetValue();
        if(s.equalsIgnoreCase("boolean")){
            return DataType.bool;
        }
        if(s.equalsIgnoreCase("void")){
            return DataType.type_unknown;
        }
        if(s.equalsIgnoreCase("integer")){
            return DataType.Num;
        }
        return DataType.type_unknown;
    }

    @Override
    public Object visit(Parameter_List node, Object data){
        node.childrenAccept(this, data);
        return data;
    }

    @Override
    public Object visit(Parameter node, Object data){
        node.childrenAccept(this, data);
        return data;
    }

    @Override
    public Object visit(Main node, Object data){
        scope = "main";
        node.childrenAccept(this, data);
        return data;

    }

    @Override
    public Object visit(Statement_Block node, Object data){
        node.childrenAccept(this, data);
        return data;
    }

    @Override
    public Object visit(Statement node, Object data){
        node.childrenAccept(this, data);
        return data;
    }

    @Override
    public Object visit(Assignment node, Object data){
        SimpleNode child1 = (SimpleNode) node.jjtGetChild(0);
        String child1Value = (String) child1.value;
        ArrayList<Integer> x = new ArrayList<Integer>();
        if(scopeWrittenAndRead.get(scope) != null && scopeWrittenAndRead.get(scope).get(child1Value) != null){
          x = scopeWrittenAndRead.get(scope).get(child1Value); 
          x.set(0,1);
          scopeWrittenAndRead.get(scope).put(child1Value, x);
        }
        else if(scopeWrittenAndRead.get("global").get(child1Value) != null){
            x = scopeWrittenAndRead.get("global").get(child1Value); 
            x.set(0,1);
            scopeWrittenAndRead.get("global").put(child1Value, x);
        }
        
        if (st.isConstant(child1Value,scope)){
            errorTable.insert(scope,child1Value,"Constant can not be assigned a new value");
        }

        SimpleNode child2 = (SimpleNode) node.jjtGetChild(1);
        String child2Value = (String) child2.value;
        ArrayList<Integer> b = new ArrayList<Integer>();
        if(child2Value != null && scopeWrittenAndRead.get(scope) != null && scopeWrittenAndRead.get(scope).get(child2Value) != null){
              b = scopeWrittenAndRead.get(scope).get(child2Value); 
              b.set(1,1);
              scopeWrittenAndRead.get(scope).put(child2Value, b);
        }
        else if(child2Value != null && scopeWrittenAndRead.get("global").get(child2Value) != null){
            b = scopeWrittenAndRead.get("global").get(child2Value); 
            b.set(1,1);
            scopeWrittenAndRead.get("global").put(child2Value, b);
        }
        
        
        DataType child1DataType = (DataType) child1.jjtAccept(this, data);
        DataType child2DataType = (DataType) node.jjtGetChild(1).jjtAccept(this, data);


        if (child1DataType == child2DataType) {
            return DataType.assign;
        }
        else {
            errorTable.insert(scope,child1Value,"Identifier assignment types do not correspond, cannot assign " + child1DataType + " to a " + child2DataType);
        }
        return DataType.type_unknown;
    }

    @Override
    public Object visit(Skip node, Object data){
        return data;
    }

    @Override
    public Object visit(Arith_Operator node, Object data){

        SimpleNode parent = (SimpleNode) node.jjtGetParent();
        SimpleNode parentID = (SimpleNode)parent.jjtGetChild(0);
        
        DataType firstChild = (DataType) node.jjtGetChild(0).jjtAccept(this, data);
        DataType secondChild = (DataType) node.jjtGetChild(1).jjtAccept(this, data);
        String operation = "add";
        while(!parent.toString().equals("Assignment")){
            parent = (SimpleNode) parent.jjtGetParent();

        }
        SimpleNode simpleId = (SimpleNode) parent.jjtGetChild(0);
        String id = (String) simpleId.value; 
        if((String)node.value == "-"){
            operation = "subtract";
        }
        if(firstChild != DataType.Num | secondChild != DataType.Num) {
            errorTable.insert(scope,id,"Arithmetic '" + node.value + "' Check Failed: Cannot " + operation + " " + firstChild +
                    " and " + secondChild);
            return DataType.type_unknown;
          }
        return DataType.Num;
    }

    @Override
    public Object visit(FunctionCall node, Object data){
        SimpleNode firstChild = (SimpleNode) node.jjtGetChild(0);
        if(st.isFunction((String)firstChild.value)){
            ArrayList<String> argsType = new ArrayList<String>();
            argsType = getArgsTypes((Arg_List) node.jjtGetChild(1),data);
            Collections.reverse(argsType);
            ArrayList<String> functionParamTypes = st.getFunctionParameters((String)firstChild.value);
            if(argsType.contains("unknown")) {
                errorTable.insert(scope,(String)firstChild.value,"argument is not declared before use");
            }
            else if (argsType.size() != functionParamTypes.size()) {
                errorTable.insert(scope,(String)firstChild.value,(String)firstChild.value + " called with incorrect number of args");
            }
            else if(!argsType.equals(functionParamTypes)) {
                errorTable.insert(scope,(String)firstChild.value,(String)firstChild.value + " called with arguments of incorrect type");
            }


            DataType firstChildDataType = (DataType) firstChild.jjtAccept(this, data);

            node.childrenAccept(this, data);
            if(functionIds.contains((String)firstChild.value)) {
                functionIds.remove((String)firstChild.value);
            }

            if(st.getType((String)firstChild.value, "global").equals("integer")) {
                return DataType.Num;
            }
            else if(st.getType((String)firstChild.value, "global").equals("boolean")) {
                return DataType.bool;
            }
            else if(st.getType((String)firstChild.value, "global").equals("void")) {
                return DataType.type_unknown;
            }
        }
        else {
            errorTable.insert(scope,"function " + (String)firstChild.value,"Function does not exist.");
        }

        return DataType.type_unknown;
    }

    @Override
    public Object visit(Logical_Operator node, Object data){
        DataType firstChild = (DataType) node.jjtGetChild(0).jjtAccept(this, data);
        DataType secondChild = (DataType) node.jjtGetChild(1).jjtAccept(this, data);
        if(firstChild != DataType.bool | secondChild != DataType.bool) {
            errorTable.insert(scope,"in condition","Logical '" + (String)node.value + "' Check Failed: Cannot " + (String)node.value + " " + firstChild +
                    " and " + secondChild);
            return DataType.type_unknown;
          }
        return DataType.bool;
    }

    @Override
    public Object visit(Negative node, Object data){
        node.childrenAccept(this, data);
        return DataType.Num;

    }

    @Override
    public Object visit(Boolean node, Object data){
        node.childrenAccept(this, data);
        return DataType.bool;
    }

    @Override
    public Object visit(Comp_Op node, Object data){

        DataType child1DataType = (DataType) node.jjtGetChild(0).jjtAccept(this, data);
        DataType child2DataType = (DataType) node.jjtGetChild(1).jjtAccept(this, data);
        String nodeValue = (String) node.value;
        if(!checkOp(child1DataType, child2DataType, nodeValue)) {
            return DataType.type_unknown;
        }
        return DataType.bool;
    }


    @Override
    public Object visit(Arg_List node, Object data){
        node.childrenAccept(this, data);
        return data;
    }

    private boolean checkOp(DataType child1, DataType child2, String node) {
        boolean check = true;
        String type = "Comparison";
        String nodeValue = node;
        if(node.equals("=") || node.equals("!=")){
            type = "Boolean";
            if((child1 != DataType.bool | child2 != DataType.bool) & (child1 != DataType.Num | child2 != DataType.Num)){
                check = false;
                errorTable.insert(scope,"in condition",type + " '" + nodeValue + "' Check Failed!" + " Cannot compare " + child1 + " to: " + child2);
            }
        }
        else if((child1 != DataType.Num | child2 != DataType.Num) ){
            check = false;
            errorTable.insert(scope,"in condition",type + " '" + nodeValue + "' Check Failed!" + " Cannot compare " + child1 + " to: " + child2);
        }
        return check;
    }

    private ArrayList<String> getArgsTypes(Arg_List node, Object data){
        ArrayList<String> types = new ArrayList<String>();
        while(node.jjtGetNumChildren() != 0) {
            if((DataType) node.jjtGetChild(0).jjtAccept(this, data) == DataType.Num) {
                types.add("integer");
            }
            else if((DataType) node.jjtGetChild(0).jjtAccept(this, data) == DataType.bool) {
                types.add("boolean");
            }
            else {
                types.add("unknown");
            }
            node = (Arg_List)node.jjtGetChild(1);
        }
        return types;
    }
}