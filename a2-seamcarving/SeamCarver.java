// Class SeamCarver
// Author: Nathaniel Hayden
// Purpose: Implement seam carving algorithm for images
//   (https://en.wikipedia.org/wiki/Seam_carving)
// Based on assignment 2 of Princeton Algorithms II course

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Arrays;
import edu.princeton.cs.algs4.Picture;
import edu.princeton.cs.algs4.StdOut;

public class SeamCarver {
    private static void pm(Object o) { StdOut.println(o); }
    
    private Picture p;
    
    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) throw new IllegalArgumentException();
        this.p = new Picture(picture);
    }
    
    private double deltaXSq(int x, int y) {
        Color cleft = p.get(x-1, y);
        Color cright = p.get(x+1, y);
        return  Math.pow(cleft.getRed() - cright.getRed(), 2) +
                Math.pow(cleft.getGreen() - cright.getGreen(), 2) +
                Math.pow(cleft.getBlue() - cright.getBlue(), 2);
    }
    private double deltaYSq(int x, int y) {
        Color cup = p.get(x, y-1);
        Color cdown = p.get(x, y+1);
        return  Math.pow(cup.getRed() - cdown.getRed(), 2) +
                Math.pow(cup.getGreen() - cdown.getGreen(), 2) +
                Math.pow(cup.getBlue() - cdown.getBlue(), 2);
    }
    
    // current picture
    public Picture picture() {
        return new Picture(p);
    }
    
    // width of current picture
    public int width() {
        return p.width();
    }
    // height of current picture
    public int height() {
        return p.height();
    }
    
    private boolean isValidXY(int x, int y) {
        if (x < 0 || x > p.width()-1 || y < 0 || y > p.height()-1)
            return false;
        return true;
    }
    
    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (!isValidXY(x, y)) throw new IllegalArgumentException();
        if (x == 0 || x == width()-1 ||  y == 0 || y == height()-1) return 1000.0;
        return Math.sqrt(deltaXSq(x, y) + deltaYSq(x, y));
    }
    
    private Picture transpose(Picture p) {
        Picture t = new Picture(height(), width());
        for (int row = 0; row < height(); row++) {
            for (int col = 0; col < width(); col++) {
                t.set(row, col, p.get(col, row));
            }
        }
        return t;
    }
    
    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        Picture orig = this.p;
        this.p = transpose(this.p);
        int[] seam = findVerticalSeam();
        this.p = orig;
        return seam;
    }
    
    private double[][] createEnergyArray() {
        double[][] e = new double[height()][width()];
        
        // initialize 1000s on borders
        for (int c = 0; c < p.width(); c++) e[0][c] = e[p.height()-1][c] = 1000.0;
        for (int r = 0; r < p.height(); r++) e[r][0] = e[r][p.width()-1] = 1000.0;
        
        for (int y = 1; y < p.height()-1; y++) {
            for (int x = 1; x < p.width()-1; x++) {
                e[y][x] = energy(x, y);
            }
        }
        return e;
    }
    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        
        if (p.height() == 1 || p.width() == 1) {
            int[] seam = new int[p.height()];
            for (int i = 0; i < seam.length; i++) seam[i] = 0;
            return seam;
        }
        
        double[][] e = createEnergyArray();
        
        int[][] nodeFrom = new int[height()][width()];
        double[][] distTo = new double[height()][width()];
        for (int x = 0; x < distTo[0].length; x++) distTo[0][x] = 1000.0;
        for (int y = 1; y < distTo.length; y++) {
          for (int x = 0; x < distTo[y].length; x++) {
              distTo[y][x] = Double.POSITIVE_INFINITY;
          }
        }
        
        for (int y = 0; y < p.height()-1; y++) {
            for (int x = 1; x < p.width()-1; x++) {
                double curDist = distTo[y][x];
                for (int offset = -1; offset <= 1; offset++) {
                    if (curDist + e[y+1][x+offset] < distTo[y+1][x+offset]) {
                        distTo[y+1][x+offset] = curDist + e[y+1][x+offset];
                        nodeFrom[y+1][x+offset] = x;
                    }
                }
            }
        }
        double minDist = Double.POSITIVE_INFINITY;
        int minIdx = Integer.MAX_VALUE;
        for (int i = 0; i < p.width(); i++) {
            // work from second-to-last row (last row adds no info)
            double curDist = distTo[p.height()-2][i];
            if (curDist < minDist) {
                minDist = curDist;
                minIdx = i;
            }
        }
        if (minIdx == Integer.MAX_VALUE) throw new IllegalArgumentException();
        
        ArrayDeque<Integer> tempseam = new ArrayDeque<>(p.height());
        tempseam.push(minIdx);
        for (int row = p.height()-1; row > 0; row--) {
            tempseam.push(nodeFrom[row][tempseam.peek()]);
        }
        
        int[] seam = new int[tempseam.size()];
        int count = 0;
        while (!tempseam.isEmpty()) {
            seam[count] = tempseam.pop();
            count++;
        }
        
        return seam;
    }
    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        if (seam == null) throw new IllegalArgumentException();
        if (height() <= 1) throw new IllegalArgumentException();
        if (!isValidHorizontalSeam(seam)) throw new IllegalArgumentException();
        
        this.p = transpose(this.p);
        removeVerticalSeam(seam);
        this.p = transpose(this.p);
    }
    
    //remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null) throw new IllegalArgumentException();
        if (width() <= 1) throw new IllegalArgumentException();
        if (!isValidVerticalSeam(seam)) throw new IllegalArgumentException();
        
        Picture cut = new Picture(width()-1, height());
        for (int row = 0; row < height(); row++) {
            for (int col = 0, ptr = 0; ptr < width()-1; col++, ptr++) {
                if (col == seam[row]) {
                    cut.set(col, row, p.get(++ptr, row));
                } else {
                    cut.set(col, row, p.get(ptr, row));
                }
            }
            if (seam[row] != width()-1) {
                cut.set(width()-2, row, p.get(width()-1, row));
            }
        }
        this.p = cut;
    }
    
    private boolean isValidHorizontalSeam(int[] seam) {
        return
            (seam.length == width() && isValidSequence(seam, height()-1)) ?
                true : false;
    }
    private boolean isValidVerticalSeam(int[] seam) {
        return 
            (seam.length == height() && isValidSequence(seam, width()-1)) ?
                true : false;
    }
    private boolean isValidSequence(int[] seam, int max) {
        if (seam[0] > max || seam[0] < 0) return false;
        for (int i = 1; i < seam.length; i++) {
            if (seam[i] < seam[i-1]-1 || seam[i] > seam[i-1]+1
                || seam[i] > max || seam[i] < 0) {
                return false;
            }
        }
        return true;
    }
    

    public static void main(String[] args) {
        SeamCarver sc = new SeamCarver(new Picture("seam/6x5.png"));
        int[] seam = sc.findVerticalSeam();
        pm(Arrays.toString(seam));
//        sc.removeVerticalSeam(seam);
        sc.removeVerticalSeam(new int[]{5, 5, 5, 5, 5});

    }

}
