import java.util.*;
public class ThreeAddrCode implements MyParserVisitor {

    private static int numLabels = 1;
    private static int tNum = 1;
    private static int parameterNum = 1;
    private static Stack<Integer> labelStack = new Stack<Integer>();

    public Object visit(SimpleNode node, Object data){
        throw new RuntimeException("Visit SimpleNode");
    }

    public Object visit(Program node, Object data){
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(Declaration_List node, Object data){
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(Variable_Declaration node, Object data){
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(Constant_Declaration node, Object data){
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(Identifier node, Object data){
        return node.value;
    }

    public Object visit(Number node, Object data){
        if(Integer.parseInt((String)node.value) < 0) {
            String parent = (node.jjtGetParent()).toString();
            if("Assignment".equals(parent)){
                return node.value;
            }
            else{
                String t = "t" + tNum;
                tNum++;
                System.out.println("\t" + t + " = " + node.value);
                return t;
            }
            
        }
        return node.value;
    }   

    public Object visit(Function_List node, Object data){
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(Function node, Object data){
        SimpleNode function = (SimpleNode) node.jjtGetChild(1);
        System.out.println((String) function.value + ":");
        node.childrenAccept(this, data);
        parameterNum = 1;
        return "function";
    }

    public Object visit(FunctionReturn node, Object data){
        if(node.jjtGetNumChildren() !=0){
            String child = (String) node.jjtGetChild(0).jjtAccept(this, data);
            System.out.println("\treturn " + child);
        }   
        return data;
    }

    public Object visit(Type node, Object data){

        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(Parameter_List node, Object data){

        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(Parameter node, Object data){
        String child = (String) node.jjtGetChild(0).jjtAccept(this, data);
        System.out.println("\t" + child + " = getparam " + parameterNum);
        parameterNum++;
        node.jjtGetChild(1).jjtAccept(this, data);
        return data;
    }

    public Object visit(Main node, Object data){
        System.out.println("main:");
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(Statement_Block node, Object data){
         node.childrenAccept(this, data);
        return data;
    }

    public Object visit(Assignment node, Object data){

        String child1 = (String) node.jjtGetChild(0).jjtAccept(this, data);
        String child2 = (String) node.jjtGetChild(1).jjtAccept(this, data);
        System.out.println("\t" + child1 + " " + "=" + " " + child2 );
        return data;

    }

    public Object visit(Statement node, Object data){

        if(((String)node.value).equals("if")){
            node.jjtGetChild(0).jjtAccept(this, data);
            int tmpCount = tNum -1;
            System.out.println("\tifz t" + tmpCount + " goto L" + numLabels);
            numLabels++;
            labelStack.push(numLabels);
            node.jjtGetChild(1).jjtAccept(this, data);
            System.out.println("\tgoto L" + numLabels);
            int tempLabel = numLabels -1;
            numLabels++;
            System.out.println("L" + tempLabel + ":");
            node.jjtGetChild(2).jjtAccept(this, data);
            System.out.println("L"+ labelStack.pop() + ":");
            return "if";
        }
        else if(((String)node.value).equals("while")){
            
            System.out.println("L" + numLabels + ":");
            numLabels++;
            labelStack.push(numLabels);
            node.jjtGetChild(0).jjtAccept(this, data);
            int tmpCount = tNum -1; 
            System.out.println("\tifz t" + tmpCount + " goto L" + numLabels);
            numLabels++;
            node.jjtGetChild(1).jjtAccept(this, data);
            int tempNumLabel = labelStack.pop();
            int tempLabel = tempNumLabel -1;
            System.out.println("\tgoto L" + tempLabel);
            System.out.println("L"+ tempNumLabel + ":");
            return "while";
        }
        else{
            node.jjtGetChild(0).jjtAccept(this, data);
            return data;
        }
    }

    public Object visit(Skip node, Object data){

        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(Arith_Operator node, Object data){
        return operatorVisitor(node,data);
    }

    public Object visit(Logical_Operator node, Object data){
        return operatorVisitor(node,data);
    }

    public Object visit(Negative node, Object data){
        String child = (String) node.jjtGetChild(0).jjtAccept(this, data);
        String parent = (node.jjtGetParent()).toString();
        if("Assignment".equals(parent)){
            return "-" + child;
        }
        String t = "t" + tNum;
        tNum++;
        System.out.println("\t" + t + " = -" + child);
        return t;

    }

    public Object visit(Boolean node, Object data){
        return (String) node.value;
    }

    public Object visit(FunctionCall node, Object data){
        int count = printArgs((Arg_List) node.jjtGetChild(1), data);
        String child = (String) node.jjtGetChild(0).jjtAccept(this, data);
        String function = child + "() " + count;
        String parent = (node.jjtGetParent()).toString();
        if("Statement_Block".equals(parent)){
            System.out.println("\t" + function);
        }
        return function;
    }

    public Object visit(Comp_Op node, Object data){
        return operatorVisitor(node,data);
    }

    public Object visit(Arg_List node, Object data){

        node.childrenAccept(this, data);
        return data;
    }

    private int printArgs(Arg_List node, Object data){
        int count = 0;
        while(node.jjtGetNumChildren() != 0) {
            count++;
            System.out.println("\tparam " + node.jjtGetChild(0).jjtAccept(this, data));
            node = (Arg_List)node.jjtGetChild(1);
        }   
        return count;
    }

    private Object operatorVisitor(SimpleNode node, Object data){
        String child1 = (String) node.jjtGetChild(0).jjtAccept(this, data);
        String child2 = (String) node.jjtGetChild(1).jjtAccept(this, data);
        String parent = (node.jjtGetParent()).toString();
        if("Assignment".equals(parent)){
            return (child1 + " " + node.value + " " + child2);
        }
        String t = "t" + tNum;
        tNum++;
        System.out.println("\t" + t + " = " + child1 + " " + node.value + " " + child2 );
        return t;
    }
}