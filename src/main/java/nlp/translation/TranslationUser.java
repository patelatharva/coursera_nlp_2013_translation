/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nlp.translation;

/**
 *
 * @author atharva
 */
public class TranslationUser {
    public static void main(String [] args){
        String dir = "/Users/atharva/Documents/Knowledge/Videos/Natural Language Processing/Assignment/Assignment 3/Translation/h3/";
        IBM1 ibm1 = new IBM1(dir+"corpus.en", dir+"corpus.es",dir+"learnedT.out", dir+ "dev.en", dir+ "dev.es", dir+"alignment_dev.p1.out");
        ibm1.learnTParams();
        ibm1.alignTranslations();
    }
}
