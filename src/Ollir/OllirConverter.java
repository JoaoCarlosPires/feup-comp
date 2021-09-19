package Ollir;

import Ollir.Statement.Expression;
import Ollir.Statement.ExpressionType;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;

public class OllirConverter {

    private final SymbolTable symbolTable;
    private String method_name;
    private int counter;

    private int counterForLoops;

    public OllirConverter(SymbolTable symbolTable,int counter) {
        this.symbolTable = symbolTable;
        this.counter = counter;
        this.counterForLoops = counter;
    }

    public String classTemplate(){
        StringBuffer buffer = new StringBuffer();

        buffer.append(this.symbolTable.getClassName() + " {\n");

        List<Symbol> fields = this.symbolTable.getFields();
        fields.forEach(symbol ->{
            buffer.append("\t.field private " + symbol.getName() + typeTemplate(symbol.getType()) + ";\n");
        });

        if(!fields.isEmpty()) buffer.append("\n");

        buffer.append(constructorTemplate(this.symbolTable.getClassName())+"\n");

        return buffer.toString();
    }

    public String constructorTemplate(String className){
        StringBuffer buffer = new StringBuffer();
        buffer.append("\t.construct " + className + "().V {\n");
        buffer.append("\t\tinvokespecial(this,\"<init>\").V;\n");
        buffer.append("\t}\n");
        return buffer.toString();
    }

    public String typeTemplate(Type type){
        switch (type.getName()){
            case "int":
                if(type.isArray())
                    return ".array.i32";
                return ".i32";
            case "boolean":
                return ".bool";
            default:
                return ("." + type.getName());
        }
    }

    public String mainTemplate(){
        return ("\t.method public static main(args.array.String).V {\n");
    }

    public String methodTemplate(){
        StringBuffer buffer = new StringBuffer();

        buffer.append("\t.method public " + this.method_name + "(");

        List<Symbol> parameters = this.symbolTable.getParameters(this.method_name);
        for (int i=0 ; i<parameters.size() ; i++){
            if (i == (parameters.size()-1)){
                buffer.append(parameters.get(i).getName() + typeTemplate(parameters.get(i).getType()));
            }else{
                buffer.append(parameters.get(i).getName() + typeTemplate(parameters.get(i).getType()) + ",");
            }
        }

        buffer.append(")" + typeTemplate(this.symbolTable.getReturnType(this.method_name)) + " {\n");

        return buffer.toString();
    }

    private Symbol containsVar(String var, List<Symbol> symbols){
        for( Symbol symbol : symbols){
            if(symbol.getName().equals(var)){
                return symbol;
            }
        }
        return null;
    }

    private String getDot_attr(List<Expression> children){
        StringBuffer buffer = new StringBuffer();
        children.forEach( child ->{
            buffer.append(',');
            buffer.append(child.getExpression_content());
        });

        return buffer.toString();
    }

    public String buildAuxVar(String aux,String type){
        return (aux+ " :=" + type + " ");
    }

    public String getNextAuxVar(String type){
        String var = "aux" + this.counter + type;
        this.counter++;
        return var;
    }

    public void dotExpTemplate(JmmNode node, Expression expression){
        List<Expression> dotChildren = expression.getChildren();
        JmmNode dotExpression = node.getChildren().get(1);
        JmmNode terminalExpression = node.getChildren().get(0).getChildren().get(0);
        Expression leftChild = dotChildren.get(0);

        StringBuffer buffer = new StringBuffer();

        if(expression.getParent() == null) { // If this is the main expression
            switch (leftChild.getExpressionType()) {
                case TERMINAL: {
                    if (this.symbolTable.getImports().contains(terminalExpression.get("var_name"))) { // import
                        buffer.append("invokestatic(" + leftChild.getExpression_content() + ",\"" + dotExpression.get("var_name") + "\"");
                        if (dotChildren.size() > 1)
                            buffer.append(getDot_attr(dotChildren.subList(1, dotChildren.size())));
                        buffer.append(").V;\n");
                        expression.setType(".V");
                    } else {  // local variable | method parameter
                        buffer.append("invokevirtual(" + terminalsTemplate(terminalExpression) + ",\"" + dotExpression.get("var_name") + "\"");
                        if (dotChildren.size() > 1)
                            buffer.append(getDot_attr(dotChildren.subList(1, dotChildren.size())));
                        if(this.symbolTable.getMethods().contains(dotExpression.get("var_name"))) {
                            buffer.append(")" + typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))) + ";\n");
                            expression.setType(typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))));
                        }else{
                            buffer.append(").V;\n");
                            expression.setType(".V");
                        }
                    }
                    break;
                }
                case NEW:
                case THIS:{
                    buffer.append("invokevirtual("+ leftChild.getExpression_content() + ",\"" + dotExpression.get("var_name")+"\"");
                    if (dotChildren.size() > 1)
                        buffer.append(getDot_attr(dotChildren.subList(1, dotChildren.size())));
                    buffer.append(")"+ typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name")))+";\n");
                    expression.setType(typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))));
                }
            }
        }else{ // If this is not the main expression
            switch (expression.getParent().getExpressionType()){
                case EQUAL:{ // don't need to create aux var
                    switch (leftChild.getExpressionType()) {
                        case TERMINAL: {
                            if(dotExpression.getKind().equals("LEN")){ //.length
                                buffer.append("arraylength(" + leftChild.getExpression_content() + ").i32");
                                expression.setType(".i32");
                            }else {
                                String parentType = expression.getParent().getType(); // type like .i32 .bool

                                if (this.symbolTable.getImports().contains(terminalExpression.get("var_name"))) { // import
                                    buffer.append("invokestatic(" + leftChild.getExpression_content() + ",\"" + dotExpression.get("var_name") + "\"");
                                    if (dotChildren.size() > 1)
                                        buffer.append(getDot_attr(dotChildren.subList(1, dotChildren.size())));
                                    buffer.append(")" + parentType);
                                    expression.setType(parentType);
                                } else {  // local variable | method parameter
                                    buffer.append("invokevirtual(" + terminalsTemplate(terminalExpression) + ",\"" + dotExpression.get("var_name") + "\"");
                                    if (dotChildren.size() > 1)
                                        buffer.append(getDot_attr(dotChildren.subList(1, dotChildren.size())));
                                    if(this.symbolTable.getMethods().contains(dotExpression.get("var_name"))) {
                                        buffer.append(")" + typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))));
                                        expression.setType(typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))));
                                    }else{
                                        buffer.append(")" + parentType);
                                        expression.setType(parentType);
                                    }
                                }
                            }
                            break;
                        }
                        case NEW:
                        case THIS:{
                            buffer.append("invokevirtual("+ leftChild.getExpression_content() + ",\"" + dotExpression.get("var_name")+"\"");
                            if (dotChildren.size() > 1)
                                buffer.append(getDot_attr(dotChildren.subList(1, dotChildren.size())));
                            buffer.append(")"+ typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))));
                            expression.setType(typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))));
                        }
                    }
                    break;
                }
                case CONTENT:{
                    switch (leftChild.getExpressionType()) {
                        case TERMINAL: {
                            if(dotExpression.getKind().equals("LEN")){ //.length
                                buffer.append("arraylength(" + leftChild.getExpression_content() + ").i32;\n");
                                expression.setType(".i32");
                            }else {
                                String parentType = expression.getParent().getType(); // type like .i32 .bool

                                if (this.symbolTable.getImports().contains(terminalExpression.get("var_name"))) { // import
                                    buffer.append("invokestatic(" + leftChild.getExpression_content() + ",\"" + dotExpression.get("var_name") + "\"");
                                    if (dotChildren.size() > 1)
                                        buffer.append(getDot_attr(dotChildren.subList(1, dotChildren.size())));
                                    buffer.append(")" + parentType + ";\n");
                                    expression.setType(parentType);
                                } else {  // local variable | method parameter
                                    buffer.append("invokevirtual(" + terminalsTemplate(terminalExpression) + ",\"" + dotExpression.get("var_name") + "\"");
                                    if (dotChildren.size() > 1)
                                        buffer.append(getDot_attr(dotChildren.subList(1, dotChildren.size())));
                                    if(this.symbolTable.getMethods().contains(dotExpression.get("var_name"))) {
                                        buffer.append(")" + typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))) + ";\n");
                                        expression.setType(typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))));
                                    }else{
                                        buffer.append(").V;\n");
                                        expression.setType(".V");
                                    }
                                }
                            }
                            break;
                        }
                        case NEW:
                        case THIS:{
                            buffer.append("invokevirtual("+ leftChild.getExpression_content() + ",\"" + dotExpression.get("var_name")+"\"");
                            if (dotChildren.size() > 1)
                                buffer.append(getDot_attr(dotChildren.subList(1, dotChildren.size())));
                            buffer.append(")"+ typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))) + ";\n");
                            expression.setType(typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))));
                        }
                    }
                    break;
                }
                default: { // need to create aux var
                    String auxVar = ""; // p.e aux1.i32
                    switch (leftChild.getExpressionType()) {
                        case TERMINAL: {
                            if(dotExpression.getKind().equals("LEN")){ //.length
                                auxVar = getNextAuxVar(".i32");
                                buffer.append(buildAuxVar(auxVar, ".i32") + "arraylength(" + leftChild.getExpression_content() + ").i32;\n");
                                expression.setType(".i32");
                            }else {
                                String parentType = expression.getParent().getType(); // type like .i32 .bool

                                if (this.symbolTable.getImports().contains(terminalExpression.get("var_name"))) { // import
                                    auxVar = getNextAuxVar(parentType);
                                    buffer.append(buildAuxVar(auxVar, parentType) + "invokestatic(" + leftChild.getExpression_content() + ",\"" + dotExpression.get("var_name") + "\"");
                                    if (dotChildren.size() > 1)
                                        buffer.append(getDot_attr(dotChildren.subList(1, dotChildren.size())));
                                    buffer.append(")" + parentType + ";\n");
                                    expression.setType(parentType);
                                } else {  // local variable | method parameter
                                    auxVar = getNextAuxVar(typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))));
                                    buffer.append(buildAuxVar(auxVar, typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name")))) + "invokevirtual(" + terminalsTemplate(terminalExpression) + ",\"" + dotExpression.get("var_name") + "\"");
                                    if (dotChildren.size() > 1)
                                        buffer.append(getDot_attr(dotChildren.subList(1, dotChildren.size())));
                                    if(this.symbolTable.getMethods().contains(dotExpression.get("var_name"))) {
                                        buffer.append(")" + typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))) + ";\n");
                                        expression.setType(typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))));
                                    }else{
                                        buffer.append(")"+parentType+";\n");
                                        expression.setType(parentType);
                                    }
                                }
                            }
                            break;
                        }
                        case NEW:
                        case THIS:{
                            auxVar = getNextAuxVar(typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))));
                            buffer.append(buildAuxVar(auxVar,typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))))+"invokevirtual("+ leftChild.getExpression_content() + ",\"" + dotExpression.get("var_name")+"\"");
                            if (dotChildren.size() > 1)
                                buffer.append(getDot_attr(dotChildren.subList(1, dotChildren.size())));
                            buffer.append(")"+ typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name")))+";\n");
                            expression.setType(typeTemplate(this.symbolTable.getReturnType(dotExpression.get("var_name"))));
                        }
                    }

                    if (expression.getParent().getExpressionType() == ExpressionType.IF || expression.getParent().getExpressionType() == ExpressionType.WHILE)
                        expression.setExpression_content(auxVar + " &&.bool 1.bool");
                    else {
                        expression.setExpression_content(auxVar);
                    }

                    expression.getExpressionSetupFromChildren();
                    expression.addToExpressionSetup(buffer.toString());
                    return;
                }
            }
        }

        expression.setExpression_content(buffer.toString());
        expression.getExpressionSetupFromChildren();
    }

    public String terminalsTemplate(JmmNode node){
        // terminalExpression == (IDENTIFIER || INTEGER)
        Symbol localVar;
        if(node.getAttributes().contains("var_name")){
            if(this.symbolTable.getImports().contains(node.get("var_name"))){ // import
                return node.get("var_name");
            }else if ((localVar = containsVar(node.get("var_name"),this.symbolTable.getLocalVariables(this.method_name))) != null){ // local variable
                return (localVar.getName() + typeTemplate(localVar.getType()));
            }else if ((localVar = containsVar(node.get("var_name"),this.symbolTable.getParameters(this.method_name))) != null){
                int position = this.symbolTable.getParameters(this.method_name).indexOf(localVar) + 1;
                return ("$" + position + "." + localVar.getName() + typeTemplate(localVar.getType()));
            }else if ((localVar = containsVar(node.get("var_name"),this.symbolTable.getFields())) != null){
                return (localVar.getName() + typeTemplate(localVar.getType()));
            }
        }else if (node.getAttributes().contains("integer_value")){
            return (node.get("integer_value") + ".i32");
        }
        return "";
    }

    public void assignTemplate(Expression expression) {
        String equal = " :=" + expression.getType() + " ";

        Expression leftChild = expression.getChildren().get(0);
        Expression rightChild = expression.getChildren().get(1);

        StringBuffer buffer = new StringBuffer();
        buffer.append(leftChild.getExpression_content() + equal + rightChild.getExpression_content() + ";\n");

        expression.setExpression_content(buffer.toString());
        expression.getExpressionSetupFromChildren();
    }

    public void newTemplate(JmmNode node, Expression expression){
        String newName = node.get("method_name");
        String type = "."+newName;
        StringBuffer content = new StringBuffer();
        StringBuffer setup = new StringBuffer();

        switch (expression.getParent().getExpressionType()){
            case EQUAL:{
                String var = expression.getParent().getChildren().get(0).getExpression_content();
                content.append( "new(" + newName + ")" + type + ";\n");
                content.append("invokespecial("+var+",\"<init>\").V");
                break;
            }
            case DOT:{
                String auxVar = getNextAuxVar(type);
                setup.append(buildAuxVar(auxVar,type) + "new(" + newName + ")" + type + ";\n");
                setup.append("invokespecial("+auxVar+",\"<init>\").V;\n");
                content.append(auxVar);
            }
        }

        expression.setExpression_content(content.toString());
        expression.getExpressionSetupFromChildren();
        expression.addToExpressionSetup(setup.toString());
    }

    public void arithmeticAndBooleanExpTemplate(Expression expression, String op, String type) {
        String operation =" " + op + type + " " ;

        Expression leftChild = expression.getChildren().get(0);
        Expression rightChild = expression.getChildren().get(1);

        StringBuffer content = new StringBuffer();
        StringBuffer setup = new StringBuffer();

        switch (expression.getParent().getExpressionType()){
            case IF:
            case WHILE:
            case EQUAL:{
                content.append( leftChild.getExpression_content() + operation + rightChild.getExpression_content());
                break;
            }
            case MINUS:
            case DOT:
            case MULTIPLICATION:
            case DIVISION:
            case LESS:
            case AND:
            case NEWARRAY:
            case RET:
            case NOT:
            case PLUS:{
                String auxVar = getNextAuxVar(type);
                setup.append(buildAuxVar(auxVar,type) + leftChild.getExpression_content() + operation + rightChild.getExpression_content() + ";\n");
                content.append(auxVar);
            }
        }

        expression.setExpression_content(content.toString());
        expression.getExpressionSetupFromChildren();
        expression.addToExpressionSetup(setup.toString());
    }

    public String booleanTemplate(JmmNode node){
        String boolean_value = node.get("boolean_value");

        switch (boolean_value){
            case "true":{
                return "1.bool";
            }
            case "false":{
                return "0.bool";
            }
        }
        return "";
    }

    public void notTemplate(Expression expression){
        String not = "";
        String setup = "";
        switch (expression.getParent().getExpressionType()){
            case IF:
            case WHILE:{
                not = getNextAuxVar(".bool");
                setup = buildAuxVar(not,".bool") + "!.bool "+expression.getChildren().get(0).getExpression_content()+";\n";
                not += " &&.bool 1.bool";
                break;
            }
            case AND:
            case LESS:
            case RET:
            case NOT: {
                not = getNextAuxVar(".bool");
                setup = buildAuxVar(not,".bool") + "!.bool "+expression.getChildren().get(0).getExpression_content()+";\n";
                break;
            }
            default:
                not = "!.bool "+expression.getChildren().get(0).getExpression_content();
        }

        expression.setExpression_content(not);
        expression.getExpressionSetupFromChildren();
        expression.addToExpressionSetup(setup);
    }

    public void returnTemplate(Expression expression){
        String ret = "ret" + expression.getType() + " ";

        Expression retChild = expression.getChildren().get(0);

        expression.setExpression_content(ret + retChild.getExpression_content() + ";\n");
        expression.getExpressionSetupFromChildren();
    }

    public void whileTemplate(Expression expression) {
        String loop = "Loop" + getNextCounterValueForLoops();
        String body = "Body" + getNextCounterValueForLoops();
        String endLoop = "EndLoop" + getNextCounterValueForLoops();

        Expression condition = expression.getChildren().get(0);
        Expression bodyContent = expression.getChildren().get(1);

        StringBuffer content = new StringBuffer();

        content.append(loop + ":\n");
        // LOOP :
        content.append(condition.getExpression_setup());
        content.append("if (" + condition.getExpression_content() + ") goto "+ body + ";\n");
        content.append("goto " + endLoop + ";\n");
        // BODY :
        content.append(body+ ":\n");
        content.append(bodyContent.getExpression_setup());
        content.append(bodyContent.getExpression_content());
        content.append("goto "+ loop + ";\n");
        // ENDLOOP :
        content.append(endLoop+ ":\n");

        // increment loop counter
        this.counterForLoops++;
        expression.setExpression_content(content.toString());
    }

    public void ifTemplate(Expression expression) {
        String body = "Body" + getNextCounterValueForLoops();
        String endIf = "EndIf" + getNextCounterValueForLoops();

        Expression condition = expression.getChildren().get(0);
        Expression bodyIf = null;
        Expression bodyElse = null;

        if (expression.getChildren().size() == 2){
            bodyIf = expression.getChildren().get(1);
        }
        else if(expression.getChildren().size() == 3){
            bodyIf = expression.getChildren().get(1);
            bodyElse = expression.getChildren().get(2);
        }

        StringBuffer content = new StringBuffer();

        //CONDITION
        content.append(condition.getExpression_setup());
        content.append("if (" + condition.getExpression_content() + ") goto "+ body + ";\n");
        //BODY ELSE
        if (bodyElse != null){
            content.append(bodyElse.getExpression_setup());
            content.append(bodyElse.getExpression_content());
        }
        content.append("goto " + endIf + ";\n");
        // BODY IF:
        content.append(body+ ":\n");
        if (bodyIf != null){
            content.append(bodyIf.getExpression_setup());
            content.append(bodyIf.getExpression_content());
        }
        // ENDIF :
        content.append(endIf+ ":\n");

        // increment loop counter
        this.counterForLoops++;
        expression.setExpression_content(content.toString());
    }

    public void contentTemplate(Expression expression){
        List<Expression> childs = expression.getChildren();

        StringBuffer content = new StringBuffer();

        childs.forEach( child ->{
            content.append(child.getExpression_content());
        });

        expression.setExpression_content(content.toString());
        expression.getExpressionSetupFromChildren();
    }

    public void setMethod_name(String method_name) {
        this.method_name = method_name;
    }

    public void setCounter(int counter) {
        this.counter = counter;
        this.counterForLoops = counter;
    }

    public void newArrayTemplate(Expression expression){
        String content = "new(array, " + expression.getChildren().get(0).getExpression_content() + ").array.i32";
        expression.setExpression_content(content);
        expression.getExpressionSetupFromChildren();
    }

    public void arrayTemplate(Expression arrayExpression) {
        Expression leftChild = arrayExpression.getChildren().get(0);
        Expression rightChild = arrayExpression.getChildren().get(1);

        String arrayName = leftChild.getExpression_content().substring(0,leftChild.getExpression_content().indexOf(".array"));

        StringBuffer content = new StringBuffer();
        StringBuffer setup = new StringBuffer();

        if(arrayExpression.getParent().getExpressionType() == ExpressionType.EQUAL){
            content.append( arrayName + "[" + rightChild.getExpression_content() + "].i32");
        }else {
            String auxVar = getNextAuxVar(".i32");
            content.append(auxVar);
            setup.append(buildAuxVar(auxVar, ".i32") + arrayName + "[" + rightChild.getExpression_content() + "].i32;\n");
        }

        arrayExpression.setType(".i32");
        arrayExpression.setExpression_content(content.toString());
        arrayExpression.getExpressionSetupFromChildren();
        arrayExpression.addToExpressionSetup(setup.toString());
    }

    public String getNextCounterValueForLoops() {
        if(this.counterForLoops == 1){
            return "";
        }
        return String.valueOf(this.counterForLoops);
    }
}
