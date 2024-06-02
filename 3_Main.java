package hibernate_bp_PrezimeIme_alasNalog_grupa;

import java.util.List;
import java.util.Scanner;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class Main {

	public static void main(String[] args) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction TR = null;

		try (Scanner ulaz = new Scanner(System.in)) {
			TR = session.beginTransaction();
			
			System.out.println("Unesite karakter za pretragu studijskih programa: ");
			String spKarakter = ulaz.next();
			
			String hqlSP = "FROM StudijskiProgram s WHERE LOWER(s.naziv) LIKE :nazivLike";
			Query<StudijskiProgram> studijskiProgramiUpit = session.createQuery(hqlSP, StudijskiProgram.class);
			studijskiProgramiUpit.setParameter("nazivLike", spKarakter + "%");
			List<StudijskiProgram> studijskiProgrami = studijskiProgramiUpit.list();
			
			for (StudijskiProgram sp : studijskiProgrami) {
				System.out.println(sp.getNaziv().trim() + " (" + sp.getId() + ") -- bodovi: " + sp.getObimespb());
				
				String hqlStudenti = 
						"SELECT s.indeks, s.ime, s.prezime, p.poeniSaStudija, p.poeniSaTesta, p.poeniSaStudija + p.poeniSaTesta AS poeniUkupno " +
						"FROM Praksa p INNER JOIN p.student AS s " +
						"WHERE s.idPrograma = :program " +
						"ORDER BY poeniUkupno DESC";
				Query<Object[]> studentiUpit = session.createQuery(hqlStudenti, Object[].class);
				studentiUpit.setParameter("program", sp.getId());
				List<Object[]> studenti = studentiUpit.list();
				
				for (Object[] student : studenti) {
					Integer indeks = (int) student[0];
					String ime = (String) student[1];
					String prezime = (String) student[2];
					Float poeniSaStudija = (float) student[3];
					Float poeniSaTesta = (float) student[4];
					Float poeniUkupno = (float) student[5];
					
					if (poeniSaTesta == -1) {
						Praksa p = session.get(Praksa.class, indeks);
						
						if (p != null) {
							session.delete(p);
						}
						else {
							System.err.println("Postoji problem sa brisanjem podataka o kandidatu sa indeksom " + indeks + ".");
						}
					}
					else {
						System.out.println("\t" + ime + " " + prezime + " " + poeniSaStudija + " " + poeniSaTesta + " " + poeniUkupno);
					}
				}
			}
			
			TR.commit();
		} catch (Exception e) {
			e.printStackTrace();
			if (TR != null) {
				TR.rollback();
			}
		}
		
		session.close();
		HibernateUtil.getSessionFactory().close();
	}
}
