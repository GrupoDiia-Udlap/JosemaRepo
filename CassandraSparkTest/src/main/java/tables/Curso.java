package tables;

import com.github.javafaker.Faker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Thagus on 18/06/17.
 */
public class Curso {
    private long id_curso;
    private String titulo;
    private String codigo;
    private String periodo; //Primavera 2017
    private int seccion;

    private Docente docente;
    private Escuela escuela;

    private String id_facebook;
    private String id_crea;

    private static String[] estaciones = {"Otoño", "Primavera"};

    public Curso(long id_curso, Docente docente, Escuela escuela) {
        Faker faker = new Faker(new Locale("es"));
        Random random = new Random();

        this.id_curso = id_curso;
        this.docente = docente;
        this.escuela = escuela;

        this.titulo = faker.lorem().characters(5, 20);
        this.codigo = faker.idNumber().valid();

        Date date = faker.date().between(new Date(946706400000L), new Date(1514786400000L));

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String year = String.valueOf(calendar.get(Calendar.YEAR));

        this.periodo = estaciones[random.nextInt(estaciones.length)] + " " + year;

        this.seccion = random.nextInt(30)+1;

        this.id_facebook = String.valueOf(faker.number().randomNumber(14, true));
        this.id_crea = String.valueOf(faker.number().randomNumber(10, true));
    }

    public Date getRandomDateInRange() {
        Faker faker = new Faker(new Locale("es"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String[] periodo = getPeriodo().split("\\s");

        Date randomDate = null;

        try {
            if (periodo[0].equals("Otoño")) {
                randomDate = faker.date().between(sdf.parse(periodo[1] + "-08-15"), sdf.parse(periodo[1] + "-12-01"));
            } else if (periodo[0].equals("Primavera")) {
                randomDate = faker.date().between(sdf.parse(periodo[1] + "-01-15"), sdf.parse(periodo[1] + "-06-01"));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return randomDate;
    }

    public long getId_curso() {
        return id_curso;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getPeriodo() {
        return periodo;
    }

    public int getSeccion() {
        return seccion;
    }

    public Docente getDocente() {
        return docente;
    }

    public Escuela getEscuela() {
        return escuela;
    }

    public String getId_facebook() {
        return id_facebook;
    }

    public String getId_crea() {
        return id_crea;
    }
}
