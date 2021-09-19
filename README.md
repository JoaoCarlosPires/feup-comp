# feup-comp
 
This project was created during the **2nd semester** of the **3rd year** of **Integrated Master in Informatics and Computing Engineering**, in **Compilers** curricular unity (FEUP).

[COMP FEUP-Sigarra](https://sigarra.up.pt/feup/en/UCURR_GERAL.FICHA_UC_VIEW?pv_ocorrencia_id=272729 "Curricular Unity Homepage")

## Project 

Our program is a relatively simple compiler of a Java based language. This compiler performs both the syntatic and semantic analysis as well as the code generation for a provided .jmm file, executing the generated code. This program was developed using JAVACC for the Compilers course of the Integrated Master in Informatics and Computing Engineering at FEUP.

## Dealing with syntactic errors 

The syntactic errors are handled depending on the type of error:

- If it's an error regarding anything other than while conditions, it throws the error and the program finishes immediately.
- Otherwise (i.e., if it's an error in the while condition), the program will "consume" the deffective tokens until a limit of 10 errors. If the limit is reached, the program will then stop.

## Semantic analysis

The semantic analysis uses the symbol table and a recursive iteration starting in the root node to analyse semantic errors that might appear in the code.

That said, the following list includes the verifications made for both types and methods.

Type Verification
	
- Check if the operands of an arithmetic operation are of the same type. (e.g. int+boolean throws an error)
- Check if any operand in an arithmetic operation is an array, which should then throw an error (e.g. array1+array2)
- Check if an array access is really made over an array (e.g. 1[10] isn't allowed)
- Check if the array index is an integer (e.g. a[true] isn't allowed)
- Check if the type of the left side of an assignment is the same as the right one (a_int=b_boolean isn't allowed)
- Check if a boolean operation is made only with booleans (&& and !)
- Check if the conditions in an if/else and while loop are booleans
- Check if the type of a variable exists
            
Method Verification
	
- Check if an used method existis both in the scope of the class being analysed and in the imported / super classes
- Check if the number of parameters while calling a method is the same as the number of parameters at the method declaration
- Check if the parameters type is the same in the method call and declaration
- Check if the return variable or value is of the same type as the one written in the declaration

## Code generation

OLLIR

- To generate the OLLIR code, we implemented a class "AstToOllirVisitor" that visits the nodes, from the father to the child, from the AST generated by the semantic analysis. This class extends "AJmmVisitor" that allows defining visitors, As such, it is possible to define methods that are executed, in the AST visit, when nodes with the type defined in the visitor are found (ex: "addVisit ('Class', este :: classVisitor) ". When a node of type 'Class' is found, the method "classVisitor" is executed with this node).
- Each of the methods created to deal with the visited nodes will call its respective method of the class "OllirConverter" inside it, which will convert the content of the node into OLLIR.
- In the case of more complex expressions, that is, that have several children for the same expression, we create an "Expression" class that stores the OLLIR translation of the child nodes that will later be used in the OLLIR translation of the parent node. 
- In relation to possible errors, one that was identified is the part of the conversion of the fields that are considered in the OLLIR translation as local variables, that is, it is not translated into a "getField" or a "putField".
- There is also the possibility of generating the code incorrectly, however for the test files present they all passed the tests.

Jasmin
- The class BackendStage is responsible for generating the Jasmin code. An object OllirResult is received by the class containing all the parsed Ollir information. 

- The class iterates all the fields, methods and other informations regarding the ollir code provided by the object. The code is then generated by analyzing the content and converting it to a String variable jasminCode containing all the final equivalent jasmin code.

- The main difficulty regarding the jasmin code generation would be the coverage of every particular case in the language, resulting in the verification of many different conditions and making the code more dense and vulnerable.

## Task distribution 

This was the work distribution for the project:

- Checkpoint 1:
  - João Pires:
    - Proceed with error treatment and recovery mechanisms for the while expression.
    - Resolve grammar conflicts.
  - Marcelo Reis:
    - Convert the provided e-BNF grammar into JavaCC grammar format in a .jj file.
    - Convert the .jj file into a .jjt file.
    - Resolve grammar conflicts.
  - Pedro Azevedo:
    - Generate a JSON from the AST.
    - Resolve grammar conflicts.
  - Tomás Gonçalves:
    - Include missing information in nodes (i.e. tree annotation; e.g. include class name in the class Node).
    - Resolve grammar conflicts.
- Checkpoint 2:
  - João Pires:
    - Symbol Table and Semantic Analysis.
  - Marcelo Reis:
    - OLLIR Generation.
  - Pedro Azevedo:
    - OLLIR Generation.
  - Tomás Gonçalves:
    - Jasmin Generation.
- Checkpoint 3:
  - João Pires:
    - Generate JVM code accepted by jasmin for conditional instructions (if and if-else).
    - Generate JVM code accepted by jasmin for loops.
    - Generate JVM code accepted by jasmin to deal with arrays.
  - Marcelo Reis:
    - Generate OLLIR code conditional instructions (if and if-else).
    - Generate OLLIR code for loops.
    - Generate OLLIR code to deal with arrays.
  - Pedro Azevedo:
    - Generate OLLIR code conditional instructions (if and if-else).
    - Generate OLLIR code for loops.
    - Generate OLLIR code to deal with arrays.
    - Complete the compiler and test it using a set of Java-- classes.
  - Tomás Gonçalves:
    - Generate JVM code accepted by jasmin for conditional instructions (if and if-else).
    - Generate JVM code accepted by jasmin for loops.
    - Generate JVM code accepted by jasmin to deal with arrays.

## Pros 

- Most stages of the compiler are running without issues, with our own examples and the ones provided.
- The semantic analysis includes two more checks than the ones asked for in the checkpoint checklist: missing types and return check.

## Cons 

- Our compiler does not support method overload.
- The generation of the Jasmin is far from perfect and has a lot of issues when generating the executable.
- The semantic analysis has some errors when testing with the following two files: QuickSort.jmm and TicTacToe.jmm.

## Team

- João Pires
- Tomás Gonçalves
- Pedro Azevedo
- Marcelo Reis
