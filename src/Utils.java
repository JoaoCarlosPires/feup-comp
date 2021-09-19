import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.io.*;



public class Utils {
	
	public static InputStream toInputStream(String text) {
        try {
            return new ByteArrayInputStream(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static FileInputStream openFile(String filename) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            System.out.println("File " + filename + " not found");
        }

        return stream;
    }

    public static String getFileContent(FileInputStream fis)
    {
        StringBuilder sb = null;
        try(BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {
            sb = new StringBuilder();
            String line;
            while(( line = br.readLine()) != null ) {
                sb.append( line );
                sb.append( '\n' );
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return normalize(sb.toString());
    }

    public static String normalize(String str) {
        String[] lines = str.split("\n");

        StringBuffer buffer = new StringBuffer();
        for(String line : lines)
            buffer.append(line.trim()+ "\n");

        return buffer.toString();
    }

    public void createFile(JasminResult jasmin, String name){
        try {
            FileWriter file = new FileWriter(name + ".j");
            file.write(jasmin.getJasminCode());
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createFile(OllirResult ollirResult, String name){
        try {
            FileWriter file = new FileWriter(name + ".ollir");
            file.write(ollirResult.getOllirCode());
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createFile(MySymbolTable table, String name){
        try {
            FileWriter file = new FileWriter(name + ".txt");
            file.write(table.print());
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createJSON(SimpleNode root, String name){
	    String json = root.toJson();

        try {
            FileWriter file = new FileWriter(name + ".json");
            file.write(json);
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


	
}