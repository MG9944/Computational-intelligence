import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;


public class LocalSearch {
	static String nazwa_pliku_test = "C:/Users/macie/OneDrive/Pulpit/berlin52.tsp";
	static int N = 0; // liczba miast
	static WspolrzedneMiast[] miasta;
	
	static class WspolrzedneMiast{
		double x;
		double y;
		boolean odwiedzone;
		public WspolrzedneMiast(double x, double y){
			this.x = x;
			this.y = y;
			this.odwiedzone = false;
		}
		public String toString(){
			return "Współrzędne " + x + ", " + y;
		}
	}
	
	static void wczytaj_dane_z_pliku(String nazwa_pliku_test) throws IOException {
		File data2 = new File(nazwa_pliku_test);
		Scanner s = new Scanner(data2);
		// trzy linie nieistotne
		s.nextLine();
		s.nextLine();
		s.nextLine();
		// czwarta-liczba miast
		N = Integer.parseInt(s.nextLine().split(" ")[1]);
		// kolejne dwie linie nieistotne
		s.nextLine();
		s.nextLine();
		System.out.println("Punkty: " + N);
		miasta = new WspolrzedneMiast[N];
		int i = 0;
		while(i < N){
			String line = s.nextLine();
			String[] vals = line.split(" ");
			double x = Double.parseDouble(vals[1]);
			double y = Double.parseDouble(vals[2]);
			WspolrzedneMiast c = new WspolrzedneMiast(x, y);
			miasta[i++] = c;
		}
		for(i = 0; i < miasta.length; i++){
			System.out.println(i+": "+miasta[i]);
		}
	}
	// odległość między współrzędnymi miast
	static double odleglosc(WspolrzedneMiast m1, WspolrzedneMiast m2){
		return (Math.sqrt((m2.x-m1.x)*(m2.x-m1.x)+(m2.y-m1.y)*(m2.y-m1.y)));
	}
	
	static int[] TSP_NearestNeighbour(){
		int[] tr = new int[N];
		try {
			wczytaj_dane_z_pliku(nazwa_pliku_test);
		}
		catch(IOException e) {
			
		}
		int aktualne_miasto = 0;
		tr[0] = aktualne_miasto;
		miasta[tr[0]].odwiedzone = true;
		// znajdz najblizszego sąsiada spośród jeszcze nieodwiedzonych
		for(int j = 1; j < N; j++){
			double odl = 10000;
			int m = -1;
			for(int k = 1; k < N; k++){
				if(!miasta[k].odwiedzone){
					// szukam sąsiada
					double d = odleglosc(miasta[aktualne_miasto], miasta[k]);
					if(d < odl){
						odl = d;
						m = k;
					}
				}
			}
			tr[j] = m;
			miasta[tr[j]].odwiedzone = true;
			aktualne_miasto = m;
		}
		return tr;
	}
	
	static int[] MiastaPoKolei(){
		int[] trasa = new int[N];
		// wybierz rozwiązanie początkowe - po kolei
		for(int i = 0; i < miasta.length; i++){
			trasa[i] = i;
		}
		return trasa;
	}
	
	static int[] MiastaLosowe(){
		// stwórz losowo wybraną permutację miast
		int[] trasa = new int[N];
		ArrayList<Integer> ListaMiast = new ArrayList<Integer>();
		Random los = new Random();
		for(int i = 0; i < miasta.length; i++){
			int losoweMiasto = los.nextInt(miasta.length);
			// jeśli dana liczba nie wystąpiła, dodaj do tablicy
			while(ListaMiast.indexOf(losoweMiasto) != -1){
				losoweMiasto = los.nextInt(miasta.length);
			}
			ListaMiast.add(losoweMiasto);
			trasa[i] = losoweMiasto;
		}
		return trasa;
	}
	
	static int[] wybierz_rozwiazanie_z_otoczenia(int[] vc){
		int[] trasa = new int[N];
		for(int i = 0; i < vc.length; i++){
			trasa[i] = vc[i];
		}
		//dlugosc trasy oryginalnej
		double dlugosc_vc = dlugosc_trasy(vc);
		for(int k = 0; k < N; k++){
			Random los = new Random();
			int los1 = 1+los.nextInt(N-1);
			int los2 = 1+los.nextInt(N-1);
			// zamien miejscami miejsca z pozycji los1 i los2
			int pom = trasa[los1];
			trasa[los1] = trasa[los2];
			trasa[los2] = pom;
			double dlugosc_tr = dlugosc_trasy(trasa);	
			if(dlugosc_tr >= dlugosc_vc){
				// rozwiazanie nie jest lepsze, odtworz trase
				pom = trasa[los1];
				trasa[los1] = trasa[los2];
				trasa[los2] = pom;
			}
			else{
				// pierwsze lepsze
				break;
			}
		}
		return trasa;
	}
	
	static int[] wybierz_rozwiazanie_poczatkowe(){
		//return MiastaPoKolei();
		//return MiastaLosowe();
		return TSP_NearestNeighbour();
	}

	static int[] LocalSearch_1(){
		boolean local = false; // pierwsze uzyskane rozwiązanie lepsze od vc - true; 
							  //najlepsze rozwiązanie spośród wszystkich sąsiadów vc - false;
		// wybieram rozwiazanie poczatkowe
		int[] vc = wybierz_rozwiazanie_poczatkowe();
		// ocena vc
		double dlugosc_vc = dlugosc_trasy(vc);
		System.out.println("Długość trasy początkowej: " + dlugosc_vc);
		do{
			int[] vn = wybierz_rozwiazanie_z_otoczenia(vc);
			// ocena vn
			double dlugosc_vn = dlugosc_trasy(vn);
			if(dlugosc_vn < dlugosc_vc){
				// nowa trasa lepsza
				vc = vn;
				dlugosc_vc = dlugosc_vn;
				System.out.println("Długość nowej trasy: " + dlugosc_vc);
			}
			else{
				local = true;
			}
		} while(!local);
		return vc;
	}

	static int[] LocalSearch_2() {
	int t = 0;
	int MAX = 10;
	int[] best;
		do {
			boolean local = true; // pierwsze uzyskane rozwiązanie lepsze od vc - true; 
								 //najlepsze rozwiązanie spośród wszystkich sąsiadów vc - false;
			// wybieram rozwiazanie poczatkowe
			int[] vc = wybierz_rozwiazanie_poczatkowe();
			// ocena vc
			double dlugosc_vc = dlugosc_trasy(vc);
			best = vc;
			System.out.println("Długość trasy początkowej: " + dlugosc_vc);
			do {
				int[] vn = wybierz_rozwiazanie_z_otoczenia(vc);
				// ocena vn
				double dlugosc_vn = dlugosc_trasy(vn);
				if(dlugosc_vn < dlugosc_vc){
					// nowa trasa lepsza
					vc = vn;
					dlugosc_vc = dlugosc_vn;
					System.out.println("Długość nowej trasy: " + dlugosc_vc);
				}
				else{
					local = true;
				}
			} while(!local);
			t++;
			double dlugosc_best = dlugosc_trasy(best);
			if(dlugosc_best < dlugosc_vc) {
				best = vc;
			}
		}while(t < MAX);
		return best;
	}
	
	static void wyswietl_trase(int[] trasa){
		for(int i = 0; i < trasa.length; i++){
			System.out.print(trasa[i] + " -> ");
		}
		System.out.println(trasa[0]);
	}
	
	static double dlugosc_trasy(int[] trasa){
		double dlugosc = 0.0;
		for(int i = 0; i < trasa.length-1; i++){
			dlugosc += odleglosc(miasta[trasa[i]], miasta[trasa[i+1]]);
		}
		dlugosc += odleglosc(miasta[trasa[trasa.length-1]], miasta[trasa[0]]);
		return dlugosc;
	}
	public static void main(String[] args) throws IOException {
			wczytaj_dane_z_pliku(nazwa_pliku_test);
			int[] trasa = LocalSearch_2();
			System.out.println("Trasa: ");
			System.out.println();
			wyswietl_trase(trasa);
			System.out.println("Długość trasy: " + dlugosc_trasy(trasa));
	}
}
