import edu.princeton.cs.algs4.Picture;

import java.util.Arrays;

public class SeamCarver{

    private static final double pixels = 1000d;

    private static final boolean yAxis = true; 
    private static final boolean xAxis = false;

    private Picture pic;

    public SeamCarver(Picture picture){
        if (picture == null)
        {
            throw new IllegalArgumentException();
        }
        this.pic = new Picture(picture);
    }

    public Picture picture()
    {
        return new Picture(this.pic);
    }

    public int width()
    {
        return pic.width();
    }

    public int height()
    {
        return pic.height();
    }


    public double energy(int x, int y)
    {
        pixValidation(x, y);

        if (x == 0 || x == (width() - 1) || y == 0 || y == (height() - 1))
        {
            return pixels;
        }
            
        double redX = pic.get(x + 1, y).getRed() - pic.get(x - 1, y).getRed();
        double greenX = pic.get(x + 1, y).getGreen() - pic.get(x - 1, y).getGreen();
        double blueX = pic.get(x + 1, y).getBlue() - pic.get(x - 1, y).getBlue();
        double redY = pic.get(x, y + 1).getRed() - pic.get(x, y - 1).getRed();
        double greenY = pic.get(x, y + 1).getGreen() - pic.get(x, y - 1).getGreen();
        double blueY = pic.get(x, y + 1).getBlue() - pic.get(x, y - 1).getBlue();

        return Math.sqrt(redX * redX + greenX * greenX + blueX * blueX + redY * redY + greenY * greenY + blueY * blueY);
    }

    public int[] findHorizontalSeam()
    {
        Pair[][] energies = new Pair[height()][width()];
        for (int i = 0; i < height(); i++){
            energies[i][0] = new Pair(pixels, -1);
        }
        for (int x = 1; x < width(); x++){
            energies[0][x] = new Pair(pixels, -1);
            for (int y = 0; y < height(); y++){
                relaxHorizontal(energies, y, x);
            }
        }
        return extractHorizontalSeam(energies);
    }

    public int[] findVerticalSeam()
    {
        Pair[][] energies = new Pair[height()][width()];
        for (int i = 0; i < width(); i++){
            energies[0][i] = new Pair(pixels, -1);
        }

        for (int y = 1; y < height(); y++){
            energies[y][0] = new Pair(pixels, -1);
            for (int x = 0; x < width(); x++){
                relaxVertical(energies, y, x);
            }
        }
        return extractVerticalSeam(energies);
    }


    public void removeHorizontalSeam(int[] seam)
    {
        if (!validSeam(seam, xAxis)){
            throw new IllegalArgumentException("Illegal seam!");
        }
        Picture seamedPicture = new Picture(width(), height() - 1);

        for (int x = 0; x < width(); x++){
            int yBias = 0;
            for (int y = 0; y < height() - 1; y++){
                if (seam[x] == y){
                    yBias = 1;
                }
                seamedPicture.set(x, y, pic.get(x, y + yBias));
            }
        }
        this.pic = seamedPicture;
    }

    public void removeVerticalSeam(int[] seam)
    {
        if (!validSeam(seam, yAxis)){
            throw new IllegalArgumentException("Illegal seam!");
        }
        Picture seamedPicture = new Picture(width() - 1, height());
        for(int y = 0; y < height(); y++){
            int xBias = 0;
            for(int x = 0; x < width() - 1; x++){
                if (seam[y] == x){
                    xBias = 1;
                }
                seamedPicture.set(x, y, pic.get(x + xBias, y));
            }
        }
        this.pic = seamedPicture;
    }

    private void pixValidation(int x, int y)
    {
        if (!validPixel(x, y)){
            throw new IllegalArgumentException("Invalid pixel: x: " + x + ", y: " + y );
        }
    }

    private boolean validPixel(int x, int y){
        return x > -1 && x < width() && y > -1 && y < height();
    }

    private void relaxVertical(Pair[][] energies, int y, int x)
    {
        double myEnergy = energy(x, y);
        Pair[] paths = {
            new Pair( validPixel(x - 1, y -1) ? myEnergy + energies[y - 1][x - 1].energy : Double.MAX_VALUE, x - 1),
            new Pair( validPixel(x, y - 1) ? myEnergy + energies[y - 1][x].energy : Double.MAX_VALUE, x),
            new Pair( validPixel(x + 1, y - 1) ? myEnergy + energies[y - 1][x + 1].energy : Double.MAX_VALUE, x + 1)
        };
        Arrays.sort(paths);
        energies[y][x] = paths[0];
    }


    private void relaxHorizontal(Pair[][] energies, int y, int x)
    {
        double myEnergy = energy(x, y);
        Pair[] paths = {
                new Pair( validPixel(x - 1, y - 1) ? myEnergy + energies[y - 1][x - 1].energy : Double.MAX_VALUE, y - 1),
                new Pair( validPixel(x - 1, y) ? myEnergy + energies[y][x - 1].energy : Double.MAX_VALUE, y),
                new Pair( validPixel(x - 1, y + 1) ? myEnergy + energies[y + 1][x - 1].energy : Double.MAX_VALUE, y + 1)
        };
        Arrays.sort(paths);
        energies[y][x] = paths[0];
    }

    private int[] extractVerticalSeam(Pair[][] energies)
    {
        int[] seam = new int[height()];
        double lowestEnergy = Double.MAX_VALUE;
        int index = -1;
        // find lowest energy
        for (int x = 0; x < width(); x++){
            if (energies[height() - 1][x].energy < lowestEnergy){
                lowestEnergy = energies[height() - 1][x].energy;
                index = x;
            }
        }

        int y = height() - 1;
        while (y > -1){
            seam[y] = index;
            index = energies[y][index].prev;
            y--;
        }
        return seam;
    }

    private int[] extractHorizontalSeam(Pair[][] energies)
    {
        int[] seam = new int[width()];
        double lowestEnergy = Double.MAX_VALUE;
        int index = -1;
        // find lowest energy
        for (int y = 0; y < height(); y++){
            if (energies[y][width() - 1].energy < lowestEnergy){
                lowestEnergy = energies[y][width() - 1].energy;
                index = y;
            }
        }

        int x = width() - 1;
        while (x > -1){
            seam[x] = index;
            index = energies[index][x].prev;
            x--;
        }
        return seam;
    }


    private boolean validSeam(int[] seam, boolean yAxis)
    {

        if (seam == null){
            return false;
        }

        if ((yAxis && seam.length != height()) || (!yAxis && seam.length != width())){
            return false;
        }

        for(int i : seam){
            if ((i < 0 ) || (yAxis && i >= width()) || (!yAxis && i>= height())){
                return false;
            }
        }
        for (int i = 0; i < seam.length - 1; i++){
            if (Math.abs(seam[i] - seam[i + 1]) > 1){
                return false;
            }
        }
        return true;
    }

    private class Pair implements Comparable<Pair>{
        public final double energy;
        public final int prev;
    
        public Pair(double energy, int prev) {
            this.energy = energy;
            this.prev = prev;
        }
    
        @Override
        public int compareTo(Pair o) {
            if (this.energy > o.energy){
                return 1;
            } else if (this.energy < o.energy){
                return -1;
            }
            return 0;
        }
    }

}