import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.List;

public class ASTAnalyser {

    private MySymbolTable symbolTable = new MySymbolTable();
    private int fields = 0;

    public void createSymbolTable(JmmNode node) {

        /* Analyse node received */
        List<String> attributes = node.getAttributes();
        for (String att : attributes) {
            switch (att) {
                case "main_package":
                    this.symbolTable.imports.add(node.get(att));
                    break;
                case "extension_name":
                    this.symbolTable.classExtends = node.get(att);
                    break;
                case "class_name":
                    this.symbolTable.className = node.get(att);
                    break;
                case "var_name":
                    String type;
                    try {
                        type = node.getChildren().get(0).get("type");
                    } catch (IndexOutOfBoundsException | NullPointerException e) {
                        break;
                    }
                    boolean isArray = false;
                    if (type.equals("[]")) {
                        type = "int";
                        isArray = true;
                    }
                    Type type1 = new Type(type, isArray);
                    Symbol symbol = new Symbol(type1, node.get(att));
                    if (this.fields == 0) {
                        this.symbolTable.fields.add(symbol);
                    } else {
                        if (node.getParent().getKind().equals("Function_parameters")) {
                            this.symbolTable.methodsArguments.get(this.fields - 1).add(symbol);
                        } else {
                            this.symbolTable.methodsVariables.get(this.fields - 1).add(symbol);
                        }

                    }
                    break;
                case "function_name":
                    this.fields++;
                    String type2;
                    ArrayList<Symbol> symbols = new ArrayList<>();
                    if (node.get(att).equals("main")) {
                        type2 = "void";
                        symbols.add(new Symbol(new Type("String", true), node.get("argument_name")));
                    } else {
                        type2 = node.getChildren().get(0).get("type");
                    }
                    boolean isArray2 = false;
                    if (type2.equals("[]")) {
                        type2 = "int";
                        isArray2 = true;
                    }
                    Type type3 = new Type(type2, isArray2);
                    Symbol symbol2 = new Symbol(type3, node.get(att));
                    this.symbolTable.methods.add(symbol2);
                    ArrayList<Symbol> symbols2 = new ArrayList<>();
                    this.symbolTable.methodsVariables.add(symbols2);
                    this.symbolTable.methodsArguments.add(symbols);
                    break;
                case "parameter_name":
                    String type4 = node.getChildren().get(0).get("type");
                    boolean isArray3 = false;
                    if (type4.equals("[]")) {
                        type4 = "int";
                        isArray3 = true;
                    }
                    Type type5 = new Type(type4, isArray3);
                    Symbol symbol3 = new Symbol(type5, node.get(att));
                    this.symbolTable.methodsArguments.get(this.fields - 1).add(symbol3);
                    break;
                default:
                    break;
            }
        }

        /* Take care of children - recursion */
        List<JmmNode> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            createSymbolTable(children.get(i));
        }

    }

    public MySymbolTable getSymbolTable() {
        return this.symbolTable;
    }
}
