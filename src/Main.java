import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.StringReader;
import java.util.List;

public class Main implements JmmParser {

	public String className;

	public JmmParserResult parse(String jmmCode) {
		
		try {
			Parser parser = new Parser(new StringReader(jmmCode));
    		SimpleNode root = parser.Program(); // returns reference to root node

    		//root.dump(""); // prints the tree on the screen
			Utils s = new Utils();
			s.createJSON(root,className);

    	
    		return new JmmParserResult(root, new ArrayList<Report>());
		} catch(ParseException e) {
			throw new RuntimeException("Error while parsing", e);
		}
	}
	
	public JmmSemanticsResult analyse(JmmParserResult jmmParser) {
		AnalysisStage analysis = new AnalysisStage();
		return analysis.semanticAnalysis(jmmParser);
	}

	public OllirResult toOllir(JmmSemanticsResult semanticsResult){
		OptimizationStage ollir = new OptimizationStage();
		return ollir.toOllir(semanticsResult);
	}

	public JasminResult toJasmin(OllirResult ollirResult){
		BackendStage jasmin = new BackendStage();
		return jasmin.toJasmin(ollirResult);
	}

    public static void main(String[] args) {
        System.out.println("Executing with args: " + Arrays.toString(args));

		Path path = Paths.get(args[0]);

		Main main = new Main();
		main.className = path.getFileName().toString().split("\\." )[0];

        JmmParserResult parserResult = main.parse(new Utils().getFileContent(new Utils().openFile(args[0])));
        JmmSemanticsResult semanticsResult = main.analyse(parserResult);
		Utils s = new Utils();
		s.createFile((MySymbolTable) semanticsResult.getSymbolTable(),main.className);

		// Check Reports
        List<Report> reports = semanticsResult.getReports();
        for (Report r : reports) {
			System.out.println(r.getStage().toString() + " " + r.getType().toString() + " - " + r.getMessage());
		}

        if (reports.size() > 0) {
        	System.exit(-1);
		}

        OllirResult ollirResult = main.toOllir(semanticsResult);
        s.createFile(ollirResult,main.className);

        reports = ollirResult.getReports();

		for (Report r : reports) {
			System.out.println(r.getStage().toString() + " " + r.getType().toString() + " - " + r.getMessage());
		}

        if (reports.size() > 0){
        	System.exit(-1);
		}
        
        // add jasmin here

		JasminResult jasminResult = main.toJasmin(ollirResult);
		s.createFile(jasminResult,main.className);
		reports = jasminResult.getReports();
		for (Report r : reports) {
			System.out.println(r.getStage().toString() + " " + r.getType().toString() + " - " + r.getMessage());
		}

		if (reports.size() > 0){
			System.exit(-1);
		}

		jasminResult.run();

        if (args[0].contains("fail")) {
            throw new RuntimeException("It's supposed to fail");
        }
    }


}