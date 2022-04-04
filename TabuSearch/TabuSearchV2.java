

import java.io.*;
import java.util.*;

public class TabuSearchV2 {

    static String[] smallInstancesFiles = { "f1_l-d_kp_10_269", "f2_l-d_kp_20_878", "f3_l-d_kp_4_20", "f4_l-d_kp_4_11", "f5_l-d_kp_15_375", "f6_l-d_kp_10_60", "f7_l-d_kp_7_50", "f8_l-d_kp_23_10000", "f9_l-d_kp_5_80", "f10_l-d_kp_20_879" };
    static String[] largeInstancesFiles = { "knapPI_1_100_1000_1", "knapPI_1_200_1000_1", "knapPI_1_500_1000_1", "knapPI_1_1000_1000_1", "knapPI_1_2000_1000_1", "knapPI_1_5000_1000_1", "knapPI_1_10000_1000_1" };
    static double[] smallInstancesOptimum = { 295, 1024, 35, 23, 481.0694, 52, 107, 9767, 130, 1025 };
    static double[] largeInstancesOptimum = { 9147, 11238, 28857, 54503, 110625, 276457, 563647 };
    static int N; // liczba przedmiotów
    static Double s[]; // wielkości obiektów
    static Double w[]; // wartości obiektów
    static int B; // pojemność pojemnika
    static ArrayList<Ruch> listaTabu; // lista tabu
    static Random random = new Random();


    static void WczytajDaneZPliku(String nazwa_pliku) throws IOException{
        BufferedReader fileReader = null;
        try{
            fileReader = new BufferedReader(new FileReader(nazwa_pliku));
            // odczyt pierwszej linii
            String pierwszaLinia = fileReader.readLine();
            String[] pierwszaLinia_podzielona = pierwszaLinia.split(" ");
            N = Integer.parseInt(pierwszaLinia_podzielona[0]); // liczba przedmiotow
            B = Integer.parseInt(pierwszaLinia_podzielona[1]); // pojemnosc pojemnika
            s = new Double[N];
            w = new Double[N];
            // odczyt kolejnych linii
            for(int k = 0; k < N; k++){
                String kolejnaLinia = fileReader.readLine();
                String[] podzielona_linia = kolejnaLinia.split(" ");
                w[k] = Double.parseDouble(podzielona_linia[0]); // wartość przedmiotu
                s[k] = Double.parseDouble(podzielona_linia[1]); // wielkość przedmiotu
            }
        }
        finally{
            if(fileReader != null) fileReader.close();
        }
    }
    static int sumaIloczynow(Double[] tab1, int[] tab2){
        int wynik = 0;
        for(int i = 0; i < N; i++){
            wynik += tab1[i] * tab2[i];
        }
        return wynik;
    }

    static class Ruch {
        public int przedmiot_1;
        public int przedmiot_2;
        public Ruch(int przedmiot_1, int przedmiot_2) {
            this.przedmiot_1 = przedmiot_1;
            this.przedmiot_2 = przedmiot_2;
        }
    }

    static boolean jestTabu(Ruch rv){
        for(int i = 0; i < listaTabu.size(); i++){
            Ruch r = listaTabu.get(i);
            if((r.przedmiot_1 == rv.przedmiot_1) &&
                    (r.przedmiot_2 == rv.przedmiot_2)){
                return true;
            }

            if((r.przedmiot_1 == rv.przedmiot_2) &&
                    (r.przedmiot_2 == rv.przedmiot_1)){
                return true;
            }
        }
        return false;
    }

    /* 
     *  ******** METODA TABU SEARCH DLA WSZYSTKICH WARIANTÓW ********
    */

    static double TabuSearch(int max_iter, int dlTabu, int dlKadencji, boolean poczatkoweRozwStosunek, boolean wlasneSasiedztwo){
        listaTabu = new ArrayList<>();
        int obecnaKad = 0;
        int[] vc;
        if(!poczatkoweRozwStosunek)
            vc = UtworzLosoweRozwiazanie();
        else
            vc = UtworzPoczatkoweRozwiazanieStosunek();
        double Vc = sumaIloczynow(w, vc);
        int[] best = vc;
        double Best = Vc;
        for(int i = 0; i < max_iter; i++){
            Ruch rv;
            if(!wlasneSasiedztwo)
                rv = WybierzRozwiazanieZOtoczenia(vc);
            else
                rv = WybierzRozwiazanieZOtoczeniaWlasne(vc);
           if(jestTabu(rv)){
               int[] buff = vc;
               buff[rv.przedmiot_1] = 0;
               buff[rv.przedmiot_2] = 1;
               if(sumaIloczynow(w, buff) > Vc && sumaIloczynow(s, buff) <= B){
                   
               }
               else{
                   continue;
               }
           }

           int[] vn = vc;
           vn[rv.przedmiot_1] = 0;
           vn[rv.przedmiot_2] = 1;
           listaTabu.add(rv);
           double Vn = sumaIloczynow(w, vn);
           if(Vn < Vc){
               vc = vn;
               Vc = Vn;
           }

            if(Vn > Best){
                best = vn;
                Best = Vn;
            }

            if(listaTabu.size() >= dlTabu){
                listaTabu.remove(0);
            }

            if(obecnaKad >= dlKadencji){
                obecnaKad = 0;
                listaTabu.remove(0);
            }

            obecnaKad++;
        }

        return Best;
    }

    /* 
    *  ******** TWORZENIE ROZWIĄZAŃ POCZĄTKOWYCH ********
    */

    static int[] UtworzPoczatkoweRozwiazanieStosunek() {
        int[] x = new int[N];
        // Struktura tablicy obliczoneWagi:
        // obliczoneWagi[obliczony stosunek wi/si][index (numer) przedmiotu]
        double[][] obliczoneWagi = new double[N][2];
        for(int i = 0; i < s.length; i++){
            obliczoneWagi[i][0] = w[i] / s[i];
            obliczoneWagi[i][1] = i;
        }
        Arrays.sort(obliczoneWagi, (v1, v2) -> Double.compare(v2[0], v1[0]));
        for(int i = 0; i < obliczoneWagi.length; i++){
            x[(int) obliczoneWagi[i][1]] = 1;
            if(sumaIloczynow(s, x) == B){
                break;
            }
            else if(sumaIloczynow(s, x) >= B){
                x[(int) obliczoneWagi[i][1]] = 0;
                break;
            }
        }

        return x;
    }

    static int[] UtworzLosoweRozwiazanie(){
        int[] x = new int[N];
        while(sumaIloczynow(s, x) < B){
            int los = random.nextInt(N);
            x[los] = 1;
            if(sumaIloczynow(s, x) > B){
                x[los] = 0;
                break;
            }
        }

        if(sumaIloczynow(s, x) == 0){
            UtworzLosoweRozwiazanie();
        }
        return x;
    }

    /*  
     *  ******** WYBIERANIE ROZWIĄZAŃ Z OTOCZENIA ********
    */

    static Ruch WybierzRozwiazanieZOtoczenia(int[] x){
        int doUsuniecia = random.nextInt(N);
        while(x[doUsuniecia] == 0){
            doUsuniecia = random.nextInt(N);
        }
        // dodaj przedmiot do x ktorego jeszcze  nie bylo
        int doDodania = random.nextInt(N);
        while(doUsuniecia == doDodania || x[doDodania] == 1){
            doDodania = random.nextInt(N);
        }
        Ruch ruch = new Ruch(doUsuniecia, doDodania);
        return ruch;
    }

    // własne sąsiedztwo polegające na wybraniu elementu o najmniejszej wartości
    static Ruch WybierzRozwiazanieZOtoczeniaWlasne(int[] x){
        Double[] obliczoneWartosciZaladowanychPrzedmiotow = new Double[N];
        for(int i = 0; i < x.length; i++){
            obliczoneWartosciZaladowanychPrzedmiotow[i] = x[i] * w[i];
        }
        List originalArray = Arrays.asList(obliczoneWartosciZaladowanychPrzedmiotow);
        double najmniejszaWartosc = (double) Collections.max(originalArray);
        for(int i = 0; i < originalArray.size(); i++){
            if((Double) originalArray.get(i) < najmniejszaWartosc && (Double) originalArray.get(i) != 0.00){
                najmniejszaWartosc = (Double) originalArray.get(i);
            }
        }
        int najmniejszaWartoscIndex = originalArray.indexOf(najmniejszaWartosc);
        List arr = Arrays.asList(w);
        Collections.sort(arr);
        for(int i = arr.size()-1; i >= 0; i--){
            x[najmniejszaWartoscIndex] = 0;
            x[arr.indexOf(arr.get(i))] = 1;
            if(sumaIloczynow(s, x) > B){
                x[arr.indexOf(arr.get(i))] = 0;
                x[najmniejszaWartoscIndex] = 1;
            }
            else{
                return new Ruch(najmniejszaWartoscIndex, arr.indexOf(arr.get(i)));
            }
        }
        return null;
    }

    /*  
     *  ******** ZAPISYWANIE DANYCH DO PLIKU ********
    */

    static void doPliku(String outputFileName, String instanceFileName, int liczbaPrzedmiotow, int pojemnoscPlecaka, double[] wyniki, double sredniaWynikow, double MRE[], double sredniaMRE) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName, true));
        writer.append("\n\nPlik: " + instanceFileName);
        writer.append("\nLiczba przedmiotów: " + liczbaPrzedmiotow);
        writer.append("\nPojemnność plecaka: " + pojemnoscPlecaka);
        writer.append("\nOtrzymane wyniki:");
        for(int i = 0; i < wyniki.length; i++){
            writer.append("\n" + wyniki[i]);
        }
        writer.append("\nŚrednia wyników: " + sredniaWynikow);
        writer.append("\n\nŚrednie wartości względne:");
        for(int i = 0; i < MRE.length; i++){
            writer.append("\n" + MRE[i]);
        }
        writer.append("\nŚrednia MRE: " + sredniaMRE);
        writer.append("\n\n-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=\n");
        writer.close();
    }

    /*  
     *  ******** PRZEPROWADZANIE TESTÓW ********
    */

    static void TestSmallInstances(int liczbaEksperymentow, String outputFileName, boolean poczRozwStosunek, boolean wlasneSasiedztwo) throws IOException{
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName, true));
            if(!poczRozwStosunek && !wlasneSasiedztwo)
                writer.append("\n**************************\n******  WARIANT 1.  ******\n**************************");
            else if(poczRozwStosunek && !wlasneSasiedztwo)
                writer.append("\n**************************\n******  WARIANT 2.  ******\n**************************");
            else if(!poczRozwStosunek && wlasneSasiedztwo)
                writer.append("\n**************************\n******  WARIANT 3.  ******\n**************************");
            else
                writer.append("\n**************************\n******  WARIANT 4.  ******\n**************************");
            writer.close();
        // dla każdego pliku instance przeprowadź eksperyment n-razy
        for(int i = 0; i < smallInstancesFiles.length; i++){
            try{
                WczytajDaneZPliku("small_instances/" + smallInstancesFiles[i]);
                System.out.println("\n\nPlik: " + smallInstancesFiles[i]);
                System.out.println("Liczba przedmiotów: " + N);
                System.out.println("Pojemność plecaka: " + B);
                int suma = 0;
                double wyniki[] = new double[liczbaEksperymentow];
                double MRE[] = new double[liczbaEksperymentow];
                double sumaMRE = 0;
                for (int j = 0; j < liczbaEksperymentow; j++){
                    wyniki[j] = TabuSearch(10000, 50, 50, poczRozwStosunek, wlasneSasiedztwo);
                    suma += wyniki[j];
                    // obliczenie błędu względnego MRE
                    MRE[j] = Math.abs(smallInstancesOptimum[i] - wyniki[j]) / smallInstancesOptimum[i];
                    sumaMRE += MRE[j];
                }
                double srednia = suma / liczbaEksperymentow;
                double sredniaMRE = sumaMRE / liczbaEksperymentow;
                System.out.println("Średnia wyników: " + srednia);
                System.out.println("Średnia MRE: " + sredniaMRE);

                // zapis do pliku
               
                    if(poczRozwStosunek)
                        doPliku(outputFileName, smallInstancesFiles[i], N, B, wyniki, srednia, MRE, sredniaMRE);
                    else
                        doPliku(outputFileName, smallInstancesFiles[i], N, B, wyniki, srednia, MRE, sredniaMRE);
            }
             catch (IOException e){
                System.out.println("Wystąpił błąd podczas otwarcia pliku " + smallInstancesFiles[i]);
            }
        }
    }


    static void TestLargeInstances(int liczbaEksperymentow, String outputFileName, boolean poczRozwStosunek, boolean wlasneSasiedztwo) throws IOException{
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName, true));
            if(!poczRozwStosunek && !wlasneSasiedztwo)
                writer.append("\n**************************\n******  WARIANT 1.  ******\n**************************");
            else if(poczRozwStosunek && !wlasneSasiedztwo)
                writer.append("\n**************************\n******  WARIANT 2.  ******\n**************************");
            else if(!poczRozwStosunek && wlasneSasiedztwo)
                writer.append("\n**************************\n******  WARIANT 3.  ******\n**************************");
            else
                writer.append("\n**************************\n******  WARIANT 4.  ******\n**************************");
            writer.close();
        // dla każdego pliku instance przeprowadź eksperyment n-razy
        for(int i = 0; i < largeInstancesFiles.length; i++){
            try{
                WczytajDaneZPliku("large_instances/" + largeInstancesFiles[i]);
                System.out.println("\n\nPlik: " + largeInstancesFiles[i]);
                System.out.println("Liczba przedmiotów: " + N);
                System.out.println("Pojemność plecaka: " + B);
                int suma = 0;
                double wyniki[] = new double[liczbaEksperymentow];
                double MRE[] = new double[liczbaEksperymentow];
                double sumaMRE = 0;
                for (int j = 0; j < liczbaEksperymentow; j++){
                    wyniki[j] = TabuSearch(10000, 50, 50, poczRozwStosunek, wlasneSasiedztwo);
                    suma += wyniki[j];
                    // obliczenie błędu względnego MRE
                    MRE[j] = Math.abs(largeInstancesOptimum[i] - wyniki[j]) / largeInstancesOptimum[i];
                    sumaMRE += MRE[j];
                }
                double srednia = suma / liczbaEksperymentow;
                double sredniaMRE = sumaMRE / liczbaEksperymentow;
                System.out.println("Średnia wyników: " + srednia);
                System.out.println("Średnia MRE: " + sredniaMRE);
                // zapis do pliku
                    if(poczRozwStosunek)
                        doPliku(outputFileName, largeInstancesFiles[i], N, B, wyniki, srednia, MRE, sredniaMRE);
                    else
                        doPliku(outputFileName, largeInstancesFiles[i], N, B, wyniki, srednia, MRE, sredniaMRE);
                 
            } catch (IOException e){
                System.out.println("Wystąpił błąd podczas otwarcia pliku " + largeInstancesFiles[i]);
            }
        }
    }

   
    public static void main(String[] args) throws IOException
    {
        /* 
         * ******     TESTY SMALL INSTANCES      ******
        */
        String outputSmallInstances = "small_instances_results.txt";
        File f = new File(outputSmallInstances);
        if(f.exists() && f.isFile()){
            if (!f.delete())
                System.out.println("Błąd: Nie usunięto pliku " + outputSmallInstances + "!");
        }

        // przeprowadź test small instances -->> WARIANT 1.
        //TestSmallInstances(10, outputSmallInstances, false, false);
        // przeprowadz test small instances -->> WARIANT 2.
        //TestSmallInstances(10, outputSmallInstances, true, false);
        // przeprowadź test small instances -->> WARIANT 3.
        //TestSmallInstances(10, outputSmallInstances, false, true);
        // przeprowadź test small instances -->> WARIANT 4.
       //TestSmallInstances(10, outputSmallInstances, true, true);
        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
        /* 
         * ******     TESTY LARGE INSTANCES      ******
        */
        String outputLargeInstances = "large_instances_results.txt";
        f = new File(outputLargeInstances);
        if(f.exists() && f.isFile()){
            if (!f.delete())
                System.out.println("Błąd: Nie usunięto pliku " + outputLargeInstances + "!");
        }

        // przeprowadź test large instances -->> WARIANT 1.
        //TestLargeInstances(10, outputLargeInstances, false, false);
        // przeprowadz test large instances -->> WARIANT 2.
       //TestLargeInstances(10, outputLargeInstances, true, false);
        // przeprowadź test large instances -->> WARIANT 3.
       //TestLargeInstances(10, outputLargeInstances, false, true);
        // przeprowadź test large instances -->> WARIANT 4.
        TestLargeInstances(10, outputLargeInstances, true, true);
    }
}
