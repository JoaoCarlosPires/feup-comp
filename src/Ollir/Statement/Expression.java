package Ollir.Statement;

import java.util.ArrayList;
import java.util.List;

public class Expression implements Statement{

    private Expression parent;

    private ExpressionType expressionType;

    private String type;
    private String expression_content;
    private String expression_setup;

    private List<Expression> children;

    public Expression(Expression parent, ExpressionType expressionType) {
        this.parent = parent;
        this.expressionType = expressionType;

        this.type = "";
        this.expression_content = "";
        this.expression_setup = "";
        this.children = new ArrayList<>();
    }

    public void getExpressionSetupFromChildren(){
        this.children.forEach( child ->{
            this.expression_setup+=child.getExpression_setup();
        });
    }

    public void addChild(Expression test){
        this.children.add(test);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setExpression_content(String expression_content) {
        this.expression_content = expression_content;
    }

    public void addToExpressionSetup(String expression_setup) {
        this.expression_setup += expression_setup;
    }

    public String getFinalContent(){
        return (this.expression_setup + this.expression_content);
    }

    public List<Expression> getChildren() {
        return children;
    }

    public ExpressionType getExpressionType() {
        return expressionType;
    }

    public String getExpression_content() {
        return expression_content;
    }

    public String getExpression_setup() {
        return expression_setup;
    }

    public Expression getParent() {
        return parent;
    }

    public String getType() {
        if(this.type.equals("")){
            if(this.children.isEmpty())
                return ".V";
            return this.children.get(0).getType();
        }
        return type;
    }

}
