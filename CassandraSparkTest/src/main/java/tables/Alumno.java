package tables;

import com.github.javafaker.Faker;

import java.sql.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Thagus on 18/06/17.
 */
public class Alumno {
    private long id_alumno;
    private String nombre;
    private String apellidos;
    private char sexo;
    private Date fecha_nacimiento;
    private String perfil_academico;
    private String especialidad;
    private String email;
    private String telefono;

    private String id_facebook;
    private String id_crea;

    private String[] perfiles_academicos = {"licenciatura"};
    private String[] especialidades = {"docente"};

    public Alumno(long id_alumno) {
        Faker faker = new Faker(new Locale("es"));
        Random random = new Random();
        this.id_alumno = id_alumno;

        this.nombre = faker.name().firstName();
        this.apellidos = faker.name().lastName();
        this.sexo = faker.demographic().sex().charAt(0);

        //Alumnos de mas de 18 años
        this.fecha_nacimiento = new Date(faker.date().past(20*360, TimeUnit.DAYS, new Date(System.currentTimeMillis()-18*31556926000L)).getTime());

        this.perfil_academico = perfiles_academicos[random.nextInt(perfiles_academicos.length)];
        this.especialidad = especialidades[random.nextInt(especialidades.length)];

        this.email = faker.internet().emailAddress();
        this.telefono = faker.phoneNumber().phoneNumber();

        this.id_facebook = String.valueOf(faker.number().randomNumber(14, true));
        this.id_crea = String.valueOf(faker.number().randomNumber(10, true));
    }

    public long getId_alumno() {
        return id_alumno;
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

    public Date getFecha_nacimiento() {
        return fecha_nacimiento;
    }

    public String getPerfil_academico() {
        return perfil_academico;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getId_facebook() {
        return id_facebook;
    }

    public String getId_crea() {
        return id_crea;
    }

    public String[] getPerfiles_academicos() {
        return perfiles_academicos;
    }

    public String[] getEspecialidades() {
        return especialidades;
    }
}
