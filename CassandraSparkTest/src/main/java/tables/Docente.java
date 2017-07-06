package tables;

import com.github.javafaker.Faker;

import java.sql.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Thagus on 18/06/17.
 */
public class Docente {
    private long id_docente;
    private String nombre;
    private String apellidos;
    private char sexo;
    private String estado_civil;
    private Date fecha_nacimiento;

    private String id_facebook;
    private String id_crea;

    public Docente(long id_docente) {
        Faker faker = new Faker(new Locale("es"));

        this.id_docente = id_docente;

        this.nombre = faker.name().firstName();
        this.apellidos = faker.name().lastName();
        this.sexo = faker.demographic().sex().charAt(0);

        this.estado_civil = faker.demographic().maritalStatus();

        //Docentes de al menos 22 años
        this.fecha_nacimiento = new Date(faker.date().past(50*360, TimeUnit.DAYS, new Date(System.currentTimeMillis()-22*31556926000L)).getTime());

        this.id_facebook = String.valueOf(faker.number().randomNumber(14, true));
        this.id_crea = String.valueOf(faker.number().randomNumber(10, true));
    }

    public long getId_docente() {
        return id_docente;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public char getSexo() {
        return sexo;
    }

    public String getEstado_civil() {
        return estado_civil;
    }

    public Date getFecha_nacimiento() {
        return fecha_nacimiento;
    }

    public String getId_facebook() {
        return id_facebook;
    }

    public String getId_crea() {
        return id_crea;
    }
}
