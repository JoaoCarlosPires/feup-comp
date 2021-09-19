import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.specs.comp.ollir.*;

import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;

import javax.swing.text.ElementIterator;

//assign, noper, call, binaryoper, return
//goto, branch, putfield, getfield, unaryoper

/**
 * Copyright 2021 SPeCS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

public class BackendStage implements JasminBackend {

    public ClassUnit ollirClass;
    public int check = 0;

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        this.ollirClass = ollirResult.getOllirClass();

        try {
            // Example of what you can do with the OLLIR class
            ollirClass.checkMethodLabels(); // check the use of labels in the OLLIR loaded
            ollirClass.buildCFGs(); // build the CFG of each method
            ollirClass.outputCFGs(); // output to .dot files the CFGs, one per method
            ollirClass.buildVarTables(); // build the table of variables for each method
            ollirClass.show(); // print to console main information about the input OLLIR

            // Convert the OLLIR to a String containing the equivalent Jasmin code
            String jasminCode = ""; // Convert node ...

            // More reports from this stage
            List<Report> reports = new ArrayList<>();

            jasminCode += ".class public " + ollirClass.getClassName();
            jasminCode += "\n";
            if(ollirClass.getSuperClass()!=null){
                jasminCode += "\n.super " + ollirClass.getSuperClass() + "\n";
            }
            else{
                jasminCode += ".super java/lang/Object" + "\n";
            }

            //Declarations
            for (int i = 0; i < ollirClass.getNumFields(); i++) {

                jasminCode += "\n";
                jasminCode += ".field ";

                Field f = ollirClass.getFields().get(i);
                ElementType t = f.getFieldType().getTypeOfElement();

                jasminCode += f.getFieldAccessModifier().toString().toLowerCase();

                if (f.isStaticField())
                    jasminCode += " static";

                if (f.isFinalField())
                    jasminCode += " final";

                jasminCode += " " + f.getFieldName();


                jasminCode += " " + printtype(t);

                if (f.isInitialized()) {
                    jasminCode += " = " + f.getInitialValue(); //adds value to variable
                }
            }

            //Methods
            for (int i = 0; i < ollirClass.getNumMethods(); i++) {

                Method m = ollirClass.getMethods().get(i);
                boolean isDefault = m.getMethodAccessModifier().toString().equals("DEFAULT");
                boolean isMain = m.getMethodName().equals("main");
                boolean flag = (m.isConstructMethod() || m.getMethodName().equals("main"));

                jasminCode += "\n";



                if(isDefault){
                    jasminCode += ".method public";
                }

                else
                    jasminCode += ".method " + m.getMethodAccessModifier().toString().toLowerCase();



                if (m.isFinalMethod())
                    jasminCode += " final";
                if (m.isStaticMethod())
                    jasminCode += " static";

                if (m.isConstructMethod())
                    jasminCode += " <init>";




                else {

                    jasminCode += " " + m.getMethodName();
                    //jasminCode += " (";

                }




                if(isMain)
                    jasminCode += "([Ljava/lang/String;)";


                else{
                    jasminCode += "(";
                    for (int j = 0; j < m.getParams().size(); j++) {
                        Element param = m.getParams().get(j);
                        ElementType t = param.getType().getTypeOfElement();


                        jasminCode += printtype(t);

                    }
                    jasminCode += ")";
                }



                jasminCode += printtype(m.getReturnType().getTypeOfElement());
                jasminCode += "\n";
                jasminCode += ".limit stack 99";
                jasminCode += "\n";
                jasminCode += ".limit locals 99";
                jasminCode += "\n";


                for(int k=0;k<m.getInstructions().size();k++){
                    if (m.getLabels().containsValue(m.getInstructions().get(k))){
                        Instruction instruction = m.getInstructions().get(k);
                        String label = "";
                        for (int j = 0; j < m.getLabels(instruction).size();j++){
                            label +=  m.getLabels(instruction).get(j) + ":\n";
                        }
                        jasminCode += label;
                    }

                    jasminCode += instruction_handler(m, m.getInstructions().get(k));
                }


                if(flag){
                    jasminCode += "\n";

                }

                if (m.isConstructMethod())
                    jasminCode += " return";


                jasminCode += "\n";
                jasminCode += ".end method \n";

            }


            return new JasminResult(ollirResult, jasminCode, reports);

        } catch (OllirErrorException e) {
            return new JasminResult(ollirClass.getClassName(), null,
                    Arrays.asList(Report.newError(Stage.GENERATION, -1, -1, "Exception during Jasmin generation", e)));
        }

    }


    public String instruction_handler(Method method, Instruction instruction){
        String temp = "";
        if (instruction.getInstType() == InstructionType.ASSIGN) {
            AssignInstruction assignInstruction = (AssignInstruction) instruction;

            if(assignInstruction.getRhs().getInstType() == InstructionType.NOPER){
                SingleOpInstruction instruc = (SingleOpInstruction) assignInstruction.getRhs();
                Element op = instruc.getSingleOperand();

                if(op.isLiteral()){
                    temp += "\n";
                    LiteralElement literalElement = (LiteralElement) op;
                    if(Integer.parseInt(literalElement.getLiteral())>5){
                        temp += "   bipush ";
                    }else {
                        temp += "   iconst_";
                    }
                    temp += literalElement.getLiteral();

                    temp += "\n";
                    temp += "   istore ";
                    Operand dest = (Operand) assignInstruction.getDest();
                    temp += method.getVarTable().get(dest.getName()).getVirtualReg();
                    temp += "\n";

                    if (check != 0)
                        temp+= "bipush " + method.getVarTable().get(dest.getName()).getVirtualReg() + "\n";
                }

                if (check != 0) {
                    Operand dest = (Operand) assignInstruction.getDest();
                    temp += "astore " + method.getVarTable().get(dest.getName()).getVirtualReg() + "\n";
                    check = 0;
                }

                if(op.isLiteral())
                    check = 1;

            }


            else if(assignInstruction.getRhs().getInstType() == InstructionType.CALL){
                temp += callfunc(method, assignInstruction.getRhs(), temp);
                if (callfunc(method, assignInstruction.getRhs(), temp).contains("newarray")) {
                    Operand dest = (Operand) assignInstruction.getDest();
                    temp+="astore " + method.getVarTable().get(dest.getName()).getVirtualReg() + "\n";
                }




            }

            else if(assignInstruction.getRhs().getInstType() == InstructionType.BINARYOPER){

                temp += binaryfunc(method, ((AssignInstruction) instruction).getRhs(), temp);
                temp += "\n";
                temp += "  istore ";
                Operand dest = (Operand) assignInstruction.getDest();
                temp += method.getVarTable().get(dest.getName()).getVirtualReg();
                temp += "\n";
            }



        }



        else if (instruction.getInstType() == InstructionType.CALL) {
            temp += callfunc(method, instruction, temp);

        }



        else if (instruction.getInstType() == InstructionType.RETURN) {
            ReturnInstruction returnInstruction = (ReturnInstruction) instruction;
            Operand op;
            temp+="\n";

            if(method.getReturnType().getTypeOfElement()==ElementType.BOOLEAN){
                if (!returnInstruction.getOperand().isLiteral()){
                    op = (Operand) returnInstruction.getOperand();
                    temp+="\n";


                    if(op.getName().equals("false")){
                        temp += "  iconst_0";
                    }

                    else if(op.getName().equals("true")){
                        temp += "iconst_1";
                    }

                    else{
                        temp += "  iload ";
                        temp += method.getVarTable().get(op.getName()).getVirtualReg();
                    }

                    temp += "\n";
                    temp += "  ireturn";
                }
            }
            else if (method.getReturnType().getTypeOfElement()==ElementType.INT32){

                if (!returnInstruction.getOperand().isLiteral()){
                    op = (Operand) returnInstruction.getOperand();

                    if (method.getVarTable().get(op.getName()) != null) {
                        temp += "\n";
                        temp += "  iload ";

                        temp += method.getVarTable().get(op.getName()).getVirtualReg();

                        temp += "\n";
                        temp += "  ireturn";

                    }
                }

            }
            else if (method.getReturnType().getTypeOfElement()==ElementType.VOID){
                temp += "\n";
                temp += "  return";
            }
            else if (method.getReturnType().getTypeOfElement()==ElementType.ARRAYREF){
                op = (Operand) returnInstruction.getOperand();
                if (method.getVarTable().get(op.getName()) != null){
                    temp += "\n";
                    temp += "  aload ";

                    temp += method.getVarTable().get(op.getName()).getVirtualReg();
                    temp += "\n";
                    temp += "areturn";
                }

            }

            return temp;


        }



        else if(instruction.getInstType() == InstructionType.BINARYOPER){
            temp += binaryfunc(method, instruction, temp);

        }

        else if(instruction.getInstType() == InstructionType.BRANCH){
            CondBranchInstruction condBranchInstruction = (CondBranchInstruction) instruction;

            Element op1 = condBranchInstruction.getLeftOperand();
            Element op2 = condBranchInstruction.getRightOperand();
            Operation op = condBranchInstruction.getCondOperation();

            boolean doubleCondition = (op2!=null);

            //operand left
            if(op1.isLiteral()){
                LiteralElement literalElement1 = (LiteralElement) op1;
                if (Integer.parseInt(literalElement1.getLiteral())>5){
                    temp+="  bipush " + literalElement1.getLiteral();
                }else{
                    temp+="  iconst_" + literalElement1.getLiteral();
                }
                temp+= "\n";
            }else{
                temp+= "  iload ";
                Operand operandLeft = (Operand) condBranchInstruction.getLeftOperand();
                temp+= method.getVarTable().get(operandLeft.getName()).getVirtualReg();
                temp+= "\n";
            }


            //operand right
            if(doubleCondition){
                if(op2.isLiteral()){
                    LiteralElement literalElement2 = (LiteralElement) op2;
                    if(Integer.parseInt(literalElement2.getLiteral())>5){
                        temp+="  bipush " + literalElement2.getLiteral();
                    }else {
                        temp+="  iconst_" + literalElement2.getLiteral();
                    }

                    temp+= "\n";
                }
                else{
                    temp+= "  iload ";
                    Operand operandRight = (Operand) condBranchInstruction.getRightOperand();
                    temp+= method.getVarTable().get(operandRight.getName()).getVirtualReg();
                    temp+= "\n";
                }

            }

            //operation
            if(op.getOpType()== OperationType.NOT)
                temp+= auxOpType(op,true, doubleCondition);
            else
                temp+= auxOpType(op, false, doubleCondition);

            // name of branch
            temp+=" " + condBranchInstruction.getLabel()+'\n';
            //System.out.println(condBranchInstruction.getLabel());



        }

        else if(instruction.getInstType() == InstructionType.GOTO){
            GotoInstruction gotoInstruction = (GotoInstruction) instruction;
            temp += "goto " + gotoInstruction.getLabel() + "\n";
        }

        

        return temp;
    }


    public String auxOpType(Operation op, boolean negative, boolean doubleCondition) {
        String temp = "if";
        if (doubleCondition) {
            temp += "_icmp";
        }
        if (op.getOpType() == OperationType.EQ) {
            temp += negative ? "eq" : "ne";
        } else if (op.getOpType() == OperationType.NEQ) {
            temp += negative ? "ne" : "eq";
        } else if (op.getOpType() == OperationType.GTH) {
            temp += negative ? "gt" : "le";
        } else if (op.getOpType() == OperationType.GTE) {
            temp += negative ? "ge" : "lt";
        } else if (op.getOpType() == OperationType.LTH) {
            temp += negative ? "lt" : "ge";
        } else if (op.getOpType() == OperationType.LTE) {
            temp += negative ? "le" : "gt";
        } else if(op.getOpType() == OperationType.ANDB){
            temp += negative ? "ne" : "eq";
        }else {
            temp = "";
        }
        return temp;
    }

    //eq, ne, lt, ge, gt, le
    //ifeq, ifne, iflt, ifge, ifgt, ifle
    //if_icmpeq, if_icmpne, if_icmplt, if_icmpge, if_icmpgt, if_icmple


    public String callfunc(Method method, Instruction instruction, String tempstring){
        CallInstruction callInstruction = (CallInstruction) instruction;
        Element el1 = callInstruction.getFirstArg(), el2 = callInstruction.getSecondArg();
        Operand o1,o2;

        String temp = tempstring;

        if(callInstruction.getInvocationType()==CallType.invokespecial){

            temp += "\n";
            temp += "  aload 0";
            temp += "\n";
            temp += "  invokespecial ";
            temp += this.ollirClass.getClassName() + "/";
            if(!el2.isLiteral()){
                String temp2 = "";
                o2 = (Operand) el2;
                temp2 += o2.getName();
                int n = temp2.length()-1;
                temp += temp2.substring(1,n);
            }
            if(el2.isLiteral()){
                String temp2 = "";
                LiteralElement literalElement = (LiteralElement) el2;
                temp2 += literalElement.getLiteral();
                int n = temp2.length()-1;
                temp += temp2.substring(1,n);
            }

            temp += "()";

            temp += printtype(callInstruction.getReturnType().getTypeOfElement());


        }

        else if(callInstruction.getInvocationType()==CallType.invokestatic){

            for(int i=0;i<callInstruction.getNumOperands();i++){

                if(callInstruction.getListOfOperands().size() > 0 && callInstruction.getListOfOperands().size()-1 >= i && !callInstruction.getListOfOperands().get(i).isLiteral()){
                    temp += "\n";
                    temp += "  iload ";
                    Operand operand = (Operand) callInstruction.getListOfOperands().get(i);
                    temp += method.getVarTable().get(operand.getName()).getVirtualReg();

                }

                if(callInstruction.getListOfOperands().size() > 0 && callInstruction.getListOfOperands().size()-1 >= i && callInstruction.getListOfOperands().get(i).isLiteral()){
                    temp += "\n";
                    LiteralElement literalElement = (LiteralElement) callInstruction.getListOfOperands().get(i);

                    if(Integer.parseInt(literalElement.getLiteral())>5){
                        temp+= "  bipush ";
                    }else {
                        temp += "  iconst_";
                    }
                    temp += literalElement.getLiteral();
                }
            }



            o1 = (Operand) el1;
            temp += "\n";
            if (o1.getName().equals("this")){
                temp +=  "  aload_0";
            }

            temp += "\n";
            temp += "  invokestatic ";
            temp += o1.getName();

            temp+="/";

            if(el2.isLiteral()){
                LiteralElement literalElement = (LiteralElement) el2;
                String temp2 = literalElement.getLiteral();
                int n = temp2.length()-1;
                temp += temp2.substring(1, n);
            }
            else if(!el2.isLiteral()){
                Operand op = (Operand) el2;
                String temp2 = op.getName();
                int n = temp2.length()-1;
                temp += temp2.substring(1,n);

            }

            temp += "(";

            for(int i=0;i<callInstruction.getNumOperands();i++){
                if (callInstruction.getListOfOperands().size() > 0 && callInstruction.getListOfOperands().size()-1 >= i)
                    temp += printtype(callInstruction.getListOfOperands().get(i).getType().getTypeOfElement());
            }



            temp += ")";
            temp += printtype(callInstruction.getReturnType().getTypeOfElement());
            temp += "\n";


        }



        else if(callInstruction.getInvocationType()==CallType.invokevirtual){


            temp += "\n";
            temp += "  aload ";
            o1 = (Operand) el1;
            temp += method.getVarTable().get(o1.getName()).getVirtualReg();



            for(int i=0;i<callInstruction.getNumOperands();i++){
                if (callInstruction.getListOfOperands().size()>0 && callInstruction.getListOfOperands().size()-1 >= i) {
                    if(callInstruction.getListOfOperands().get(i).isLiteral()){


                        temp += "\n";
                        LiteralElement literalElement = (LiteralElement) callInstruction.getListOfOperands().get(i);

                        if(Integer.parseInt(literalElement.getLiteral())>5){
                            temp += "  bipush ";
                        }else {
                            temp += "  iconst_";
                        }
                        temp += literalElement.getLiteral();
                    }
                    else{

                        temp += "\n";
                        temp += "  iload ";
                        Operand o = (Operand) callInstruction.getListOfOperands().get(i);
                        temp += method.getVarTable().get(o.getName()).getVirtualReg();

                    }
                }
            }

            temp += "\n";
            temp += "  invokevirtual ";
            temp += this.ollirClass.getClassName();
            temp += "/";



            if(el2.isLiteral()){
                LiteralElement literalElement = (LiteralElement) el2;
                String temp2 = literalElement.getLiteral();
                int n = temp2.length()-1;
                temp += temp2.substring(1,n);
            }
            else{
                o2 = (Operand) el2;
                String temp2 = o2.getName();
                int n = temp2.length()-1;
                temp += temp2.substring(1,n);
            }

            temp += "(";


            for(int i=0;i<callInstruction.getNumOperands();i++){
                if (callInstruction.getListOfOperands().size()>0 && callInstruction.getListOfOperands().size()-1 >= i) {
                    Element e = callInstruction.getListOfOperands().get(i);
                    temp += printtype(e.getType().getTypeOfElement());
                }
            }

            temp += ")" + printtype(callInstruction.getReturnType().getTypeOfElement());
            temp += "\n";

        } else if (callInstruction.getInvocationType()==CallType.NEW) {
            if (callInstruction.getFirstArg().toString().equals("ARRAYREF")) {

                Element el = callInstruction.getListOfOperands().get(0);

                if (el.isLiteral()){
                    LiteralElement e = (LiteralElement) el;

                    if(Integer.parseInt(e.getLiteral())>5){
                        temp += "bipush " + e.getLiteral() + "\n";
                    }
                    else {
                        temp += "iconst_" + e.getLiteral() + "\n";
                    }

                    temp+="newarray int\n";
                }
            }

        } else if (callInstruction.getInvocationType()== CallType.arraylength){
            Operand o = (Operand) el1;
            AssignInstruction assignInstruction = (AssignInstruction) instruction;
            temp += "\n";
            temp +="aload " + method.getVarTable().get(o.getName()).getVirtualReg() + "\n";
            temp += "arraylength \n";
            Operand dest = (Operand) assignInstruction.getDest();
            temp += "istore " + method.getVarTable().get(dest.getName()).getVirtualReg() + "\n";
        }

        return temp;


    }


    public String binaryfunc(Method method, Instruction instruction,String tempstring){

        String temp = tempstring;
        BinaryOpInstruction binaryInstruction = (BinaryOpInstruction) instruction;



        Element el1 = binaryInstruction.getLeftOperand(),el2 = binaryInstruction.getRightOperand();
        Operand o1, o2;


        //Left

        if(!el1.isLiteral()){
            o1 = (Operand) el1;


            if(el1.getType().getTypeOfElement() == ElementType.INT32){
                temp += "\n";
                temp += "  iload ";
                temp += method.getVarTable().get(o1.getName()).getVirtualReg();
            }
        }
        else{
            temp += "\n";
            temp += "  ldc ";
            LiteralElement literalElement1 = (LiteralElement) el1;
            temp += literalElement1.getLiteral();

        }




        //Right
        if(!el2.isLiteral()){
            o2 = (Operand) el2;

            if(el2.getType().getTypeOfElement() == ElementType.INT32){
                temp += "\n";
                temp += "  iload ";
                temp += method.getVarTable().get(o2.getName()).getVirtualReg();
            }
        }


        else{
            temp += "\n";
            temp += "  ldc ";
            LiteralElement literalElement = (LiteralElement) el2;
            temp += literalElement.getLiteral();

        }


        temp += "\n  ";

        if(binaryInstruction.getUnaryOperation().getOpType() == OperationType.ADD){
            temp += "iadd";
        }
        else if(binaryInstruction.getUnaryOperation().getOpType() == OperationType.DIV){
            temp += "idiv";
        }
        else if(binaryInstruction.getUnaryOperation().getOpType() == OperationType.MUL){
            temp += "imul";
        }
        else if(binaryInstruction.getUnaryOperation().getOpType() == OperationType.SUB){
            temp += "isub";
        }

        return temp;


    }


    public String printtype(ElementType elementType) {

        if (elementType == ElementType.INT32) {
            return "I";
        }
        else if (elementType == ElementType.BOOLEAN) {
            return "Z";
        }
        else if (elementType == ElementType.ARRAYREF) {
            return "[I";
        }
        else if (elementType == ElementType.STRING) {
            return "Ljava/lang/String;";
        }
        else if (elementType == ElementType.VOID) {
            return "V";
        }
        else{
            return null;
        }

    }

}
