package com.example.jancsi_pc.playingwithsensors;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;


class FeatureExtractor {
    private static int WINSIZE;
    //private static final int WINSIZE = 512;
    //private static final int WINSIZE = 256;
    //private static final int WINSIZE = 128;
    //private static final int WINSIZE = 1024;
    //private static final int WINSIZE = 2048;
    //private static final int WINSIZE = 512;
    //private static final int WINSIZE = 32;
    private static final String DIRECTORY = "D:\\GaitBiometricsData\\zju-gaitacc2\\session_2\\subj_";
    //a translateFile szamara
    private static final String OUTFILE = "D:/GaitBiometricsData/gaitacc_cycles_session2b.arff";
    private static final String DIRECTORY2 = "D:/GaitBiometricsData/zju-gaitacc/zju-gaitacc/session_2/subj_";
    private static /*final*/ ArrayList<String> file;

    public FeatureExtractor(){
        //translation zju-gaitacc session0/1/2
        /*String input,output;
        for(int i=1;i<154;++i){
            input=DIRECTORY2+(i<10?"00"+i:i<100?"0"+i:i);
            output=DIRECTORY+(i<10?"00"+i:i<100?"0"+i:i);
            for(int j=1;j<7;++j){
                File out=new File(output+"/rec_"+j);
                if(!out.exists()){
                    out.mkdirs();
                }
                for(int k=1;k<6;++k){
                    //System.out.println(input+"/rec_"+j+"/"+k+".txt"+"\t"+output+"/rec_"+j+"/"+k+".log");
                    translateFile(input+"/rec_"+j+"/"+k+".txt" , output+"\\rec_"+j+"\\"+k+".log");
                }
            }
        }*/
        //for zju-gaitacc
        //creating the list file names
        file=new ArrayList<String>();
        for(int i=1;i<154;++i){//user: 22/153/153 +1
            for(int j=1;j<7;++j){//rec session
                for(int k=3;k<4;++k){//position  3 = desired
                    file.add((i<10?"00"+i:i<100?"0"+i:i)+"/rec_"+j+"/"+k+".log");
                }
            }
        }


        PrintStream writer = null;
        try {
            writer = new PrintStream(OUTFILE);
        } catch (FileNotFoundException ex) {
            System.out.println("File open error");
            System.exit(1);
        }
        writer.print("@relation accelerometer\n\n");
        writer.println("@attribute minimum_for_axis_X numeric");
        writer.println("@attribute minimum_for_axis_Y numeric");
        writer.println("@attribute minimum_for_axis_Z numeric");
        writer.println("@attribute minimum_for_magnitude numeric");
        writer.println("@attribute average_acceleration_for_axis_X numeric");
        writer.println("@attribute average_acceleration_for_axis_Y numeric");
        writer.println("@attribute average_acceleration_for_axis_Z numeric");
        writer.println("@attribute average_acceleration_for_magnitude numeric");
        writer.println("@attribute standard_deviation_for_axis_X numeric");
        writer.println("@attribute standard_deviation_for_axis_Y numeric");
        writer.println("@attribute standard_deviation_for_axis_Z numeric");
        writer.println("@attribute standard_deviation_for_magnitude numeric");
        writer.println("@attribute average_absolute_difference_for_axis_X numeric");
        writer.println("@attribute average_absolute_difference_for_axis_Y numeric");
        writer.println("@attribute average_absolute_difference_for_axis_Z numeric");
        writer.println("@attribute average_absolute_difference_for_magnitude numeric");
        writer.println("@attribute zero_crossing_rate_for_axis_X numeric");
        writer.println("@attribute zero_crossing_rate_for_axis_Y numeric");
        writer.println("@attribute zero_crossing_rate_for_axis_Z numeric");

        writer.println("@attribute bin0_X numeric");
        writer.println("@attribute bin1_X numeric");
        writer.println("@attribute bin2_X numeric");
        writer.println("@attribute bin3_X numeric");
        writer.println("@attribute bin4_X numeric");
        writer.println("@attribute bin5_X numeric");
        writer.println("@attribute bin6_X numeric");
        writer.println("@attribute bin7_X numeric");
        writer.println("@attribute bin8_X numeric");
        writer.println("@attribute bin9_X numeric");

        writer.println("@attribute bin0_Y numeric");
        writer.println("@attribute bin1_Y numeric");
        writer.println("@attribute bin2_Y numeric");
        writer.println("@attribute bin3_Y numeric");
        writer.println("@attribute bin4_Y numeric");
        writer.println("@attribute bin5_Y numeric");
        writer.println("@attribute bin6_Y numeric");
        writer.println("@attribute bin7_Y numeric");
        writer.println("@attribute bin8_Y numeric");
        writer.println("@attribute bin9_Y numeric");

        writer.println("@attribute bin0_Z numeric");
        writer.println("@attribute bin1_Z numeric");
        writer.println("@attribute bin2_Z numeric");
        writer.println("@attribute bin3_Z numeric");
        writer.println("@attribute bin4_Z numeric");
        writer.println("@attribute bin5_Z numeric");
        writer.println("@attribute bin6_Z numeric");
        writer.println("@attribute bin7_Z numeric");
        writer.println("@attribute bin8_Z numeric");
        writer.println("@attribute bin9_Z numeric");

        writer.println("@attribute bin0_magnitude numeric");
        writer.println("@attribute bin1_magnitude numeric");
        writer.println("@attribute bin2_magnitude numeric");
        writer.println("@attribute bin3_magnitude numeric");
        writer.println("@attribute bin4_magnitude numeric");
        writer.println("@attribute bin5_magnitude numeric");
        writer.println("@attribute bin6_magnitude numeric");
        writer.println("@attribute bin7_magnitude numeric");
        writer.println("@attribute bin8_magnitude numeric");
        writer.println("@attribute bin9_magnitude numeric");

        //writer.print("@attribute userid {u001,u002,u003,u004,u005,u006,u007,u008,u009,u010,u011,u012,u013,u014,u015,u016,u017,u018,u019,u020,u021,u022,u023,u024,u025,u026,u027,u028,u029,u030,u031,u032,u033,u034,u035,u036,u037,u038,u039,u040,u041,u042,u043,u044,u045,u046,u047,u048,u049,u050} \n\n");
        writer.print("@attribute userid {u001,u002,u003,u004,u005,u006,u007,u008,u009,u010,u011,u012,u013,u014,u015,u016,u017,u018,u019,u020,u021,u022,u023,u024,u025,u026,u027,u028,u029,u030,u031,u032,u033,u034,u035,u036,u037,u038,u039,u040,u041,u042,u043,u044,u045,u046,u047,u048,u049,u050,u051,u052,u053,u054,u055,u056,u057,u058,u059,u060,u061,u062,u063,u064,u065,u066,u067,u068,u069,u070,u071,u072,u073,u074,u075,u076,u077,u078,u079,u080,u081,u082,u083,u084,u085,u086,u087,u088,u089,u090,u091,u092,u093,u094,u095,u096,u097,u098,u099,u100,u101,u102,u103,u104,u105,u106,u107,u108,u109,u110,u111,u112,u113,u114,u115,u116,u117,u118,u119,u120,u121,u122,u123,u124,u125,u126,u127,u128,u129,u130,u131,u132,u133,u134,u135,u136,u137,u138,u139,u140,u141,u142,u143,u144,u145,u146,u147,u148,u149,u150,u151,u152,u153} \n\n");

        writer.println("@data");


        Scanner scanner = null;
        double[] cordX={};
        double[] cordZ={};
        double[] cordY={};
        double[] Amag={};
        int counter=0;
        int zero[]={0,0,0};
        double meanX,meanY,meanZ,meanA,minX,minY,minZ,minA;
        final int bins=10;
        int myitems[]={0};  //stores the positons where the new steps start in the cycles.txt
        int cyclometer;  //indicates the number of the curent step
        int position=0;  //indicates the position in the cordX,cordY,... arrays
        for(int i=0;i<file.size();++i){
            //search the biggest WINSIZE
            WINSIZE=0;
            counter=0;
            cyclometer=1;
            Scanner cycleScanner = null;
            try {
                cycleScanner = new Scanner( new File (DIRECTORY2 + file.get(i).substring(0, file.get(i).length()-6) + "/cycles.txt"));
            } catch (FileNotFoundException ex) {
                Log.e("FeatExtrFileNotFoundEx", ex.getMessage());
            }
            if( cycleScanner.hasNextLine() ){
                String line = cycleScanner.nextLine().trim();
                //Ures sorokat atugorja
                if( line.isEmpty() ) {
                    System.out.println("cycles.txt is empty for"+file.get(i));
                    System.exit(1);
                }
                String elems[] = line.split(",");
                myitems=new int[elems.length];
                for(int ix=0;ix<elems.length;++ix){
                    myitems[ix]=Integer.parseInt(elems[ix]);   //storing the indexes
                }
                for(int ix=1;ix<myitems.length;++ix){   //finding the biggest winsize
                    if(WINSIZE<myitems[ix]-myitems[ix-1]){
                        WINSIZE=myitems[ix]-myitems[ix-1];
                    }
                }

            }
            cycleScanner.close();

            //file with accelerometer data
            try {
                //scanner= new Scanner( new File (DIRECTORY + file.get(i).substring(0, 9) + "/" + file.get(i)) );
                //for zju-gaitacc:
                scanner= new Scanner( new File (DIRECTORY + file.get(i)));
            } catch (FileNotFoundException ex) {
                System.out.println("Unable to open file "+file.get(i));
                System.exit(1);
            }

            //reading every line but using just those between the cycle
            if( scanner.hasNextLine()){
                scanner.nextLine();   //header
            }
            while( scanner.hasNextLine() && counter<myitems[0]-1 ){  //skipping measurements before the first new step starts
                scanner.nextLine();
                counter++;
            }
            //using the biggest winsize at declaration
            cordX = new double[WINSIZE+1];
            cordZ = new double[WINSIZE+1];
            cordY = new double[WINSIZE+1];
            Amag = new double[WINSIZE+1];
            position=0;
            while( scanner.hasNextLine() && cyclometer<myitems.length){  //lines starting the first index in cycles.txt
                //Sor kiolvasasa, folos szokozok levagasa a vegekrol
                String line = scanner.nextLine().trim();
                //Ures sorokat atugorja
                if( line.isEmpty() ) continue;
                String items[] = line.split("\t");

                //items[0] = timestamp
                //cordX[counter]=Double.parseDouble(items[1]);
                //cordY[counter]=Double.parseDouble(items[2]);
                //cordZ[counter]=Double.parseDouble(items[3]);
                //for zju-gaitacc:
                //position=counter%WINSIZE;   //corresponding position in cordX/Y... array
                cordX[position]=Double.parseDouble(items[0]);
                cordY[position]=Double.parseDouble(items[1]);
                cordZ[position]=Double.parseDouble(items[2]);
                Amag[position]=Math.sqrt(cordX[position]*cordX[position]+cordY[position]*cordY[position]+cordZ[position]*cordZ[position]);

                //zero crossing
                if(position>0){
                    if(cordX[position-1]*cordX[position]<=0){
                        zero[0]++;
                    }
                    if(cordY[position-1]*cordY[position]<=0){
                        zero[1]++;
                    }
                    if(cordZ[position-1]*cordZ[position]<=0){
                        zero[2]++;
                    }
                }
                position++;
                counter++;

                //extracting features from vectors only when:
                //didn't reached the last step and the counter (index) is corresponding to the current step
                //reached file end
                if((cyclometer<myitems.length && counter>=myitems[cyclometer]) || !scanner.hasNextLine()){
                    //System.out.println(counter+"  "+cyclometer);
                    //-------FEATURES
                    //min
                    if(cordX.length<=0){
                        System.out.println("neg"+cordX.length);
                        break;
                    }
                    minX=min(cordX,position);
                    minY=min(cordY,position);
                    minZ=min(cordZ,position);
                    minA=min(Amag,position);
                    writer.print(minX+",");
                    writer.print(minY+",");
                    writer.print(minZ+",");
                    writer.print(minA+",");

                    meanX=mean(cordX,position);
                    meanY=mean(cordY,position);
                    meanZ=mean(cordZ,position);
                    meanA=mean(Amag,position);
                    if(Double.isNaN(meanX) && cyclometer<myitems.length){
                        System.out.println(file.get(i)+","+cyclometer+","+counter+","+myitems[cyclometer]);
                    }
                    writer.print(meanX+",");
                    writer.print(meanY+",");
                    writer.print(meanZ+",");
                    writer.print(meanA+",");

                    writer.print(stddev(cordX,position,meanX)+",");
                    writer.print(stddev(cordY,position,meanY)+",");
                    writer.print(stddev(cordZ,position,meanZ)+",");
                    writer.print(stddev(Amag,position,meanA)+",");

                    writer.print(absdif(cordX,position,meanX)+",");
                    writer.print(absdif(cordY,position,meanY)+",");
                    writer.print(absdif(cordZ,position,meanZ)+",");
                    writer.print(absdif(Amag,position,meanA)+",");

                    writer.print((double)zero[0]/position+",");
                    writer.print((double)zero[1]/position+",");
                    writer.print((double)zero[2]/position+",");

                    //hist
                    //System.out.println(counter);
                    //System.out.println(minX+" "+max(cordX,counter));
                    //writer.print(histoToString(histogram(cordX,counter,minX,max(cordX,counter),bins),bins)+",");
                    //writer.print(histoToString(histogram(cordY,counter,minY,max(cordY,counter),bins),bins)+",");
                    //writer.print(histoToString(histogram(cordZ,counter,minZ,max(cordZ,counter),bins),bins)+",");
                    //writer.print(histoToString(histogram(Amag,counter,minA,max(Amag,counter),bins),bins)+",");

                    writer.print(histoToString(histogram(cordX,position,-1.5*9.8,1.5*9.8,bins),bins)+",");
                    writer.print(histoToString(histogram(cordY,position,-1.5*9.8,1.5*9.8,bins),bins)+",");
                    writer.print(histoToString(histogram(cordZ,position,-1.5*9.8,1.5*9.8,bins),bins)+",");
                    writer.print(histoToString(histogram(Amag,position,-1.5*9.8,1.5*9.8,bins),bins)+",");

                    writer.print("u"+file.get(i).substring(0, 3)+"\n");

                    zero[0]=zero[1]=zero[2]=0;
                    cordX = new double[WINSIZE+1];
                    cordZ = new double[WINSIZE+1];
                    cordY = new double[WINSIZE+1];
                    Amag = new double[WINSIZE+1];
                    //counter=0;
                    cyclometer++;
                    position=0;
                }
            }
        }
        scanner.close();
        writer.close();
    }

    private static double min(double[] arr, int length) {
        double m=arr[0];
        for(int i=0;i<length;++i){
            if(m>arr[i]){
                m=arr[i];
            }
        }
        return m;
    }

    private static double max(double[] arr, int length) {
        double m=arr[0];
        for(int i=0;i<length;++i){
            if(m<arr[i]){
                m=arr[i];
            }
        }
        return m;
    }

    private static double mean(double[] arr, int length) {
        double total = 0.0;
        for (int i = 0; i < length; i++)
            total += arr[i];
        if(Double.isNaN(total)){
            System.out.println("total "+total);
        }
        if(Double.isNaN(length)||length<=0){
            System.out.println("length "+length);
        }
        return total / length;
    }

    private static double stddev(double[] arr, int length, double mymean) {
        double sum=0.0;
        for (int i = 0; i < length; i++){
            sum += Math.pow(arr[i]-mymean,2);
        }
        return Math.sqrt(sum/length);
    }

    private static double absdif(double[] arr, int length, double mymean) {
        double sum=0.0;
        for (int i = 0; i < length; i++){
            sum += Math.abs(arr[i]-mymean);
        }
        return sum/length;
    }

    private static double[] histogram(double[] arr, int length, double mymin, double mymax, int noOfBins) {
        double histo[]=new double[noOfBins];
        //double binSize=Math.floor((Math.abs(mymin)+Math.abs(mymax))/noOfBins);
        double binSize=(mymax-mymin)/noOfBins;
        int index;
        for(int i=0;i<length;++i){

            index=(int)Math.floor(Math.abs(arr[i]-mymin)/binSize);    //corresponding bin

            if(index>=noOfBins){
                histo[noOfBins-1]++;
            }
            else{
                if(index<0){
                    histo[0]++;
                }
                else{
                    histo[index]++;
                }
            }
        }
        //percentage
        for(int i=0;i<noOfBins;++i){
            histo[i]/=length;
        }
        return histo;
    }

    private static String histoToString(double[] histo, int length){
        String str="";
        for(int i=0;i<length-1;++i){
            str+=histo[i]+",";
        }
        str+=histo[length-1];
        return str;
    }

    //not used now
    private static void translateFile(String fileName,String outFile){
        Scanner scanner = null;

        PrintStream writer = null;
        try {
            writer = new PrintStream(outFile);
        } catch (FileNotFoundException ex) {
            System.out.println("File opening error "+outFile);
            System.exit(1);
        }

        ArrayList<Double> cordX = new ArrayList<>();
        ArrayList<Double> cordY = new ArrayList<>();
        ArrayList<Double> cordZ = new ArrayList<>();

        try {
            scanner= new Scanner(new File(fileName));
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file "+fileName);
            System.exit(1);
        }
        //X
        if( scanner.hasNextLine() ){
            //Sor kiolvasasa, folos szokozok levagasa a vegekrol
            String line = scanner.nextLine().trim();
            //Ures sorokat atugorja
            if( line.isEmpty() ) {
                System.out.println("Ures sor!!");
                return;
            }
            String items[] = line.split(",");
            for(String s : items){
                cordX.add(Double.parseDouble(s)*9.8);
            }
        }
        else{
            System.out.println("Nincs eleg sor!!");
            return;
        }
        //Y
        if( scanner.hasNextLine() ){
            //Sor kiolvasasa, folos szokozok levagasa a vegekrol
            String line = scanner.nextLine().trim();
            //Ures sorokat atugorja
            if( line.isEmpty() ) {
                System.out.println("Ures sor!!");
                return;
            }
            String items[] = line.split(",");
            for(String s : items){
                cordY.add(Double.parseDouble(s)*9.8);
            }
        }
        else{
            System.out.println("Nincs eleg sor!!");
            return;
        }
        //Z
        if( scanner.hasNextLine() ){
            //Sor kiolvasasa, folos szokozok levagasa a vegekrol
            String line = scanner.nextLine().trim();
            //Ures sorokat atugorja
            if( line.isEmpty() ) {
                System.out.println("Ures sor!!");
                return;
            }
            String items[] = line.split(",");
            for(String s : items){
                cordZ.add(Double.parseDouble(s)*9.8);
            }
        }
        else{
            System.out.println("Nincs eleg sor!!");
            return;
        }

        if(cordX.size()!=cordY.size()||cordX.size()!=cordZ.size()){
            System.out.println("A harom sor hossza nem egyenlo!!");
            return;
        }

        for(int i=0;i<cordX.size();++i){
            writer.println(cordX.get(i)+"\t"+cordY.get(i)+"\t"+cordZ.get(i));
        }

    }

}
