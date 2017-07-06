package tables;

/**
 * Created by Thagus on 18/06/17.
 */
public class EscuelaDocente {
    private Escuela escuela;
    private Docente docente;

    public EscuelaDocente(Escuela escuela, Docente docente) {
        this.escuela = escuela;
        this.docente = docente;
    }

    public Escuela getEscuela() {
        return escuela;
    }

    public Docente getDocente() {
        return docente;
    }
}
