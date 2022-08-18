/*Exemplo plugin para k-nearest
   Prof. Joaquim Felipe */

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
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

class Imagem implements Comparable<Imagem>{
    public String img_name;
    public double dist;
    public String[] vector = new String[7];  

    public Imagem (double dist, String img_name, String[] vector) {
        this.dist = dist;
        this.img_name = img_name;
        for (int i = 0; i < 7; i++) 
        {
            this.vector[i] = vector[i];
        }
    }

    public double getDist(){
        return dist;
    }

    @Override public int compareTo(Imagem o){
        if (this.dist > o.getDist()) {
            return 1;
        }
        if (this.dist < o.getDist()) {
            return -1;
        }

        return 0;
    }
}

public class K_Nearest implements PlugInFilter {
    ImagePlus reference;        /* Reference image */ 
    int k;                      /*  Number of nearest neighbors */
    int level;                  /*  Wavelet decoposition level */

    public int setup(String arg, ImagePlus imp) {
        /* Criacao do arquivo Hu.txt */
        CreationHuTxt();
        
        reference = imp;
        ImageConverter ic = new ImageConverter(imp);
        ic.convertToGray8();

        /* Calculo dos 7 Momentos de Hu da imagem referencia, e os armazena no arquivo Hu.txt */
        imgReference(imp);

        return DOES_ALL;
    }

    public void run(ImageProcessor img) {
        GenericDialog gd = new GenericDialog("k-nearest neighbor search", IJ.getInstance());
        gd.addNumericField("Number of nearest neighbors (K):", 1, 0);
        gd.showDialog();
        if (gd.wasCanceled())
            return;
        k = (int) gd.getNextNumber();

        SaveDialog sd = new SaveDialog("Open search folder...", "any file (required)", "");
        if (sd.getFileName()==null) return;
        String dir = sd.getDirectory();

        search(dir);
    }

    public void search(String dir) {
        IJ.log("");
        IJ.log("Searching images");
        if (!dir.endsWith(File.separator))
            dir += File.separator;
        String[] list = new File(dir).list();
        if (list==null) return;

        if (k < 0 || k > list.length-1) {
            IJ.log("K invalido");
            return;
        }
        
        /* Calculo dos Momentos de Hu, os armazenando no arquivo Hu.txt */
        WriteOnHuTxt(list, dir);

        /* Caixa de dialogo para o usuario escolher a funcao distancia */
        int ans = dialogDistFunc();

        /* Calculo da funcao distancia de cada vetor de caracteristica da imagem referencia com as demais imagens */
        int index_1 = 0;
        double temp = 0.0;
        String imgname;
        String [] reference = new String [7];
        String [] newImg = new String [7];
        double [] referenceD = new double[7];
        double [] newImgD = new double [7];
        String[] arrOfStr = new String[8];
        List<Imagem> imagens = new ArrayList<Imagem>();
        Imagem res;

        if (ans ==1 || ans == 2 || ans == 3) {
            BufferedReader reader;
            try {
                File file = new File ("/home/nfs/12543590/ImageJ/plugins/Hu.txt");
                reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                arrOfStr = line.split(";", 8);
                for (int k = 0; k < 7; k++)
                {
                    reference[k] = arrOfStr[k];
                }
                referenceD = string_to_double(reference);
                line = reader.readLine();
                
                while (line != null && index_1 <= list.length-1) {
                    arrOfStr = line.split(";", 8);
                    for (int k = 0; k < 7; k++) {
                        newImg[k] = arrOfStr[k];
                    }
                    imgname = arrOfStr[7];
                    newImgD = string_to_double(newImg);
                    temp = DistAns(ans,referenceD, newImgD);
                    res = new Imagem(temp, imgname, newImg);
                    imagens.add(res);
                    index_1++;
                    line = reader.readLine();
                }
                reader.close();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }

            /* Ordenacao da classe Imagem com base no valor da variavel dist */
            Collections.sort(imagens);
            
            /* Criacao do arquivo FuncDist para armazenar nele o resultado da funcao distancia, 
            vetor da imagem, seu nome, e as k imagens mais proximas*/
            CreationFuncDistTxt();

            /* Escrita do resultado da funcao distancia, do vetor e do nome das demais imagens. Junto eh mostrado as imagens*/
            WriteOnFuncDistTxt(imagens);

            IJ.showProgress(1.0);
            IJ.showStatus("");   
        }  
        
        else {
            IJ.log("Funcao invalida");
        }
    }
    
    public double[] string_to_double(String[] str)
    {
        double[] vector = new double [7];
        for (int i = 0; i < 7; i++)
        {
            vector[i] = Double.parseDouble(str[i]);
        }
        return vector;
    }

    public void imgReference(ImagePlus imp)
    {
        try {
            FileWriter myWriter = new FileWriter("/home/nfs/12543590/ImageJ/plugins/Hu.txt");
            ImagePlus image = new Opener().openImage(imp.getOriginalFileInfo().directory, imp.getOriginalFileInfo().fileName);
            ImageAccess input = new ImageAccess(image.getProcessor());
            String str = "";
            double[] temp = Momentos_Hu.momentosHu(input);
            for (int p = 0; p < 7; p++)
            {
                str = str + temp[p] + ";";
            }
            
            myWriter.write(str);
            myWriter.write(imp.getOriginalFileInfo().fileName);
            myWriter.write("\n");
            myWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int dialogDistFunc()
    {
        GenericDialog gd = new GenericDialog("k-nearest neighbor search", IJ.getInstance());
        gd.addNumericField("Distance Function Number:", 1, 0);
        gd.addMessage("Manhattan - 1");
        gd.addMessage("Euclidiana - 2");
        gd.addMessage("Chebychev - 3");
        gd.showDialog();
        if (gd.wasCanceled())
            return 0;
        return (int) gd.getNextNumber();
    }

    public void WriteOnHuTxt(String[] list, String dir)
    {
        try {
            FileWriter myWriter = new FileWriter("/home/nfs/12543590/ImageJ/plugins/Hu.txt", true);

            /*  PASSA POR TODAS AS IMAGENS DA PASTA */
            for (int i=0; i<list.length; i++) {
                IJ.showStatus(i+"/"+list.length+": "+list[i]);   /* mostra na interface */
                IJ.showProgress((double)i / list.length);  /* barra de progresso */
                File f = new File(dir+list[i]);
                IJ.log(list[i]);
                if (!f.isDirectory()) {
                    ImagePlus image = new Opener().openImage(dir, list[i]); /* abre imagem */
                    if (image != null) {  
                        /* CODIGO PARA HU */ 
                        String str = "";

                        ImageAccess input = new ImageAccess(image.getProcessor()); 

                        double [] temp = Momentos_Hu.momentosHu(input);
                        for (int p = 0; p < 7; p++)
                        {
                            str = str + temp[p] + ";";
                        }
                        /* FIM DO CODIGO PARA HU */
                        
                        myWriter.write(str);
                        myWriter.write (list[i]);
                        myWriter.write("\n");
                    }
                }
            }
            myWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void CreationHuTxt()
    {
        try {
            File myObj = new File("/home/nfs/12543590/ImageJ/plugins/Hu.txt");
            if (myObj.createNewFile()) {
              System.out.println("File created: " + myObj.getName());
            } else {
              System.out.println("File already exists.");
            }
          } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void CreationFuncDistTxt()
    {
        try {
            File myObj = new File("/home/nfs/12543590/ImageJ/plugins/FuncDist.txt");
            if (myObj.createNewFile()) {
              System.out.println("File created: " + myObj.getName());
            } else {
              System.out.println("File already exists.");
            }
          } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void WriteOnFuncDistTxt(List<Imagem> imagens) 
    {
        try {
            FileWriter myWriter = new FileWriter("/home/nfs/12543590/ImageJ/plugins/FuncDist.txt");
            String res = "";
            for (int i = 0; i < k+1; i++) {
                res = " [ ";
                res = res + imagens.get(i).vector[0] + ";" + imagens.get(i).vector[1] + ";" + imagens.get(i).vector[2] + ";" + imagens.get(i).vector[3]
                + ";" + imagens.get(i).vector[4] + ";" + imagens.get(i).vector[5] + ";" + imagens.get(i).vector[6] + " ] ";
                myWriter.write(Double.toString(imagens.get(i).dist));
                myWriter.write(res);
                myWriter.write(imagens.get(i).img_name);
                myWriter.write("\n");
                ImagePlus image = new Opener().openImage("/home/nfs/12543590/Downloads/imagem_teste/", imagens.get(i).img_name); 
                if (image != null) {
                    image.show();
                }
            }
            myWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double DistAns(int answer, double[] arr1, double[] arr2)
    {
        if (answer == 1)
        {
            return DistanceFunctions.Manhattan(arr1, arr2);
        }
        else if (answer == 2) {
            return DistanceFunctions.Euclide(arr1, arr2);
        }
        else {
            return DistanceFunctions.Chebychev(arr1, arr2);
        }
    }
}