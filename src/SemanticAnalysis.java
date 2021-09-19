import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalysis {

    private MySymbolTable mySymbolTable;
    private String currentMethod;
    private List<Report> reports = new ArrayList<>();

    SemanticAnalysis(MySymbolTable mySymbolTable) {
        this.mySymbolTable = mySymbolTable;
        this.currentMethod = "";
    }

    public void typeVerification(JmmNode node) {

        String nodeName = node.getKind();

        if (nodeName.equals("Array")) {
            if (node.getNumChildren() > 1) {
                if (node.getChildren().get(0).getNumChildren() > 0) {
                    if (!isArray(node.getChildren().get(0))) {
                        reports.add(new Report(ReportType.ERROR,
                                Stage.SEMANTIC,
                                -1,
                                "Array Access over other type other than array"));
                    }
                }
                if (node.getChildren().get(1).getKind().equals("ArrayExpression")) {
                    JmmNode auxNode = node.getChildren().get(1).getChildren().get(0);
                    if (auxNode.getChildren().get(0).equals("TerminalExpression")) {
                        if (!getType(auxNode).equals("int"))
                            reports.add(new Report(ReportType.ERROR,
                                    Stage.SEMANTIC,
                                    -1,
                                    "Array position different from integer"));
                    } else {
                        if (!getTypeExpression(auxNode).equals("int"))
                            reports.add(new Report(ReportType.ERROR,
                                    Stage.SEMANTIC,
                                    -1,
                                    "Array position different from integer"));
                    }
                }
            }
        } else if (nodeName.equals("NewArray")) {
            JmmNode auxNode = node.getChildren().get(0).getChildren().get(0);
            if (auxNode.equals("TerminalExpression")) {
                if (!getType(auxNode).equals("int"))
                    reports.add(new Report(ReportType.ERROR,
                            Stage.SEMANTIC,
                            -1,
                            "Array position different from integer"));
            } else {
                if (!getTypeExpression(auxNode.getParent()).equals("int"))
                    reports.add(new Report(ReportType.ERROR,
                            Stage.SEMANTIC,
                            -1,
                            "Array position different from integer"));
            }
        } else if (nodeName.equals("Assign")) {
            JmmNode leftSide = node.getChildren().get(0);
            JmmNode rightSide = node.getChildren().get(1);

            while (!leftSide.getKind().equals("TerminalExpression") && leftSide.getNumChildren() > 0) {
                leftSide = leftSide.getChildren().get(0);
            }
            String leftType = getType(leftSide);

            rightSide = rightSide.getChildren().get(1);
            while (!rightSide.getKind().equals("Expression") && rightSide.getNumChildren() > 0) {
                rightSide = rightSide.getChildren().get(0);
            }
            String rightType = getTypeExpression(rightSide);


            if (leftType.equals("NOT DEFINED") || rightType.equals("NOT DEFINED"))
                reports.add(new Report(ReportType.ERROR,
                        Stage.SEMANTIC,
                        -1,
                        "Assignment with undefined variable"));
            else if (!leftType.equals(rightType) && !leftType.equals("ASSUME") && !rightType.equals("ASSUME")) {
                reports.add(new Report(ReportType.ERROR,
                        Stage.SEMANTIC,
                        -1,
                        "Assignment of different types"));
            }
        } else if (nodeName.equals("IfElse") || nodeName.equals("While")) {
            if (!getTypeExpression(node.getChildren().get(0)).equals("boolean")) {
                reports.add(new Report(ReportType.ERROR,
                        Stage.SEMANTIC,
                        -1,
                        "If/While: Condition different from boolean"));
            }
        } else if (nodeName.equals("Method")) {
            this.currentMethod = node.get("function_name");
        } else if (nodeName.equals("Return")) {
            String returnType = getTypeExpression(node.getChildren().get(0));
            String sReturnType = this.mySymbolTable.getReturnType(node.getParent().get("function_name")).getName();
            if (sReturnType.equals("int") && this.mySymbolTable.getReturnType(node.getParent().get("function_name")).isArray())
                sReturnType = "int[]";
            if (!returnType.equals(sReturnType))
                reports.add(new Report(ReportType.ERROR,
                        Stage.SEMANTIC,
                        -1,
                        "Return type is not the same as the one present in method declaration"));
        } else if (nodeName.equals("Dot")) {
            String classType = getType(node.getChildren().get(0));
            JmmNode method = node.getChildren().get(1);
            if (method.getKind().equals("LEN")) {
                if (!classType.equals("int[]"))
                    reports.add(new Report(ReportType.ERROR,
                            Stage.SEMANTIC,
                            -1,
                            "Use of length over invalid type"));
            } else {
                String methodName = method.get("var_name");
                if (classType.equals("this") || classType.equals(this.mySymbolTable.getClassName()) || classType.equals("ASSUME")) {
                    if (!this.mySymbolTable.getMethods().contains(methodName)) {
                        if (this.mySymbolTable.getSuper().equals("null"))
                            reports.add(new Report(ReportType.ERROR,
                                    Stage.SEMANTIC,
                                    -1,
                                    "Use of undeclared method"));
                    } else {
                        if (method.getNumChildren() != this.mySymbolTable.getParameters(methodName).size()) {
                            if (this.mySymbolTable.getSuper().equals("null")) {
                                reports.add(new Report(ReportType.ERROR,
                                        Stage.SEMANTIC,
                                        -1,
                                        "Incorrect number of parameters"));
                            }
                        } else {
                            List<Symbol> param = this.mySymbolTable.getParameters(methodName);
                            List<Type> declParam = new ArrayList<>();
                            for (Symbol p : param) {
                                declParam.add(p.getType());
                            }
                            List<Type> usedParam = validParamenters(node.getChildren().get(1));
                            for (int i = 0; i < usedParam.size(); i++) {
                                if (!usedParam.get(i).equals(declParam.get(i))) {
                                    if (this.mySymbolTable.getSuper().equals("null")) {
                                        reports.add(new Report(ReportType.ERROR,
                                                Stage.SEMANTIC,
                                                -1,
                                                "Parameters don't match"));
                                    }
                                }
                            }
                        }
                    }
                } else if (!this.mySymbolTable.getImports().contains(classType)) {
                    reports.add(new Report(ReportType.ERROR,
                            Stage.SEMANTIC,
                            -1,
                            "Use of method from undeclared class"));
                }
            }
        } else if (nodeName.equals("VarDeclaration")) {
            JmmNode type = node.getChildren().get(0);
            if(!type.get("type").equals("int") && !type.get("type").equals("boolean") && !type.get("type").equals("[]")) {
                if (!this.mySymbolTable.getImports().contains(type.get("type")) && !this.mySymbolTable.getClassName().equals(type.get("type"))) {
                    reports.add(new Report(ReportType.ERROR,
                            Stage.SEMANTIC,
                            -1,
                            "Missing Type"));
                }
            }
        }

        /* Take care of children - recursion */
        List<JmmNode> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            typeVerification(children.get(i));
        }
    }

    public List<Type> validParamenters(JmmNode dotExpression) {
        List<JmmNode> params = dotExpression.getChildren();
        List<Type> pTypes = new ArrayList<>();
        String type = "";
        for (JmmNode p : params) {
            type = getTypeExpression(p);
            if (type.equals("int[]")) {
                pTypes.add(new Type("int", true));
            } else {
                pTypes.add(new Type(type, false));
            }
        }
        return pTypes;
    }

    // Recebe SEMPRE um nó do tipo Expression
    public String getTypeExpression(JmmNode node) {
        String nodeName = node.getChildren().get(0).getKind();

        if (nodeName.equals("Plus") ||
            nodeName.equals("Minus")||
            nodeName.equals("Mult") ||
            nodeName.equals("Div")  ||
            nodeName.equals("Less")) {

            List<JmmNode> operands = node.getChildren().get(0).getChildren();
            JmmNode leftOperand = operands.get(0).getChildren().get(0);
            JmmNode rightOperand = operands.get(1).getChildren().get(0);

            String leftType = "", rightType = "";

            if (leftOperand.getKind().equals("Expression"))
                leftType = getTypeExpression(leftOperand);
            else if (leftOperand.getKind().equals("Variable"))
                leftType = getType(leftOperand.getParent());
            else if (leftOperand.getKind().equals("Dot")) {
                if (leftOperand.getChildren().get(1).getAttributes().size() > 0) {
                    Type left = this.mySymbolTable.getReturnType(leftOperand.getChildren().get(1).get("var_name"));
                    leftType = left != null ? left.getName() : "null";
                } else {
                    leftType = "int";
                }
            } else if (leftOperand.getKind().equals("Bool"))
                leftType = "boolean";
            else if (leftOperand.getKind().equals("Integer"))
                leftType = "int";
            else if (leftOperand.getKind().equals("TerminalExpression") && leftOperand.getParent().getNumChildren() > 1) {
                if (leftOperand.getParent().getChildren().get(1).getKind().equals("LEN")) {
                    leftType = "int";
                } else if (leftOperand.getParent().getChildren().get(1).getKind().equals("ArrayExpression")) {
                    leftType = "int";
                } else if (leftOperand.getParent().getKind().equals("Div") ||
                        leftOperand.getParent().getKind().equals("Plus") ||
                        leftOperand.getParent().getKind().equals("Mult") ||
                        leftOperand.getParent().getKind().equals("Less") ||
                        leftOperand.getParent().getKind().equals("Minus")) {
                    leftType = "int";
                } else if (leftOperand.getParent().equals("Dot")) {
                    leftType = getTypeExpression(leftOperand.getParent().getParent().getParent());
                } else if (leftOperand.getParent().getParent().getKind().equals("Div") ||
                        leftOperand.getParent().getParent().getKind().equals("Plus") ||
                        leftOperand.getParent().getParent().getKind().equals("Mult") ||
                        leftOperand.getParent().getParent().getKind().equals("Less") ||
                        leftOperand.getParent().getParent().getKind().equals("Minus")) {
                    leftType = "int";
                }
            } else if (leftOperand.getKind().equals("Plus") ||
                    leftOperand.getKind().equals("Minus")||
                    leftOperand.getKind().equals("Mult") ||
                    leftOperand.getKind().equals("Div")  ||
                    leftOperand.getKind().equals("Less")) {
                leftType = "int";
            }

            if (rightOperand.getKind().equals("Expression"))
                rightType = getTypeExpression(rightOperand);
            else if (rightOperand.getKind().equals("Variable"))
                rightType = getType(rightOperand.getParent());
            else if (rightOperand.getKind().equals("Dot")) {
                if (rightOperand.getChildren().get(1).getAttributes().size() > 0) {
                    Type right = this.mySymbolTable.getReturnType(rightOperand.getChildren().get(1).get("var_name"));
                    rightType = right!=null?right.getName():"null";
                } else {
                    rightType = "int";
                }
            } else if (rightOperand.getKind().equals("Bool"))
                rightType = "boolean";
            else if (rightOperand.getKind().equals("Integer"))
                rightType = "int";
            else if (rightOperand.getKind().equals("TerminalExpression") && rightOperand.getParent().getNumChildren() > 1) {
                if (rightOperand.getParent().getChildren().get(1).getKind().equals("LEN")) {
                    rightType = "int";
                } else if (rightOperand.getParent().getChildren().get(1).getKind().equals("ArrayExpression")) {
                    rightType = "int";
                } else if (rightOperand.getParent().getKind().equals("Div") ||
                        rightOperand.getParent().getKind().equals("Plus") ||
                        rightOperand.getParent().getKind().equals("Mult") ||
                        rightOperand.getParent().getKind().equals("Less") ||
                        rightOperand.getParent().getKind().equals("Minus")) {
                    rightType = "int";
                } else if (rightOperand.getParent().equals("Dot")) {
                    rightType = getTypeExpression(rightOperand.getParent().getParent().getParent());
                } else if (rightOperand.getParent().getParent().getKind().equals("Div") ||
                        rightOperand.getParent().getParent().getKind().equals("Plus") ||
                        rightOperand.getParent().getParent().getKind().equals("Mult") ||
                        rightOperand.getParent().getParent().getKind().equals("Less") ||
                        rightOperand.getParent().getParent().getKind().equals("Minus")) {
                    rightType = "int";
                }
            } else if (rightOperand.getKind().equals("Plus") ||
                    rightOperand.getKind().equals("Minus")||
                    rightOperand.getKind().equals("Mult") ||
                    rightOperand.getKind().equals("Div")  ||
                    rightOperand.getKind().equals("Less")) {
                rightType = "int";
            }

            if (rightType.equals("int[]") || leftType.equals("int[]")) {
                reports.add(new Report(ReportType.ERROR,
                        Stage.SEMANTIC,
                        -1,
                        "Invalid Operation: Operands cannot be arrays"));
                return "ERROR";
            } else if (!rightType.equals("int") || !leftType.equals("int")) {
                reports.add(new Report(ReportType.ERROR,
                                       Stage.SEMANTIC,
                                  -1,
                              "Invalid Operation: Operands have different types"));
                return "ERROR";
            }
            return nodeName.equals("Less")?"boolean":"int";
        } else if (nodeName.equals("Array")) {
            if (node.getChildren().get(0).getNumChildren() > 1) {
                return "int";
            } else if (node.getChildren().get(0).getChildren().get(0).getNumChildren() > 1) {
                if (node.getChildren().get(0).getChildren().get(0).getChildren().get(1).getAttributes().size() > 0) {
                    Type auxT = this.mySymbolTable.getReturnType(node.getChildren().get(0).getChildren().get(0).getChildren().get(1).get("var_name"));
                    return auxT != null ? auxT.getName() : "null";
                }
            }
            return getType(node.getChildren().get(0).getChildren().get(0));
        } else if (nodeName.equals("And")) {
            List<JmmNode> operands = node.getChildren().get(0).getChildren();
            JmmNode leftOperand = operands.get(0);
            JmmNode rightOperand = operands.get(1);
            if (rightOperand.getKind().equals("LogicalNegation"))
                rightOperand = rightOperand.getChildren().get(0);

            String leftType = leftOperand.getKind();
            String rightType = rightOperand.getKind();

            while(!rightType.equals("Expression") && !rightType.equals("TerminalExpression")) {
                rightOperand = rightOperand.getChildren().get(0);
                rightType = rightOperand.getKind();
            }

            while(!leftType.equals("Expression") && !leftType.equals("TerminalExpression")) {
                leftOperand = leftOperand.getChildren().get(0);
                leftType = leftOperand.getKind();
            }

            leftType = leftOperand.getKind().equals("Expression")?getTypeExpression(leftOperand):getType(leftOperand);
            rightType = rightOperand.getKind().equals("Expression")?getTypeExpression(rightOperand):getType(rightOperand);

            if (leftType.equals("this"))
                leftType = this.mySymbolTable.getReturnType(leftOperand.getParent().getChildren().get(1).get("var_name")).getName();
            if (rightType.equals("this"))
                rightType = this.mySymbolTable.getReturnType(rightOperand.getParent().getChildren().get(1).get("var_name")).getName();
            if ((!leftType.equals("boolean")) || (!rightType.equals("boolean"))) {
                reports.add(new Report(ReportType.ERROR,
                        Stage.SEMANTIC,
                        -1,
                        "Invalid Operation (&&): Operands are not booleans"));
                return "ERROR";
            }
            return "boolean";
        } else if (nodeName.equals("LogicalNegation")) {
            JmmNode parentNode = node.getChildren().get(0).getChildren().get(0);
            if (getType(parentNode.getChildren().get(0)).equals("this")) {
                return this.mySymbolTable.getReturnType(parentNode.getChildren().get(1).get("var_name")).getName();
            } else {
                if (!getType(parentNode).equals("boolean")) {
                    reports.add(new Report(ReportType.ERROR,
                            Stage.SEMANTIC,
                            -1,
                            "Invalid Operation (!): Operand is not boolean"));
                    return "ERROR";
                }
            }
            return "boolean";
        } else if (nodeName.equals("TerminalExpression")) {
            return getType(node.getChildren().get(0));
        } else if (nodeName.equals("Dot")) {
            String classType = getType(node.getChildren().get(0).getChildren().get(0));
            if (node.getChildren().get(0).getChildren().get(1).getKind().equals("LEN")) {
                if (!classType.equals("int[]"))
                    reports.add(new Report(ReportType.ERROR,
                            Stage.SEMANTIC,
                            -1,
                            "Use of length over invalid type"));
                return "int";
            } else {
                String methodName = node.getChildren().get(0).getChildren().get(1).get("var_name");
                String type = "";
                if (classType.equals("this") || classType.equals(this.mySymbolTable.getClassName())) {
                    if (this.mySymbolTable.getMethods().contains(methodName)) {
                        if (this.mySymbolTable.getReturnType(methodName).getName().equals("int") && this.mySymbolTable.getReturnType(methodName).isArray())
                            type = "int[]";
                        else
                            type = this.mySymbolTable.getReturnType(methodName).getName();
                    } else {
                        type = "ASSUME";
                    }
                } else {
                    if (this.mySymbolTable.getImports().contains(classType)) {
                        type = "ASSUME"; //If the class is imported
                    } else if (classType.equals("ASSUME")) {
                        return this.mySymbolTable.getReturnType(methodName).getName();
                    } else {
                        type = "ERROR";
                    }
                }
                return type;
            }
        }
        return "ERROR";
    }

    // Recebe SEMPRE um nó do tipo TerminalExpression
    public String getType(JmmNode node) {
        if (node.getParent().getNumChildren() > 1 && node.getParent().getChildren().get(1).getKind().equals("ArrayExpression"))
            return "int";

        if (node.getNumChildren() > 0) {
            switch (node.getChildren().get(0).getKind()) {
                case "Bool":
                    return "boolean";
                case "Integer":
                    return "int";
                case "This":
                    return "this";
                case "Variable":
                    String varName = node.getChildren().get(0).get("var_name");
                    if (!this.currentMethod.equals("")) {
                        List<Symbol> localVariables = this.mySymbolTable.getLocalVariables(this.currentMethod);
                        for (Symbol l : localVariables) {
                            if (l.getName().equals(varName)) {
                                if (l.getType().getName().equals("int")) {
                                    return l.getType().isArray()?"int[]":"int";
                                }
                                return l.getType().getName();
                            }
                        }
                        List<Symbol> param = this.mySymbolTable.getParameters(this.currentMethod);
                        for (Symbol p : param) {
                            if (p.getName().equals(varName)) {
                                if (p.getType().getName().equals("int")) {
                                    return p.getType().isArray()?"int[]":"int";
                                }
                                return p.getType().getName();
                            }
                        }
                    }
                    List<Symbol> fields = this.mySymbolTable.getFields();
                    for (Symbol f : fields) {
                        if (f.getName().equals(varName)) {
                            if (f.getType().getName().equals("int")) {
                                return f.getType().isArray()?"int[]":"int";
                            }
                            return f.getType().getName();
                        }
                    }
                    return varName;
                case "NewArray":
                    return "int[]";
                case "NewMethod":
                    if (this.mySymbolTable.getMethods().contains(node.getChildren().get(0).get("method_name")))
                        return this.mySymbolTable.getReturnType(node.getChildren().get(0).get("method_name")).getName();
                    else
                        return "ASSUME";
                case "Expression":
                    return getTypeExpression(node.getChildren().get(0));
                default:
                    return "ERROR";
            }
        }
        return "ERROR";
    }

    // Recebe SEMPRE um nó do tipo TerminalExpression
    public boolean isArray(JmmNode node) {
        switch (node.getChildren().get(0).getKind()) {
            case "Variable":
                String varName = node.getChildren().get(0).get("var_name");
                if (!this.currentMethod.equals("")) {
                    List<Symbol> localVariables = this.mySymbolTable.getLocalVariables(this.currentMethod);
                    for (Symbol l : localVariables) {
                        if (l.getName().equals(varName))
                            return l.getType().isArray();
                    }
                    List<Symbol> param = this.mySymbolTable.getParameters(this.currentMethod);
                    for (Symbol p : param) {
                        if (p.getName().equals(varName))
                            return p.getType().isArray();
                    }
                }
                List<Symbol> fields = this.mySymbolTable.getFields();
                for (Symbol f : fields) {
                    if (f.getName().equals(varName))
                        return f.getType().isArray();
                }
                return false;
            default:
                return false;
        }
    }

    public List<Report> getReports() {
        return reports;
    }
}
