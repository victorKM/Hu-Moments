import java.io.*;
import ij.*;
import ij.io.*;
import ij.ImagePlus;
import ij.process.*;
import ij.gui.*;
import ij.plugin.filter.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;


public class Momentos_Hu {
    public Momentos_Hu() {
    }
    
	static public double [] momentosHu(ImageAccess input) {
		double [] Hu = new double[7];
        double m00 = 0.0;
        double m10 = 0.0;
        double m01 = 0.0;
        double center_x = 0.0;
        double center_y = 0.0;
        double u[][] = new double [4][4];
        double n[][] = new double [4][4];
        int nx = input.getWidth();
        int ny = input.getHeight();
        double value = 0.0;

        //calcular m00, m10, m01, u00, center_x e center_y
        for(int x = 0; x<nx; x++) {
            for(int y = 0 ; y<ny; y++) {
                value = input.getPixel(x,y);
                if (value != 0.0)
                {
                    m00++;
                    m10 += x+1;
                    m01 += y+1;
                }
            }
        }

        u[0][0] = m00;
        center_x = m10/m00;
        center_y = m01/m00;

        //calcular os u's necessarios
        u = U(u, center_x, center_y, value, nx, ny, input);
        
        //calcular os n's ncessarios
        n = N(n,u);

        //calcular momento de Hu e retorna-lo
        return Hus(n, u, Hu);
	}

    static public double [][] U(double[][] u, double center_x, double center_y, double value, int nx, int ny, ImageAccess input)
    {
        for(int x = 0; x<nx; x++) {
            for(int y=0;y<ny; y++) {
                value = input.getPixel(x,y);
                if (value != 0.0)
                {
                    u[0][2] += Math.pow(y-center_y, 2);
                    u[0][3] += Math.pow(y-center_y, 3);
                    u[1][1] += Math.pow(x-center_x, 1)*Math.pow(y-center_y, 1);
                    u[1][2] += Math.pow(x-center_x, 1)*Math.pow(y-center_y, 2);
                    u[2][0] += Math.pow(x-center_x, 2);
                    u[2][1] += Math.pow(x-center_x, 2)*Math.pow(y-center_y, 1);
                    u[3][0] += Math.pow(x-center_x, 3);
                }
            }
        }

        return u;
    }

    static public double [][] N(double[][] n, double[][] u)
    {
        n[0][2] = u[0][2]/Math.pow(u[0][0], 2);
        n[0][3] = u[0][3]/Math.pow(u[0][0], 2.5);
        n[1][1] = u[1][1]/Math.pow(u[0][0], 2);
        n[1][2] = u[1][2]/Math.pow(u[0][0], 2.5);
        n[2][0] = u[2][0]/Math.pow(u[0][0], 2);
        n[2][1] = u[2][1]/Math.pow(u[0][0], 2.5);
        n[3][0] = u[3][0]/Math.pow(u[0][0], 2.5);

        return n;
    }

    static public double [] Hus(double[][] n, double[][] u, double[] Hu)
    {
        Hu[0] = n[2][0] + n[0][2];

        Hu[1] = Math.pow(n[2][0] - n[0][2], 2) + 4*Math.pow(n[1][1], 2);

        Hu[2] = Math.pow(n[3][0] - 3*n[1][2], 2) + Math.pow(3*n[2][1] - n[0][3], 2);

        Hu[3] = Math.pow(n[3][0] + n[1][2], 2) + Math.pow(n[2][1]+n[0][3], 2);

        Hu[4] = (n[3][0] - 3*n[1][2])*(n[3][0] + n[1][2])*((Math.pow(n[3][0]+n[1][2], 2) - 3*Math.pow(n[2][1] + 3*n[0][3],2)))+
                (3*n[2][1] - n[0][3])*(n[2][1] + n[0][3])*((3*Math.pow(n[3][0] + n[2][1], 2) - Math.pow(n[2][1]+n[0][3],2)));

        Hu[5] = (n[2][0]-n[0][2])*((Math.pow(n[3][0] + n[1][2],2) - Math.pow(n[2][1] + n[0][3], 2))) + 
                4*n[1][1]*(n[3][0] + 3*n[1][2])*(n[2][1] + n[0][3]);

        Hu[6] = (3*n[2][1] - n[0][3])*(n[3][0]+ n[1][2])*((Math.pow(n[3][0] + n[1][2], 2) - 3*(Math.pow(n[2][1]+n[0][3],2))))+
                (3*n[1][2] - n[3][0])*(n[2][1] + n[0][3])*((3*Math.pow(n[3][0] + n[1][2], 2) - Math.pow(n[2][1] + n[0][3], 2)));

        return Hu;
    }
}
