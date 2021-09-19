import java.util.ArrayList;
import java.util.List;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class MySymbolTable implements SymbolTable {

    public ArrayList<String> imports = new ArrayList<>();
    public String className;
    public String classExtends = "null";
    public ArrayList<Symbol> fields = new ArrayList<>();
    public ArrayList<Boolean> initFields = new ArrayList<>();
    // Treat methods like Symbols so that the name can be the name of the method and the Type the return type.
    public ArrayList<Symbol> methods = new ArrayList<>();
    public ArrayList<ArrayList<Symbol>> methodsArguments = new ArrayList<ArrayList<Symbol>>();
    public ArrayList<ArrayList<Symbol>> methodsVariables = new ArrayList<ArrayList<Symbol>>();
    public ArrayList<ArrayList<Boolean>> initMethodsVariables = new ArrayList<ArrayList<Boolean>>();

	/**
     * @return a list of fully qualified names of imports
     */
    public List<String> getImports() {
        return imports;
    }

    /**
     * @return the name of the main class
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return the name that the classes extends, or null if the class does not extend another class
     */
    public String getSuper() {
        return classExtends;
    }

    /**
     * @return a list of Symbols that represent the fields of the class
     */
    public List<Symbol> getFields() {
        return fields;
    }
    
    /**
     * @return a list with the names of the methods of the class
     */
    public List<String> getMethods() {
        ArrayList<String> methodsNames = new ArrayList<>();
        for (Symbol s : methods) {
            methodsNames.add(s.getName());
        }
        return methodsNames;
    }

    /**
     * @return the return type of the given method
     */
    public Type getReturnType(String methodName) {
        for (Symbol s : methods) {
            if (s.getName().equals(methodName))
                return s.getType();
        }
        return null;
    }

    /**
     * @param methodName
     * @return a list of parameters of the given method
     */
    public List<Symbol> getParameters(String methodName) {
        int i = 0;
        for (Symbol s : methods) {
            if (s.getName().equals(methodName))
                break;
            i++;
        }
        return methodsArguments.get(i);
    }

    /**
     * @param methodName
     * @return a list of local variables declared in the given method
     */
    public List<Symbol> getLocalVariables(String methodName) {
        int i = 0;
        for (Symbol s : methods) {
            if (s.getName().equals(methodName))
                break;
            i++;
        }
        return methodsVariables.get(i);
    }

    @Override
    public String print() {
        String toString = "";
        toString += "\nSYMBOL TABLE\n";

        List<String> imports = getImports();
        for (String i : imports)
            toString += "Import: " + i + "\n";

        toString += "Class Name: " + getClassName() + "\n";
        toString += "Extends: " + getSuper() + "\n";

        List<Symbol> fields = getFields();
        for (Symbol f : fields)
            toString += "Field: " + f.getType() + " " + f.getName() + "\n";

        List<String> methods = getMethods();
        for (String m : methods) {
            toString += "Method: " + m + "\n";
            toString += "Method Return: " + getReturnType(m).getName() +"\n";
            List<Symbol> param = getParameters(m);
            for (Symbol p : param)
                toString += "Parameter: " + p.getType() + " " + p.getName() +"\n";
            List<Symbol> vars = getLocalVariables(m);
            for (Symbol v : vars)
                toString += "Local Variable: " + v.getType() + " " + v.getName() + "\n";
        }
        toString += "\n";

        return toString;
    }
}
