

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


public class TabuSearchV1 {

	   static String nazwa_pliku_test = "assign500.txt";
	   static int M; // liczba pracowników i zadań
		static int[][] c; // koszt przypisania pracowników do zadań
		static ArrayList<Ruch> listaTabu; // lista tabu

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
	            //wiersze
	            for(int i = 0; i < M; i++){
	                int s = 0;
	                for(int j = 0; j < M; j++){
	                    s += z[i][j];
	                }
	                if(s != 1) return false;
	            }
	            //kolumny
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

	  //losowy pracownik i zadania
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
		Random random = new Random();
		int losPierwszy = random.nextInt(M);
		int losDrugi = random.nextInt(M);
		Ruch aktualnyRuch;
		int numerZadania1 = 0, numerZadania2 = 0;
		do{
			losPierwszy = random.nextInt(M);
			losDrugi = random.nextInt(M);
			while(losPierwszy == losDrugi){
				losPierwszy = random.nextInt(M);
				losDrugi = random.nextInt(M);
			}
			for(int i=0; i < M ;i++) {
				if (kopia.z[losPierwszy][i] == 1) {
					numerZadania1 = i;
				}
				if (kopia.z[losDrugi][i] == 1) {
					numerZadania2 = i;
				}
			}
			aktualnyRuch = new Ruch(losPierwszy, losDrugi, numerZadania1, numerZadania2);
		} while(jestTabu(aktualnyRuch));
		if(!jestTabu(aktualnyRuch)){
			for(int i = 0; i < M; i++){
				int pom = kopia.z[losPierwszy][i];
				kopia.z[losPierwszy][i] = kopia.z[losDrugi][i];
				kopia.z[losDrugi][i] = pom;
			}
			listaTabu.add(aktualnyRuch); // dodaj na liste tabu
		}
		return kopia;
	}


	public static RozwiazanieAlgorytmu TS(int maxIter, int dlTabu, int dlKadencji, int max_wewn_iter){
		listaTabu = new ArrayList<>();
		int obecnaKad = 0;
		RozwiazanieAlgorytmu vc = UtworzLosoweRozwiazanie();
		vc.ObliczFc();
		int Vc = vc.fc;
		RozwiazanieAlgorytmu best = vc;
		int Best = vc.fc;
		for(int i = 0; i < maxIter; i++){
			for(int j = 0; j < max_wewn_iter; j++){
				RozwiazanieAlgorytmu vn;
				do {
					vn = WybierzRozwiazanieZOtoczenia(vc);
				} while(!vn.CzyPoprawne());
				vn.ObliczFc();
				int Vn = vn.fc;
				if(Vn < Vc){
					vc = vn;
					Vc = vn.fc;
				}
				if(Vn < Best){
					best = vn;
					Best = vn.fc;
				}
				obecnaKad++;
				if(obecnaKad >= dlKadencji){
					listaTabu.remove(0);
					obecnaKad=0;
				}
				if(listaTabu.size() >= dlTabu){
					listaTabu.remove(0);
				}
			}
		}
		return best;
	}


	static class Ruch {
		public int prac_1;
		public int prac_2;
		public int zad_1;
		public int zad_2;

		public Ruch(int prac_1, int prac_2, int zad_1, int zad_2) {
			this.prac_1 = prac_1;
			this.prac_2 = prac_2;
			this.zad_1 = zad_1;
			this.zad_2 = zad_2;
		}
	}

	
	static boolean jestTabu(Ruch rv){
		for(int i = 0; i < listaTabu.size(); i++){
			Ruch r = listaTabu.get(i);
			if((r.prac_1 == rv.prac_1) &&
				(r.zad_1 == rv.zad_1) &&
				(r.prac_2 == rv.prac_2) &&
				(r.zad_2 == rv.zad_2)){
				return true;
			}

			if((r.prac_1 == rv.prac_2) &&
				(r.zad_1 == rv.zad_2) &&
				(r.prac_2 == rv.prac_1) &&
				(r.zad_2 == rv.zad_1)){
				return true;
			}
		}
		return false;
	}

	public static void wypiszTablice(int[][] tablica){
		for(int i = 0; i < M; i++){
			for(int j = 0; j < M; j++){
				System.out.println(tablica[i][j]);
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

	public static void wyswietlParametry(int maxIter, int dlTabu, int dlKadencji){
		System.out.println("Zadania i pracownicy: " + M);
		System.out.println("Liczba iteracji: " + maxIter);
		System.out.println("Długość listy tabu: " + dlTabu);
		System.out.println("Długość kadencji: " + dlKadencji);
	}

	public static void wykonajEksperymenty(int maxIter, int dlTabu, int dlKadencji, int liczbaEksperymentow){
		wyswietlParametry(maxIter, dlTabu, dlKadencji);
		System.out.println("Liczba eksperymentów: " + liczbaEksperymentow);
		int sum = 0;
		for(int i = 0; i < liczbaEksperymentow; i++){
			RozwiazanieAlgorytmu rozwiazanie = TS(maxIter, dlTabu, dlKadencji, 10);
			sum += rozwiazanie.fc;
		}
		double srednia = (double) sum / liczbaEksperymentow;
		System.out.println("Średnia wartości funkcji celu: " + srednia);
	}

	public static void main(String[] args) throws IOException {
			WczytajDaneZPliku(nazwa_pliku_test);
			System.out.println("----------------- Zadanie 1. -----------------");
			RozwiazanieAlgorytmu roz = TS(1000, 50, 50, 10);
			wyswietlParametry(1000, 50, 50);
			System.out.println("Wartość funkcji celu: " + roz.fc);
			System.out.println("----------------------------------------------");

			System.out.println("\n----------------- Zadanie 2. -----------------");
			wykonajEksperymenty(1000, 50, 50, 10);
			System.out.println("----------------------------------------------");

			System.out.println("\n----------------- Zadanie 3. -----------------");
			wykonajEksperymenty(1000, 5000, 5000, 10);
			System.out.println();
			wykonajEksperymenty(1000, 2000, 2000, 10);
			System.out.println();
			wykonajEksperymenty(1000, 200, 200, 10);
			System.out.println();
			wykonajEksperymenty(1000, 1000, 200, 10);
			System.out.println();
			wykonajEksperymenty(1000, 200, 1000, 10);
			System.out.println("----------------------------------------------");

			System.out.println("\n----------------- Zadanie 4. -----------------");
			// podpunkt a.
			wykonajEksperymenty(1000, 5000, 5000, 10);
			System.out.println();
			wykonajEksperymenty(2000, 5000, 5000, 10);
			System.out.println();
			wykonajEksperymenty(5000, 5000, 5000, 10);

			// podpunkt b.
			System.out.println();
			wykonajEksperymenty(1000, 2000, 2000, 10);
			System.out.println();
			wykonajEksperymenty(2000, 2000, 2000, 10);
			System.out.println();
			wykonajEksperymenty(5000, 2000, 2000, 10);

			// podpunkt c.
			System.out.println();
			wykonajEksperymenty(1000, 200, 200, 10);
			System.out.println();
			wykonajEksperymenty(2000, 200, 200, 10);
			System.out.println();
			wykonajEksperymenty(5000, 200, 200, 10);

			// podpunkt d.
			System.out.println();
			wykonajEksperymenty(1000, 1000, 200, 10);
			System.out.println();
			wykonajEksperymenty(2000, 1000, 200, 10);
			System.out.println();
			wykonajEksperymenty(5000, 1000, 200, 10);
			System.out.println("----------------------------------------------");
		} 
	}



