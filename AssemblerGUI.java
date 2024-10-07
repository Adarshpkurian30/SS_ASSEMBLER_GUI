import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class AssemblerGUI extends JFrame {
    private JTextArea outputInput, outputOptab, intermediateFile, symtab, outputPass2;
    private JButton startButton;
    private String inputFileContent = "";
    private String optabFileContent = "";
    private int locctr = 0;
    private int locctr1 = 0;
    private int loca = 0;
    private ArrayList<String> opcodeList = new ArrayList<>();
    private HashMap<String, String> opcodeHex = new HashMap<>();
    private HashMap<String, String> symTab = new HashMap<>();  // Store label and its address
    private ArrayList<String> symList = new ArrayList<>();
    
    public AssemblerGUI() {
        setTitle("Pass 1 & Pass 2 Assembler Simulator");
        setLayout(new BorderLayout());
        
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new GridLayout(3, 2));
        
        JButton inputButton = new JButton("Upload Input File");
        JButton optabButton = new JButton("Upload Optab File");
        
        inputButton.addActionListener(e -> loadFile("input"));
        optabButton.addActionListener(e -> loadFile("optab"));
        
        filePanel.add(inputButton);
        filePanel.add(optabButton);
        
        startButton = new JButton("Start");
        startButton.setEnabled(false);
        startButton.addActionListener(e -> pass1(inputFileContent));
        
        filePanel.add(startButton);
        
        outputInput = new JTextArea(10, 30);
        outputOptab = new JTextArea(10, 30);
        intermediateFile = new JTextArea(10, 30);
        symtab = new JTextArea(10, 30);
        outputPass2 = new JTextArea(10, 30);
        
        outputInput.setBorder(BorderFactory.createTitledBorder("Input File"));
        outputOptab.setBorder(BorderFactory.createTitledBorder("Optab File"));
        intermediateFile.setBorder(BorderFactory.createTitledBorder("Intermediate File"));
        symtab.setBorder(BorderFactory.createTitledBorder("Symtab"));
        outputPass2.setBorder(BorderFactory.createTitledBorder("Pass 2 Output"));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new GridLayout(5, 1));
        textPanel.add(new JScrollPane(outputInput));
        textPanel.add(new JScrollPane(outputOptab));
        textPanel.add(new JScrollPane(intermediateFile));
        textPanel.add(new JScrollPane(symtab));
        textPanel.add(new JScrollPane(outputPass2));
        
        add(filePanel, BorderLayout.NORTH);
        add(textPanel, BorderLayout.CENTER);
        
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void loadFile(String type) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                if (type.equals("input")) {
                    inputFileContent = content;
                    outputInput.setText(inputFileContent);
                } else if (type.equals("optab")) {
                    optabFileContent = content;
                    outputOptab.setText(optabFileContent);
                    processOptabContent(optabFileContent);
                }
                checkFilesAndEnableButton();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkFilesAndEnableButton() {
        if (!inputFileContent.isEmpty() && !optabFileContent.isEmpty()) {
            startButton.setEnabled(true);
        }
    }

    private void pass1(String content) {
        String[] lines = content.split("\n");
        intermediateFile.setText("");
        symtab.setText("Label\tLocctr\tFlag\n\n\n");

        locctr = 0; // Reset LOCCTR for new assembly

        for (String line : lines) {
            String[] words = line.trim().split("\\s+");
            if (words.length <= 3) {
                String label = words.length > 0 ? words[0] : "";
                String opcode = words.length > 1 ? words[1] : "";
                String operand = words.length > 2 ? words[2] : "";

                // Display label, opcode, and operand
                intermediateFile.append(Integer.toHexString(locctr).toUpperCase() + "\t" + label + "\t" + opcode + "\t" + operand + "\n");

                // Update symbol table if there's a label
                if (!label.isEmpty() && !symList.contains(label) && !(label == "**")) {
                    symList.add(label);
                    symTab.put(label, Integer.toHexString(locctr).toUpperCase());  // Store symbol with its LOCCTR
                    symtab.append(label + "\t" + Integer.toHexString(locctr).toUpperCase() + "\t o \n");
                }

                if (opcode.equals("START")) {
                    locctr = Integer.parseInt(operand, 16);
                    loca=locctr;
                    locctr1=locctr;
                } else {
                    if (opcodeList.contains(opcode)) {
                        locctr += 3;  // Instruction size
                        locctr1 =locctr;
                    } else {
                        switch (opcode) {
                            case "WORD": locctr += 3; break;
                            case "RESW": locctr += 3 * Integer.parseInt(operand); break;
                            case "RESB": locctr += Integer.parseInt(operand); break;
                            case "BYTE": locctr += operand.length() - 3; break; // Assuming BYTE uses single byte
                        }
                    }
                }
            }
        }
        locctr1=locctr1-loca;
        loca=locctr-loca;
        pass2();
    }

    private void processOptabContent(String content) {
        String[] lines = content.split("\n");
        opcodeList.clear();
        opcodeHex.clear();

        for (String line : lines) {
            String[] words = line.trim().split("\\s+");
            if (words.length == 2) {
                String opcode = words[0];
                String hexcode = words[1];
                opcodeList.add(opcode);
                opcodeHex.put(opcode, hexcode);
            }
        }
    }

    private void pass2() {
        String[] lines = intermediateFile.getText().split("\n");
        outputPass2.setText("");
        outputPass2.append("H^");
        String[] word = lines[0].trim().split("\\s+");
        if (word[1].length()<6) {
            String str1="";
            for(int i=word[1].length();i<6;i++){
                str1=str1+"_";
            }
            outputPass2.append(word[1]+str1);
        }
        else{
            outputPass2.append(word[1].substring(0,6));
        }
        outputPass2.append("^00"+word[3]+"^");
        String wod=Integer.toHexString(loca).toUpperCase();
        if (wod.length()<6) {
            String str1="";
            for(int i=wod.length();i<6;i++){
                str1="0"+str1;
            }
            outputPass2.append(str1+wod);
        }
        else{
            outputPass2.append(wod.substring(0,6));
        }
        System.out.println(locctr1);
        outputPass2.append("\nT^00"+word[3]+"^"+Integer.toHexString(locctr1).toUpperCase());
        int inc=locctr1/3;

        for (int i=1;i<inc;i++) {
            String[] words = lines[i].trim().split("\\s+");
            if (words.length >= 3) {
                String address = words[0];
                String opcode = words[2];
                String operand = words[3];
                String objectCode = "";

                if (opcodeHex.containsKey(opcode)) {
                    String opcodeHexValue = opcodeHex.get(opcode);
                    String operandAddress;  // Default to 0000 if no valid operand

                    // Check if operand is a symbol (label)
                    if (symTab.containsKey(operand)) {
                        operandAddress =opcodeHexValue+symTab.get(operand);
                    }
                    else{
                        int n=operand.length();
                        if (operand.substring((n-2),n).equals(",X")) {
                            String str=operand.substring(0,n-2);
                            if (symTab.containsKey(str)) {
                                str=symTab.get(str);
                                n=Integer.parseInt(str,16);
                                n+=32768;
                                operandAddress =opcodeHexValue+Integer.toHexString(n).toUpperCase();
                            }
                            else
                                operandAddress =opcodeHexValue+"0000";
                        }
                        else
                            operandAddress =opcodeHexValue+"0000";
                    }
                    outputPass2.append("^"+operandAddress);
                    // Construct object code
                }
                else
                    outputPass2.append("^000000");
            }
        }
        int inx=lines.length;
        for (int in=inc;in<inx;in++) {
            String[] words = lines[in].trim().split("\\s+");
            if (words.length >= 3) {
                String address = words[0];
                String opcode = words[2];
                String operand = words[3];
                String objectCode = "";
                if(opcode.equals("WORD")){
                    outputPass2.append("\nT^00"+address+"^3^");
                    if (operand.length()<6) {
                        String str1="";
                        for(int j=operand.length();j<6;j++){
                            str1=str1+"0";
                        }
                        outputPass2.append(str1+operand);
                    }
                    else{
                        outputPass2.append(operand.substring(0,6));
                    }
                }
                if (opcode.equals("BYTE")){
                    String str1=operand.substring(2, (operand.length()-1));
                    if (operand.charAt(0)==('C')){
                        outputPass2.append("\nT^00"+address+"^"+str1.length()+"^");
                        for(int j=0;j<str1.length();j++){
                            int assci=(int) str1.charAt(j);
                            outputPass2.append(Integer.toHexString(assci).toUpperCase());
                        }
                   }
                    else if(operand.charAt(0)==('X')){
                        int num=(str1.length()+1)/2;
                        if(num>(str1.length())/2){
                            str1="0"+str1;
                        }
                        outputPass2.append("\nT^00"+address+"^"+num+"^"+str1);
                    }
                }
            }
        }
        String[] wordr = lines[0].trim().split("\\s+");
        outputPass2.append("\nE^00"+wordr[3]);
    }

    public static void main (String[] args) {
        SwingUtilities.invokeLater(() -> new AssemblerGUI());
    }
}