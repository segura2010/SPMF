/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.pfv.spmf.tools.calculate_measures_sequential_rules;

import java.io.IOException;

/**
 *
 * @author alberto
 */
public class Main {
    
    public static void main(String [] arg) throws IOException{
        
        MeasureCalculator calculator = new MeasureCalculator();
        calculator.calculate("/Users/alberto/Desktop/Universidad/Doctorado/NuevasPropuestas/DBs/FIFA_db.txt", "/Users/alberto/Desktop/Universidad/Doctorado/NuevasPropuestas/DBs/resultados/FIFA/cmdeo_test.txt", "/Users/alberto/Desktop/Universidad/Doctorado/NuevasPropuestas/DBs/resultados/FIFA/cmdeo_measures.txt");
        //calculator.calculate("/Users/alberto/Desktop/Universidad/Doctorado/NuevasPropuestas/DBs/test.txt", "/Users/alberto/Desktop/Universidad/Doctorado/NuevasPropuestas/DBs/resultados/test.txt", "/Users/alberto/Desktop/Universidad/Doctorado/NuevasPropuestas/DBs/resultados/test_measures.txt");
    }
    
}
