package practica1_xarxes;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;


public class Wget {
	public static void main(String[] args) throws IOException {	
		String nomFitxer = "no_Fitxer";
		boolean z = false, a = false, gz = false, entrar = false;
		
		if(args.length != 0) {
			for(String word: args) {
				if(entrar) {
					entrar = false;
					nomFitxer = word;
				}
				if(word.equals("-f")) {
					entrar = true;
				}else {
					if(word.equals("-a")) {
						a = true;
					}else {
						if(word.equals("-z")) {
							z = true;
						}else {
							if(word.equals("-gz")) {
								gz = true;
							}
						}
					}
				}
			}					
			
			FileReader in = new FileReader(nomFitxer);
		    BufferedReader br = new BufferedReader(in);
		    String line;
		    int index = 0;
		   
		    while ((line = br.readLine()) != null) {
		        //PORTEM FLUX D'ENTRADA DES DEL WEB
		       
		        Flux fl = new Flux(line, z, a, gz, index);
		        fl.start();
		        index++;
		    }
		    in.close();
		}
	}
}