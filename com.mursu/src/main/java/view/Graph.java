package view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders a continuously updating line chart onto a JavaFX {@link Canvas}.
 *
 * <p>The graph is used by the simulator UI to visualize a moving stream of
 * numeric values, such as the current mid-price during a simulation run.
 * Internally it keeps a sliding window of the latest values up to
 * {@code maxPoints}, redraws the canvas on every JavaFX animation pulse, and
 * paints a background grid plus a formatted price axis.</p>
 */
public class Graph {
    private final Canvas canvas;
    private final GraphicsContext gc;

    private final List<Double> values = new ArrayList<>();

    private final int maxPoints;

    private final double gridSpacingX = 50;
    private final double priceAxisWidth = 70;

    /**
     * Creates a graph renderer bound to the given canvas and starts automatic repainting.
     *
     * @param canvas the canvas used as the drawing surface
     * @param maxPoints the maximum number of recent values kept in memory and shown
     */
    public Graph(Canvas canvas, int maxPoints) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.maxPoints = maxPoints;

        startAnimation();
    }

    /**
     * Appends a new value to the graph.
     *
     * <p>If the internal buffer grows beyond {@code maxPoints}, the oldest value
     * is discarded so the chart remains a fixed-width sliding window.</p>
     *
     * @param value the next numeric sample to display
     */
    public void addValue(double value) {
        values.add(value);

        if(values.size() > maxPoints){
            values.remove(0);
        }
    }

    private void startAnimation(){
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                draw();
            }
        };

        timer.start();
    }

    private void draw(){
        double fullWidth = canvas.getWidth();
        double fullHeight = canvas.getHeight();

        double width = fullWidth - priceAxisWidth;
        double height = fullHeight;

        gc.setFill(Color.BLACK);
        gc.fillRect(0,0,fullWidth,fullHeight);

        if(values.isEmpty()) {return;}

        double max = values.stream().mapToDouble(v->v).max().orElse(1);
        double min = values.stream().mapToDouble(v->v).min().orElse(0);

        double range = max-min;
        if(range == 0) {range = 1;}

        double padding = Math.max(0.5, range * 0.5);
        min -= padding;
        max += padding;
        range = max - min;

        double step = niceStep(range);

        double gridStart = Math.floor(min/step) * step;
        double gridEnd = Math.ceil(max/step) * step;

        drawGrid(width,height,gridStart,gridEnd,step);
        drawLine(width,height,gridStart,gridEnd);
        drawPriceAxis(width,height,gridStart,gridEnd,step);
    }

    private void drawGrid(double width,double height,double gridStart,double gridEnd,double step) {
        gc.setStroke(Color.rgb(60,60,60));
        gc.setLineWidth(1);

        double range = gridEnd-gridStart;

        for(double value = gridStart; value <= gridEnd; value += step) {
            double normalized = (value-gridStart)/range;
            double y = height - normalized*height;

            gc.strokeLine(0,y,width,y);
        }

        for(double x = 0; x < width; x += gridSpacingX) {
            gc.strokeLine(x,0,x,height);
        }
    }

    private void drawLine(double width,double height,double min,double max) {
        if(values.size()<2) {return;}

        double range = max-min;
        if(range==0) {range = 1;}

        double stepX = width/(double)maxPoints;

        gc.save();
        gc.beginPath();
        gc.rect(0,0,width,height);
        gc.clip();

        gc.setStroke(Color.LIME);
        gc.setLineWidth(2);

        gc.beginPath();

        for(int i=0;i<values.size();i++) {
            double normalized = (values.get(i)-min)/range;

            double x = i*stepX;
            double y = height-normalized*height;

            if(i==0){
                gc.moveTo(x,y);
            }else{
                gc.lineTo(x,y);
            }
        }

        gc.stroke();
        gc.restore();
    }

    private void drawPriceAxis(double width,double height,double start,double end,double step) {
        gc.setFill(Color.rgb(40,40,40));
        gc.fillRect(width,0,priceAxisWidth,height);

        gc.setFill(Color.WHITE);

        double range = end-start;

        for(double value=start; value<=end; value+=step) {
            double normalized = (value-start)/range;
            double y = height-normalized*height;

            if(y < 10 || y > height-2) {continue;}
            String label = formatPrice(value);

            gc.fillText(label,width+8,y+4);
        }
    }

    private double niceStep(double range) {
        double rough = range/6;

        double exponent = Math.floor(Math.log10(rough));
        double fraction = rough/Math.pow(10,exponent);

        double niceFraction;

        if(fraction<1.5) {
            niceFraction = 1;
        } else if(fraction<3) {
            niceFraction = 2;
        } else if(fraction<7) {
            niceFraction = 5;
        } else {
            niceFraction = 10;
        }

        return niceFraction*Math.pow(10,exponent);
    }

    private String formatPrice(double value) {
        if(value>=100)
            return String.format("%.0f",value);
        else if(value>=10)
            return String.format("%.1f",value);
        else
            return String.format("%.2f",value);
    }
}
