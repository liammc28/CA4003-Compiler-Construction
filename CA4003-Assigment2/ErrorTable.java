import java.util.*;

public class ErrorTable extends Object {
	private Hashtable<String, Hashtable<String, LinkedList<String>>> errorTable;
    
    ErrorTable() {
        errorTable = new Hashtable<>();
    }

    public void insert(String scope, String id,String error) {
        if(errorTable.get(scope) != null){
            LinkedList<String> tmp = errorTable.get(scope).get(id);
            Hashtable<String, LinkedList<String>>  tmpTable = errorTable.get(scope);
            if (tmp == null) {
                tmp = new LinkedList<String>();
                tmp.add(error);
                tmpTable.put(id,tmp);
                errorTable.put(scope, tmpTable);
            } 
            else {
                tmp.addFirst(error);
            }
        }
        else{
            Hashtable<String, LinkedList<String>> newError = new Hashtable<String, LinkedList<String>>();
            LinkedList<String> tmpErrorList = new LinkedList<String>();
            tmpErrorList.addFirst(error);
            newError.put(id,tmpErrorList);
            errorTable.put(scope,newError);
        }
    }

    public void printErrors(){
        Enumeration e = errorTable.keys();
        System.out.println();
            System.out.println();
            System.out.println("--------------------------------------");
            System.out.println("Semantic Errors");
            System.out.println("--------------------------------------");
            System.out.println();
        if(e.hasMoreElements()){
            while (e.hasMoreElements()) {
                String scope = (String) e.nextElement();
                errorTable.get(scope);
                Enumeration e2 = errorTable.get(scope).keys();
                System.out.println("Errors in " + scope +":");
                System.out.println("----------------");
                while(e2.hasMoreElements()){
                    String id = (String) e2.nextElement();
                    LinkedList<String> errorList = errorTable.get(scope).get(id);
                    LinkedList<String>  errorListWithoutDuplicates = new LinkedList<>(new LinkedHashSet<>(errorList));//removes duplicate errors
                    String pural = "";
                    if(errorListWithoutDuplicates.size()>1){
                        pural = "s";
                    }
                    System.out.println("\tError" + pural + " with " + id +":");
                    for (String error : errorListWithoutDuplicates) {
                        System.out.println("\t\t" + error);
                    }
                    System.out.println();
                }
                System.out.println();
            }
        }  
        else{
            System.out.println("This file contains no Semantic errors");            
        }      
    }
}