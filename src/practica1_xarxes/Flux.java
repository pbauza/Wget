package practica1_xarxes;

import java.io.IOException;
import java.net.URL;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.Thread;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Flux extends Thread {

	private boolean m_z = false, m_a = false, m_gz = false;
	private int m_index, m_CasArgs = 0;
	private String m_u;

	Flux(String u, boolean z, boolean a, boolean gz, int index) {
		m_u = u;
		m_z = z;
		m_a = a;
		m_gz = gz;
		m_index = index;

		if (m_z)
			m_CasArgs++;

		if (m_a)
			m_CasArgs++;

		if (m_gz)
			m_CasArgs++;

	}

	public void run() {
		try {
			URL u;
			u = new URL(m_u);
			System.out.println(m_u);
			InputStream is;
			is = u.openStream();
			byte[] buff = new byte[1];

			String nomFitxer = m_u.substring(m_u.lastIndexOf("/") + 1);
			String extensio = nomFitxer.substring(nomFitxer.lastIndexOf(".") + 1);

			int punts = 0;

			//------------------------------------------------------------------------------
			// MIREM LES EXTENSIONS

			for (int p = 0; p < nomFitxer.length(); p++) {
				if (nomFitxer.charAt(p) == '.') {
					punts++;
				}
			}
			if (punts != 1) {
				extensio = ".html";
				nomFitxer = "index" + m_index + extensio;
			}

			if (extensio.equals("php")) {
				extensio = ".html";
				nomFitxer = "index" + m_index + extensio;
			}

			if (m_a) {
				if (extensio.equals(".html") || extensio.equals(".txt")) {
					nomFitxer = nomFitxer + ".asc";
				}
			}

			//-------------------------------------------------------------------------------

			switch (m_CasArgs) {

			case 0: // CASO SIN ARGUMENTOS
				OutputStream out = new FileOutputStream(nomFitxer);
				while (is.read(buff) != -1) {
					out.write(buff);
				}
				out.close();

				break;
			case 1:

				if (m_z) // COMPRESSIO ZIP
				{
					ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(nomFitxer + ".zip"));
					ZipEntry e = new ZipEntry(nomFitxer);
					zipout.putNextEntry(e);

					while (is.read(buff) != -1) {
						zipout.write(buff);
					}

					zipout.closeEntry();
					zipout.close();

				} else if (m_gz) { // COMPRESSIO GZ
					GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(nomFitxer + ".gz"));

					while (is.read(buff) != -1) {
						gos.write(buff);
					}

					gos.close();

				} else if (m_a) { // COMPRESSIO ASCII
					ToAscii(nomFitxer, u, extensio);
				}

				break;
			case 2:

				if (m_z && m_gz) // ZIP + GZIP
				{
					File tempZipFile = File.createTempFile(nomFitxer, ".zip");
					ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(tempZipFile));
					ZipEntry e = new ZipEntry(nomFitxer);
					zipout.putNextEntry(e);

					while (is.read(buff) != -1) {
						zipout.write(buff);

					}

					zipout.closeEntry();
					zipout.close();

					GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(nomFitxer + ".zip.gz"));

					FileInputStream zipin = new FileInputStream(tempZipFile);

					while (zipin.read(buff) != -1) {
						gos.write(buff);
					}
					// gos.finish();

					zipin.close();
					gos.close();

				} else {
					if (m_a && m_z) { // ZIP + ASCII
						ToAsciiZip(nomFitxer, u, extensio);
					} else {
						if (m_a && m_gz) {
							ToAsciiGZ(nomFitxer, u, extensio);
						}
					}
				}
				break;
			case 3: // ASC + ZIP + GZ
				ToAsciiZIPGZ(nomFitxer, u, extensio);
				break;
			default:
				break;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void ToAsciiZIPGZ(String nomFitxer, URL u, String extensio) {

		try {
			InputStream is;
			is = u.openStream();
			File tempZipFile = File.createTempFile(nomFitxer, ".zip");
			ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(tempZipFile));
			ZipEntry e = new ZipEntry(nomFitxer);
			zipout.putNextEntry(e);
			byte[] buff = new byte[1];

			if (extensio.equals(".html") || extensio.equals(".txt")) {
				Html2AsciiInputStream asc = new Html2AsciiInputStream(is);
				int inAsc = asc.read();
				boolean noTag = false; // controla si es un <(nombre)
				boolean Tag = false; // controla si hi ha una tag
				while (inAsc != -1) {
					if (inAsc == -2) { // llegeix un <
						inAsc = asc.read();

						if (inAsc > 48 && inAsc < 57) {
							noTag = true;
						}
						if (noTag) {
							zipout.write(60); // escrivim el <
							zipout.write(inAsc); // escrivim el nombre
							noTag = false;
						} else {
							Tag = true;
						}
					} else {
						if (Tag) {
							if (inAsc == -3) {
								zipout.write(92);
								zipout.write(110);
								Tag = false;
							}
						} else {
							if (inAsc != -3) {
								zipout.write(inAsc);
							} else {
								zipout.write(62);
							}
						}
					}
					inAsc = asc.read();
				}
				asc.close();
			} else {
				while (is.read(buff) != -1) {
					zipout.write(buff);
				}
			}

			zipout.closeEntry();
			zipout.close();

			GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(nomFitxer + ".zip.gz"));

			FileInputStream zipin = new FileInputStream(tempZipFile);

			while (zipin.read(buff) != -1) {
				gos.write(buff);
			}
			// gos.finish();

			zipin.close();
			gos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void ToAsciiGZ(String nomFitxer, URL u, String extensio) {
		try {
			InputStream is;
			is = u.openStream();
			GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(nomFitxer + ".gz"));

			if (extensio.equals(".html") || extensio.equals(".txt")) {
				Html2AsciiInputStream asc = new Html2AsciiInputStream(is);
				int inAsc = asc.read();
				boolean noTag = false; // controla si es un <(nombre)
				boolean Tag = false; // controla si hi ha una tag
				while (inAsc != -1) {
					if (inAsc == -2) { // llegeix un <
						inAsc = asc.read();

						if (inAsc > 48 && inAsc < 57) {
							noTag = true;
						}
						if (noTag) {
							gos.write(60); // escrivim el <
							gos.write(inAsc); // escrivim el nombre
							noTag = false;
						} else {
							Tag = true;
						}
					} else {
						if (Tag) {
							if (inAsc == -3) {
								gos.write(92);
								gos.write(110);
								Tag = false;
							}
						} else {
							if (inAsc != -3) {
								gos.write(inAsc);
							} else {
								gos.write(62);
							}
						}
					}
					inAsc = asc.read();
				}
				asc.close();
				gos.close();
			} else {
				byte[] buff = new byte[1];
				while (is.read(buff) != -1) {
					gos.write(buff);
				}
				gos.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void ToAsciiZip(String nomFitxer, URL u, String extensio) {
		try {
			InputStream is;
			is = u.openStream();
			ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(nomFitxer + ".zip"));
			ZipEntry e = new ZipEntry(nomFitxer);
			zipout.putNextEntry(e);

			if (extensio.equals(".html") || extensio.equals(".txt")) {
				Html2AsciiInputStream asc = new Html2AsciiInputStream(is);
				int inAsc = asc.read();
				boolean noTag = false; // controla si es un <(nombre)
				boolean Tag = false; // controla si hi ha una tag
				while (inAsc != -1) {
					if (inAsc == -2) { // llegeix un <
						inAsc = asc.read();

						if (inAsc > 48 && inAsc < 57) {
							noTag = true;
						}
						if (noTag) {
							zipout.write(60); // escrivim el <
							zipout.write(inAsc); // escrivim el nombre
							noTag = false;
						} else {
							Tag = true;
						}
					} else {
						if (Tag) {
							if (inAsc == -3) {
								zipout.write(92);
								zipout.write(110);
								Tag = false;
							}
						} else {
							if (inAsc != -3) {
								zipout.write(inAsc);
							} else {
								zipout.write(62);
							}
						}
					}
					inAsc = asc.read();
				}
				asc.close();

				zipout.closeEntry();
				zipout.close();
			} else {
				byte[] buff = new byte[1];
				while (is.read(buff) != -1) {
					zipout.write(buff);
				}
				zipout.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void ToAscii(String nomFitxer, URL u, String extensio) {
		try {
			InputStream is;
			is = u.openStream();
			OutputStream out;
			out = new FileOutputStream(nomFitxer);

			if (extensio.equals(".html") || extensio.equals(".txt")) {
				Html2AsciiInputStream asc = new Html2AsciiInputStream(is);
				int inAsc = asc.read();
				boolean noTag = false; // controla si es un <(nombre)
				boolean Tag = false; // controla si hi ha una tag
				while (inAsc != -1) {
					if (inAsc == -2) { // llegeix un <
						inAsc = asc.read();

						if (inAsc > 48 && inAsc < 57) {
							noTag = true;
						}
						if (noTag) {
							out.write(60); // escrivim el <
							out.write(inAsc); // escrivim el nombre
							noTag = false;
						} else {
							Tag = true;
						}
					} else {
						if (Tag) {
							if (inAsc == -3) {
								out.write(92);
								out.write(110);
								Tag = false;
							}
						} else {
							if (inAsc != -3) {
								out.write(inAsc);
							} else {
								out.write(62);
							}
						}
					}
					inAsc = asc.read();
				}
				asc.close();
				out.close();
			} else {
				byte[] buff = new byte[1];
				while (is.read(buff) != -1) {
					out.write(buff);
				}
				out.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}