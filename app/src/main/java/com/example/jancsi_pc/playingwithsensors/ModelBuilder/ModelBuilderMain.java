package com.example.jancsi_pc.playingwithsensors.ModelBuilder;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.jancsi_pc.playingwithsensors.Utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import FeatureExtractorLibrary.FeatureExtractor;
import FeatureExtractorLibrary.Settings;
//import sun.security.jca.GetInstance;
//import weka.classifiers.Classifier;
//import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.Debug;
//import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

import java.io.*;
//import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ModelBuilderMain {

    public static void main(String[] args) throws Exception {

        String TAG = "ModelBuilderMain";

        /*Settings.usingFrames(512);
        Settings.setOutputHasHeader(true); // full arff, no header

        FeatureExtractor.extractFeaturesFromCsvFileToCsvFile(RAWDATADummy,FEATURESDummy);
        FeatureExtractor.extractFeaturesFromCsvFileToCsvFile(RAWDATAUser,FEATURESUser);
        */

        /*String RAWDATADummy = "D://GaitBiom//ModelBuilder//rawdata_rRHyStiEKkN4Cq5rVSxlpvrCwA72_20181129_152100.csv";   // DWN-FB
        String RAWDATAUser = "D://GaitBiom//ModelBuilder//rawdata_LnntbFQGpBeHx3RwMu42e2yOks32_20181121_213611.csv";
        String FEATURESDummy = "D://GaitBiom//ModelBuilder//features_Dummy.arff";
        String FEATURESUser = "D://GaitBiom//ModelBuilder//features_User.arff";
        String MODELPATHUser = "D://GaitBiom//ModelBuilder//model_LnntbFQGpBeHx3RwMu42e2yOks32.mdl";
        */

        String RAWDATAUser = "";  // same as dummy but for user
        String FEATURESDummy = "";
        String FEATURESUser = "";
        String MODELPATHUser = "";

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        FirebaseFirestore.getInstance();
        DocumentReference mDocRef = FirebaseFirestore.getInstance()
                .collection("user_records_2/" )
                .document( Util.mAuth.getUid() + "" );
        mDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                }else{

                }
            }
        });




        //getFeatures(RAWDATADummy,FEATURESDummy);
        getFeatures(RAWDATAUser,FEATURESUser);

        //TODO save features to firestore...

        mergeArffFiles(FEATURESDummy, FEATURESUser);

        try{
            CreateAndSaveModel(FEATURESUser, MODELPATHUser);
        }
        catch (Exception e){
            e.printStackTrace();
            Logger.getLogger(ModelBuilderMain.class.getName()).log(Level.SEVERE, null, e);
        }

        //TODO save model to firestore...

    }

    public static void getFeatures(String rawDataFile, String featureFile){
        String TAG = "ModelBuilderMain";
        Log.d(TAG,">>RUN>>getFeatures(RAWDATAUser,FEATURESUser); ");

        Settings.usingFrames(512);
        Settings.setOutputHasHeader(true); // full arff, no header

        try {
            FeatureExtractor.extractFeaturesFromCsvFileToCsvFile(rawDataFile, featureFile);
        } catch (Exception e) {
            //e.printStackTrace();
            Logger.getLogger(FeatureExtractor.class.getName()).log(Level.SEVERE, null, e);
        }
        Log.d(TAG,"<<FINISHED<<getFeatures(); ");
    }

    public static void CreateAndSaveModel(String userFeatureFilePath, String userModelFilePath){
        String TAG = "ModelBuilderMain";
        Log.d(TAG,">>RUN>>CreateAndSaveModel(FEATURESUser, MODELPATHUser); ");

        ModelGenerator mg = new ModelGenerator();

        //the create muxed feature function save the mixed data in the first file to use less space
        Instances dataset = mg.loadDataset(userFeatureFilePath);

        Filter filter = new Normalize();

        // divide dataset to train dataset 80% and test dataset 20%
        int trainSize = (int) Math.round(dataset.numInstances() * 0.8);
        int testSize = dataset.numInstances() - trainSize;

        dataset.randomize(new Debug.Random(1));// if you comment this line the accuracy of the model will be droped from 96.6% to 80%

        //Normalize dataset
        try {
            filter.setInputFormat(dataset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Instances datasetnor = null;
        try {
            datasetnor = Filter.useFilter(dataset, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Instances traindataset = new Instances(datasetnor, 0, trainSize);
        Instances testdataset = new Instances(datasetnor, trainSize, testSize);

        // build classifier with train dataset
        //MultilayerPerceptron ann = (MultilayerPerceptron) mg.buildClassifier(traindataset);
        RandomForest ann = (RandomForest) mg.buildClassifier(traindataset);

        // Evaluate classifier with test dataset
        String evalsummary = mg.evaluateModel(ann, traindataset, testdataset);
        System.out.println("Evaluation: " + evalsummary);

        //Save model
        mg.saveModel(ann, userModelFilePath);

        Log.d(TAG,"<<FINISHED<<CreateAndSaveModel(); ");
    }

    //the create muxed feature function save the mixed data in the first file to use less space
    public static void mergeArffFiles(String input, String output){
        String TAG = "ModelBuilderMain";
        Log.d(TAG,">>RUN>>mergeArffFiles(FEATURESDummy, FEATURESUser); ");

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(input));
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file " + input);
        }

        StringBuilder sb=new StringBuilder();
        Scanner scanner2 = null;

        try {
            scanner2 = new Scanner(new File(output));
        } catch (Exception ex) {
            System.out.println("File not found: "+output);
        }

        FileWriter writer = null;

        String line2=null;
        while(scanner2.hasNextLine()){
            line2 = scanner2.nextLine().trim();
            if(line2.contains("@attribute userID") || line2.contains("@attribute userid") || line2.contains("@attribute userId")){
                break;
            }
            sb.append(line2+"\n");
        }
        String line=null;
        while(scanner.hasNextLine()){
            line = scanner.nextLine().trim();
            if(line.contains("@attribute userID") || line.contains("@attribute userid") || line.contains("@attribute userId")){
                break;
            }
        }

        String item1=line.split(" ")[2];
        String item2=line2.split(" ")[2];
        sb.append("@attribute userID{"+ item1.substring(1,item1.length()-1) +","+ item2.substring(1,item2.length()-1) +"}\n\n");
        while(scanner.hasNextLine()){
            line = scanner.nextLine().trim();
            if(line.equals("@data")){
                break;
            }
        }

        while (scanner2.hasNextLine()) {

            line = scanner2.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }
            sb.append(line+"\n");
        }
        try {
            writer = new FileWriter(output, false);
        } catch (Exception ex) {
            System.out.println("File not found: "+output);
        }
        try {
            writer.write(sb.toString());
        } catch (IOException ex) {
            Logger.getLogger(ModelBuilderMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        while (scanner.hasNextLine()) {

            line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            try {
                //writer.println(line);
                writer.write(line+"\n");
            } catch (IOException ex) {
                Logger.getLogger(ModelBuilderMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        scanner.close();
        scanner2.close();
        try {
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(ModelBuilderMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        Log.d(TAG,"<<FINISHED<<mergeArffFiles(); ");
    }

}
