package pt.up.fe.comp.jmm.ast;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Expression2Ollir {

    private static final String auxStr = "aux";
    private int auxCounter = 1;

    private StringBuffer expressionCode = new StringBuffer();
    private JmmNode node;
    private SymbolTable st;
    private ArrayList<String> auxVariables = new ArrayList<>();
    private String method = "";

    LinkedList<JmmNode> Expressions2Analyse = new LinkedList<>();
    LinkedList<String> aux = new LinkedList<>();

    public Expression2Ollir(JmmNode node, SymbolTable st, String method) throws Exception {
        this.node = node;
        this.st = st;
        this.method = method;

        analyse(this.node);
    }

    private void getAllDot(JmmNode node){

        for (JmmNode child : node.getChildren()){
            if (child.getKind().equals("Dot"))
                Expressions2Analyse.add(child);

            getAllDot(child);
        }

    }

    private void analyse(JmmNode node) throws Exception {

        this.getAllDot(node);

        System.out.println("Inside Analysis");

        while (!Expressions2Analyse.isEmpty()) {

            JmmNode currentNode = Expressions2Analyse.pop();

            String var = currentNode.getChildren().get(0).get("var_name");
            String function = currentNode.getChildren().get(1).get("var_name");

            //Println
            if (this.st.getImports().contains(var)) {

                String temp = "invokestatic(" + var + "," + "\"" + function + "\"";

                for (JmmNode child : currentNode.getChildren().get(1).getChildren()){
                    for (JmmNode grandchild : child.getChildren()){

                        if (grandchild.getChildren().size() == 0 && grandchild.getKind().equals("TerminalExpression")){
                            temp += "," + this.getTypeLocalVar(grandchild.get("var_name"));
                        }
                        else {
                            String subfunction = grandchild.getChildren().get(0).getChildren().get(1).get("var_name");
                            Type ret = st.getReturnType(subfunction);

                            // Pop aux var
                            /*if (ret.isArray()){
                                temp += ", aux" + auxCounter-- + ".array." + ret.getName();
                            }
                            else {*/
                                temp += ", aux" + auxCounter-- + "." + ret.getName();
                            //}

                        }


                    }

                }

                temp += ").V\n";
                expressionCode.append(temp);
            } else {
                // CASE : asssignment to local var
                // Ex.: a = s.add(a,b)
                if (Expressions2Analyse.isEmpty()){

                    JmmNode ExpressionNode = currentNode.getParent().getParent().getParent().getParent();

                    String assignmentName = "";
                    String rettype = "";

                    // GET THE VAR TO ASSIGN THE VALUE
                    for (int i = 0; i < ExpressionNode.getChildren().size(); i++){

                        JmmNode child = ExpressionNode.getChildren().get(i);
                        if (child.getKind().equals("Assignment") &&
                                child.getChildren().get(0).getChildren().get(0).getChildren().get(0).getChildren().get(0).get("var_name").equals(var) &&
                                child.getChildren().get(0).getChildren().get(0).getChildren().get(0).getChildren().get(1).get("var_name").equals(function)){

                            JmmNode assignedVar = ExpressionNode.getChildren().get(i - 1).getChildren().get(0);

                            if (assignedVar.getKind().equals("Expression")
                                    && assignedVar.getChildren().get(0).getKind().equals("TerminalExpression")
                                    && assignedVar.getChildren().get(0).getChildren().size() == 0){

                                assignmentName = assignedVar.getChildren().get(0).get("var_name");

                                List<Symbol> vars = this.st.getLocalVariables(this.method);

                                for (Symbol s: vars){
                                    if (s.getName().equals(assignmentName)){

                                        assignmentName = assignmentName + ".";

                                        switch (s.getType().getName()){

                                            case "int":
                                                if (s.getType().isArray()) {
                                                    assignmentName += "array.i32 :-.array.i32";
                                                    rettype = "array.i32";
                                                }
                                                else{
                                                    assignmentName += "i32 :-.i32";
                                                    rettype = "i32";
                                                }
                                                break;
                                            case "boolean":
                                                assignmentName += "bool :-.bool";
                                                rettype = "bool";
                                                break;
                                            default:
                                                assignmentName += s.getName() + " :-." + s.getName();
                                                rettype = s.getName();
                                                break;
                                        }
                                        break;
                                    }
                                }

                            }

                        }
                    }

                    if (assignmentName.equals("") || rettype.equals("")){
                        throw new Exception("You done goofed son");
                    }
                    // write the function in OLLIR
                    else {
                        assignmentName += " invokevirtual(" + var + "," + "\"" + function + "\"";


                        for (JmmNode child : currentNode.getChildren().get(1).getChildren()){
                            for (JmmNode grandchild : child.getChildren()){

                                if (grandchild.getChildren().size() == 0 && grandchild.getKind().equals("TerminalExpression")){
                                    assignmentName += "," + this.getTypeLocalVar(grandchild.get("var_name"));
                                }
                                else {
                                    String subfunction = grandchild.getChildren().get(0).getChildren().get(1).get("var_name");
                                    Type ret = st.getReturnType(subfunction);

                                    // Pop aux var
                                    /*if (ret.isArray()){
                                        temp += ", aux" + auxCounter-- + ".array." + ret.getName();
                                    }
                                    else {*/
                                    assignmentName += ", aux" + auxCounter-- + "." + ret.getName();
                                    //}

                                }


                            }

                        }

                        assignmentName += ")." + rettype;
                        expressionCode.append(assignmentName);

                    }
                }
                else {
                    // CASE : asssignment to auxiliary var
                    // Ex.: s.add(a,s.add(a,b))
                    String type = this.st.getReturnType(function).getName();

                    String temp = "";

                    switch (type){
                        case "int":
                            temp += this.auxStr + this.auxCounter++ + ".i32 :=.i32";
                            break;
                        case "bool":
                            temp += this.auxStr + this.auxCounter++ + ".bool :=.bool";
                            break;
                        default:
                            temp += this.auxStr + this.auxCounter++ + "." + type + ":=." + type;
                            break;
                    }

                    temp +=  "invokevirtual(" + var + "," + "\"" + function + "\"";

                    for (JmmNode child : currentNode.getChildren().get(1).getChildren()){
                        for (JmmNode grandchild : child.getChildren()){

                            if (grandchild.getChildren().size() == 0 && grandchild.getKind().equals("TerminalExpression")){
                                temp += "," + this.getTypeLocalVar(grandchild.get("var_name"));
                            }
                            else {
                                String subfunction = grandchild.getChildren().get(0).getChildren().get(1).get("var_name");
                                Type ret = st.getReturnType(subfunction);

                                // Pop aux var
                            /*if (ret.isArray()){
                                temp += ", aux" + auxCounter-- + ".array." + ret.getName();
                            }
                            else {*/
                                temp += ", aux" + auxCounter-- + "." + ret.getName();
                                //}

                            }


                        }

                    }

                    temp += ").V\n";
                    expressionCode.append(temp);



                }

            }

        }
    }

    private String getTypeLocalVar(String var_name){
        String var_type = "";

        List<Symbol> vars = this.st.getLocalVariables(this.method);

        for (Symbol s: vars){
            if (s.getName().equals(var_name)){

                var_type = var_name + ".";

                switch (s.getType().getName()){

                    case "int":
                        if (s.getType().isArray())
                            var_type += "array.i32";
                        else
                            var_type += "i32";
                        break;
                    case "boolean":
                        var_type += "bool";
                        break;
                    default:
                        var_type += s.getName();
                        break;
                }
                break;
            }
        }


        return var_type;
    }


    public StringBuffer getExpressionCode() {
        return expressionCode;
    }
}
