package models

abstract class Directional {

  final case class Students_All(idcourse: String)
  final case class StudentsMaterials_SM(idcourse: String)
  final case class All_All(idcourse: String)
  final case class Materials_Students(idcourse: String)
  final case class StudentsFaculty_SF(idcourse: String)
  final case class Unknown()

}
