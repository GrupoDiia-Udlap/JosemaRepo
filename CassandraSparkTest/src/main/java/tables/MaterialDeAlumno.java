package tables;

import com.github.javafaker.Faker;

import java.sql.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Thagus on 18/06/17.
 */
public class MaterialDeAlumno {
    private long id_material_alumno;
    private Alumno alumno;
    private Curso curso;
    private Date fecha_creacion;
    private String contenido; //tema
    private String url_ubicacion;

    public MaterialDeAlumno(long id, Alumno alumno, Curso curso) {
        Faker faker = new Faker(new Locale("es"));
        Random random = new Random();

        this.id_material_alumno = id;
        this.alumno = alumno;
        this.curso = curso;

        //Obtener el periodo del curso para generar el material en ese rango
        this.fecha_creacion = new Date(curso.getRandomDateInRange().getTime());

        this.contenido = faker.lorem().characters(5, 20);

        this.url_ubicacion = faker.internet().url();

    }

    public long getId_material_alumno() {
        return id_material_alumno;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public Curso getCurso() {
        return curso;
    }

    public Date getFecha_creacion() {
        return fecha_creacion;
    }

    public String getContenido() {
        return contenido;
    }

    public String getUrl_ubicacion() {
        return url_ubicacion;
    }
}
