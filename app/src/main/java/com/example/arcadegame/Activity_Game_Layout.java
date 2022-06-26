package com.example.arcadegame;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.util.Pair;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Activity_Game_Layout extends SurfaceView implements Runnable {
    Thread thread = null;
    boolean canDraw = false;
    Canvas canvas;
    Bitmap bgrd;
    SurfaceHolder surfaceholder;
    Paint player_fill, obstacle_fill;
    Path some_path;
    Path path_here;
    double fps, fTimeInSec, fTimeInMSec, fTimeInNSec;
    double tLF, tEOR, delT;
    Random random = new Random();
    boolean[] isVisible = new boolean[30];
    Integer[] coordinatesOfObstacles = new Integer[60];
    int player_x;
    int player_y;
    int player_width;
    int player_height;
    int x_dir;
    int y_dir;
    int move_frwd = 1;
    int pushBack = 1;

    public Activity_Game_Layout(Context context) {
        super(context);
        surfaceholder = getHolder();
        bgrd = BitmapFactory.decodeResource(getResources(), R.drawable.bgpic);
        //setBackgroundResource(R.drawable.bgpic);
        player_x = 100;
        player_y = 100;
        player_width = 50;
        player_height = 50;
        x_dir = 1;
        y_dir = 7;
        fps = 20;
        fTimeInSec = 1/fps;
        fTimeInMSec = fTimeInSec*1000;
        fTimeInNSec = fTimeInMSec*1000000;
    }
    @Override
    public void run() {
        prepBrush();
        some_path = new Path();
        path_here = new Path();
        tLF = System.nanoTime();
        Pair<String[], Integer[]> obstacles = createObstacles();
        while (canDraw){
            if (surfaceholder.getSurface().isValid()) {
                canvas = surfaceholder.lockCanvas();
                canvas.drawBitmap(bgrd, 0, 0, null);
                coordinatesOfObstacles = initializeObstacles(obstacles.first, obstacles.second, coordinatesOfObstacles);
                some_path.reset();
                defaultPlayerMove();
                drawObstacles(obstacles.first, obstacles.second, 0, coordinatesOfObstacles, isVisible);
                drawQuad(player_x, player_y, player_x + player_width, player_y + player_height, 0);
                drawObstacles(obstacles.first, obstacles.second, 1, coordinatesOfObstacles, isVisible);
                checkCollisions();
                surfaceholder.unlockCanvasAndPost(canvas);
                updateObstaclesSeen(obstacles.first, obstacles.second, coordinatesOfObstacles, isVisible);
                coordinatesOfObstacles = updateObstaclePositions(coordinatesOfObstacles, isVisible);
            }
            tEOR = System.nanoTime();
            delT = fTimeInNSec - (tEOR-tLF);
            try{
                if (delT>0) Thread.sleep((long) (delT / 1000000));
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
            tLF = System.nanoTime();
        }
    }

    public void pause() {
        canDraw = false;
        while (true){
            try {
                thread.join();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        thread=null;
    }

    public void resume() {
        canDraw = true;
        thread = new Thread(this);
        thread.start();
    }

    public void prepBrush(){
        player_fill = new Paint();
        player_fill.setColor(Color.GRAY);
        player_fill.setStyle(Paint.Style.FILL);
        obstacle_fill = new Paint();
        obstacle_fill.setColor(Color.BLACK);
        obstacle_fill.setStyle(Paint.Style.STROKE);
    }

    public void drawQuad(int x1, int y1, int x2, int y2, int p){
        some_path.reset();
        some_path.moveTo(toPxs(x1), toPxs(y1));
        some_path.lineTo(toPxs(x2), toPxs(y1));
        some_path.lineTo(toPxs(x2), toPxs(y2));
        some_path.lineTo(toPxs(x1), toPxs(y2));
        some_path.lineTo(toPxs(x1), toPxs(y1));
        if (p==0){this.canvas.drawPath(some_path, player_fill);}
        else{this.canvas.drawPath(some_path, obstacle_fill);}

    }

    public void drawTriangle(int x1, int y1, int x2, int y2,int x3, int y3){
        path_here.reset();
        path_here.moveTo(toPxs(x1), toPxs(y1));
        path_here.lineTo(toPxs(x2), toPxs(y2));
        path_here.lineTo(toPxs(x3), toPxs(y3));
        path_here.lineTo(toPxs(x1), toPxs(y1));
        this.canvas.drawPath(some_path, obstacle_fill);
    }

    public int toPxs(int dps){
        return (int)(dps * getResources().getDisplayMetrics().density);
    }

    public void userTouched(){
        player_y = 100;
    }

    public void defaultPlayerMove(){
        if (player_x > canvas.getWidth()*0.6){
            move_frwd = -1;
        }
        if (move_frwd == -1 && player_x<100){
            move_frwd = 1;
        }
        //Log.d(TAG, "defaultPlayerMove: "+ Float.toString(toPxs(player_y)) + Float.toString(getHeight()));
        if (toPxs(player_y+player_height) > toPxs(canvas.getHeight())){
            player_y=100;
        }

        int rnd = random.nextInt(2);
        player_x += (move_frwd * x_dir * rnd);
        player_y += y_dir;
    }

    public void updateObstaclesSeen(String[] shapes, Integer[] sizes, Integer[] coords, boolean[] isOnScreen){
        for (int i=0;i<29;i++){
            if (isOnScreen[i] && !isOnScreen[i+1]){
                switch(shapes[i]){
                    case "Rectangle":
                        if (toPxs(coords[2*i]+300) < toPxs(canvas.getWidth())){
                            isOnScreen[i+1] = true;
                        }
                    case "Square":
                        if (toPxs(coords[2*i] + 300) < toPxs(canvas.getWidth())){
                            isOnScreen[i+1] = true;
                        }
                    case "Circle":
                        if (toPxs(coords[2*i]+sizes[2*i]) < toPxs(canvas.getWidth())){
                            isOnScreen[i+1] = true;
                        }
                    case "Triangle":
                        if (toPxs(coords[2*i] + sizes[2*i]) < toPxs(canvas.getWidth())){
                            isOnScreen[i+1] = true;
                        }
                    }
                }
            }
        }

    public Pair<String[], Integer[]> createObstacles(){
        String[] shapes=new String[]{"Circle", "Rectangle", "Square", "Triangle"};
        String[] finalShapesList = new String[30];
        Integer[] coordinates = new Integer[60];
        for (int i=0;i < 30; i++){
            String curr_shape = shapes[random.nextInt(shapes.length)];
            finalShapesList[i] = curr_shape;
            switch (curr_shape){
                case "Circle":
                    int radius = ThreadLocalRandom.current().nextInt(75, 151);
                    int y_val = ThreadLocalRandom.current().nextInt(0, 151);
                    coordinates[2*i] = radius;
                    coordinates[(2*i)+1] = y_val;
                case "Rectangle":
                    int width = ThreadLocalRandom.current().nextInt(50, 200);
                    int height = ThreadLocalRandom.current().nextInt(50, 350);
                    coordinates[(2*i)] = width;
                    coordinates[(2*i)+1] = height;
                case "Square":
                    int a = ThreadLocalRandom.current().nextInt(50, 200);
                    coordinates[(2*i)] = a;
                    coordinates[(2*i)+1] = a;
                case "Triangle":
                    int base = ThreadLocalRandom.current().nextInt(50, 200);
                    int perpendicular_height = ThreadLocalRandom.current().nextInt(50, 350);
                    coordinates[(2*i)] = base;
                    coordinates[(2*i)+1] = perpendicular_height;
            }
        }
    return new Pair<>(finalShapesList, coordinates);
    }

    public void drawObstacles(String[] shape, Integer[] sizes, Integer firstTime, Integer[] coords, boolean[] isOnScreen){
        if (firstTime==0){
            isOnScreen[0] = true;
            switch (shape[0]){
                case "Rectangle":
                    drawQuad(coords[0], coords[1], coords[0] + sizes[0], coords[1]+sizes[1], 1);
                case "Square":
                    drawQuad(coords[0], coords[1], coords[0] + sizes[0], coords[1]+sizes[1], 1);
                case "Circle":
                    this.canvas.drawCircle(coords[0], coords[1], sizes[1], obstacle_fill);
                case "Triangle":
                    drawTriangle(coords[0] + sizes[0]/2, coords[1], coords[0], coords[1] + sizes[1], coords[0] + sizes[0], coords[1]+sizes[1]);
            }
        }
        else{
            for (int i=0;i<30;i++){
                if (isOnScreen[i]) {
                    switch (shape[i]) {
                        case "Rectangle":
                            drawQuad(coords[i], coords[i+1], coords[i] + sizes[i], coords[i+1]+sizes[i+1], 1);
                        case "Square":
                            drawQuad(coords[i], coords[i+1], coords[i] + sizes[i], coords[i+1]+sizes[i+1], 1);
                        case "Circle":
                            this.canvas.drawCircle(coords[i], coords[i+1], sizes[i+1], obstacle_fill);
                        case "Triangle":
                            drawTriangle(coords[i] + sizes[i]/2, coords[i+1], coords[i], coords[i+1] + sizes[i+1], coords[i] + sizes[i], coords[i+1]+sizes[i+1]);
                    }
                }
            }
        }
    }

    public void checkCollisions(){

    }
    public Integer[] initializeObstacles(String[] types, Integer[] sizes, Integer[] coords){
        int i = 0;
        for (String shape: types){
            int rnd = random.nextInt(2);
            switch(shape) {
                case "Circle":
                    coords[i] = toPxs(canvas.getWidth());
                    if (rnd == 0) {
                        coords[i + 1] = toPxs(canvas.getHeight());
                    }
                    else{
                        coords[i + 1] = toPxs(0);
                    }
                case "Rectangle":
                    coords[i] = toPxs(canvas.getWidth());
                    if (rnd == 0) {
                        coords[i + 1] = toPxs(canvas.getWidth());
                    }
                    else{
                        coords[i + 1] = toPxs(0);
                    }
                case "Square":
                    coords[i] = toPxs(canvas.getWidth());
                    if (rnd == 0) {
                        coords[i + 1] = toPxs(canvas.getWidth() - sizes[(i/2)+1]);
                    }
                    else{
                        coords[i + 1] = toPxs(0);
                    }
                case "Triangle":
                    coords[i] = toPxs(canvas.getWidth());
                    if (rnd == 0) {
                        coords[i + 1] = toPxs(canvas.getWidth() - sizes[(i/2)+1]);
                    }
                    else{
                        coords[i + 1] = toPxs(0);
                    }
            }
            i+=2;
            Log.d(TAG, "initializeObstacles: "+ Arrays.toString(coords));
        }
        return coords;
    }

    public Integer[] updateObstaclePositions(Integer[] coords, boolean[] onScreen){
        for (int i=0;i<30;i++){
            if (onScreen[i]){
                coords[2*i] -= pushBack;
            }
        }
        return coords;
    }
}
