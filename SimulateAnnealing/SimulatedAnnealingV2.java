import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

//Dodany wlasny operator:regresji
public class SimulatedAnnealingV2 {
    static String nazwa_pliku_test = "assign500.txt";
    static int M; // liczba pracowników i zadań
    static int[][] c; // koszt przypisania pracowników do zadań
    static int MAX_ITER = 300;
    static int n = 10;
    static int t = 0;
    static double T = 100;
    static double alfa = 0.95;

    public static void WczytajDaneZPliku(String nazwa_pliku) throws IOException {
        BufferedReader fileReader = null;
        try{
            fileReader = new BufferedReader(new FileReader(nazwa_pliku));
            String pierwszaLinia = fileReader.readLine().trim();
            M = Integer.parseInt(pierwszaLinia); // liczba pracowników i zadań
            c = new int[M][M];
            int wiersz = 0;
            int kolumna = 0;
            String kolejnaLinia = fileReader.readLine();
            while(kolejnaLinia != null){
                kolejnaLinia = kolejnaLinia.trim();
                String[] kolejnaLiniaPodzielona = kolejnaLinia.split(" ");
                int ileElementowWWierszu = kolejnaLiniaPodzielona.length;
                int ktoryElementWWierszu = 0;
                while(ktoryElementWWierszu < ileElementowWWierszu){
                    String odczytanyKoszt = kolejnaLiniaPodzielona[ktoryElementWWierszu];
                    c[wiersz][kolumna] = Integer.parseInt(odczytanyKoszt);
                    if(kolumna < M-1){
                        kolumna++;
                    }
                    else{
                        wiersz++;
                        kolumna = 0;
                    }
                    ktoryElementWWierszu++;
                }
                kolejnaLinia = fileReader.readLine();
            }
        }
        finally{
            if(fileReader != null) fileReader.close();
        }
    }

    public static class RozwiazanieAlgorytmu {
        int fc;
        int[][] z;
        RozwiazanieAlgorytmu(){
            z = new int[M][M];
            for(int i = 0; i < M; i++){
                for(int j = 0; j < M; j++){
                    z[i][j] = 0;
                }
            }
            fc = 0;
        }

        RozwiazanieAlgorytmu(RozwiazanieAlgorytmu v){
            z = new int[M][M];
            for(int i = 0; i < M; i++){
                for(int j = 0; j < M; j++){
                    z[i][j] = v.z[i][j];
                }
            }
            fc = v.fc;
        }

        void ObliczFc(){
            fc = 0;
            for(int i = 0; i < M; i++){
                for(int j = 0; j < M; j++){
                    fc += c[i][j] * z[i][j];
                }
            }
        }

        boolean CzyPoprawne(){
            // sprawdzam wiersze
            for(int i = 0; i < M; i++){
                int s = 0;
                for(int j = 0; j < M; j++){
                    s += z[i][j];
                }
                if(s != 1) return false;
            }
            // sprawdzam kolumny
            for(int j = 0; j < M; j++){
                int s = 0;
                for(int i = 0; i < M; i++){
                    s += z[i][j];
                }
                if(s != 1) return false;
            }
            return true;
        }
    }

    // vc losowe pracownik i zadania
    static RozwiazanieAlgorytmu UtworzLosoweRozwiazanie(){
        ArrayList<Integer> lista = new ArrayList<Integer>(M);
        for(int j = 0; j < M; j++){
            lista.add(j);
        }
        RozwiazanieAlgorytmu v = new RozwiazanieAlgorytmu();
        Random random = new Random();
        for(int i = 0; i < M; i++){
            int losowa = random.nextInt(lista.size());
            v.z[i][lista.get(losowa)] = 1;
            lista.remove(losowa);
        }
        v.ObliczFc();
        return v;
    }

    static RozwiazanieAlgorytmu WybierzRozwiazanieZOtoczenia(RozwiazanieAlgorytmu rozwiazanie){
        RozwiazanieAlgorytmu kopia = new RozwiazanieAlgorytmu(rozwiazanie);
        Random losZadania = new Random();
        int losPierwszy = losZadania.nextInt(M);
        int losDrugi = losZadania.nextInt(M);
        while(losPierwszy == losDrugi && losPierwszy<= losDrugi){
            losPierwszy = losZadania.nextInt(M);
            losDrugi = losZadania.nextInt(M);
        }
        int lZamian = (int)Math.floor((losDrugi - losPierwszy) / 2);
        //zamiana zadan
        for( int k = 0; k <= lZamian; k++) {
            for (int i = 0; i < M; i++) {
                int pom = kopia.z[losPierwszy][i];
                kopia.z[losPierwszy][i] = kopia.z[losDrugi][i];
                kopia.z[losDrugi][i] = pom;
            }
            losPierwszy++;
            losDrugi--;
        }
        return kopia;
    }

    static RozwiazanieAlgorytmu SA() {
        RozwiazanieAlgorytmu vc = UtworzLosoweRozwiazanie();
        int Vc = vc.fc;
        for(int i = 0; i < MAX_ITER; i++){
            for(int j = 0; j < n; j++){
                RozwiazanieAlgorytmu vn;
                do{
                    vn = WybierzRozwiazanieZOtoczenia(vc);
                }while(!vn.CzyPoprawne());
                vn.ObliczFc();
                int Vn = vn.fc;
                if(Vn < Vc){
                    Vc = Vn;
                    vc = vn;
                }
                else{
                    Random los = new Random();
                    double losowa = los.nextDouble();
                    double e = Math.pow(Math.exp(1), (Vc - Vn)/T);
                    if(losowa < e){
                        Vc = Vn;
                        vc = vn;
                    }
                }
            }
            T = T * alfa;
            t++;
        }
        return vc;
    }

    public static void WypiszTablice(int[][] tablica){
        for(int i = 0; i < M; i++){
            for(int j = 0; j < M; j++){
                System.out.printf("%4d", tablica[i][j]);
            }
            System.out.println();
        }
    }

    public static void WypiszRozwiazanie(RozwiazanieAlgorytmu tablica){
        for(int i = 0; i < M; i++){
            System.out.println(i+1);
            for(int j = 0; j < M; j++){
                System.out.println(tablica.z[i][j]);
            }
            System.out.println();
        }
    }

    public static void ZmianaPoczatkowychParametrow(int maxIter, int N, int t, double Alfa){
        MAX_ITER = maxIter;
        n = N;
        T = t;
        alfa = Alfa;
    }

    public static void WyswietlParametry(){
        System.out.println("Zadań i pracowników: " + M);
        System.out.println("MAX_ITER: " + MAX_ITER);
        System.out.println("N: " + n);
        System.out.println("T: " + T);
        System.out.println("alfa: " + alfa);
    }
    //PODSTAWOWE

    public static void PierwszyTest(){
        ZmianaPoczatkowychParametrow(300, 10, 100, 0.95);
        WyswietlParametry();
        int lEks = 10;
        int sFCEks = 0;
        double srFCEks = 0.0;
        for(int i = 1; i <= lEks; i++){
            RozwiazanieAlgorytmu Podstawowy1 = SA();
            sFCEks += Podstawowy1.fc;
            System.out.println(Podstawowy1.fc);
        }
        srFCEks = sFCEks / lEks;
        System.out.println("Średnia: " + srFCEks);
    }
    //ROZSZERZONE

    public static void TestyAlfa(double[] alfa, int t){
        ZmianaPoczatkowychParametrow(300, 10, t, alfa[0]);
        WyswietlParametry();
        for(int i = 0; i < alfa.length; i++){
            System.out.println("Alfa: " + alfa[i]);
            int lEks = 10;
            int sFCEks = 0;
            double srFCEks = 0.0;
            for(int j = 1; j <= lEks; j++){
                ZmianaPoczatkowychParametrow(300, 10, t, alfa[i]);
                RozwiazanieAlgorytmu rozwiazanie = SA();
                System.out.println(rozwiazanie.fc);
                sFCEks += rozwiazanie.fc;
            }
            srFCEks = sFCEks / lEks;
            System.out.println("Średnia: " + srFCEks);
        }
    }

    public static void TestyMAX_ITER(int[] MAX_ITER){
        ZmianaPoczatkowychParametrow(MAX_ITER[0], 10, 100, 0.95);
        WyswietlParametry();
        for(int i = 0; i < MAX_ITER.length; i++){
            System.out.println("MAX_ITER: " + MAX_ITER[i]);
            int lEks = 10;
            int sFCEks = 0;
            double srFCEks = 0.0;
            for(int j = 1; j <= lEks; j++){
                ZmianaPoczatkowychParametrow(MAX_ITER[i], 10, 100, 0.95);
                RozwiazanieAlgorytmu rozwiazanie = SA();
                System.out.println(rozwiazanie.fc);
                sFCEks += rozwiazanie.fc;
            }
            srFCEks = sFCEks / lEks;
            System.out.println("Średnia: " + srFCEks);
        }
    }

    public static void TestyN(int[] N){
        ZmianaPoczatkowychParametrow(300, N[0], 100, 0.95);
        WyswietlParametry();
        for(int i = 0; i < N.length; i++){
            System.out.println("N: " + N[i]);
            int lEks = 10;
            int sFCEks = 0;
            double srFCEks = 0.0;
            for(int j = 1; j <= lEks; j++){
                ZmianaPoczatkowychParametrow(300, N[i], 100, 0.95);
                RozwiazanieAlgorytmu rozwiazanie = SA();
                System.out.println(rozwiazanie.fc);
                sFCEks += rozwiazanie.fc;
            }
            srFCEks = sFCEks / lEks;
            System.out.println("Średnia: " + srFCEks);
        }
    }

    public static void main(String[] args) throws IOException {
        WczytajDaneZPliku(nazwa_pliku_test);
        //PODSTAWOWY
        System.out.println("PODSTAWOWY");
        PierwszyTest();
        System.out.println("****************************************");

        //ROZSZERZONE
        System.out.println("a)Testy dla parametrow alfa: 0.99, 0.95, 0.8, 0.5, 0.2");
        double[] alfa = {0.99, 0.95, 0.8, 0.5, 0.2};
        int t = 100;
        TestyAlfa(alfa, t);
        System.out.println("*****************************************");
        System.out.println("b)Testy dla parametrow alfa: 0.99, 0.95, 0.8, 0.5, 0.2 oraz T = 1000");
        t = 1000;
        TestyAlfa(alfa, t);
        System.out.println("******************************************");
        System.out.println("c)Testy dla parametrow MAX_ITER: 100, 300, 500, 800, 1000");
        int[] MAX_ITER = {100, 300, 500, 800, 1000};
        TestyMAX_ITER(MAX_ITER);
        System.out.println("*******************************************");
        System.out.println("d)Testy dla parametrow N: 10, 50, 100, 200, 500");
        int[] N = {10, 50, 100, 200, 500};
        TestyN(N);
        System.out.println("********************************************");
        System.out.println("e)Optymalne wartości dające najlepszą funkcję celu");
        ZmianaPoczatkowychParametrow(650, 725, 1000, 0.95);
        WyswietlParametry();
        int lEks = 10;
        int sFCEks = 0;
        double srFCEks = 0.0;
        for(int j = 1; j <= lEks; j++){
            ZmianaPoczatkowychParametrow(650, 725, 1000, 0.95);
            RozwiazanieAlgorytmu optymalne = SA();
            System.out.println(optymalne.fc);
            sFCEks += optymalne.fc;
        }
        srFCEks = sFCEks / lEks;
        System.out.println("Średnia: " + srFCEks);
    }
}

