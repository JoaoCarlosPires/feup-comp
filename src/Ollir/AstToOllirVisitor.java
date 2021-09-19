package Ollir;

import Ollir.Statement.Expression;
import Ollir.Statement.ExpressionType;
import Ollir.Statement.Statement;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import java.util.ArrayList;
import java.util.List;

public class AstToOllirVisitor extends AJmmVisitor<Statement, String> {
	private SymbolTable symbolTable;
	private List<Report> reports;
	private OllirConverter ollirConverter;

	public AstToOllirVisitor(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
		this.reports = new ArrayList<>();
		this.ollirConverter = new OllirConverter(this.symbolTable,1);

		// visitors
		addVisit("Class", this::classVisitor);
		addVisit("Method", this::methodVisitor);
		addVisit("Integer", this::terminalVisitor);
		addVisit("Variable", this::terminalVisitor);
		addVisit("This", this::thisVisitor);
		addVisit("NewMethod", this::newVisitor);
		addVisit("Dot", this::dotExpression);
		addVisit("Assign", this::assignmentVisitor);
		addVisit("Plus", this::plusVisitor);
		addVisit("Minus", this::minusVisitor);
		addVisit("Div", this::divVisitor);
		addVisit("Mult", this::mulVisitor);
		addVisit("Less", this::lessVisitor);
		addVisit("And", this::andVisitor);
		addVisit("Return", this::returnVisitor);
		addVisit("While", this::whileVisitor);
		addVisit("IfElse", this::ifVisitor);
		addVisit("Bool",this::booleanVisitor);
		addVisit("LogicalNegation",this::notVisitor);
		addVisit("NewArray", this::newArrayVisitor);
		addVisit("Content", this::contentVisitor);
		addVisit("Array", this::arrayVisitor);
		setDefaultVisit(this::defaultVisitor);
	}

	public List<Report> getReports() {
		return reports;
	}

	private String alignCode(String draft){
		StringBuffer alignedCode = new StringBuffer();

		String [] codeLines = draft.split("\n");

		for (String line : codeLines){
			if(line.matches("Loop(.*):") || line.matches("Body(.*):") || line.matches("EndLoop(.*):") || line.matches("EndIf(.*):"))
				alignedCode.append("\t"+line + "\n");
			else
				alignedCode.append("\t\t"+line + "\n");
		}

		return alignedCode.toString();
	}

	private String defaultVisitor(JmmNode node, Statement statement) {
		StringBuffer ollir = new StringBuffer();
		for (JmmNode child : node.getChildren()) {
			ollir.append(visit(child, statement));
		}
		return ollir.toString();
	}

	private String classVisitor(JmmNode node, Statement statement) {
		StringBuffer ollir = new StringBuffer();
		ollir.append(ollirConverter.classTemplate());
		ollir.append(this.defaultVisitor(node, statement));
		ollir.append("}\n");
		return ollir.toString();
	}

	private String methodVisitor(JmmNode node, Statement statement) {
		StringBuffer ollir = new StringBuffer();

		String method_name = node.get("function_name");
		ollirConverter.setMethod_name(method_name);

		ollirConverter.setCounter(1);

		if(method_name.equals("main")) // main method
			ollir.append(ollirConverter.mainTemplate());
		else // normal method
			ollir.append(ollirConverter.methodTemplate());

		ollir.append(alignCode(this.defaultVisitor(node,null)));
		if(symbolTable.getReturnType(method_name).getName().equals("void"))
			ollir.append("\t\tret.V;\n");
		ollir.append("\t}\n\n");
		return ollir.toString();
	}

	private String dotExpression(JmmNode node, Statement statement){
		Expression dotExpression;
		if(statement == null){
			dotExpression = new Expression(null, ExpressionType.DOT);
			this.defaultVisitor(node,dotExpression);
			ollirConverter.dotExpTemplate(node,dotExpression);
			return dotExpression.getFinalContent();
		}else{
			dotExpression = new Expression((Expression) statement,ExpressionType.DOT);
			this.defaultVisitor(node,dotExpression);
			ollirConverter.dotExpTemplate(node,dotExpression);
			((Expression) statement).addChild(dotExpression);
			return "";
		}
	}

	private String terminalVisitor(JmmNode node , Statement statement){
		if(statement == null) return "";
		Expression terminalExpression = new Expression((Expression) statement,ExpressionType.TERMINAL);
		String content = ollirConverter.terminalsTemplate(node);
		if( (((Expression) statement).getExpressionType() == ExpressionType.IF ) || (((Expression) statement).getExpressionType() == ExpressionType.WHILE ))
			terminalExpression.setExpression_content(content + " &&.bool 1.bool");
		else if ((((Expression) statement).getExpressionType() == ExpressionType.ARRAY ) && (node.getKind().equals("Integer"))){
			String auxVar = ollirConverter.getNextAuxVar(".i32");
			terminalExpression.addToExpressionSetup(ollirConverter.buildAuxVar(auxVar,".i32") + content + ";\n");
			terminalExpression.setExpression_content(auxVar);
		}else
			terminalExpression.setExpression_content(content);
		int dotIndex;
		if(content.contains(".array")){
			dotIndex = content.indexOf(".array");
		}else{
			dotIndex = content.indexOf('.');
		}
		if (dotIndex != -1)
			terminalExpression.setType(content.substring(dotIndex));
		((Expression) statement).addChild(terminalExpression);
		return "";
	}

	private String thisVisitor(JmmNode node , Statement statement){
		if(statement == null) return "";
		Expression thisExpression = new Expression((Expression) statement,ExpressionType.THIS);
		thisExpression.setExpression_content("this");
		((Expression) statement).addChild(thisExpression);
		return "";
	}

	private String assignmentVisitor(JmmNode node, Statement statement) {
		Expression assignmentExpression;
		if(statement == null){
			assignmentExpression = new Expression(null,ExpressionType.EQUAL);
			this.defaultVisitor(node,assignmentExpression);
			ollirConverter.assignTemplate(assignmentExpression);
			return assignmentExpression.getFinalContent();
		}else{
			assignmentExpression = new Expression((Expression)statement,ExpressionType.EQUAL);
			this.defaultVisitor(node,assignmentExpression);
			ollirConverter.assignTemplate(assignmentExpression);
			((Expression) statement).addChild(assignmentExpression);
			return "";
		}
	}

	private String newVisitor(JmmNode node, Statement statement){
		if(statement == null) return "";
		Expression newExpression = new Expression((Expression) statement,ExpressionType.NEW);
		ollirConverter.newTemplate(node,newExpression);
		((Expression) statement).addChild(newExpression);
		return "";
	}

	private String plusVisitor(JmmNode node, Statement statement){
		Expression plusExpression;
		if(statement == null){ // Probably this will never gonna happen
			plusExpression = new Expression(null,ExpressionType.PLUS);
			this.defaultVisitor(node,plusExpression);
			ollirConverter.arithmeticAndBooleanExpTemplate(plusExpression,"+",".i32");
			return plusExpression.getFinalContent();
		}else{
			plusExpression = new Expression((Expression)statement,ExpressionType.PLUS);
			this.defaultVisitor(node,plusExpression);
			ollirConverter.arithmeticAndBooleanExpTemplate(plusExpression,"+",".i32");
			((Expression) statement).addChild(plusExpression);
			return "";
		}
	}

	private String minusVisitor(JmmNode node, Statement statement){
		Expression minusExpression;
		if(statement == null){ // Probably this will never gonna happen
			minusExpression = new Expression(null,ExpressionType.MINUS);
			this.defaultVisitor(node,minusExpression);
			ollirConverter.arithmeticAndBooleanExpTemplate(minusExpression,"-",".i32");
			return minusExpression.getFinalContent();
		}else{
			minusExpression = new Expression((Expression)statement,ExpressionType.MINUS);
			this.defaultVisitor(node,minusExpression);
			ollirConverter.arithmeticAndBooleanExpTemplate(minusExpression,"-",".i32");
			((Expression) statement).addChild(minusExpression);
			return "";
		}
	}

	private String divVisitor(JmmNode node, Statement statement){
		Expression divExpression;
		if(statement == null){ // Probably this will never gonna happen
			divExpression = new Expression(null,ExpressionType.DIVISION);
			this.defaultVisitor(node,divExpression);
			ollirConverter.arithmeticAndBooleanExpTemplate(divExpression,"/",".i32");
			return divExpression.getFinalContent();
		}else{
			divExpression = new Expression((Expression)statement,ExpressionType.DIVISION);
			this.defaultVisitor(node,divExpression);
			ollirConverter.arithmeticAndBooleanExpTemplate(divExpression,"/",".i32");
			((Expression) statement).addChild(divExpression);
			return "";
		}
	}

	private String mulVisitor(JmmNode node, Statement statement){
		Expression mulExpression;
		if(statement == null){ // Probably this will never gonna happen
			mulExpression = new Expression(null,ExpressionType.MULTIPLICATION);
			this.defaultVisitor(node,mulExpression);
			ollirConverter.arithmeticAndBooleanExpTemplate(mulExpression,"*",".i32");
			return mulExpression.getFinalContent();
		}else{
			mulExpression = new Expression((Expression)statement,ExpressionType.MULTIPLICATION);
			this.defaultVisitor(node,mulExpression);
			ollirConverter.arithmeticAndBooleanExpTemplate(mulExpression,"*",".i32");
			((Expression) statement).addChild(mulExpression);
			return "";
		}
	}

	private String lessVisitor(JmmNode node, Statement statement){
		Expression lessExpression;
		if(statement == null){ // Probably this will never gonna happen
			lessExpression = new Expression(null,ExpressionType.LESS);
			this.defaultVisitor(node,lessExpression);
			ollirConverter.arithmeticAndBooleanExpTemplate(lessExpression,"<",".i32");
			return lessExpression.getFinalContent();
		}else{
			lessExpression = new Expression((Expression)statement,ExpressionType.LESS);
			this.defaultVisitor(node,lessExpression);
			ollirConverter.arithmeticAndBooleanExpTemplate(lessExpression,"<",".i32");
			((Expression) statement).addChild(lessExpression);
			return "";
		}
	}

	private String andVisitor(JmmNode node, Statement statement){
		Expression andExpression;
		if(statement == null){ // Probably this will never gonna happen
			andExpression = new Expression(null,ExpressionType.AND);
			this.defaultVisitor(node,andExpression);
			ollirConverter.arithmeticAndBooleanExpTemplate(andExpression,"&&",".bool");
			return andExpression.getFinalContent();
		}else{
			andExpression = new Expression((Expression)statement,ExpressionType.AND);
			this.defaultVisitor(node,andExpression);
			ollirConverter.arithmeticAndBooleanExpTemplate(andExpression,"&&",".bool");
			((Expression) statement).addChild(andExpression);
			return "";
		}
	}

	private String booleanVisitor(JmmNode node, Statement statement){
		if(statement == null) return "";
		Expression booleanExpression = new Expression((Expression) statement,ExpressionType.BOOL);
		String content = ollirConverter.booleanTemplate(node);
		if( (((Expression) statement).getExpressionType() == ExpressionType.IF ) || (((Expression) statement).getExpressionType() == ExpressionType.WHILE ))
			booleanExpression.setExpression_content(content + " &&.bool 1.bool");
		else
			booleanExpression.setExpression_content(content);
		int dotIndex = content.lastIndexOf('.');
		if (dotIndex != -1)
			booleanExpression.setType(content.substring(dotIndex));
		((Expression) statement).addChild(booleanExpression);
		return "";
	}

	private String notVisitor(JmmNode node, Statement statement){
		Expression notExpression;
		if(statement == null){ // Probably this will never gonna happen
			notExpression = new Expression(null,ExpressionType.NOT);
			this.defaultVisitor(node,notExpression);
			ollirConverter.notTemplate(notExpression);
			return notExpression.getFinalContent();
		}else{
			notExpression = new Expression((Expression) statement,ExpressionType.NOT);
			this.defaultVisitor(node,notExpression);
			ollirConverter.notTemplate(notExpression);
			((Expression) statement).addChild(notExpression);
			return "";
		}
	}

	private String whileVisitor(JmmNode node, Statement statement) {
		Expression whileExpression;
		if(statement == null){
			whileExpression = new Expression(null, ExpressionType.WHILE);
			this.defaultVisitor(node,whileExpression);
			ollirConverter.whileTemplate(whileExpression);
			return whileExpression.getFinalContent();
		}else{
			whileExpression = new Expression((Expression) statement,ExpressionType.WHILE);
			this.defaultVisitor(node,whileExpression);
			ollirConverter.whileTemplate(whileExpression);
			((Expression) statement).addChild(whileExpression);
			return "";
		}
	}

	private String ifVisitor(JmmNode node, Statement statement) {
		Expression ifExpression;
		if (statement == null){
			ifExpression = new Expression(null,ExpressionType.IF);
			this.defaultVisitor(node,ifExpression);
			ollirConverter.ifTemplate(ifExpression);
			return ifExpression.getFinalContent();

		}else {
			ifExpression = new Expression((Expression) statement,ExpressionType.IF);
			this.defaultVisitor(node,ifExpression);
			ollirConverter.ifTemplate(ifExpression);
			((Expression) statement).addChild(ifExpression);
			return "";
		}
	}

	private String newArrayVisitor(JmmNode node, Statement statement){
		if(statement == null) return "";
		Expression newArrayExpression = new Expression((Expression) statement,ExpressionType.NEWARRAY);
		this.defaultVisitor(node,newArrayExpression);
		ollirConverter.newArrayTemplate(newArrayExpression);
		((Expression) statement).addChild(newArrayExpression);
		return "";
	}

	private String contentVisitor(JmmNode node, Statement statement){
		if(statement == null) return "";
		Expression content = new Expression((Expression) statement,ExpressionType.CONTENT);
		this.defaultVisitor(node,content);
		ollirConverter.contentTemplate(content);
		((Expression) statement).addChild(content);
		return "";
	}

	private String arrayVisitor(JmmNode node, Statement statement){
		if(statement == null) return "";
		Expression arrayExpression = new Expression((Expression) statement,ExpressionType.ARRAY);
		this.defaultVisitor(node,arrayExpression);
		ollirConverter.arrayTemplate(arrayExpression);
		((Expression) statement).addChild(arrayExpression);
		return "";
	}

	private String returnVisitor(JmmNode node, Statement statement){
		Expression returnExpression;
		if(statement == null){
			returnExpression = new Expression(null,ExpressionType.RET);
			this.defaultVisitor(node,returnExpression);
			ollirConverter.returnTemplate(returnExpression);
			return returnExpression.getFinalContent();
		}
		return "";
	}

}
