package com.example.synthesizerc;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.ArrayList;

public class FinalSynthesizeApplication extends Application {
    static AnchorPane mainCenter;
    public  static  Circle speaker;
    public static  AudioComponent finalAudioComponent;
   public static Button mixerBtn;
    Clip c =  null;
    public static ArrayList<AudioComponentWidgetBase> widgets_ =new ArrayList<>();
    public static ArrayList<AudioComponentWidgetBase> Connected_widgets_ =new ArrayList<>();
//    ArrayList<AudioComponentWidgetBase> SpeakerWidgets = SpeakerWidget.SpeakerWidgets_;

    //mixer
    public static MixerWidget mix;
    public static VolumeWidget vol;
    @Override
    public void start(Stage stage) throws IOException {
        BorderPane mainLayout  = new BorderPane();
        /*********************** Top panel for the scene ***********************/
        HBox topPanel = new HBox();
        topPanel.setPadding(new Insets(5));
        topPanel.setSpacing(80);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setStyle("-fx-background-color: oldlace");

        Button E4_Note = new Button("E4");
        Button B_Note = new Button("B3");
        Button G_Note = new Button("G3");
        Button D_Note = new Button("D3");
        Button A_Note = new Button("A2");
        Button E2_Note = new Button("E2");

        // Reference: https://en.wikipedia.org/wiki/Guitar_tunings
        E4_Note.setOnAction(e -> PlayNoteKey(329.63));
        B_Note.setOnAction(e -> PlayNoteKey(246.94));
        G_Note.setOnAction(e -> PlayNoteKey(196.00));
        D_Note.setOnAction(e -> PlayNoteKey(146.83));
        A_Note.setOnAction(e -> PlayNoteKey(110.00));
        E2_Note.setOnAction(e -> PlayNoteKey(82.41));

        topPanel.getChildren().add(E4_Note);
        topPanel.getChildren().add(B_Note);
        topPanel.getChildren().add(G_Note);
        topPanel.getChildren().add(D_Note);
        topPanel.getChildren().add(A_Note);
        topPanel.getChildren().add(E2_Note);
        mainLayout.setTop(topPanel);

        //right panel
        VBox rightpanel = new VBox();
        rightpanel.setPadding(new Insets(60));
        rightpanel.setSpacing(80);
        rightpanel.setAlignment(Pos.CENTER);
        rightpanel.setStyle("-fx-background-color:rgb(246,229,229)");
        Button sinewaveBtn=new Button("SineWave");
        mixerBtn=new Button("Mixer");
        Button VolumeBtn3=new Button("Volume");
        mixerBtn.setPadding(new Insets(6));
        VolumeBtn3.setPadding(new Insets(5));
        sinewaveBtn.setOnAction(e->creatComponent());
        mixerBtn.setOnAction(e->{
            creatMixerComponent();
            //mixerBtn.setDisable(true);
        });
//        mixerBtn.setOnAction(e->{
//            mixerBtn.setDisable(true);
//        });

        VolumeBtn3.setOnAction(e->creatVolumeComponent());
        rightpanel.getChildren().add(sinewaveBtn);
        rightpanel.getChildren().add(mixerBtn);
        rightpanel.getChildren().add(VolumeBtn3);

        //center panel
        mainCenter = new AnchorPane();
        mainCenter.setStyle("-fx-background-color: #ffffff");
        speaker = new Circle(400,200,15);
        RadialGradient gradient1 = new RadialGradient(0,
                .1,
                0.5,
                0.5,
                2,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.YELLOW),
                new Stop(1, Color.RED));
        speaker.setFill(gradient1);
        mainCenter.getChildren().add(speaker);

        mainLayout.setCenter(mainCenter);
        mainLayout.setLeft(rightpanel);

        //Bottom Panel
        HBox bottomPanel = new HBox();
        bottomPanel.setStyle("-fx-background-color: #d2dde8");
        Button playBtn = new Button("PLAY");
        Button stopBtn = new Button("STOP");
        bottomPanel.getChildren().add(playBtn);
        bottomPanel.getChildren().add(stopBtn);
        bottomPanel.setAlignment(Pos.CENTER);
        mainLayout.setBottom(bottomPanel);
        playBtn.setOnAction( e -> {
            playAudio();
        });
        stopBtn.setOnAction(e-> {
            stopAudio();
                });

        Scene scene = new Scene(mainLayout,800,600);
        stage.setTitle("💗Spencer's Synthesizer💗");
        stage.setScene(scene);
        stage.show();

    }
    private void playAudio(){
        try {
            c = AudioSystem.getClip();
            AudioFormat format16 = new AudioFormat(44100, 16, 1, true, false);
           Mixer  mixer = new Mixer();
           Volume volume = new Volume();
           int count = 0;

           for(AudioComponentWidgetBase w: Connected_widgets_){
               if(w instanceof  SineWaveWidget){
                   mixer.connectInput(w.ac_);
                   count++;
               }
//at least one SineWaveWidget is connected to the mixer, otherwise the audio will not play.
           }

           if(Connected_widgets_.size()>0 && count > 0){
               volume.connectInput(mixer);
               if(VolumeWidget.VolumeSliderValue != 0){
                   volume.setVolumeScale(VolumeWidget.VolumeSliderValue);
               }



               AudioClip clip = volume.getClip();

//            byte[] data = widgets_.get(0).ac_.getClip().getData();

               c.open(format16, clip.getData(), 0, clip.getData().length);
               c.start();
               AudioListener listener= new AudioListener(c);
               c.addLineListener(listener);
           }
           else {
               System.out.println("No connected Wigets");
           }

        }
        catch (LineUnavailableException k){
            System.out.println(k.getMessage());
        }
    }

    private  void stopAudio(){
        if(c == null){
            return;
        }
        else
        {
            c.stop();
        }
    }

    private void creatComponent() {
//creat new object AudioComponentWidget and show it
        AudioComponent sineWave = new SineWave(200);
        AudioComponentWidgetBase acw = new SineWaveWidget(sineWave,mainCenter,"Sine Wave ");
        ((SineWaveWidget) acw).addFrequencySlider();
        mainCenter.getChildren().add(acw);
        widgets_.add(acw);

    }

    private void creatMixerComponent() {
//creat new object mixer Widget and show it
        AudioComponent mixer = new Mixer();
        //ArrayList<AudioComponent> mixer = new ArrayList<>();
        MixerWidget acw = new MixerWidget(mixer,mainCenter,"Mixer");
        acw.addInputCircle();
        mainCenter.getChildren().add(acw);
        widgets_.add(acw);
        mix = acw;
    }
    private  void creatVolumeComponent(){
        Volume volume = new Volume(10);
        VolumeWidget acw = new VolumeWidget(volume,mainCenter,"Volume ");
        acw.addVolumeSlider();
        acw.addInputCircle();
        mainCenter.getChildren().add(acw);
        widgets_.add(acw);
        vol = acw;
    }


    private void PlayNoteKey(double frequency){
        Clip c = null; // Note, this is different from our AudioClip class.
        try {
            c = AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

// This is the format that we're following, 44.1 KHz mono audio, 16 bits per sample.
        AudioFormat format16 = new AudioFormat(44100, 16, 1, true, false);
        AudioComponent gen = new SineWave(frequency);
        AudioClip mixedClip = gen.getClip();


        try {
            c.open(format16, mixedClip.getData(), 0, mixedClip.getData().length); // Reads data from our byte array to play it.
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        System.out.println("About to play...");
        c.start(); // Plays it.
        c.loop(0); // Plays it 2 more times if desired, so 6 seconds total

// Makes sure the program doesn't quit before the sound plays.
        while (c.getFramePosition() < AudioClip.TOTAL_SAMPLES || c.isActive() || c.isRunning()) {
            // Do nothing while we wait for the note to play.
        }
        System.out.println("Done.");
        c.close();
    }
    public static void main(String[] args) {
        launch();
    }
}