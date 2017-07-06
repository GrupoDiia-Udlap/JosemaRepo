package tables;

import java.util.Random;

/**
 * Created by Thagus on 18/06/17.
 */
public class Evaluacion {
    private Alumno alumno;
    private Material material;
    private float calificacion;

    public Evaluacion(Alumno alumno, Material material) {
        Random random = new Random();
        this.alumno = alumno;
        this.material = material;

        this.calificacion = random.nextFloat()*10;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public Material getMaterial() {
        return material;
    }

    public float getCalificacion() {
        return calificacion;
    }
}
