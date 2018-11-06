package com.example.jancsi_pc.playingwithsensors;

import android.util.Log;
import java.nio.charset.Charset;
import java.util.ArrayList;

//creates a byte[] list containing the features
class FeatureExtractor {
    private static StringBuilder header = new StringBuilder();
    private static StringBuilder dataString = new StringBuilder();
    private byte[] bytes;
    private ArrayList<byte[]> byteList = new ArrayList<>();
    private static int WINSIZE;
    private ArrayList<Accelerometer> dataset;

    public FeatureExtractor(ArrayList<Accelerometer> receivedData){
        dataset=receivedData;

        //adding header
        generateHeader();
        bytes=header.toString().getBytes(Charset.defaultCharset());
        byteList.add(bytes);

        double[] cordX={};
        double[] cordZ={};
        double[] cordY={};
        double[] Amag={};
        int counter=0;
        int zero[]={0,0,0};
        double meanX,meanY,meanZ,meanA,minX,minY,minZ,minA;
        final int bins=10;
        int cyclometer;  //indicates the number of the curent step
        int position=0;  //indicates the position in the cordX,cordY,... arrays
        for(int i=0;i<dataset.size();++i){
            //search the biggest WINSIZE
            WINSIZE=0;
            int counter2=dataset.get(0).getStep(); //first step in this case
            cyclometer=1;
            int index=0;
            while(index<dataset.size()-1){
                while(dataset.get(index).getStep()==cyclometer){
                    counter2++;
                }
                if(counter2>WINSIZE){
                    WINSIZE=counter2;
                }
            }

            //using the biggest WINSIZE at declaration
            cordX = new double[WINSIZE+1];
            cordZ = new double[WINSIZE+1];
            cordY = new double[WINSIZE+1];
            Amag = new double[WINSIZE+1];

            while(cyclometer == dataset.get(i).getStep()){ //while it is in the same step
                cordX[position]=dataset.get(i).getX();
                cordY[position]=dataset.get(i).getY();
                cordZ[position]=dataset.get(i).getZ();
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

                int noOfSteps=dataset.get(dataset.size()-1).getStep()-dataset.get(0).getStep();
                //extracting features from vectors if the step has ended
                if((counter<dataset.size() && cyclometer<noOfSteps && dataset.get(counter).getStep()>=cyclometer)){ //CHECK TODO
                    ///not outOfBounds          not last step              end of step
                    //-------FEATURES
                    //min
                    if(cordX.length<=0){
                        Log.i("TAGneg",""+cordX.length);
                        break;
                    }
                    minX=min(cordX,position);
                    minY=min(cordY,position);
                    minZ=min(cordZ,position);
                    minA=min(Amag,position);
                    dataString.append(minX);dataString.append(",");
                    dataString.append(minY);dataString.append(",");
                    dataString.append(minZ);dataString.append(",");
                    dataString.append(minA);dataString.append(",");

                    meanX=mean(cordX,position);
                    meanY=mean(cordY,position);
                    meanZ=mean(cordZ,position);
                    meanA=mean(Amag,position);
                    if(Double.isNaN(meanX) && cyclometer<noOfSteps){ //??? TODO
                        Log.i("NANerror",cyclometer+","+counter+","+dataset.get(i).getStep());
                    }
                    dataString.append(meanX);dataString.append(",");
                    dataString.append(meanY);dataString.append(",");
                    dataString.append(meanZ);dataString.append(",");
                    dataString.append(meanA);dataString.append(",");

                    dataString.append(stddev(cordX,position,meanX));dataString.append(",");
                    dataString.append(stddev(cordY,position,meanY));dataString.append(",");
                    dataString.append(stddev(cordZ,position,meanZ));dataString.append(",");
                    dataString.append(stddev(Amag,position,meanA));dataString.append(",");

                    dataString.append(absdif(cordX,position,meanX));dataString.append(",");
                    dataString.append(absdif(cordY,position,meanY));dataString.append(",");
                    dataString.append(absdif(cordZ,position,meanZ));dataString.append(",");
                    dataString.append(absdif(Amag,position,meanA));dataString.append(",");

                    dataString.append((double)zero[0]/position);dataString.append(",");
                    dataString.append((double)zero[1]/position);dataString.append(",");
                    dataString.append((double)zero[2]/position);dataString.append(",");

                    //hist
                    //System.out.println(counter);
                    //System.out.println(minX+" "+max(cordX,counter));
                    //dataString.append(histoToString(histogram(cordX,counter,minX,max(cordX,counter),bins),bins)+",");
                    //dataString.append(histoToString(histogram(cordY,counter,minY,max(cordY,counter),bins),bins)+",");
                    //dataString.append(histoToString(histogram(cordZ,counter,minZ,max(cordZ,counter),bins),bins)+",");
                    //dataString.append(histoToString(histogram(Amag,counter,minA,max(Amag,counter),bins),bins)+",");

                    dataString.append(histoToString(histogram(cordX,position,-1.5*9.8,1.5*9.8,bins),bins));dataString.append(",");
                    dataString.append(histoToString(histogram(cordY,position,-1.5*9.8,1.5*9.8,bins),bins));dataString.append(",");
                    dataString.append(histoToString(histogram(cordZ,position,-1.5*9.8,1.5*9.8,bins),bins));dataString.append(",");
                    dataString.append(histoToString(histogram(Amag,position,0,3*9.8,bins),bins));dataString.append(",");

                    //userid
                    dataString.append("u"+"ID"+"\n"); //TODO

                    //add to the list
                    bytes=dataString.toString().getBytes(Charset.defaultCharset());
                    byteList.add(bytes);

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
    }

    private static void generateHeader(){
        header.append("@relation accelerometer\n\n");
        header.append("@attribute minimum_for_axis_X numeric\n");
        header.append("@attribute minimum_for_axis_Y numeric\n");
        header.append("@attribute minimum_for_axis_Z numeric\n");
        header.append("@attribute minimum_for_magnitude numeric\n");
        header.append("@attribute average_acceleration_for_axis_X numeric\n");
        header.append("@attribute average_acceleration_for_axis_Y numeric\n");
        header.append("@attribute average_acceleration_for_axis_Z numeric\n");
        header.append("@attribute average_acceleration_for_magnitude numeric\n");
        header.append("@attribute standard_deviation_for_axis_X numeric\n");
        header.append("@attribute standard_deviation_for_axis_Y numeric\n");
        header.append("@attribute standard_deviation_for_axis_Z numeric\n");
        header.append("@attribute standard_deviation_for_magnitude numeric\n");
        header.append("@attribute average_absolute_difference_for_axis_X numeric\n");
        header.append("@attribute average_absolute_difference_for_axis_Y numeric\n");
        header.append("@attribute average_absolute_difference_for_axis_Z numeric\n");
        header.append("@attribute average_absolute_difference_for_magnitude numeric\n");
        header.append("@attribute zero_crossing_rate_for_axis_X numeric\n");
        header.append("@attribute zero_crossing_rate_for_axis_Y numeric\n");
        header.append("@attribute zero_crossing_rate_for_axis_Z numeric\n");

        header.append("@attribute bin0_X numeric\n");
        header.append("@attribute bin1_X numeric\n");
        header.append("@attribute bin2_X numeric\n");
        header.append("@attribute bin3_X numeric\n");
        header.append("@attribute bin4_X numeric\n");
        header.append("@attribute bin5_X numeric\n");
        header.append("@attribute bin6_X numeric\n");
        header.append("@attribute bin7_X numeric\n");
        header.append("@attribute bin8_X numeric\n");
        header.append("@attribute bin9_X numeric\n");

        header.append("@attribute bin0_Y numeric\n");
        header.append("@attribute bin1_Y numeric\n");
        header.append("@attribute bin2_Y numeric\n");
        header.append("@attribute bin3_Y numeric\n");
        header.append("@attribute bin4_Y numeric\n");
        header.append("@attribute bin5_Y numeric\n");
        header.append("@attribute bin6_Y numeric\n");
        header.append("@attribute bin7_Y numeric\n");
        header.append("@attribute bin8_Y numeric\n");
        header.append("@attribute bin9_Y numeric\n");

        header.append("@attribute bin0_Z numeric\n");
        header.append("@attribute bin1_Z numeric\n");
        header.append("@attribute bin2_Z numeric\n");
        header.append("@attribute bin3_Z numeric\n");
        header.append("@attribute bin4_Z numeric\n");
        header.append("@attribute bin5_Z numeric\n");
        header.append("@attribute bin6_Z numeric\n");
        header.append("@attribute bin7_Z numeric\n");
        header.append("@attribute bin8_Z numeric\n");
        header.append("@attribute bin9_Z numeric\n");

        header.append("@attribute bin0_magnitude numeric\n");
        header.append("@attribute bin1_magnitude numeric\n");
        header.append("@attribute bin2_magnitude numeric\n");
        header.append("@attribute bin3_magnitude numeric\n");
        header.append("@attribute bin4_magnitude numeric\n");
        header.append("@attribute bin5_magnitude numeric\n");
        header.append("@attribute bin6_magnitude numeric\n");
        header.append("@attribute bin7_magnitude numeric\n");
        header.append("@attribute bin8_magnitude numeric\n");
        header.append("@attribute bin9_magnitude numeric\n");

        //TODO
        //header.append("@attribute userid {u001,u002,u003,u004,u005,u006,u007,u008,u009,u010,u011,u012,u013,u014,u015,u016,u017,u018,u019,u020,u021,u022,u023,u024,u025,u026,u027,u028,u029,u030,u031,u032,u033,u034,u035,u036,u037,u038,u039,u040,u041,u042,u043,u044,u045,u046,u047,u048,u049,u050} \n\n");
        header.append("@attribute userid {u001,u002,u003,u004,u005,u006,u007,u008,u009,u010,u011,u012,u013,u014,u015,u016,u017,u018,u019,u020,u021,u022,u023,u024,u025,u026,u027,u028,u029,u030,u031,u032,u033,u034,u035,u036,u037,u038,u039,u040,u041,u042,u043,u044,u045,u046,u047,u048,u049,u050,u051,u052,u053,u054,u055,u056,u057,u058,u059,u060,u061,u062,u063,u064,u065,u066,u067,u068,u069,u070,u071,u072,u073,u074,u075,u076,u077,u078,u079,u080,u081,u082,u083,u084,u085,u086,u087,u088,u089,u090,u091,u092,u093,u094,u095,u096,u097,u098,u099,u100,u101,u102,u103,u104,u105,u106,u107,u108,u109,u110,u111,u112,u113,u114,u115,u116,u117,u118,u119,u120,u121,u122,u123,u124,u125,u126,u127,u128,u129,u130,u131,u132,u133,u134,u135,u136,u137,u138,u139,u140,u141,u142,u143,u144,u145,u146,u147,u148,u149,u150,u151,u152,u153} \n\n");

        header.append("@data");
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
            Log.i("NANerror2","total "+total);
        }
        if(Double.isNaN(length)||length<=0){
            Log.i("NANlength","length "+length);
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

    public ArrayList<byte[]> getByteList() {
        return byteList;
    }

}
