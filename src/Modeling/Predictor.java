/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Modeling;

import ML_Formulas.LinearRegFormulas;
import MatrixOps.MatrixValues;
import StockData.*;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Matt Jones
 */
public class Predictor {
    public void predictAllStocks(final ModelTypes MODEL_TYPE, final int DAYS_IN_FUTURE, final Date testDate) throws Exception {

        StockDataHandler sdh = new StockDataHandler();
        List<StockTicker> stockList = sdh.getAllStockTickers(true); //FIX THIS LATER, SET TO FALSE!!!!
        for (StockTicker ticker : stockList) {

            System.out.println("Ticker = " + ticker.getTicker());
            
            //Get Features and pre calculated weights
            double[] features = sdh.getFeatures(ticker.getTicker(), testDate);
            if (features == null) //If features are unavailable for this date then skip it
                continue;
            
            //Extract the weights
            List<Weight> listWeights = sdh.getWeights(ticker.getTicker(), MODEL_TYPE);
            int numWeights = listWeights.size();
            
            //Sanity Check
            if (features.length != numWeights) {
                System.out.println("Features = " + features.length);
                System.out.println("Weights = " + numWeights);
                throw new Exception("Method: predictAllStocks, Desc: Number of features != Number of weights."); 
            }
            
            double[] weights = new double[numWeights];
            double[] avg = new double[numWeights];
            double[] range = new double[numWeights];
            
            for (int i = 0; i < weights.length; i++) {
                weights[i] = listWeights.get(i).getTheta().doubleValue();
                avg[i] = listWeights.get(i).getAverage().doubleValue();
                range[i] = listWeights.get(i).getRange().doubleValue();
            }
            
            //Mean Normalization of the features
            features = MatrixValues.meanNormalization(features, avg, range);
            
            //Hypothesis
            double value = LinearRegFormulas.hypothesis(features, weights);
            BigDecimal bd = new BigDecimal(value);

            Calendar c = Calendar.getInstance();
            c.setTime(testDate);
            c.add(Calendar.DATE, DAYS_IN_FUTURE);
            
            sdh.setStockPredictions(ticker.getTicker(), c.getTime(), MODEL_TYPE, bd);
        }
    }
}
