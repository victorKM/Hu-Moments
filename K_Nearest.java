import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.io.Opener;
import ij.io.SaveDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    ImagePlus reference;        
    int k;                                  

    public int setup(String arg, ImagePlus imp) {
        /* Criacao do arquivo Hu.txt */
        CreationHuTxt();

        reference = imp;

        return DOES_ALL;
    }

    public void run(ImageProcessor img) {
        GenericDialog gd = new GenericDialog("k-nearest neighbor search", IJ.getInstance());
        gd.addNumericField("Number of nearest neighbors (K):", 1, 0);
        gd.showDialog();
        if (gd.wasCanceled())
            return;
        k = (int) gd.getNextNumber();

        String dir = reference.getOriginalFileInfo().directory;
        ImageConverter ic = new ImageConverter(reference);
        ic.convertToGray8();

        /* Calculo dos 7 Momentos de Hu da imagem referencia, e os armazena no arquivo Hu.txt */
        imgReference(reference);
    
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
        int index = 0;
        double distanceResult = 0.0;
        String imgname;
        String [] reference = new String [7];
        String [] newImg = new String [7];
        double [] referenceD = new double[7];
        double [] newImgD = new double [7];
        String[] str = new String[8];
        Imagem[] imgs = new Imagem[70];

        if (ans >=1 && ans <= 3) {
            BufferedReader reader;
            try {
                File file = new File ("C:\\Faculdade\\ImageJ\\plugins\\huMoments\\Hu.txt");
                reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                str = line.split(";", 8);
                for (int k = 0; k < 7; k++)
                {
                    reference[k] = str[k];
                }
                referenceD = string_to_double(reference);
                line = reader.readLine();
                
                while (line != null && index <= list.length-1) {
                    str = line.split(";", 8);
                    for (int k = 0; k < 7; k++) {
                        newImg[k] = str[k];
                    }
                    imgname = str[7];
                    newImgD = string_to_double(newImg);
                    if (ans == 1) {
                        distanceResult = DistanceFunctions.Manhattan(referenceD, newImgD);
                    } else if (ans == 2) {
                        distanceResult = DistanceFunctions.Euclide(referenceD, newImgD);
                    } else {
                        distanceResult = DistanceFunctions.Chebychev(referenceD, newImgD);
                    }
                    imgs[index] = new Imagem(distanceResult, imgname, newImg);
                    index++;
                    line = reader.readLine();
                }
                reader.close();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }

            /* Ordenacao da classe Imagem com base no valor da variavel dist */
            List<Imagem> imagens = new ArrayList<Imagem>();
            for (int i = 0; i < index; i++) {
                imagens.add(imgs[i]);
            }
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
            FileWriter myWriter = new FileWriter("C:\\Faculdade\\ImageJ\\plugins\\huMoments\\Hu.txt");
            ImageAccess input = new ImageAccess(imp.getProcessor());
            String str = "";
            double[] hu = Momentos_Hu.momentosHu(input);
            for (int p = 0; p < 7; p++)
            {
                str = str + hu[p] + ";";
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
            FileWriter myWriter = new FileWriter("C:\\Faculdade\\ImageJ\\plugins\\huMoments\\Hu.txt", true);

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

                        double [] hu = Momentos_Hu.momentosHu(input);
                        for (int p = 0; p < 7; p++)
                        {
                            str = str + hu[p] + ";";
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
            File myObj = new File("C:\\Faculdade\\ImageJ\\plugins\\huMoments\\Hu.txt");
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
            File myObj = new File("C:\\Faculdade\\ImageJ\\plugins\\huMoments\\FuncDist.txt");
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
            FileWriter myWriter = new FileWriter("C:\\Faculdade\\ImageJ\\plugins\\huMoments\\FuncDist.txt");
            String str = "";
            for (int i = 0; i < k+1; i++) {
                str = " [ ";
                str = str + imagens.get(i).vector[0] + ";" + imagens.get(i).vector[1] + ";" + imagens.get(i).vector[2] + ";" + imagens.get(i).vector[3]
                + ";" + imagens.get(i).vector[4] + ";" + imagens.get(i).vector[5] + ";" + imagens.get(i).vector[6] + " ] ";
                myWriter.write(Double.toString(imagens.get(i).dist));
                myWriter.write(str);
                myWriter.write(imagens.get(i).img_name);
                myWriter.write("\n");
                ImagePlus image = new Opener().openImage("C:\\Faculdade\\ImageJ\\imagens", imagens.get(i).img_name); 
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
}