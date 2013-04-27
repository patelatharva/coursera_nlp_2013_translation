/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nlp.translation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author atharva
 */
public class IBM1 {

    private final String enSenFileName;
    private final String frSenFileName;
    private final String learnedTFileName;
    private final String inputEnFileName;
    private final String inputFrFileName;
    Map<String, Map<String, Double>> enForTMap;
    private final String alignmentsOutputFileName;

    public IBM1(String enSenFileName, String frSenFileName, String learnedTFileName, String inputEnFileName, String inputFrFileName, String alignmentsOutputFileName) {
        this.enSenFileName = enSenFileName;
        this.frSenFileName = frSenFileName;
        this.learnedTFileName = learnedTFileName;
        this.inputEnFileName = inputEnFileName;
        this.inputFrFileName = inputFrFileName;
        this.alignmentsOutputFileName = alignmentsOutputFileName;
        this.enForTMap = new HashMap<String, Map<String, Double>>();
    }

    public void learnTParams() {
        initializeTParams();
        updateTParams();
    }

    private void initializeTParams() {
        FileReader frEn = null;
        FileReader frFr = null;
        BufferedReader brEn = null;
        BufferedReader brFr = null;

        try {
            frEn = new FileReader(enSenFileName);
            brEn = new BufferedReader(frEn);
            frFr = new FileReader(frSenFileName);
            brFr = new BufferedReader(frFr);
            String enLine = brEn.readLine();
            enLine = "NULL " + enLine;
            String frLine = brFr.readLine();
            while (enLine != null && frLine != null) {
                String[] enWords = enLine.split(" ");
                String[] frWords = frLine.split(" ");
                for (String enWord : enWords) {
                    for (String frWord : frWords) {
                        if (enForTMap.containsKey(enWord)) {
                            Map<String, Double> frEForTMap = enForTMap.get(enWord);
                            frEForTMap.put(frWord, 1.0);
                        } else {
                            Map<String, Double> frEForTMap = new HashMap<String, Double>();
                            frEForTMap.put(frWord, 1.0);
                            enForTMap.put(enWord, frEForTMap);
                        }
                    }
                }

                enLine = brEn.readLine();
                enLine = "NULL " + enLine;
                frLine = brFr.readLine();
            }
            for (String enWord : enForTMap.keySet()) {
                Map<String, Double> frEnT = enForTMap.get(enWord);
                double nE = frEnT.keySet().size();

                for (String frWord : frEnT.keySet()) {
                    frEnT.put(frWord, 1.0 / nE);
                }
            }
//            System.out.println(enForTMap);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (brEn != null) {
                try {
                    brEn.close();
                } catch (IOException ex) {
                    Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (brFr != null) {
                try {
                    brFr.close();
                } catch (IOException ex) {
                    Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private void updateTParams() {

        int numOfIterations = 5;
        Map<String, Double> c;
        for (int i = 1; i <= 5; i++) {
            c = new HashMap<String, Double>();
            updateCounts(c);
            updateT(c);
        }
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(learnedTFileName);

            bw = new BufferedWriter(fw);
            for (String enWord : enForTMap.keySet()) {
                Map<String, Double> frEnT = enForTMap.get(enWord);
                for (Iterator<String> it = frEnT.keySet().iterator(); it.hasNext();) {
                    String frWord = it.next();
                    StringBuilder sb = new StringBuilder(frWord)
                            .append(" ")
                            .append(enWord)
                            .append(" ")
                            .append(frEnT.get(frWord));
                    bw.write(sb.toString());
                    bw.newLine();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ex) {
                    Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void updateCounts(Map<String, Double> c) {
        FileReader frEn = null;
        FileReader frFr = null;
        BufferedReader brEn = null;
        BufferedReader brFr = null;
        try {
            frFr = new FileReader(frSenFileName);
            frEn = new FileReader(enSenFileName);
            brEn = new BufferedReader(frEn);
            brFr = new BufferedReader(frFr);

            String enLine = brEn.readLine();
            enLine = "NULL " + enLine;
            String frLine = brFr.readLine();
            while (enLine != null && frLine != null) {
                String[] frWords = frLine.split(" ");
                String[] enWords = enLine.split(" ");
                for (String frWord : frWords) {
                    double dDenom = 0.0;

                    for (String enWord : enWords) {
                        dDenom += enForTMap.get(enWord).get(frWord);
                    }
                    for (String enWord : enWords) {
                        String enFr = enWord + "_" + frWord;
                        double dNumerator = enForTMap.get(enWord).get(frWord);
                        double d = dNumerator / dDenom;
                        if (c.containsKey(enFr)) {
                            c.put(enFr, c.get(enFr) + d);
                        } else {
                            c.put(enFr, d);
                        }
                        if (c.containsKey(enWord)) {
                            c.put(enWord, c.get(enWord) + d);
                        } else {
                            c.put(enWord, d);
                        }
//                            enForTMap.get(enWord).put(frWord, c.get(enFr) / c.get(enWord));
                    }
                }

                enLine = brEn.readLine();
                enLine = "NULL " + enLine;
                frLine = brFr.readLine();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (brEn != null) {
                try {
                    brEn.close();
                } catch (IOException ex) {
                    Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (brFr != null) {
                try {
                    brFr.close();
                } catch (IOException ex) {
                    Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    void alignTranslations() {
        readLearnedT();
        FileReader frEn = null;
        FileReader frFr = null;
        BufferedReader brEn = null;
        BufferedReader brFr = null;
        FileWriter fwAl = null;
        BufferedWriter bwAl = null;
        try {
            frEn = new FileReader(inputEnFileName);
            brEn = new BufferedReader(frEn);
            frFr = new FileReader(inputFrFileName);
            brFr = new BufferedReader(frFr);
            fwAl = new FileWriter(alignmentsOutputFileName);
            bwAl = new BufferedWriter(fwAl);
            String enLine = brEn.readLine();
            enLine = "NULL " + enLine;
            String frLine = brFr.readLine();


            int k = 0;
            while (enLine != null && frLine != null) {
                k++;
                String[] enWords = enLine.split(" ");
                String[] frWords = frLine.split(" ");
                for (int i = 0; i < frWords.length; i++) {
                    int maxJ = Integer.MIN_VALUE;
                    double maxT = Double.MIN_VALUE;
                    for (int j = 0; j < enWords.length; j++) {
                        double t = enForTMap.get(enWords[j]).get(frWords[i]);
                        if (t > maxT) {
                            maxT = t;
                            maxJ = j;
                        }

                    }
                    if (maxJ != 0) {
                        StringBuilder sb = new StringBuilder(new Integer(k).toString())
                                .append(" ")
                                .append(maxJ)
                                .append(" ")
                                .append(i + 1);

                        bwAl.write(sb.toString());
                        bwAl.newLine();
                    }
                }


                enLine = brEn.readLine();
                enLine = "NULL " + enLine;
                frLine = brFr.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (brEn != null) {
                try {
                    brEn.close();
                } catch (IOException ex) {
                    Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (brFr != null) {
                try {
                    brFr.close();
                } catch (IOException ex) {
                    Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (bwAl != null) {
                try {
                    bwAl.close();
                } catch (IOException ex) {
                    Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }


    }

    private void readLearnedT() {
        FileReader frT = null;
        BufferedReader brT = null;
        try {
            frT = new FileReader(learnedTFileName);
            brT = new BufferedReader(frT);
            String line = brT.readLine();
            while (line != null) {
                String[] tokens = line.split(" ");
                String frWord = tokens[0];
                String enWord = tokens[1];
                String tString = tokens[2];
                double t = Double.parseDouble(tString);
                if (enForTMap.containsKey(enWord)) {
                    Map<String, Double> frEnT = enForTMap.get(enWord);
                    frEnT.put(frWord, t);
                } else {
                    Map<String, Double> frEnT = new HashMap<String, Double>();
                    frEnT.put(frWord, t);
                    enForTMap.put(enWord, frEnT);
                }
                line = brT.readLine();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (brT != null) {
                try {
                    brT.close();
                } catch (IOException ex) {
                    Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void updateT(Map<String, Double> c) {

        FileReader frEn = null;
        FileReader frFr = null;
        BufferedReader brEn = null;
        BufferedReader brFr = null;
        try {
            frFr = new FileReader(frSenFileName);
            frEn = new FileReader(enSenFileName);
            brEn = new BufferedReader(frEn);
            brFr = new BufferedReader(frFr);

            String enLine = brEn.readLine();
            enLine = "NULL " + enLine;
            String frLine = brFr.readLine();
            while (enLine != null && frLine != null) {
                String[] frWords = frLine.split(" ");
                String[] enWords = enLine.split(" ");
                for (String frWord : frWords) {


                    for (String enWord : enWords) {
                        String enFr = enWord + "_" + frWord;
                        enForTMap.get(enWord).put(frWord, c.get(enFr) / c.get(enWord));
                    }
                }

                enLine = brEn.readLine();
                enLine = "NULL " + enLine;
                frLine = brFr.readLine();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (brEn != null) {
                try {
                    brEn.close();
                } catch (IOException ex) {
                    Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (brFr != null) {
                try {
                    brFr.close();
                } catch (IOException ex) {
                    Logger.getLogger(IBM1.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
