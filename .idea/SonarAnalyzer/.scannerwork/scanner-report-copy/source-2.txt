package org.example;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

 class AvgSpd1 {
     private static Logger logger = Logger.getLogger(AvgSpd1.class.getName());
     public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        logger.setLevel(Level.INFO);
        logger.log(Level.INFO, "Enter the distance");
        double distance = input.nextDouble();
        logger.log(Level.INFO, "Enter the speed");
        double speed = input.nextDouble();
        double result = avgSpd(distance, speed);
        logger.log(Level.INFO, "the Avg speed {0} ", result);


    }

     // TESTABLE METHOD - no I/O, just pure logic
     public static double calculateAndLogAvgSpeed(double distance, double speed) {
         double result = avgSpd(distance, speed);
         logger.log(Level.INFO, "Calculated avg speed: {0}", result);
         return result;
     }

    public static double avgSpd(double distance, double time){
     if (time == 0){
         throw new IllegalArgumentException("The time must be greater than zero");
     }

        return distance / time;
    }
}
