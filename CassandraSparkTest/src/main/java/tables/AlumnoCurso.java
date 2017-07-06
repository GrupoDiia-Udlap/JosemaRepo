package tables;

/**
 * Created by Thagus on 18/06/17.
 */
public class AlumnoCurso {
    private Alumno alumno;
    private Curso curso;

    public AlumnoCurso(Alumno alumno, Curso curso) {
        this.alumno = alumno;
        this.curso = curso;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public Curso getCurso() {
        return curso;
    }
}
